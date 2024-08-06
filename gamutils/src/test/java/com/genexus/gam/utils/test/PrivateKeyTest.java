package com.genexus.gam.utils.test;

import com.genexus.gam.utils.keys.PrivateKeyUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.interfaces.RSAPrivateKey;

public class PrivateKeyTest {

	private static String path_RSA_sha256_2048;
	private static String alias;
	private static String password;

	@BeforeClass
	public static void setUp() {
		String resources = System.getProperty("user.dir").concat("/src/test/resources");
		path_RSA_sha256_2048 = resources.concat("/dummycerts/RSA_sha256_2048/");
		alias = "1";
		password = "dummy";
	}

	@Test
	public void testLoadPfx() {
		RSAPrivateKey key = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.pfx", alias, password);
		Assert.assertNotNull("testLoadPfx", key);
		RSAPrivateKey key1 = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.pfx", "", password);
		Assert.assertNotNull("testLoadPfx empty alias", key1);
	}

	@Test
	public void testLoadJks() {
		RSAPrivateKey key = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.jks", alias, password);
		Assert.assertNotNull("testLoadJks", key);
		RSAPrivateKey key1 = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.jks", "", password);
		Assert.assertNotNull("testLoadJks empty alias", key1);
	}

	@Test
	public void testLoadPkcs12() {
		RSAPrivateKey key = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.pkcs12", alias, password);
		Assert.assertNotNull("testLoadPkcs12", key);
		RSAPrivateKey key1 = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.pkcs12", "", password);
		Assert.assertNotNull("testLoadPkcs12 empty alias", key1);
	}

	@Test
	public void testLoadP12() {
		RSAPrivateKey key = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.p12", alias, password);
		Assert.assertNotNull("testLoadP12", key);
		RSAPrivateKey key1 = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256_cert.p12", "", password);
		Assert.assertNotNull("testLoadP12 empty alias", key1);
	}

	@Test
	public void testLoadPem() {
		RSAPrivateKey key = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256d_key.pem", "", "");
		Assert.assertNotNull("testLoadPem", key);
	}

	@Test
	public void testLoadKey() {
		RSAPrivateKey key = PrivateKeyUtil.getPrivateKey(path_RSA_sha256_2048 + "sha256d_key.key", "", "");
		Assert.assertNotNull("testLoadKey", key);
	}

	@Test
	public void testLoadBase64() {
		String base64 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC1zgaU+Wh63p9DNWoAy64252EvZjN49AY3x0QCnAa8JO9Pk7znQwrxEFUKgZzv0GHEYW7+X+uyJr7BW4TA6fuJJ8agE/bmZRZyjdJjoue0FML6fbmCZ9Tsxpxe4pzispyWQ8jYT4Kl4I3fdZNUSn4XSidnDKBISeC05mrcchDKhInpiYDJ481lsB4JTEti3S4Xy/ToKwY4t6attw6z5QDhBc+Yro+YUqruliOAKqcfybe9k07jwMCvFVM1hrYYJ7hwHDSFo3MKwZ0y2gw0w6SgVBxLFo+KYP3q63b5wVhD8lzaSh+8UcyiHM2/yjEej7EnRFzdclTSNXRFNaiLnEVdAgMBAAECggEBAJP8ajslcThisjzg47JWGS8z1FXi2Q8hg1Yv61o8avcHEY0y8tdEKUnkQ3TT4E0M0CgsL078ATz4cNmvhzYIv+j66aEv3w/XRRhl/NWBqx1YsQV5BWHy5sz9Nhe+WnnlbbSa5Ie+4NfpG1LDv/Mi19RZVg15p5ZwHGrkDCP47VYKgFXw51ZPxq/l3IIeq4PyueC/EPSAp4e9qei7p85k3i2yiWsHgZaHwHgDTx2Hgq1y/+/E5+HNxL2OlPr5lzlN2uIPZ9Rix2LDh0FriuCEjrXFsTJHw4LTK04rkeGledMtw6/bOTxibFbgeuQtY1XzG/M0+xlP2niBbAEA4Z6vTsECgYEA6k7LSsh6azsk0W9+dE6pbc3HisOoKI76rXi38gEQdCuF04OKt46WttQh4r1+dseO4OgjXtRMS0+5Hmx2jFXjPJexMgLftvrbwaVqg9WHenKL/qj5imCn4kVaa4Jo1VHFaIY+1b+iv+6WY/leFxGntAki9u4PRogRrUrWLRH9keUCgYEAxqLisgMHQGcpJDHJtI2N+HUgLDN065PtKlEP9o6WBwAb86/InVaTo2gmEvmslNQDYH16zdTczWMHnnBx1B012KpUD+t5CWIvMZdsTnMRDjWhdgm5ylN9NT89t5X8GPvo36WjuXAKWWjcRodzRgo57z9achCyMKhGU5yDOxh8jhkCgYAx6rtwoSlDcwQzAjfEe4Wo+PAL5gcLLPrGvjMiAYwJ08Pc/ectl9kP9j2J2qj4kSclTw9KApyGZuOfUagn2Zxhqkd7yhTzHJp4tM7uay1DrueYR1NyYYkisXfD87J1z8forsDwNLVtglzTy6p56674sgGa7bifZBmv+4OJco286QKBgQC4dGXDHGDNg36G590A1zpw8ILxyM7YPEPOOfxy3rGeypEqV6AZy13KLlq84DFM+xwvrBYvsW1hJIbcsFpjuMRZ8MGjDu0Us6JTkOO4bc32vgKzlBB9O85XdeSf6J1zrenwVOaWut5BbMiwjfOTpMdrzg71QV/XI0w7NGoApJp1cQKBgERfI6AfJTaKtEpfX3udR1B3zra1Y42ppU2TvGI5J2/cItENoyRmtyKYDp2I036/Pe63nuIzs31i6q/hCr9Tv3AGoSVKuPLpCWv5xVO/BPhGs5dwx81nUo0/P+H2X8dx7g57PQY4uf4F9+EIXeAdbPqfB8GBW7RX3FDx5NpB+Hh/";
		RSAPrivateKey key = PrivateKeyUtil.getPrivateKey(base64, "", "");
		Assert.assertNotNull("testLoadBase64", key);
	}
}
