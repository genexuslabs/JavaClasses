package com.genexus.db.driver;

import com.genexus.util.Encryption;
import com.genexus.util.GXService;

import java.io.*;

public class ExternalProviderHelper {

	public static String getServicePropertyValue(GXService s, String propName, boolean isSecure){
		String value = s.getProperties().get(propName);
		if (value != null){
			if (isSecure){
				value = Encryption.decrypt64(value);
			}
		}
		return value;
	}


	public static String getEnvironmentVariable(String name, boolean required) throws Exception{
		String value = System.getenv(name);
		if ((value == null || value.length() == 0) && required){
			throw new Exception("Parameter " + name + " is required");
		}
		return value;
	}

	public static InputStreamWithLength getInputStreamContentLength(InputStream input) throws IOException {
		File tempFile = File.createTempFile("upload-", ".tmp");
		try (OutputStream out = new FileOutputStream(tempFile)) {
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = input.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		}
		long size = tempFile.length();
		InputStream newInput = new FileInputStream(tempFile);
		return new InputStreamWithLength(newInput, size, tempFile);
	}

	public static class InputStreamWithLength {
		public final InputStream inputStream;
		public final long contentLength;
		public final File tempFile; // nullable

		public InputStreamWithLength(InputStream inputStream, long contentLength, File tempFile) {
			this.inputStream = inputStream;
			this.contentLength = contentLength;
			this.tempFile = tempFile;
		}
	}
}
