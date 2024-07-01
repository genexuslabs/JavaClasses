package com.genexus.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.utils.SecurityUtils;

public class SignatureUtils {

	final static String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	final static String SCHEMA_LANG = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	final static String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	public static Document documentFromFile(String path, String xmlSchema, Error error) {

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			if (xmlSchema != null && !SecurityUtils.compareStrings(xmlSchema, "")) {
				if (!validateExtensionSchema(xmlSchema)) {
					error.setError("SU002", "The schema file should be an xsd, dtd or xml file");
					return null;
				}

				dbf.setValidating(true);
				try {
					dbf.setAttribute(SCHEMA_LANG, XML_SCHEMA);
					dbf.setAttribute(SCHEMA_SOURCE, xmlSchema);
				} catch (Exception e) {
					error.setError("SU003", e.getMessage());
					return null;
				}
			}
			Document doc = db.parse(path);
			return doc;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			error.setError("SU001", "Unable to load file");
			return null;
		}

	}

	private static final InputStream inputStringtoStream(String text, Error error) {
		return new ByteArrayInputStream(getBytes(text, error));
	}

	public static final InputStream inputFileToStream(String path) throws IOException {
		final File initialFile = new File(path);
		final InputStream targetStream = new DataInputStream(new FileInputStream(initialFile));
		return targetStream;
	}

	public static boolean writeToFile(String text, String path, String prefix, Error error) {
		try (PrintWriter out = new PrintWriter(path)) {
			out.println(prefix);
			out.println(text);
			return true;
		} catch (FileNotFoundException e) {
			error.setError("SU007", "Error writing file");
			return false;
		}
	}

	public static Document documentFromString(String text, String xmlSchema, Error error) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			if (xmlSchema != null && !SecurityUtils.compareStrings(xmlSchema, "")) {
				if (!validateExtensionSchema(xmlSchema)) {
					error.setError("SU004", "The schema file should be an xsd, dtd or xml file");
					return null;
				}

				dbf.setValidating(true);
				try {
					dbf.setAttribute(SCHEMA_LANG, XML_SCHEMA);
					dbf.setAttribute(SCHEMA_SOURCE, xmlSchema);
				} catch (Exception e) {
					error.setError("SU006", e.getMessage());
					return null;
				}
			}
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputStringtoStream(text, error));
			return doc;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			error.setError("SU005", "Error reading XML");
			return null;
		}
	}

	/**
	 * @param inputText
	 *            String UTF-8 text
	 * @return byte array representation of the String UTF-8 input text
	 */
	private static byte[] getBytes(String inputText, Error error) {
		byte[] output = null;
		try {
			output = inputText.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			error.setError("DS004", "Wrong encoding, use UTF-8");
			e.printStackTrace();
			return null;
		}
		return output;
	}

	public static Element getRootElement(Document doc) {
		Element root = null;
		NodeList list = doc.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i) instanceof Element) {
				root = (Element) list.item(i);
				break;
			}
		}
		root = doc.getDocumentElement();
		return root;
	}

	public static Element getElement(Document doc, String elementName) {
		Element node = null;
		NodeList list = doc.getElementsByTagName(elementName);
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i) instanceof Element) {
				node = (Element) list.item(i);
				break;
			}
		}
		return node;
	}

	public static Node getNodeFromPath(Document doc, String expression, Error error) {
		XPath xPath = XPathFactory.newInstance().newXPath();

		try {
			return (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			error.setError("SU008", "Could not found any node that matches de xPath predicate");
			return null;
		}
	}

	public static Node getNodeFromID(Document doc, String id, String xPath, Error error) {
		if (id == null || SecurityUtils.compareStrings(id, "")) {
			error.setError("SU010", "Error, id data is empty");
			return null;
		}
		String idToFind = xPath.substring(1);

		Node root = getRootElement(doc);

		NodeList list = root.getChildNodes();
		Node n = recursivegetNodeFromID(list, id, idToFind);
		if (n == null) {
			error.setError("SU009", "Could not find element with id " + idToFind);
		}
		return n;

	}

	private static Node findAttribute(Node node, String id, String idToFind) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element eElement = (Element) node;
			if (SecurityUtils.compareStrings(eElement.getAttribute(id), idToFind)) {
				return node;
			}
		}
		return null;
	}

	private static Node recursivegetNodeFromID(NodeList list, String id, String idToFind) {
		if (list.getLength() == 0) {
			return null;
		} else {
			for (int i = 0; i < list.getLength(); i++) {
				Node node = findAttribute(list.item(i), id, idToFind);
				if (node == null) {
					Node n1  =  recursivegetNodeFromID(list.item(i).getChildNodes(), id, idToFind);
					if(n1 != null)
					{
						return n1;
					}
				}else
				{
					return node;
				}
			}
			return null;
		}
	}

	public static boolean validateExtensionXML(String path) {
		if (SecurityUtils.extensionIs(path, ".xml")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean validateExtensionSchema(String path) {
		if (SecurityUtils.extensionIs(path, ".xsd") || SecurityUtils.extensionIs(path, ".dtd")
			|| SecurityUtils.extensionIs(path, ".xml")) {
			return true;
		} else {
			return false;
		}
	}

	public static KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException {
		KeyFactory kf = null;
		if (SecurityUtils.compareStrings("ECDSA", algorithm)) {
			kf = kf.getInstance("EC");
		} else {
			kf = kf.getInstance("RSA");
		}
		return kf;

	}

}
