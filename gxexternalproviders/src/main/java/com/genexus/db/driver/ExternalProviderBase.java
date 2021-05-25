package com.genexus.db.driver;

import com.genexus.util.Encryption;
import com.genexus.util.GXService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ExternalProviderBase {
	private static Logger logger = LogManager.getLogger(ExternalProviderS3.class);
	private GXService service;

	abstract String getName();

	static final String DEFAULT_ACL = "DEFAULT_ACL";
	static final String DEFAULT_EXPIRATION = "DEFAULT_EXPIRATION";
	static final String FOLDER = "FOLDER_NAME";

	@Deprecated
	static final String DEFAULT_ACL_DEPRECATED = "STORAGE_PROVIDER_DEFAULT_ACL";
	@Deprecated
	static final String DEFAULT_EXPIRATION_DEPRECATED = "STORAGE_PROVIDER_DEFAULT_EXPIRATION";

	static final int DEFAULT_EXPIRATION_MINUTES = 24 * 60;
	 ResourceAccessControlList defaultAcl = ResourceAccessControlList.PublicRead;

	public ExternalProviderBase() {
		init();
	}


	public ExternalProviderBase(GXService s) {
		this.service = s;
		init();
	}

	private void init() {
		String aclS = getPropertyValue(DEFAULT_ACL, DEFAULT_ACL_DEPRECATED, "");
		if (aclS.length() > 0) {
			this.defaultAcl = ResourceAccessControlList.parse(aclS);
		}
	}

	public String getEncryptedPropertyValue(String propertyName, String alternativePropertyName) throws Exception {
		String value = getEncryptedPropertyValue(propertyName, alternativePropertyName, null);
		if (value == null) {
			String errorMessage = String.format("Service configuration error - Property name %s must be defined", resolvePropertyName(propertyName));
			logger.fatal(errorMessage);
			throw new Exception(errorMessage);
		}
		return value;
	}

	public String getEncryptedPropertyValue(String propertyName, String alternativePropertyName, String defaultValue) {
		String value = getPropertyValue(propertyName, alternativePropertyName, defaultValue);
		if (value != null && value.length() > 0) {
			try {
				value = Encryption.decrypt64(value);
			}
			catch (Exception e) {
				logger.warn("Could not decrypt property name: " + resolvePropertyName(propertyName));
			}
		}
		return value;
	}

	public String getPropertyValue(String propertyName, String alternativePropertyName) throws Exception{
		String value = getPropertyValue(propertyName, alternativePropertyName, null);
		if (value == null) {
			String errorMessage = String.format("Service configuration error - Property name %s must be defined", resolvePropertyName(propertyName));
			logger.fatal(errorMessage);
			throw new Exception(errorMessage);
		}
		return value;
	}

	public String getPropertyValue(String propertyName, String alternativePropertyName, String defaultValue) {
		propertyName = resolvePropertyName(propertyName);
		String value = System.getenv(propertyName);
		if (value == null || value.length() == 0){
			value = System.getenv(alternativePropertyName);
		}
		if (this.service != null) {
			value = this.service.getProperties().get(propertyName);
			if (value == null || value.length() == 0) {
				value = this.service.getProperties().get(alternativePropertyName);
			}
		}
		return value != null? value: defaultValue;
	}

	private String resolvePropertyName(String propertyName) {
		return String.format("STORAGE_%s_%s", getName(), propertyName);
	}

}
