package com.genexus.saml20.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DSig {

	private static final Logger logger = LogManager.getLogger(DSig.class);

	public static String validateSignatures(Document xmlDoc, String certPath, String certAlias, String certPassword) {
		logger.trace("validateSignatures");
		List<Element> assertions = new ArrayList<Element>();
		X509Certificate cert = Keys.loadCertificate(certPath, certAlias, certPassword);

		NodeList nodes = findElementsByPath(xmlDoc, "//*[@ID]");

		NodeList signatures = xmlDoc.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_SIGNATURE);
		//check the message is signed - security measure
		if(signatures.getLength() == 0){
			return "";
		}
		for (int i = 0; i < signatures.getLength(); i++) {
			Element signedElement = findNodeById(nodes, getSignatureID((Element) signatures.item(i)));
			assertions.add(signedElement);
			if (signedElement == null) {
				return "";
			}
			signedElement.setIdAttribute("ID", true);
			try {
				XMLSignature signature = new XMLSignature((Element) signatures.item(i), "");
				//verifies the signature algorithm is one expected - security meassure
				if (!verifySignatureAlgorithm((Element) signatures.item(i))) {
					return "";
				}
				if (!signature.checkSignatureValue(cert)) {
					return "";
				}
			} catch (Exception e) {
				logger.error("validateSignatures", e);
				return "";
			}
		}
		return buildXml(assertions, xmlDoc);
	}

	public static String buildXml(List<Element> assertions, Document xmlDoc){
		//security meassure against assertion manipulation, it assures that every assertion to be used on the app has been signed and verified
		Element element = xmlDoc.getDocumentElement();
		Node response  = element.cloneNode(false);

		NodeList status = element.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:protocol", "Status");
		response.appendChild(status.item(0));

		for(Element elem: assertions){
			if(!elem.getLocalName().equals("Response")){
				Node node = elem.cloneNode(true);
				response.appendChild(node);
			}
		}
		return Encoding.elementToString((Element) response);
	}

	private static boolean verifySignatureAlgorithm(Element elem) {
		logger.trace("verifySignatureAlgorithm");
		NodeList signatureMethod = elem.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_SIGNATUREMETHOD);
		String signatureAlgorithm = signatureMethod.item(0).getAttributes().getNamedItem(Constants._ATT_ALGORITHM).getNodeValue();
		logger.debug(MessageFormat.format("verifySignatureAlgorithm - algorithm: {0}", signatureAlgorithm));
		String[] algorithm = signatureAlgorithm.split("#");
		List<String> validAlgorithms = Arrays.asList("rsa-sha1", "rsa-sha256", "rsa-sha512");
		for (String alg : validAlgorithms) {
			if (algorithm[1].trim().equals(alg)) {
				return true;
			}
		}
		logger.error(MessageFormat.format("verifySignatureAlgorithm - Invalid Signature algorithm {0}", algorithm[1]));
		return false;
	}

	private static String getSignatureID(Element signatureElement) {
		return signatureElement.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_REFERENCE).item(0).getAttributes().getNamedItem(Constants._ATT_URI).getNodeValue();
	}

	private static NodeList findElementsByPath(Document doc, String xPath) {
		logger.trace("findElementsByPath");
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile(xPath);
			return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		} catch (Exception e) {
			logger.error("findElementsByPath", e);
			return null;
		}
	}

	private static Element findNodeById(NodeList nodes, String id) {
		logger.trace("findNodeById");
		if (nodes == null) {
			logger.error("findNodeById - Document node list is empty");
			return null;
		}
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getAttributes().getNamedItem("ID").getNodeValue().equals(id.substring(1))) {
				return (Element) nodes.item(i);
			}
		}
		logger.error(MessageFormat.format("Element with id {0} not found", id.substring(1)));
		return null;
	}

}
