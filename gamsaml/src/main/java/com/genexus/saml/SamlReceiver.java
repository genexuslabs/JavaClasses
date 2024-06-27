package com.genexus.saml;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.apache.xml.security.exceptions.Base64DecodingException;
import org.apache.xml.security.utils.Base64;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSBoolean;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.impl.KeyStoreCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

public class SamlReceiver extends SamlHelper {
	public static final ILogger logger = LogManager.getLogger(SamlReceiver.class);

	private String samlString;
	public SamlReceiver() {
		super();
	}

	public Assertion getSAMLAssertion(String responseMessage) throws GamSamlException {
		Response resp = getSAMLResponse(responseMessage);
		logger.debug("[getSAMLAssertion] resp:" + resp.toString());

		Assertion assertion;
		if (resp.getAssertions().size() > 0) {
			assertion = resp.getAssertions().get(0);
			logger.debug("[getSAMLAssertion] Properties.State: " + GamSamlProperties.getState());
			if (GamSamlProperties.getState() == null || GamSamlProperties.getState().trim().isEmpty()) {
				String[] resultString = new String[1];
				if (!assertion.getConditions().getAudienceRestrictions().isEmpty()) {
					String audience = assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI();
					String entityID = "EntityID=" + audience;
					logger.debug("[getSAMLAssertion] EntityID: " + entityID);
					try {
						Class<?> gamClass = Class.forName("genexus.security.api.getauthenticationparmsfromstate");
						Object gamObj = gamClass.getDeclaredConstructor(int.class).newInstance(-1);
						Class<?>[] paramTypes = {String.class, java.lang.String[].class};
						Method method = gamClass.getMethod("execute", paramTypes);
						method.invoke(gamObj, entityID, resultString);
					} catch (Exception e) {
						logger.error("[getSAMLAssertion] load class with reflection ", e);
					}
					String result = resultString[0].trim();
					logger.debug("[getSAMLAssertion] result: " + result);
					try {
						GamSamlProperties.getInstance().saveConfig(result);
					} catch (Exception e) {
						logger.error("[getSAMLAssertion] ", e);
					}
				} else {
					logger.error("[getSAMLAssertion/Audience Error-Empty]");
				}
			}
			try {
				if (p_validateSignature(assertion)) {
					samlString = responseMessage;
					return assertion;
				} else {
					return null;
				}
			} catch (Exception e) {
				logger.error("[getSAMLAssertion] " + e.getMessage(), e);
				return null;
			}
		} else {
			logger.debug("[getSAMLAssertion] resp.getAssertions().size() == 0");
			return null;
		}
	}

	public String getStatusAssertion(String responseMessage) throws GamSamlException {
		Response resp = getSAMLResponse(responseMessage);
		return resp.getStatus().getStatusCode().getValue();
	}

	public String getStatusAssertionMessage(String responseMessage) throws GamSamlException {
		Response resp = getSAMLResponse(responseMessage);
		return resp.getStatus().getStatusMessage().getMessage();
	}

	@SuppressWarnings("static-access")
	public Response getSAMLResponse(String responseMessage) throws GamSamlException {
		try {
			byte[] base64DecodedResponse = Base64.decode(responseMessage);
			ByteArrayInputStream is = new ByteArrayInputStream(base64DecodedResponse);

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

			Document document = docBuilder.parse(is);
			Element element = document.getDocumentElement();

			UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

			XMLObject responseXmlObj = unmarshaller.unmarshall(element);

			return (Response) responseXmlObj;

		} catch (Exception e1) {
			logger.error("[getSAMLResponse] ", e1);
			throw new GamSamlException(e1);
		}
	}


	private boolean p_validateSignature(SignableSAMLObject assertion) throws GamSamlException, SecurityException {
		logger.debug("[p_validateSignature] assertion.isSigned()" + assertion.isSigned());
		if (!assertion.isSigned()) {
			return false;
		} else {
			Map<String, String> passwordMap = new HashMap<String, String>();

			KeyStoreCredentialResolver resolver = new KeyStoreCredentialResolver(getTrustStore(), passwordMap);

			try {
				Criterion criteria = new EntityIdCriterion(GamSamlProperties.getCriteriaKey());
				CriteriaSet criteriaSet = new CriteriaSet(criteria);
				resolver.resolveSingle(criteriaSet);
				SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
				try {
					profileValidator.validate(assertion.getSignature());
				} catch (org.opensaml.xmlsec.signature.support.SignatureException e) {
					logger.error("[p_validateSignature] ", e);
				}

				SignatureValidator.validate(assertion.getSignature(), getTrustStoreCredential());

			} catch (SecurityException | org.opensaml.xmlsec.signature.support.SignatureException e) {
				logger.error("[p_validateSignature] ", e);
				throw new GamSamlException(e);
			} catch (ResolverException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
	}

	// detached signature
	public boolean validateSignature(SignableSAMLObject assertion) throws GamSamlException{
		logger.debug("[validate detached signature] response.isSigned() " + assertion.isSigned());
		if (!assertion.isSigned()) {
			return false;
		} else {
			try {
				SAMLSignatureProfileValidator profileValidator = new SAMLSignatureProfileValidator();
				profileValidator.validate(assertion.getSignature());

				SignatureValidator.validate(assertion.getSignature(), getTrustStoreCredential());

			} catch (org.opensaml.xmlsec.signature.support.SignatureException e) {
				logger.error("[validateSignature] ", e);
				throw new GamSamlException(e);
			}
			return true;
		}
	}

	public boolean validateDetachedSignature(String signature, String document, String algorithm, String saml) throws GamSamlException,
		InvalidKeyException, SignatureException, NoSuchAlgorithmException, IOException, Base64DecodingException {

		String urlEncode = URLEncoder.encode(algorithm, "UTF-8");
		String samlRequestEncode = URLEncoder.encode(document, "UTF-8");

		String concat = saml + "=" + samlRequestEncode + "&" + "SigAlg=" + urlEncode;

		byte[] signatureBytes = Base64.decode(signature);
		byte[] documentBytes = concat.getBytes();
		String code;
		if (saml.equals("SAMLRequest")) {
			code = "SHA256withRSA";
		} else {
			logger.debug("[validateDetachedSignature] algorithm: " + algorithm);
			code = getAlgorithmCode(algorithm);
		}

		Credential cred = getTrustStoreCredential();
		PublicKey publicKey = cred.getPublicKey();
		Signature verifier = Signature.getInstance(code);
		verifier.initVerify(publicKey);
		verifier.update(documentBytes);
		boolean verified = verifier.verify(signatureBytes);
		logger.debug("[validateDetachedSignature] validSignature: " + verified);
		return verified;
	}


	private String getAlgorithmCode(String algorithm) {
		logger.debug("[getAlgorithmCode] algorithm: " + algorithm);
		String code;
		if (algorithm.contains("rsa")) {
			if (algorithm.contains("sha1"))
				code = "SHA1withRSA";
			else if (algorithm.contains("sha256"))
				code = "SHA256withRSA";
			else {
				logger.debug("[getAlgorithmCode] code not matched: " + algorithm);
				code = algorithm;
			}

		} else {
			logger.debug("[getAlgorithmCode] code not matched: " + algorithm);
			code = algorithm;
		}
		logger.debug("[getAlgorithmCode] returned code: " + code);
		return code;
	}


	public SamlAssertion getDataFromAssertion(Assertion assertion) {
		String authenticationMethod = SamlAssertion.UNKNOWN;
		boolean presencial = false, certificate = false;
		String uid = "", document = "", countryDocument = "", typeDocument = "", completeName = "", issuer = "", name1 = "", name2 = "", lastName1 = "", lastName2 = "";
		String fullAttributesJson = "[";
		if (assertion.getIssuer() != null) {
			issuer = assertion.getIssuer().getValue();
		}
		for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {

			for (Attribute data : attributeStatement.getAttributes()) {
				String attributeName = data.getName();
				if (data.getAttributeValues().size() == 1) {
					String attributeValue = getStringValueFromXMLObject(data.getAttributeValues().get(0));
					fullAttributesJson += "{\"Key\":\"" + attributeName.trim() + "\",\"Value\":\"" + attributeValue.trim() + "\"},";
					if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttNOMBRECOMPLETO())) {
						completeName = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttDOCUMENTO())) {
						document = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttPAISDOCUMENTO())) {
						countryDocument = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttPRESENCIAL())) {
						presencial = isTrueString(attributeValue);
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttTIPODOCUMENTO())) {
						typeDocument = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttCERTIFICADO())) {
						certificate = isTrueString(attributeValue);
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttUID())) {
						uid = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttPRIMERNOMBRE())) {
						name1 = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttSEGUNDONOMBRE())) {
						name2 = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttPRIMERAPELLIDO())) {
						lastName1 = attributeValue;
					} else if (attributeName.equalsIgnoreCase(GamSamlProperties.getAttSEGUNDOAPELLIDO())) {
						lastName2 = attributeValue;
					}
				}
			}
		}
		fullAttributesJson = fullAttributesJson.substring(0, fullAttributesJson.length() - 1);
		fullAttributesJson += "]";

		if (assertion.getAuthnStatements().size() == 1) {
			logger.debug("[getDataFromAssertion] assertion.getAuthnStatements().size() == 1");
			String aMethod = getStringValueFromXMLObject(
				assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef());
			if (aMethod != null && aMethod.equalsIgnoreCase(AuthnContext.PPT_AUTHN_CTX)) {
				authenticationMethod = SamlAssertion.PASSWORD;
			} else if (aMethod != null && aMethod.equalsIgnoreCase(AuthnContext.SMARTCARD_PKI_AUTHN_CTX)) {
				authenticationMethod = SamlAssertion.SMARTCARD;
			}
		}
		return new SamlAssertion(uid, countryDocument, typeDocument, document, completeName, presencial, certificate,
			authenticationMethod, issuer, name1, name2, lastName1, lastName2, fullAttributesJson);
	}

	private boolean isTrueString(String attributeValue) {
		return attributeValue.equalsIgnoreCase(GamSamlProperties.getAttTRUE());
	}

	public static String getStringValueFromXMLObject(org.opensaml.core.xml.XMLObject xmlObject) {
		String value = "";
		if (xmlObject instanceof XSString) {
			logger.debug("[getStringValueFromXMLObject] xmlObj instanceof XSString");
			value = ((XSString) xmlObject).getValue();
		} else if (xmlObject instanceof XSAny) {
			logger.debug("[getStringValueFromXMLObject] xmlObj instanceof XSAny");
			value = ((XSAny) xmlObject).getTextContent();
		} else if (xmlObject instanceof AuthnContextClassRef) {
			logger.debug("[getStringValueFromXMLObject] xmlObj instanceof AuthnContextClassRef");
			value = ((AuthnContextClassRef) xmlObject).getAuthnContextClassRef();
		} else if (xmlObject instanceof XSBoolean) {
			logger.debug("[getStringValueFromXMLObject] xmlObj instanceof XSBoolean");
			value = ((XSBoolean) xmlObject).getValue().toString();
		} else {
			logger.debug("[getStringValueFromXMLObject] xmlObj instanceof unknown: " + xmlObject.getSchemaType());
			value = ((XSString) xmlObject).getValue();
		}
		if (value == null) {
			value = "";
			logger.debug("[getStringValueFromXMLObject] value is null");
		}
		logger.debug("[getStringValueFromXMLObject] final value is " + value);
		return value;
	}

	public String getSAMLString() {
		return samlString;
	}

	public LogoutResponse getLogoutResponse(String responseMessage, String signature, String alg) throws GamSamlException{

		try {
			byte[] base64DecodedResponse = Base64.decode(responseMessage);
			ByteArrayInputStream bytesIn = new ByteArrayInputStream(base64DecodedResponse);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

			Document document = docBuilder.parse(bytesIn);

			Element element = document.getDocumentElement();

			UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject responseXmlObj = unmarshaller.unmarshall(element);
			LogoutResponse resp = (LogoutResponse) responseXmlObj;

			if (alg == null) {
				logger.debug("[getLogoutResponse] alg is empty");
				alg = resp.getSignature().getSignatureAlgorithm();
				logger.debug("[getLogoutResponse] Get alg ");
			}


			if (validateSignature(resp)) {
				return resp;
			} else {
				return null;
			}

		} catch (Exception e1) {
			logger.error("[getLogoutResponse] ", e1);
			throw new GamSamlException(e1);
		}
	}

	public LogoutRequest getLogoutRequest(String responseMessage) throws GamSamlException {

		try {

			byte[] base64DecodedResponse = Base64.decode(responseMessage);
			ByteArrayInputStream bytesIn = new ByteArrayInputStream(base64DecodedResponse);

			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();

			Document document = docBuilder.parse(bytesIn);
			Element element = document.getDocumentElement();

			UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
			XMLObject responseXmlObj = unmarshaller.unmarshall(element);
			LogoutRequest resp = (LogoutRequest) responseXmlObj;


			return resp;

		} catch (Exception e1) {
			logger.error("[getLogoutRequest] ", e1);
			throw new GamSamlException(e1);
		}
	}
}