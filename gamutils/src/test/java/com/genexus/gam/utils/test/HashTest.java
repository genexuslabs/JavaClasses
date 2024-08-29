package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.genexus.gam.utils.Random;
import com.genexus.gam.utils.test.resources.CryptographicHash;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HashTest {

	private static String one;
	private static String two;
	private static String three;
	private static String four;
	private static String five;

	private static CryptographicHash cryptographicHash;

	@BeforeClass
	public static void setUp() {
		one = "one";
		two = "two";
		three = "three";
		four = "four";
		five = "five";
		cryptographicHash = new CryptographicHash("SHA-512");
	}

	@Test
	public void testSha512() {
		Assert.assertEquals("one: ", cryptographicHash.ComputeHash(one), GamUtilsEO.sha512(one));
		Assert.assertEquals("two: ", cryptographicHash.ComputeHash(two), GamUtilsEO.sha512(two));
		Assert.assertEquals("three: ", cryptographicHash.ComputeHash(three), GamUtilsEO.sha512(three));
		Assert.assertEquals("four: ", cryptographicHash.ComputeHash(four), GamUtilsEO.sha512(four));
		Assert.assertEquals("five: ", cryptographicHash.ComputeHash(five), GamUtilsEO.sha512(five));
	}

	@Test
	public void testSha512Random() {
		for (int i = 0; i < 100; i++) {
			String value = Random.alphanumeric(15);
			Assert.assertEquals("random sha512 ", cryptographicHash.ComputeHash(value), GamUtilsEO.sha512(value));
		}
	}
	@Test
	public void testSha256()
	{
		String[] arrayInputs = new String[] {one, two, three, four, five};
		String[] arrayRes= new String[] {"dpLDrTVAu4A8Ags67mbNiIcSMjTqDG5xQ8Ct1z/0Me0=", "P8TM/nRYcOLA2Z9x8w/wZWyN7dQcwdfT03aw2+aF4vM=", "i1udsME9skJWyCmqNkqpDG0uujGLkjKkq5MTuVTTVV8=", "BO+vCA9aPnThwp0cpqSFaTgsu80yTo1Z0rg+8hwDnwA=", "IisL1R/O9+ZcLmLbLtZUVwE7q1a+b6/rGe4R1FMVPIA="};
		for(int i = 0; i < arrayInputs.length; i++)
		{
			Assert.assertEquals("testSha256 error", GamUtilsEO.sha256(arrayInputs[i]), arrayRes[i]);
		}
	}

}