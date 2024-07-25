package com.genexus.cryptography.test.symmetric;

import com.genexus.cryptography.symmetric.SymmetricStreamCipher;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class StreamEncryptionTest extends SecurityAPITestObject {

	protected static String key8;
	protected static String key32;
	protected static String key128;
	protected static String key256;
	protected static String key1024;
	protected static String key6144;
	protected static String key8192;

	protected static String IV64;
	protected static String IV128;
	protected static String IV192;
	protected static String IV256;
	protected static String IV512;
	protected static String IV1024;
	protected static String IV6144;

	private static String plainText;

	private static String[] encodings;
	private static EncodingUtil eu;

	@Override
	protected void setUp() {

		plainText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam venenatis ex sit amet risus pellentesque, a faucibus quam ultrices. Ut tincidunt quam eu aliquam maximus. Quisque posuere risus at erat blandit eleifend. Curabitur viverra rutrum volutpat. Donec quis quam tellus. Aenean fermentum elementum augue, a semper risus scelerisque sit amet. Nullam vitae sapien vitae dui ullamcorper dapibus quis quis leo. Sed neque felis, pellentesque in risus et, lobortis ultricies nulla. Quisque quis quam risus. Donec vestibulum, lectus vel vestibulum eleifend, velit ante volutpat lacus, ut mattis quam ligula eget est. Sed et pulvinar lectus. In mollis turpis non ipsum vehicula, sit amet rutrum nibh dictum. Duis consectetur convallis ex, eu ultricies enim bibendum vel. Vestibulum vel libero nibh. Morbi nec odio mattis, vestibulum quam blandit, pretium orci.Aenean pellentesque tincidunt nunc a malesuada. Etiam gravida fermentum mi, at dignissim dui aliquam quis. Nullam vel lobortis libero. Phasellus non gravida posuere";
		SymmetricKeyGenerator keyGen = new SymmetricKeyGenerator();

		/**** GENERATE KEYS ****/
		key8 = keyGen.doGenerateKey("GENERICRANDOM", 8);
		key32 = keyGen.doGenerateKey("GENERICRANDOM", 32);
		key128 = keyGen.doGenerateKey("GENERICRANDOM", 128);
		key256 = keyGen.doGenerateKey("GENERICRANDOM", 256);
		key1024 = keyGen.doGenerateKey("GENERICRANDOM", 1024);
		key8192 = keyGen.doGenerateKey("GENERICRANDOM", 8192);
		key6144 = keyGen.doGenerateKey("GENERICRANDOM", 6144);

		/**** GENERATE IVs ****/
		IV64 = keyGen.doGenerateIV("GENERICRANDOM", 64);
		IV128 = keyGen.doGenerateIV("GENERICRANDOM", 128);
		IV192 = keyGen.doGenerateIV("GENERICRANDOM", 192);
		IV256 = keyGen.doGenerateIV("GENERICRANDOM", 256);
		IV512 = keyGen.doGenerateIV("GENERICRANDOM", 512);
		IV1024 = keyGen.doGenerateIV("GENERICRANDOM", 1024);
		IV6144 = keyGen.doGenerateIV("GENERICRANDOM", 6144);

		encodings = new String[] { "UTF_8", "UTF_16", "UTF_16BE", "UTF_16LE", "UTF_32", "UTF_32BE", "UTF_32LE", "SJIS",
			"GB2312" };

		eu = new EncodingUtil();

	}

	// HC256 key 256 bits, IV 128 o 256 bits
	// SALSA20 key 256 o 128 bits, 64 bit nonce

	public static Test suite() {
		return new TestSuite(StreamEncryptionTest.class);
	}

	@Override
	public void runTest() {
		testRC4();
		testSALSA20();
		testHC256();
		testCHACHA20();
		testXSALSA20();
		testISAAC();
	}

	private void testBulkAlgorithms(String algorithm, String key, String IV) {
		for (int i = 0; i < encodings.length; i++) {
			eu.setEncoding(encodings[i]);

			SymmetricStreamCipher symCipher = new SymmetricStreamCipher();
			String encrypted = symCipher.doEncrypt(algorithm, key, IV, plainText);
			String decrypted = symCipher.doDecrypt(algorithm, key, IV, encrypted);
			assertTrue(SecurityUtils.compareStrings(plainText, decrypted));

			True(SecurityUtils.compareStrings(plainText, decrypted), symCipher);
		}
	}

	public void testRC4() {
		// RC4 key 1024, no nonce
		testBulkAlgorithms("RC4", key1024, "");
	}
	public void testHC256() {
		// HC256 key 256 bits, IV 128 o 256 bits
		testBulkAlgorithms("HC256", key256, IV128);
		testBulkAlgorithms("HC256", key256, IV256);

	}

	public void testSALSA20() {
		// SALSA20 key 256 o 128 bits, 64 bit nonce
		testBulkAlgorithms("SALSA20", key128, IV64);
		testBulkAlgorithms("SALSA20", key256, IV64);

	}


	public void testCHACHA20() {
		// CHACHA key 128 o 256, IV 64 bits
		testBulkAlgorithms("CHACHA20", key128, IV64);
		testBulkAlgorithms("CHACHA20", key256, IV64);
	}


	public void testXSALSA20() {
		// SALSA20 key 256 bits, 192 bit nonce
		testBulkAlgorithms("XSALSA20", key256, IV192);
	}

	public void testISAAC() {
		// ISAAC 32, 8192 key, no nonce
		testBulkAlgorithms("ISAAC", key32, "");
		testBulkAlgorithms("ISAAC", key8192, "");
	}

}
