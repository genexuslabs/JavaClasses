package com.genexus.db.driver;


public class TestExternalProviderOracle extends TestExternalProvider {

	@Override
	public String getProviderName(){
		return ExternalProviderS3.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderS3();
	}

}
