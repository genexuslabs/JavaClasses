package com.genexus.gam.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;

public class Random {

	private static Logger logger = LogManager.getLogger(Random.class);

	private static SecureRandom instanceRandom() {
		try {
			return new SecureRandom();
		} catch (Exception e) {
			logger.error("instanceRandom", e);
			return null;
		}
	}

	public static String randomAlphanumeric(int length) {
		SecureRandom random = instanceRandom();
		if (random == null) {
			logger.error("randomAlphanumeric SecureRandom is null");
			return "";
		}
		String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++)
			sb.append(characters.charAt(random.nextInt(characters.length())));
		return sb.toString();
	}

	public static String randomNumeric(int length) {
		SecureRandom random = instanceRandom();
		if (random == null) {
			logger.error("randomNumeric SecureRandom is null");
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}
}
