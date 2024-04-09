package com.genexus.db.driver;
public class TestExternalProviderS3V2 extends TestExternalProvider {

	@Override
	public String getProviderName(){
		return ExternalProviderS3V2ACL.NAME;
	}

	@Override
	public ExternalProvider getExternalProvider() throws Exception {
		return new ExternalProviderS3V2ACL();
	}

}
