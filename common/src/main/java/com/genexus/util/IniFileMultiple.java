package com.genexus.util;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import java.io.IOException;
import java.io.InputStream;

public class IniFileMultiple extends IniFile {
	public static final ILogger logger = LogManager.getLogger(IniFileMultiple.class);
	private IniFile additionalProperties;

	public IniFileMultiple(InputStream in) throws IOException {
		super(in);
	}

	public void addConfigurationSource(String configId, InputStream in) {
		if (in != null) {
			try {
				additionalProperties = new IniFile(in);
				logger.debug(String.format("Additional configuration file '%s' lodad", configId));
			} catch (Exception e) {
				logger.warn(String.format("Could not read additional configuration file: '%s'", configId), e);
			}
		}
	}

	@Override
	public String getProperty(String section, String key, String defaultValue) {
		String value = null;
		if (additionalProperties != null) {
			value = additionalProperties.getProperty(section, key);
		}
		if (value == null) {
			value = super.getProperty(section, key, defaultValue);
		}
		return value;
	}
}
