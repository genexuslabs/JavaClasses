package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.genexus.gam.utils.Encoding;
import com.genexus.gam.utils.Random;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

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
			return new String(java.util.Base64.getUrlDecoder().decode(base64Url), StandardCharsets.ISO_8859_1);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Test
	public void testBase64Url() {
		String[] utf8 = new String[]{"GQTuYnnS9AbcKXndwxiZbxk4Q60nhuEd", "rf7tZx8aWO28YOKLISDWY33HuarNHkIZ", "sF7Ic0iuZxE50nz3W5Jnj7R0nQlRD0b1", "GGKmW2ubkhnA9ASaVlVAKM6FQdPCQ1pj", "LMW0GSCVyeGiGzf84eIwuX6OHAfur9fp", "zq9Kni7W1r0UIzG9hjYeiqJhSYlWVZSa", "WcyhGLQNyQkP2YmOjVtIilpqcHgYCzjq", "DuhO4PBiXRDDj50RBRo8wNUU8R3UXbp0", "pkPfYXOyoLUsEwm4HjjDB6E2c3aUjYNh", "fgbrZoKKMym9HN5zlKj0a8ohgQlJm3PM", "owGXQ7p6BeFeK1KFVOsdbSRd0sMwgFRU"};
		String[] b64 = new String[]{"R1FUdVlublM5QWJjS1huZHd4aVpieGs0UTYwbmh1RWQ", "cmY3dFp4OGFXTzI4WU9LTElTRFdZMzNIdWFyTkhrSVo", "c0Y3SWMwaXVaeEU1MG56M1c1Sm5qN1IwblFsUkQwYjE", "R0dLbVcydWJraG5BOUFTYVZsVkFLTTZGUWRQQ1ExcGo", "TE1XMEdTQ1Z5ZUdpR3pmODRlSXd1WDZPSEFmdXI5ZnA", "enE5S25pN1cxcjBVSXpHOWhqWWVpcUpoU1lsV1ZaU2E", "V2N5aEdMUU55UWtQMlltT2pWdElpbHBxY0hnWUN6anE", "RHVoTzRQQmlYUkREajUwUkJSbzh3TlVVOFIzVVhicDA", "cGtQZllYT3lvTFVzRXdtNEhqakRCNkUyYzNhVWpZTmg", "ZmdiclpvS0tNeW05SE41emxLajBhOG9oZ1FsSm0zUE0", "b3dHWFE3cDZCZUZlSzFLRlZPc2RiU1JkMHNNd2dGUlU"};
		for (int i = 0; i < utf8.length; i++) {
			Assert.assertEquals(MessageFormat.format("testBase64Url toBase64Url fail index: {0}", i), b64[i], Encoding.toBase64Url(utf8[i]));
			Assert.assertEquals(MessageFormat.format("testBase64Url fromBase64Url fail index: {0}", i), utf8[i], Encoding.fromBase64Url(b64[i]));
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
