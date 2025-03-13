package com.genexus.saml20;

@SuppressWarnings("unused")
public class SamlParms {

	private String id;
	private String destination;
	private String acs;
	private String issuer;
	private String certPath;
	private String certPass;
	private String certAlias;
	private String policyFormat;
	private String authnContext;
	private String spName;
	private boolean forceAuthn;
	private String nameID;
	private String sessionIndex;
	private String trustCertPath;
	private String trustCertPass;
	private String trustCertAlias;


	public SamlParms()
	{
		id = "";
		destination = "";
		acs = "";
		issuer = "";
		certPath = "";
		certPass = "";
		certAlias = "";
		policyFormat = "";
		authnContext = "";
		spName = "";
		forceAuthn = false;
		nameID = "";
		sessionIndex = "";
		trustCertAlias = "";
		trustCertPass = "";
		trustCertPath = "";
	}

	public void setId(String value)
	{
		id = value;
	}

	public String getId()
	{
		return id;
	}

	public void setDestination(String value)
	{
		destination = value;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setAcs(String value)
	{
		acs = value;
	}

	public String getAcs()
	{
		return acs;
	}

	public void setIssuer(String value)
	{
		issuer = value;
	}

	public String getIssuer()
	{
		return issuer;
	}

	public void setCertPath(String value)
	{
		certPath = value;
	}

	public String getCertPath()
	{
		return certPath;
	}

	public void setCertPass(String value)
	{
		certPass = value;
	}

	public String getCertPass()
	{
		return certPass;
	}

	public void setCertAlias(String value)
	{
		certAlias = value;
	}

	public String getCertAlias()
	{
		return certAlias;
	}

	public void setPolicyFormat(String value)
	{
		policyFormat = value;
	}

	public String getPolicyFormat()
	{
		return policyFormat;
	}

	public void setAuthnContext(String value)
	{
		authnContext = value;
	}

	public String getAuthnContext()
	{
		return authnContext;
	}

	public void setSPName(String value)
	{
		spName = value;
	}

	public String getSPName()
	{
		return spName;
	}

	public void setForceAuthn(boolean value)
	{
		forceAuthn = value;
	}

	public boolean getForceAuthn()
	{
		return forceAuthn;
	}

	public void setNameID(String value)
	{
		nameID = value;
	}

	public String getNameID()
	{
		return nameID;
	}

	public void setSessionIndex(String value)
	{
		sessionIndex = value;
	}

	public String getSessionIndex()
	{
		return sessionIndex;
	}

	public void setTrustCertPath(String value)
	{
		trustCertPath = value;
	}

	public String getTrustCertPath()
	{
		return trustCertPath;
	}

	public void setTrustCertPass(String value)
	{
		trustCertPass = value;
	}

	public String getTrustCertPass()
	{
		return trustCertPass;
	}

	public void setTrustCertAlias(String value)
	{
		trustCertAlias = value;
	}

	public String getTrustCertAlias()
	{
		return trustCertAlias;
	}
}
