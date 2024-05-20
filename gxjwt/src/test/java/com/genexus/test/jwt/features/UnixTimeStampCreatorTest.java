package com.genexus.test.jwt.features;

import com.genexus.JWT.utils.UnixTimestampCreator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class UnixTimeStampCreatorTest extends SecurityAPITestObject {

	protected static String date;
	protected static UnixTimestampCreator creator;

	protected static String expected;

	public static Test suite() {
		return new TestSuite(UnixTimeStampCreatorTest.class);
	}

	@Override
	public void runTest() {
		testCreate();
	}



	@Override
	public void setUp() {
		date = "2023/07/19 11:41:00";
		creator = new UnixTimestampCreator();
		expected = "1689766860";
	}


	public void testCreate()
	{
		String obtained = creator.create(date);
		Equals(expected, obtained, creator);
	}
}
