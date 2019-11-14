package com.genexus.db.driver;

import org.junit.Test;

public class TestExternalProviderIBM {

	@Test
	public void testUpload(){

		//(String api_key, String service_instance_id,  String bucketName, String folderName, String location)
		String apikey = "";
		String service_instance_id = "";
		String bucketName = "";
		String folderName = "";
		String location = "";

		ExternalProviderIBM p = new ExternalProviderIBM(apikey, service_instance_id, bucketName, folderName, location);
	}
}
