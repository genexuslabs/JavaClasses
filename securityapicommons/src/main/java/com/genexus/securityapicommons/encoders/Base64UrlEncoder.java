package com.genexus.securityapicommons.encoders;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.config.EncodingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.UrlBase64;

import java.nio.charset.StandardCharsets;

public class Base64UrlEncoder extends SecurityAPIObject {

	private static final Logger logger = LogManager.getLogger(Base64UrlEncoder.class);

	public Base64UrlEncoder() {
		super();
	}

	public String toBase64(String text) {
		this.error.cleanError();
		logger.debug("toBase64");
		EncodingUtil eu = new EncodingUtil();
		byte[] textBytes = eu.getBytes(text);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		try {
			byte[] resultBytes = UrlBase64.encode(textBytes);
			return new String(resultBytes, StandardCharsets.UTF_8).replaceAll("[\ufffd]", "");
		} catch (Exception e) {
			this.error.setError("BS001", e.getMessage());
			logger.error("toBase64", e);
			return "";
		}
	}

	public String toPlainText(String base64Text) {
		this.error.cleanError();
		logger.debug("toPlainText");
		byte[] bytes;
		try {
			bytes = UrlBase64.decode(base64Text);
		} catch (Exception e) {
			this.error.setError("BS002", e.getMessage());
			logger.error("toPlainText", e);
			return "";
		}
		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(bytes);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		return result;
	}

	public String toStringHexa(String base64Text) {
		this.error.cleanError();
		logger.debug("toStringHexa");
		try {
			byte[] bytes = UrlBase64.decode(base64Text);
			return Hex.toHexString(bytes).toUpperCase();
		} catch (Exception e) {
			this.error.setError("BS003", e.getMessage());
			logger.error("toStringHexa", e);
			return "";
		}
	}

	public String fromStringHexaToBase64(String stringHexa) {
		this.error.cleanError();
		logger.debug("fromStringHexaToBase64");
		try {
			byte[] stringBytes = UrlBase64.encode(Hex.decode(stringHexa));
			return new String(stringBytes, StandardCharsets.UTF_8).replaceAll("[\ufffd]", "");
		} catch (Exception e) {
			this.error.setError("BS005", e.getMessage());
			logger.error("fromStringHexaToBase64", e);
			return "";
		}
	}

	public String base64ToBase64Url(String base64Text) {
		this.error.cleanError();
		logger.debug("base64ToBase64Url");
		try {
			byte[] b64bytes = UrlBase64.encode(Base64.decode(base64Text));
			return new String(b64bytes, StandardCharsets.UTF_8).replaceAll("[\ufffd]", "");
		} catch (Exception e) {
			this.error.setError("BS007", e.getMessage());
			logger.error("base64ToBase64Url", e);
			return "";
		}
	}

	public String base64UrlToBase64(String base64UrlText) {
		this.error.cleanError();
		logger.debug("base64UrlToBase64");
		try {
			byte[] b64bytes = Base64.encode(UrlBase64.decode(base64UrlText));
			return new String(b64bytes, StandardCharsets.UTF_8).replaceAll("[\ufffd]", "");
		} catch (Exception e) {
			this.error.setError("BS008", e.getMessage());
			logger.error("base64UrlToBase64", e);
			return "";
		}
	}
}
