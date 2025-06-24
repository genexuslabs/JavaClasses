package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.genexus.gam.utils.Pkce;
import org.junit.Assert;
import org.junit.Test;

import java.text.MessageFormat;

public class PkceTest {

	@Test
	public void testPkceS256()
	{
		int i = 0;
		while (i<50)
		{
			String[] s256_true = GamUtilsEO.pkce_create(20, "S256").split(",");
			Assert.assertTrue("testPkceS256 true", GamUtilsEO.pkce_verify(s256_true[0], s256_true[1], "S256"));

			String[] s256_false = GamUtilsEO.pkce_create(20, "S256").split(",");
			Assert.assertFalse("testPkceS256 false", GamUtilsEO.pkce_verify(MessageFormat.format("{0}tralala",s256_false[0]), s256_false[1], "S256"));
			i++;
		}
	}

	@Test
	public void testPkcePlain()
	{
		int i = 0;
		while (i<50)
		{
			String[] plain_true = GamUtilsEO.pkce_create(20, "PLAIN").split(",");
			Assert.assertTrue("testPkceS256", GamUtilsEO.pkce_verify(plain_true[0], plain_true[1], "PLAIN"));

			String[] plain_false = GamUtilsEO.pkce_create(20, "PLAIN").split(",");
			Assert.assertFalse("testPkceS256 false", GamUtilsEO.pkce_verify(MessageFormat.format("{0}tralala",plain_false[0]), plain_false[1], "PLAIN"));
			i++;
		}
	}

}
