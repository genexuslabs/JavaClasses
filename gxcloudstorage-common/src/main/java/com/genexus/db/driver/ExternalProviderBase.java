package com.genexus.db.driver;

import com.genexus.util.Encryption;
import com.genexus.util.GXService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ExternalProviderBase {
	private static Logger logger = LogManager.getLogger(ExternalProviderBase.class);
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


	/**
	 * Starts the necessary processes required for the usage of an External Object.
	 * This method ensures that the Provider is installed correctly before any
	 * operations are performed. It should be called at the beginning to set up
	 * the environment for the External Object.
	 */
	public void start() {
		
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
		String encryptedOrUnEncryptedValue = getPropertyValue(propertyName, alternativePropertyName, defaultValue);
		String decryptedValue = encryptedOrUnEncryptedValue;
		if (encryptedOrUnEncryptedValue != null && encryptedOrUnEncryptedValue.length() > 0) {
			try {
				String decryptedTemp = Encryption.tryDecrypt64(encryptedOrUnEncryptedValue);
				decryptedValue = (decryptedTemp != null) ? decryptedTemp: encryptedOrUnEncryptedValue;
			}
			catch (Exception e) {
				logger.warn("Could not decrypt property name: " + resolvePropertyName(propertyName));
			}
		}
		return decryptedValue;
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
		String value = readFromEnvVars(propertyName, alternativePropertyName);
		if (value != null) {
			return value;
		}
		String resolvedPtyName = resolvePropertyName(propertyName);
		if (service != null) {
			value = this.service.getProperties().get(resolvedPtyName);
			if (value == null || value.length() == 0) {
				value = this.service.getProperties().get(alternativePropertyName);
			}
		}
		return value != null? value: defaultValue;
	}

	private String readFromEnvVars(String propertyName, String alternativePropertyName) {
		if (service != null && !service.getAllowOverrideWithEnvVarSettings()){
			return null;
		}

		String value = System.getenv(resolvePropertyName(propertyName));
		if (value == null){
			value = System.getenv(alternativePropertyName);
		}
		return value;
	}

	private String resolvePropertyName(String propertyName) {
		return String.format("STORAGE_%s_%s", getName(), propertyName);
	}

}
