package com.genexus.securityapicommons.utils;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.config.EncodingUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

	private static final Logger logger = LogManager.getLogger(SecurityUtils.class);

	public static boolean compareStrings(String one, String two) {
		if (one != null && two != null) {
			return one.compareToIgnoreCase(two) == 0;
		} else {
			return false;
		}

	}

	public static byte[] getFileBytes(String pathInput, Error error) {
		byte[] aux = null;
		try {
			File initialFile = new File(pathInput);
			aux = Files.readAllBytes(initialFile.toPath());
		} catch (Exception e) {
			error.setError("SU001", e.getMessage());
		}
		return aux;
	}

	public static InputStream getFileStream(String pathInput, Error error) {
		InputStream aux = null;
		try {
			aux = new FileInputStream(new File(pathInput));
		} catch (FileNotFoundException e) {
			error.setError("SU002", e.getMessage());
		}
		return aux;
	}

	public static boolean validateExtension(String path, String extension) {
		return extensionIs(path, extension);
	}

	public static String getFileExtension(String path) {
		int lastIndexOf = path.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return path.substring(lastIndexOf);
	}

	public static boolean extensionIs(String path, String ext) {
		return getFileExtension(path).compareToIgnoreCase(ext) == 0;
	}

	public static KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException {
		return compareStrings("ECDSA", algorithm) ? KeyFactory.getInstance("EC"): KeyFactory.getInstance("RSA");
	}

	private static final InputStream inputStringtoStream(String text) {
		return new ByteArrayInputStream(new EncodingUtil().getBytes(text));
	}

	public static final InputStream inputFileToStream(String path) throws IOException {
		final File initialFile = new File(path);
		final InputStream targetStream = new DataInputStream(new FileInputStream(initialFile));
		return targetStream;
	}

	public static byte[] hexaToByte(String hex, Error error) {
		byte[] output;
		try {
			output = Hex.decode(hex);
		} catch (Exception e) {
			error.setError("SU004", e.getMessage());
			return null;
		}
		return output;
	}

	public static InputStream stringToStream(String input, Error error) {
		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(input);
		if (eu.hasError()) {
			error = eu.getError();
			return null;
		} else {

			try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
				return inputStream;
			} catch (Exception e) {
				error.setError("SU003", e.getMessage());
			}
			return null;
		}
	}

	public static boolean validateStringInput(String classs, String method, String name, String value, Error error) {
		if (value.isEmpty()) {
			error.setError("SU006", String.format("Class: %1$s Method: %2$s - The parameter %3$s cannot be empty", classs, method, name));
			logger.error(String.format("Class: %1$s Method: %2$s - The parameter %3$s cannot be empty", classs, method, name));
			return false;
		}
		return true;
	}

	public static boolean validateObjectInput(String classs, String method, String name, Object value, Error error) {
		if (value == null) {
			error.setError("SU007", String.format("Class: %1$s Method: %2$s - The parameter %3$s cannot be empty", classs, method, name));
			logger.error(String.format("Class: %1$s Method: %2$s - The parameter %3$s cannot be empty", classs, method, name));
			return false;
		}
		return true;
	}
}
