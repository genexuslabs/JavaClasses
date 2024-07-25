package com.genexus.xmlsignature.test.dsig;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;
import com.genexus.utils.KeyInfoType;

import junit.framework.Test;
import junit.framework.TestSuite;

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
	private static String identifierAttribute;
	private static String id;
	private static String xmlUnsignedID;
	private static String xmlIDSchemaPath;

	@Override
	protected void setUp() {
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
		arrayCanonicalization = new String[] { "C14n_WITH_COMMENTS", "C14n_OMIT_COMMENTS", "exc_C14n_OMIT_COMMENTS",
			"exc_C14N_WITH_COMMENTS" };
		arrayKeyInfoType = new String[] { "KeyValue", "X509Certificate", "NONE" };

		options = new DSigOptions();

		optionsXPath = new DSigOptions();

		error = new Error();
		xmlUnsignedXPathFile = resources.concat("/bookSample.xml");
		xPath = "/bookstore/book[1]";

		xmlUnsignedIDPathFile = resources.concat("/xmlID.xml");
		identifierAttribute = "id";
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

	public static Test suite() {
		return new TestSuite(RSASigningTest.class);
	}

	private void bulkTest(String pathCert, String pathKey, String pathSigned, boolean ispkcs12, boolean isEncrypted) {
		for (int k = 0; k < arrayKeyInfoType.length; k++) {
			options.setKeyInfoType(arrayKeyInfoType[k]);
			optionsXPath.setKeyInfoType(arrayKeyInfoType[k]);
			optionsID.setKeyInfoType(arrayKeyInfoType[k]);
			CertificateX509 cert = new CertificateX509();
			if (ispkcs12) {
				cert.loadPKCS12(pathCert, alias, password);
			} else {
				cert.load(pathCert);
			}
			PrivateKeyManager key = new PrivateKeyManager();
			if (ispkcs12) {
				key.loadPKCS12(pathKey, alias, password);
			} else if(isEncrypted){
				key.loadEncrypted(pathKey, password);
			}else {
				key.load(pathKey);
			}

			bulkTestWithKeyInfo(cert, key, pathSigned);
			bulkTestWithKeyInfoXPath(cert, key, pathSigned);
			bulkTestWithKeyInfoID(cert, key, pathSigned);
		}

	}

	private void bulkTestWithKeyInfoXPath(CertificateX509 certificate, PrivateKeyManager key, String pathSigned) {
		XmlDSigSigner signer = new XmlDSigSigner();

		String pathSignedXPath = pathSigned + "_xPAth";
		for (int c = 0; c < arrayCanonicalization.length; c++) {

			/**** TEST FILES ****/
			optionsXPath.setDSigSignatureType(dSigType);
			optionsXPath.setCanonicalization(arrayCanonicalization[c]);
			boolean signedFile = signer.doSignFileElement(xmlUnsignedXPathFile, xPath, key, certificate,
				xmlSignedPathRoot + pathSignedXPath + ".xml", optionsXPath);
			assertTrue(signedFile);
			True(signedFile, signer);

			boolean verifyFile = false;
			KeyInfoType keyInfo = KeyInfoType.getKeyInfoType(optionsXPath.getKeyInfoType(), error);
			if (keyInfo != KeyInfoType.NONE) {
				verifyFile = signer.doVerifyFile(xmlSignedPathRoot + pathSignedXPath + ".xml", optionsXPath);
			} else {
				verifyFile = signer.doVerifyFileWithCert(xmlSignedPathRoot + pathSignedXPath + ".xml", certificate,
					optionsXPath);
			}
			assertTrue(verifyFile);
			True(verifyFile, signer);

			/**** TEST STRINGS ****/

			String signedString = signer.doSignElement(xmlUnsignedXPath, xPath, key, certificate, optionsXPath);
			boolean resultSignString = false;
			if (keyInfo != KeyInfoType.NONE) {
				resultSignString = signer.doVerify(signedString, optionsXPath);
			} else {
				resultSignString = signer.doVerifyWithCert(signedString, certificate, optionsXPath);

			}
			assertTrue(resultSignString);
			True(resultSignString, signer);

		}
	}

	private void bulkTestWithKeyInfo(CertificateX509 certificate, PrivateKeyManager key, String pathSigned) {
		XmlDSigSigner signer = new XmlDSigSigner();

		for (int c = 0; c < arrayCanonicalization.length; c++) {

			/**** TEST FILES ****/
			options.setDSigSignatureType(dSigType);
			options.setCanonicalization(arrayCanonicalization[c]);
			boolean signedFile = signer.doSignFile(xmlUnsignedPath, key, certificate,
				xmlSignedPathRoot + pathSigned + ".xml", options);
			assertTrue(signedFile);
			True(signedFile, signer);

			boolean verifyFile = false;
			KeyInfoType keyInfo = KeyInfoType.getKeyInfoType(options.getKeyInfoType(), error);
			if (keyInfo != KeyInfoType.NONE) {
				verifyFile = signer.doVerifyFile(xmlSignedPathRoot + pathSigned + ".xml", options);
			} else {
				verifyFile = signer.doVerifyFileWithCert(xmlSignedPathRoot + pathSigned + ".xml", certificate, options);
			}
			assertTrue(verifyFile);
			True(verifyFile, signer);

			/**** TEST STRINGS ****/

			String signedString = signer.doSign(xmlUnsigned, key, certificate, options);
			boolean resultSignString = false;
			if (keyInfo != KeyInfoType.NONE) {
				resultSignString = signer.doVerify(signedString, options);
			} else {
				resultSignString = signer.doVerifyWithCert(signedString, certificate, options);

			}
			assertTrue(resultSignString);
			True(resultSignString, signer);

		}
	}

	private void bulkTestWithKeyInfoID(CertificateX509 certificate, PrivateKeyManager key, String pathSigned) {
		XmlDSigSigner signer = new XmlDSigSigner();
		String pathSignedID = pathSigned + "_id";
		for (int c = 0; c < arrayCanonicalization.length; c++) {

			/**** TEST FILES ****/
			optionsID.setDSigSignatureType(dSigType);
			optionsID.setCanonicalization(arrayCanonicalization[c]);

			optionsID.setXmlSchemaPath(xmlIDSchemaPath);
			boolean signedFile = signer.doSignFileElement(xmlUnsignedIDPathFile, id, key, certificate,
				xmlSignedPathRoot + pathSignedID + ".xml", optionsID);
			assertTrue(signedFile);
			True(signedFile, signer);

			boolean verifyFile = false;
			optionsID.setXmlSchemaPath("");
			KeyInfoType keyInfo = KeyInfoType.getKeyInfoType(optionsID.getKeyInfoType(), error);
			if (keyInfo != KeyInfoType.NONE) {

				verifyFile = signer.doVerifyFile(xmlSignedPathRoot + pathSignedID + ".xml", optionsID);
			} else {
				verifyFile = signer.doVerifyFileWithCert(xmlSignedPathRoot + pathSignedID + ".xml", certificate,
					optionsID);
			}
			assertTrue(verifyFile);
			True(verifyFile, signer);

			/**** TEST STRINGS ****/
			optionsID.setXmlSchemaPath(xmlIDSchemaPath);
			String signedString = signer.doSignElement(xmlUnsignedID, id, key, certificate, optionsID);
			boolean resultSignString = false;

			optionsID.setXmlSchemaPath("");
			if (keyInfo != KeyInfoType.NONE) {
				resultSignString = signer.doVerify(signedString, optionsID);
			} else {
				resultSignString = signer.doVerifyWithCert(signedString, certificate, optionsID);

			}
			assertTrue(resultSignString);
			True(resultSignString, signer);

		}
	}

	@Override
	public void runTest() {
		test_sha1_1024_DER();
		test_sha1_1024_PEM();
		test_sha1_1024_PEM_Encrypted();
		test_sha1_1024_PKCS12();

		test_sha256_1024_DER();
		test_sha256_1024_PEM();
		test_sha256_1024_PEM_Encrypted();
		test_sha256_1024_PKCS12();

		test_sha256_2048_DER();
		test_sha256_2048_PEM();
		test_sha256_2048_PEM_Encrypted();
		test_sha256_2048_PKCS12();

		test_sha512_2048_DER();
		test_sha512_2048_PEM();
		test_sha512_2048_PEM_Encrypted();
		test_sha512_2048_PKCS12();


	}

	public void test_sha1_1024_DER() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.crt";
		String pathSigned = "test_sha1_1024_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false);

	}

	public void test_sha1_1024_PEM() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		String pathSigned = "test_sha1_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}



	public void test_sha1_1024_PKCS12() {
		String pathKey = path_RSA_sha1_1024 + "sha1_cert.p12";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.p12";
		String pathSigned = "test_sha1_1024_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false);
	}

	public void test_sha1_1024_PublicKey() {
		String pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_pubkey.pem";
		String pathSigned = "test_sha1_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}

	public void test_sha256_1024_DER() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.crt";
		String pathSigned = "test_sha256_1024_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}

	public void test_sha256_1024_PEM() {
		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		String pathSigned = "test_sha256_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}



	public void test_sha256_1024_PKCS12() {
		String pathKey = path_RSA_sha256_1024 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.p12";
		String pathSigned = "test_sha256_1024_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false);
	}

	public void test_sha256_2048_DER() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.crt";
		String pathSigned = "test_sha256_2048_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}

	public void test_sha256_2048_PEM() {
		String pathKey = path_RSA_sha256_2048 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		String pathSigned = "test_sha256_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}



	public void test_sha256_2048_PKCS12() {
		String pathKey = path_RSA_sha256_2048 + "sha256_cert.p12";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.p12";
		String pathSigned = "test_sha256_2048_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false);
	}

	public void test_sha512_2048_DER() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.crt";
		String pathSigned = "test_sha512_2048_DER";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}

	public void test_sha512_2048_PEM() {
		String pathKey = path_RSA_sha512_2048 + "sha512d_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		String pathSigned = "test_sha512_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, false);
	}



	public void test_sha512_2048_PKCS12() {
		String pathKey = path_RSA_sha512_2048 + "sha512_cert.p12";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.p12";
		String pathSigned = "test_sha512_2048_PKCS12";
		bulkTest(pathCert, pathKey, pathSigned, true, false);
	}

	public void test_sha1_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha1_1024 + "sha1_key.pem";
		String pathCert = path_RSA_sha1_1024 + "sha1_cert.pem";
		String pathSigned = "test_sha1_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true);
	}

	public void test_sha256_1024_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_1024 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.pem";
		String pathSigned = "test_sha256_1024_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true);
	}

	public void test_sha256_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha256_2048 + "sha256_key.pem";
		String pathCert = path_RSA_sha256_2048 + "sha256_cert.pem";
		String pathSigned = "test_sha256_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true);
	}

	public void test_sha512_2048_PEM_Encrypted() {
		String pathKey = path_RSA_sha512_2048 + "sha512_key.pem";
		String pathCert = path_RSA_sha512_2048 + "sha512_cert.pem";
		String pathSigned = "test_sha512_2048_PEM";
		bulkTest(pathCert, pathKey, pathSigned, false, true);
	}
}
