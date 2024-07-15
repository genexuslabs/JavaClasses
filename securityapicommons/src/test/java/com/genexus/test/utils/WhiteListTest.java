package com.genexus.test.utils;

import com.genexus.securityapicommons.utils.ExtensionsWhiteList;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class WhiteListTest extends SecurityAPITestObject {

	private String pathWithPoints;
	private String pathWithPointsFail;
	private String pathWithoutPoints;
	private String pathWithoutPointsFail;
	private ExtensionsWhiteList list;

	public static Test suite() {
		return new TestSuite(WhiteListTest.class);
	}

	@Override
	public void runTest() {
		testWithPoints();
		testWithoutPoints();
	}

	@Override
	public void setUp() {
		pathWithPoints = "C:\\Temp\\file.txt";
		pathWithoutPoints = "C:\\Temp\\ftpstest.txt";
		pathWithPointsFail = "C:\\Temp\\ftpstest.pdf";
		pathWithoutPointsFail = "C:\\Temp\\ftpstest.pdf";
		list = new ExtensionsWhiteList();
		list.setExtension(".txt");
	}

	public void testWithPoints() {
		boolean res1 = list.isValid(pathWithPoints);
		assertTrue(res1);
		boolean res2 = list.isValid(pathWithPointsFail);
		assertFalse(res2);

	}

	public void testWithoutPoints() {
		boolean res1 = list.isValid(pathWithoutPoints);
		assertTrue(res1);
		boolean res2 = list.isValid(pathWithoutPointsFail);
		assertFalse(res2);
	}

}