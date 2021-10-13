package com.genexus.db.driver;


public class TestExternalProviderS3 extends TestExternalProvider {

	@Override
	public String getProviderName(){
		return ExternalProviderS3.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderS3();
	}

}
