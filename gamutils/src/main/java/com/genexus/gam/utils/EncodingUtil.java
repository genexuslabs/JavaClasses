package com.genexus.gam.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.UrlBase64;

public class EncodingUtil {

	private static final Logger logger = LogManager.getLogger(EncodingUtil.class);

	public static String b64ToB64Url(String input) {
		logger.debug("b64ToB64Url");
		try {
			return new String(UrlBase64.encode(Base64.decode(input)), "UTF-8").replaceAll("[\ufffd]", "");
		} catch (Exception e) {
			logger.error("b64ToB64Url", e);
			return "";
		}
	}
}
