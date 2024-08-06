package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.genexus.gam.utils.json.Jwt;
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
		String resources = System.getProperty("user.dir").concat("/src/test/resources");
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
	public void testCreatePkcs8()
	{
		String token = GamUtilsEO.createJWTWithFile(path_RSA_sha256_2048 + "sha256d_key.pem", "", "", payload, header);
		Assert.assertFalse("testCreatePkcs8", token.isEmpty());
	}

	@Test
	public void testVerifyDer() {
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.cer", "", "", tokenFile);
		Assert.assertTrue("testVerifyDer", result);
	}

	@Test
	public void testVerifyBase64() {
		String base64 = "MIIEATCCAumgAwIBAgIJAIAqvKHZ+gFhMA0GCSqGSIb3DQEBCwUAMIGWMQswCQYDVQQGEwJVWTETMBEGA1UECAwKTW9udGV2aWRlbzETMBEGA1UEBwwKTW9udGV2aWRlbzEQMA4GA1UECgwHR2VuZVh1czERMA8GA1UECwwIU2VjdXJpdHkxEjAQBgNVBAMMCXNncmFtcG9uZTEkMCIGCSqGSIb3DQEJARYVc2dyYW1wb25lQGdlbmV4dXMuY29tMB4XDTIwMDcwODE4NTcxN1oXDTI1MDcwNzE4NTcxN1owgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC1zgaU+Wh63p9DNWoAy64252EvZjN49AY3x0QCnAa8JO9Pk7znQwrxEFUKgZzv0GHEYW7+X+uyJr7BW4TA6fuJJ8agE/bmZRZyjdJjoue0FML6fbmCZ9Tsxpxe4pzispyWQ8jYT4Kl4I3fdZNUSn4XSidnDKBISeC05mrcchDKhInpiYDJ481lsB4JTEti3S4Xy/ToKwY4t6attw6z5QDhBc+Yro+YUqruliOAKqcfybe9k07jwMCvFVM1hrYYJ7hwHDSFo3MKwZ0y2gw0w6SgVBxLFo+KYP3q63b5wVhD8lzaSh+8UcyiHM2/yjEej7EnRFzdclTSNXRFNaiLnEVdAgMBAAGjUDBOMB0GA1UdDgQWBBQtQAWJRWNr/OswPSAdwCQh0Eei/DAfBgNVHSMEGDAWgBQtQAWJRWNr/OswPSAdwCQh0Eei/DAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQCjHe3JbNKv0Ywc1zlLacUNWcjLbmzvnjs8Wq5oxtf5wG5PUlhLSYZ9MPhuf95PlibnrO/xVY292P5lo4NKhS7VOonpbPQ/PrCMO84Pz1LGfM/wCWQIowh6VHq18PiZka9zbwl6So0tgClKkFSRk4wpKrWX3+M3+Y+D0brd8sEtA6dXeYHDtqV0YgjKdZIIOx0vDT4alCoVQrQ1yAIq5INT3cSLgJezIhEadDv3Tc7bMxMFeL+81qHm9Z/9/KE6Z+JB0ZEOkF/2NSQJd+Z7MBR8CxOdTQis3ltMoXDatNkjZ2Env40sw4NICB8YYhsWMIarew5uNT+RS28YHNlbmogh";
		boolean result = GamUtilsEO.verifyJWTWithFile(base64, "", "", tokenFile);
		Assert.assertTrue("testVerifyBase64", result);
	}

	@Test
	public void testVerifyPkcs12() {
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password, tokenFile);
		Assert.assertTrue("testVerifyPkcs12", result);
	}

	@Test
	public void testCreatePkcs12()
	{
		String token = GamUtilsEO.createJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password, payload, header);
		Assert.assertFalse("testCreatePkcs12", token.isEmpty());
	}

	@Test
	public void testCreateBase64()
	{
		String base64 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC1zgaU+Wh63p9DNWoAy64252EvZjN49AY3x0QCnAa8JO9Pk7znQwrxEFUKgZzv0GHEYW7+X+uyJr7BW4TA6fuJJ8agE/bmZRZyjdJjoue0FML6fbmCZ9Tsxpxe4pzispyWQ8jYT4Kl4I3fdZNUSn4XSidnDKBISeC05mrcchDKhInpiYDJ481lsB4JTEti3S4Xy/ToKwY4t6attw6z5QDhBc+Yro+YUqruliOAKqcfybe9k07jwMCvFVM1hrYYJ7hwHDSFo3MKwZ0y2gw0w6SgVBxLFo+KYP3q63b5wVhD8lzaSh+8UcyiHM2/yjEej7EnRFzdclTSNXRFNaiLnEVdAgMBAAECggEBAJP8ajslcThisjzg47JWGS8z1FXi2Q8hg1Yv61o8avcHEY0y8tdEKUnkQ3TT4E0M0CgsL078ATz4cNmvhzYIv+j66aEv3w/XRRhl/NWBqx1YsQV5BWHy5sz9Nhe+WnnlbbSa5Ie+4NfpG1LDv/Mi19RZVg15p5ZwHGrkDCP47VYKgFXw51ZPxq/l3IIeq4PyueC/EPSAp4e9qei7p85k3i2yiWsHgZaHwHgDTx2Hgq1y/+/E5+HNxL2OlPr5lzlN2uIPZ9Rix2LDh0FriuCEjrXFsTJHw4LTK04rkeGledMtw6/bOTxibFbgeuQtY1XzG/M0+xlP2niBbAEA4Z6vTsECgYEA6k7LSsh6azsk0W9+dE6pbc3HisOoKI76rXi38gEQdCuF04OKt46WttQh4r1+dseO4OgjXtRMS0+5Hmx2jFXjPJexMgLftvrbwaVqg9WHenKL/qj5imCn4kVaa4Jo1VHFaIY+1b+iv+6WY/leFxGntAki9u4PRogRrUrWLRH9keUCgYEAxqLisgMHQGcpJDHJtI2N+HUgLDN065PtKlEP9o6WBwAb86/InVaTo2gmEvmslNQDYH16zdTczWMHnnBx1B012KpUD+t5CWIvMZdsTnMRDjWhdgm5ylN9NT89t5X8GPvo36WjuXAKWWjcRodzRgo57z9achCyMKhGU5yDOxh8jhkCgYAx6rtwoSlDcwQzAjfEe4Wo+PAL5gcLLPrGvjMiAYwJ08Pc/ectl9kP9j2J2qj4kSclTw9KApyGZuOfUagn2Zxhqkd7yhTzHJp4tM7uay1DrueYR1NyYYkisXfD87J1z8forsDwNLVtglzTy6p56674sgGa7bifZBmv+4OJco286QKBgQC4dGXDHGDNg36G590A1zpw8ILxyM7YPEPOOfxy3rGeypEqV6AZy13KLlq84DFM+xwvrBYvsW1hJIbcsFpjuMRZ8MGjDu0Us6JTkOO4bc32vgKzlBB9O85XdeSf6J1zrenwVOaWut5BbMiwjfOTpMdrzg71QV/XI0w7NGoApJp1cQKBgERfI6AfJTaKtEpfX3udR1B3zra1Y42ppU2TvGI5J2/cItENoyRmtyKYDp2I036/Pe63nuIzs31i6q/hCr9Tv3AGoSVKuPLpCWv5xVO/BPhGs5dwx81nUo0/P+H2X8dx7g57PQY4uf4F9+EIXeAdbPqfB8GBW7RX3FDx5NpB+Hh/";
		String token = GamUtilsEO.createJWTWithFile(base64, "", "", payload, header);
		Assert.assertFalse("testCreateBase64", token.isEmpty());
	}

	@Test
	public void testVerifyPkcs12_withoutalias() {
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", "", password, tokenFile);
		Assert.assertTrue("testVerifyPkcs12_withoutalias", result);
	}

	@Test
	public void testCreatePkcs12_withoutalias() {
		String token = GamUtilsEO.createJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", "", password, payload, header);
		Assert.assertFalse("testCreatePkcs12_withoutalias", token.isEmpty());
	}

	@Test
	public void testCombine()
	{
		String token = GamUtilsEO.createJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password, payload, header);
		Assert.assertFalse("testCombine create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJWTWithFile(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password, tokenFile);
		Assert.assertTrue("testCombine verify", result);
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
