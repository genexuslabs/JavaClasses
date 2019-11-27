package com.genexus.db.driver;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestExternalProviderIBM {
	private static String bucketName = "svergara-ibm-gx-bucket";
	private static String endpoint = "s3.sao01.cloud-object-storage.appdomain.cloud";

	@Test
	@Ignore
	public void testUpload(){
		ExternalProviderIBM p = getExternalProviderIBM();

		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String upload = p.upload(relativePath, "text.txt", false);

		assertTrue(urlExists(upload));
		assertFalse(urlExists(String.format("https://%s/%s/text123.txt", endpoint, bucketName)));
	}

	@Test
	@Ignore
	public void testCopy(){
		ExternalProviderIBM p = getExternalProviderIBM();
		String copyFileName = "copy-text.txt";
		Boolean isPrivate = false;
		copy(p, copyFileName, isPrivate);
	}


	@Test
	@Ignore
	public void testCopyPrivate(){
		ExternalProviderIBM p = getExternalProviderIBM();
		String copyFileName = "copy-text-private.txt";
		Boolean isPrivate = true;
		copy(p, copyFileName, isPrivate);
	}

	private void copy(ExternalProviderIBM p, String copyFileName, Boolean isPrivate) {
		String fileName = "text.txt";
		Path path = Paths.get("resources", fileName);
		String relativePath = path.toString();
		p.upload(relativePath, fileName, isPrivate);
		String upload = p.get(fileName, isPrivate, 100);
		assertTrue(urlExists(upload));

		p.delete(copyFileName, isPrivate);
		assertFalse(urlExists(String.format("https://%s/%s/%s", endpoint, bucketName, copyFileName)));
		p.copy("text.txt", copyFileName, isPrivate);
		upload = p.get(copyFileName, isPrivate, 100);
		assertTrue(urlExists(upload));
	}

	@Test
	@Ignore
	public void multimediaUpload() {
		ExternalProviderIBM p = getExternalProviderIBM();
		String copyFileName = "copy-text-private.txt";
		Boolean isPrivate = true;

		String fileName = "text.txt";
		Path path = Paths.get("resources", fileName);
		String relativePath = path.toString();
		p.upload(relativePath, fileName, isPrivate);
		String upload = p.get(fileName, isPrivate, 100);
		assertTrue(urlExists(upload));

		p.delete(copyFileName, isPrivate);
		assertFalse(urlExists(String.format("https://%s/%s/%s", endpoint, bucketName, copyFileName)));
		p.copy("text.txt", copyFileName, false);
		upload = p.get(copyFileName, false, 100);
		assertTrue(urlExists(upload));
	}

	@Test
	@Ignore
	public void testGetFile(){
		testUpload();
		ExternalProviderIBM p = getExternalProviderIBM();
		String url = p.get("text.txt", false, 10);
		assertTrue(urlExists(url));
	}

	@Test
	@Ignore
	public void testDownloadFile(){
		ExternalProviderIBM p = getExternalProviderIBM();
		testUpload();

		String filePath = String.format("resources%stest%stext.txt", File.separator, File.separator);
		File f = new File(filePath);
		f.delete();
		assertFalse(f.exists());
		p.download("text.txt", filePath, false);
		assertTrue(f.exists());
	}

	@Test
	@Ignore
	public void testDeleteFile(){
		ExternalProviderIBM p = getExternalProviderIBM();

		testUpload();
		p.delete("text.txt", false);
		assertFalse(urlExists(String.format("https://%s/%s/text.txt", endpoint, bucketName)));
	}

	@Test
	@Ignore
	public void testUploadPrivateFile(){
		ExternalProviderIBM p = getExternalProviderIBM();

		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String externalFileName = "text-private.txt";

		p.upload(relativePath, externalFileName, true);

		String upload = p.get(externalFileName, true, 10);
		System.out.println(upload);
		assertTrue(urlExists(upload));

		assertFalse(urlExists(String.format("https://%s/%s/text123.txt", endpoint, bucketName)));
	}


	private ExternalProviderIBM getExternalProviderIBM() {
		String accessKey = "1c297a240f6b40c587caa9e923dc9d32";
		String secretKey = "6ade8a159ebef7a6413fdb1224e32e092f4d13f3aade1119";
		String bucket = bucketName;
		String folderName = "";
		String location = "sao01";

		return new ExternalProviderIBM(accessKey, secretKey, bucket, folderName, location, endpoint);
	}


	private boolean urlExists(String httpUrl) {
		try {
			URL url = new URL(httpUrl);
			InputStream is = url.openStream();
			is.close();
			return true;
		}
		catch(Exception e)
		{

		}
		return false;
	}
}
