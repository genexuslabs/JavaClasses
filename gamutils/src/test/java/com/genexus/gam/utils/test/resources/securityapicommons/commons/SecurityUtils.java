package com.genexus.gam.utils.test.resources.securityapicommons.commons;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.util.encoders.Hex;

import com.genexus.gam.utils.test.resources.securityapicommons.config.EncodingUtil;

public class SecurityUtils {

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
		if (extensionIs(path, extension)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param path path to the file
	 * @return file extension
	 */
	public static String getFileExtension(String path) {

		int lastIndexOf = path.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return path.substring(lastIndexOf);
	}

	/**
	 * @param path path to the file
	 * @param ext  extension of the file
	 * @return true if the file has the extension
	 */
	public static boolean extensionIs(String path, String ext) {
		return getFileExtension(path).compareToIgnoreCase(ext) == 0;
	}

	public static KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException {
		KeyFactory kf = null;
		if (compareStrings("ECDSA", algorithm)) {
			kf = kf.getInstance("EC");
		} else {
			kf = kf.getInstance("RSA");
		}
		return kf;

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

	public static boolean validateStringInput(String name, String value, Error error)
	{
		if(value == null)
		{
			error.setError("SU005", String.format("The parameter %s cannot be empty", name));
			return false;
		}
		if(value.isEmpty())
		{
			error.setError("SU006", String.format("The parameter %s cannot be empty", name));
			return false;
		}
		return true;
	}

	public static boolean validateObjectInput(String name, Object value, Error error)
	{
		if(value == null)
		{
			error.setError("SU007", String.format("The parameter %a cannot be empty", name));
			return false;
		}
		return true;
	}
}