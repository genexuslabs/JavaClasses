package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.genexus.gam.utils.Encoding;
import com.genexus.gam.utils.Random;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.UrlBase64;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class EncodingTest {

	@Test
	public void testB64ToB64Url() {
		int i = 0;
		do {
			String randomString = GamUtilsEO.randomAlphanumeric(128);
			String testing = GamUtilsEO.base64ToBase64Url(Base64.toBase64String(randomString.getBytes(StandardCharsets.UTF_8)));
			Assert.assertEquals("testB64ToB64Url", randomString, b64UrlToUtf8(testing));
			i++;
		} while (i < 50);
	}

	private static String b64UrlToUtf8(String base64Url) {
		try {
			byte[] bytes = UrlBase64.decode(base64Url);
			return new String(bytes, StandardCharsets.UTF_8).replaceAll("[\ufffd]", "");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Test
	public void testToBase64Url() {
		int i = 0;
		do {
			String randomString = GamUtilsEO.randomAlphanumeric(128);
			String testing = GamUtilsEO.toBase64Url(randomString);
			Assert.assertEquals("testB64ToB64Url", randomString, GamUtilsEO.fromBase64Url(testing));
			i++;
		} while (i < 50);
	}

	@Test
	public void testHexaToBase64()
	{
		int i = 0;
		do {
			String randomHexa = Random.hexaBits(128);
			String testing = b64ToHexa(Encoding.hexaToBase64(randomHexa));
			Assert.assertEquals("testB64ToB64Url", randomHexa, testing);
			i++;
		} while (i < 50);
	}

	private static String b64ToHexa(String base64) {
		try {
			byte[] bytes = Base64.decode(base64);
			return Hex.toHexString(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


}
