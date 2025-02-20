package com.genexus.securityapicommons.encoders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Hex;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.config.EncodingUtil;

public class HexaEncoder extends SecurityAPIObject {

	private static final Logger logger = LogManager.getLogger(HexaEncoder.class);

	public HexaEncoder() {
		super();
	}

	public String toHexa(String plainText) {
		this.error.cleanError();
		logger.debug("toHexa");
		EncodingUtil eu = new EncodingUtil();
		byte[] stringBytes = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		try {
			return Hex.toHexString(stringBytes, 0, stringBytes.length).toUpperCase();
		} catch (Exception e) {
			this.error.setError("HE001", e.getMessage());
			logger.error("toHexa", e);
			return "";
		}
	}

	public String fromHexa(String stringHexa) {
		this.error.cleanError();

		logger.debug("fromHexa");
		byte[] resBytes;
		try {
			resBytes = Hex.decode(fixString(stringHexa));
		} catch (Exception e) {
			this.error.setError("HE002", e.getMessage());
			logger.error("fromHexa", e);
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


	public boolean isHexa(String input) {
		this.error.cleanError();
		logger.debug("isHexa");
		try {
			Hex.decode(fixString(input));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String fixString(String input) {
		if (!input.contains("-")) {
			return input;
		} else {
			return input.replace("-", "");
		}
	}


}