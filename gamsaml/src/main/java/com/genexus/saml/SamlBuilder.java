package com.genexus.saml;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;

import javax.xml.namespace.QName;


public class SamlBuilder {
	public static final ILogger logger = LogManager.getLogger(SamlBuilder.class);
	protected XMLObjectBuilderFactory builderFactory;

	public SamlBuilder() {
		try {
			SamlBootstrap instanceSAMLBootstrap = SamlBootstrap.getInstance();
			builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
		} catch (Exception e) {
			logger.error("[constructor]  ", e);
		}

	}


	/*@SuppressWarnings({"unused", "unchecked"})
	public <T> T buildSAMLObject(final Class<T> objectClass, QName qName) {
		return (T) builderFactory.getBuilder(qName).buildObject(qName);
	}*/

	protected Issuer buildSamlIssuer() {
		return (Issuer) builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME).buildObject(Issuer.DEFAULT_ELEMENT_NAME);
	}

	protected NameIDPolicy buildSamlNameIDPolicy() {
		return (NameIDPolicy) builderFactory.getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME).buildObject(NameIDPolicy.DEFAULT_ELEMENT_NAME);
	}

	protected AuthnContextClassRef buildSamlAuthnContextClassRef() {
		return (AuthnContextClassRef) builderFactory.getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME).buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
	}

	protected RequestedAuthnContext buildSamlRequestedAuthnContext() {
		return (RequestedAuthnContext) builderFactory.getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME).buildObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
	}

	protected AuthnRequest buildSamlAuthnAuthnRequest() {
		return (AuthnRequest) builderFactory.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME).buildObject(AuthnRequest.DEFAULT_ELEMENT_NAME);
	}

	protected SingleSignOnService buildSamlSingleSignOnServiceEndpoint() {
		return (SingleSignOnService) builderFactory.getBuilder(SingleSignOnService.DEFAULT_ELEMENT_NAME).buildObject(SingleSignOnService.DEFAULT_ELEMENT_NAME);
	}

	public ArtifactResponse buildSamlArtifactResponse() {
		return (ArtifactResponse) builderFactory.getBuilder(ArtifactResponse.DEFAULT_ELEMENT_NAME).buildObject(ArtifactResponse.DEFAULT_ELEMENT_NAME);
	}

}
