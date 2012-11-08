package com.thinkaurelius.titan.diskstorage;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.thinkaurelius.titan.diskstorage.util.ByteBufferUtil;
import com.thinkaurelius.titan.diskstorage.util.RecordIterator;
import com.thinkaurelius.titan.graphdb.database.serialize.DataOutput;
import com.thinkaurelius.titan.graphdb.database.serialize.Serializer;
import com.thinkaurelius.titan.graphdb.database.serialize.kryo.KryoSerializer;
import com.thinkaurelius.titan.testutil.RandomGenerator;

public class KeyValueStoreUtil {

	public static final Serializer serial = new KryoSerializer(true);
	public static final long idOffset = 1000;
	
	public static String[] generateData(int numKeys) {
		String[] ret = new String[numKeys];
		for (int i=0;i<numKeys;i++) {
			ret[i]=RandomGenerator.randomString();
		}
		return ret;
	}
	
	public static String[][] generateData(int numKeys, int numColumns) {
		String[][] ret = new String[numKeys][numColumns];
		for (int i=0;i<numKeys;i++) {
			for (int j=0;j<numColumns;j++) {
				ret[i][j]= RandomGenerator.randomString();
			}
		}
		return ret;
	}
	
	public static void print(String[] data) {
		System.out.println(Arrays.toString(data));
	}
	
	public static void print(String[][] data) {
		for (int i=0;i<data.length;i++) print(data[i]);
	}
	
	public static ByteBuffer getBuffer(int no) {
		return ByteBufferUtil.getLongByteBuffer(no+idOffset);
	}
	
	public static int getID(ByteBuffer b) {
		long res = b.getLong()-idOffset;
		assertTrue(res>=0 && res<Integer.MAX_VALUE);
		return (int)res;
	}
	
	public static ByteBuffer getBuffer(String s) {
		DataOutput out = serial.getDataOutput(50, true);
		out.writeObjectNotNull(s);
		return out.getByteBuffer();
	}
	
	public static String getString(ByteBuffer b) {
		return serial.readObjectNotNull(b, String.class);
	}
    
    public static int count(RecordIterator<?> iterator) throws StorageException {
        int count=0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }
	
}
