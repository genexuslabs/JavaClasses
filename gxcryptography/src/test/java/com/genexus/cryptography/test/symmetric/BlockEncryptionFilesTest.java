package com.genexus.cryptography.test.symmetric;

import com.genexus.cryptography.checksum.ChecksumCreator;
import com.genexus.cryptography.symmetric.SymmetricBlockCipher;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlockEncryptionFilesTest extends SecurityAPITestObject {


	protected static String key256;
	protected static String key192;
	protected static String key160;
	protected static String key128;
	protected static String key64;

	protected static String IV256;
	protected static String IV224;
	protected static String IV192;
	protected static String IV160;
	protected static String IV128;
	protected static String IV64;

	protected static String pathInput;
	protected static String pathOutputEncrypted;
	protected static String pathOutput;

	@BeforeClass
	public static void setUp() {

		pathInput = resources.concat("/flag.jpg");
		pathOutputEncrypted = tempFolder.toString() + "flagEncrypted";
		pathOutput = tempFolder + "flagOut.jpg";

		SymmetricKeyGenerator keyGen = new SymmetricKeyGenerator();

		// CREATE KEYS

		key256 = keyGen.doGenerateKey("GENERICRANDOM", 256);
		key160 = keyGen.doGenerateKey("GENERICRANDOM", 160);
		key192 = keyGen.doGenerateKey("GENERICRANDOM", 192);
		key128 = keyGen.doGenerateKey("GENERICRANDOM", 128);
		key64 = keyGen.doGenerateKey("GENERICRANDOM", 64);

		// CREATE IVs

		IV256 = keyGen.doGenerateIV("GENERICRANDOM", 256);
		IV224 = keyGen.doGenerateIV("GENERICRANDOM", 224);
		IV192 = keyGen.doGenerateIV("GENERICRANDOM", 192);
		IV160 = keyGen.doGenerateIV("GENERICRANDOM", 160);
		IV128 = keyGen.doGenerateIV("GENERICRANDOM", 128);
		IV64 = keyGen.doGenerateIV("GENERICRANDOM", 64);
	}

	@Test
	public void testAES() {
		testBulkFiles("AES", "CBC", "ZEROBYTEPADDING", key128, IV128);
		testBulkFiles("AES", "CBC", "ZEROBYTEPADDING", key192, IV128);
		testBulkFiles("AES", "CBC", "ZEROBYTEPADDING", key256, IV128);

		testBulkFiles("AES", "ECB", "ZEROBYTEPADDING", key128, IV128);
		testBulkFiles("AES", "ECB", "ZEROBYTEPADDING", key192, IV128);
		testBulkFiles("AES", "ECB", "ZEROBYTEPADDING", key256, IV128);

		testBulkFiles("AES", "CBC", "PKCS7PADDING", key128, IV128);
		testBulkFiles("AES", "CBC", "PKCS7PADDING", key192, IV128);
		testBulkFiles("AES", "CBC", "PKCS7PADDING", key256, IV128);

		testBulkFiles("AES", "CBC", "X923PADDING", key128, IV128);
		testBulkFiles("AES", "CBC", "X923PADDING", key192, IV128);
		testBulkFiles("AES", "CBC", "X923PADDING", key256, IV128);

		testBulkFiles("AES", "CBC", "ISO7816D4PADDING", key128, IV128);
		testBulkFiles("AES", "CBC", "ISO7816D4PADDING", key192, IV128);
		testBulkFiles("AES", "CBC", "ISO7816D4PADDING", key256, IV128);

		testBulkGCM("AES", key128, IV128);
		testBulkGCM("AES", key192, IV128);
		testBulkGCM("AES", key256, IV128);
	}

	@Test
	public void testDES() {
		testBulkFiles("DES", "CBC", "ZEROBYTEPADDING", key64, IV64);
	}

	@Test
	public void testTRIPLEDES() {
		testBulkFiles("TRIPLEDES", "CBC", "ZEROBYTEPADDING", key128, IV64);
		testBulkFiles("TRIPLEDES", "CBC", "ZEROBYTEPADDING", key192, IV64);
	}

	@Test
	public void testRIJNDAEL() {
		testBulkFiles("RIJNDAEL_128", "CBC", "ZEROBYTEPADDING", key128, IV128);
		testBulkFiles("RIJNDAEL_128", "CBC", "ZEROBYTEPADDING", key256, IV128);
		testBulkFiles("RIJNDAEL_256", "CBC", "ZEROBYTEPADDING", key128, IV256);
		testBulkFiles("RIJNDAEL_256", "CBC", "ZEROBYTEPADDING", key256, IV256);

		testBulkFiles("RIJNDAEL_128", "ECB", "ZEROBYTEPADDING", key128, IV128);
		testBulkFiles("RIJNDAEL_128", "ECB", "ZEROBYTEPADDING", key256, IV128);
		testBulkFiles("RIJNDAEL_256", "ECB", "ZEROBYTEPADDING", key128, IV256);
		testBulkFiles("RIJNDAEL_256", "ECB", "ZEROBYTEPADDING", key256, IV256);

		testBulkFiles("RIJNDAEL_128", "CBC", "PKCS7PADDING", key128, IV128);
		testBulkFiles("RIJNDAEL_128", "CBC", "PKCS7PADDING", key256, IV128);
		testBulkFiles("RIJNDAEL_256", "CBC", "PKCS7PADDING", key128, IV256);
		testBulkFiles("RIJNDAEL_256", "CBC", "PKCS7PADDING", key256, IV256);

		testBulkFiles("RIJNDAEL_128", "CBC", "X923PADDING", key128, IV128);
		testBulkFiles("RIJNDAEL_128", "CBC", "X923PADDING", key256, IV128);
		testBulkFiles("RIJNDAEL_256", "CBC", "X923PADDING", key128, IV256);
		testBulkFiles("RIJNDAEL_256", "CBC", "X923PADDING", key256, IV256);

		testBulkFiles("RIJNDAEL_128", "CBC", "ISO7816D4PADDING", key128, IV128);
		testBulkFiles("RIJNDAEL_128", "CBC", "ISO7816D4PADDING", key256, IV128);
		testBulkFiles("RIJNDAEL_256", "CBC", "ISO7816D4PADDING", key128, IV256);
		testBulkFiles("RIJNDAEL_256", "CBC", "ISO7816D4PADDING", key256, IV256);

		testBulkGCM("RIJNDAEL_128", key128, IV128);
		testBulkGCM("RIJNDAEL_128", key256, IV128);
	}

	@Test
	public void testTWOFISH() {
		testBulkFiles("TWOFISH", "CBC", "ZEROBYTEPADDING", key128, IV128);
		testBulkFiles("TWOFISH", "CBC", "ZEROBYTEPADDING", key192, IV128);
		testBulkFiles("TWOFISH", "CBC", "ZEROBYTEPADDING", key256, IV128);


		testBulkFiles("TWOFISH", "ECB", "ZEROBYTEPADDING", key128, IV128);
		testBulkFiles("TWOFISH", "ECB", "ZEROBYTEPADDING", key192, IV128);
		testBulkFiles("TWOFISH", "ECB", "ZEROBYTEPADDING", key256, IV128);

		testBulkFiles("TWOFISH", "CBC", "PKCS7PADDING", key128, IV128);
		testBulkFiles("TWOFISH", "CBC", "PKCS7PADDING", key192, IV128);
		testBulkFiles("TWOFISH", "CBC", "PKCS7PADDING", key256, IV128);


		testBulkFiles("TWOFISH", "CBC", "X923PADDING", key128, IV128);
		testBulkFiles("TWOFISH", "CBC", "X923PADDING", key192, IV128);
		testBulkFiles("TWOFISH", "CBC", "X923PADDING", key256, IV128);


		testBulkFiles("TWOFISH", "CBC", "ISO7816D4PADDING", key128, IV128);
		testBulkFiles("TWOFISH", "CBC", "ISO7816D4PADDING", key192, IV128);
		testBulkFiles("TWOFISH", "CBC", "ISO7816D4PADDING", key256, IV128);

		testBulkGCM("TWOFISH", key128, IV128);
		testBulkGCM("TWOFISH", key192, IV128);
		testBulkGCM("TWOFISH", key256, IV128);

	}

	private void testBulkFiles(String algorithm, String mode, String padding, String key, String IV) {
		SymmetricBlockCipher cipher = new SymmetricBlockCipher();
		boolean encrypts = cipher.doEncryptFile(algorithm, mode, padding, key, IV, pathInput, pathOutputEncrypted);
		True(encrypts, cipher);
		boolean decrypts = cipher.doDecryptFile(algorithm, mode, padding, key, IV, pathOutputEncrypted, pathOutput);
		True(decrypts, cipher);
		ChecksumCreator check = new ChecksumCreator();
		String checksum = check.generateChecksum(pathInput, "LOCAL_FILE", "CRC8_DARC");
		boolean checks = check.verifyChecksum(pathOutput, "LOCAL_FILE", "CRC8_DARC", checksum);
		True(checks, check);
	}

	private void testBulkGCM(String algorithm, String key, String nonce) {
		SymmetricBlockCipher cipher = new SymmetricBlockCipher();
		boolean encrypts = cipher.doAEADEncryptFile(algorithm, "AEAD_GCM", key, 128, nonce, pathInput, pathOutputEncrypted);
		True(encrypts, cipher);
		boolean decrypts = cipher.doAEADDecryptFile(algorithm, "AEAD_GCM", key, 128, nonce, pathOutputEncrypted, pathOutput);
		True(decrypts, cipher);
		ChecksumCreator check = new ChecksumCreator();
		String checksum = check.generateChecksum(pathInput, "LOCAL_FILE", "CRC8_DARC");
		boolean checks = check.verifyChecksum(pathOutput, "LOCAL_FILE", "CRC8_DARC", checksum);
		True(checks, check);
	}

}
