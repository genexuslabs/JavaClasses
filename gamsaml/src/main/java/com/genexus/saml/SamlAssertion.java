package com.genexus.saml;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.opensaml.saml.saml2.core.Assertion;


public class SamlAssertion {
	public static final ILogger logger = LogManager.getLogger(SamlAssertion.class);
	public static final String UNKNOWN = "Unknown";
	public static final String PASSWORD = "PasswordProtectedTransport";
	public static final String SMARTCARD = "SmartcardPKI";
	private String authenticationMethod;
	private boolean presencial, certificate, error;
	private String uid, document, countryDocument, typeDocument, completeName, issuer, name2, lastName2, lastName1, name1, fullAttributesJson, errorMessage, errorTrace;


	public SamlAssertion get(String samlParameter) {
		Assertion assertion;
		try {
			SamlReceiver receiver = new SamlReceiver();
			assertion = receiver.getSAMLAssertion(samlParameter);
			if (!receiver.isError()) {
				error = false;
				errorMessage = "";
				errorTrace = "";
				return receiver.getDataFromAssertion(assertion);
			} else {
				error = true;
				errorMessage = receiver.getErrorMessage();
				errorTrace = receiver.getErrorTrace();
			}
		} catch (Exception e) {
			error = true;
			errorMessage = e.getMessage();
			errorTrace = e.toString();
		}
		return new SamlAssertion();
	}

	public boolean isError() {
		return error;

	}

	public String getErrorMessage() {
		return errorMessage;

	}

	public String getErrorTrace() {
		return errorTrace;

	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public SamlAssertion(String uid, String countryDocument, String typeDocument,
						 String document, String completeName,
						 boolean presencial, boolean certificate, String authenticationMethod, String issuer,
						 String name1, String name2, String lastName1, String lastName2, String fullAttributesJson) {
		super();
		logger.debug("[SamlAssertion constructor with parameters] ");

		this.uid = uid;
		this.countryDocument = countryDocument;
		this.typeDocument = typeDocument;
		this.document = document;

		this.completeName = completeName;
		this.lastName1 = lastName1;
		this.lastName2 = lastName2;
		this.name1 = name1;
		this.name2 = name2;

		this.presencial = presencial;
		this.certificate = certificate;
		this.authenticationMethod = authenticationMethod;
		this.issuer = issuer;

		this.fullAttributesJson = fullAttributesJson;
	}


	public SamlAssertion() {
		this.uid = "";
		this.countryDocument = "";
		this.typeDocument = "";
		this.document = "";

		this.completeName = "";
		this.lastName1 = "";
		this.lastName2 = "";
		this.name1 = "";
		this.name2 = "";

		this.presencial = false;
		this.certificate = false;
		this.authenticationMethod = SamlAssertion.UNKNOWN;
		this.issuer = "";

		this.fullAttributesJson = "";
	}

	@Override
	public String toString() {
		return "SAMLAssertion [authenticationMethod=" + authenticationMethod
			+ ", presencial=" + presencial
			+ ", uid=" + uid
			+ ", certificate=" + certificate
			+ ", completeName=" + completeName
			+ ", document=" + document
			+ ", countryDocument=" + countryDocument
			+ ", typeDocument=" + typeDocument
			+ ", issuer=" + issuer + "]";
	}

	public String getAuthenticationMethod() {
		return authenticationMethod;
	}

	public String getDocument() {
		return document;
	}

	public String getCountryDocument() {
		return countryDocument;
	}

	public String getCompleteName() {
		return completeName;
	}

	public String getUid() {
		return uid;
	}

	public boolean getCertificado() {
		return certificate;
	}

	public boolean getPresencial() {
		return presencial;
	}

	public String getLastName1() {
		return lastName1;
	}

	public String getName1() {
		return name1;
	}

	public String getLastName2() {
		return lastName2;
	}

	public String getName2() {
		return name2;
	}

	public String getTypeDocument() {
		return typeDocument;
	}

	public boolean isCertificate() {
		return certificate;
	}

	public boolean isPresencial() {
		return presencial;
	}

	public String getFullAttributesJson() {
		return fullAttributesJson;
	}

	public void setAuthenticationMethod(String authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
	}

	public void setCertificate(boolean certificate) {
		this.certificate = certificate;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public void setCountryDocument(String countryDocument) {
		this.countryDocument = countryDocument;
	}

	public void setPresencial(boolean presencial) {
		this.presencial = presencial;
	}

	public void setCompleteName(String completeName) {
		this.completeName = completeName;
	}

	public void setLastName1(String lastName1) {
		this.lastName1 = lastName1;
	}

	public void setName1(String name1) {
		this.name1 = name1;
	}

	public void setLastName2(String lastName2) {
		this.lastName2 = lastName2;
	}

	public void setName2(String name2) {
		this.name2 = name2;
	}

	public void setTypeDocument(String typeDocument) {
		this.typeDocument = typeDocument;
	}

	public void setFullAttributesJson(String fullAttributesJson) {
		this.fullAttributesJson = fullAttributesJson;
	}

}
