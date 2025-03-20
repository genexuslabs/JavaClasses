package com.genexus.commons;


import com.genexus.securityapicommons.commons.Certificate;
import com.genexus.securityapicommons.commons.PrivateKey;
import com.genexus.securityapicommons.commons.SecurityAPIObject;

@SuppressWarnings("unused")
public abstract class XmlDSigSignerObject extends SecurityAPIObject {

	public XmlDSigSignerObject() {
		super();
	}

	public abstract boolean doSignFile(String xmlFilePath, PrivateKey key, Certificate certificate,
									   String outputPath, DSigOptions options);

	public abstract boolean doSignFileElement(String xmlFilePath, String xPath, PrivateKey key,
											  Certificate certificate, String outputPath, DSigOptions options);

	public abstract String doSign(String xmlInput, PrivateKey key, Certificate certificate,
								  DSigOptions options);

	public abstract String doSignElement(String xmlInput, String xPath, PrivateKey key,
										 Certificate certificate, DSigOptions options);

	public abstract boolean doVerify(String xmlSigned, DSigOptions options);

	public abstract boolean doVerifyFile(String xmlFilePath, DSigOptions options);

	public abstract boolean doVerifyWithCert(String xmlSigned, Certificate certificate, DSigOptions options);

	public abstract boolean doVerifyFileWithCert(String xmlFilePath, Certificate certificate, DSigOptions options);

}
