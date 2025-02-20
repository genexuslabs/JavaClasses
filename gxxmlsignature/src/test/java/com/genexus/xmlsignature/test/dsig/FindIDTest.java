package com.genexus.xmlsignature.test.dsig;

import com.genexus.commons.DSigOptions;
import com.genexus.dsig.XmlDSigSigner;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.TestSuite;
import org.junit.BeforeClass;
import org.junit.Test;

public class FindIDTest extends SecurityAPITestObject {

	private static CertificateX509 cert;
	private static PrivateKeyManager key;
	private static XmlDSigSigner signer;
	private static DSigOptions options;
	private static String xmlInput;
	private static String xPath;

	@BeforeClass
	public static void setUp() {
		cert = new CertificateX509();
		cert.load(resources.concat("/dummycerts/RSA_sha256_1024/sha256_cert.crt"));//"C:\\Temp\\dummycerts\\RSA_sha256_1024\\sha256_cert.crt");
		key = new PrivateKeyManager();
		key.load(resources.concat("/dummycerts/RSA_sha256_1024/sha256d_key.pem")); //"C:\\Temp\\dummycerts\\RSA_sha256_1024\\sha256d_key.pem");

		signer = new XmlDSigSigner();
		options = new DSigOptions();

		options.setIdentifierAttribute("Id");

		xmlInput = "<envEvento xmlns=\"http://www.portalfiscal.inf.br/nfe\" versao=\"1.00\"><idLote>1</idLote><evento versao=\"1.00\"><infEvento Id=\"ID2102103521011431017000298855005000016601157405784801\"><cOrgao>91</cOrgao><tpAmb>1</tpAmb><CNPJ>31102046000145</CNPJ><chNFe>35210114310170002988550050000166011574057848</chNFe><dhEvento>2021-01-26T11:12:34-03:00</dhEvento><tpEvento>210210</tpEvento><nSeqEvento>1</nSeqEvento><verEvento>1.00</verEvento><detEvento versao=\"1.00\"><descEvento>Ciencia da Operacao</descEvento></detEvento></infEvento></evento></envEvento>";
		xPath = "#ID2102103521011431017000298855005000016601157405784801";
	}

	@Test
	public void testFindID() {
		String signed = signer.doSignElement(xmlInput, xPath, key, cert, options);
		assertFalse(SecurityUtils.compareStrings(signed, ""));
		assertFalse(signer.hasError());
	}

}
