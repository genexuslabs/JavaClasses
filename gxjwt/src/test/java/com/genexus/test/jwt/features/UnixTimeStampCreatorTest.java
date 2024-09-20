package com.genexus.test.jwt.features;

import com.genexus.JWT.utils.UnixTimestampCreator;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class UnixTimeStampCreatorTest extends SecurityAPITestObject {

	protected static String date;
	protected static UnixTimestampCreator creator;

	protected static String expected;


	@BeforeClass
	public static void setUp() {
		date = "2023/07/19 11:41:00";
		creator = new UnixTimestampCreator();
		expected = "1689766860";
	}


	@Test
	public void testCreate() {
		String obtained = creator.create(date);
		Equals(expected, obtained, creator);
	}
}
