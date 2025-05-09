package com.genexus.saml20.utils;

import com.genexus.saml20.utils.xml.Attribute;
import com.genexus.saml20.utils.xml.Element;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xml.security.c14n.Canonicalizer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SamlAssertionUtils {

	private static final Logger logger = LogManager.getLogger(SamlAssertionUtils.class);

	private static final String _saml_protocolNS = "urn:oasis:names:tc:SAML:2.0:protocol"; //saml2p
	private static final String _saml_assertionNS = "urn:oasis:names:tc:SAML:2.0:assertion"; //saml2

	public static Document loadDocument(String xml) {
		logger.trace("loadDocument");
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);


			//disable parser's DTD reading - security meassure
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(inputStream);
		} catch (Exception e) {
			logger.error("loadDocument", e);
			return null;
		}
	}

	public static boolean isLogout(Document xmlDoc){
		return xmlDoc.getDocumentElement().getLocalName().equals("LogoutResponse");
	}

	public static Document createLogoutRequest(String id, String issuer, String nameID, String sessionIndex, String destination) {
		logger.trace("createLogoutRequest");

		ZonedDateTime nowUtc = ZonedDateTime.now(java.time.ZoneOffset.UTC);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String issueInstant = nowUtc.format(formatter);


		String saml2p = "urn:oasis:names:tc:SAML:2.0:protocol";
		String saml2 = "urn:oasis:names:tc:SAML:2.0:assertion";

		DocumentBuilder builder = createDocumentBuilder();

		assert builder != null;
		Document doc = builder.newDocument();

		org.w3c.dom.Element request = doc.createElementNS(saml2p, "saml2p:LogoutRequest");
		request.setAttribute("ID", id);
		request.setAttribute("Version", "2.0");
		request.setAttribute("IssueInstant", issueInstant);
		//request.setAttribute("ProtocolBinding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		request.setAttribute("Destination", destination);
		request.setAttribute("Reason", "urn:oasis:names:tc:SAML:2.0:logout:user");

		org.w3c.dom.Element issuerElem = doc.createElementNS(saml2, "saml2:Issuer");
		issuerElem.setTextContent(issuer);
		request.appendChild(issuerElem);

		org.w3c.dom.Element nameIDElem = doc.createElementNS(saml2, "saml2:NameID");
		nameIDElem.setTextContent(nameID);
		request.appendChild(nameIDElem);

		org.w3c.dom.Element sessionElem = doc.createElementNS(saml2p, "saml2p:SessionIndex");
		sessionElem.setTextContent(sessionIndex);
		request.appendChild(sessionElem);

		doc.appendChild(request);

		logger.debug(MessageFormat.format("createLogoutRequest - XML request: {0}", Encoding.documentToString(doc)));
		return doc;
	}

	public static Document createLoginRequest(String id, String destination, String acsUrl, String issuer, String policyFormat, String authContext, String spname, boolean forceAuthn) {
		logger.trace("createLoginRequest");

		ZonedDateTime nowUtc = ZonedDateTime.now(java.time.ZoneOffset.UTC);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String issueInstant = nowUtc.format(formatter);

		String samlp = "urn:oasis:names:tc:SAML:2.0:protocol";
		String saml = "urn:oasis:names:tc:SAML:2.0:assertion";


		DocumentBuilder builder = createDocumentBuilder();

		assert builder != null;
		Document doc = builder.newDocument();

		org.w3c.dom.Element request = doc.createElementNS(samlp, "saml2p:AuthnRequest");
		request.setAttribute("ID", id);
		request.setAttribute("Version", "2.0");
		request.setAttribute("IssueInstant", issueInstant);
		//request.setAttribute("ProtocolBinding", "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		request.setAttribute("Destination", destination);
		request.setAttribute("AssertionConsumerServiceURL", acsUrl);
		request.setAttribute("ForceAuthn", Boolean.toString(forceAuthn));

		org.w3c.dom.Element issuerElem = doc.createElementNS(saml, "saml2:Issuer");
		issuerElem.setTextContent(issuer);
		request.appendChild(issuerElem);

		org.w3c.dom.Element policy = doc.createElementNS(samlp, "saml2p:NameIDPolicy");
		policy.setAttribute("Format", policyFormat.trim());
		policy.setAttribute("AllowCreate", "true");
		policy.setAttribute("SPNameQualifier", spname);
		request.appendChild(policy);

		org.w3c.dom.Element authContextElem = doc.createElementNS(samlp, "saml2p:RequestedAuthnContext");
		authContextElem.setAttribute("Comparison", "exact");

		org.w3c.dom.Element authnContextClass = doc.createElementNS(saml, "saml2:AuthnContextClassRef");
		authnContextClass.setTextContent(authContext);
		authContextElem.appendChild(authnContextClass);
		request.appendChild(authContextElem);


		doc.appendChild(request);

		logger.debug(MessageFormat.format("CreateLoginRequest - XML request: {0}", Encoding.documentToString(doc)));
		return doc;
	}

	private static DocumentBuilder createDocumentBuilder() {
		logger.trace("createDocumentBuilder");
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			return factory.newDocumentBuilder();
		} catch (Exception e) {
			logger.error("createDocumentBuilder", e);
			return null;
		}
	}

	public static Document canonicalizeXml(String xml) {
		//delete comments from the xml - security meassure
		logger.trace("canoncalizeXml");
		logger.debug(MessageFormat.format("xmlString: {0}", xml));
		try {
			org.apache.xml.security.Init.init();

			Document doc = loadDocument(xml);

			Canonicalizer canonicalizer = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			canonicalizer.canonicalizeSubtree(doc, out);

			String canonicalizedXML = out.toString(StandardCharsets.UTF_8.name());

			return loadDocument(canonicalizedXML);

		} catch (Exception e) {
			logger.error("canoncalizeXml", e);
			return null;
		}
	}

	public static String getLoginInfo(Document xmlDoc) {
		List<Attribute> atributeList = new ArrayList<Attribute>();
		atributeList.add(new Attribute(_saml_assertionNS, "SubjectConfirmationData", "InResponseTo"));
		atributeList.add(new Attribute(_saml_assertionNS, "Conditions", "NotOnOrAfter"));
		atributeList.add(new Attribute(_saml_assertionNS, "Conditions", "NotBefore"));
		atributeList.add(new Attribute(_saml_assertionNS, "SubjectConfirmationData", "Recipient"));
		atributeList.add(new Attribute(_saml_assertionNS, "AuthnStatement", "SessionIndex"));
		atributeList.add(new Attribute(_saml_protocolNS, "Response", "Destination"));
		atributeList.add(new Attribute(_saml_protocolNS, "StatusCode", "Value"));

		List<Element> elementList = new ArrayList<Element>();
		elementList.add(new Element(_saml_assertionNS, "Issuer"));
		elementList.add(new Element(_saml_assertionNS, "Audience"));
		elementList.add(new Element(_saml_assertionNS, "NameID"));

		return printJson(xmlDoc, atributeList, elementList);
	}

	public static String getLogoutInfo(Document doc) {
		logger.trace("getLogoutInfo");
		List<Attribute> atributeList = new ArrayList<Attribute>();
		atributeList.add(new Attribute(_saml_protocolNS, "LogoutResponse", "Destination"));
		atributeList.add(new Attribute(_saml_protocolNS, "LogoutResponse", "InResponseTo"));
		atributeList.add(new Attribute(_saml_protocolNS, "StatusCode", "Value"));

		List<Element> elementList = new ArrayList<Element>();
		elementList.add(new Element(_saml_assertionNS, "Issuer"));

		return printJson(doc, atributeList, elementList);
	}

	public static String getLoginAttribute(Document doc, String name) {
		logger.trace("getLoginAttribute");
		NodeList nodes = getAtttributeElements(doc);

		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getAttributes().getNamedItem("Name").getNodeValue().equals(name)) {
				String value = nodes.item(i).getTextContent() == null ? getAttributeContent(nodes.item(i)): nodes.item(i).getTextContent();
				logger.debug(MessageFormat.format("getLoginAttribute -- attribute name: {0}, value: {1}", name, value));
				return value;
			}
		}
		logger.error(MessageFormat.format("getLoginAttribute -- Could not find attribute with name {0}", name));
		return "";
	}

	public static String getRoles(Document doc, String name) {
		logger.trace("getRoles");
		NodeList nodes = getAtttributeElements(doc);
		List<String> roles = new ArrayList<>();
		for (int i = 0; i < nodes.getLength(); i++) {
			if (nodes.item(i).getAttributes().getNamedItem("Name").getNodeValue().equals(name)) {
				NodeList nList = nodes.item(i).getChildNodes();
				for (int j = 0; j < nList.getLength(); j++) {
					if (!nList.item(j).getTextContent().trim().isEmpty()) {
						roles.add(nList.item(j).getTextContent().trim());
					}
				}
				if (roles.isEmpty()) {
					NodeList eList = ((org.w3c.dom.Element) nodes.item(i)).getElementsByTagName("AttributeValue");
					for (int j = 0; j < eList.getLength(); j++) {
						if (!nList.item(j).getTextContent().trim().isEmpty()) {
							roles.add(nList.item(j).getTextContent().trim());
						}
					}
				}
				return String.join(",", roles);

			}
		}
		logger.debug(MessageFormat.format("GetRoles -- Could not find attribute with name {0}", name));
		return "";
	}

	private static String getAttributeContent(Node node) {
		String value = node.getChildNodes().item(0).getTextContent().trim();
		return value.isEmpty() ? ((org.w3c.dom.Element) node).getElementsByTagName("AttributeValue").item(0).getTextContent() : value;
	}

	private static NodeList getAtttributeElements(Document doc) {
		NodeList nodes = doc.getElementsByTagNameNS(_saml_assertionNS, "Attribute");
		return nodes.getLength() == 0 ? doc.getElementsByTagName("Attribute") : nodes;
	}

	private static String printJson(Document xmlDoc, List<Attribute> atributes, List<Element> elements) {
		logger.trace("PrintJson");
		StringBuilder json = new StringBuilder("{");
		for (Attribute at : atributes) {
			String value = at.printJson(xmlDoc);
			if (value != null) {
				json.append(MessageFormat.format("{0},", value));
			}

		}

		int counter = 0;
		for (Element el : elements) {
			String value = el.printJson(xmlDoc);
			if (value != null) {
				if (counter != elements.size() - 1) {
					json.append(MessageFormat.format("{0},", value));
				} else {
					json.append(MessageFormat.format("{0} }", value));
				}
			}
			counter++;
		}
		logger.debug(MessageFormat.format("printJson -- json: {0}", json.toString()));
		return json.toString();
	}
}
