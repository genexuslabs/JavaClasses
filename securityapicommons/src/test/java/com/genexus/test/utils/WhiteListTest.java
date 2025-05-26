package com.genexus.test.utils;

import com.genexus.securityapicommons.utils.ExtensionsWhiteList;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class WhiteListTest extends SecurityAPITestObject {

	private static String pathWithPoints;
	private static String pathWithPointsFail;
	private static String pathWithoutPoints;
	private static String pathWithoutPointsFail;
	private static ExtensionsWhiteList list;


	@BeforeClass
	public static void setUp() {
		pathWithPoints = "C:\\Temp\\file.txt";
		pathWithoutPoints = "C:\\Temp\\ftpstest.txt";
		pathWithPointsFail = "C:\\Temp\\ftpstest.pdf";
		pathWithoutPointsFail = "C:\\Temp\\ftpstest.pdf";
		list = new ExtensionsWhiteList();
		list.setExtension(".txt");
	}

	@Test
	public void testWithPoints() {
		boolean res1 = list.isValid(pathWithPoints);
		assertTrue(res1);
		boolean res2 = list.isValid(pathWithPointsFail);
		assertFalse(res2);

	}

	@Test
	public void testWithoutPoints() {
		boolean res1 = list.isValid(pathWithoutPoints);
		assertTrue(res1);
		boolean res2 = list.isValid(pathWithoutPointsFail);
		assertFalse(res2);
	}

}
