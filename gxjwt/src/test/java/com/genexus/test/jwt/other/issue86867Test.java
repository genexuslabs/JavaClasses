package com.genexus.test.jwt.other;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("NewClassNamingConvention")
public class issue86867Test extends SecurityAPITestObject {

	protected static JWTCreator jwt;
	protected static JWTOptions options1;
	protected static JWTOptions options2;
	protected static PrivateClaims claims;
	protected static String token;
	protected static String path_RSA_sha256_1024;

	@BeforeClass
	public static void setUp() {
		options1 = new JWTOptions();
		options2 = new JWTOptions();
		jwt = new JWTCreator();
		claims = new PrivateClaims();
		claims.setClaim("hola1", "hola1");
		path_RSA_sha256_1024 = resources.concat("/dummycerts/RSA_sha256_1024/");

		String pathKey = path_RSA_sha256_1024 + "sha256d_key.pem";
		String pathCert = path_RSA_sha256_1024 + "sha256_cert.crt";
		PrivateKeyManager key = new PrivateKeyManager();
		CertificateX509 cert = new CertificateX509();
		key.load(pathKey);
		cert.load(pathCert);

		options1.setCertificate(cert);
		options1.setPrivateKey(key);
		options1.addRegisteredClaim("iss", "GXSA");
		options1.addRegisteredClaim("sub", "subject1");
		options1.addRegisteredClaim("aud", "audience1");

		options2.addRegisteredClaim("iss", "GXSA");
		options2.addRegisteredClaim("sub", "subject1");
		options2.addRegisteredClaim("aud", "audience1");
		options2.setCertificate(cert);

		token = jwt.doCreate("RS256", claims, options1);
	}

	@Test
	public void testVerificationWithoutPrivateKey() {
		boolean validation = jwt.doVerify(token, "RS256", claims, options2);
		assertTrue(validation);
	}

}
