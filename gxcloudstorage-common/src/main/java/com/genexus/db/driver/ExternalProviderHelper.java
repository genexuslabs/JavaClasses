package com.genexus.db.driver;

import com.genexus.util.Encryption;
import com.genexus.util.GXService;

import java.io.*;
import org.apache.tika.Tika;

public class ExternalProviderHelper {

	public static final int BUFFER_MARK_LIMIT = 128 * 1024;

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
		InputStream bufferedInput = new BufferedInputStream(new FileInputStream(tempFile));
		bufferedInput.mark(BUFFER_MARK_LIMIT);
		Tika tika = new Tika();
		String detectedContentType = tika.detect(bufferedInput);
		bufferedInput.reset();
		return new InputStreamWithLength(bufferedInput, size, tempFile, detectedContentType);
	}

	public static class InputStreamWithLength implements AutoCloseable {
		public final InputStream inputStream;
		public final long contentLength;
		public final File tempFile; // nullable
		public final String detectedContentType;

		public InputStreamWithLength(InputStream inputStream, long contentLength, File tempFile, String detectedContentType) {
			this.inputStream = inputStream;
			this.contentLength = contentLength;
			this.tempFile = tempFile;
			this.detectedContentType = detectedContentType;
		}
		
		@Override
		public void close() throws IOException {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} finally {
				if (tempFile != null && tempFile.exists()) {
					tempFile.delete();
				}
			}
		}
	}
}
