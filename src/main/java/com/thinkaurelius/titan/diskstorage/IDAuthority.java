package com.thinkaurelius.titan.diskstorage;

import com.thinkaurelius.titan.graphdb.database.idassigner.IDBlockSizer;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface IDAuthority {

    /**
     * Returns an array that specifies a block of ids, i.e. the ids between return[0] (inclusive) and return[1] (exclusive).
     * It is guaranteed that the block of ids for the particular partition id is uniquely assigned, that is,
     * the block of ids has not been previously and will not subsequently be assigned again when invoking this method
     * on the local or any remote machine that is connected to the underlying storage backend.
     *
     * In other words, this method has to ensure that ids are uniquely assigned per partition.
     *
     * @param partition Partition for which to request an id block. Must be bigger or equal to 0
     * @return a range of ids for the particular partition
     */
    public long[] getIDBlock(int partition) throws StorageException;

    /**
     * Sets the {@link IDBlockSizer} to be used by this IDAuthority. The IDBlockSizer specifies the block size for 
     * each partition guaranteeing that the same partition will always be assigned the same block size.
     * 
     * The IDBlockSizer cannot be changed for an IDAuthority that has already been used (i.e. after invoking {@link #getIDBlock(int)}.
     * 
     * @param sizer The IDBlockSizer to be used by this IDAuthority
     */
    public void setIDBlockSizer(IDBlockSizer sizer);

}
