package com.genexus.cryptography.test.symmetric;

import com.genexus.cryptography.symmetric.SymmetricBlockCipher;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class BlockEncryptionTest extends SecurityAPITestObject {
	//public class BlockEncryptionTest{
	private static String plainText;
	private static String plainTextCTS;
	private static String[] arrayPaddings;
	private static String[] arrayModes;
	private static String[] arrayModes_160_224;
	private static String[] arrayModes64;
	private static String[] arrayNoncesCCM;
	private static String[] arrayNonces;
	private static int[] arrayTagsGCM;
	private static int[] arrayMacsEAX;
	private static int[] arrayTagsCCM;

	protected static String key1024;
	protected static String key512;
	protected static String key448;
	protected static String key256;
	protected static String key192;
	protected static String key160;
	protected static String key128;
	protected static String key64;

	protected static String IV1024;
	protected static String IV512;
	protected static String IV256;
	protected static String IV224;
	protected static String IV192;
	protected static String IV160;
	protected static String IV128;
	protected static String IV64;

	private static SymmetricKeyGenerator keyGen;

	private static String[] encodings;
	private static EncodingUtil eu;

	@Override
	protected void setUp() {

		// new EncodingUtil().setEncoding("UTF8");
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
		IV1024 = keyGen.doGenerateIV("GENERICRANDOM", 1024);
		IV512 = keyGen.doGenerateIV("GENERICRANDOM", 512);
		IV256 = keyGen.doGenerateIV("GENERICRANDOM", 256);
		IV224 = keyGen.doGenerateIV("GENERICRANDOM", 224);
		IV192 = keyGen.doGenerateIV("GENERICRANDOM", 192);
		IV160 = keyGen.doGenerateIV("GENERICRANDOM", 160);
		IV128 = keyGen.doGenerateIV("GENERICRANDOM", 128);
		IV64 = keyGen.doGenerateIV("GENERICRANDOM", 64);

		/**** CREATE nonces ****/
		String nonce104 = keyGen.doGenerateIV("GENERICRANDOM", 104);
		String nonce64 = keyGen.doGenerateIV("GENERICRANDOM", 64);
		String nonce56 = keyGen.doGenerateIV("GENERICRANDOM", 56);
		arrayNoncesCCM = new String[] { nonce56, nonce64, nonce104 };

		/**** CREATE PADDINGS ****/
		arrayPaddings = new String[] { "PKCS7PADDING", "ISO10126D2PADDING", "X923PADDING", "ISO7816D4PADDING",
			"ZEROBYTEPADDING" };

		/**** CREATEMODES ****/
		arrayModes = new String[] { "ECB", "CBC", "CFB", "CTR", "CTS", "OFB", "OPENPGPCFB" };
		arrayModes_160_224 = new String[] { "ECB", "CBC", "CTR", "CTS", "OPENPGPCFB" }; //CFB mode does not work on 160 and 224 block sizes
		arrayModes64 = new String[] { "ECB", "CBC", "CFB", "CTR", "CTS", "OFB", "OPENPGPCFB" };
		arrayTagsGCM = new int[] { 128, 120, 112, 104, 96 };
		arrayTagsCCM = new int[] { 64, 128 };
		arrayMacsEAX = new int[] { 8, 16, 64, 128 };
		arrayNonces = new String[] { IV64, IV128, IV192, IV256 };

		encodings = new String[] { "UTF_8", "UTF_16", "UTF_16BE", "UTF_16LE", "UTF_32", "UTF_32BE", "UTF_32LE", "SJIS",
			"GB2312" };

		eu = new EncodingUtil();
	}

	public static Test suite() {
		return new TestSuite(BlockEncryptionTest.class);
	}

	private void testBulkAlgorithm(String algorithm, String[] modes, String[] paddings, String key, String IV,
								   boolean cts) {
		for (int i = 0; i < encodings.length; i++) {
			eu.setEncoding(encodings[i]);
			if (cts) {
				testBulkAlgorithmCTS(algorithm, modes, paddings, key, IV, plainTextCTS);
			} else {
				testBulkAlgorithmCTS(algorithm, modes, paddings, key, IV, plainText);
			}
		}
	}

	private void testBulkAlgorithmCTS(String algorithm, String[] modes, String[] paddings, String key, String IV,
									  String text) {
		SymmetricBlockCipher symBlockCipher = new SymmetricBlockCipher();
		for (int m = 0; m < modes.length; m++) {
			for (int p = 0; p < arrayPaddings.length; p++) {
				String encrypted = symBlockCipher.doEncrypt(algorithm, modes[m], arrayPaddings[p], key, IV, text);
				String decrypted = symBlockCipher.doDecrypt(algorithm, modes[m], arrayPaddings[p], key, IV,
					encrypted);
				String resText = eu.getString(eu.getBytes(text));
				assertTrue(SecurityUtils.compareStrings(resText, decrypted));
				True(true, symBlockCipher);
			}

		}
	}

	private void testCCM(String algorithm, String key, boolean cts) {
		if (cts) {
			testCCM_CTS(algorithm, key, plainTextCTS);
		} else {
			testCCM_CTS(algorithm, key, plainText);
		}
	}

	private void testCCM_CTS(String algorithm, String key, String text) {
		for (int n = 0; n < arrayNoncesCCM.length; n++) {
			for (int t = 0; t < arrayTagsCCM.length; t++) {
				for (int p = 0; p < arrayPaddings.length; p++) {
					SymmetricBlockCipher symBlockCipher = new SymmetricBlockCipher();
					String encrypted = symBlockCipher.doAEADEncrypt(algorithm, "AEAD_CCM", key, arrayTagsCCM[t],
						arrayNoncesCCM[n], text);
					String decrypted = symBlockCipher.doAEADDecrypt(algorithm, "AEAD_CCM", key, arrayTagsCCM[t],
						arrayNoncesCCM[n], encrypted);
					assertTrue(SecurityUtils.compareStrings(text, decrypted));
					True(SecurityUtils.compareStrings(text, decrypted), symBlockCipher);
				}
			}
		}
	}

	private void testEAX(String algorithm, String key, boolean cts) {
		if (cts) {
			testEAX_CTS(algorithm, key, plainTextCTS);
		} else {
			testEAX_CTS(algorithm, key, plainText);
		}
	}

	private void testEAX_CTS(String algorithm, String key, String text) {
		for (int m = 0; m < arrayMacsEAX.length; m++) {
			for (int n = 0; n < arrayNonces.length; n++) {
				for (int p = 0; p < arrayPaddings.length; p++) {
					SymmetricBlockCipher symBlockCipher = new SymmetricBlockCipher();
					String encrypted = symBlockCipher.doAEADEncrypt(algorithm, "AEAD_EAX", key, arrayMacsEAX[m],
						arrayNonces[n], text);
					String decrypted = symBlockCipher.doAEADDecrypt(algorithm, "AEAD_EAX", key, arrayMacsEAX[m],
						arrayNonces[n], encrypted);
					assertTrue(SecurityUtils.compareStrings(text, decrypted));
					True(SecurityUtils.compareStrings(text, decrypted), symBlockCipher);
				}
			}
		}
	}

	private void testGCM(String algorithm, String key, boolean cts) {
		if (cts) {
			testGCM_CTS(algorithm, key, plainTextCTS);
		} else {
			testGCM_CTS(algorithm, key, plainText);
		}
	}

	private void testGCM_CTS(String algorithm, String key, String text) {
		for (int m = 0; m < arrayTagsGCM.length; m++) {
			for (int n = 0; n < arrayNonces.length; n++) {
				for (int p = 0; p < arrayPaddings.length; p++) {
					SymmetricBlockCipher symBlockCipher = new SymmetricBlockCipher();
					String encrypted = symBlockCipher.doAEADEncrypt(algorithm, "AEAD_GCM", key, arrayTagsGCM[m],
						arrayNonces[n], text);
					String decrypted = symBlockCipher.doAEADDecrypt(algorithm, "AEAD_GCM", key, arrayTagsGCM[m],
						arrayNonces[n], encrypted);
					assertTrue(SecurityUtils.compareStrings(text, decrypted));
					True(SecurityUtils.compareStrings(text, decrypted), symBlockCipher);
				}
			}
		}
	}

	public void testAES() {
		// key legths 128,192 & 256
		// blocksize 128

		testBulkAlgorithm("AES", arrayModes, arrayPaddings, key128, IV128, false);
		testBulkAlgorithm("AES", arrayModes, arrayPaddings, key192, IV128, false);
		testBulkAlgorithm("AES", arrayModes, arrayPaddings, key256, IV128, false);

		testCCM("AES", key128, false);
		testCCM("AES", key192, false);
		testCCM("AES", key256, false);

		testGCM("AES", key128, false);
		testGCM("AES", key192, false);
		testGCM("AES", key256, false);

		testEAX("AES", key128, false);
		testEAX("AES", key192, false);
		testEAX("AES", key256, false);

	}

	public void testBLOWFISH() {
		// key lengths 0...448
		// blocksize 64
		// no gcm
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("BLOWFISH", arrayModes64, arrayPaddings, key128, IV64, false);
		testBulkAlgorithm("BLOWFISH", arrayModes64, arrayPaddings, key192, IV64, false);
		testBulkAlgorithm("BLOWFISH", arrayModes64, arrayPaddings, key256, IV64, false);
		testBulkAlgorithm("BLOWFISH", arrayModes64, arrayPaddings, key448, IV64, false);

	}

	public void testCAMELLIA() {
		// key lengths 128.192.256
		// blocksize 128

		testBulkAlgorithm("CAMELLIA", arrayModes, arrayPaddings, key128, IV128, false);
		testBulkAlgorithm("CAMELLIA", arrayModes, arrayPaddings, key192, IV128, false);
		testBulkAlgorithm("CAMELLIA", arrayModes, arrayPaddings, key256, IV128, false);

		testCCM("CAMELLIA", key128, false);
		testCCM("CAMELLIA", key192, false);
		testCCM("CAMELLIA", key256, false);

		testGCM("CAMELLIA", key128, false);
		testGCM("CAMELLIA", key192, false);
		testGCM("CAMELLIA", key256, false);

		testEAX("CAMELLIA", key128, false);
		testEAX("CAMELLIA", key192, false);
		testEAX("CAMELLIA", key256, false);

	}

	public void testCAST5() {
		// key length 0...128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("CAST5", arrayModes64, arrayPaddings, key64, IV64, false);
		testBulkAlgorithm("CAST5", arrayModes64, arrayPaddings, key128, IV64, false);

	}

	public void testCAST6() {
		// key length 0...256
		// blocksize 128
		testBulkAlgorithm("CAST6", arrayModes, arrayPaddings, key64, IV128, false);
		testBulkAlgorithm("CAST6", arrayModes, arrayPaddings, key128, IV128, false);
		testBulkAlgorithm("CAST6", arrayModes, arrayPaddings, key192, IV128, false);
		testBulkAlgorithm("CAST6", arrayModes, arrayPaddings, key256, IV128, false);

		// testCCM(String algorithm, String key, int macSize, boolean cts) {
		// testGCM(String algorithm, String key, String nonce, boolean cts) {
		// testEAX(String algorithm, String key, String nonce, boolean cts) {

		testCCM("CAST6", key64, false);
		testCCM("CAST6", key128, false);
		testCCM("CAST6", key192, false);
		testCCM("CAST6", key256, false);

		testGCM("CAST6", key64, false);
		testGCM("CAST6", key128, false);
		testGCM("CAST6", key192, false);
		testGCM("CAST6", key256, false);

		testEAX("CAST6", key128, false);
		testEAX("CAST6", key192, false);
		testEAX("CAST6", key256, false);

	}

	public void testDES() {
		// key length 64
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX no se puede usar (keylength != 128, 192 o 256

		testBulkAlgorithm("DES", arrayModes64, arrayPaddings, key64, IV64, false);

	}

	public void testTRIPLEDES() {
		// key length 128.192
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("TRIPLEDES", arrayModes64, arrayPaddings, key128, IV64, false);
		testBulkAlgorithm("TRIPLEDES", arrayModes64, arrayPaddings, key192, IV64, false);

	}

	public void testDSTU7624() {
		// key length 128, 256, 512
		// blocksize 128.256.512
		// input should be as lenght as the block
		testBulkAlgorithm("DSTU7624_128", arrayModes, arrayPaddings, key128, IV128, true);
		testBulkAlgorithm("DSTU7624_256", arrayModes, arrayPaddings, key256, IV256, true);
		testBulkAlgorithm("DSTU7624_512", arrayModes, arrayPaddings, key512, IV512, true);

		testCCM("DSTU7624_128", key128, true);
		testGCM("DSTU7624_128", key128, true);
		testEAX("DSTU7624_128", key128, true);

	}

	public void testGOST28147() {
		// key length 256
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("GOST28147", arrayModes64, arrayPaddings, key256, IV64, false);

	}

	public void testNOEKEON() {
		// key length 128
		// blocksize 128
		testBulkAlgorithm("NOEKEON", arrayModes, arrayPaddings, key128, IV128, false);

		testCCM("NOEKEON", key128, false);
		testGCM("NOEKEON", key128, false);
		testEAX("NOEKEON", key128, false);

	}

	public void testRC2() {
		// key length 0...1024
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("RC2", arrayModes64, arrayPaddings, key64, IV64, false);
		testBulkAlgorithm("RC2", arrayModes64, arrayPaddings, key128, IV64, false);
		testBulkAlgorithm("RC2", arrayModes64, arrayPaddings, key192, IV64, false);
		testBulkAlgorithm("RC2", arrayModes64, arrayPaddings, key256, IV64, false);
		testBulkAlgorithm("RC2", arrayModes64, arrayPaddings, key512, IV64, false);
		testBulkAlgorithm("RC2", arrayModes64, arrayPaddings, key1024, IV64, false);

	}

	public void testRC532() {
		// key length 0...128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("RC532", arrayModes64, arrayPaddings, key64, IV64, false);
		testBulkAlgorithm("RC532", arrayModes64, arrayPaddings, key128, IV64, false);
	}

	public void testRC6() {
		// key length 0...256
		// blocksize 128
		testBulkAlgorithm("RC6", arrayModes, arrayPaddings, key64, IV128, false);
		testBulkAlgorithm("RC6", arrayModes, arrayPaddings, key128, IV128, false);
		testBulkAlgorithm("RC6", arrayModes, arrayPaddings, key192, IV128, false);
		testBulkAlgorithm("RC6", arrayModes, arrayPaddings, key256, IV128, false);

		testCCM("RC6", key64, false);
		testCCM("RC6", key128, false);
		testCCM("RC6", key192, false);
		testCCM("RC6", key256, false);

		testGCM("RC6", key64, false);
		testGCM("RC6", key128, false);
		testGCM("RC6", key192, false);
		testGCM("RC6", key256, false);

		testEAX("RC6", key128, false);
		testEAX("RC6", key192, false);
		testEAX("RC6", key256, false);
	}

	public void testRIJNDAEL() {
		// key length 128.160.224.256
		// blocksize 128, 160, 192, 224, 256

		testBulkAlgorithm("RIJNDAEL_128", arrayModes, arrayPaddings, key128, IV128, false);
		testBulkAlgorithm("RIJNDAEL_128", arrayModes, arrayPaddings, key160, IV128, false);
		testBulkAlgorithm("RIJNDAEL_128", arrayModes, arrayPaddings, key192, IV128, false);
		testBulkAlgorithm("RIJNDAEL_128", arrayModes, arrayPaddings, key256, IV128, false);

		//Don't support CFB or OFB
		testBulkAlgorithm("RIJNDAEL_160", arrayModes_160_224, arrayPaddings, key128, IV160, false);
		testBulkAlgorithm("RIJNDAEL_160", arrayModes_160_224, arrayPaddings, key160, IV160, false);
		testBulkAlgorithm("RIJNDAEL_160", arrayModes_160_224, arrayPaddings, key192, IV160, false);
		testBulkAlgorithm("RIJNDAEL_160", arrayModes_160_224, arrayPaddings, key256, IV160, false);

		testBulkAlgorithm("RIJNDAEL_192", arrayModes, arrayPaddings, key128, IV192, false);
		testBulkAlgorithm("RIJNDAEL_192", arrayModes, arrayPaddings, key160, IV192, false);
		testBulkAlgorithm("RIJNDAEL_192", arrayModes, arrayPaddings, key192, IV192, false);
		testBulkAlgorithm("RIJNDAEL_192", arrayModes, arrayPaddings, key256, IV192, false);

		//Don't support CFB or OFB
		testBulkAlgorithm("RIJNDAEL_224", arrayModes_160_224, arrayPaddings, key128, IV224, false);
		testBulkAlgorithm("RIJNDAEL_224", arrayModes_160_224, arrayPaddings, key160, IV224, false);
		testBulkAlgorithm("RIJNDAEL_224", arrayModes_160_224, arrayPaddings, key192, IV224, false);
		testBulkAlgorithm("RIJNDAEL_224", arrayModes_160_224, arrayPaddings, key256, IV224, false);

		testBulkAlgorithm("RIJNDAEL_256", arrayModes, arrayPaddings, key128, IV256, false);
		testBulkAlgorithm("RIJNDAEL_256", arrayModes, arrayPaddings, key160, IV256, false);
		testBulkAlgorithm("RIJNDAEL_256", arrayModes, arrayPaddings, key192, IV256, false);
		testBulkAlgorithm("RIJNDAEL_256", arrayModes, arrayPaddings, key256, IV256, false);

		testCCM("RIJNDAEL_128", key128, false);
		testCCM("RIJNDAEL_128", key160, false);
		testCCM("RIJNDAEL_128", key192, false);
		testCCM("RIJNDAEL_128", key256, false);

		testGCM("RIJNDAEL_128", key128, false);
		testGCM("RIJNDAEL_128", key160, false);
		testGCM("RIJNDAEL_128", key192, false);
		testGCM("RIJNDAEL_128", key256, false);

		testEAX("RIJNDAEL_128", key128, false);
		testEAX("RIJNDAEL_128", key192, false);
		testEAX("RIJNDAEL_128", key256, false);

	}

	public void testSEED() {
		// key length 128
		// blocksize 128

		testBulkAlgorithm("SEED", arrayModes, arrayPaddings, key128, IV128, false);

		testCCM("SEED", key128, false);
		testGCM("SEED", key128, false);
		testEAX("SEED", key128, false);
	}

	public void testSERPENT() {
		// key length 128.192.256
		// blocksize 128
		testBulkAlgorithm("SERPENT", arrayModes, arrayPaddings, key128, IV128, false);
		testBulkAlgorithm("SERPENT", arrayModes, arrayPaddings, key192, IV128, false);
		testBulkAlgorithm("SERPENT", arrayModes, arrayPaddings, key256, IV128, false);

		testCCM("SERPENT", key128, false);
		testCCM("SERPENT", key192, false);
		testCCM("SERPENT", key256, false);

		testGCM("SERPENT", key128, false);
		testGCM("SERPENT", key192, false);
		testGCM("SERPENT", key256, false);

		testEAX("SERPENT", key128, false);
		testEAX("SERPENT", key192, false);
		testEAX("SERPENT", key256, false);

	}

	public void testSKIPJACK() {
		// key length 128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("SKIPJACK", arrayModes64, arrayPaddings, key128, IV64, false);

	}

	public void testSM4() {
		// key length 128
		// blocksize 128
		testBulkAlgorithm("SM4", arrayModes, arrayPaddings, key128, IV128, false);

		testCCM("SM4", key128, false);
		testGCM("SM4", key128, false);
		testEAX("SM4", key128, false);
	}

	public void testTEA() {
		// key length 128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("TEA", arrayModes64, arrayPaddings, key128, IV64, false);

	}

	public void testTHREEFISH() {
		// key length 256.512.1024
		// blocksize 256.512.1024
		// key must be same size as the block
		// the input must be the same length or longer than the block
		// no se puede usar CCM (blosize!=128)
		// GCM siempre explota

		testBulkAlgorithm("THREEFISH_256", arrayModes, arrayPaddings, key256, IV256, true);
		testBulkAlgorithm("THREEFISH_512", arrayModes, arrayPaddings, key512, IV512, true);
		testBulkAlgorithm("THREEFISH_1024", arrayModes, arrayPaddings, key1024, IV1024, true);
	}

	public void testTWOFISH() {
		// key length 128.192.256
		// blocksize 128
		testBulkAlgorithm("TWOFISH", arrayModes, arrayPaddings, key128, IV128, false);
		testBulkAlgorithm("TWOFISH", arrayModes, arrayPaddings, key192, IV128, false);
		testBulkAlgorithm("TWOFISH", arrayModes, arrayPaddings, key256, IV128, false);

		testCCM("TWOFISH", key128, false);
		testCCM("TWOFISH", key192, false);
		testCCM("TWOFISH", key256, false);

		testGCM("TWOFISH", key128, false);
		testGCM("TWOFISH", key192, false);
		testGCM("TWOFISH", key256, false);

		testEAX("TWOFISH", key128, false);
		testEAX("TWOFISH", key192, false);
		testEAX("TWOFISH", key256, false);

	}

	public void testXTEA() {
		// key length 128
		// blocksize 64
		// no se puede usar CCM (blosize!=128)
		// no se pude usar GCM (blocksize <128)
		// EAX siempre explota, no se puede usar con AEAD

		testBulkAlgorithm("XTEA", arrayModes64, arrayPaddings, key128, IV64, false);

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
		testTHREEFISH();
		testTWOFISH();
		testXTEA();

	}
}
