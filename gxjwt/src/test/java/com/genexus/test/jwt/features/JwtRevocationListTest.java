package com.genexus.test.jwt.features;

import com.genexus.JWT.JWTCreator;
import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.JWT.utils.RevocationList;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class JwtRevocationListTest extends SecurityAPITestObject {

	protected static String ID;
	protected static JWTCreator jwt;
	protected static JWTOptions options;
	protected static PrivateClaims claims;
	protected static SymmetricKeyGenerator keyGen;
	protected static String token;
	protected static RevocationList rList;


	@BeforeClass
	public static void setUp() {
		jwt = new JWTCreator();
		options = new JWTOptions();
		keyGen = new SymmetricKeyGenerator();
		claims = new PrivateClaims();
		rList = new RevocationList();


		options.addRegisteredClaim("iss", "GXSA");
		options.addRegisteredClaim("sub", "subject1");
		options.addRegisteredClaim("aud", "audience1");
		ID = "0696bb20-6223-4a1c-9ebf-e15c74387b9c, 0696bb20-6223-4a1c-9ebf-e15c74387b9c";//&guid.Generate()
		options.addRegisteredClaim("jti", ID);
		claims.setClaim("hola1", "hola1");
		claims.setClaim("hola2", "hola2");

		String hexaKey = keyGen.doGenerateKey("GENERICRANDOM", 256);
		options.setSecret(hexaKey);
		options.addRevocationList(rList);

		token = jwt.doCreate("HS256", claims, options);
	}

	@Test
	public void testPositive() {
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		True(verification, jwt);
	}

	@Test
	public void testNegative() {
		rList.addIDToRevocationList(ID);
		boolean verification = jwt.doVerify(token, "HS256", claims, options);
		assertFalse(verification);
		assertFalse(jwt.hasError());

	}
}
