package com.genexus.cryptography.test.passwordDerivation;

import com.genexus.cryptography.passwordDerivation.PasswordDerivation;
import com.genexus.securityapicommons.encoders.HexaEncoder;
import com.genexus.test.commons.SecurityAPITestObject;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PasswordDerivationDomainSpacesTest extends SecurityAPITestObject{

	protected static String password;
	protected static PasswordDerivation pd;

	/* ARGON2 */
	protected static int iterations;
	protected static int parallelism;
	protected static int memory;
	protected static int hashLength;
	protected static String saltArgon2;
	protected static String[] argon2Version;
	protected static String[] argon2HashType;
	protected static String[] argon2Results;

	@Override
	protected void setUp() {
		pd = new PasswordDerivation();
		password = "password";


		/* ARGON2 */
		iterations = 1;
		parallelism = 2;
		memory = 4;
		hashLength = 32;
		saltArgon2 = "14ae8da01afea8700c2358dcef7c5358d9021282bd88663a4562f59fb74d22ee";
		argon2Version = new String[] {"ARGON2_VERSION_10 ", " ARGON2_VERSION_13"};
		argon2HashType = new String[] {"ARGON2_d ", " ARGON2_i", "ARGON2_id"};
		argon2Results = new String[] {"f9hF4rzwC9AvfFMK8ZHvKoQeipc7OUQ/dBV4nBer57U=", "QuNCd8sy8STFTBeylfaUVWAN8w3PDl0L94rr9TqaK/g=", "El6fozCF2xSzdcrfR0QO8U1Zmh4OuRZPwufAvqXcLiY=", "xvlSYizqgM93gmi7cTDvkXda41QDj6fTaCC2cpltt3E=", "jDKqkLzOaxFQ2vHwB3/UQiSI2wO+2cDk6Y1VQwSXzz4=", "A8icyy1A7VlunnJKBZXJl/BkNmVQ5FlMznCNKS1YJCM="};


	}

	public static Test suite() {
		return new TestSuite(PasswordDerivationDomainSpacesTest.class);
	}

	@Override
	public void runTest() {
		testArgon2();
	}

	public void testArgon2() {
		int i=0;
		for (String version : argon2Version)
		{
			for(String hashType : argon2HashType)
			{
				String derivated = pd.doGenerateArgon2(version, hashType, iterations, memory, parallelism, password, saltArgon2, hashLength);
				assertEquals(argon2Results[i], derivated);
				Equals(argon2Results[i], derivated, pd);
				i++;
			}
		}

	}

}
