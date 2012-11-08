package com.thinkaurelius.titan.diskstorage.berkeleydb.je;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Transaction;
import com.thinkaurelius.titan.diskstorage.PermanentStorageException;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.TransactionHandle;
import com.thinkaurelius.titan.diskstorage.util.RecordIterator;

public class BerkeleyJETxHandle implements TransactionHandle {

	private Transaction tx;
	private final boolean isReadOnly;
    private Set<RecordIterator> openIterators = null;
	
	public BerkeleyJETxHandle(Transaction t, boolean readOnly) {
		isReadOnly = readOnly;
		tx = t;
	}
	
	public BerkeleyJETxHandle(Transaction t) {
		this(t,false);
	}
	
	
	public Transaction getTransaction() {
		return tx;
	}

    synchronized void registerIterator(RecordIterator<?> iterator) {
        if (openIterators==null) openIterators = new HashSet<RecordIterator>();
        openIterators.add(iterator);
    }
    
    synchronized void unregisterIterator(RecordIterator<?> iterator) {
        if (openIterators!=null) openIterators.remove(iterator);
    }
    
    private void closeOpenIterators() throws StorageException {
        if (openIterators!=null) {
            for (RecordIterator iterator : Lists.newArrayList(openIterators)) { //copied to avoid ConcurrentmodificationException
                iterator.close();
            }
            assert openIterators.isEmpty();
        }
    }
	
	@Override
	public synchronized void abort() throws StorageException {
	    if (tx==null) return;
		try {
            closeOpenIterators();
	    	tx.abort();
	    	tx=null;
		} catch(DatabaseException e) {
			throw new PermanentStorageException(e);
		}
	}
	
	@Override
	public synchronized void commit() throws StorageException {
	    if (tx==null) return;
		try {
            closeOpenIterators();
            tx.commit();
            tx=null;
		} catch(DatabaseException e) {
            throw new PermanentStorageException(e);
		}		
	}


	@Override
	public boolean isReadOnly() {
		return isReadOnly;
	}

}
