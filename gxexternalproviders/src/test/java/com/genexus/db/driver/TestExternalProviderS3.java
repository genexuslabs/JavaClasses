package com.genexus.db.driver;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestExternalProviderS3 {
	private ExternalProvider provider;
	private String endpoint;
	private String bucketName;
	private static String PROVIDER_NAME = "S3";

	private ExternalProvider getExternalProvider() {
		try {
			return new ExternalProviderS3();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Before
	public void beforeEachTestMethod() {

		boolean testEnabled = false;
		try {
			testEnabled = ExternalProviderHelper.getEnvironmentVariable(PROVIDER_NAME + "_TEST_ENABLED", false).equals("true");
		}
		catch (Exception e) {

		}
		assumeTrue(testEnabled);
		try {
			provider = getExternalProvider();
		} catch (Exception e) {
		}
		assertTrue(provider != null);

		java.io.BufferedReader in = null;
		int statusCode = 0;
		String response = "";
		try {

			java.net.URL url = new java.net.URL("https://api.github.com/users/google");
			java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				response += inputLine;
			}
			statusCode = con.getResponseCode();
			in.close();

		}
		catch (Exception e) {

		}
		finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {}
			}
		}

	}


	@Test
	public void testUploadPublicMethod(){
		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String upload = provider.upload(relativePath, "text.txt", ResourceAccessControlList.PublicRead);

		assertTrue("Not found URL: " + upload, urlExists(upload));
		assertFalse(urlExists(String.format("https://%s/%s/text123.txt", endpoint, bucketName)));
	}

	@Test
	public void testUploadDefaultMethod(){
		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String upload = provider.upload(relativePath, "text.txt", ResourceAccessControlList.Default);

		assertTrue("Not found URL: " + upload, urlExists(upload));
		assertFalse(urlExists(String.format("https://%s/%s/text123.txt", endpoint, bucketName)));
	}

	@Test
	public void testCopyMethod(){
		String copyFileName = "copy-text.txt";
		copy(copyFileName, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testCopyPrivateMethod(){
		String copyFileName = "copy-text-private.txt";
		copy(copyFileName, ResourceAccessControlList.Private);
	}

	private void copy(String copyFileName, ResourceAccessControlList acl) {
		String fileName = "text.txt";
		Path path = Paths.get("resources", fileName);
		String relativePath = path.toString();
		provider.upload(relativePath, fileName, acl);
		String upload = provider.get(fileName, acl, 100);
		assertTrue(urlExists(upload));

		provider.delete(copyFileName, acl);
		assertFalse(urlExists(String.format("https://%s/%s/%s", endpoint, bucketName, copyFileName)));
		provider.copy("text.txt", copyFileName, acl);
		upload = provider.get(copyFileName, acl, 100);
		assertTrue(urlExists(upload));
	}

	@Test
	public void testMultimediaUpload() {
		String copyFileName = "copy-text-private.txt";
		ResourceAccessControlList acl = ResourceAccessControlList.Private;

		String fileName = "text.txt";
		Path path = Paths.get("resources", fileName);
		String relativePath = path.toString();
		provider.upload(relativePath, fileName, acl);
		String upload = provider.get(fileName, acl, 100);
		assertTrue(urlExists(upload));

		provider.delete(copyFileName, acl);
		assertFalse(urlExists(String.format("https://%s/%s/%s", endpoint, bucketName, copyFileName)));
		provider.copy("text.txt", copyFileName, acl);
		upload = provider.get(copyFileName, acl, 100);
		assertTrue(urlExists(upload));
	}

	@Test
	public void testGetMethod(){
		testUploadPublicMethod();
		String url = provider.get("text.txt", ResourceAccessControlList.Private, 10);
		assertTrue(urlExists(url));
	}

	@Test
	public void testDownloadMethod(){
		testUploadPublicMethod();

		String filePath = String.format("resources%stest%stext.txt", File.separator, File.separator);
		File f = new File(filePath);
		f.delete();
		assertFalse(f.exists());
		provider.download("text.txt", filePath, ResourceAccessControlList.PublicRead);
		assertTrue(f.exists());
	}

	@Test
	public void testDeleteFile(){
		testUploadPublicMethod();
		provider.delete("text.txt", ResourceAccessControlList.PublicRead);
		assertFalse(urlExists(String.format("https://%s/%s/text.txt", endpoint, bucketName)));
	}

	@Test
	public void testUploadPrivateMethod(){
		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String externalFileName = "text-private.txt";

		provider.upload(relativePath, externalFileName, ResourceAccessControlList.Private);

		String upload = provider.get(externalFileName, ResourceAccessControlList.Private, 10);
		assertTrue(urlExists(upload));
		assertFalse(urlExists(upload.substring(0, upload.indexOf("?") + 1)));
		assertFalse(urlExists(String.format("https://%s/%s/%s", endpoint, bucketName, externalFileName)));
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
