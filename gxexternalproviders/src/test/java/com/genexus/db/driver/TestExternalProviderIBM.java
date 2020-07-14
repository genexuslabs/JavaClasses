package com.genexus.db.driver;

import org.junit.Ignore;
import org.junit.Test;

import static com.genexus.db.driver.ResourceAccessControlList.Default;
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
		String upload = p.upload(relativePath, "text.txt", Default);

		assertTrue(urlExists(upload));
		assertFalse(urlExists(String.format("https://%s/%s/text123.txt", endpoint, bucketName)));
	}

	@Test
	@Ignore
	public void testCopy(){
		ExternalProviderIBM p = getExternalProviderIBM();
		String copyFileName = "copy-text.txt";
		copy(p, copyFileName, ResourceAccessControlList.PublicRead);
	}


	@Test
	@Ignore
	public void testCopyPrivate(){
		ExternalProviderIBM p = getExternalProviderIBM();
		String copyFileName = "copy-text-private.txt";
		copy(p, copyFileName, ResourceAccessControlList.Private);
	}

	private void copy(ExternalProviderIBM p, String copyFileName, ResourceAccessControlList acl) {
		String fileName = "text.txt";
		Path path = Paths.get("resources", fileName);
		String relativePath = path.toString();
		p.upload(relativePath, fileName, acl);
		String upload = p.get(fileName, acl, 100);
		assertTrue(urlExists(upload));

		p.delete(copyFileName, acl);
		assertFalse(urlExists(String.format("https://%s/%s/%s", endpoint, bucketName, copyFileName)));
		p.copy("text.txt", copyFileName, acl);
		upload = p.get(copyFileName, acl, 100);
		assertTrue(urlExists(upload));
	}

	@Test
	@Ignore
	public void multimediaUpload() {
		ExternalProviderIBM p = getExternalProviderIBM();
		String copyFileName = "copy-text-private.txt";
		ResourceAccessControlList acl = ResourceAccessControlList.Private;

		String fileName = "text.txt";
		Path path = Paths.get("resources", fileName);
		String relativePath = path.toString();
		p.upload(relativePath, fileName, acl);
		String upload = p.get(fileName, acl, 100);
		assertTrue(urlExists(upload));

		p.delete(copyFileName, acl);
		assertFalse(urlExists(String.format("https://%s/%s/%s", endpoint, bucketName, copyFileName)));
		p.copy("text.txt", copyFileName, acl);
		upload = p.get(copyFileName, acl, 100);
		assertTrue(urlExists(upload));
	}

	@Test
	@Ignore
	public void testGetFile(){
		testUpload();
		ExternalProviderIBM p = getExternalProviderIBM();
		String url = p.get("text.txt", ResourceAccessControlList.Private, 10);
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
		p.download("text.txt", filePath, ResourceAccessControlList.PublicRead);
		assertTrue(f.exists());
	}

	@Test
	@Ignore
	public void testDeleteFile(){
		ExternalProviderIBM p = getExternalProviderIBM();

		testUpload();
		p.delete("text.txt", ResourceAccessControlList.PublicRead);
		assertFalse(urlExists(String.format("https://%s/%s/text.txt", endpoint, bucketName)));
	}

	@Test
	@Ignore
	public void testUploadPrivateFile(){
		ExternalProviderIBM p = getExternalProviderIBM();

		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String externalFileName = "text-private.txt";

		p.upload(relativePath, externalFileName, ResourceAccessControlList.Private);

		String upload = p.get(externalFileName, ResourceAccessControlList.Private, 10);
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

		return new ExternalProviderIBM(accessKey, secretKey, bucket, folderName, location, endpoint, "");
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
