package com.genexus;


import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.util.EnvVarReader;
import com.github.stefanbirkner.systemlambda.SystemLambda;
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
	public void testGxReadEnvVar() throws Exception {
		Connect.init();
		ModelContext modelContext = ModelContext.getModelContext(GXcfg.class);

		SystemLambda.withEnvironmentVariable(FIRST_ENV_VAR, FIRST_VALUE)
			.execute(() -> {
				String envVarValue = EnvVarReader.getEnvironmentVar(GX_DATASTORE, GX_PROP, null);
				Assert.assertEquals(FIRST_VALUE, envVarValue);
			});

		SystemLambda.withEnvironmentVariable(SECOND_ENV_VAR, SECOND_VALUE)
			.execute(() -> {
				String envVarValue = EnvVarReader.getEnvironmentVar(GX_DATASTORE, GX_PROP, null);
				Assert.assertEquals(SECOND_VALUE, envVarValue);
			});

		SystemLambda.withEnvironmentVariable(FIRST_ENV_VAR, FIRST_VALUE)
			.and(SECOND_ENV_VAR, SECOND_VALUE)
			.execute(() -> {
				String envVarValue1 = EnvVarReader.getEnvironmentVar(GX_DATASTORE, GX_PROP, null);
				Assert.assertEquals(SECOND_VALUE, envVarValue1); // Prioridad según tu EnvVarReader
			});

		SystemLambda.withEnvironmentVariable(FIRST_ENV_VAR, null)
			.and(SECOND_ENV_VAR, null)
			.execute(() -> {
				String envVarValue = EnvVarReader.getEnvironmentVar(GX_DATASTORE, GX_PROP, null);
				Assert.assertNull(envVarValue);
			});
	}
}
