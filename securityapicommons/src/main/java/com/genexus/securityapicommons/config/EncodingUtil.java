package com.genexus.securityapicommons.config;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EncodingUtil extends SecurityAPIObject {

	private static final Logger logger = LogManager.getLogger(EncodingUtil.class);

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

	public byte[] getBytes(String inputText) {
		logger.debug(String.format("getBytes - Encoding = %s ", Global.getGlobalEncoding()));
		AvailableEncoding encoding = AvailableEncoding.getAvailableEncoding(Global.getGlobalEncoding(), this.error);
		if (this.hasError()) {
			return null;
		}
		try {
			String encodingString = AvailableEncoding.valueOf(encoding);
			return inputText.trim().getBytes(encodingString);
		} catch (Exception e) {
			this.error.setError("EU001", e.getMessage());
			logger.error("getBytes", e);
			return null;
		}
	}

	public String getString(byte[] bytes) {
		logger.debug(String.format("getString - Encoding = %s ", Global.getGlobalEncoding()));
		AvailableEncoding encoding = AvailableEncoding.getAvailableEncoding(Global.getGlobalEncoding(), this.error);
		if (this.hasError()) {
			return "";
		}
		try {
			String encodingString = AvailableEncoding.valueOf(encoding);
			return new String(bytes, encodingString).replaceAll("[\ufffd]", "").trim();
		} catch (Exception e) {
			this.error.setError("EU002", e.getMessage());
			logger.error("getString", e);
			return "";
		}
	}


}
