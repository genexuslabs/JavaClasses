package com.genexus.db.driver;

import com.genexus.util.Encryption;
import com.genexus.util.GXService;

public class ExternalProviderHelper {

	public static String getServicePropertyValue(GXService s, String propName, boolean isSecure){
		String value = s.getProperties().get(propName);
		if (value != null){
			if (isSecure){
				value = Encryption.decrypt64(value);
			}
		}
		return value;
	}

	public static String getServicePropertyValue(GXService s, String propName, String propNameLegacy, boolean isSecure){
		String value = getServicePropertyValue(s, propName, isSecure);
		if (value == null || value.length() == 0)
		{
			value = getServicePropertyValue(s, propNameLegacy, isSecure);
		}
		return value;
	}

	public static String getServiceEncryptedPropertyValue(GXService s, String propName, String propNameLegacy){
		String value = getServicePropertyValue(s, propName, true);
		if (value == null || value.length() == 0)
		{
			value = getServicePropertyValue(s, propNameLegacy, true);
		}
		return value;
	}

	public static String getServicePropertyValue(GXService s, String propName, String propNameLegacy){
		String value = getServicePropertyValue(s, propName, true);
		if (value == null || value.length() == 0)
		{
			value = getServicePropertyValue(s, propNameLegacy, true);
		}
		return value;
	}

	public static String getEnvironmentVariable(String name, boolean required) throws Exception{
		String value = System.getenv(name);
		if ((value == null || value.length() == 0) && required){
			throw new Exception("Parameter " + name + " is required");
		}
		return value;
	}
}
