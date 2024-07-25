package com.genexus.cryptography.test.asymmetric;

import com.genexus.cryptography.asymmetric.AsymmetricSigner;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ECDSASigningTest extends SecurityAPITestObject {
	private static String path_ecdsa_sha1;
	private static String path_ecdsa_sha256;

	private static String plainText;
	private static String pathFile;

	private static String alias;
	private static String password;

	private static String[] encodings;
	private static EncodingUtil eu;

	@Override
	protected void setUp() {

		path_ecdsa_sha1 = resources.concat("/dummycerts/ECDSA_sha1/");
		path_ecdsa_sha256 = resources.concat("/dummycerts/ECDSA_sha256/");

		plainText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam venenatis ex sit amet risus pellentesque, a faucibus quam ultrices. Ut tincidunt quam eu aliquam maximus. Quisque posuere risus at erat blandit eleifend. Curabitur viverra rutrum volutpat. Donec quis quam tellus. Aenean fermentum elementum augue, a semper risus scelerisque sit amet. Nullam vitae sapien vitae dui ullamcorper dapibus quis quis leo. Sed neque felis, pellentesque in risus et, lobortis ultricies nulla. Quisque quis quam risus. Donec vestibulum, lectus vel vestibulum eleifend, velit ante volutpat lacus, ut mattis quam ligula eget est. Sed et pulvinar lectus. In mollis turpis non ipsum vehicula, sit amet rutrum nibh dictum. Duis consectetur convallis ex, eu ultricies enim bibendum vel. Vestibulum vel libero nibh. Morbi nec odio mattis, vestibulum quam blandit, pretium orci.Aenean pellentesque tincidunt nunc a malesuada. Etiam gravida fermentum mi, at dignissim dui aliquam quis. Nullam vel lobortis libero. Phasellus non gravida posuere";
		pathFile = resources.concat("/flag.jpg");

		alias = "1";
		password = "dummy";

		encodings = new String[] { "UTF_8", "UTF_16", "UTF_16BE", "UTF_16LE", "UTF_32", "UTF_32BE", "UTF_32LE", "SJIS",
			"GB2312" };

		eu = new EncodingUtil();
	}

	private void bulkTest(PrivateKeyManager key, PublicKey cert, String hashAlgorithm, boolean isPublicKey)
	{
		bulkTestText( key,  cert,  hashAlgorithm, isPublicKey);
		bulkTestFile( key,  cert,  hashAlgorithm, isPublicKey);

	}

	private void bulkTestText(PrivateKeyManager key, PublicKey cert, String hashAlgorithm, boolean isPublicKey) {
		AsymmetricSigner asymSig = new AsymmetricSigner();
		String signature = asymSig.doSign(key, hashAlgorithm, plainText);
		boolean result = isPublicKey ? asymSig.doVerifyWithPublicKey(cert, plainText, signature, hashAlgorithm): asymSig.doVerify((CertificateX509)cert, plainText, signature);
		assertTrue(result);
		True(result, asymSig);
	}

	private void bulkTestFile(PrivateKeyManager key, PublicKey cert, String hashAlgorithm, boolean isPublicKey) {
		AsymmetricSigner asymSig = new AsymmetricSigner();
		String signature = asymSig.doSignFile(key, hashAlgorithm, pathFile);
		boolean result = isPublicKey ? asymSig.doVerifyFileWithPublicKey(cert, pathFile, signature, hashAlgorithm): asymSig.doVerifyFile((CertificateX509)cert, pathFile, signature);
		assertTrue(result);
		True(result, asymSig);
	}

	private void runTestWithEncoding(PrivateKeyManager key, PublicKey cert, String hash, boolean isPublicKey) {
		for (int i = 0; i < encodings.length; i++) {
			eu.setEncoding(encodings[i]);
			bulkTest(key, cert, hash, isPublicKey);
		}
	}

	public static Test suite() {
		return new TestSuite(ECDSASigningTest.class);
	}

	public void test_ecdsa_sha1_PEM() {
		String pathKey = path_ecdsa_sha1 + "sha1_key.pem";
		String pathCert = path_ecdsa_sha1 + "sha1_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA1", false);

	}

	public void test_ecdsa_sha1_DER() {
		String pathKey = path_ecdsa_sha1 + "sha1_key.pem";
		String pathCert = path_ecdsa_sha1 + "sha1_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA1", false);

	}

	public void test_ecdsa_sha1_PKCS12() {
		String pathKey = path_ecdsa_sha1 + "sha1_cert.p12";
		String pathCert = path_ecdsa_sha1 + "sha1_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		runTestWithEncoding(key, cert, "SHA1", false);
	}

	public void test_ecdsa_sha256_PEM() {
		String pathKey = path_ecdsa_sha256 + "sha256_key.pem";
		String pathCert = path_ecdsa_sha256 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_ecdsa_sha256_DER() {
		String pathKey = path_ecdsa_sha256 + "sha256_key.pem";
		String pathCert = path_ecdsa_sha256 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_ecdsa_sha256_PKCS12() {
		String pathKey = path_ecdsa_sha256 + "sha256_cert.p12";
		String pathCert = path_ecdsa_sha256 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		runTestWithEncoding(key, cert, "SHA256", false);

	}

	public void test_ecdsa_sha256_PublicKey() {
		String pathKey = path_ecdsa_sha256 + "sha256_key.pem";
		String pathCert = path_ecdsa_sha256 + "sha256_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		PublicKey cert = new PublicKey();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA256", true);

	}

	public void test_ecdsa_sha1_PublicKey() {
		String pathKey = path_ecdsa_sha1 + "sha1_key.pem";
		String pathCert = path_ecdsa_sha1 + "sha1_pubkey.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		PublicKey cert = new PublicKey();
		cert.load(pathCert);
		runTestWithEncoding(key, cert, "SHA1", true);

	}

	@Override
	public void runTest() {
		test_ecdsa_sha256_PEM();
		test_ecdsa_sha256_DER();
		test_ecdsa_sha256_PKCS12();

		test_ecdsa_sha1_PEM();
		test_ecdsa_sha1_DER();
		test_ecdsa_sha1_PKCS12();

		test_ecdsa_sha256_PublicKey();
		test_ecdsa_sha1_PublicKey();

	}

}
