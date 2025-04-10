package com.genexus.dsig;

import com.genexus.commons.DSigOptions;
import com.genexus.config.Config;
import com.genexus.securityapicommons.commons.Certificate;
import com.genexus.securityapicommons.commons.Key;
import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.algorithms.SignatureAlgorithm;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.transforms.params.XPathContainer;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class XmlDSigSigner extends SecurityAPIObject {

	private static final Logger logger = LogManager.getLogger(XmlDSigSigner.class);

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private String digest;
	private String asymAlgorithm;

	public XmlDSigSigner() {
		super();
		if (Config.getUseLineBreaks()) {
			org.apache.xml.security.Init.init();
		} else {
			/*** CONDITIONAL ***/
			/** https://issues.apache.org/jira/browse/SANTUARIO-482 **/
			System.setProperty("org.apache.xml.security.ignoreLineBreaks", "true");
			org.apache.xml.security.Init.init();
		}

	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public boolean doSignFile(String xmlFilePath, com.genexus.securityapicommons.commons.PrivateKey privateKey,
							  Certificate certificate, String outputPath, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doSignFile");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFile", "xmlFilePath", xmlFilePath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFile", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFile", "certificate", certificate, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFile", "outputPath", outputPath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFile", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		return Boolean.parseBoolean(axuiliarSign(xmlFilePath, privateKey, certificate, outputPath, options, true, "", null));
	}

	public boolean doSignFileWithPublicKey(String xmlFilePath, com.genexus.securityapicommons.commons.PrivateKey privateKey, com.genexus.securityapicommons.commons.PublicKey publicKey, String outputPath, DSigOptions options, String hash) {
		this.error.cleanError();
		logger.debug("doSignFileWithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileWithPublicKey", "xmlFilePath", xmlFilePath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileWithPublicKey", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileWithPublicKey", "publicKey", publicKey, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileWithPublicKey", "outputPath", outputPath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileWithPublicKey", "options", options, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileWithPublicKey", "hash", hash, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		return Boolean.parseBoolean(axuiliarSign(xmlFilePath, privateKey, publicKey, outputPath, options, true, "", hash));
	}

	public String doSign(String xmlInput, com.genexus.securityapicommons.commons.PrivateKey privateKey,
						 Certificate certificate, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doSign");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSign", "xmlInput", xmlInput, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSign", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSign", "certificate", certificate, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSign", "options", options, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return axuiliarSign(xmlInput, privateKey, certificate, "", options, false, "", null);
	}

	public String doSignWithPublicKey(String xmlInput, com.genexus.securityapicommons.commons.PrivateKey privateKey, com.genexus.securityapicommons.commons.PublicKey publicKey, DSigOptions options, String hash) {
		this.error.cleanError();
		logger.debug("doSignWithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignWithPublicKey", "xmlInput", xmlInput, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignWithPublicKey", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignWithPublicKey", "publicKey", publicKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignWithPublicKey", "options", options, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignWithPublicKey", "hash", hash, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return axuiliarSign(xmlInput, privateKey, publicKey, "", options, false, "", hash);
	}

	public boolean doSignFileElement(String xmlFilePath, String xPath,
									 com.genexus.securityapicommons.commons.PrivateKey privateKey, Certificate certificate, String outputPath,
									 DSigOptions options) {
		this.error.cleanError();
		logger.debug("doSignFileElement");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileElement", "xmlFilePath", xmlFilePath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileElement", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileElement", "certificate", certificate, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileElement", "outputPath", outputPath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileElement", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		return Boolean.parseBoolean(axuiliarSign(xmlFilePath, privateKey, certificate, outputPath, options, true, xPath, null));
	}

	public boolean doSignFileElementWithPublicKey(String xmlFilePath, String xPath,
												  com.genexus.securityapicommons.commons.PrivateKey privateKey, com.genexus.securityapicommons.commons.PublicKey publicKey, String outputPath,
												  DSigOptions options, String hash) {
		this.error.cleanError();
		logger.debug("doSignFileElementWithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileElementWithPublicKey", "xmlFilePath", xmlFilePath, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileElementWithPublicKey", "xPath", xPath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileElementWithPublicKey", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileElementWithPublicKey", "publicKey", publicKey, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileElementWithPublicKey", "outputPath", outputPath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignFileElementWithPublicKey", "options", options, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignFileElementWithPublicKey", "hash", hash, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		return Boolean.parseBoolean(axuiliarSign(xmlFilePath, privateKey, publicKey, outputPath, options, true, xPath, hash));
	}

	public String doSignElement(String xmlInput, String xPath, com.genexus.securityapicommons.commons.PrivateKey privateKey,
								Certificate certificate, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doSignElement");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignElement", "xmlInput", xmlInput, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignElement", "xPath", xPath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignElement", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignElement", "certificate", certificate, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignElement", "options", options, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return axuiliarSign(xmlInput, privateKey, certificate, "", options, false, xPath, null);
	}

	public String doSignElementWithPublicKey(String xmlInput, String xPath, com.genexus.securityapicommons.commons.PrivateKey privateKey, com.genexus.securityapicommons.commons.PublicKey publicKey
		, DSigOptions options, String hash) {
		this.error.cleanError();
		logger.debug("doSignElementWithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignElementWithPublicKey", "xmlInput", xmlInput, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignElementWithPublicKey", "xPath", xPath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignElementWithPublicKey", "privateKey", privateKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignElementWithPublicKey", "publicKey", publicKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doSignElementWithPublicKey", "options", options, this.error);
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doSignElementWithPublicKey", "hash", hash, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return axuiliarSign(xmlInput, privateKey, publicKey, "", options, false, xPath, hash);
	}

	public boolean doVerify(String xmlSigned, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doVerify");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doVerify", "xmlSigned", xmlSigned, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerify", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		return auxiliarVerify(xmlSigned, options, false, false, null);
	}

	public boolean doVerifyFile(String xmlFilePath, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doVerifyFile");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doVerifyFile", "xmlFilePath", xmlFilePath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyFile", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		return auxiliarVerify(xmlFilePath, options, true, false, null);
	}

	public boolean doVerifyWithCert(String xmlSigned, Certificate certificate, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doVerifyWithCert");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doVerifyWithCert", "xmlSigned", xmlSigned, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyWithCert", "certificate", certificate, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyWithCert", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		return auxiliarVerify(xmlSigned, options, false, true, certificate);

	}

	public boolean doVerifyFileWithCert(String xmlFilePath, Certificate certificate, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doVerifyFileWithCert");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doVerifyFileWithCert", "xmlFilePath", xmlFilePath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyFileWithCert", "certificate", certificate, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyFileWithCert", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		return auxiliarVerify(xmlFilePath, options, true, true, certificate);
	}

	public boolean doVerifyWithPublicKey(String xmlSigned, com.genexus.securityapicommons.commons.PublicKey publicKey, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doVerifyWithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doVerifyWithPublicKey", "xmlSigned", xmlSigned, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyWithPublicKey", "publicKey", publicKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyWithPublicKey", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		return auxiliarVerify(xmlSigned, options, false, true, publicKey);
	}

	public boolean doVerifyFileWithPublicKey(String xmlFilePath, com.genexus.securityapicommons.commons.PublicKey publicKey, DSigOptions options) {
		this.error.cleanError();
		logger.debug("doVerifyFileWithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(XmlDSigSigner.class), "doVerifyFileWithPublicKey", "xmlFilePath", xmlFilePath, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyFileWithPublicKey", "publicKey", publicKey, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(XmlDSigSigner.class), "doVerifyFileWithPublicKey", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		return auxiliarVerify(xmlFilePath, options, true, true, publicKey);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private String axuiliarSign(String xmlInput, com.genexus.securityapicommons.commons.PrivateKey key,
								Key publicKey, String outputPath, DSigOptions options, boolean isFile, String xPath, String hash) {
		logger.debug("axuiliarSign");
		if (TransformsWrapper.getTransformsWrapper(options.getDSigSignatureType(),
			this.error) != TransformsWrapper.ENVELOPED) {
			error.setError("XD001", "Not implemented DSigType");
		}

		com.genexus.securityapicommons.commons.PublicKey cert = (hash != null) ? (com.genexus.securityapicommons.commons.PublicKey) publicKey : (CertificateX509) publicKey;
		if (cert.hasError()) {
			this.error = cert.getError();
			return "";
		}

		Document xmlDoc = loadDocument(isFile, xmlInput, options);
		if (this.hasError()) {
			return "";
		}
		String result = Sign(xmlDoc, (PrivateKeyManager) key, cert, options.getDSigSignatureType(),
			options.getCanonicalization(), options.getKeyInfoType(), xPath, options.getIdentifierAttribute(), hash);
		if (isFile) {
			if ((result == null) || SecurityUtils.compareStrings(result, "")) {
				return "false";
			} else {
				String prefix = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
				return Boolean.toString(SignatureUtils.writeToFile(result, outputPath, prefix, this.error));
			}
		}
		return result;
	}

	@SuppressWarnings("DataFlowIssue")
	private String Sign(Document xmlInput, PrivateKeyManager key, com.genexus.securityapicommons.commons.PublicKey certificate, String dSigType,
						String canonicalizationType, String keyInfoType, String xpath, String id, String hash) {
		logger.debug("Sign");
		SignatureElementType signatureElementType;
		if (!SecurityUtils.compareStrings(xpath, "")) {
			if (xpath.charAt(0) == '#') {
				signatureElementType = SignatureElementType.id;
				if (id == null || SecurityUtils.compareStrings(id, "")) {
					this.error.setError("XD003", "Identifier attribute name missing");
					return "";
				}
			} else {
				signatureElementType = SignatureElementType.path;
			}
		} else {
			signatureElementType = SignatureElementType.document;
		}
		inicializeInstanceVariables(key, certificate, hash);
		Element rootElement = SignatureUtils.getRootElement(xmlInput);

		CanonicalizerWrapper canonicalizerWrapper = CanonicalizerWrapper.getCanonicalizerWrapper(canonicalizationType,
			this.error);
		String canonicalizationMethodAlgorithm = CanonicalizerWrapper
			.getCanonicalizationMethodAlorithm(canonicalizerWrapper, error);
		XMLSignatureWrapper xMLSignatureWrapper = XMLSignatureWrapper
			.getXMLSignatureWrapper(this.asymAlgorithm + "_" + this.digest, this.error);
		TransformsWrapper transformsWrapper = TransformsWrapper.getTransformsWrapper(dSigType, this.error);
		String signatureTypeTransform = TransformsWrapper.getSignatureTypeTransform(transformsWrapper, this.error);
		MessageDigestAlgorithmWrapper messageDigestAlgorithmWrapper = MessageDigestAlgorithmWrapper
			.getMessageDigestAlgorithmWrapper(this.digest, this.error);
		if (this.hasError()) {
			return "";
		}

		Element canonElem = XMLUtils.createElementInSignatureSpace(xmlInput, Constants._TAG_CANONICALIZATIONMETHOD);
		canonElem.setAttributeNS(null, Constants._ATT_ALGORITHM, canonicalizationMethodAlgorithm);
		SignatureAlgorithm signatureAlgorithm = null;
		XMLSignature sig = null;
		try {
			signatureAlgorithm = new SignatureAlgorithm(xmlInput,
				XMLSignatureWrapper.getSignatureMethodAlgorithm(xMLSignatureWrapper, this.error));
			if (this.hasError()) {
				return "";
			}
			sig = new XMLSignature(xmlInput, null, signatureAlgorithm.getElement(), canonElem);
		} catch (Exception e) {
			this.error.setError("XD004", e.getMessage());
			logger.error("Sign", e);
			return null;
		}

		Transforms transforms = new Transforms(xmlInput);
		String referenceURI = "";
		try {
			transforms.addTransform(signatureTypeTransform);
			transforms.addTransform(canonicalizationMethodAlgorithm);
			switch (signatureElementType) {
				case path:
					Node xpathNode = SignatureUtils.getNodeFromPath(xmlInput, xpath, this.error);
					if (this.hasError() || xpathNode == null) {
						return "";
					}
					Node parentNode = xpathNode.getParentNode();
					parentNode.appendChild(sig.getElement());
					XPathContainer xpathC = new XPathContainer(xmlInput);
					xpathC.setXPath(xpath);
					transforms.addTransform(Transforms.TRANSFORM_XPATH, xpathC.getElementPlusReturns());
					break;
				case id:
					Node idNode = SignatureUtils.getNodeFromID(xmlInput, id, xpath, this.error);
					if (this.hasError()) {
						return "";
					}
					Element idElement = (Element) idNode;
					idElement.setIdAttribute(id, true);
					referenceURI = xpath;
					Node parentNodeID = idNode.getParentNode();
					parentNodeID.appendChild(sig.getElement());
					break;
				default:
					rootElement.appendChild(sig.getElement());
					break;
			}
			sig.addDocument(referenceURI, transforms,
				MessageDigestAlgorithmWrapper.getDigestMethod(messageDigestAlgorithmWrapper, this.error));
		} catch (Exception e) {
			this.error.setError("XD005", e.getMessage());
			logger.error("Sign", e);
			return "";
		}
		KeyInfoType kyInfo = KeyInfoType.getKeyInfoType(keyInfoType, this.error);
		if (this.hasError()) {
			return "";
		}
		switch (kyInfo) {

			case X509Certificate:

				if (hash != null) {
					this.error.setError("XD002", "The file included is a Public Key, cannot include a certificate on the signature");
					logger.error("Sign - The file included is a Public Key, cannot include a certificate on the signature");
					return "";
				}
				try {
					X509Certificate x509Certificate = ((CertificateX509) certificate).Cert();
					X509Data x509data = new X509Data(sig.getDocument());
					x509data.addIssuerSerial(x509Certificate.getIssuerDN().getName(), x509Certificate.getSerialNumber());
					x509data.addSubjectName(x509Certificate);
					x509data.addCertificate(x509Certificate);
					sig.getKeyInfo().add(x509data);
				} catch (Exception e) {
					this.error.setError("XD006", e.getMessage());
				}
				break;
			case KeyValue:
				sig.addKeyInfo(this.publicKey);
				break;
			case NONE:
				break;
			default:
				this.error.setError("XD007", "Undefined KeyInfo type");
				logger.error("Sign - Undefined KeyInfo type");
				return "";
		}
		try {
			sig.sign(this.privateKey);
		} catch (Exception e) {
			error.setError("XD008", e.getMessage());
			return null;
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		XMLUtils.outputDOMc14nWithComments(xmlInput, bos);

		return bos.toString();
	}

	private void inicializeInstanceVariables(PrivateKeyManager key, com.genexus.securityapicommons.commons.PublicKey certificate, String hash) {
		this.privateKey = key.getPrivateKey();
		this.publicKey = certificate.getPublicKey();
		this.asymAlgorithm = certificate.getAlgorithm();
		if (hash == null) {
			this.digest = ((CertificateX509) certificate).getPublicKeyHash();
		} else {
			this.digest = hash;
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private boolean auxiliarVerify(String input, DSigOptions options, boolean isFile, boolean withCert,
								   Key key) {
		logger.debug("auxiliarVerify");
		if (TransformsWrapper.getTransformsWrapper(options.getDSigSignatureType(),
			this.error) != TransformsWrapper.ENVELOPED) {
			error.setError("XD001", "Not implemented DSigType");
			logger.error("auxiliarVerify - Not implemented DSigType");
		}
		Document doc = loadDocument(isFile, input, options);
		if (this.hasError()) {
			return false;
		}
		if (isFile) {

			try {
				File f = new File(input);
				String baseURI = f.toURI().toURL().toString();
				return verify(doc, baseURI, options.getIdentifierAttribute(), withCert, key);
			} catch (Exception e) {
				this.error.setError("XD009", e.getMessage());
				logger.error("auxiliarVerify", e);
				return false;
			}
		}
		return verify(doc, "", options.getIdentifierAttribute(), withCert, key);
	}

	private boolean verify(Document doc, String baseURI, String id, boolean withCert, Key key) {
		logger.debug("verify");
		Element sigElement = (Element) doc.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_SIGNATURE)
			.item(0);
		if (id != null && !SecurityUtils.compareStrings(id, "")) {
			Element ref = (Element) doc.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_REFERENCE)
				.item(0);
			String sigId = ref.getAttribute(Constants._ATT_URI);
			if (SecurityUtils.compareStrings(sigId, "")) {
				this.error.setError("XD010", "Could not find Reference URI for id");
				logger.error("verify - Could not find Reference URI for id");
				return false;
			}
			Element idElement = (Element) SignatureUtils.getNodeFromID(doc, id, sigId, this.error);
			if (idElement == null) {
				this.error.setError("XD011", "Could not find node from ID");
				logger.error("verify - Could not find node from ID");
				return false;
			}
			idElement.setIdAttribute(id, true);
		}

		try {

			XMLSignature signature = new XMLSignature(sigElement, baseURI);
			if (withCert) {
				PublicKey pk = ((com.genexus.securityapicommons.commons.PublicKey) key).getPublicKey();
				return signature.checkSignatureValue(pk);
			} else {
				return signature.checkSignatureValue(signature.getKeyInfo().getPublicKey());
			}
		} catch (Exception e) {

			this.error.setError("XD012", e.getMessage());
			logger.error("verify", e);
			return false;
		}
	}

	private Document loadDocument(boolean isFile, String path, DSigOptions options) {
		logger.debug("loadDocument");
		if (isFile) {
			if (!SignatureUtils.validateExtensionXML(path)) {
				this.error.setError("XD013", "Not XML file");
				logger.error("loadDocument - Not XML file");
				return null;
			}
			return SignatureUtils.documentFromFile(path, options.getXmlSchemaPath(), this.error);

		} else {
			return SignatureUtils.documentFromString(path, options.getXmlSchemaPath(), this.error);
		}
	}
}
