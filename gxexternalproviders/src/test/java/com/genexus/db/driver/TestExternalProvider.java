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

	private static String TEST_SAMPLE_FILE_NAME = "text.txt";
	private static String TEST_SAMPLE_FILE_PATH = Paths.get("resources", TEST_SAMPLE_FILE_NAME).toString();

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
		String upload = provider.upload(TEST_SAMPLE_FILE_PATH, TEST_SAMPLE_FILE_NAME, ResourceAccessControlList.PublicRead);
		ensureUrl(upload, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testUploadDefaultMethod(){
		String upload = provider.upload(TEST_SAMPLE_FILE_PATH, TEST_SAMPLE_FILE_NAME, ResourceAccessControlList.Default);
		ensureUrl(upload, ResourceAccessControlList.Default);
	}

	@Test
	public void testUploadAndCopyDefault(){
		testUploadAndCopyByAcl(ResourceAccessControlList.Default, ResourceAccessControlList.Default);
	}

	@Test
	public void testUploadAndCopyPrivate(){
		testUploadAndCopyByAcl(ResourceAccessControlList.Private, ResourceAccessControlList.Private);
	}

	@Test
	public void testUploadAndCopyPublic(){
		testUploadAndCopyByAcl(ResourceAccessControlList.PublicRead, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testUploadAndCopyMixed(){
		testUploadAndCopyByAcl(ResourceAccessControlList.Default, ResourceAccessControlList.Private);
	}

	@Test
	public void testUploadAndCopyPrivateToPublic(){
		testUploadAndCopyByAcl(ResourceAccessControlList.Private, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testUploadAndCopyPublicToPrivate(){
		testUploadAndCopyByAcl(ResourceAccessControlList.PublicRead, ResourceAccessControlList.Private);
	}


	public void testUploadAndCopyByAcl(ResourceAccessControlList aclUpload, ResourceAccessControlList aclCopy){
		String copyFileName = "test-upload-and-copy.txt";
		deleteSafe(TEST_SAMPLE_FILE_PATH);
		deleteSafe(copyFileName);
		String upload = provider.upload(TEST_SAMPLE_FILE_PATH, TEST_SAMPLE_FILE_NAME, aclUpload);
		assertTrue("Not found URL: " + upload, urlExists(upload));

		String copyUrl = tryGet(copyFileName, aclCopy);
		assertFalse("URL cannot exist: " + copyUrl, urlExists(copyUrl));

		provider.copy(TEST_SAMPLE_FILE_NAME, copyFileName, aclCopy);
		upload = provider.get(copyFileName, aclCopy, 100);
		ensureUrl(upload, aclCopy);
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
		provider.upload(TEST_SAMPLE_FILE_PATH, TEST_SAMPLE_FILE_NAME, acl);
		String upload = provider.get(TEST_SAMPLE_FILE_NAME, acl, 100);
		ensureUrl(upload, acl);

		deleteSafe(copyFileName);
		wait(1000); //Google CDN replication seems to be delayed.

		String urlCopy = tryGet(copyFileName, ResourceAccessControlList.PublicRead);
		assertFalse("URL cannot exist: " + urlCopy, urlExists(urlCopy));

		provider.copy("text.txt", copyFileName, acl);
		upload = provider.get(copyFileName, acl, 100);
		ensureUrl(upload, acl);
	}


	private void wait(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (Exception e) {}

	}
	private String getSafe(String objectName, ResourceAccessControlList acl) {
		try {
			return provider.get(objectName, acl, 100);
		}
		catch (Exception e)
		{

		}
		return "";
	}

	private boolean deleteSafe(String objectName) {
		try {
			provider.delete(objectName, ResourceAccessControlList.Private);
		}
		catch (Exception e) {}
		try {
			provider.delete(objectName, ResourceAccessControlList.PublicRead);
		}
		catch (Exception e) {}

		return true;
	}

	@Test
	public void testMultimediaUpload() {
		String copyFileName = "copy-text-private.txt";
		ResourceAccessControlList acl = ResourceAccessControlList.Private;

		provider.upload(TEST_SAMPLE_FILE_PATH, TEST_SAMPLE_FILE_NAME, acl);
		String upload = provider.get(TEST_SAMPLE_FILE_NAME, acl, 100);
		ensureUrl(upload, acl);

		deleteSafe(copyFileName);
		provider.copy(TEST_SAMPLE_FILE_NAME, copyFileName, acl);
		upload = tryGet(copyFileName, acl);
		ensureUrl(upload, acl);
	}

	@Test
	public void testGetMethod(){
		testUploadPublicMethod();
		String url = provider.get("text.txt", ResourceAccessControlList.PublicRead, 10);
		ensureUrl(url, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testGetObjectName(){
		testUploadPublicMethod();
		String url = provider.get(TEST_SAMPLE_FILE_NAME, ResourceAccessControlList.PublicRead, 10);
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
		provider.download(TEST_SAMPLE_FILE_NAME, filePath, ResourceAccessControlList.PublicRead);
		assertTrue(f.exists());
	}

	@Test
	public void testDeleteFile(){
		testUploadPublicMethod();
		String url = tryGet(TEST_SAMPLE_FILE_NAME, ResourceAccessControlList.PublicRead);
		ensureUrl(url, ResourceAccessControlList.PublicRead);
		provider.delete(TEST_SAMPLE_FILE_NAME, ResourceAccessControlList.PublicRead);

		url = tryGet(TEST_SAMPLE_FILE_NAME, ResourceAccessControlList.PublicRead);
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
		String externalFileName = "text-private-2.txt";

		deleteSafe(externalFileName);
		String signedUrl = provider.upload(TEST_SAMPLE_FILE_PATH, externalFileName, ResourceAccessControlList.Private);
		ensureUrl(signedUrl, ResourceAccessControlList.Private);
		signedUrl = provider.get(externalFileName, ResourceAccessControlList.Private, 10);
		ensureUrl(signedUrl, ResourceAccessControlList.Private);

	}

	private void ensureUrl(String signedOrUnsignedUrl, ResourceAccessControlList acl) {
		assertTrue("Resource not found: " + signedOrUnsignedUrl, urlExists(signedOrUnsignedUrl));
		if (acl == ResourceAccessControlList.Private) {
			String noSignedUrl = signedOrUnsignedUrl.substring(0, signedOrUnsignedUrl.indexOf("?") + 1);
			assertFalse("Resource must be private: " + noSignedUrl, urlExists(noSignedUrl));
		}
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
