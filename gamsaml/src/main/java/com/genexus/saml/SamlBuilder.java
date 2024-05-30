package com.genexus.saml;

import java.security.SecureRandom;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml.saml2.metadata.impl.SingleSignOnServiceBuilder;


public class SamlBuilder {
	public static final ILogger logger = LogManager.getLogger(SamlBuilder.class);
	protected XMLObjectBuilderFactory builderFactory;
	protected SecureRandom iDGenerator;

	public SamlBuilder() {
		try {
			SamlBootstrap instanceSAMLBootstrap = SamlBootstrap.getInstance();
			iDGenerator = instanceSAMLBootstrap.getIdGenerator();
			builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
			logger.debug("[constructor] idGenerator: " + iDGenerator.toString().trim());
		} catch (Exception e) {
			logger.error("[constructor]  ", e);
		}

	}

	@SuppressWarnings("unchecked")
	protected <SAMLObjectType extends SAMLObject, BuilderT extends SAMLObjectBuilder<SAMLObjectType>> SAMLObjectType buildSamlObject(javax.xml.namespace.QName defaultElementName, Class<BuilderT> type) {
		BuilderT requestBuilder = (BuilderT) builderFactory.getBuilder(defaultElementName);
		return requestBuilder.buildObject();
	}

	protected Issuer buildSamlIssuer() {
		return buildSamlObject(Issuer.DEFAULT_ELEMENT_NAME, IssuerBuilder.class);
	}

	protected NameIDPolicy buildSamlNameIDPolicy() {
		return buildSamlObject(NameIDPolicy.DEFAULT_ELEMENT_NAME, NameIDPolicyBuilder.class);
	}

	protected AuthnContextClassRef buildSamlAuthnContextClassRef() {
		return buildSamlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME, AuthnContextClassRefBuilder.class);
	}

	protected RequestedAuthnContext buildSamlRequestedAuthnContext() {
		return buildSamlObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME, RequestedAuthnContextBuilder.class);
	}

	protected AuthnRequest buildSamlAuthnAuthnRequest() {
		return buildSamlObject(AuthnRequest.DEFAULT_ELEMENT_NAME, AuthnRequestBuilder.class);
	}

	protected SingleSignOnService buildSamlSingleSignOnServiceEndpoint() {
		return buildSamlObject(SingleSignOnService.DEFAULT_ELEMENT_NAME, SingleSignOnServiceBuilder.class);
	}

	public ArtifactResponse buildSamlArtifactResponse() {
		return buildSamlObject(ArtifactResponse.DEFAULT_ELEMENT_NAME, ArtifactResponseBuilder.class);
	}

}
