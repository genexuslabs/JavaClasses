package com.genexus.cryptography.checksum.utils;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

@SuppressWarnings("LoggingSimilarMessage")
public enum ChecksumInputType {

	BASE64, HEX, TXT, ASCII, LOCAL_FILE, NONE;

	private static final Logger logger = LogManager.getLogger(ChecksumInputType.class);

	public static ChecksumInputType getChecksumInputType(String checksumInputType, Error error) {
		if (error == null) return ChecksumInputType.NONE;
		if (checksumInputType == null) {
			error.setError("CHI06", "Unrecognized checksum input type");
			logger.error("Unrecognized checksum input type");
			return ChecksumInputType.NONE;
		}
		switch (checksumInputType.toUpperCase().trim()) {
			case "BASE64":
				return ChecksumInputType.BASE64;
			case "HEX":
				return ChecksumInputType.HEX;
			case "TXT":
				return ChecksumInputType.TXT;
			case "ASCII":
				return ChecksumInputType.ASCII;
			case "LOCAL_FILE":
				return ChecksumInputType.LOCAL_FILE;
			default:
				error.setError("CHI01", "Unrecognized checksum input type");
				logger.error("Unrecognized checksum input type");
				return null;
		}
	}

	public static String valueOf(ChecksumInputType checksumInputType, Error error) {
		if (error == null) return "";
		switch (checksumInputType) {
			case BASE64:
				return "BASE64";
			case HEX:
				return "HEX";
			case TXT:
				return "TXT";
			case ASCII:
				return "ASCII";
			case LOCAL_FILE:
				return "LOCAL_FILE";
			default:
				error.setError("CHI02", "Unrecognized checksum input type");
				logger.error("Unrecognized checksum input type");
				return "";
		}
	}

	public static byte[] getBytes(ChecksumInputType checksumInputType, String input, Error error) {
		if (error == null) return null;
		EncodingUtil eu = new EncodingUtil();
		try {
			switch (checksumInputType) {
				case BASE64:
					return org.bouncycastle.util.encoders.Base64.decode(input);
				case HEX:
					return SecurityUtils.hexaToByte(input, error);
				case TXT:
					return eu.getBytes(input);
				case ASCII:
					return input.getBytes(StandardCharsets.US_ASCII);
				case LOCAL_FILE:
					return SecurityUtils.getFileBytes(input, error);
				default:
					error.setError("CHII05", "Unrecognized checksum input type");
					logger.error("Unrecognized checksum input type");
					return null;
			}
		} catch (Exception e) {
			error.setError("CHI03", e.getMessage());
			logger.error("getBytes", e);
			return null;
		}
	}
}
