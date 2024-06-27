package com.genexus.saml;

import com.genexus.ModelContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.internet.HttpContext;
import org.apache.xml.security.stax.impl.util.IDGenerator;
import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.saml.saml2.core.impl.*;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;


public class SamlHelper extends SamlBuilder {
	public static final ILogger logger = LogManager.getLogger(SamlHelper.class);
	private static final String STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

	private static final String STATUS_PARTIAL_LOGOUT = "urn:oasis:names:tc:SAML:2.0:status:PartialLogout";
	public SamlHelper() {
		super();
	}

	public AuthnRequest createStockAuthnRequest() {

		String serviceProviderEntityId = GamSamlProperties.getServiceProviderEntityId();
		DateTime now = new DateTime();

		Issuer issuer = buildSamlIssuer();
		issuer.setValue(serviceProviderEntityId);

		NameIDPolicy nameIDPolicy = buildSamlNameIDPolicy();
		nameIDPolicy.setSPNameQualifier(serviceProviderEntityId);
		nameIDPolicy.setAllowCreate(true);

		String NameIDFormat = GamSamlProperties.getNameIDPolicyFormat();
		if (NameIDFormat == null || NameIDFormat.isEmpty())
			nameIDPolicy.setFormat(NameIDType.UNSPECIFIED);
		else
			nameIDPolicy.setFormat(NameIDFormat);

		AuthnContextClassRef ref = buildSamlAuthnContextClassRef();

		boolean removeAuthContext = false;
		String configuredAuthContext = GamSamlProperties.getAuthnContext();
		if (configuredAuthContext.equals("NONE"))
			removeAuthContext = true;
		else {
			if (configuredAuthContext.equals("SMARTCARD_AUTHN_CTX")) {
				ref.setAuthnContextClassRef(AuthnContext.SMARTCARD_PKI_AUTHN_CTX);
				logger.debug("[createStockAuthnRequest] AuthnContext.SMARTCARD_PKI_AUTHN_CTX");
			} else if (configuredAuthContext.equals("PPT_AUTHN_CTX")) {
				ref.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
				logger.debug("[createStockAuthnRequest] AuthnContext.PPT_AUTHN_CTX");
			} else if (configuredAuthContext.startsWith("urn:oasis:names:tc:SAML:2.0")) {
				ref.setAuthnContextClassRef(configuredAuthContext);
				logger.debug("[createStockAuthnRequest] AuthnContext: " + configuredAuthContext);
			}
		}


		AuthnRequest authnRequest = buildSamlAuthnAuthnRequest();

		if (!removeAuthContext) {
			RequestedAuthnContext authnContext = buildSamlRequestedAuthnContext();
			authnContext.getAuthnContextClassRefs().add(ref);
			authnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
			authnRequest.setRequestedAuthnContext(authnContext);
		}

		authnRequest.setID(IDGenerator.generateID(null));
		authnRequest.setIssueInstant(now);
		authnRequest.setIssuer(issuer);
		authnRequest.setNameIDPolicy(nameIDPolicy);

		String samlDestination = GamSamlProperties.getSamlEndpointLocation();
		authnRequest.setDestination(samlDestination);
		logger.debug("[createStockAuthnRequest] setDestination = " + samlDestination);

		String AssertionConsumerServiceURL = GamSamlProperties.getAssertionConsumerServiceURL();
		authnRequest.setAssertionConsumerServiceURL(AssertionConsumerServiceURL);
		logger.debug("[createStockAuthnRequest] AssertionConsumerServiceURL = " + AssertionConsumerServiceURL);


		//force authentication
		String setForceAuthn = GamSamlProperties.getForceAuthn();
		if (setForceAuthn.equals("true")) {
			authnRequest.setForceAuthn(true);
		}

		return authnRequest;

	}


	public void doAuthenticationRedirect(final HttpServletResponse response) throws GamSamlException {

		AuthnRequest authnRequest = createStockAuthnRequest();
		org.opensaml.messaging.context.MessageContext context = new org.opensaml.messaging.context.MessageContext();
		context.setMessage(authnRequest);

		SingleSignOnService samlEndpoint = buildSamlSingleSignOnServiceEndpoint();


		String isRedirectBinding = GamSamlProperties.isRedirectBinding();
		if (isRedirectBinding.equals("true")) {
			samlEndpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
			logger.debug("[doAuthenticationRedirect] SAMLConstants.SAML2_REDIRECT_BINDING_URI");
		} else {
			samlEndpoint.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
			logger.debug("[doAuthenticationRedirect] SAMLConstants.SAML2_POST_BINDING_URI");
		}

		String samlEndpointLocation = GamSamlProperties.getSamlEndpointLocation();
		samlEndpoint.setLocation(samlEndpointLocation);

		SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true);
		SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
		endpointContext.setEndpoint(samlEndpoint);


		// Signature
		//To change it: http://shibboleth.net/pipermail/dev/2015-November/007623.html

		org.opensaml.security.credential.Credential c = getCredential();
		SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();
		signatureSigningParameters.setSigningCredential(c);
		//signs whit RSASHA256 no matter the certificate
		signatureSigningParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
		context.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(signatureSigningParameters);

		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		encoder.setMessageContext(context);
		encoder.setHttpServletResponse(response);

		try {
			//add state to take gam properties
			context.getSubcontext(SAMLBindingContext.class, true).setRelayState(GamSamlProperties.getState());
			encoder.initialize();
			encoder.encode();

		} catch (Exception e) {
			logger.error("[doAuthenticationRedirect]  ", e);
			throw new GamSamlException(e);
		}
	}

	private org.opensaml.security.credential.Credential getCredential() {

		logger.debug("[getCredential] Enter");
		String keyStoreFilePath = GamSamlProperties.getKeyStPathCredential();
		String keyStorePwd = GamSamlProperties.getKeyStPwdCredential();
		String keyAlias = GamSamlProperties.getKeyAliasCredential();

		File keyStoreFile = new File(keyStoreFilePath);
		FileInputStream keyStoreFis;


		try {
			keyStoreFis = new FileInputStream(keyStoreFile);
			KeyStore keyStore = KeyStore.getInstance(GamSamlProperties.getKeyStoreCredential());
			keyStore.load(keyStoreFis, keyStorePwd.toCharArray());

			PrivateKey pk = (PrivateKey) keyStore.getKey(keyAlias, keyStorePwd.toCharArray());
			logger.debug("verifying certificate");
			java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) keyStore.getCertificate(keyAlias);
			cert.verify(keyStore.getCertificate(keyAlias).getPublicKey());
			logger.debug("certificate verified");
			return new org.opensaml.security.x509.BasicX509Credential(cert, pk);
		} catch (Exception e) {
			logger.error("[getCredential] ", e);
			return null;
		}

	}


	protected KeyStore getTrustStore() throws GamSamlException {

		String keyStoreFilePath = GamSamlProperties.getKeyStPathTrustSt();
		String keyStorePwd = GamSamlProperties.getKeyStPwdTrustSt();

		KeyStore keyStore = null;
		File keyStoreFile = new File(keyStoreFilePath);
		FileInputStream keyStoreFis;
		try {
			keyStoreFis = new FileInputStream(keyStoreFile);
			keyStore = KeyStore.getInstance(GamSamlProperties.getKeyStoreTrustSt());
			keyStore.load(keyStoreFis, keyStorePwd.toCharArray());

		} catch (KeyStoreException e) {
			logger.error("[getTrustStore] KeyStoreException ", e);
			extracted(e);
		} catch (Exception e) {
			logger.error("[getTrustStore] ", e);
			throw new GamSamlException(e);
		}

		return keyStore;
	}

	private void extracted(KeyStoreException e) throws GamSamlException {
		throw new GamSamlException(e);
	}

	protected Credential getTrustStoreCredential() throws GamSamlException {

		String keyStoreFilePath = GamSamlProperties.getKeyStoreFilePathTrustCred();
		String keyStorePwd = GamSamlProperties.getKeyStorePwdTrustCred();
		String keyAlias = GamSamlProperties.getKeyAliasTrustCred();
		File keyStoreFile = new File(keyStoreFilePath);
		FileInputStream keyStoreFis;
		try {
			keyStoreFis = new FileInputStream(keyStoreFile);
			KeyStore keyStore = KeyStore.getInstance(GamSamlProperties.getKeyStoreTrustCred());
			keyStore.load(keyStoreFis, keyStorePwd.toCharArray());
			Certificate cert = keyStore.getCertificate(keyAlias);
			return new org.opensaml.security.x509.BasicX509Credential((X509Certificate) cert);
		} catch (Exception e) {
			logger.error("[getTrustStoreCredential] ", e);
			throw new GamSamlException(e);
		}
	}

	private LogoutRequest createLogoutRequestGlobal(String sessionIndex, String identifier, String nameIdentifier) throws IllegalArgumentException, java.lang.SecurityException, IllegalAccessException {

		//create the logout request
		SAMLObjectBuilder<LogoutRequest> logoutRequestBuilder = (SAMLObjectBuilder<LogoutRequest>) builderFactory.getBuilder(LogoutRequest.DEFAULT_ELEMENT_NAME);
		LogoutRequest logoutReq = logoutRequestBuilder.buildObject();

		String destinoglobal = GamSamlProperties.getSingleLogoutendpoint();
		logoutReq.setDestination(destinoglobal);

		//Random id of the transaction
		logoutReq.setID(identifier);


		//Timestamp of the assertion.
		DateTime issueInstant = new DateTime();
		logoutReq.setIssueInstant(issueInstant);

		// Version 2.0.
		SAMLVersion version = SAMLVersion.VERSION_20;
		logoutReq.setVersion(version);

		//Entity ID of SP.
		String Issuer = GamSamlProperties.getServiceProviderEntityId();
		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = (org.opensaml.saml.saml2.core.Issuer) issuerBuilder.buildObject();
		issuer.setValue(Issuer);
		logoutReq.setIssuer(issuer);

		NameID nameID = new NameIDBuilder().buildObject();
		nameID.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transient");
		nameID.setValue(nameIdentifier);
		nameID.setNameQualifier(Issuer);
		logoutReq.setNameID(nameID);

		SessionIndex sessionIndexElement = new SessionIndexBuilder().buildObject();
		sessionIndexElement.setSessionIndex(sessionIndex);
		logoutReq.getSessionIndexes().add(sessionIndexElement);

		return logoutReq;
	}

	public String createIdentifier() {
		return IDGenerator.generateID(null);
	}

	public void doLogoutGlobalRedirect(final HttpServletResponse response, String sessionindex, String identificador, String nameIdentifier) throws IllegalArgumentException, SecurityException, IllegalAccessException {

		LogoutRequest logoutrequest = createLogoutRequestGlobal(sessionindex, identificador, nameIdentifier);
		org.opensaml.messaging.context.MessageContext context = new org.opensaml.messaging.context.MessageContext();
		context.setMessage(logoutrequest);

		SingleSignOnService samlEndpoint = buildSamlSingleSignOnServiceEndpoint();

		samlEndpoint.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);

		String samlEndpointLocation = GamSamlProperties.getSingleLogoutendpoint();
		samlEndpoint.setLocation(samlEndpointLocation);
		SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true);
		SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
		endpointContext.setEndpoint(samlEndpoint);

		// Signature

		org.opensaml.security.credential.Credential c = getCredential();
		SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();
		signatureSigningParameters.setSigningCredential(c);
		//signs always with RSASHA256 no matter the certificate
		signatureSigningParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
		context.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(signatureSigningParameters);

		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		encoder.setMessageContext(context);
		encoder.setHttpServletResponse(response);

		try {
			logger.debug("[doLogoutGlobalRedirect] RelayState: " + GamSamlProperties.getState());
			context.getSubcontext(SAMLBindingContext.class, true).setRelayState(GamSamlProperties.getState());
			encoder.initialize();
			encoder.encode();

		} catch (Exception e) {
			logger.error("[doLogoutGlobalRedirect] ", e);
		}
	}

	public void doLogoutLocalRedirect(LogoutRequest req, final HttpServletResponse response, HttpContext HttpContext, Class contextClass) throws IllegalArgumentException, SecurityException, IllegalAccessException {

		String result = "";

		//GAM's sesion destroy
		String url = "";
		ModelContext modelContext = new ModelContext(contextClass);
		modelContext.setHttpContext(HttpContext);
		String sessionIndex = req.getSessionIndexes().get(0).getSessionIndex();
		NameID nameId = req.getNameID();
		String externalToken = nameId.getFormat() + "," + nameId.getValue();
		externalToken += "::" + sessionIndex;
		try {
			Class<?> gamClass = Class.forName("genexus.security.api.gamexternalauthenticationinputsaml20");
			Object gamObj = gamClass.getDeclaredConstructor(int.class, ModelContext.class).newInstance(-1, modelContext);
			Class<?>[] paramTypes = {String.class, String.class, String.class, String.class};
			Method method = gamClass.getMethod("executeUdp", paramTypes);
			url = (String) method.invoke(gamObj, "saml=signout", "", externalToken, "");
		} catch (Exception e) {
			logger.error("[doLogoutLocalRedirect]  reflection", e);
		}
		result = STATUS_SUCCESS;
		String singleLogoutEndpoint = url;
		LogoutResponse logoutresponse = createLogoutResponseLocal(req, result, singleLogoutEndpoint);

		SingleSignOnService samlEndpoint = buildSamlSingleSignOnServiceEndpoint();
		org.opensaml.messaging.context.MessageContext context = new org.opensaml.messaging.context.MessageContext();
		context.setMessage(logoutresponse);
		samlEndpoint.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		String samlEndpointLocation = singleLogoutEndpoint;
		samlEndpoint.setLocation(samlEndpointLocation);

		SAMLPeerEntityContext peerEntityContext = context.getSubcontext(SAMLPeerEntityContext.class, true);
		SAMLEndpointContext endpointContext = peerEntityContext.getSubcontext(SAMLEndpointContext.class, true);
		endpointContext.setEndpoint(samlEndpoint);

		// Signature
		org.opensaml.security.credential.Credential c = getCredential();
		SignatureSigningParameters signatureSigningParameters = new SignatureSigningParameters();
		signatureSigningParameters.setSigningCredential(c);
		//signs always with RSASHA256 no matter the certificate
		signatureSigningParameters.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
		context.getSubcontext(SecurityParametersContext.class, true).setSignatureSigningParameters(signatureSigningParameters);

		context.getSubcontext(SAMLBindingContext.class, true).setRelayState(GamSamlProperties.getState());

		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		encoder.setMessageContext(context);
		encoder.setHttpServletResponse(response);

		try {
			encoder.initialize();
			encoder.encode();
		} catch (Exception e) {
			logger.error("[doLogoutLocalRedirect]  ", e);
		}
	}

	private LogoutResponse createLogoutResponseLocal(LogoutRequest req, String result, String singleLogoutEndpoint) throws IllegalArgumentException, java.lang.SecurityException, IllegalAccessException {

		//create the logout response
		LogoutResponse logoutResp = new LogoutResponseBuilder().buildObject();

		String destinoglobal = null;
		if (singleLogoutEndpoint == null)
			destinoglobal = GamSamlProperties.getSingleLogoutendpoint();
		else
			destinoglobal = singleLogoutEndpoint;


		logoutResp.setDestination(destinoglobal);

		//assertion's id
		logoutResp.setID(IDGenerator.generateID(null));

		//Authentication Request Assertion ID
		logoutResp.setInResponseTo(req.getID());

		//assertion's timestamp.
		DateTime issueInstant = new DateTime();
		logoutResp.setIssueInstant(issueInstant);

		// Version 2.0.
		SAMLVersion version = SAMLVersion.VERSION_20;
		logoutResp.setVersion(version);

		String Issuer = GamSamlProperties.getServiceProviderEntityId();
		IssuerBuilder issuerBuilder = new IssuerBuilder();
		Issuer issuer = (org.opensaml.saml.saml2.core.Issuer) issuerBuilder.buildObject();
		issuer.setValue(Issuer);
		logoutResp.setIssuer(issuer);

		//Success
		Status status = (Status) new StatusBuilder().buildObject();
		StatusCode code = new StatusCodeBuilder().buildObject();
		code.setValue(result);
		status.setStatusCode(code);
		logoutResp.setStatus(status);

		return logoutResp;
	}

	public boolean validateLogoutResponse(LogoutResponse resp, String identificador) {
		boolean validate = false;
		String id = resp.getInResponseTo();
		if (identificador.equals(id)) {
			//take off status
			Status status = resp.getStatus();
			StatusCode code = status.getStatusCode();
			String value = code.getValue();
			if (value.equals(STATUS_SUCCESS) || value.equals(STATUS_PARTIAL_LOGOUT)) {
				validate = true;
			}
		}
		return validate;
	}

}
