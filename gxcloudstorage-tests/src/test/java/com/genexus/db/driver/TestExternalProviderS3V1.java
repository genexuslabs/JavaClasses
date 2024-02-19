package com.genexus.db.driver;

public class TestExternalProviderS3V1 extends TestExternalProvider {
	@Override
	public String getProviderName(){
		return ExternalProviderS3V1.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderS3V1();
	}

}
