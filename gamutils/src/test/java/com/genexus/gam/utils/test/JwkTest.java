package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import com.nimbusds.jose.jwk.JWKSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;

public class JwkTest {

	private static String testJWK;
	private static String header;
	private static String payload;

	@BeforeClass
	public static void setUp() {
		testJWK = "{\"p\":\"2XZj5mZxLJlFjN0rK45gg25TtaDUrJ38-EK4F6n60X13Vb4oSeOUNWr7w1S2ibh7RFD687lKY588APrXK1zF33Ape3pRDz3JFZdPx5K_g_hK-ZlLNlHzXTk0okWqIszKdi2v_RLH-zow4GE-rwtW-bKDj3pdKB0_KWafhIv7X5c\",\"kty\":\"RSA\",\"q\":\"1ZsAPTJAcVLDVNFa_cNjzg0NS9DZZYsga22ESKPXy_pPkH0oOzlRm40BcYxiPR2yVDx0Ya7x_ekXQ9K-pKBE_U0YBE2PR6rDUV6Vr21O9BNuWUNXCg_VYosmyVF4hEG0itJty7OYXZPRCr9hAWRl7DjqGKZ1Y5wnaNMCR4gGuss\",\"d\":\"VxkWReeOVfMKlbi2Grf_XIddyMzZ0oid5BRjagXYnmtjxlddcIUrS0vJGPqSSbtyDN7-gPB98rSsJfzDBx3tbN9Vjr6vCs3pk_mi6xnCA1iKbIvW3wM3s4rP_6vCMBdwgSxIHufpLsmB9esPBuwYpOdEXJvRk_F6swSR6J6gBcNR3uL8Ht0OxtfiDP0X3C2aHe5HM9jkLlrqjcNWZDu1dXnDGh2fILF5lAPI7qssYuZzddOE5I7GMh3DuwfQ73W3ITyiezELJMnDKaEQUJAAEDWoqcGCGB14jDJwyKSNROouQtby_RP2uM-IO3_Hq1-u2Wu1QCXNK3R17NVeV_ktZw\",\"e\":\"AQAB\",\"use\":\"sig\",\"kid\":\"c22bd870-634c-4860-9ccf-e38eca0b315b\",\"qi\":\"j5iACnxbBCPfPIv-QcGID0xLFWZAN7wU9t3B_16M-axl8OOQ0ll4ZjQ4hYmw_eYOk9TY7HbzR3SqfLoCh_As5QFvZhO1E_9JHw2IXDBBbkXY5m3vCGtrUwQXPX83nIHlVk6pJh6d5A3tfLm5ECz0nkiYbGdmtISsMiR-zfVryng\",\"dp\":\"prU01o8YGcmSYO-4RZbLdFZiw-18vKwNH0D-od2EU47sqgWyGxrlJqJSSScrHJ8ZmIDAMZGNbpvGwzWJOEvRwX3ZvzhA5f9GpU-vMF7WhNQWngwfdZATkhblu7TOPgli-IAD123Lc1Pj3k-OX2DBF4D7jEWRHsx0_EcY6OLrHRc\",\"alg\":\"RS256\",\"dq\":\"F6f03NIl5Ob_jvMompX7BaTYZh8ZFG_WBU-5qLnMemCcUyopPHXandl94W9kqdQSHdYcJX1Ue4RG-VHrnxvIyCyzjjZwucUloGtTNHxslAda3zPf_dNHFITIpN8K88q7DezEEB0xsJtgOUp8mcTerMyY0GYO9hsjGi7UP8vGwwU\",\"n\":\"tXMsASuh2-d6MJWifChlbsGKpcMYJtpczMeMdtZHdZgPBR-y2obZMOVjiuVOciU6bV8BtmLjc1IF1xENfL0LFgO9G9QvUyFfLHIhZR7c-NsELe5tC0aH_tDqYtjDTb-o9s6FZREFTrFy4R8jc6ZmfHtUPpzrv1SC3-cr1-sCi9TXlUtHRmYOLDbJFqriuRMhWxd7iPqeVac7tqGWCa_ALDERjDaZa0TLLLEbO6EubiLfk5lavO0jWOw9dxsvYouV0JGbxeczyevis5vC3XD3_yQJVC-r37lNDRiMbDD8hclMa75HXYJp7LNvScB5K6YERzfSullL7DF2q09dGVKCvQ\"}";
		header = "{\n" +
			"  \"alg\": \"RS256\",\n" +
			"  \"kid\": \"c22bd870-634c-4860-9ccf-e38eca0b315b\",\n" +
			"  \"typ\": \"JWT\"\n" +
			"}";
		payload = "{\n" +
			"  \"sub\": \"1234567890\",\n" +
			"  \"name\": \"John Doe\",\n" +
			"  \"iat\": 1516239022\n" +
			"}";
	}

	@Test
	public void testGenerateKeyPair() {
		String jwk = GamUtilsEO.generateKeyPair();
		Assert.assertFalse("Generate key pair jwk", jwk.isEmpty());
	}

	@Test
	public void testPublicJwk() {
		String jwk = GamUtilsEO.generateKeyPair();
		String public_jwk = GamUtilsEO.getPublicJwk(jwk);
		String public_jwks = "{\"keys\": [" + public_jwk + "]}";
		try {
			JWKSet jwks = JWKSet.parse(public_jwks).toPublicJWKSet();
			Assert.assertNotNull("To public JWK fail", jwks);
		} catch (ParseException e) {
			Assert.fail("Exception on testPublicJwk" + e.getMessage());
		}
	}

	@Test
	public void testCreateJwt() {
		String jwt = GamUtilsEO.jwk_createJwt(testJWK, payload, header);
		Assert.assertNotNull("testCreateJwt fail", jwt);
	}

	@Test
	public void testVerifyJwt() {
		String jwt = GamUtilsEO.jwk_createJwt(testJWK, payload, header);
		boolean result = GamUtilsEO.jwk_verifyJWT(testJWK, jwt);
		Assert.assertTrue("testVerifyJwt fail", result);
	}

	@Test
	public void testVerifyJwt_wrong() {
		String jwk = GamUtilsEO.generateKeyPair();
		String jwt = GamUtilsEO.jwk_createJwt(jwk, payload, header);
		boolean result = GamUtilsEO.jwk_verifyJWT(testJWK, jwt);
		Assert.assertFalse("testVerifyJwt_wrong fail", result);
	}

}
