package com.genexus.gam.utils.test.resources.securityapicommons.config;

import com.genexus.gam.utils.test.resources.securityapicommons.commons.Error;

public enum AvailableEncoding {
	UTF_8, UTF_16, UTF_16BE, UTF_16LE, UTF_32, UTF_32BE, UTF_32LE, SJIS, GB2312;

	public static AvailableEncoding getAvailableEncoding(String encoding, Error error) {
		encoding = encoding.replace("-", "_");
		encoding = encoding.toUpperCase();
		switch (encoding.trim()) {
			case "UTF_8":
				return UTF_8;
			case "UTF_16":
				return UTF_16;
			case "UTF_16BE":
				return UTF_16BE;
			case "UTF_16LE":
				return UTF_16LE;
			case "UTF_32":
				return UTF_32;
			case "UTF_32BE":
				return UTF_32BE;
			case "UTF_32LE":
				return UTF_32LE;
			case "SJIS":
				return SJIS;
			case "GB2312":
				return GB2312;
			default:
				error.setError("AE001", "Unknown encoding or not available");
				return null;
		}
	}

	public static boolean existsEncoding(String encoding) {
		encoding = encoding.replace("-", "_");
		encoding = encoding.toUpperCase();
		switch (encoding) {
			case "UTF_8":
			case "UTF_16":
			case "UTF_16BE":
			case "UTF_16LE":
			case "UTF_32":
			case "UTF_32BE":
			case "UTF_32LE":
			case "SJIS":
			case "GB2312":
				return true;
			default:
				return false;
		}
	}

	public static String valueOf(AvailableEncoding availableEncoding) {
		switch (availableEncoding) {
			case UTF_8:
				return "UTF-8";
			case UTF_16:
				return "UTF-16";
			case UTF_16BE:
				return "UTF-16BE";
			case UTF_16LE:
				return "UTF-16LE";
			case UTF_32:
				return "UTF-32";
			case UTF_32BE:
				return "UTF-32BE";
			case UTF_32LE:
				return "UTF-32LE";
			case SJIS:
				return "Shift_JIS";
			case GB2312:
				return "GB2312";
			default:
				return "";
		}
	}

	public static String hexaBloom(AvailableEncoding availableEncoding) {
		switch (availableEncoding) {

			case UTF_16:
			case UTF_16BE:
				return "fffffffffffffffd";
			case UTF_16LE:
				return "fffffffdffffffff";
			case UTF_32:
			case UTF_32BE:
				return "00fffffffffffffffd";
			case UTF_32LE:
				return "fffffffdffffffff00";
			default:
				return "";
		}
	}

}
