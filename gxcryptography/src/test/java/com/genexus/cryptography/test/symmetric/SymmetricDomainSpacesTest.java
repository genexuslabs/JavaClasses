package com.genexus.cryptography.test.symmetric;

import com.genexus.cryptography.symmetric.SymmetricBlockCipher;
import com.genexus.cryptography.symmetric.SymmetricStreamCipher;
import com.genexus.securityapicommons.keys.SymmetricKeyGenerator;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class SymmetricDomainSpacesTest extends SecurityAPITestObject {

	private static String plainText;
	private static String key128;
	private static String IV128;
	private static SymmetricBlockCipher symBlockCipher;

	private static String plainTextStream;
	private static String key1024;
	private static SymmetricStreamCipher symStreamCipher;

	@BeforeClass
	public static void setUp() {
		plainText = "Neque porro quisquam est qui dolorem ipsum quia dolor sit amet";
		SymmetricKeyGenerator keyGen = new SymmetricKeyGenerator();

		key128 = keyGen.doGenerateKey(" GENERICRANDOM", 128);
		IV128 = keyGen.doGenerateIV("GENERICRANDOM ", 128);
		symBlockCipher = new SymmetricBlockCipher();

		plainTextStream = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam venenatis ex sit amet risus pellentesque, a faucibus quam ultrices. Ut tincidunt quam eu aliquam maximus. Quisque posuere risus at erat blandit eleifend. Curabitur viverra rutrum volutpat. Donec quis quam tellus. Aenean fermentum elementum augue, a semper risus scelerisque sit amet. Nullam vitae sapien vitae dui ullamcorper dapibus quis quis leo. Sed neque felis, pellentesque in risus et, lobortis ultricies nulla. Quisque quis quam risus. Donec vestibulum, lectus vel vestibulum eleifend, velit ante volutpat lacus, ut mattis quam ligula eget est. Sed et pulvinar lectus. In mollis turpis non ipsum vehicula, sit amet rutrum nibh dictum. Duis consectetur convallis ex, eu ultricies enim bibendum vel. Vestibulum vel libero nibh. Morbi nec odio mattis, vestibulum quam blandit, pretium orci.Aenean pellentesque tincidunt nunc a malesuada. Etiam gravida fermentum mi, at dignissim dui aliquam quis. Nullam vel lobortis libero. Phasellus non gravida posuere";
		key1024 = keyGen.doGenerateKey("GENERICRANDOM ", 1024);
		symStreamCipher = new SymmetricStreamCipher();
	}

	@Test
	public void testBlockDomains() {
		String encrypted = symBlockCipher.doEncrypt("AES ", "CBC ", "ZEROBYTEPADDING ", key128, IV128, plainText);
		assertFalse(symBlockCipher.hasError());
		String decrypted = symBlockCipher.doDecrypt(" AES", " CBC", " ZEROBYTEPADDING", key128, IV128, encrypted);
		assertFalse(symBlockCipher.hasError());
		assertTrue(SecurityUtils.compareStrings(decrypted, plainText));
	}

	@Test
	public void testStreamDomains() {
		String encrypted = symStreamCipher.doEncrypt("RC4 ", key1024, "", plainTextStream);
		assertFalse(symStreamCipher.hasError());
		String decrypted = symStreamCipher.doDecrypt(" RC4", key1024, "", encrypted);
		assertFalse(symStreamCipher.hasError());
		assertTrue(SecurityUtils.compareStrings(decrypted, plainTextStream));
	}

}
