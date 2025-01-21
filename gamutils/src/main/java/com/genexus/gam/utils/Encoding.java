package com.genexus.gam.utils;


import com.nimbusds.jose.util.Base64URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.encoders.UrlBase64;

import java.nio.charset.StandardCharsets;

public class Encoding {

	private static final Logger logger = LogManager.getLogger(Encoding.class);

	public static String b64ToB64Url(String input) {
		logger.debug("b64ToB64Url");
		try {
			return new String(UrlBase64.encode(Base64.decode(input)), "UTF-8").replaceAll("[\ufffd]", "");
		} catch (Exception e) {
			logger.error("b64ToB64Url", e);
			return "";
		}
	}

	public static String hexaToBase64(String hexa)
	{
		logger.debug("hexaToBase64");
		try{
			return Base64.toBase64String(Hex.decode(hexa));
		}catch (Exception e)
		{
			logger.error("hexaToBase64", e);
			return "";
		}
	}

	public static String toBase64Url(String input)
	{
		logger.debug("UTF8toBase64Url");
		try{
			return new String(UrlBase64.encode(input.getBytes(StandardCharsets.UTF_8)));
		}catch (Exception e)
		{
			logger.error("UTF8toBase64Url", e);
			return "";
		}
	}

	public static String fromBase64Url(String base64Url)
	{
		logger.debug("fromBase64Url");
		try{
			return new String(UrlBase64.decode(base64Url), StandardCharsets.UTF_8);
		}catch (Exception e)
		{
			logger.error("fromBase64Url", e);
			return "";
		}
	}
}
