package com.genexus.cryptography.test.asymmetric;

import com.genexus.cryptography.asymmetric.StandardSigner;
import com.genexus.cryptography.asymmetric.utils.SignatureStandardOptions;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class RSAStandardSigningTest extends SecurityAPITestObject {

	private static String path_RSA_sha1_1024;
	private static String path_RSA_sha256_1024;
	private static String path_RSA_sha256_2048;
	private static String path_RSA_sha512_2048;
	private static String[] encodings;
	private static EncodingUtil eu;

	private static String plainText;

	public static String alias;
	public static String password;

	@BeforeClass
	public static void setUp() {

		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/");
		path_RSA_sha256_1024 = resources.concat("/dummycerts/RSA_sha256_1024/");
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		path_RSA_sha512_2048 = resources.concat("/dummycerts/RSA_sha512_2048/");

		plainText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam venenatis ex sit amet risus pellentesque, a faucibus quam ultrices. Ut tincidunt quam eu aliquam maximus. Quisque posuere risus at erat blandit eleifend. Curabitur viverra rutrum volutpat. Donec quis quam tellus. Aenean fermentum elementum augue, a semper risus scelerisque sit amet. Nullam vitae sapien vitae dui ullamcorper dapibus quis quis leo. Sed neque felis, pellentesque in risus et, lobortis ultricies nulla. Quisque quis quam risus. Donec vestibulum, lectus vel vestibulum eleifend, velit ante volutpat lacus, ut mattis quam ligula eget est. Sed et pulvinar lectus. In mollis turpis non ipsum vehicula, sit amet rutrum nibh dictum. Duis consectetur convallis ex, eu ultricies enim bibendum vel. Vestibulum vel libero nibh. Morbi nec odio mattis, vestibulum quam blandit, pretium orci.Aenean pellentesque tincidunt nunc a malesuada. Etiam gravida fermentum mi, at dignissim dui aliquam quis. Nullam vel lobortis libero. Phasellus non gravida posuere";

		alias = "1";
		password = "dummy";

		encodings = new String[] {"UTF_8", "UTF_16", "UTF_16BE", "UTF_16LE", "UTF_32", "UTF_32BE", "UTF_32LE", "SJIS", "GB2312"};

		eu = new EncodingUtil();


	}

	private void bulkTest(SignatureStandardOptions options) {
		for (String encoding : encodings) {
			eu.setEncoding(encoding);
			test(options);
		}
	}
	private void test(SignatureStandardOptions options)
	{
		options.setEncapsulated(true);
		StandardSigner signer = new StandardSigner();
		String signed_encapsulated = signer.sign(plainText, options);
		boolean result_encapsulated = signer.verify(signed_encapsulated, "", options);
		True(result_encapsulated, signer);

		options.setEncapsulated(false);
		String signed = signer.sign(plainText, options);
		boolean result = signer.verify(signed, plainText, options);
		True(result, signer);
	}

	@Test
	public void test_sha1_1024_DER() {

		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);
	}

	@Test
	public void test_sha1_1024_PEM() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha1_1024_PKCS12() {
		String pathKey = path_RSA_sha1_1024 + "sha1_cert.p12";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_1024_DER() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_1024_PEM() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_1024_PKCS12() {
		String pathKey = path_RSA_sha256_1024 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_2048_DER() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_2048_PEM() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_2048_PKCS12() {
		String pathKey = path_RSA_sha256_2048 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha512_2048_DER() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha512_2048_PEM() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.load(pathKey);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha512_2048_PKCS12() {
		String pathKey = path_RSA_sha512_2048 + "sha512_cert.p12";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.p12";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadPKCS12(pathKey, alias, password);
		CertificateX509 cert = new CertificateX509();
		cert.loadPKCS12(pathCert, alias, password);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);
	}

	@Test
	public void test_base64()
	{
		String base64stringCert = "MIIEDzCCAvegAwIBAgIUdbZlqwWgv2JQlHp4SIAfMesMCKswDQYJKoZIhvcNAQELBQAwgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wHhcNMjUwNzA4MTUwODI3WhcNMzAwNzA3MTUwODI3WjCBljELMAkGA1UEBhMCVVkxEzARBgNVBAgMCk1vbnRldmlkZW8xEzARBgNVBAcMCk1vbnRldmlkZW8xEDAOBgNVBAoMB0dlbmVYdXMxETAPBgNVBAsMCFNlY3VyaXR5MRIwEAYDVQQDDAlzZ3JhbXBvbmUxJDAiBgkqhkiG9w0BCQEWFXNncmFtcG9uZUBnZW5leHVzLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALnQswXumxsNCyABl2u/+rRMZYGpN4OY9DXppahuffN/byQ5v092DYZ7X4bnpWI7xoP94NRoaLsIu0hzr1+5mMVnRbqyeU28opr/p3Aq0YX89utvbpumHDF7d5VPVSJ+wAf9i/6Ck5Pd/QdCBfnbKjJEFAM/HBKbhG2k0FtonkHSx/mxN0TTGL3dBuVcpO4p57fWqKqKiV3MtKo79OxbhigLjy5I3YxNhObE0n7ByKIDt5O4qvJ1DvRMTviLfG4D/U3f7VvmucAnxyQJc6uuodL4XsG5iPXdjgFX9wLIDdKPAvpuM7bEbDvdqGHpmmG1JfYlhBiZgNLKno0YyisY1DkCAwEAAaNTMFEwHQYDVR0OBBYEFFYk4TOHdhQ6Yk3sKcrouG7XgLBNMB8GA1UdIwQYMBaAFFYk4TOHdhQ6Yk3sKcrouG7XgLBNMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAH/DbEa70n+GAaqFViAoFuzcpxNHvjl8oH4LTeGK/dVVOYn58B79psf0EcsJ9LxrNgOCl9ZRuxLnat1/MGptCJjQeP5US8RtiTTpNnm5GbKdu6fq17ub9RjETlsX+OjDQuyk4+1B5DKo2bbqveWG+O7M2a7/7CVZ3wMu+Cg0FaUJAHBAOP68K7is21Fdy4CSxdvqOahKiComKQOBHfIjJsogUU/xR4Izw+HJV6oTbt39CXGhrKIli4CNxzAsJtDbkplwAN/wCooR8PEM6kbLTzJCY8JpS404Z8NG2tbKyKtc6Mz6iZ8oM1AVdzjoxyPx0YtehyNB39bMqIWO5U73yQ0=";
		String base64stringKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC50LMF7psbDQsgAZdrv/q0TGWBqTeDmPQ16aWobn3zf28kOb9Pdg2Ge1+G56ViO8aD/eDUaGi7CLtIc69fuZjFZ0W6snlNvKKa/6dwKtGF/Pbrb26bphwxe3eVT1UifsAH/Yv+gpOT3f0HQgX52yoyRBQDPxwSm4RtpNBbaJ5B0sf5sTdE0xi93QblXKTuKee31qiqioldzLSqO/TsW4YoC48uSN2MTYTmxNJ+wciiA7eTuKrydQ70TE74i3xuA/1N3+1b5rnAJ8ckCXOrrqHS+F7BuYj13Y4BV/cCyA3SjwL6bjO2xGw73ahh6ZphtSX2JYQYmYDSyp6NGMorGNQ5AgMBAAECggEAEBPwKZySbINzFFCO8D0q+DF+vFAOrvhWc9kaWGSXHrFGxfT+ihBGQ1NRhLQZKf16GS79Jo6SNqd0F7ogJpbsknLJJHUPl0Zchi+GJr0jEVuTZ0kRDQQRKcbr0KON/kTGtj+eFBDrAWm9McTot3cwmNYt1R9qJ8IFHLzyD7a8WtLjA/mNK6zYkimBpga1rWmkCyyNSP+KhEeUYGmig+LiubQmIwHbYrzDEeb1DGjKyE9upuwGs+nwdLStkEC7gOfsx7FM8lpnSeKstMjTmYz4j5TMyVdiIBY4yP9B6a6haFSNIpFm0YzNf0vesUHRh64HR/HCEkul34rOOaTqbjRlcQKBgQDrDT1AFzasJCJVUehtSmFNOUU2SIJLPC/Wh4VdrXtUNEcbxkjw3PFuzWkAixs99kUiefcGjKfVPUGOK8OV+6VUC5ckmYlHW1cx1iFwxcYfasmfb/tOQeWDmggPcITmNVlOolVwn+VYCUP4mt/B2wISNDu59QHV4u8+zZ3IToPBswKBgQDKYB4zA78GIw4/8/ywFH7NMz11BYnrfqLq5ZZBZvYML0nnzApZqtate+xUid5v5T++Koy4NCx+dMda6WOhRX3io9K3nrM1KiiWpynI4qHO06+LGH33rBFnE1dbhTx0SVU9UpCagauoNREUXnXi7py7fGdhlOsG8rcttRsd5BlkYwKBgQC7t2gKLj/QfE8bGn3oAnXwyWMX9hJwaVG/H54H8UtENTfw24tXKOx70/oen/mSo4IVBZidl2lV6ETZeOQLfNxNYbBEX4X+AdmCCIPOX3RZlNwOw8zMc94LGtGDGxZYD5USMpzPhDMR+txYx78ZP4HI7gQg/6WGnmT5IBb5aJLa9wKBgQCTxJ0oaMrZg01LWy8drslrscdlI/cx0dTJqXwOI0zzVrAjJbRFBt4b7ImCrOyTTZQ+mbkIY2g9qa1K73GE90XU8APTeXinEDJ01nhHK1w0thLOgMKxzp0iY1f9Bos+6bDoxtm5R4d8mcrv0Y1IdyxQJaUi9maqOx2PrVawe7YiuwKBgQC2G1+b8j8ddu/jVplBjIVK1Bnih0PUhRKVs5SbwPQwIq+MbGhKH4cuFQUgcyaLW76twchcL8eoebwwkLYhjQkbwSHane3/4TKJmD+ZfLRrRjaPKeFVngWTaK5OiebhvhZ6UFy+6JUE6u99H+sitG3ytL6/0MFplgV+7OnOcaaqLQ==";
		PrivateKeyManager key = new PrivateKeyManager();
		key.fromBase64(base64stringKey);
		CertificateX509 cert = new CertificateX509();
		cert.fromBase64(base64stringCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);
	}

	@Test
	public void test_sha1_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha1_1024 + "sha1_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_1024 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha256_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_2048 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}

	@Test
	public void test_sha512_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha512_2048 + "sha512_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		PrivateKeyManager key = new PrivateKeyManager();
		key.loadEncrypted(pathKey, password);
		CertificateX509 cert = new CertificateX509();
		cert.load(pathCert);
		SignatureStandardOptions options = new SignatureStandardOptions();
		options.setCertificate(cert);
		options.setPrivateKey(key);
		bulkTest(options);

	}
}
