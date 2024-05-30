package com.genexus.ftps.utils;

import com.genexus.securityapicommons.commons.Error;

public enum FtpEncoding {

	BINARY, ASCII,;

	public static FtpEncoding getFtpEncoding(String ftpEncoding, Error error) {
		switch (ftpEncoding.toUpperCase().trim()) {
			case "BINARY":
				return BINARY;
			case "ASCII":
				return ASCII;
			default:
				error.setError("FE001", "Unknown encoding");
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
				return "";
		}
	}

}
