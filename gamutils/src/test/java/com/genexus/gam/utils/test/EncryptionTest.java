package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import org.junit.Assert;
import org.junit.Test;

public class EncryptionTest {

	@Test
	public void testAesGcm() {
		String key = GamUtilsEO.randomHexaBits(256);
		String nonce = GamUtilsEO.randomHexaBits(128);
		String txt = "hello world";
		int macSize = 64;
		String encrypted = GamUtilsEO.AesGcm(txt, key, nonce, macSize, true);
		Assert.assertFalse("testAesGcm encrypt", encrypted.isEmpty());
		String decrypted = GamUtilsEO.AesGcm(encrypted, key, nonce, macSize, false);
		Assert.assertEquals("testAesGcm decrypt", txt, decrypted);
	}
}
