package com.genexus.db.driver;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestExternalProviderCommon {

	@Test
	public void testGetProviderNormalizedUrl(){
		String url = "http://myhost.com/base/myresource.png";
		String result = ExternalProviderCommon.getProviderObjectAbsoluteUriSafe(null, url);
		assertEquals(result, url);

		url = "/serverRelativeUrl/myresource.png";
		result = ExternalProviderCommon.getProviderObjectAbsoluteUriSafe(null, url);
		assertEquals(result, url);

	}
}
