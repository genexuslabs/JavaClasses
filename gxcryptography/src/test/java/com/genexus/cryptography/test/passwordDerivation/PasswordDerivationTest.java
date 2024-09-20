package com.genexus.cryptography.test.passwordDerivation;

import com.genexus.cryptography.passwordDerivation.PasswordDerivation;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.encoders.HexaEncoder;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PasswordDerivationTest extends SecurityAPITestObject {


	protected static String password;
	protected static Error error;
	protected static HexaEncoder hexa;
	protected static PasswordDerivation pd;

	// SCRYPT

	protected static String saltScrypt;
	protected static int N;
	protected static int r;
	protected static int p;
	protected static int keyLenght;
	protected static String expectedScrypt;

	// BCRYPT
	protected static int cost;
	protected static String saltBcrypt;
	protected static String expectedBcrypt;

	// ARGON2
	protected static int iterations;
	protected static int parallelism;
	protected static int memory;
	protected static int hashLength;
	protected static String saltArgon2;
	protected static String[] argon2Version;
	protected static String[] argon2HashType;
	protected static String[] argon2Results;

	@BeforeClass
	public static void setUp() {
		new EncodingUtil().setEncoding("UTF8");
		password = "password";
		error = new Error();
		hexa = new HexaEncoder();
		pd = new PasswordDerivation();

		// SCRYPT
		N = 16384;
		r = 8;
		p = 1;
		keyLenght = 256;
		saltScrypt = "123456";
		expectedScrypt = "Yc4uLDXRam2HNQxFdEJWkNYADNWgelNIidOmFE8G3G9G9j6/l/fXP43ErypBMZGs+6PLVPBPRop2pHWWIhUg4hXDoM8+fsp10wBoV3p06yxxdZu7LV19gcgwgL2tnMTN/H8Y7YYM9KpFwdFqMXbIX4DPN2hrL6DAXxNIYJO7Pcm1l9qPrOwpsZZjE032nYlXch6t8/4HRxWFOFRl8t5UjtILEyFdg1w3kLlYzP46XJV1IqGEMyFjeQbtz/c7MteZmID0aSxLtoZJPF3TA41vs09hLlhG/AoMiVQ+EXsp3vZZzg7t4RNrWfuLd2H+oFEqeEUNUisUoB8IWmyAZgn2QQ==";
		// BCRYPT
		cost = 6;
		saltBcrypt = "0c6a8a8235bb90219d004aa4056ec884";
		expectedBcrypt = "XoHha7SLqyY2AKgIIetMdjYBM5bizqPc";
		// ARGON2
		iterations = 1;
		parallelism = 2;
		memory = 4;
		hashLength = 32;
		saltArgon2 = "14ae8da01afea8700c2358dcef7c5358d9021282bd88663a4562f59fb74d22ee";
		argon2Version = new String[]{"ARGON2_VERSION_10", "ARGON2_VERSION_13"};
		argon2HashType = new String[]{"ARGON2_d", "ARGON2_i", "ARGON2_id"};
		argon2Results = new String[]{"f9hF4rzwC9AvfFMK8ZHvKoQeipc7OUQ/dBV4nBer57U=", "QuNCd8sy8STFTBeylfaUVWAN8w3PDl0L94rr9TqaK/g=", "El6fozCF2xSzdcrfR0QO8U1Zmh4OuRZPwufAvqXcLiY=", "xvlSYizqgM93gmi7cTDvkXda41QDj6fTaCC2cpltt3E=", "jDKqkLzOaxFQ2vHwB3/UQiSI2wO+2cDk6Y1VQwSXzz4=", "A8icyy1A7VlunnJKBZXJl/BkNmVQ5FlMznCNKS1YJCM="};

	}

	@Test
	public void testScrypt() {
		String derivated = pd.doGenerateSCrypt(password, saltScrypt, N, r, p, keyLenght);
		Assert.assertEquals(expectedScrypt, derivated);
		Equals(expectedScrypt, derivated, pd);
	}

	@Test
	public void testDefaultScrypt() {
		String derivated = pd.doGenerateDefaultSCrypt(password, saltScrypt);
		Assert.assertEquals(expectedScrypt, derivated);
		Equals(expectedScrypt, derivated, pd);
	}

	@Test
	public void testBcrypt() {
		String derivated = pd.doGenerateBcrypt(password, saltBcrypt, cost);
		Assert.assertEquals(expectedBcrypt, derivated);
		Equals(expectedBcrypt, derivated, pd);
	}

	@Test
	public void testDefaultBcrypt() {
		String derivated = pd.doGenerateDefaultBcrypt(password, saltBcrypt);
		Assert.assertEquals(expectedBcrypt, derivated);
		Equals(expectedBcrypt, derivated, pd);
	}


	@Test
	public void testArgon2() {
		int i = 0;
		for (String version : argon2Version) {
			for (String hashType : argon2HashType) {
				String derivated = pd.doGenerateArgon2(version, hashType, iterations, memory, parallelism, password, saltArgon2, hashLength);
				Assert.assertEquals(argon2Results[i], derivated);
				Equals(argon2Results[i], derivated, pd);
				i++;
			}
		}

	}

}
