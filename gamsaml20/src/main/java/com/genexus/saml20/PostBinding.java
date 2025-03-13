package com.genexus.saml20;

import com.genexus.saml20.utils.DSig;
import com.genexus.saml20.utils.Encoding;
import com.genexus.saml20.utils.SamlAssertionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.text.MessageFormat;

@SuppressWarnings("unused")
public class PostBinding extends Binding{

	private static final Logger logger = LogManager.getLogger(PostBinding.class);

	private Document xmlDoc;

	public PostBinding()
	{
		logger.trace("PostBinding constructor");
		xmlDoc = null;
	}
	// EXTERNAL OBJECT PUBLIC METHODS  - BEGIN


	public void init(String xml)
	{
		logger.trace("init");
		this.xmlDoc = SamlAssertionUtils.canonicalizeXml(xml);
		logger.debug(MessageFormat.format("Init - XML IdP response: {0}", Encoding.documentToString(xmlDoc)));
	}

	public static String login(SamlParms parms, String relayState)
	{
		//not implemented yet
		logger.error("login - NOT IMPLEMENTED");
		return "";
	}

	public static String logout(SamlParms parms, String relayState)
	{
		//not implemented yet
		logger.error("logout - NOT IMPLEMENTED");
		return "";
	}

	public boolean verifySignatures(SamlParms parms)
	{
		return DSig.validateSignatures(this.xmlDoc, parms.getTrustCertPath(), parms.getTrustCertAlias(), parms.getTrustCertPass());
	}

	public String getLoginAssertions()
	{
		logger.trace("getLoginAssertions");
		return SamlAssertionUtils.getLoginInfo(this.xmlDoc);
	}

	public String getLogoutAssertions()
	{
		logger.trace("getLogoutAssertions");
		return SamlAssertionUtils.getLogoutInfo(this.xmlDoc);
	}

	public String getLoginAttribute(String name)
	{
		logger.trace("getLoginAttribute");
		return SamlAssertionUtils.getLoginAttribute(this.xmlDoc, name);
	}

	public String getRoles(String name)
	{
		logger.debug("getRoles");
		return SamlAssertionUtils.getRoles(this.xmlDoc, name);
	}

	// EXTERNAL OBJECT PUBLIC METHODS  - END
}
