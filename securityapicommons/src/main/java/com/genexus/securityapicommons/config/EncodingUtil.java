package com.genexus.securityapicommons.config;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.bouncycastle.util.encoders.Hex;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.utils.SecurityUtils;

public class EncodingUtil extends SecurityAPIObject {

	/**
	 * EncodingUtil class constructor
	 */
	public EncodingUtil() {
		super();
	}

	public void setEncoding(String enc) {
		if (AvailableEncoding.existsEncoding(enc)) {
			Global.setGlobalEncoding(enc);
		} else {
			this.error.setError("EU003", "set encoding error");
		}
	}

	/**
	 * @param inputText
	 *            String UTF-8 text
	 * @return byte array representation of the String UTF-8 input text
	 */
	public byte[] getBytes(String inputText) {
		byte[] output = null;
		String encoding = Global.getGlobalEncoding();
		AvailableEncoding aEncoding = AvailableEncoding.getAvailableEncoding(encoding, this.error);
		if (this.hasError()) {
			return null;
		}
		String encodingString = AvailableEncoding.valueOf(aEncoding);
		try {
			output = inputText.trim().getBytes(encodingString);
		} catch (UnsupportedEncodingException e) {
			this.error.setError("EU001", e.getMessage());
			return null;
		}

		this.error.cleanError();
		return output;
	}

	/**
	 * @param bytes
	 *            byte array representation of a String UTF-8 text
	 * @return String UTF-8 text
	 */
	public String getString(byte[] bytes) {
		String res = null;
		String encoding = Global.getGlobalEncoding();

		AvailableEncoding aEncoding = AvailableEncoding.getAvailableEncoding(encoding, this.error);
		if (this.hasError()) {
			return "";
		}
		String encodingString = AvailableEncoding.valueOf(aEncoding);
		try {
			res = new String(bytes, encodingString).replaceAll("[\ufffd]", "");
		} catch (UnsupportedEncodingException e) {
			this.error.setError("EU002", e.getMessage());
			return "";
		}
		this.error.cleanError();
		return res.trim();
	}


}
