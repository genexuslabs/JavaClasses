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
	public void testGetFile(){
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
		String apikey = "Mq4aI_Tx-ogBXDb1v10CmjuX2_j6odJJVejsqMyFDthd";
		String service_instance_id = "crn:v1:bluemix:public:cloud-object-storage:global:a/47b84451ab70b94737518f7640a9ee42:43fac307-dbff-4b09-8aef-9c788d4a16f8::";
		String bucket = bucketName;
		String folderName = "";
		String location = "sao01";

		return new ExternalProviderIBM(apikey, service_instance_id, bucket, folderName, location, endpoint);
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
