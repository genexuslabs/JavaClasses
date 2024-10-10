package com.genexus.ftps.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum FtpsProtocol {

	TLS1_0, TLS1_1, TLS1_2, SSLv2, SSLv3,
	;
	private static final Logger logger = LogManager.getLogger(FtpsProtocol.class);

	public static FtpsProtocol getFtpsProtocol(String ftpsProtocol, Error error) {
		switch (ftpsProtocol.trim()) {
			case "TLS1_0":
				return TLS1_0;
			case "TLS1_1":
				return TLS1_1;
			case "TLS1_2":
				return TLS1_2;
			case "SSLv2":
				return SSLv2;
			case "SSLv3":
				return SSLv3;
			default:
				error.setError("FP001", "Unknown protocol");
				logger.error("Unknown protocol");
				return null;

		}

	}

	public static String valueOf(FtpsProtocol ftpsProtocol, Error error) {
		switch (ftpsProtocol) {
			case TLS1_0:
				return "TLS1_0";
			case TLS1_1:
				return "TLS1_1";
			case TLS1_2:
				return "TLS1_2";
			case SSLv2:
				return "SSLv2";
			case SSLv3:
				return "SSLv3";
			default:
				error.setError("FP002", "Unknown protocol");
				logger.error("Unknown protocol");
				return "";
		}
	}
}
