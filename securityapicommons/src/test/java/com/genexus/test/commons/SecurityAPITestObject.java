package com.genexus.test.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

public class SecurityAPITestObject {

	public static final String resources = System.getProperty("user.dir").concat("/src/test/resources");
	;

	@ClassRule
	public static final TemporaryFolder tempFolder = new TemporaryFolder();

	public static void True(boolean result, SecurityAPIObject object) {
		Assert.assertTrue(result);
		Assert.assertFalse(object.hasError());
	}

	public static void False(boolean result, SecurityAPIObject object) {
		Assert.assertFalse(result);
		Assert.assertTrue(object.hasError());
	}

	public void Equals(String expected, String obtained, SecurityAPIObject object) {
		Assert.assertTrue(SecurityUtils.compareStrings(expected, obtained) && !object.hasError());
	}

	public static void assertTrue(boolean condition) {
		Assert.assertTrue(condition);
	}

	public static void assertFalse(boolean condition) {
		Assert.assertFalse(condition);
	}
}
