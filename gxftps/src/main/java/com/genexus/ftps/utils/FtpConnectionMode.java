package com.genexus.ftps.utils;

import com.genexus.securityapicommons.commons.Error;

public enum FtpConnectionMode {
	ACTIVE, PASSIVE,;

	public static FtpConnectionMode getFtpMode(String ftpMode, Error error) {
		switch (ftpMode.toUpperCase().trim()) {
			case "ACTIVE":
				return ACTIVE;
			case "PASSIVE":
				return PASSIVE;
			default:
				error.setError("FM001", "Unrecognized FtpMode");
				return null;
		}
	}

	public static String valueOf(FtpConnectionMode ftpMode, Error error)
	{
		switch(ftpMode)
		{
			case ACTIVE:
				return "ACTIVE";
			case PASSIVE:
				return "PASSIVE";
			default:
				error.setError("FM002", "Unrecognized FtpMode");
				return "";
		}
	}
}
