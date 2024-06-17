package com.genexus.saml;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;


public class GXExternalObject {
	public static final ILogger logger = LogManager.getLogger(GXExternalObject.class);
	private SamlReceiver receiver;

	public GXExternalObject() {
		receiver = new SamlReceiver();
	}
	public SamlAssertion getSamlAssertion(String sAMLParameter) {
		Assertion assertion;
		try {
			assertion = receiver.getSAMLAssertion(sAMLParameter);
			return receiver.getDataFromAssertion(assertion);
		} catch (Exception e) {
			logger.error("[getSamlAssertion]", e);
		}
		return new SamlAssertion();
	}

	public String getSessionIndex(String samlParameter) {
		try {
			Assertion assertion = receiver.getSAMLAssertion(samlParameter);
			AuthnStatement authStmt = assertion.getAuthnStatements().get(0);
			return authStmt.getSessionIndex();
		} catch (Exception e) {
			logger.error("[getSessionIndex]", e);
		}
		return "";
	}
}
