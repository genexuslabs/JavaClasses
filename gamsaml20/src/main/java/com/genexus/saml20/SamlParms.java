package com.genexus.saml20;

@SuppressWarnings("unused")
public class SamlParms {

	private String id;
	private String endPointLocation; //IdP Login URL
	private String singleLogoutEndpoint; //IdP Logout URL
	private String acs;
	private String identityProviderEntityID; //issuer
	private String certPath;
	private String certPass;
	private String certAlias;
	private String policyFormat;
	private String authnContext;
	private String serviceProviderEntityID; //spName
	private boolean forceAuthn;
	private String nameID;
	private String sessionIndex;
	private String trustCertPath;
	private String trustCertPass;
	private String trustCertAlias;


	public SamlParms() {
		id = "";
		endPointLocation = "";
		singleLogoutEndpoint = "";
		acs = "";
		identityProviderEntityID = "";
		certPath = "";
		certPass = "";
		certAlias = "";
		policyFormat = "";
		authnContext = "";
		serviceProviderEntityID = "";
		forceAuthn = false;
		nameID = "";
		sessionIndex = "";
		trustCertAlias = "";
		trustCertPass = "";
		trustCertPath = "";
	}

	public void setId(String value) {
		id = value;
	}

	public String getId() {
		return id;
	}

	public void setEndPointLocation(String value) {
		endPointLocation = value;
	}

	public String getEndPointLocation() {
		return endPointLocation;
	}

	public void setSingleLogoutEndpoint(String value) {
		singleLogoutEndpoint = value;
	}

	public String getSingleLogoutEndpoint() {
		return singleLogoutEndpoint;
	}

	public void setAcs(String value) {
		acs = value;
	}

	public String getAcs() {
		return acs;
	}

	public void setIdentityProviderEntityID(String value) {
		identityProviderEntityID = value;
	}

	public String getIdentityProviderEntityID() {
		return identityProviderEntityID;
	}

	public void setCertPath(String value) {
		certPath = value;
	}

	public String getCertPath() {
		return certPath;
	}

	public void setCertPass(String value) {
		certPass = value;
	}

	public String getCertPass() {
		return certPass;
	}

	public void setCertAlias(String value) {
		certAlias = value;
	}

	public String getCertAlias() {
		return certAlias;
	}

	public void setPolicyFormat(String value) {
		policyFormat = value;
	}

	public String getPolicyFormat() {
		return policyFormat;
	}

	public void setAuthnContext(String value) {
		authnContext = value;
	}

	public String getAuthnContext() {
		return authnContext;
	}

	public void setServiceProviderEntityID(String value) {
		serviceProviderEntityID = value;
	}

	public String getServiceProviderEntityID() {
		return serviceProviderEntityID;
	}

	public void setForceAuthn(boolean value) {
		forceAuthn = value;
	}

	public boolean getForceAuthn() {
		return forceAuthn;
	}

	public void setNameID(String value) {
		nameID = value;
	}

	public String getNameID() {
		return nameID;
	}

	public void setSessionIndex(String value) {
		sessionIndex = value;
	}

	public String getSessionIndex() {
		return sessionIndex;
	}

	public void setTrustCertPath(String value) {
		trustCertPath = value;
	}

	public String getTrustCertPath() {
		return trustCertPath;
	}

	public void setTrustCertPass(String value) {
		trustCertPass = value;
	}

	public String getTrustCertPass() {
		return trustCertPass;
	}

	public void setTrustCertAlias(String value) {
		trustCertAlias = value;
	}

	public String getTrustCertAlias() {
		return trustCertAlias;
	}
}
