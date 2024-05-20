package com.genexus.cryptography.test.mac;

import com.genexus.cryptography.mac.Cmac;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CmacTest extends SecurityAPITestObject{
	private static String plainText;
	private static String plainTextCTS;

	protected static String key1024;
	protected static String key512;
	protected static String key448;
	protected static String key256;
	protected static String key192;
	protected static String key160;
	protected static String key128;
	protected static String key64;

	protected static String IV128;
	protected static String IV64;

	private static SymmetricKeyGenerator keyGen;

	private static String[] encodings;
	private static EncodingUtil eu;

	@Override
	protected void setUp()
	{
		plainText = "Neque porro quisquam est qui dolorem ipsum quia dolor sit amet";
		plainTextCTS = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam venenatis ex sit amet risus pellentesque, a faucibus quam ultrices. Ut tincidunt quam eu aliquam maximus. Quisque posuere risus at erat blandit eleifend. Curabitur viverra rutrum volutpat. Donec quis quam tellus. Aenean fermentum elementum augue, a semper risus scelerisque sit amet. Nullam vitae sapien vitae dui ullamcorper dapibus quis quis leo. Sed neque felis, pellentesque in risus et, lobortis ultricies nulla. Quisque quis quam risus. Donec vestibulum, lectus vel vestibulum eleifend, velit ante volutpat lacus, ut mattis quam ligula eget est. Sed et pulvinar lectus. In mollis turpis non ipsum vehicula, sit amet rutrum nibh dictum. Duis consectetur convallis ex, eu ultricies enim bibendum vel. Vestibulum vel libero nibh. Morbi nec odio mattis, vestibulum quam blandit, pretium orci.Aenean pellentesque tincidunt nunc a malesuada. Etiam gravida fermentum mi, at dignissim dui aliquam quis. Nullam vel lobortis libero. Phasellus non gravida posuere";

		keyGen = new SymmetricKeyGenerator();

		/**** CREATE KEYS ****/
		key1024 = keyGen.doGenerateKey("GENERICRANDOM", 1024);
		key512 = keyGen.doGenerateKey("GENERICRANDOM", 512);
		key448 = keyGen.doGenerateKey("GENERICRANDOM", 448);
		key256 = keyGen.doGenerateKey("GENERICRANDOM", 256);
		key160 = keyGen.doGenerateKey("GENERICRANDOM", 160);
		key192 = keyGen.doGenerateKey("GENERICRANDOM", 192);
		key128 = keyGen.doGenerateKey("GENERICRANDOM", 128);
		key64 = keyGen.doGenerateKey("GENERICRANDOM", 64);

		/**** CREATE IVs ****/
		IV128 = keyGen.doGenerateIV("GENERICRANDOM", 128);
		IV64 = keyGen.doGenerateIV("GENERICRANDOM", 64);


		encodings = new String[] {"UTF_8", "UTF_16", "UTF_16BE", "UTF_16LE", "UTF_32", "UTF_32BE", "UTF_32LE","SJIS", "GB2312" };
		eu = new EncodingUtil();

	}

	private void testBulkAlgorithm(String algorithm, String key, String IV, int macSize, boolean cts)
	{
		for (int i = 0; i < encodings.length; i++) {
			eu.setEncoding(encodings[i]);
			if (cts) {
				testBulkAlgorithmCTS(algorithm, key, IV, macSize, plainTextCTS);
			} else {
				testBulkAlgorithmCTS(algorithm, key, IV, macSize, plainText);
			}
		}
	}

	private void testBulkAlgorithmCTS(String algorithm, String key, String IV, int macSize, String input)
	{

		Cmac mac = new Cmac();
		String res = mac.calculate(input, key, algorithm, macSize);
		boolean verified = mac.verify(input, key, res, algorithm, macSize);
		//System.out.println("res: "+res);
		/*if(mac.hasError())
		{
			System.out.println("Eror. Code: "+mac.getErrorCode()+" Desc: "+mac.getErrorDescription());
		}else {
			System.out.println("no error");
		}*/
		assertTrue(verified);
		True(true, mac);
	}

	public static Test suite() {
		return new TestSuite(CmacTest.class);
	}

	@Override
	public void runTest() {
		testAES();
		testBLOWFISH();

		testCAMELLIA();
		testCAST5();
		testCAST6();
		testDES();
		testTRIPLEDES();
		testDSTU7624();
		testGOST28147();
		testNOEKEON();
		testRC2();
		testRC6();
		testRC532();
		testRIJNDAEL();
		testSEED();
		testSERPENT();
		testSKIPJACK();
		testTEA();
		testTWOFISH();
		testXTEA();
	}

	public void testAES() {
		// key legths 128,192 & 256
		// blocksize 128

		testBulkAlgorithm("AES", key128, IV128, 128, false);
		testBulkAlgorithm("AES", key128, IV128, 0, false);
		testBulkAlgorithm("AES", key128, IV128, 64, false);

		testBulkAlgorithm("AES", key128, "", 128, false);
		testBulkAlgorithm("AES", key128, "", 0, false);
		testBulkAlgorithm("AES", key128, "", 64, false);

		testBulkAlgorithm("AES", key192, IV128, 128, false);
		testBulkAlgorithm("AES", key192, IV128, 0, false);
		testBulkAlgorithm("AES", key192, IV128, 64, false);

		testBulkAlgorithm("AES", key192, "", 128, false);
		testBulkAlgorithm("AES", key192, "", 0, false);
		testBulkAlgorithm("AES", key192, "", 64, false);

		testBulkAlgorithm("AES", key256, IV128, 128, false);
		testBulkAlgorithm("AES", key256, IV128, 0, false);
		testBulkAlgorithm("AES", key256, IV128, 64, false);

		testBulkAlgorithm("AES", key256, "", 128, false);
		testBulkAlgorithm("AES", key256, "", 0, false);
		testBulkAlgorithm("AES", key256, "", 64, false);



	}

	public void testBLOWFISH() {
		// key lengths 0...448
		// blocksize 64
		// no gcm
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("BLOWFISH", key128, IV64, 64, false);
		testBulkAlgorithm("BLOWFISH", key128, IV64, 0, false);

		testBulkAlgorithm("BLOWFISH", key128, "", 64, false);
		testBulkAlgorithm("BLOWFISH", key128, "", 0, false);

		testBulkAlgorithm("BLOWFISH", key192, IV64, 64, false);
		testBulkAlgorithm("BLOWFISH", key192, IV64, 0, false);

		testBulkAlgorithm("BLOWFISH", key192, "", 64, false);
		testBulkAlgorithm("BLOWFISH", key192, "", 0, false);

		testBulkAlgorithm("BLOWFISH", key256, IV64, 64, false);
		testBulkAlgorithm("BLOWFISH", key256, IV64, 0, false);

		testBulkAlgorithm("BLOWFISH", key256, "", 64, false);
		testBulkAlgorithm("BLOWFISH", key256, "", 0, false);

	}

	public void testCAMELLIA() {
		// key lengths 128.192.256
		// blocksize 128

		testBulkAlgorithm("CAMELLIA", key128, IV128, 128, false);
		testBulkAlgorithm("CAMELLIA", key128, IV128, 64, false);
		testBulkAlgorithm("CAMELLIA", key128, IV128, 0, false);

		testBulkAlgorithm("CAMELLIA", key128, "", 128, false);
		testBulkAlgorithm("CAMELLIA", key128, "", 64, false);
		testBulkAlgorithm("CAMELLIA", key128, "", 0, false);

		testBulkAlgorithm("CAMELLIA", key192, IV128, 128, false);
		testBulkAlgorithm("CAMELLIA", key192, IV128, 64, false);
		testBulkAlgorithm("CAMELLIA", key192, IV128, 0, false);

		testBulkAlgorithm("CAMELLIA", key192, "", 128, false);
		testBulkAlgorithm("CAMELLIA", key192, "", 64, false);
		testBulkAlgorithm("CAMELLIA", key192, "", 0, false);

		testBulkAlgorithm("CAMELLIA", key256, IV128, 128, false);
		testBulkAlgorithm("CAMELLIA", key256, IV128, 64, false);
		testBulkAlgorithm("CAMELLIA", key256, IV128, 0, false);

		testBulkAlgorithm("CAMELLIA", key256, "", 128, false);
		testBulkAlgorithm("CAMELLIA", key256, "", 64, false);
		testBulkAlgorithm("CAMELLIA", key256, "", 0, false);


	}

	public void testCAST5() {
		// key length 0...128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("CAST5", key64, IV64, 64, false);
		testBulkAlgorithm("CAST5", key64, IV64, 0, false);

		testBulkAlgorithm("CAST5", key64, "", 64, false);
		testBulkAlgorithm("CAST5", key64, "", 0, false);

		testBulkAlgorithm("CAST5", key128, IV64, 64, false);
		testBulkAlgorithm("CAST5", key128, IV64, 0, false);

		testBulkAlgorithm("CAST5", key128, "", 64, false);
		testBulkAlgorithm("CAST5", key128, "", 0, false);


	}

	public void testCAST6() {
		// key length 0...256
		// blocksize 128
		testBulkAlgorithm("CAST6", key64, IV128, 128, false);
		testBulkAlgorithm("CAST6", key64, IV128, 0, false);
		testBulkAlgorithm("CAST6", key64, IV128, 64, false);

		testBulkAlgorithm("CAST6", key64, "", 128, false);
		testBulkAlgorithm("CAST6", key64, "", 0, false);
		testBulkAlgorithm("CAST6", key64, "", 64, false);

		testBulkAlgorithm("CAST6", key128, IV128, 128, false);
		testBulkAlgorithm("CAST6", key128, IV128, 0, false);
		testBulkAlgorithm("CAST6", key128, IV128, 64, false);

		testBulkAlgorithm("CAST6", key128, "", 128, false);
		testBulkAlgorithm("CAST6", key128, "", 0, false);
		testBulkAlgorithm("CAST6", key128, "", 64, false);

		testBulkAlgorithm("CAST6", key192, IV128, 128, false);
		testBulkAlgorithm("CAST6", key192, IV128, 0, false);
		testBulkAlgorithm("CAST6", key192, IV128, 64, false);

		testBulkAlgorithm("CAST6", key192, "", 128, false);
		testBulkAlgorithm("CAST6", key192, "", 0, false);
		testBulkAlgorithm("CAST6", key192, "", 64, false);

		testBulkAlgorithm("CAST6", key256, IV128, 128, false);
		testBulkAlgorithm("CAST6", key256, IV128, 0, false);
		testBulkAlgorithm("CAST6", key256, IV128, 64, false);

		testBulkAlgorithm("CAST6", key256, "", 128, false);
		testBulkAlgorithm("CAST6", key256, "", 0, false);
		testBulkAlgorithm("CAST6", key256, "", 64, false);


	}

	public void testDES() {
		// key length 64
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX no se puede usar (keylength != 128, 192 o 256

		testBulkAlgorithm("DES", key64, IV64, 64, false);
		testBulkAlgorithm("DES", key64, IV64, 0, false);

		testBulkAlgorithm("DES", key64, "", 64, false);
		testBulkAlgorithm("DES", key64, "", 0, false);

	}

	public void testTRIPLEDES() {
		// key length 128.192
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("TRIPLEDES", key128, IV64, 64, false);
		testBulkAlgorithm("TRIPLEDES", key128, IV64, 0, false);

		testBulkAlgorithm("TRIPLEDES", key128, "", 64, false);
		testBulkAlgorithm("TRIPLEDES", key128, "", 0, false);

		testBulkAlgorithm("TRIPLEDES", key192, IV64, 64, false);
		testBulkAlgorithm("TRIPLEDES", key192, IV64, 0, false);

		testBulkAlgorithm("TRIPLEDES", key192, "", 64, false);
		testBulkAlgorithm("TRIPLEDES", key192, "", 0, false);

	}

	public void testDSTU7624() {
		// key length 128, 256, 512
		// blocksize 128.256.512
		// input should be as lenght as the block
		testBulkAlgorithm("DSTU7624_128", key128, IV128, 128, true);
		testBulkAlgorithm("DSTU7624_128", key128, IV128, 64, true);
		testBulkAlgorithm("DSTU7624_128", key128, IV128, 0, true);

		testBulkAlgorithm("DSTU7624_128", key128, "", 128, true);
		testBulkAlgorithm("DSTU7624_128", key128, "", 64, true);
		testBulkAlgorithm("DSTU7624_128", key128, "", 0, true);


	}

	public void testGOST28147() {
		// key length 256
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("GOST28147", key256, IV64, 64, false);
		testBulkAlgorithm("GOST28147", key256, IV64, 0, false);

		testBulkAlgorithm("GOST28147", key256, "", 64, false);
		testBulkAlgorithm("GOST28147", key256, "", 0, false);

	}

	public void testNOEKEON() {
		// key length 128
		// blocksize 128
		testBulkAlgorithm("NOEKEON", key128, IV128, 128, false);
		testBulkAlgorithm("NOEKEON", key128, IV128, 64, false);
		testBulkAlgorithm("NOEKEON", key128, IV128, 0, false);


		testBulkAlgorithm("NOEKEON", key128, "", 128, false);
		testBulkAlgorithm("NOEKEON", key128, "", 64, false);
		testBulkAlgorithm("NOEKEON", key128, "", 0, false);
	}

	public void testRC2() {
		// key length 0...1024
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("RC2", key64, IV64, 64, false);
		testBulkAlgorithm("RC2", key64, IV64, 0, false);

		testBulkAlgorithm("RC2", key64, "", 64, false);
		testBulkAlgorithm("RC2", key64, "", 0, false);

		testBulkAlgorithm("RC2", key128, IV64, 64, false);
		testBulkAlgorithm("RC2", key128, IV64, 0, false);

		testBulkAlgorithm("RC2", key128, "", 64, false);
		testBulkAlgorithm("RC2", key128, "", 0, false);

		testBulkAlgorithm("RC2", key192, IV64, 64, false);
		testBulkAlgorithm("RC2", key192, IV64, 0, false);

		testBulkAlgorithm("RC2", key192, "", 64, false);
		testBulkAlgorithm("RC2", key192, "", 0, false);

		testBulkAlgorithm("RC2", key256, IV64, 64, false);
		testBulkAlgorithm("RC2", key256, IV64, 0, false);

		testBulkAlgorithm("RC2", key256, "", 64, false);
		testBulkAlgorithm("RC2", key256, "", 0, false);

		testBulkAlgorithm("RC2", key448, IV64, 64, false);
		testBulkAlgorithm("RC2", key448, IV64, 0, false);

		testBulkAlgorithm("RC2", key448, "", 64, false);
		testBulkAlgorithm("RC2", key448, "", 0, false);

		testBulkAlgorithm("RC2", key512, IV64, 64, false);
		testBulkAlgorithm("RC2", key512, IV64, 0, false);

		testBulkAlgorithm("RC2", key512, "", 64, false);
		testBulkAlgorithm("RC2", key512, "", 0, false);

		testBulkAlgorithm("RC2", key1024, IV64, 64, false);
		testBulkAlgorithm("RC2", key1024, IV64, 0, false);

		testBulkAlgorithm("RC2", key1024, "", 64, false);
		testBulkAlgorithm("RC2", key1024, "", 0, false);


	}

	public void testRC532() {
		// key length 0...128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("RC532", key64, IV64, 64, false);
		testBulkAlgorithm("RC532", key64, IV64, 0, false);

		testBulkAlgorithm("RC532", key64, "", 64, false);
		testBulkAlgorithm("RC532", key64, "", 0, false);

		testBulkAlgorithm("RC532", key128, IV64, 64, false);
		testBulkAlgorithm("RC532", key128, IV64, 0, false);

		testBulkAlgorithm("RC532", key128, "", 64, false);
		testBulkAlgorithm("RC532", key128, "", 0, false);
	}

	public void testRC6() {
		// key length 0...256
		// blocksize 128
		testBulkAlgorithm("RC6", key64, IV128, 128,false);
		testBulkAlgorithm("RC6", key64, IV128, 64,false);
		testBulkAlgorithm("RC6", key64, IV128, 0,false);

		testBulkAlgorithm("RC6", key64, "", 128,false);
		testBulkAlgorithm("RC6", key64, "", 64,false);
		testBulkAlgorithm("RC6", key64, "", 0,false);

		testBulkAlgorithm("RC6", key128, IV128, 128,false);
		testBulkAlgorithm("RC6", key128, IV128, 64,false);
		testBulkAlgorithm("RC6", key128, IV128, 0,false);

		testBulkAlgorithm("RC6", key128, "", 128,false);
		testBulkAlgorithm("RC6", key128, "", 64,false);
		testBulkAlgorithm("RC6", key128, "", 0,false);

		testBulkAlgorithm("RC6", key192, IV128, 128,false);
		testBulkAlgorithm("RC6", key192, IV128, 64,false);
		testBulkAlgorithm("RC6", key192, IV128, 0,false);

		testBulkAlgorithm("RC6", key192, "", 128,false);
		testBulkAlgorithm("RC6", key192, "", 64,false);
		testBulkAlgorithm("RC6", key192, "", 0,false);

		testBulkAlgorithm("RC6", key256, IV128, 128,false);
		testBulkAlgorithm("RC6", key256, IV128, 64,false);
		testBulkAlgorithm("RC6", key256, IV128, 0,false);

		testBulkAlgorithm("RC6", key256, "", 128,false);
		testBulkAlgorithm("RC6", key256, "", 64,false);
		testBulkAlgorithm("RC6", key256, "", 0,false);

	}

	public void testRIJNDAEL() {
		// key length 128.160.224.256
		// blocksize 128, 160, 192, 224, 256

		testBulkAlgorithm("RIJNDAEL_128", key128, IV128, 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key128, IV128, 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key128, IV128, 0, false);

		testBulkAlgorithm("RIJNDAEL_128", key128, "", 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key128, "", 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key128, "", 0, false);

		testBulkAlgorithm("RIJNDAEL_128", key160, IV128, 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key160, IV128, 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key160, IV128, 0, false);

		testBulkAlgorithm("RIJNDAEL_128", key160, "", 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key160, "", 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key160, "", 0, false);

		testBulkAlgorithm("RIJNDAEL_128", key192, IV128, 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key192, IV128, 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key192, IV128, 0, false);

		testBulkAlgorithm("RIJNDAEL_128", key192, "", 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key192, "", 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key192, "", 0, false);

		testBulkAlgorithm("RIJNDAEL_128", key256, IV128, 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key256, IV128, 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key256, IV128, 0, false);

		testBulkAlgorithm("RIJNDAEL_128", key256, "", 128, false);
		testBulkAlgorithm("RIJNDAEL_128", key256, "", 64, false);
		testBulkAlgorithm("RIJNDAEL_128", key256, "", 0, false);


	}

	public void testSEED() {
		// key length 128
		// blocksize 128


		testBulkAlgorithm("SEED", key128, IV128, 128, false);
		testBulkAlgorithm("SEED", key128, IV128, 0, false);
		testBulkAlgorithm("SEED", key128, IV128, 64, false);

		testBulkAlgorithm("SEED", key128, "", 0, false);
		testBulkAlgorithm("SEED", key128, "", 128, false);
		testBulkAlgorithm("SEED", key128, "", 64, false);

	}

	public void testSERPENT() {
		// key length 128.192.256
		// blocksize 128

		testBulkAlgorithm("SERPENT", key128, IV128, 128, false);
		testBulkAlgorithm("SERPENT", key128, IV128, 0, false);
		testBulkAlgorithm("SERPENT", key128, IV128, 64, false);

		testBulkAlgorithm("SERPENT", key128, "", 128, false);
		testBulkAlgorithm("SERPENT", key128, "", 0, false);
		testBulkAlgorithm("SERPENT", key128, "", 64, false);

		testBulkAlgorithm("SERPENT", key192, IV128, 128, false);
		testBulkAlgorithm("SERPENT", key192, IV128, 0, false);
		testBulkAlgorithm("SERPENT", key192, IV128, 64, false);

		testBulkAlgorithm("SERPENT", key192, "", 128, false);
		testBulkAlgorithm("SERPENT", key192, "", 0, false);
		testBulkAlgorithm("SERPENT", key192, "", 64, false);

		testBulkAlgorithm("SERPENT", key256, IV128, 128, false);
		testBulkAlgorithm("SERPENT", key256, IV128, 0, false);
		testBulkAlgorithm("SERPENT", key256, IV128, 64, false);

		testBulkAlgorithm("SERPENT", key256, "", 128, false);
		testBulkAlgorithm("SERPENT", key256, "", 0, false);
		testBulkAlgorithm("SERPENT", key256, "", 64, false);

	}

	public void testSKIPJACK() {
		// key length 128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("SKIPJACK", key128, IV64, 64, false);
		testBulkAlgorithm("SKIPJACK", key128, IV64, 0, false);

		testBulkAlgorithm("SKIPJACK", key128, "", 64, false);
		testBulkAlgorithm("SKIPJACK", key128, "", 0, false);

	}

	public void testSM4() {
		// key length 128
		// blocksize 128
		testBulkAlgorithm("SM4", key128, IV128, 128, false);
		testBulkAlgorithm("SM4", key128, IV128, 0, false);
		testBulkAlgorithm("SM4", key128, IV128, 64, false);

		testBulkAlgorithm("SM4", key128, "", 128, false);
		testBulkAlgorithm("SM4", key128, "", 0, false);
		testBulkAlgorithm("SM4", key128, "", 64, false);

	}

	public void testTEA() {
		// key length 128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("TEA",  key128, IV64, 64, false);
		testBulkAlgorithm("TEA",  key128, IV64, 0, false);

		testBulkAlgorithm("TEA",  key128, "", 64, false);
		testBulkAlgorithm("TEA",  key128, "", 0, false);

	}

	public void testTWOFISH() {
		// key length 128.192.256
		// blocksize 128
		testBulkAlgorithm("TWOFISH", key128, IV128, 128, false);
		testBulkAlgorithm("TWOFISH", key128, IV128, 0, false);
		testBulkAlgorithm("TWOFISH", key128, IV128, 64, false);

		testBulkAlgorithm("TWOFISH", key128, "", 128, false);
		testBulkAlgorithm("TWOFISH", key128, "", 0, false);
		testBulkAlgorithm("TWOFISH", key128, "", 64, false);


		testBulkAlgorithm("TWOFISH", key192, IV128, 128, false);
		testBulkAlgorithm("TWOFISH", key192, IV128, 0, false);
		testBulkAlgorithm("TWOFISH", key192, IV128, 64, false);

		testBulkAlgorithm("TWOFISH", key192, "", 128, false);
		testBulkAlgorithm("TWOFISH", key192, "", 0, false);
		testBulkAlgorithm("TWOFISH", key192, "", 64, false);

		testBulkAlgorithm("TWOFISH", key256, IV128, 128, false);
		testBulkAlgorithm("TWOFISH", key256, IV128, 0, false);
		testBulkAlgorithm("TWOFISH", key256, IV128, 64, false);

		testBulkAlgorithm("TWOFISH", key256, "", 128, false);
		testBulkAlgorithm("TWOFISH", key256, "", 0, false);
		testBulkAlgorithm("TWOFISH", key256, "", 64, false);


	}

	public void testXTEA() {
		// key length 128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("XTEA", key128, IV64, 64, false);
		testBulkAlgorithm("XTEA", key128, IV64, 0, false);

		testBulkAlgorithm("XTEA", key128, "", 64, false);
		testBulkAlgorithm("XTEA", key128, "", 0, false);


	}
}
