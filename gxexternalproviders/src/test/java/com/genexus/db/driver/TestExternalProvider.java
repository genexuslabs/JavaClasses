package com.genexus.db.driver;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public abstract class TestExternalProvider {
	protected ExternalProvider provider;

	public abstract String getProviderName();
	public abstract ExternalProvider getExternalProvider() throws Exception ;

	@Before
	public void beforeEachTestMethod() {

		boolean testEnabled = false;
		try {
			testEnabled = ExternalProviderHelper.getEnvironmentVariable(getProviderName() + "_TEST_ENABLED", false).equals("true");
		}
		catch (Exception e) {

		}
		assumeTrue(testEnabled);
		try {
			provider = getExternalProvider();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(provider != null);
	}


	@Test
	public void testUploadPublicMethod(){
		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String upload = provider.upload(relativePath, "text.txt", ResourceAccessControlList.PublicRead);

		assertTrue("Not found URL: " + upload, urlExists(upload));
	}

	@Test
	public void testUploadDefaultMethod(){
		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String upload = provider.upload(relativePath, "text.txt", ResourceAccessControlList.Default);

		assertTrue("Not found URL: " + upload, urlExists(upload));
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

		provider.get(copyFileName, acl, 100);
		provider.delete(copyFileName, acl);
		assertFalse(urlExists(tryGet(copyFileName, acl)));

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
		provider.copy("text.txt", copyFileName, acl);
		upload = tryGet(copyFileName, acl);
		assertTrue(urlExists(upload));
	}

	@Test
	public void testGetMethod(){
		testUploadPublicMethod();
		String url = provider.get("text.txt", ResourceAccessControlList.PublicRead, 10);
		assertTrue(urlExists(url));
	}

	@Test
	public void testGetObjectName(){
		testUploadPublicMethod();
		String url = provider.get("text.txt", ResourceAccessControlList.PublicRead, 10);
		assertTrue(urlExists(url));
		String objectName = provider.getObjectNameFromURL(url);
		assertEquals("text.txt", objectName);
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
		String url = tryGet("text.txt", ResourceAccessControlList.PublicRead);
		assertTrue(urlExists(url));
		provider.delete("text.txt", ResourceAccessControlList.PublicRead);

		url = tryGet("text.txt", ResourceAccessControlList.PublicRead);
		assertFalse(urlExists(url));
	}

	private String tryGet(String objectName, ResourceAccessControlList acl) {
		String getValue = "";
		try {
			getValue = provider.get(objectName, acl, 5);
		}
		catch (Exception e) {

		}
		return getValue;
	}

	@Test
	public void testUploadPrivateMethod(){
		Path path = Paths.get("resources", "text.txt");
		String relativePath = path.toString();
		String externalFileName = "text-private2.txt";

		String signedUrl = provider.upload(relativePath, externalFileName, ResourceAccessControlList.Private);
		assertTrue(urlExists(signedUrl));

		signedUrl = provider.get(externalFileName, ResourceAccessControlList.Private, 10);
		assertTrue(urlExists(signedUrl));

		String noSignedUrl = signedUrl.substring(0, signedUrl.indexOf("?") + 1);
		assertFalse("Resource must be private: " + noSignedUrl, urlExists(noSignedUrl));

	}

	protected boolean urlExists(String httpUrl) {
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
