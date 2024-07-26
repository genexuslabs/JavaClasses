package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RandomTest {

	private static int l128;
	private static int l256;

	private static int l5;

	private static int l10;

	@BeforeClass
	public static void setUp() {
		l128 = 128;
		l256 = 256;
		l5 = 5;
		l10 = 10;
	}

	@Test
	public void testRandomNumeric() {
		String l5_string = GamUtilsEO.randomNumeric(l5);
		Assert.assertEquals("l5 numeric: ", l5, l5_string.length());

		String l10_string = GamUtilsEO.randomNumeric(l10);
		Assert.assertEquals("l10 numeric: ", l10, l10_string.length());

		String l128_string = GamUtilsEO.randomNumeric(l128);
		Assert.assertEquals("l128 numeric: ", l128, l128_string.length());

		String l256_string = GamUtilsEO.randomNumeric(l256);
		Assert.assertEquals("l256 numeric: ", l256, l256_string.length());

	}

	@Test
	public void testRandomAlphanumeric() {
		String l5_string = GamUtilsEO.randomAlphanumeric(l5);
		Assert.assertEquals("l5 alphanumeric: ", l5, l5_string.length());

		String l10_string = GamUtilsEO.randomAlphanumeric(l10);
		Assert.assertEquals("l10 alphanumeric: ", l10, l10_string.length());

		String l128_string = GamUtilsEO.randomAlphanumeric(l128);
		Assert.assertEquals("l128 alphanumeric: ", l128, l128_string.length());

		String l256_string = GamUtilsEO.randomAlphanumeric(l256);
		Assert.assertEquals("l256 alphanumeric: ", l256, l256_string.length());
	}
}
