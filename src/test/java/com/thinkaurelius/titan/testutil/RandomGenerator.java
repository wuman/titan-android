package com.thinkaurelius.titan.testutil;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RandomGenerator {

	private final static int standardLower = 7;
	private final static int standardUpper = 21;
	
	public static String[] randomStrings(int number) {
		return randomStrings(number,standardLower,standardUpper);
	}
	
	public static String[] randomStrings(int number, int lowerLen, int upperLen) {
		String[] ret = new String[number];
		for (int i=0;i<number;i++)
			ret[i]=randomString(lowerLen,upperLen);
		return ret;
	}
	
	public static String randomString() {
		return randomString(standardLower,standardUpper);
	}
	
	public static String randomString(int lowerLen, int upperLen) {
		assert lowerLen>0 && upperLen>=lowerLen;
		int length = randomInt(lowerLen,upperLen);
		StringBuilder s = new StringBuilder();
		for (int i=0;i<length;i++) {
			s.append((char)randomInt(97,120));
		}
		return s.toString();
	}
	
	/**
	 * Generate a pseudorandom number using Math.random().
	 * 
	 * @param lower minimum returned random number, inclusive
	 * @param upper maximum returned random number, exclusive
	 * @return the generated pseudorandom
	 */
	public static int randomInt(int lower, int upper) {
		assert upper>lower;
		int interval = upper - lower;
		// Generate a random int on [lower, upper)
		double rand = Math.floor(Math.random() * interval) + lower;
		// Shouldn't happen
		if (rand >= upper)
			rand = upper-1;
		// Cast and return
		return (int)rand;
	}
	
	/**
	 * Generate a pseudorandom number using Math.random().
	 * 
	 * @param lower minimum returned random number, inclusive
	 * @param upper maximum returned random number, exclusive
	 * @return the generated pseudorandom
	 */
	public static long randomLong(long lower, long upper) {
		assert upper>lower;
		long interval = upper - lower;
		// Generate a random int on [lower, upper)
		double rand = Math.floor(Math.random() * interval) + lower;
		// Shouldn't happen
		if (rand >= upper)
			rand = upper-1;
		// Cast and return
		return (long)rand;
	}
	
	@Test
	public void testRandomInt() {
		long sum = 0;
		int trials = 100000;
		for (int i=0;i<trials;i++) {
			sum += randomInt(1,101);
		}
		double avg = sum*1.0/trials;
		double error = (5/Math.pow(trials, 0.3));
		//System.out.println(error);
		assertTrue(Math.abs(avg-50.5)<error);
	}
	
	@Test
	public void testRandomLong() {
		long sum = 0;
		int trials = 100000;
		for (int i=0;i<trials;i++) {
			sum += randomLong(1,101);
		}
		double avg = sum*1.0/trials;
		double error = (5/Math.pow(trials, 0.3));
		//System.out.println(error);
		assertTrue(Math.abs(avg-50.5)<error);
	}
	
	@Test
	public void testRandomString() {
		for (int i=0;i<20;i++)
			System.out.println(randomString(5,20));
	}
	
}
