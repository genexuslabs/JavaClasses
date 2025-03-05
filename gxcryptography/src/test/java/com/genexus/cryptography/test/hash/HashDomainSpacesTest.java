package com.genexus.cryptography.test.hash;

import com.genexus.cryptography.hash.Hashing;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class HashDomainSpacesTest extends SecurityAPITestObject {

	protected static String plainText;
	protected static String SHA1Digest = "38F00F8738E241DAEA6F37F6F55AE8414D7B0219";
	protected static Hashing hash;

	@BeforeClass
	public static void setUp() {
		SHA1Digest = "38F00F8738E241DAEA6F37F6F55AE8414D7B0219";
		hash = new Hashing();
		plainText = "Lorem ipsum dolor sit amet";
	}

	@Test
	public void testSpaces() {
		String digest = hash.doHash(" SHA1", plainText);
		assertFalse(hash.hasError());
		assertTrue(SecurityUtils.compareStrings(digest, SHA1Digest));
	}
}
