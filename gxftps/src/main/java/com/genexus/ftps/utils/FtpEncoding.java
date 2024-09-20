package com.genexus.ftps.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum FtpEncoding {

	BINARY, ASCII,
	;
	private static final Logger logger = LogManager.getLogger(FtpEncoding.class);

	public static FtpEncoding getFtpEncoding(String ftpEncoding, Error error) {
		switch (ftpEncoding.toUpperCase().trim()) {
			case "BINARY":
				return BINARY;
			case "ASCII":
				return ASCII;
			default:
				error.setError("FE001", "Unknown encoding");
				logger.error("Unknown encoding");
				return null;
		}
	}

	public static String valueOf(FtpEncoding ftpEncoding, Error error) {
		switch (ftpEncoding) {
			case BINARY:
				return "BINARY";
			case ASCII:
				return "ASCII";
			default:
				error.setError("FE002", "Unknown encoding");
				logger.error("Unknown encoding");
				return "";
		}
	}

}
