package com.genexus.saml20.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

public class DSig {

	private static final Logger logger = LogManager.getLogger(DSig.class);

	public static boolean validateSignatures(Document xmlDoc, String certPath, String certAlias, String certPassword) {
		logger.trace("validateSignatures");
		X509Certificate cert = Keys.loadCertificate(certPath, certAlias, certPassword);

		NodeList nodes = findElementsByPath(xmlDoc, "//*[@ID]");

		NodeList signatures = xmlDoc.getElementsByTagNameNS(Constants.SignatureSpecNS, Constants._TAG_SIGNATURE);

		for (int i = 0; i < signatures.getLength(); i++) {
			Element signedElement = findNodeById(nodes, getSignatureID((Element) signatures.item(i)));
			if (signedElement == null) {
				return false;
			}
			signedElement.setIdAttribute("ID", true);
			try {
				XMLSignature signature = new XMLSignature((Element) signatures.item(i), "");
				if (!signature.checkSignatureValue(cert)) {
					return false;
				}
			} catch (Exception e) {
				logger.error("validateSignatures", e);
				return false;
			}
		}
		return true;
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
