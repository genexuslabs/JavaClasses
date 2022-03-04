package com.genexus.db.driver;

import com.genexus.specific.java.Connect;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public abstract class TestExternalProvider {
	protected ExternalProvider provider;

	public abstract String getProviderName();
	public abstract ExternalProvider getExternalProvider() throws Exception ;


	private String testSampleFileName;
	private String testSampleFilePath;
	private String uniqueId;


	public boolean supportsObjectAcls() {
		return true;
	}

	@Before
	public void beforeEachTestMethod() {

		Connect.init();

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


		uniqueId = Integer.toString(new Random().nextInt(5000));
		testSampleFileName = String.format("text-%s.txt", uniqueId);
		testSampleFilePath = Paths.get("resources", testSampleFileName).toString();

		try {
			List<String> lines = Arrays.asList("This is a Test File for Standard Classes Java");
			Path file = Paths.get(testSampleFilePath);
			Files.write(file, lines, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{

		}
	}


	@Test
	public void testUploadPublicMethod(){
		String upload = provider.upload(testSampleFilePath, testSampleFileName, ResourceAccessControlList.PublicRead);
		ensureUrl(upload, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testUploadDefaultMethod(){
		String upload = provider.upload(testSampleFilePath, testSampleFileName, ResourceAccessControlList.Default);
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
		Assume.assumeTrue(supportsObjectAcls());
		testUploadAndCopyByAcl(ResourceAccessControlList.Default, ResourceAccessControlList.Private);
	}

	@Test
	public void testUploadAndCopyPrivateToPublic(){
		Assume.assumeTrue(supportsObjectAcls());
		testUploadAndCopyByAcl(ResourceAccessControlList.Private, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testUploadAndCopyPublicToPrivate(){
		Assume.assumeTrue(supportsObjectAcls());
		testUploadAndCopyByAcl(ResourceAccessControlList.PublicRead, ResourceAccessControlList.Private);
	}


	public void testUploadAndCopyByAcl(ResourceAccessControlList aclUpload, ResourceAccessControlList aclCopy){
		String copyFileName = buildRandomTextFileName("test-upload-and-copy");
		deleteSafe(testSampleFileName);
		deleteSafe(copyFileName);
		String upload = provider.upload(testSampleFilePath, testSampleFileName, aclUpload);
		assertTrue("Not found URL: " + upload, urlExists(upload));

		String copyUrl = tryGet(copyFileName, aclCopy);
		assertFalse("URL cannot exist: " + copyUrl, urlExists(copyUrl));

		provider.copy(testSampleFileName, copyFileName, aclCopy);
		upload = provider.get(copyFileName, aclCopy, 100);
		ensureUrl(upload, aclCopy);
	}

	@Test
	public void testCopyMethod(){
		String copyFileName = buildRandomTextFileName("copy-text");
		copy(copyFileName, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testExistsDirectory(){
		String copyFileName = buildRandomTextFileName("tempFolder/f1/f2/test-upload-and-copy");
		String upload = provider.upload(testSampleFilePath, copyFileName, ResourceAccessControlList.Default);
		assertTrue("Not found URL: " + upload, urlExists(upload));
		String folder = "tempFolder";
		boolean existsDir = provider.existsDirectory(folder);
		assertTrue("Directory does not exists " + folder + " - " + upload, existsDir);
		String folderNotExists = "tempFolderNotExists";
		existsDir = provider.existsDirectory(folder);
		assertTrue("Directory does not exists " + folderNotExists, existsDir);
	}

	@Test
	public void testCopyPrivateMethod(){
		String copyFileName = buildRandomTextFileName("copy-text-private");
		copy(copyFileName, ResourceAccessControlList.Private);
	}

	private void copy(String copyFileName, ResourceAccessControlList acl) {
		provider.upload(testSampleFilePath, testSampleFileName, acl);
		String upload = provider.get(testSampleFileName, acl, 100);
		ensureUrl(upload, acl);

		deleteSafe(copyFileName);
		wait(1000); //Google CDN replication seems to be delayed.

		String urlCopy = tryGet(copyFileName, ResourceAccessControlList.PublicRead);
		assertFalse("URL cannot exist: " + urlCopy, urlExists(urlCopy));

		provider.copy(testSampleFileName, copyFileName, acl);
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
		String copyFileName = buildRandomTextFileName("copy-text-private");
		ResourceAccessControlList acl = ResourceAccessControlList.Private;

		provider.upload(testSampleFilePath, testSampleFileName, acl);
		String upload = provider.get(testSampleFileName, acl, 100);
		ensureUrl(upload, acl);

		deleteSafe(copyFileName);
		provider.copy(testSampleFileName, copyFileName, acl);
		upload = tryGet(copyFileName, acl);
		ensureUrl(upload, acl);
	}

	@Test
	public void testGetMethod(){
		testUploadPublicMethod();
		String url = provider.get(testSampleFileName, ResourceAccessControlList.PublicRead, 10);
		ensureUrl(url, ResourceAccessControlList.PublicRead);
	}

	@Test
	public void testGetObjectName(){
		testUploadPublicMethod();
		String url = provider.get(testSampleFileName, ResourceAccessControlList.PublicRead, 10);
		assertTrue(urlExists(url));
		String objectName = provider.getObjectNameFromURL(url);
		assertEquals(testSampleFileName, objectName);
	}

	@Test
	public void testDownloadMethod(){
		testUploadPublicMethod();

		String filePath = String.format("resources%stest%stext.txt", File.separator, File.separator);
		File f = new File(filePath);
		f.delete();
		assertFalse(f.exists());
		provider.download(testSampleFileName, filePath, ResourceAccessControlList.PublicRead);
		assertTrue(f.exists());
	}

	@Test
	public void testDeleteFile(){
		testUploadPublicMethod();
		String url = tryGet(testSampleFileName, ResourceAccessControlList.PublicRead);
		ensureUrl(url, ResourceAccessControlList.PublicRead);
		provider.delete(testSampleFileName, ResourceAccessControlList.PublicRead);

		url = tryGet(testSampleFileName, ResourceAccessControlList.PublicRead);
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
		String externalFileName = buildRandomTextFileName("text-private-2");

		deleteSafe(externalFileName);
		String signedUrl = provider.upload(testSampleFilePath, externalFileName, ResourceAccessControlList.Private);
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

	private String buildRandomTextFileName(String name)
	{
		return String.format("%s_%s.txt", name, uniqueId);
	}
}
