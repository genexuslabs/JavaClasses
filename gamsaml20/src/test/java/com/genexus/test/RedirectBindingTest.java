package com.genexus.test;

import com.genexus.saml20.RedirectBinding;
import com.genexus.saml20.SamlParms;
import com.genexus.saml20.utils.Encoding;
import com.genexus.saml20.utils.Hash;
import com.genexus.saml20.utils.Keys;
import com.genexus.saml20.utils.SamlAssertionUtils;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class RedirectBindingTest {

	public static final String resources = System.getProperty("user.dir").concat("/src/test/resources");

	private static String alias;
	private static String password;
	private static RedirectBinding redirectBindingLogoutResponse;

	@BeforeClass
	public static void setUp() {
		alias = "1";
		password = "dummy1";

		redirectBindingLogoutResponse = new RedirectBinding();
		String logoutResponse = "SAMLResponse=fVHLauQwEPwVo7tsSX7JwnYICVkCyR52ZnPYyyBLPYnBozZueZPPX8%2BEOQSWHJvqquqqbm8%2BTlPyFxYaMXRMpoIlEBz6Mbx27Pf%2BgWuWULTB2wkDdCwgu%2BlbsqdpNk%2F4imv8BTRjIEg2pUDmAnVsXYJBSyOZYE9AJjqzu31%2BMioVZl4wosOJJfdAcQw2XszfYpzJZNmEzk5vSDH7cft8OLMOdxgCuIjLT4gPmaUJU0vzB0se7zt2GKq8sMo7nov6yItKAdd60FwcRV0rXRWFhm01XC%2Fd40aq68ZrbS23FQheQJVz7SvNS%2B%2B9ODYbLO1GIlrhMZwbiB1TQpVclFw2e1kaKY3KU1XWf1jycm1wy8f69kJbPhv5vgtLBMs5P%2Buv%2BSlS%2Bj4Gj%2B%2BUBohZCa4ehmPDpa7VFs81fFBCcitBatk01ZCXWZt9el5%2Fs4s2rvR1ukMPyYudVvj%2BJrpsm93qHBCxrG%2Bzr6LZ%2F%2F7f%2FwM%3D&RelayState=http%3A%2F%2Frelaystate.com&SigAlg=http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha256&Signature=ZhxqgSDAmtwxtUAXCafCNAXKLwL9iPgsqInuZfQ97dyPsGyszpgJftjgHBtoQpz159NjFpX0dGicVier2TQa82JBqgxUvdPT6mg%2FppdG7Z%2BnOXNttflqCd7mA3b%2FUOmWE4XgODz2mym%2BNPBmETAYmKofXo5ghpQc8IgGpI166%2F5VOwwhLcrg76HeYSxubxS4BoFUtLmpRnkaww9VQPZPIyh4kBmsCqe%2FV4QvM626ehdXDjPIciBgylt2ENMfQGZo83ubMB7KxgDNdErBgmTpILxftLn3ZH0FJAbM%2B3bzj6DFJ1yLuyUnUbdxOjoKaRskil853jKqmbvQtxRQ4QvZIg%3D%3D";
		redirectBindingLogoutResponse.init(logoutResponse);
	}

	@Test
	public void testSignatureValidation_true() {
		SamlParms parms = new SamlParms();
		parms.setTrustCertPath(resources.concat("/mykeystore.jks"));
		parms.setTrustCertAlias(alias);
		parms.setTrustCertPass(password);
		Assert.assertTrue("testSignatureValidation_true Logout", redirectBindingLogoutResponse.verifySignatures(parms));
	}

	@Test
	public void testSignatureValidation_false() {
		SamlParms parms = new SamlParms();
		parms.setTrustCertPath(resources.concat("/keystore.jks"));
		parms.setTrustCertAlias(alias);
		parms.setTrustCertPass(password);
		Assert.assertFalse("testSignatureValidation_false Logout", redirectBindingLogoutResponse.verifySignatures(parms));
	}


	@Test
	public void testGetLogoutAssertions() {
		String expected = "{\"Destination\": \"https://localhost/GAM_SAML_ConnectorNetF/aslo.aspx\",\"InResponseTo\": \"_779d88aa-a6e0-4e63-8d68-5ddd0f97791a\",\"Value\": \"urn:oasis:names:tc:SAML:2.0:status:Success\",\"Issuer\": \"https://sts.windows.net/5ec7bbf9-1872-46c9-b201-a1e181996b35/\" }";
		Assert.assertEquals(expected, redirectBindingLogoutResponse.getLogoutAssertions());
	}

	@Test
	public void testIsLogout() {
		String loginResponse = "SAMLResponse=5Vbfb%2BJGEH7uSf0frH03%2FoGNjRW40qSpkJJcFOipvZdoWQ%2FgO3vX3V0Hcn99Zw0mYHqE3lWqTn2yPDOe%2Beb7dmZ98XZd5NYTSJUJPiBexyUWcCbSjC8G5LfptR0TS2nKU5oLDgPCBXk7vFC0yP0yeQBVCq7AwiRcJRvrgFSSJ4KqTCWcFqASzZLJ6PYm8TtuUkqhBRM52X6zVgOy1LpMHGe1WnVW3Y6QC8d3Xc%2F5%2FfZmwpZQUGJdgdIZp7oGuQ3PBaP5Uijt%2FDq6fTQFHi8F58C0kHegrx1KmepQVa6JNb4akCy1w27kRj0v7rluHHleEEZe0I17YRB3%2BxjEm36mYkAeGcy7bBa5duy5gR0wd2bHaRrazAv6EaPhPEgR2VipCsbcUKQHxHf90HZD23enXj8JeokXdmK394FY7xuKkQQy%2FPHNDxsOk%2Fp7uU%2Fgaf6oUiAND8S6FrKg%2BnS4sWDj8zo0Aa4z%2FUyGe4SLT5p2mCgcWH%2Fyn0q6iP7sxn88FM%2FTMI0unH2QL6jLZKKprpSxtEyXIgXrPc0rOA1M1dHJpGIMlCJOndw5yr5ladR0%2FVVEbdWPfb%2FXDyK%2F6wZ9PAhhL%2FK9KHY9P%2Br77jco2ZLyv5ClgTCpZh9xAGpTY7vDWuOrV2F5Ha8Nq6BZPkpTaQQa0hw%2B4hqQovMZZjTPhfppYQIMygbQptRB9S0inMx5ZrIaDW9BL0V6miFWJDOgEuSG4lP5rqimXze8d0K%2F4%2B%2FkaK5BtiUPvY3kEUr%2BACwrMzDn4p8vH2fLh%2FPFBjYKOscSbnvG2DQzgcog%2FhlQHzg6oXtwz2zrQKdRlWKDDJBFLTO2g3UUMJzcY8sN2p31oMcvJWv8Lw3ttzmq9JKb0YcCqbbq1zPmcYLHE1ONeQrrc5VvtY6FEJKGtW73%2FOK5zHGjPMB8eHLpsISZODTf42MlZHqPFx7qCelUUo4nVOoXlv4me4vHFrIDx46qAxI1Mj6rNBx6j9yWmVUcQVySv5gxJrXhnN1FMYWxNmtihnGsNaW7MvVNsLvqs9fvejurFWeAPwgqS%2FRziSjXuPExIV%2Bct4YOq7cobXyv0nKdSaXN6%2FdCzWKdwtO%2Fy8GNWGT8%2F3k0jqwHA7XzNn8ZaNz9vjQ30fAv&RelayState=http%3A%2F%2Frelaystate.com&SigAlg=http%3A%2F%2Fwww.w3.org%2F2001%2F04%2Fxmldsig-more%23rsa-sha256&Signature=aeNkIQLkkrNIxVgd1slzZXJpkEzvU0LIwqMR9wWLRT%2FjMHo7ldaCeGlFk3H%2Bbr4l3qEttjsTBWgTGgPDgzax7DDCUSvJPdAh0YB14T9oZ243cxap2OOi483TkBPt%2BwM6Q4AaePWbH1NdUvFUmP9ovl4Ub3iC4O%2FmZFRR3l4TU4z5ZR5OO8%2FFm%2BppvYXf%2FJDbsTLkKgF72a1lD1YhNWdqYKx3%2BQ22x94osmXis3omG7cdNDlo8ULesWL2RVXzftjmHa9zqWidTrHjyA6fSouTV3pQHmzrI8t9g3tuk5jKzTbOPmF2KBhEPzvN26jH2Bdy5b4PCvkJ1L9VeJKlGwBejQ%3D%3D";
		RedirectBinding redirectBindingLoginResponse = new RedirectBinding();
		redirectBindingLoginResponse.init(loginResponse);
		Assert.assertTrue("testIsLogout Logout", redirectBindingLogoutResponse.isLogout());
		Assert.assertFalse("testIsLogout Login", redirectBindingLoginResponse.isLogout());
	}

	@Test
	public void testGetRelayState() {
		String expected = "http://relaystate.com";
		Assert.assertEquals(expected, redirectBindingLogoutResponse.getRelayState());
	}

	@Test
	public void testLoginRequest() {
		String function = "Login";
		RedirectBinding redirectBinding = new RedirectBinding();
		SamlParms parms = createParameters();
		String samlRequest = redirectBinding.login(parms, "http://relaystate.com");
		String queryString = getQueryString(samlRequest);
		Map<String, String> redirectMessage = parseRedirect(queryString);

		//test login request parameters
		testRequestParameters(redirectMessage, function);

		//test login signature
		testRequestSignature(redirectMessage, function);

		//test login request xml parameters
		String xml = Encoding.decodeAndInflateXmlParameter(redirectMessage.get("SAMLRequest"));
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><saml2p:AuthnRequest AssertionConsumerServiceURL=\"http://myapp.com/acs\" Destination=\"http://endpoint/saml\" ForceAuthn=\"false\" ID=\"_idtralala\" IssueInstant=\"" + getIssuerInstant(xml, "AuthnRequest") + "\" Version=\"2.0\" xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\"><saml2:Issuer xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\"/><saml2p:NameIDPolicy AllowCreate=\"true\" Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress\" SPNameQualifier=\"SPEntityID\"/><saml2p:RequestedAuthnContext Comparison=\"exact\"><saml2:AuthnContextClassRef xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml2:AuthnContextClassRef></saml2p:RequestedAuthnContext></saml2p:AuthnRequest>";
		Assert.assertEquals("Test Login request xml parameters", expectedXml, xml);

	}

	@Test
	public void testLogoutRequest() {
		String function = "Logout";
		RedirectBinding redirectBinding = new RedirectBinding();
		SamlParms parms = createParameters();
		String samlRequest = redirectBinding.logout(parms, "http://relaystate.com");
		String queryString = getQueryString(samlRequest);
		Map<String, String> redirectMessage = parseRedirect(queryString);

		//test logout request parameters
		testRequestParameters(redirectMessage, function);

		//test logout signature
		testRequestSignature(redirectMessage, function);

		//test logout request xml parameters
		String xml = Encoding.decodeAndInflateXmlParameter(redirectMessage.get("SAMLRequest"));
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><saml2p:LogoutRequest Destination=\"http://idp.com/slo\" ID=\"_idtralala\" IssueInstant=\"" + getIssuerInstant(xml, "LogoutRequest") + "\" Reason=\"urn:oasis:names:tc:SAML:2.0:logout:user\" Version=\"2.0\" xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\"><saml2:Issuer xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">SPEntityID</saml2:Issuer><saml2:NameID xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">nameID</saml2:NameID><saml2p:SessionIndex>123456789</saml2p:SessionIndex></saml2p:LogoutRequest>";
		Assert.assertEquals("Test Logout request xml parameters", expectedXml, xml);

	}

	private void testRequestParameters(Map<String, String> redirectMessage, String function) {
		String relayState = decodeParm(redirectMessage.get("RelayState"));
		Assert.assertEquals(MessageFormat.format("Test {0} parameters RelayState", function), "http://relaystate.com", relayState);
		String sigAlg = decodeParm(redirectMessage.get("SigAlg"));
		Assert.assertEquals(MessageFormat.format("Test {0} request parameters SigAlg", function), "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", sigAlg);
	}

	private void testRequestSignature(Map<String, String> redirectMessage, String function) {
		boolean verifies = verifySignature_internal(resources.concat("/mykeystore.jks"), password, alias, redirectMessage);
		Assert.assertTrue(MessageFormat.format("Test {0} request signature", function), verifies);
	}

	private static String getIssuerInstant(String xml, String name) {
		Document doc = SamlAssertionUtils.canonicalizeXml(xml);
		return doc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:protocol", name).item(0).getAttributes().getNamedItem("IssueInstant").getNodeValue();

	}

	private static String decodeParm(String parm) {
		try {
			return URLDecoder.decode(parm, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static String getQueryString(String samlRequest) {
		try {
			java.net.URL url = new java.net.URL(samlRequest);
			return url.getQuery();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static SamlParms createParameters() {
		SamlParms parms = new SamlParms();
		parms.setCertPath(resources.concat("/mykeystore.jks"));
		parms.setCertPass(password);
		parms.setCertAlias(alias);
		parms.setAcs("http://myapp.com/acs");
		parms.setForceAuthn(false);
		parms.setServiceProviderEntityID("EntityID");
		parms.setServiceProviderEntityID("SPEntityID");
		parms.setPolicyFormat("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress");
		parms.setAuthnContext("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
		parms.setEndPointLocation("http://endpoint/saml");
		parms.setId("_idtralala");
		parms.setSingleLogoutEndpoint("http://idp.com/slo");
		parms.setSessionIndex("123456789");
		parms.setServiceProviderEntityID("SPEntityID");
		parms.setNameID("nameID");
		return parms;
	}


	private static Map<String, String> parseRedirect(String request) {
		Map<String, String> result = new HashMap<>();
		String[] redirect = request.split("&");

		for (String s : redirect) {
			String[] res = s.split("=");
			result.put(res[0], res[1]);
		}
		return result;
	}

	private static boolean verifySignature_internal(String certPath, String certPass, String certAlias, Map<String, String> redirectMessage) {


		byte[] signature = Encoding.decodeParameter(redirectMessage.get("Signature"));

		String signedMessage;
		if (redirectMessage.containsKey("RelayState")) {
			signedMessage = MessageFormat.format("SAMLRequest={0}", redirectMessage.get("SAMLRequest"));
			signedMessage += MessageFormat.format("&RelayState={0}", redirectMessage.get("RelayState"));
			signedMessage += MessageFormat.format("&SigAlg={0}", redirectMessage.get("SigAlg"));
		} else {
			signedMessage = MessageFormat.format("SAMLRequest={0}", redirectMessage.get("SAMLRequest"));
			signedMessage += MessageFormat.format("&SigAlg={0}", redirectMessage.get("SigAlg"));
		}

		byte[] query = signedMessage.getBytes(StandardCharsets.UTF_8);

		X509Certificate cert = Keys.loadCertificate(certPath, certAlias, certPass);

		try (InputStream inputStream = new ByteArrayInputStream(query)) {
			String sigalg = URLDecoder.decode(redirectMessage.get("SigAlg"), StandardCharsets.UTF_8.name());
			RSADigestSigner signer = new RSADigestSigner(Hash.getDigest(Hash.getHashFromSigAlg(sigalg)));
			setUpSigner(signer, inputStream, Keys.getAsymmetricKeyParameter(cert), false);
			return signer.verifySignature(signature);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void setUpSigner(Signer signer, InputStream input, AsymmetricKeyParameter asymmetricKeyParameter, boolean toSign) {

		byte[] buffer = new byte[8192];
		int n;
		try {
			signer.init(toSign, asymmetricKeyParameter);
			while ((n = input.read(buffer)) > 0) {
				signer.update(buffer, 0, n);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
