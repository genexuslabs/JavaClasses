package com.genexus.cryptography.test.asymmetric;

import com.genexus.cryptography.asymmetric.StandardSigner;
import com.genexus.cryptography.asymmetric.utils.SignatureStandardOptions;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;
import junit.framework.Test;
import junit.framework.TestSuite;

public class ECDSAStandardSigningTest extends SecurityAPITestObject {

	private static String path_ecdsa_sha1;
	private static String path_ecdsa_sha256;

	private static String plainText;

	private static String alias;
	private static String password;

	private static String[] encodings;
	private static EncodingUtil eu;

	protected void setUp() {

		path_ecdsa_sha1 = "C:\\Temp\\dummycerts\\ECDSA_sha1\\";
		path_ecdsa_sha256 = "C:\\Temp\\dummycerts\\ECDSA_sha256\\";

		plainText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam venenatis ex sit amet risus pellentesque, a faucibus quam ultrices. Ut tincidunt quam eu aliquam maximus. Quisque posuere risus at erat blandit eleifend. Curabitur viverra rutrum volutpat. Donec quis quam tellus. Aenean fermentum elementum augue, a semper risus scelerisque sit amet. Nullam vitae sapien vitae dui ullamcorper dapibus quis quis leo. Sed neque felis, pellentesque in risus et, lobortis ultricies nulla. Quisque quis quam risus. Donec vestibulum, lectus vel vestibulum eleifend, velit ante volutpat lacus, ut mattis quam ligula eget est. Sed et pulvinar lectus. In mollis turpis non ipsum vehicula, sit amet rutrum nibh dictum. Duis consectetur convallis ex, eu ultricies enim bibendum vel. Vestibulum vel libero nibh. Morbi nec odio mattis, vestibulum quam blandit, pretium orci.Aenean pellentesque tincidunt nunc a malesuada. Etiam gravida fermentum mi, at dignissim dui aliquam quis. Nullam vel lobortis libero. Phasellus non gravida posuere";

		alias = "1";
		password = "dummy";

		encodings = new String[] { "UTF_8", "UTF_16", "UTF_16BE", "UTF_16LE", "UTF_32", "UTF_32BE", "UTF_32LE", "SJIS",
			"GB2312" };

		eu = new EncodingUtil();
	}

	private void bulkTest(SignatureStandardOptions options) {
		for (int i = 0; i < encodings.length; i++) {
			eu.setEncoding(encodings[i]);
			test(options);
		}
	}
	private void test(SignatureStandardOptions options)
	{
		options.setEncapsulated(true);
		StandardSigner signer = new StandardSigner();
		String signed_encapsulated = signer.sign(plainText, options);
		boolean result_encapsulated = signer.verify(signed_encapsulated, "", options);
		assertTrue(result_encapsulated);
		True(result_encapsulated, signer);

		options.setEncapsulated(false);
		String signed = signer.sign(plainText, options);
		boolean result = signer.verify(signed, plainText, options);
		assertTrue(result);
		True(result, signer);
	}

	public static Test suite() {
		return new TestSuite(ECDSAStandardSigningTest.class);
	}

	@Override
	public void runTest() {
		test_ecdsa_sha256_PEM();
		test_ecdsa_sha256_DER();
		test_ecdsa_sha256_PKCS12();

		test_ecdsa_sha1_PEM();
		test_ecdsa_sha1_DER();
		test_ecdsa_sha1_PKCS12();
	}

	public void test_ecdsa_sha1_PEM()
	{
		String pathKey = path_ecdsa_sha1 + "sha1_key.pem";
		String pathCert = path_ecdsa_sha1 + "sha1_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);
	}

	public void test_ecdsa_sha1_DER() {
		String pathKey = path_ecdsa_sha1 + "sha1_key.pem";
		String pathCert = path_ecdsa_sha1 + "sha1_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	public void test_ecdsa_sha1_PKCS12() {
		String pathKey = path_ecdsa_sha1 + "sha1_cert.p12";
		String pathCert = path_ecdsa_sha1 + "sha1_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);
	}

	public void test_ecdsa_sha256_PEM() {
		String pathKey = path_ecdsa_sha256 + "sha256_key.pem";
		String pathCert = path_ecdsa_sha256 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	public void test_ecdsa_sha256_DER() {
		String pathKey = path_ecdsa_sha256 + "sha256_key.pem";
		String pathCert = path_ecdsa_sha256 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	public void test_ecdsa_sha256_PKCS12() {
		String pathKey = path_ecdsa_sha256 + "sha256_cert.p12";
		String pathCert = path_ecdsa_sha256 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

}