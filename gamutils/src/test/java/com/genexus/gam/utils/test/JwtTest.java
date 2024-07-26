package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.genexus.gam.utils.Jwt;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileReader;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.UUID;

public class JwtTest {

	private static String resources;
	private static String header;
	private static String payload;
	private static String kid;
	private static RSAKey keypair;
	private static String path_RSA_sha256_2048;
	private static String alias;
	private static String password;
	private static String tokenFile;

	@BeforeClass
	public static void setUp() {
		resources = System.getProperty("user.dir").concat("/src/test/resources");
		kid = UUID.randomUUID().toString();
		header = "{\n" +
			"  \"alg\": \"RS256\",\n" +
			"  \"kid\": \"" + kid + "\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}";
		payload = "{\n" +
			"  \"sub\": \"1234567890\",\n" +
			"  \"name\": \"John Doe\",\n" +
			"  \"iat\": 1516239022\n" +
			"}";
		keypair = generateKeys();
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		alias = "1";
		password = "dummy";
		tokenFile = Jwt.create(loadPrivateKey(path_RSA_sha256_2048 + "sha256d_key.pem"), payload, header);
	}

	private static RSAKey generateKeys() {
		try {
			return new RSAKeyGenerator(2048)
				.keyUse(KeyUse.SIGNATURE)
				.keyID(kid)
				.algorithm(Algorithm.parse("RS256"))
				.generate();
		} catch (Exception e) {
			return null;
		}
	}

	@Test
	public void testCreate() {
		try {
			String token = Jwt.create(keypair.toRSAPrivateKey(), payload, header);
			Assert.assertFalse("testCreate fail", token.isEmpty());
		} catch (Exception e) {
			Assert.fail("testCreate fail. Exception: " + e.getMessage());
		}
	}

	@Test
	public void testVerify() {
		try {
			String token = Jwt.create(keypair.toRSAPrivateKey(), payload, header);
			boolean verifies = Jwt.verify(keypair.toRSAPublicKey(), token);
			Assert.assertTrue("testVerify fail", verifies);
		} catch (Exception e) {
			Assert.fail("testVerify fail. Exception: " + e.getMessage());
		}
	}

	@Test
	public void testVerify_wrong() {
		try {
			String token = Jwt.create(keypair.toRSAPrivateKey(), payload, header);
			boolean verifies = Jwt.verify(generateKeys().toRSAPublicKey(), token);
			Assert.assertFalse("testVerify fail", verifies);
		} catch (Exception e) {
			Assert.fail("testVerify_wrong fail. Exception: " + e.getMessage());
		}
	}

	@Test
	public void testVerifyPkcs8() {
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.pem", "", "", tokenFile);
		Assert.assertTrue("testVerifyPkcs8", result);
	}

	@Test
	public void testVerifyDer() {
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.cer", "", "", tokenFile);
		Assert.assertTrue("testVerifyDer", result);
	}

	@Test
	public void testVerifyPkcs12() {
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password, tokenFile);
		Assert.assertTrue("testVerifyPkcs12", result);
	}

	@Test
	public void testVerifyPkcs12_withoutalias() {
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", "", password, tokenFile);
		Assert.assertTrue("testVerifyPkcs12_withoutalias", result);
	}

	private static RSAPrivateKey loadPrivateKey(String path) {
		try (FileReader privateKeyReader = new FileReader(path)) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof PEMKeyPair) {
					PEMKeyPair pemKeyPair = (PEMKeyPair) obj;
					PrivateKeyInfo privateKeyInfo = pemKeyPair.getPrivateKeyInfo();
					KeyFactory kf = KeyFactory.getInstance("RSA");
					PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded());
					return (RSAPrivateKey) kf.generatePrivate(keySpec);
				} else {
					Assert.fail("loadPrivateKey fail, not PEMKeyPair type");
					return null;
				}
			}
		} catch (Exception e) {
			Assert.fail("loadPrivateKey fail with exception " + e.getMessage());
			return null;
		}
	}
}
