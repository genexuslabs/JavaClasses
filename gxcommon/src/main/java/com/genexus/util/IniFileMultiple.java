package com.genexus.util;

import java.io.*;

public class IniFileMultiple extends IniFile {
	private IniFile additionalProperties;

	public IniFileMultiple(InputStream in, InputStream in2) throws IOException {
		super(in);
		try {
			additionalProperties = new IniFile(in2);
		} catch (Exception e) { }
	}

	@Override
	public String getProperty(String section, String key, String defaultValue) {
		String value = null;
		if (additionalProperties != null){
			value = additionalProperties.getProperty(section, key);
		}
		if (value == null) {
			value = super.getProperty(section, key, defaultValue);
		}
		return value;
	}


}
