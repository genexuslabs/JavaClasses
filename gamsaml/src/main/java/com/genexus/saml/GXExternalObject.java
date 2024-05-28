package com.genexus.saml;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;

import java.util.ArrayList;
import java.util.List;


public class GXExternalObject {
	private SamlReceiver receiver;
	private boolean error;
	private String errorMessage, errorTrace;

	public GXExternalObject() {
		try {
			receiver = new SamlReceiver();
			error = false;
			errorMessage = "";
			errorTrace = "";
		} catch (Exception e) {
			error = true;
			errorMessage = e.getMessage();
			errorTrace = e.toString();
		}
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

	public SamlAssertion getSamlAssertion(String sAMLParameter) {
		Assertion assertion;
		try {
			assertion = receiver.getSAMLAssertion(sAMLParameter);
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

	public String getSessionIndex(String sAMLParameter) {
		Assertion assertion;
		try {
			assertion = receiver.getSAMLAssertion(sAMLParameter);
			List<Assertion> assertions = new ArrayList<Assertion>();
			assertions.add(assertion);
			String sessionIndex = null;
			if (assertions != null && assertions.size() > 0) {
				List<AuthnStatement> authnStatements = assertions.get(0).getAuthnStatements();
				if (authnStatements != null && authnStatements.size() > 0) {
					AuthnStatement authStmt = authnStatements.get(0);
					sessionIndex = authStmt.getSessionIndex();
				}
			}
			error = false;
			errorMessage = "";
			errorTrace = "";
			return sessionIndex;
		} catch (Exception e) {
			error = true;
			errorMessage = e.getMessage();
			errorTrace = e.toString();
		}
		return "";
	}
}
