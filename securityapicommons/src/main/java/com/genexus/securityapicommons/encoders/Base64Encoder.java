package com.genexus.securityapicommons.encoders;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.config.EncodingUtil;

/**
 * @author sgrampone
 *
 */
public class Base64Encoder extends SecurityAPIObject {

	/**
	 * Base64Encoder class constructor
	 */
	public Base64Encoder() {
		super();
	}

	/**
	 * @param text
	 *            String UTF-8 plain text to encode
	 * @return Base64 String text encoded
	 */
	public String toBase64(String text) {
		this.error.cleanError();
		EncodingUtil eu = new EncodingUtil();
		byte[] textBytes = eu.getBytes(text);
		if(eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		String result = "";
		try {
			result =  Base64.toBase64String(textBytes);
		}catch(Exception e)
		{
			this.error.setError("BS001", e.getMessage());
			return "";
		}
		return result;
	}

	/**
	 * @param base64Text
	 *            String Base64 encoded
	 * @return String UTF-8 plain text from Base64
	 */
	public String toPlainText(String base64Text) {
		this.error.cleanError();
		byte[] bytes;
		try {
			bytes = Base64.decode(base64Text);
		}catch(Exception e)
		{
			this.error.setError("BS002", e.getMessage());
			return "";
		}
		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(bytes);
		if(eu.hasError())
		{
			this.error = eu.getError();
			return "";
		}
		return result;
	}

	/**
	 * @param base64Text
	 *            String Base64 encoded
	 * @return String Hexa representation of base64Text
	 */
	public String toStringHexa(String base64Text) {
		this.error.cleanError();
		byte[] bytes;
		try {
			bytes = Base64.decode(base64Text);
		}catch(Exception e)
		{
			this.error.setError("BS003", e.getMessage());
			return "";
		}
		String result = "";
		try {
			result = Hex.toHexString(bytes).toUpperCase();
		}catch(Exception e)
		{
			this.error.setError("BS004", e.getMessage());
			return "";
		}
		return result;
	}

	/**
	 * @param stringHexa
	 *            String Hexa
	 * @return String Base64 encoded of stringHexa
	 */
	public String fromStringHexaToBase64(String stringHexa) {
		this.error.cleanError();
		byte[] stringBytes;
		try {
			stringBytes = Hex.decode(stringHexa);
		}catch(Exception e)
		{
			this.error.setError("BS005", e.getMessage());
			return "";
		}
		String result = "";
		try {
			result = Base64.toBase64String(stringBytes);
		}catch(Exception e)
		{
			this.error.setError("BS006", e.getMessage());
			return "";
		}
		return result;
	}
}
