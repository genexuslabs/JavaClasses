package com.genexus.saml20.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum PolicyFormat {

	UNSPECIFIED, EMAIL, ENCRYPTED, TRANSIENT, ENTITY, KERBEROS, WIN_DOMAIN_QUALIFIED, X509_SUBJECT;

	private static final Logger logger = LogManager.getLogger(PolicyFormat.class);

	public static PolicyFormat getPolicyFormat(String format)
	{
		logger.trace("GetPolicyFormat");
		switch(format.toUpperCase().trim())
		{
			case "UNSPECIFIED":
				return UNSPECIFIED;
			case "EMAIL":
				return EMAIL;
			case "ENCRYPTED":
				return ENCRYPTED;
			case "TRANSIENT":
				return TRANSIENT;
			case "ENTITY":
				return ENTITY;
			case "KERBEROS":
				return KERBEROS;
			case "WIN_DOMAIN_QUALIFIED":
				return WIN_DOMAIN_QUALIFIED;
			case "X509_SUBJECT":
				return X509_SUBJECT;
			default:
				logger.error("Unknown policy format");
				return null;
		}
	}

	public static String valueOf(PolicyFormat format)
	{
		logger.trace("ValueOf");
		switch (format)
		{
			case UNSPECIFIED:
				return "UNSPECIFIED";
			case EMAIL:
				return "EMAIL";
			case ENCRYPTED:
				return "ENCRYPTED";
			case TRANSIENT:
				return "TRANSIENT";
			case ENTITY:
				return "ENTITY";
			case KERBEROS:
				return "KERBEROS";
			case WIN_DOMAIN_QUALIFIED:
				return "WIN_DOMAIN_QUALIFIED";
			case X509_SUBJECT:
				return "X509_SUBJECT";
			default:
				logger.error("Unknown policy format");
				return "";
		}
	}

	public static String getPolicyFormatXmlValue(PolicyFormat format)
	{
		logger.trace("GetPolicyFormatXmlValue");
		switch (format)
		{
			case UNSPECIFIED:
				return "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
			case EMAIL:
				return "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
			case ENCRYPTED:
				return "urn:oasis:names:tc:SAML:2.0:nameid-format:encrypted";
			case TRANSIENT:
				return "urn:oasis:names:tc:SAML:2.0:nameid-format:transient";
			case ENTITY:
				return "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";
			case KERBEROS:
				return "urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos";
			case WIN_DOMAIN_QUALIFIED:
				return "urn:oasis:names:tc:SAML:2.0:nameid-format:windowsDomainQualifiedName";
			case X509_SUBJECT:
				return "urn:oasis:names:tc:SAML:2.0:nameid-format:x509Subject";
			default:
				logger.error("Unknown policy format");
				return "";
		}
	}
}
