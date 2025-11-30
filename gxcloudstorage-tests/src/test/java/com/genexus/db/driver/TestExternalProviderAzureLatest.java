package com.genexus.db.driver;


public class TestExternalProviderAzureLatest extends TestExternalProvider {
	@Override
	public String getProviderName(){
		return ExternalProviderAzureStorage.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderAzureStorageLatest();
	}

	@Override
	public boolean supportsObjectAcls() {
		return false;
	}
}
