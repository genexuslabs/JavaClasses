package com.genexus.ftps.utils;

import com.genexus.securityapicommons.commons.Error;

public enum FtpEncryptionMode {

	IMPLICIT, EXPLICIT,;

	public static FtpEncryptionMode getFtpEncryptionMode(String ftpEncryptionMode, Error error) {
		switch (ftpEncryptionMode.toUpperCase().trim()) {
			case "IMPLICIT":
				return IMPLICIT;
			case "EXPLICIT":
				return EXPLICIT;
			default:
				error.setError("EM001", "Unknown encryption mode");
				return null;
		}
	}

	public static String valueOf(FtpEncryptionMode ftpEncryptionMode, Error error) {
		switch (ftpEncryptionMode) {
			case IMPLICIT:
				return "IMPLICIT";
			case EXPLICIT:
				return "EXPLICIT";
			default:
				error.setError("EM002", "Unknown encryption mode");
				return "";
		}
	}
}
