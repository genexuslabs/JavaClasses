package com.genexus.test.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.utils.SecurityUtils;

import junit.framework.TestCase;

public class SecurityAPITestObject extends TestCase {

	public String resources;


	public SecurityAPITestObject() {
		resources = System.getProperty("user.dir").concat("/src/test/resources");
	}

	public void True(boolean result, SecurityAPIObject object)
	{
		assertTrue(result);
		assertFalse(object.hasError());
	}

	public void False(boolean result, SecurityAPIObject object)
	{
		assertFalse(result);
		assertTrue(object.hasError());
	}

	public void Equals(String expected, String obtained, SecurityAPIObject object)
	{
		assertTrue(SecurityUtils.compareStrings(expected, obtained) && !object.hasError());
	}
}
