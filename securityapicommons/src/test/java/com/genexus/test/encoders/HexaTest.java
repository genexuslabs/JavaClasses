package com.genexus.test.encoders;

import com.genexus.securityapicommons.encoders.HexaEncoder;
import com.genexus.test.commons.SecurityAPITestObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class HexaTest extends SecurityAPITestObject {

	protected static String expected_plainText;
	protected static String expected_hexaText;
	protected static HexaEncoder hexa;


	@BeforeClass
	public static void setUp() {
		expected_plainText = "hello world";
		expected_hexaText = "68656C6C6F20776F726C64";
		hexa = new HexaEncoder();

	}

	@Test
	public void testFromHexa() {
		String plainText = hexa.fromHexa(expected_hexaText);
		Equals(expected_plainText, plainText, hexa);
	}

	@Test
	public void testToHexa() {
		String hexaText = hexa.toHexa(expected_plainText);
		Equals(expected_hexaText, hexaText, hexa);
	}

	@Test
	public void testIsHexa() {
		boolean isHexaTrue = hexa.isHexa(expected_hexaText);
		assertTrue(isHexaTrue);
		assertFalse(hexa.hasError());
		boolean isHexaFalse = hexa.isHexa(expected_plainText);
		assertFalse(isHexaFalse);
		assertFalse(hexa.hasError());
		boolean isHexaTrue_ = hexa.isHexa("68-65-6C-6C-6F-20-77-6F-72-6C-64");
		assertTrue(isHexaTrue_);
		assertFalse(hexa.hasError());
	}
}
