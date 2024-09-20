package com.genexus.securityapicommons.encoders;

import com.genexus.securityapicommons.keys.CertificateX509;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.config.EncodingUtil;


public class Base64Encoder extends SecurityAPIObject {

	private static final Logger logger = LogManager.getLogger(Base64Encoder.class);

	public Base64Encoder() {
		super();
	}

	public String toBase64(String text) {
		logger.debug("toBase64");
		this.error.cleanError();
		this.error.cleanError();
		EncodingUtil eu = new EncodingUtil();
		byte[] textBytes = eu.getBytes(text);
		if(eu.hasError()) {
			this.error = eu.getError();
			return "";
		}

		try {
			return Base64.toBase64String(textBytes);
		}catch(Exception e)
		{
			this.error.setError("BS001", e.getMessage());
			logger.error("toBase64", e);
			return "";
		}
	}

	public String toPlainText(String base64Text) {
		this.error.cleanError();
		logger.debug("toPlainText");
		this.error.cleanError();
		byte[] bytes;
		try {
			bytes = Base64.decode(base64Text);
		}catch(Exception e)
		{
			this.error.setError("BS002", e.getMessage());
			logger.error("toPlainText", e);
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

	public String toStringHexa(String base64Text) {
		this.error.cleanError();
		logger.debug("toStringHexa");
		try {
			byte[] bytes = Base64.decode(base64Text);
			return Hex.toHexString(bytes).toUpperCase();
		}catch(Exception e)
		{
			this.error.setError("BS003", e.getMessage());
			logger.error("toStringHexa", e);
			return "";
		}
	}

	public String fromStringHexaToBase64(String stringHexa) {
		this.error.cleanError();
		logger.debug("fromStringHexaToBase64");
		try {
			byte[] stringBytes = Hex.decode(stringHexa);
			return Base64.toBase64String(stringBytes);
		}catch(Exception e)
		{
			this.error.setError("BS005", e.getMessage());
			logger.error("fromStringHexaToBase64", e);
			return "";
		}
	}
}
