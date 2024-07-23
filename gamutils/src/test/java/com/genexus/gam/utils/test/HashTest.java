package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.genexus.gam.utils.Random;
import com.genexus.gam.utils.test.resources.CryptographicHash;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

public class HashTest extends TestCase {

	private static String one;
	private static String two;
	private static String three;
	private static String four;
	private static String five;

	private CryptographicHash cryptographicHash;


	public static Test suite() {
		return new TestSuite(HashTest.class);
	}

	@Override
	public void runTest() {
		testSha512();
		testSha512Random();
	}

	@Override
	public void setUp() {
		one = "one";
		two = "two";
		three = "three";
		four = "four";
		five = "five";
		cryptographicHash = new CryptographicHash("SHA-512");
	}


	public void testSha512() {
		Assert.assertEquals("one: ", cryptographicHash.ComputeHash(one), GamUtilsEO.sha512(one));
		Assert.assertEquals("two: ", cryptographicHash.ComputeHash(two), GamUtilsEO.sha512(two));
		Assert.assertEquals("three: ", cryptographicHash.ComputeHash(three), GamUtilsEO.sha512(three));
		Assert.assertEquals("four: ", cryptographicHash.ComputeHash(four), GamUtilsEO.sha512(four));
		Assert.assertEquals("five: ", cryptographicHash.ComputeHash(five), GamUtilsEO.sha512(five));
	}

	public void testSha512Random()
	{
		for(int i = 0; i <100; i++)
		{
			String value = Random.randomAlphanumeric(15);
			Assert.assertEquals("random sha512 ", cryptographicHash.ComputeHash(value), GamUtilsEO.sha512(value));
		}
	}

}