package com.genexus.xmlsignature.test.dsig;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.TestSuite;
import org.junit.BeforeClass;
import org.junit.Test;

public class XmlSignatureDomainSpacesTest extends SecurityAPITestObject  {
	private static String path_RSA_sha1_1024;
	private static String xmlUnsigned;
	private static String dSigType;
	private static DSigOptions options;
	private static String pathKey;
	private static String pathCert;
	private static XmlDSigSigner signer;
	private static PrivateKeyManager key;
	private static CertificateX509 cert;

	@BeforeClass
	public static void setUp() {
		signer = new XmlDSigSigner();
		path_RSA_sha1_1024 = resources.concat("/dummycerts/RSA_sha1_1024/"); //"C:\\Temp\\dummycerts\\RSA_sha1_1024\\";
		xmlUnsigned = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<Envelope xmlns=\"http://example.org/envelope\">"
			+ "  <Body>" + "    Ola mundo" + "  </Body>" + "</Envelope>";
		dSigType = "ENVELOPED ";
		options = new DSigOptions();

		pathKey = path_RSA_sha1_1024 + "sha1d_key.pem";
		pathCert = path_RSA_sha1_1024 + "sha1_cert.crt";

		key = new PrivateKeyManager();
		cert = new CertificateX509();

	}

	@Test
	public void testDomains()
	{
		key.load(pathKey);
		assertFalse(key.hasError());
		cert.load(pathCert);
		assertFalse(cert.hasError());
		options.setDSigSignatureType(dSigType);
		options.setCanonicalization("C14n_OMIT_COMMENTS ");
		options.setKeyInfoType(" X509Certificate");
		String signed = signer.doSign(xmlUnsigned, key, cert, options);
		assertFalse(signer.hasError());
		boolean verified = signer.doVerify(signed, options);
		True(verified, signer);
	}
}
