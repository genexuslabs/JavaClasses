package com.genexus.cryptography.checksum.utils;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;

public enum ChecksumInputType {

	BASE64, HEX, TXT, ASCII, LOCAL_FILE, NONE;

	public static ChecksumInputType getChecksumInputType(String checksumInputType, Error error) {
		if(error == null) return ChecksumInputType.NONE;
		if (checksumInputType == null)
		{
			error.setError("CHI06", "Unrecognized checksum input type");
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
				return "";
		}
	}

	public static byte[] getBytes(ChecksumInputType checksumInputType, String input, Error error) {
		if (error == null) return null;
		EncodingUtil eu = new EncodingUtil();
		byte[] aux = null;
		switch (checksumInputType) {
			case BASE64:
				try {
					aux = org.bouncycastle.util.encoders.Base64.decode(input);
				} catch (Exception e) {
					error.setError("CHI03", e.getMessage());
				}
				break;
			case HEX:
				aux = SecurityUtils.hexaToByte(input, error);
				break;
			case TXT:
				aux = eu.getBytes(input);
				if (eu.hasError()) {
					error = eu.getError();
				}
				break;
			case ASCII:
				try {
					aux = input.getBytes("US-ASCII");
				} catch (Exception e) {
					error.setError("CHI04", e.getMessage());
				}
				break;
			case LOCAL_FILE:
				aux = SecurityUtils.getFileBytes(input, error);
				break;
			default:
				error.setError("CHII05", "Unrecognized checksum input type");
				break;
		}
		return aux;
	}
}
