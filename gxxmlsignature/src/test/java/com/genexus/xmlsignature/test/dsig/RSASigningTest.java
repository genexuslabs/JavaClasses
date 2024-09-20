package com.genexus.xmlsignature.test.dsig;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.utils.KeyInfoType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RSASigningTest extends SecurityAPITestObject {

	private static String path_RSA_sha1_1024;
	private static String path_RSA_sha256_1024;
	private static String path_RSA_sha256_2048;
	private static String path_RSA_sha512_2048;

	private static String xmlUnsigned;
	private static String xmlUnsignedPath;
	private static String xmlSignedPathRoot;

	private static String alias;
	private static String password;

	private static String dSigType;

	private static String[] arrayCanonicalization;
	private static String[] arrayKeyInfoType;

	private static DSigOptions options;
	private static DSigOptions optionsXPath;
	private static DSigOptions optionsID;

	private static Error error;

	private static String xmlUnsignedXPathFile;
	private static String xPath;
	private static String xmlUnsignedXPath;

	private static String xmlUnsignedIDPathFile;
	private static String id;
	private static String xmlUnsignedID;
	private static String xmlIDSchemaPath;

	@BeforeClass
	public static void setUp() {
		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/");
		path_RSA_sha256_1024 = resources.concat("/dummycerts/RSA_sha256_1024/");
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		path_RSA_sha512_2048 = resources.concat("/dummycerts/RSA_sha512_2048/");

		alias = "1";
		password = "dummy";

		xmlUnsigned = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Envelope xmlns=\"http://example.org/envelope\">"
			+ "  <Body>" + "    Ola mundo" + "  </Body>" + "</Envelope>";

		xmlUnsignedPath = resources.concat("/tosign.xml");
		xmlSignedPathRoot = tempFolder.toString();

		dSigType = "ENVELOPED";
		arrayCanonicalization = new String[]{"C14n_WITH_COMMENTS", "C14n_OMIT_COMMENTS", "exc_C14n_OMIT_COMMENTS",
			"exc_C14N_WITH_COMMENTS"};
		arrayKeyInfoType = new String[]{"KeyValue", "X509Certificate", "NONE"};

		options = new DSigOptions();

		optionsXPath = new DSigOptions();

		error = new Error();
		xmlUnsignedXPathFile = resources.concat("/bookSample.xml");
		xPath = "/bookstore/book[1]";

		xmlUnsignedIDPathFile = resources.concat("/xmlID.xml");
		String identifierAttribute = "id";
		id = "#tag1";
		xmlIDSchemaPath = resources.concat("/xmlIDSchema.xsd");

		optionsID = new DSigOptions();
		optionsID.setIdentifierAttribute(identifierAttribute);

		xmlUnsignedID = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<messages>\r\n" + "  <note id='tag1'>\r\n"
			+ "    <to>Tove</to>\r\n" + "    <from>Jani</from>\r\n" + "    <heading>Reminder</heading>\r\n"
			+ "    <body>Don't forget me this weekend!</body>\r\n" + "  </note>\r\n" + "  <note id='tag2'>\r\n"
			+ "    <to>Jani</to>\r\n" + "    <from>Tove</from>\r\n" + "    <heading>Re: Reminder</heading>\r\n"
			+ "    <body>I will not</body>\r\n" + "  </note>\r\n" + "</messages>";

		xmlUnsignedXPath = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<bookstore>\r\n"
			+ "<book category=\"cooking\">\r\n" + "  <title lang=\"en\">Everyday Italian</title>\r\n"
			+ "  <author>Giada De Laurentiis</author>\r\n" + "  <year>2005</year>\r\n"
			+ "  <price>30.00</price>\r\n" + "</book>\r\n" + "<book category=\"children\">\r\n"
			+ "  <title lang=\"en\">Harry Potter</title>\r\n" + "  <author>J K. Rowling</author>\r\n"
			+ "  <year>2005</year>\r\n" + "  <price>29.99</price>\r\n" + "</book>\r\n"
			+ "<book category=\"web\">\r\n" + "  <title lang=\"en\">XQuery Kick Start</title>\r\n"
			+ "  <author>James McGovern</author>\r\n" + "  <author>Per Bothner</author>\r\n"
			+ "  <author>Kurt Cagle</author>\r\n" + "  <author>James Linn</author>\r\n"
			+ "  <author>Vaidyanathan Nagarajan</author>\r\n" + "  <year>2003</year>\r\n"
			+ "  <price>49.99</price>\r\n" + "</book>\r\n" + "<book category=\"web\">\r\n"
			+ "  <title lang=\"en\">Learning XML</title>\r\n" + "  <author>Erik T. Ray</author>\r\n"
			+ "  <year>2003</year>\r\n" + "  <price>39.95</price>\r\n" + "</book>\r\n" + "</bookstore>";
	}


	private void bulkTest(String pathCert, String pathKey, String pathSigned, boolean ispkcs12, boolean isEncrypted, String hash) {
		for (String s : arrayKeyInfoType) {

			options.setKeyInfoType(s);
			optionsXPath.setKeyInfoType(s);
			optionsID.setKeyInfoType(s);

			PublicKey cert = hash != null ? loadPublicKey(pathCert, alias, password, true, ispkcs12) : loadPublicKey(pathCert, alias, password, false, ispkcs12);

			PrivateKeyManager key = new PrivateKeyManager();
			if (ispkcs12) {
				key.loadPKCS12(pathKey, alias, password);
			} else if (isEncrypted) {
				key.loadEncrypted(pathKey, password);
			} else {
				key.load(pathKey);
			}

			bulkTestWithKeyInfo(cert, key, pathSigned, hash);
			bulkTestWithKeyInfoXPath(cert, key, pathSigned, hash);
			bulkTestWithKeyInfoID(cert, key, pathSigned, hash);
		}

	}

	private PublicKey loadPublicKey(String path, String alias, String password, boolean isPublic, boolean ispkcs12) {
		if (isPublic) {
			PublicKey key = new PublicKey();
			key.load(path);
			return key;
		} else {
			PublicKey cert = new CertificateX509();
			if (ispkcs12) {
				cert.loadPKCS12(path, alias, password);
			} else {
				cert.load(path);
			}
			return cert;
		}
	}

	private boolean verifyWithCert(XmlDSigSigner signer, CertificateX509 certificate, String signedString, String signedPath, boolean isString, DSigOptions options) {
		KeyInfoType keyInfo = KeyInfoType.getKeyInfoType(options.getKeyInfoType(), error);
		if (isString) {
			return keyInfo != KeyInfoType.NONE ? signer.doVerify(signedString, options) : signer.doVerifyWithCert(signedString, certificate, options);
		} else {
			return keyInfo != KeyInfoType.NONE ? signer.doVerifyFile(signedPath, options) : signer.doVerifyFileWithCert(signedPath, certificate, options);
		}
	}

	private boolean verifyWithPublicKey(XmlDSigSigner signer, PublicKey publicKey, String signedString, String signedPath, boolean isString, DSigOptions options) {
		KeyInfoType keyInfo = KeyInfoType.getKeyInfoType(options.getKeyInfoType(), error);
		if (isString) {
			return keyInfo != KeyInfoType.NONE ? signer.doVerify(signedString, options) : signer.doVerifyWithPublicKey(signedString, publicKey, options);
		} else {
			return keyInfo != KeyInfoType.NONE ? signer.doVerifyFile(signedPath, options) : signer.doVerifyFileWithPublicKey(signedPath, publicKey, options);
		}
	}

	private void bulkTestWithKeyInfoXPath(PublicKey certificate, PrivateKeyManager key, String pathSigned, String hash) {
		XmlDSigSigner signer = new XmlDSigSigner();

		String pathSignedXPath = pathSigned + "_xPAth";

		for (String s : arrayCanonicalization) {

			optionsXPath.setDSigSignatureType(dSigType);
			optionsXPath.setCanonicalization(s);

			// TEST FILES

			if (hash != null) {
				if (!optionsXPath.getKeyInfoType().equals("X509Certificate")) {
					boolean signedFile = signer.doSignFileElementWithPublicKey(xmlUnsignedXPathFile, xPath, key, certificate, xmlSignedPathRoot + pathSignedXPath + ".xml", optionsXPath, hash);
					True(signedFile, signer);
					boolean verifyFile = verifyWithPublicKey(signer, certificate, "", xmlSignedPathRoot + pathSignedXPath + ".xml", false, optionsXPath);
					True(verifyFile, signer);
				} else {
					boolean signedFile = signer.doSignFileElementWithPublicKey(xmlUnsignedXPathFile, xPath, key, certificate, xmlSignedPathRoot + pathSignedXPath + ".xml", optionsXPath, hash);
					Assert.assertFalse(signedFile);
				}
			} else {
				boolean signedFile = signer.doSignFileElement(xmlUnsignedXPathFile, xPath, key, (CertificateX509) certificate,
					xmlSignedPathRoot + pathSignedXPath + ".xml", optionsXPath);
				True(signedFile, signer);
				boolean verifyFile = verifyWithCert(signer, (CertificateX509) certificate, "", xmlSignedPathRoot + pathSignedXPath + ".xml", false, optionsXPath);
				True(verifyFile, signer);
			}

			// TEST STRINGS

			if (hash != null) {
				if (!optionsXPath.getKeyInfoType().equals("X509Certificate")) {
					String signedString = signer.doSignElementWithPublicKey(xmlUnsignedXPath, xPath, key, certificate, optionsXPath, hash);
					boolean resultSignString = verifyWithPublicKey(signer, certificate, signedString, "", true, optionsXPath);
					True(resultSignString, signer);
				} else {
					String signedString = signer.doSignElementWithPublicKey(xmlUnsignedXPath, xPath, key, certificate, optionsXPath, hash);
					Assert.assertTrue(SecurityUtils.compareStrings(signedString, ""));
				}
			} else {
				String signedString = signer.doSignElement(xmlUnsignedXPath, xPath, key, (CertificateX509) certificate, optionsXPath);
				boolean resultSignString = verifyWithCert(signer, (CertificateX509) certificate, signedString, "", true, optionsXPath);
				True(resultSignString, signer);
			}
		}
	}

	private void bulkTestWithKeyInfo(PublicKey certificate, PrivateKeyManager key, String pathSigned, String hash) {
		XmlDSigSigner signer = new XmlDSigSigner();

		for (String s : arrayCanonicalization) {

			options.setDSigSignatureType(dSigType);
			options.setCanonicalization(s);

			// TEST FILES

			if (hash != null) {
				if (!optionsXPath.getKeyInfoType().equals("X509Certificate")) {
					boolean signedFile = signer.doSignFileWithPublicKey(xmlUnsignedPath, key, certificate, xmlSignedPathRoot + pathSigned + ".xml", options, hash);
					True(signedFile, signer);
					boolean verifyFile = verifyWithPublicKey(signer, certificate, "", xmlSignedPathRoot + pathSigned + ".xml", false, options);
					True(verifyFile, signer);
				} else {
					boolean signedFile = signer.doSignFileWithPublicKey(xmlUnsignedPath, key, certificate, xmlSignedPathRoot + pathSigned + ".xml", options, hash);
					Assert.assertFalse(signedFile);
				}
			} else {
				boolean signedFile = signer.doSignFile(xmlUnsignedPath, key, (CertificateX509) certificate,
					xmlSignedPathRoot + pathSigned + ".xml", options);
				True(signedFile, signer);
				boolean verifyFile = verifyWithCert(signer, (CertificateX509) certificate, "", xmlSignedPathRoot + pathSigned + ".xml", false, options);
				True(verifyFile, signer);
			}

			// TEST STRINGS

			if (hash != null) {
				if (!optionsXPath.getKeyInfoType().equals("X509Certificate")) {
					String signedString = signer.doSignWithPublicKey(xmlUnsigned, key, certificate, options, hash);
					boolean resultSignString = verifyWithPublicKey(signer, certificate, signedString, "", true, options);
					True(resultSignString, signer);
				} else {
					String signedString = signer.doSignWithPublicKey(xmlUnsigned, key, certificate, options, hash);
					Assert.assertTrue(SecurityUtils.compareStrings(signedString, ""));
				}
			} else {
				String signedString = signer.doSign(xmlUnsigned, key, (CertificateX509) certificate, options);
				boolean resultSignString = verifyWithCert(signer, (CertificateX509) certificate, signedString, "", true, options);
				True(resultSignString, signer);
			}
		}
	}

	private void bulkTestWithKeyInfoID(PublicKey certificate, PrivateKeyManager key, String pathSigned, String hash) {
		XmlDSigSigner signer = new XmlDSigSigner();
		String pathSignedID = pathSigned + "_id";
		for (String s : arrayCanonicalization) {

			optionsID.setDSigSignatureType(dSigType);
			optionsID.setCanonicalization(s);

			// TEST FILES

			if (hash != null) {
				if (!optionsXPath.getKeyInfoType().equals("X509Certificate")) {
					optionsID.setXmlSchemaPath(xmlIDSchemaPath);
					boolean signedFile = signer.doSignFileElementWithPublicKey(xmlUnsignedIDPathFile, id, key, certificate, xmlSignedPathRoot + pathSignedID + ".xml", optionsID, hash);
					True(signedFile, signer);
					optionsID.setXmlSchemaPath("");
					boolean verifyFile = verifyWithPublicKey(signer, certificate, "", xmlSignedPathRoot + pathSignedID + ".xml", false, optionsID);
					True(verifyFile, signer);
				} else {
					optionsID.setXmlSchemaPath(xmlIDSchemaPath);
					boolean signedFile = signer.doSignFileElementWithPublicKey(xmlUnsignedIDPathFile, id, key, certificate, xmlSignedPathRoot + pathSignedID + ".xml", optionsID, hash);
					Assert.assertFalse(signedFile);
				}
			} else {
				optionsID.setXmlSchemaPath(xmlIDSchemaPath);
				boolean signedFile = signer.doSignFileElement(xmlUnsignedIDPathFile, id, key, (CertificateX509) certificate,
					xmlSignedPathRoot + pathSignedID + ".xml", optionsID);
				True(signedFile, signer);

				optionsID.setXmlSchemaPath("");
				boolean verifyFile = verifyWithCert(signer, (CertificateX509) certificate, "", xmlSignedPathRoot + pathSignedID + ".xml", false, optionsID);
				True(verifyFile, signer);
			}

			// TEST STRINGS

			if (hash != null) {
				if (!optionsXPath.getKeyInfoType().equals("X509Certificate")) {
					optionsID.setXmlSchemaPath(xmlIDSchemaPath);
					String signedString = signer.doSignElementWithPublicKey(xmlUnsignedID, id, key, certificate, optionsID, hash);
					optionsID.setXmlSchemaPath("");
					boolean resultSignString = verifyWithPublicKey(signer, certificate, signedString, "", true, optionsID);
					True(resultSignString, signer);
				} else {
					String signedString = signer.doSignElementWithPublicKey(xmlUnsignedID, id, key, certificate, optionsID, hash);
					Assert.assertTrue(SecurityUtils.compareStrings(signedString, ""));
				}
			} else {
				optionsID.setXmlSchemaPath(xmlIDSchemaPath);

				String signedString = signer.doSignElement(xmlUnsignedID, id, key, (CertificateX509) certificate, optionsID);

				optionsID.setXmlSchemaPath("");
				boolean resultSignString = verifyWithCert(signer, (CertificateX509) certificate, signedString, "", true, optionsID);
				True(resultSignString, signer);
			}
		}
	}

	@Test
	public void test_sha1_1024_DER() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.crt";
		String pathSigned = "test_sha1_1024_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);

	}

	@Test
	public void test_sha1_1024_PEM() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		String pathSigned = "test_sha1_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);
	}

	@Test
	public void test_sha1_1024_PKCS12() {
		String pathKey = path_RSA_sha1_1024 + "sha1_cert.p12";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.p12";
		String pathSigned = "test_sha1_1024_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false, null);
	}

	@Test
	public void test_sha1_1024_PublicKey() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_pubkey.pem";
		String pathSigned = "test_sha1_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false, "SHA1");
	}

	@Test
	public void test_sha256_1024_DER() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.crt";
		String pathSigned = "test_sha256_1024_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);
	}

	@Test
	public void test_sha256_1024_PublicKey() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_pubkey.pem";
		String pathSigned = "test_sha256_1024_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false, "SHA256");
	}

	@Test
	public void test_sha256_1024_PEM() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		String pathSigned = "test_sha256_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);
	}

	@Test
	public void test_sha256_1024_PKCS12() {
		String pathKey = path_RSA_sha256_1024 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.p12";
		String pathSigned = "test_sha256_1024_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false, null);
	}

	@Test
	public void test_sha256_2048_DER() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.crt";
		String pathSigned = "test_sha256_2048_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);
	}

	@Test
	public void test_sha256_2048_PublicKey() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_pubkey.pem";
		String pathSigned = "test_sha256_2048_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false, "SHA256");
	}

	@Test
	public void test_sha256_2048_PEM() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		String pathSigned = "test_sha256_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);
	}

	@Test
	public void test_sha256_2048_PKCS12() {
		String pathKey = path_RSA_sha256_2048 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.p12";
		String pathSigned = "test_sha256_2048_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false, null);
	}

	@Test
	public void test_sha512_2048_DER() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.crt";
		String pathSigned = "test_sha512_2048_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);
	}

	@Test
	public void test_sha512_2048_PublicKey() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_pubkey.pem";
		String pathSigned = "test_sha512_2048_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false, "SHA512");
	}

	@Test
	public void test_sha512_2048_PEM() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		String pathSigned = "test_sha512_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false, null);
	}

	@Test
	public void test_sha512_2048_PKCS12() {
		String pathKey = path_RSA_sha512_2048 + "sha512_cert.p12";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.p12";
		String pathSigned = "test_sha512_2048_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false, null);
	}

	@Test
	public void test_sha1_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha1_1024 + "sha1_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		String pathSigned = "test_sha1_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true, null);
	}

	@Test
	public void test_sha256_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_1024 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		String pathSigned = "test_sha256_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true, null);
	}

	@Test
	public void test_sha256_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_2048 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		String pathSigned = "test_sha256_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true, null);
	}

	@Test
	public void test_sha512_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha512_2048 + "sha512_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		String pathSigned = "test_sha512_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true, null);
	}
}