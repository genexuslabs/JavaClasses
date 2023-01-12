package com.genexus.db.driver;


public class TestExternalProviderIBM extends TestExternalProvider {

	@Override
	public String getProviderName(){
		return ExternalProviderIBM.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderIBM();
	}

}
