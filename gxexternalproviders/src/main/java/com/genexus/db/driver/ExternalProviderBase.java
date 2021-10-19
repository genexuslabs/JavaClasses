package com.genexus.db.driver;

import com.genexus.services.ServiceHelper;
import com.genexus.util.Encryption;
import com.genexus.util.GXService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ExternalProviderBase {
	private static Logger logger = LogManager.getLogger(ExternalProviderBase.class);
	static final String SERVICE_TYPE_NAME = "STORAGE";
	static final String DEFAULT_ACL = "DEFAULT_ACL";
	static final String DEFAULT_EXPIRATION = "DEFAULT_EXPIRATION";
	static final String FOLDER = "FOLDER_NAME";

	private ServiceHelper sHelper;

	@Deprecated
	static final String DEFAULT_ACL_DEPRECATED = "STORAGE_PROVIDER_DEFAULT_ACL";
	@Deprecated
	static final String DEFAULT_EXPIRATION_DEPRECATED = "STORAGE_PROVIDER_DEFAULT_EXPIRATION";

	static final int DEFAULT_EXPIRATION_MINUTES = 24 * 60;
	ResourceAccessControlList defaultAcl = ResourceAccessControlList.PublicRead;

	public ExternalProviderBase(GXService s, String name) {
		sHelper = new ServiceHelper(s, name, SERVICE_TYPE_NAME);
		init();
	}

	private void init() {
		String aclS = getPropertyValue(DEFAULT_ACL, DEFAULT_ACL_DEPRECATED, "");
		if (aclS.length() > 0) {
			this.defaultAcl = ResourceAccessControlList.parse(aclS);
		}
	}

	public String getEncryptedPropertyValue(String propertyName, String alternativePropertyName) throws Exception {
		return sHelper.getEncryptedPropertyValue(propertyName, alternativePropertyName);
	}

	public String getEncryptedPropertyValue(String propertyName, String alternativePropertyName, String defaultValue) {
		return sHelper.getEncryptedPropertyValue(propertyName, alternativePropertyName, defaultValue);
	}

	public String getPropertyValue(String propertyName, String alternativePropertyName) throws Exception{
		return sHelper.getPropertyValue(propertyName, alternativePropertyName);
	}

	public String getPropertyValue(String propertyName, String alternativePropertyName, String defaultValue) {
		return sHelper.getPropertyValue(propertyName, alternativePropertyName, defaultValue);
	}

}
