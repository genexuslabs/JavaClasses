package com.genexus.util;

import com.genexus.ModelContext;

public class EnvVarReader {

    static String[] m_invalidChars = { ".", "|" };
    static final String ENVVAR_PREFIX = "GX_";

    public static String getEnvironmentVar(String section, String key, String mappedKey) {

		if (mappedKey!=null) {
			return System.getenv(mappedKey);
		}else{
			String prefix = ENVVAR_PREFIX;
			if (section != null && !section.isEmpty() && section != "Client") {
				section = replaceInvalidChars(section);
				String realKey = key;
				key = String.format("%s%s_%s", prefix, section.toUpperCase(), key.toUpperCase());
				String envVar = System.getenv(key);
				if (envVar != null)
					return envVar;
				if (ModelContext.getModelContext() != null) {
					section = section.replace(replaceInvalidChars(ModelContext.getModelContext().getPackageName() + "|").toUpperCase(), "");
					key = String.format("%s%s_%s", prefix, section.toUpperCase(), realKey.toUpperCase());
				}
				else
					return null;
			} else
				key = String.format("%s%s", prefix, key.toUpperCase());
			return System.getenv(key);
		}
	}

	private static String replaceInvalidChars(String section) {
		for (int i = 0; i < m_invalidChars.length; i++)
			section = section.replace(m_invalidChars[i], "_");
		return section;
	}

    public static String getEnvironmentValue(String serviceType, String serviceName, String propertyName) {
        String envVarName = String.format("%s%s_%s", ENVVAR_PREFIX, serviceType.toUpperCase(), propertyName.toUpperCase());
        String value = System.getenv(envVarName);

        if (value != null)
            return value;

        envVarName = String.format("%s%s__%s_%s", ENVVAR_PREFIX, serviceType.toUpperCase(), serviceName.toUpperCase(), propertyName.toUpperCase());

        return System.getenv(envVarName);
    }
}