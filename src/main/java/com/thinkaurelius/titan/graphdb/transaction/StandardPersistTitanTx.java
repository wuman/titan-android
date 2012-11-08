package com.thinkaurelius.titan.graphdb.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cern.colt.list.AbstractLongList;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.thinkaurelius.titan.core.TitanException;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.diskstorage.StorageException;
import com.thinkaurelius.titan.diskstorage.TransactionHandle;
import com.thinkaurelius.titan.graphdb.database.InternalTitanGraph;
import com.thinkaurelius.titan.graphdb.query.AtomicQuery;
import com.thinkaurelius.titan.graphdb.relations.AttributeUtil;
import com.thinkaurelius.titan.graphdb.relations.InternalRelation;
import com.thinkaurelius.titan.graphdb.relations.factory.StandardPersistedRelationFactory;
import com.thinkaurelius.titan.graphdb.types.manager.TypeManager;
import com.thinkaurelius.titan.graphdb.vertices.InternalTitanVertex;
import com.thinkaurelius.titan.graphdb.vertices.factory.StandardVertexFactories;
import com.tinkerpop.blueprints.Vertex;

public class StandardPersistTitanTx extends AbstractTitanTx {

	private static final String LOG_TAG = StandardPersistTitanTx.class.getSimpleName();

	private final TransactionHandle txHandle;
		
	private Set<InternalRelation> deletedEdges;
	private List<InternalRelation> addedEdges;


	public StandardPersistTitanTx(InternalTitanGraph g, TypeManager etManage, TransactionConfig config,
                                  TransactionHandle tx) {
		super(g, StandardVertexFactories.DefaultPersisted,new StandardPersistedRelationFactory(),
				etManage,config);
		Preconditions.checkNotNull(g);
		txHandle = tx;

		if (config.isReadOnly()) {
			deletedEdges = ImmutableSet.of();
			addedEdges = ImmutableList.of();
		} else {
			deletedEdges = Collections.newSetFromMap(new ConcurrentHashMap<InternalRelation,Boolean>(10,0.75f,1));
			addedEdges = Collections.synchronizedList(new ArrayList<InternalRelation>());
		}
	}


	/* ---------------------------------------------------------------
	 * TitanVertex and TitanRelation creation
	 * ---------------------------------------------------------------
	 */

	@Override
	public boolean isDeletedRelation(InternalRelation relation) {
		if (relation.isRemoved()) return true;
		else return deletedEdges.contains(relation);
	}

	@Override
	public boolean containsVertex(long id) {
		if (super.containsVertex(id)) return true;
		else return graphdb.containsVertexID(id, this);
	}

	@Override
	public void deletedRelation(InternalRelation relation) {
		super.deletedRelation(relation);
		if (relation.isLoaded() && !relation.isInline()) {
			//Only store those deleted edges that matter, i.e. those that we need to erase from memory on their own		
			boolean success = deletedEdges.add(relation);
			assert success;
		}
	}
	

	@Override
	public void addedRelation(InternalRelation relation) {
		super.addedRelation(relation);
		if (!relation.isInline()) {
			//Only store those added edges that matter, i.e. those that we need to erase from memory on their own
			addedEdges.add(relation);
		}
		
	}
	
	@Override
	public void loadedRelation(InternalRelation relation) {
		super.loadedRelation(relation);
	}

	
	/* ---------------------------------------------------------------
	 * Index Handling
	 * ---------------------------------------------------------------
	 */	
	

	@Override
	public TitanVertex getVertex(TitanKey key, Object value) {
		TitanVertex node = super.getVertex(key, value);
		if (node==null && !key.isNew()) {
			//Look up
            value = AttributeUtil.prepareAttribute(value, key.getDataType());
			long[] ids = graphdb.indexRetrieval(value, key, this);
			if (ids.length==0) {
                //TODO Set NO-ENTRY
                return null;
            } else {
				assert ids.length==1;
				InternalTitanVertex n = getExistingVertex(ids[0]);
				addProperty2Index(key, value, n);
				return n;
			}
		} else return node;
	}
	
	@Override
	public long[] getVertexIDsFromDisk(TitanKey type, Object attribute) {
		Preconditions.checkArgument(type.hasIndex(),"Can only retrieve vertices for indexed property types.");
		if (!type.isNew()) {
			long[] ids = graphdb.indexRetrieval(attribute, type, this);
			return ids;
		} else return new long[0];
	}

    @Override
    public Iterable<Vertex> getVertices() {
        return Iterables.concat(super.getVertices(),new VertexIterable(graphdb,this));
    }
	
	/* ---------------------------------------------------------------
	 * TitanProperty / TitanRelation Loading
	 * ---------------------------------------------------------------
	 */	

	
	@Override
	public void loadRelations(AtomicQuery query) {
		graphdb.loadRelations(query, this);
	}
	
	@Override
	public AbstractLongList getRawNeighborhood(AtomicQuery query) {
		return graphdb.getRawNeighborhood(query, this);
	}
	
	private void clear() {
		addedEdges=null;
		deletedEdges=null;
	}

	@Override
	public synchronized void commit() {
        Preconditions.checkArgument(isOpen(),"The transaction has already been closed");
        
        try {
            if (!addedEdges.isEmpty() || !deletedEdges.isEmpty()) {
                graphdb.save(addedEdges, deletedEdges, this);
            }
            txHandle.commit();
            super.commit();
        } catch (StorageException e) {
            try {
                txHandle.abort();
                throw new TitanException("Could not commit transaction due to exception during persistence",e);
            } catch (StorageException s) {
                throw new TitanException("Failure while trying to abort a unsuccessfully committed transaction",s);
            } finally {
                super.abort();
            }
        } finally {
            clear();
        }
	}

	@Override
	public synchronized void abort() {
        Preconditions.checkArgument(isOpen(),"The transaction has already been closed");
        try {
		    txHandle.abort();
        } catch (StorageException e) {
            throw new TitanException("Could not abort transaction due to exception during persistence",e);
        } finally {
            super.abort();
            clear();
        }
	}
	
	@Override
	public TransactionHandle getTxHandle() {
		return txHandle;
	}

	@Override
	public boolean hasModifications() {
		return !getTxConfiguration().isReadOnly() && (!deletedEdges.isEmpty() || !addedEdges.isEmpty());
	}

}
