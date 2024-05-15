package com.genexus.securityapicommons.encoders;

import org.bouncycastle.util.encoders.Hex;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.config.EncodingUtil;

/**
 * @author sgrampone
 *
 */
public class HexaEncoder extends SecurityAPIObject {

	/**
	 * Hexa class contstructor
	 */
	public HexaEncoder() {
		super();
	}

	/**
	 * @param plainText
	 *            String UTF-8 plain text
	 * @return String Hexa hexadecimal representation of plainText
	 */
	public String toHexa(String plainText) {
		this.error.cleanError();
		EncodingUtil eu = new EncodingUtil();
		byte[] stringBytes = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		String hexa = "";
		try
		{
			hexa = Hex.toHexString(stringBytes, 0, stringBytes.length);
		}catch(Exception e)
		{
			this.error.setError("HE001", e.getMessage());
			return "";
		}
		return hexa.toUpperCase();
	}

	/**
	 * @param stringHexa
	 *            String hexadecimal representation of a text
	 * @return String UTF-8 plain text from stringHexa
	 */
	public String fromHexa(String stringHexa) {

		this.error.cleanError();
		byte[] resBytes;
		try
		{
			resBytes = Hex.decode(fixString(stringHexa));
		}catch(Exception e)
		{
			this.error.setError("HE002", e.getMessage());
			return "";
		}
		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(resBytes);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		return result;
	}


	public boolean isHexa(String input)
	{
		this.error.cleanError();
		try
		{
			Hex.decode(fixString(input));
		}catch(Exception e)
		{
			return false;
		}
		return true;
	}

	public static String fixString(String input)
	{
		if(!input.contains("-"))
		{
			return input;
		}else {
			String inputStr = input.replace("-", "");
			return inputStr;
		}
	}


}