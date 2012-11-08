package com.thinkaurelius.titan.diskstorage;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thinkaurelius.titan.diskstorage.util.KeyValueEntry;
import com.thinkaurelius.titan.diskstorage.util.KeyValueStorageManager;
import com.thinkaurelius.titan.diskstorage.util.OrderedKeyValueStore;
import com.thinkaurelius.titan.diskstorage.util.RecordIterator;
import com.thinkaurelius.titan.diskstorage.util.ScanKeyValueStore;

public abstract class KeyValueStoreTest {

	private int numKeys = 2000;
    private String storeName = "testStore1";

	
	protected KeyValueStorageManager manager;
	protected TransactionHandle tx;
	protected OrderedKeyValueStore store;
	
	@Before
	public void setUp() throws Exception {
        openStorageManager().clearStorage();
		open();
	}
	
	public void open() throws StorageException {
        manager = openStorageManager();
        tx = manager.beginTransaction();
        store = manager.openDatabase(storeName);
    }

    public abstract KeyValueStorageManager openStorageManager() throws StorageException;
	
	@After
	public void tearDown() throws Exception {
		close();
	}

    public void close() throws StorageException {
        if (tx!=null) tx.commit();
        store.close();
        manager.close();
    }
    
    public void clopen() throws StorageException {
        close();
        open();
    }

	@Test
	public void createDatabase() {
		//Just setup and shutdown
	}
	
	
	public String[] generateValues() {
		return KeyValueStoreUtil.generateData(numKeys);
	}
	
	public void loadValues(String[] values) throws StorageException {
		List<KeyValueEntry> entries = new ArrayList<KeyValueEntry>();
		for (int i=0;i<numKeys;i++) {
			entries.add(new KeyValueEntry(KeyValueStoreUtil.getBuffer(i), KeyValueStoreUtil.getBuffer(values[i])));
		}
		store.insert(entries, tx);
	}
	
	public Set<Integer> deleteValues(int start, int every) throws StorageException {
		Set<Integer> removed = new HashSet<Integer>();
		List<ByteBuffer> keys = new ArrayList<ByteBuffer>();
		for (int i=start;i<numKeys;i=i+every) {
			removed.add(i);
			keys.add(KeyValueStoreUtil.getBuffer(i));
		}
		store.delete(keys, tx);
		return removed;
	}
	
	public void checkValueExistence(String[] values) throws StorageException {
		checkValueExistence(values,new HashSet<Integer>());
	}
	
	public void checkValueExistence(String[] values, Set<Integer> removed) throws StorageException {
		for (int i=0;i<numKeys;i++) {
			boolean result = store.containsKey(KeyValueStoreUtil.getBuffer(i), tx);
			if (removed.contains(i)) {
				assertFalse(result);
			} else {
				assertTrue(result);
			}
		}
	}
	
	public void checkValues(String[] values) throws StorageException {
		checkValues(values,new HashSet<Integer>());
	}
	
	public void checkValues(String[] values, Set<Integer> removed) throws StorageException {
		for (int i=0;i<numKeys;i++) {
			ByteBuffer result = store.get(KeyValueStoreUtil.getBuffer(i), tx);
			if (removed.contains(i)) {
				assertNull(result);
			} else {
				Assert.assertEquals(values[i], KeyValueStoreUtil.getString(result));
			}
		}
	}
	
	@Test
	public void storeAndRetrieve() throws StorageException {
		String[] values = generateValues();
		System.out.println("Loading values...");
		loadValues(values);

		System.out.println("Checking values...");
		checkValueExistence(values);
		checkValues(values);
	}
	
	@Test
	public void storeAndRetrieveWithClosing() throws StorageException {
		String[] values = generateValues();
		System.out.println("Loading values...");
		loadValues(values);
		clopen();
		System.out.println("Checking values...");
		checkValueExistence(values);
		checkValues(values);
	}
	
	@Test
	public void deletionTest1() throws StorageException {
		String[] values = generateValues();
		System.out.println("Loading values...");
		loadValues(values);
		clopen();
		Set<Integer> deleted = deleteValues(0,10);
		System.out.println("Checking values...");
		checkValueExistence(values,deleted);
		checkValues(values,deleted);
	}
	
	@Test
	public void deletionTest2() throws StorageException {
		String[] values = generateValues();
		System.out.println("Loading values...");
		loadValues(values);
		Set<Integer> deleted = deleteValues(0,10);
		clopen();
		System.out.println("Checking values...");
		checkValueExistence(values,deleted);
		checkValues(values,deleted);
	}
    
    @Test
    public void scanTest() throws StorageException {
        if (manager.getFeatures().supportsScan()) {
            ScanKeyValueStore scanstore = (ScanKeyValueStore)store;
            String[] values = generateValues();
            loadValues(values);
            RecordIterator<ByteBuffer> iterator0 = scanstore.getKeys(tx);
            assertEquals(numKeys,KeyValueStoreUtil.count(iterator0));
            clopen();
            scanstore = (ScanKeyValueStore)store;
            RecordIterator<ByteBuffer> iterator1 = scanstore.getKeys(tx);
            RecordIterator<ByteBuffer> iterator2 = scanstore.getKeys(tx);
            RecordIterator<ByteBuffer> iterator3 = scanstore.getKeys(tx);
            assertEquals(numKeys,KeyValueStoreUtil.count(iterator1));
            assertEquals(numKeys,KeyValueStoreUtil.count(iterator2));
        }
    }
	
	public void checkSlice(String[] values, Set<Integer> removed, int start, int end, int limit) throws StorageException {
		List<KeyValueEntry> entries;
		if (limit<=0)
			entries = store.getSlice(KeyValueStoreUtil.getBuffer(start), KeyValueStoreUtil.getBuffer(end), tx);
		else
			entries = store.getSlice(KeyValueStoreUtil.getBuffer(start), KeyValueStoreUtil.getBuffer(end), limit, tx);
		
		int pos=0;
		for (int i=start;i<end;i++) {
			if (removed.contains(i)) continue;
			if (pos<limit) {
			KeyValueEntry entry = entries.get(pos);
			int id = KeyValueStoreUtil.getID(entry.getKey());
			String str = KeyValueStoreUtil.getString(entry.getValue());
			assertEquals(i,id);
			assertEquals(values[i],str);
			}
			pos++;
		}
		if (limit>0 && pos>=limit) assertEquals(limit,entries.size());
		else {
			assertNotNull(entries);
			assertEquals(pos,entries.size());
		}		
	}
	
	@Test
	public void intervalTest1() throws StorageException {
		String[] values = generateValues();
		System.out.println("Loading values...");
		loadValues(values);
		Set<Integer> deleted = deleteValues(0,10);
		clopen();
		checkSlice(values,deleted,5,25,-1);
		checkSlice(values,deleted,5,250,10);
		checkSlice(values,deleted,500,1250,-1);
		checkSlice(values,deleted,500,1250,1000);
		checkSlice(values,deleted,500,1250,100);
		checkSlice(values,deleted,50,20,10);
		checkSlice(values,deleted,50,20,-1);
	
	}



}
 