package com.genexus.db.driver;


public class TestExternalProviderGoogleCloudStorage extends TestExternalProvider {
	@Override
	public String getProviderName(){
		return ExternalProviderGoogle.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderGoogle();
	}

}
