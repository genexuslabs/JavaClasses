package com.genexus.saml20;

@SuppressWarnings("unused")
public abstract class Binding {

	abstract void init(String input);

	static String login(SamlParms parms, String relayState) {
		return "";
	}

	static String logout(SamlParms parms, String relayState) {
		return "";
	}

	abstract boolean verifySignatures(SamlParms parms);

	abstract String getLoginAssertions();

	abstract String getLoginAttribute(String name);

	abstract String getRoles(String name);

	abstract String getLogoutAssertions();
}
