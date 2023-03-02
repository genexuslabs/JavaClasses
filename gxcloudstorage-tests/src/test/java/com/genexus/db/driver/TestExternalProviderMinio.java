package com.genexus.db.driver;


import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestExternalProviderMinio extends TestExternalProvider {

	@Override
	public String getProviderName(){
		return ExternalProviderS3.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderS3();
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

		//Minio seems to not support ACL by Object
		/*
		String noSignedUrl = signedUrl.substring(0, signedUrl.indexOf("?") + 1);
		assertFalse("Resource must be private: " + noSignedUrl, urlExists(noSignedUrl));
		*/
	}

	@Override
	public boolean supportsObjectAcls() {
		return false;
	}
}
