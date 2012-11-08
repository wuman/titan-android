package com.thinkaurelius.titan.graphdb.serializer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.thinkaurelius.titan.diskstorage.util.ByteBufferUtil;
import com.thinkaurelius.titan.testutil.RandomGenerator;

public class ByteBufferTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testEndian() {
		ByteBuffer b = ByteBuffer.allocate(10);
		assertEquals(b.order(),ByteOrder.BIG_ENDIAN);
	}
	
	@Test
	public void byteBufferConstruct() {
		ByteBuffer b = ByteBufferUtil.getLongByteBuffer(131);
		assertTrue(b.hasRemaining());
		assertEquals(b.remaining(),8);
		assertEquals(b.position(),0);
		assertEquals(b.limit(),8);
	}
	
	@Test
	public void byteBufferComparison() {
		int trails = 10000;
		int width = 600;
		for (int i=0;i<trails;i++) {
			long id = RandomGenerator.randomLong(width + 1, Long.MAX_VALUE / 2);
			ByteBuffer constant = ByteBufferUtil.getLongByteBuffer(id);
			for (int off=-width;off<=width;off+=5) {
				ByteBuffer compare = ByteBufferUtil.getLongByteBuffer(id+off);
				//System.out.println("+" + id + " vs +" + (id+off));
				if (off>0) {
					assertTrue(ByteBufferUtil.isSmallerThan(constant, compare));
				} else {
					assertFalse(ByteBufferUtil.isSmallerThan(constant, compare));
				}
				//constant.rewind();
			}
		}
	}
	
	
	@After
	public void tearDown() throws Exception {
	}

}
