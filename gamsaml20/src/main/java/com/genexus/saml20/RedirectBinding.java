package com.genexus.saml20;

import com.genexus.saml20.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class RedirectBinding extends Binding{

	private static final Logger logger = LogManager.getLogger(RedirectBinding.class);

	private Document xmlDoc;
	private Map<String, String> redirectMessage;

	// EXTERNAL OBJECT PUBLIC METHODS  - BEGIN


	public RedirectBinding()
	{
		logger.trace("RedirectBinding constructor");
	}

	public void init(String queryString)
	{
		logger.trace("init");
		logger.debug(MessageFormat.format("init - queryString : {0}", queryString));
		this.redirectMessage = parseRedirect(queryString);
		String xml = Encoding.decodeAndInflateXmlParameter(this.redirectMessage.get("SAMLResponse"));
		logger.debug("init - inflated xml: {0}", xml);
		this.xmlDoc = SamlAssertionUtils.canonicalizeXml(xml);
		logger.debug(MessageFormat.format("init - XML IdP response: {0}", Encoding.documentToString(xmlDoc)));
	}


	public static String login(SamlParms parms, String relayState)
	{
		Document request = SamlAssertionUtils.createLoginRequest(parms.getId(), parms.getDestination(), parms.getAcs(), parms.getIssuer(), parms.getPolicyFormat(), parms.getAuthnContext(), parms.getSPName(), parms.getForceAuthn());
		return generateQuery(request, parms.getDestination(), parms.getCertPath(), parms.getCertPass(), parms.getCertAlias(), relayState);
	}

	public static String logout(SamlParms parms, String relayState)
	{
		Document request = SamlAssertionUtils.createLogoutRequest(parms.getId(), parms.getIssuer(), parms.getNameID(), parms.getSessionIndex(), parms.getDestination());
		return generateQuery(request, parms.getDestination(), parms.getCertPath(), parms.getCertPass(), parms.getCertAlias(), relayState);
	}

	public boolean verifySignatures(SamlParms parms)
	{
		logger.debug("verifySignatures");

		try
		{
			return DSig.validateSignatures(this.xmlDoc, parms.getTrustCertPath(), parms.getTrustCertAlias(), parms.getTrustCertPass());
		}catch(Exception e)
		{
			logger.error("verifySignature", e);
			return false;
		}
	}

	public String getLogoutAssertions()
	{
		logger.trace("getLogoutAssertions");
		return SamlAssertionUtils.getLogoutInfo(this.xmlDoc);
	}

	public String getRelayState()
	{
		logger.trace("getRelayState");
		try {
			return this.redirectMessage.get("RelayState") == null ? "" : URLDecoder.decode(this.redirectMessage.get("RelayState"), StandardCharsets.UTF_8.name());
		}catch (Exception e)
		{
			logger.error("getRelayState", e);
			return "";
		}
	}

	public String getLoginAssertions()
	{
		//Getting user's data by URL parms (GET) is deemed insecure so we are not implementing this method for redirect binding
		logger.error("getLoginAssertions - NOT IMPLEMENTED insecure SAML implementation");
		return "";
	}

	public String getRoles(String name)
	{
		//Getting user's data by URL parms (GET) is deemed insecure so we are not implementing this method for redirect binding
		logger.error("getRoles - NOT IMPLEMENTED insecure SAML implementation");
		return "";
	}

	public String getLoginAttribute(String name)
	{
		//Getting user's data by URL parms (GET) is deemed insecure so we are not implementing this method for redirect binding
		logger.error("getLoginAttribute - NOT IMPLEMENTED insecure SAML implementation");
		return "";
	}

	// EXTERNAL OBJECT PUBLIC METHODS  - END

	private static Map<String, String> parseRedirect(String request)
	{
		logger.trace("parseRedirect");
		Map<String,String> result = new HashMap<>();
		String[] redirect = request.split("&");

		for(String s : redirect)
		{
			String[] res = s.split("=");
			result.put(res[0], res[1]);
		}
		return result;
	}

	private static String generateQuery(Document request, String destination, String certPath, String certPass, String alias, String relayState)
	{
		logger.trace("generateQuery");
		try {
			String samlRequestParameter = Encoding.delfateAndEncodeXmlParameter(Encoding.documentToString(request));
			String relayStateParameter = URLEncoder.encode(relayState, StandardCharsets.UTF_8.name());
			Hash hash = Keys.isBase64(certPath) ? Hash.getHash(certPass.toUpperCase().trim()) : Hash.getHash(Keys.getHash(certPath, alias, certPass));

			String sigAlgParameter = URLEncoder.encode(Hash.getSigAlg(hash), StandardCharsets.UTF_8.name());
			String query = MessageFormat.format("SAMLRequest={0}&RelayState={1}&SigAlg={2}", samlRequestParameter, relayStateParameter, sigAlgParameter);
			String signatureParameter = URLEncoder.encode(signRequest_RedirectBinding(query, certPath, certPass, hash, alias), StandardCharsets.UTF_8.name());

			query += MessageFormat.format("&Signature={0}", signatureParameter);

			logger.debug(MessageFormat.format("generateQuery - query: {0}", query));
			return MessageFormat.format("{0}?{1}", destination, query);
		}catch (Exception e)
		{
			logger.error("generateQuery", e);
			return "";
		}

	}

	private static String signRequest_RedirectBinding(String query, String path, String password, Hash hash, String alias)
	{
		logger.trace("signRequest_RedirectBinding");
		RSADigestSigner signer= new RSADigestSigner(Hash.getDigest(hash));
		byte[] inputText = query.getBytes(StandardCharsets.UTF_8);
		try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
			setUpSigner(signer, inputStream, Keys.loadPrivateKey(path, alias, password), true);
			byte[] outputBytes = signer.generateSignature();
			return Base64.toBase64String(outputBytes);
		}catch (Exception e)
		{
			logger.error("signRequest_RedirectBinding", e);
			return "";
		}
	}

	private static void setUpSigner(Signer signer, InputStream input, AsymmetricKeyParameter asymmetricKeyParameter,
									boolean toSign) {
		logger.trace("setUpSigner");
		byte[] buffer = new byte[8192];
		int n;
		try {
			signer.init(toSign, asymmetricKeyParameter);
			while ((n = input.read(buffer)) > 0) {
				signer.update(buffer, 0, n);
			}
		} catch (Exception e) {
			logger.error("setUpSigner", e);
		}
	}
}
