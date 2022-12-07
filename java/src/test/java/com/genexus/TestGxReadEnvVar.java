package com.genexus;


import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.util.EnvVarReader;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestGxReadEnvVar {

	private static final String GX_PROP = "DB_URL";
	private static final String GX_DATASTORE = "COM.GENEXUS.SAMPLEAPP|DEFAULT";
	private static final String FIRST_ENV_VAR = "GX_DEFAULT_DB_URL";
	private static final String SECOND_ENV_VAR = "GX_COM_GENEXUS_SAMPLEAPP_DEFAULT_DB_URL";
	private static final String FIRST_VALUE = "FirstDB_URL";
	private static final String SECOND_VALUE = "SecondDB_URL";

	@Test
	public void testGxReadEnvVar()
	{
		Connect.init();
		ModelContext modelContext = ModelContext.getModelContext(GXcfg.class);
		try {
			Map<String, String> newenv = new HashMap<>();
			newenv.put(FIRST_ENV_VAR, FIRST_VALUE);
			setEnvVar(newenv);
			String envVarValue = EnvVarReader.getEnvironmentVar(GX_DATASTORE, GX_PROP, null);
			Assert.assertEquals(FIRST_VALUE, envVarValue);

			newenv.put(SECOND_ENV_VAR, SECOND_VALUE);
			setEnvVar(newenv);
			envVarValue = EnvVarReader.getEnvironmentVar(GX_DATASTORE, GX_PROP, null);
			Assert.assertEquals(SECOND_VALUE, envVarValue);
			newenv.put(FIRST_ENV_VAR, null);
			setEnvVar(newenv);
			newenv.put(SECOND_ENV_VAR, null);
			setEnvVar(newenv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setEnvVar(Map<String, String> newenv) throws Exception{
		try {
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
			env.putAll(newenv);
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
			cienv.putAll(newenv);
		} catch (NoSuchFieldException e)
		{
			Class[] classes = Collections.class.getDeclaredClasses();
			Map<String, String> env = System.getenv();
			for (Class cl : classes) {
				if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
					Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					Object obj = field.get(env);
					Map<String, String> map = (Map<String, String>) obj;
					map.clear();
					map.putAll(newenv);
				}
			}
		}
	}
}
