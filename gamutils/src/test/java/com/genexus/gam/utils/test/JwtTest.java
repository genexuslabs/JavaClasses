package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.nimbusds.jose.jwk.JWK;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class JwtTest {

	private static String headerRsa;
	private static String payload;
	private static String path_RSA_sha256_2048;
	private static String alias;
	private static String password;

	@BeforeClass
	public static void setUp() {

		String resources = System.getProperty("user.dir").concat("/src/test/resources");
		String kid = UUID.randomUUID().toString();
		headerRsa = "{\n" +
			"  \"alg\": \"RS256\",\n" +
			"  \"kid\": \"" + kid + "\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}";
		payload = "{\n" +
			"  \"sub\": \"1234567890\",\n" +
			"  \"name\": \"John Doe\",\n" +
			"  \"iat\": 1516239022\n" +
			"}";
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		alias = "1";
		password = "dummy";
	}

	@Test
	public void test_pkcs8_pem() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256d_key.pem", "", "", payload, headerRsa);
		Assert.assertFalse("test_pkcs8 create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.cer", "", "", token);
		Assert.assertTrue("test_pkcs8 verify cer", result);
	}

	@Test
	public void test_get() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256d_key.pem", "", "", payload, headerRsa);
		Assert.assertFalse("test_get create", token.isEmpty());
		String header_get = GamUtilsEO.getJwtHeader(token);
		Assert.assertFalse("test_get getHeader", header_get.isEmpty());
		String payload_get = GamUtilsEO.getJwtPayload(token);
		Assert.assertFalse("test_get getPayload", payload_get.isEmpty());
	}

	@Test
	public void test_pkcs8_key() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256d_key.key", "", "", payload, headerRsa);
		Assert.assertFalse("test_pkcs8 create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.crt", "", "", token);
		Assert.assertTrue("test_pkcs8 verify crt", result);
	}

	@Test
	public void test_pkcs8_encrypted() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256_key.pem", "", password, payload, headerRsa);
		Assert.assertFalse("test_pkcs8_encrypted", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.crt", "", "", token);
		Assert.assertTrue("test_pkcs8_encrypted verify crt", result);
	}

	@Test
	public void test_pkcs12_p12() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password, payload, headerRsa);
		Assert.assertFalse("test_pkcs12_p12 create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password, token);
		Assert.assertTrue("test_pkcs12_p12 verify", result);
	}

	@Test
	public void test_pkcs12_pkcs12() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256_cert.pkcs12", alias, password, payload, headerRsa);
		Assert.assertFalse("test_pkcs12_pkcs12 create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.pkcs12", alias, password, token);
		Assert.assertTrue("test_pkcs12_pkcs12 verify", result);
	}

	@Test
	public void test_pkcs12_jks() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256_cert.jks", alias, password, payload, headerRsa);
		Assert.assertFalse("test_pkcs12_jks create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.jks", alias, password, token);
		Assert.assertTrue("test_pkcs12_jks verify", result);
	}

	@Test
	public void test_pkcs12_pfx() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256_cert.pfx", alias, password, payload, headerRsa);
		Assert.assertFalse("test_pkcs12_pfx create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.pfx", alias, password, token);
		Assert.assertTrue("test_pkcs12_pfx verify", result);
	}

	@Test
	public void test_pkcs12_noalias() {
		String token = GamUtilsEO.createJwtRsa(path_RSA_sha256_2048 + "sha256_cert.jks", "", password, payload, headerRsa);
		Assert.assertFalse("test_pkcs12_noalias jks create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(path_RSA_sha256_2048 + "sha256_cert.jks", "", password, token);
		Assert.assertTrue("test_pkcs12_noalias jks verify", result);
	}

	@Test
	public void test_b64() {
		String publicKey = "MIIEATCCAumgAwIBAgIJAIAqvKHZ+gFhMA0GCSqGSIb3DQEBCwUAMIGWMQswCQYDVQQGEwJVWTETMBEGA1UECAwKTW9udGV2aWRlbzETMBEGA1UEBwwKTW9udGV2aWRlbzEQMA4GA1UECgwHR2VuZVh1czERMA8GA1UECwwIU2VjdXJpdHkxEjAQBgNVBAMMCXNncmFtcG9uZTEkMCIGCSqGSIb3DQEJARYVc2dyYW1wb25lQGdlbmV4dXMuY29tMB4XDTIwMDcwODE4NTcxN1oXDTI1MDcwNzE4NTcxN1owgZYxCzAJBgNVBAYTAlVZMRMwEQYDVQQIDApNb250ZXZpZGVvMRMwEQYDVQQHDApNb250ZXZpZGVvMRAwDgYDVQQKDAdHZW5lWHVzMREwDwYDVQQLDAhTZWN1cml0eTESMBAGA1UEAwwJc2dyYW1wb25lMSQwIgYJKoZIhvcNAQkBFhVzZ3JhbXBvbmVAZ2VuZXh1cy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC1zgaU+Wh63p9DNWoAy64252EvZjN49AY3x0QCnAa8JO9Pk7znQwrxEFUKgZzv0GHEYW7+X+uyJr7BW4TA6fuJJ8agE/bmZRZyjdJjoue0FML6fbmCZ9Tsxpxe4pzispyWQ8jYT4Kl4I3fdZNUSn4XSidnDKBISeC05mrcchDKhInpiYDJ481lsB4JTEti3S4Xy/ToKwY4t6attw6z5QDhBc+Yro+YUqruliOAKqcfybe9k07jwMCvFVM1hrYYJ7hwHDSFo3MKwZ0y2gw0w6SgVBxLFo+KYP3q63b5wVhD8lzaSh+8UcyiHM2/yjEej7EnRFzdclTSNXRFNaiLnEVdAgMBAAGjUDBOMB0GA1UdDgQWBBQtQAWJRWNr/OswPSAdwCQh0Eei/DAfBgNVHSMEGDAWgBQtQAWJRWNr/OswPSAdwCQh0Eei/DAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQCjHe3JbNKv0Ywc1zlLacUNWcjLbmzvnjs8Wq5oxtf5wG5PUlhLSYZ9MPhuf95PlibnrO/xVY292P5lo4NKhS7VOonpbPQ/PrCMO84Pz1LGfM/wCWQIowh6VHq18PiZka9zbwl6So0tgClKkFSRk4wpKrWX3+M3+Y+D0brd8sEtA6dXeYHDtqV0YgjKdZIIOx0vDT4alCoVQrQ1yAIq5INT3cSLgJezIhEadDv3Tc7bMxMFeL+81qHm9Z/9/KE6Z+JB0ZEOkF/2NSQJd+Z7MBR8CxOdTQis3ltMoXDatNkjZ2Env40sw4NICB8YYhsWMIarew5uNT+RS28YHNlbmogh";
		String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC1zgaU+Wh63p9DNWoAy64252EvZjN49AY3x0QCnAa8JO9Pk7znQwrxEFUKgZzv0GHEYW7+X+uyJr7BW4TA6fuJJ8agE/bmZRZyjdJjoue0FML6fbmCZ9Tsxpxe4pzispyWQ8jYT4Kl4I3fdZNUSn4XSidnDKBISeC05mrcchDKhInpiYDJ481lsB4JTEti3S4Xy/ToKwY4t6attw6z5QDhBc+Yro+YUqruliOAKqcfybe9k07jwMCvFVM1hrYYJ7hwHDSFo3MKwZ0y2gw0w6SgVBxLFo+KYP3q63b5wVhD8lzaSh+8UcyiHM2/yjEej7EnRFzdclTSNXRFNaiLnEVdAgMBAAECggEBAJP8ajslcThisjzg47JWGS8z1FXi2Q8hg1Yv61o8avcHEY0y8tdEKUnkQ3TT4E0M0CgsL078ATz4cNmvhzYIv+j66aEv3w/XRRhl/NWBqx1YsQV5BWHy5sz9Nhe+WnnlbbSa5Ie+4NfpG1LDv/Mi19RZVg15p5ZwHGrkDCP47VYKgFXw51ZPxq/l3IIeq4PyueC/EPSAp4e9qei7p85k3i2yiWsHgZaHwHgDTx2Hgq1y/+/E5+HNxL2OlPr5lzlN2uIPZ9Rix2LDh0FriuCEjrXFsTJHw4LTK04rkeGledMtw6/bOTxibFbgeuQtY1XzG/M0+xlP2niBbAEA4Z6vTsECgYEA6k7LSsh6azsk0W9+dE6pbc3HisOoKI76rXi38gEQdCuF04OKt46WttQh4r1+dseO4OgjXtRMS0+5Hmx2jFXjPJexMgLftvrbwaVqg9WHenKL/qj5imCn4kVaa4Jo1VHFaIY+1b+iv+6WY/leFxGntAki9u4PRogRrUrWLRH9keUCgYEAxqLisgMHQGcpJDHJtI2N+HUgLDN065PtKlEP9o6WBwAb86/InVaTo2gmEvmslNQDYH16zdTczWMHnnBx1B012KpUD+t5CWIvMZdsTnMRDjWhdgm5ylN9NT89t5X8GPvo36WjuXAKWWjcRodzRgo57z9achCyMKhGU5yDOxh8jhkCgYAx6rtwoSlDcwQzAjfEe4Wo+PAL5gcLLPrGvjMiAYwJ08Pc/ectl9kP9j2J2qj4kSclTw9KApyGZuOfUagn2Zxhqkd7yhTzHJp4tM7uay1DrueYR1NyYYkisXfD87J1z8forsDwNLVtglzTy6p56674sgGa7bifZBmv+4OJco286QKBgQC4dGXDHGDNg36G590A1zpw8ILxyM7YPEPOOfxy3rGeypEqV6AZy13KLlq84DFM+xwvrBYvsW1hJIbcsFpjuMRZ8MGjDu0Us6JTkOO4bc32vgKzlBB9O85XdeSf6J1zrenwVOaWut5BbMiwjfOTpMdrzg71QV/XI0w7NGoApJp1cQKBgERfI6AfJTaKtEpfX3udR1B3zra1Y42ppU2TvGI5J2/cItENoyRmtyKYDp2I036/Pe63nuIzs31i6q/hCr9Tv3AGoSVKuPLpCWv5xVO/BPhGs5dwx81nUo0/P+H2X8dx7g57PQY4uf4F9+EIXeAdbPqfB8GBW7RX3FDx5NpB+Hh/";
		String token = GamUtilsEO.createJwtRsa(privateKey, "", "", payload, headerRsa);
		Assert.assertFalse("test_b64 create", token.isEmpty());
		boolean result = GamUtilsEO.verifyJwtRsa(publicKey, "", "", token);
		Assert.assertTrue("test_b64 verify", result);
	}

	@Test
	public void test_json_jwk() {
		String keyPair = GamUtilsEO.generateKeyPair();
		String token = GamUtilsEO.createJwtRsa(keyPair, "", "", payload, headerRsa);
		Assert.assertFalse("test_json_jwk create", token.isEmpty());
		String publicJwk = GamUtilsEO.getPublicJwk(keyPair);
		boolean result = GamUtilsEO.verifyJwtRsa(publicJwk, "", "", token);
		Assert.assertTrue("test_json_jwk verify", result);
	}

	@Test
	public void test_json_jwks() {
		String keyPair = GamUtilsEO.generateKeyPair();
		String publicJwk = GamUtilsEO.getPublicJwk(keyPair);
		String header_jwks = makeHeader(publicJwk);
		String token = GamUtilsEO.createJwtRsa(keyPair, "", "", payload, header_jwks);
		Assert.assertFalse("test_json_jwks create", token.isEmpty());
		String publicJwks = "{\"keys\": [" + publicJwk + "]}";
		boolean result = GamUtilsEO.verifyJwtRsa(publicJwks, "", "", token);
		Assert.assertTrue("test_json_jwks verify", result);
	}

	@Test
	public void test_json_Sha256()
	{
		String header = "{\n" +
			"  \"alg\": \"HS256\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}";
		int[] lengths = new int[]{32, 64, 128};
		for (int n : lengths) {
			String secret = GamUtilsEO.randomAlphanumeric(n);
			String token = GamUtilsEO.createJwtSha(secret, payload, header);
			Assert.assertFalse("test_json_Sha256 create", token.isEmpty());
			boolean result = GamUtilsEO.verifyJwtSha(secret, token);
			Assert.assertTrue("test_json_Sha256 verify", result);
		}
	}

	@Test
	public void test_json_Sha512()
	{
		String header = "{\n" +
			"  \"alg\": \"HS512\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}";
		int[] lengths = new int[]{64, 128};
		for (int n : lengths) {
			String secret = GamUtilsEO.randomAlphanumeric(n);
			String token = GamUtilsEO.createJwtSha(secret, payload, header);
			Assert.assertFalse("test_json_Sha512 create", token.isEmpty());
			boolean result = GamUtilsEO.verifyJwtSha(secret, token);
			Assert.assertTrue("test_json_Sha512 verify", result);
		}
	}

	@Test
	public void test_VerifyAlgorithm_True()
	{
		String header = "{\n" +
			"  \"alg\": \"HS512\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}";
		String secret = GamUtilsEO.randomAlphanumeric(64);
		String token = GamUtilsEO.createJwtSha(secret, payload, header);
		boolean resultSha512 = GamUtilsEO.verifyAlgorithm("HS512", token);
		Assert.assertTrue("test_VerifyAlgorithm_True", resultSha512);
	}

	@Test
	public void test_VerifyAlgorithm_False()
	{
		String header = "{\n" +
			"  \"alg\": \"HS512\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}";
		String secret = GamUtilsEO.randomAlphanumeric(64);
		String token = GamUtilsEO.createJwtSha(secret, payload, header);
		boolean resultSha512 = GamUtilsEO.verifyAlgorithm("RS256", token);
		Assert.assertFalse("test_VerifyAlgorithm_False", resultSha512);
	}

	private static String makeHeader(String publicJwk) {
		try {
			JWK jwk = JWK.parse(publicJwk);
			return "{\n" +
				"  \"alg\": \"RS256\",\n" +
				"  \"kid\": \"" + jwk.getKeyID() + "\",\n" +
				"  \"typ\": \"JWT\"\n" +
				"}";

		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


}
