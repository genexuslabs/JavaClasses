package com.genexus.ftps.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum FtpConnectionMode {
	ACTIVE, PASSIVE,
	;

	private static final Logger logger = LogManager.getLogger(FtpConnectionMode.class);

	public static FtpConnectionMode getFtpMode(String ftpMode, Error error) {
		switch (ftpMode.toUpperCase().trim()) {
			case "ACTIVE":
				return ACTIVE;
			case "PASSIVE":
				return PASSIVE;
			default:
				error.setError("FM001", "Unrecognized FtpMode");
				logger.error("Unrecognized FtpMode");
				return null;
		}
	}

	public static String valueOf(FtpConnectionMode ftpMode, Error error) {
		switch (ftpMode) {
			case ACTIVE:
				return "ACTIVE";
			case PASSIVE:
				return "PASSIVE";
			default:
				error.setError("FM002", "Unrecognized FtpMode");
				logger.error("Unrecognized FtpMode");
				return "";
		}
	}
}
