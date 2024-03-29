package com.genexus.services;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import com.genexus.util.Encryption;
import com.genexus.util.GXService;


public class ServiceSettingsReader {
	private static ILogger logger = LogManager.getLogger(ServiceSettingsReader.class);

	private GXService service;
	private String name;
	private String serviceTypeName;

	public ServiceSettingsReader(String serviceType, String instanceName, GXService service) {
		service = service;
		name = instanceName;
		serviceTypeName = serviceType;
	}

	public String getEncryptedPropertyValue(String propertyName, String alternativePropertyName) throws ServiceConfigurationException {
		String value = getEncryptedPropertyValue(propertyName, alternativePropertyName, null);
		if (value == null) {
			String errorMessage = String.format("Service configuration error - Property name %s must be defined", resolvePropertyName(propertyName));
			logger.fatal(errorMessage);
			throw new ServiceConfigurationException(errorMessage);
		}
		return value;
	}

	public String getEncryptedPropertyValue(String propertyName, String alternativePropertyName, String defaultValue) {
		String encryptedOrUnEncryptedValue = getPropertyValue(propertyName, alternativePropertyName, defaultValue);
		String decryptedValue = encryptedOrUnEncryptedValue;
		if (encryptedOrUnEncryptedValue != null && encryptedOrUnEncryptedValue.length() > 0) {
			try {
				String decryptedTemp = Encryption.tryDecrypt64(encryptedOrUnEncryptedValue);
				decryptedValue = (decryptedTemp != null) ? decryptedTemp : encryptedOrUnEncryptedValue;
			} catch (Exception e) {
				logger.warn("Could not decrypt property name: " + resolvePropertyName(propertyName));
			}
		}
		return decryptedValue;
	}

	public String getPropertyValue(String propertyName, String alternativePropertyName) throws ServiceConfigurationException {
		String value = getPropertyValue(propertyName, alternativePropertyName, null);
		if (value == null) {
			String errorMessage = String.format("Service configuration error - Property name %s must be defined", resolvePropertyName(propertyName));
			logger.fatal(errorMessage);
			throw new ServiceConfigurationException(errorMessage);
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
		return value != null ? value : defaultValue;
	}

	private String readFromEnvVars(String propertyName, String alternativePropertyName) {
		if (service != null && !service.getAllowOverrideWithEnvVarSettings()) {
			return null;
		}

		String value = System.getenv(resolvePropertyName(propertyName));
		if (value == null) {
			value = System.getenv(alternativePropertyName);
		}
		return value;
	}

	private String resolvePropertyName(String propertyName) {
		return String.format("%s_%s_%s", serviceTypeName, name, propertyName);
	}

	public String getName() {
		return this.name;
	}
}
