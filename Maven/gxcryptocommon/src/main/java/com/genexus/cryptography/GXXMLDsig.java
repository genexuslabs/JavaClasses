package com.genexus.cryptography;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.genexus.cryptography.signing.xml.Canonicalizer;
import com.genexus.internet.StringCollection;

public class GXXMLDsig {

	private PrivateKey _pKey;
	private X509Certificate _cert;
	private GXCertificate _gxCert;

	private List<String> _references;
	private String _canonicalizationMethod;
	private boolean _detached;
	private StringCollection _keyInfoClauses;
	private int _lastError;
	private String _lastErrorDescription;
	private boolean _validateCertificate;

	public GXXMLDsig() {
		_references = new ArrayList<String>();
		_keyInfoClauses = new StringCollection() {
			{
				add("X509IssuerSerial");
				add("X509SubjectName");
				add("X509Certificate");
			}
		};
		_canonicalizationMethod = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
		_detached = false;
	}

	private void initialize() {
		setError(0);
	}

	public void addReference(String reference) {
		_references.add(reference);
	}

	public String sign(String xml) {
		return signElements(xml, "");
	}

	public String signElements(String xml, String xPath) {
		initialize();
		if (!anyError()) {
			if (!_gxCert.hasPrivateKey())
			{
				setError(5);
				return "";
			}
			try {

				Document doc = Utils.documentFromString(Canonicalizer.canonize(xml), true);
				if (doc == null) {
					setError(2);
					return "";
				}				
				ArrayList<Element> list = new ArrayList<Element>();

				if (xPath.equals("")) {
					list.add(doc.getDocumentElement());
				} else {
					XPath xPathHelper = XPathFactory.newInstance().newXPath();
					NodeList nodeList = (NodeList) xPathHelper.evaluate(xPath, doc, XPathConstants.NODESET);
					for (int i = 0; i < nodeList.getLength(); i++) {
						list.add((Element) nodeList.item(i));
					}
				}

				for (int i = 0; i < list.size(); i++) {

					Element element = (Element) list.get(i);

					// Create a DOM XMLSignatureFactory that will be used to
					// generate the enveloped signature.

					// removes signature element if present.
					NodeList nodeListSignature = element.getElementsByTagName("Signature");
					for (int j = 0; j < nodeListSignature.getLength(); j++) {
						Node parentSignature = nodeListSignature.item(j).getParentNode();
						parentSignature.removeChild(nodeListSignature.item(j));
					}

					DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
					Document docToBeSigned = docBuilder.newDocument();

					docToBeSigned.appendChild(docToBeSigned.importNode(element, true));

					ElementProxy.setDefaultPrefix(org.apache.xml.security.utils.Constants.SignatureSpecNS, "");

					XMLSignature signature = new XMLSignature(docToBeSigned, "",
							XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);

					docToBeSigned.getDocumentElement().appendChild(signature.getElement());
					Transforms transforms = new Transforms(docToBeSigned);
					transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);

					if (_references.size() > 0) {
						for (int j = 0; j < _references.size(); j++) {
							signature.addDocument(_references.get(j), transforms,
									org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);
						}
					} else {
						signature.addDocument("", transforms,
								org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1); // Signs
																								// the
						// whole
						// document
					}

					setKeyInfo(signature);

					signature.sign(_gxCert.getPrivateKey());

					Node p = element.getParentNode();
					p.replaceChild(doc.importNode(docToBeSigned.getDocumentElement(), true), element);
				}
				return Utils.serialize(doc);

			} catch (NoSuchAlgorithmException e) {
				Utils.logError(e);
				setError(3);
			} catch (InvalidAlgorithmParameterException e) {
				Utils.logError(e);
			} catch (Exception e) {
				Utils.logError(e);
				setError(6, e.getMessage());
			}
		}
		return "";
	}

	public boolean verify(String xml) {
		initialize();
		Document doc = null;
		try {
			doc = Utils.documentFromString(Canonicalizer.canonize(xml), true);

		} catch (Exception e) {
		}

		if (doc == null) {
			setError(2);
			return false;
		}

		// Find Signature element.
		NodeList nl = doc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature");
		if (nl.getLength() == 0) {
			return false;
		}

		try {
			Element sigElement = (Element) nl.item(0);
			XMLSignature signature = new XMLSignature(sigElement, "");

			boolean certValid = true;
			if (_validateCertificate) {
				certValid = _gxCert.verify();
				if (!certValid) {
					setError(7);
				}
			}
			KeyInfo ki = signature.getKeyInfo();
			boolean valid = true;
			if (ki == null) {
				setError(8);
			}

			X509Certificate cert = signature.getKeyInfo().getX509Certificate();
			if (cert == null) {
				PublicKey pk = signature.getKeyInfo().getPublicKey();
				if (pk == null) {
					setError(7);
				}
				valid = signature.checkSignatureValue(pk);
			} else {
				valid = signature.checkSignatureValue(cert);
			}
			if (!valid) {
				setError(9);
			}
			return valid && certValid;
		} catch (XMLSignatureException e) {
			Utils.logError(e);
			setError(6);
		} catch (XMLSecurityException e) {
			Utils.logError(e);
			setError(6);
		}
		return false;
	}

	private void setKeyInfo(XMLSignature signature) {
		X509Data x509data = new X509Data(signature.getDocument());
		if (_keyInfoClauses.getCount() > 0) {
			List<Object> x509DataContent = new ArrayList<Object>();
			for (int i = 1; i <= _keyInfoClauses.getCount(); i++) {
				String item = _keyInfoClauses.item(i);
				if (item.equals("X509IssuerSerial")) {
					x509data.addIssuerSerial(_gxCert.getCertificate().getIssuerDN().getName(), _gxCert.getCertificate()
							.getSerialNumber());
				} else if (item.equals("X509SubjectName")) {

					x509DataContent.add(_cert.getIssuerDN().getName());
				} else if (item.equals("X509Certificate")) {
					try {
						x509data.addCertificate(_gxCert.getCertificate());
					} catch (XMLSecurityException e) {
					}
				} else if (item.equals("RSAKeyValue")) {
					signature.getKeyInfo().add(_gxCert.getCertificate().getPublicKey());
				}
			}
		}
		signature.getKeyInfo().add(x509data);
	}

	private void setError(int errorCode) {
		setError(errorCode, "");
	}

	private void setError(int errorCode, String errDsc) {
		_lastError = errorCode;
		switch (errorCode) {
		case 0:
			_lastErrorDescription = "";
			break;
		case 1:
			_lastErrorDescription = "Cannot sign an empty xml.";
			break;
		case 2:
			_lastErrorDescription = "Input XML is not valid";
			break;
		case 3:
			_lastErrorDescription = "Invalid Algorithm format";
			break;
		case 4:
			_lastErrorDescription = Constants.CERT_NOT_INITIALIZED;
			break;
		case 5:
			_lastErrorDescription = Constants.PRIVATEKEY_NOT_PRESENT;
			break;
		case 6:
			_lastErrorDescription = Constants.SIGNATURE_EXCEPTION;
			break;
		case 7:
			_lastErrorDescription = "Certificate is not valid";
			break;
		case 8:
			_lastErrorDescription = "Signature element was not found";
			break;
		case 9:
			_lastErrorDescription = "Signature is not valid";
			break;
		default:
			break;
		}
		if (!errDsc.equals("")) {
			if (!_lastErrorDescription.equals("")) {
				_lastErrorDescription = String.format("%s - %s", _lastErrorDescription, errDsc);
			} else {
				_lastErrorDescription = errDsc;
			}
		}
	}

	public GXCertificate getCertificate() {
		return _gxCert;
	}

	public void setCertificate(GXCertificate cert) {
		this._gxCert = cert;
		this._pKey = cert.getPrivateKey();
		this._cert = cert.getCertificate();
	}

	private Boolean anyError() {

		if (_gxCert == null || (_gxCert != null && !_gxCert.certLoaded())) {
			setError(4); // Certificate not initialized
		}
		return _lastError != 0;
	}

	public int getErrCode() {

		return _lastError;

	}

	public void setValidateCertificate(Boolean validate) {
		this._validateCertificate = validate;
	}

	public Boolean getValidateCertificate() {
		return this._validateCertificate;
	}

	public String getErrDescription() {

		return _lastErrorDescription;

	}
	
	public StringCollection getKeyInfoClauses(){
		return _keyInfoClauses;
	}

}
