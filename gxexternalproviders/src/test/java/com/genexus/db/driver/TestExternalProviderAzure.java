package com.genexus.db.driver;


public class TestExternalProviderAzure extends TestExternalProvider {
	@Override
	public String getProviderName(){
		return ExternalProviderAzureStorage.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderAzureStorage();
	}

	@Override
	public boolean supportsObjectAcls() {
		return false;
	}
}
