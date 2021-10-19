package com.genexus.services;

import com.genexus.util.Encryption;
import com.genexus.util.GXService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServiceHelper {
	private static Logger logger = LogManager.getLogger(ServiceHelper.class);
	private GXService service;
	private String serviceName;
	private String serviceTypeName;

	public ServiceHelper(GXService s, String sName, String sTypeName) {
		this.service = s;
		serviceName = sName;
		serviceTypeName = sTypeName;
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
		return String.format("%s_%s_%s", serviceTypeName, serviceName, propertyName);
	}

}