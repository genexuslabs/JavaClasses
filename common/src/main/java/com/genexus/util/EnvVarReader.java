package com.genexus.util;

public class EnvVarReader {

    static String[] m_invalidChars = { ".", "|" };
    static final String ENVVAR_PREFIX = "GX_";

    public static String getEnvironmentValue(String section, String key, boolean addPrefix) {
    	String prefix;
    	if (addPrefix)
    		prefix = ENVVAR_PREFIX;
    	else
    		prefix="";
        if (section != null && !section.isEmpty() && section != "Client") {
            for (int i = 0; i < m_invalidChars.length; i++)
                section = section.replace(m_invalidChars[i], "_");
            key = String.format("%s%s_%s", prefix, section.toUpperCase(), key.toUpperCase());
        } else
            key = String.format("%s%s", prefix, key.toUpperCase());

        return System.getenv(key);
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