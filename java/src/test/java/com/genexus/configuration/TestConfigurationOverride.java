package com.genexus.configuration;

import com.genexus.Application;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestConfigurationOverride {

	@Before
	public void setUpStreams() {
		Connect.init();
		Application.init(GXcfg.class);
	}

	@Test
	public void testReadConfiguration() {
		String packageName = Application.getClientPreferences().getPACKAGE();
		assertEquals("com.genexus.sampleapp", packageName);
	}

	@Test
	public void testReadConfigurationFromDev() {
		String customOverridablePty = Application.getClientPreferences().getProperty("MY_CUSTOM_PTY", "");
		assertEquals("SAMPLE_VALUE_FOR_DEV", customOverridablePty);
	}

	@Test
	public void testReadConfigurationFromProdError() {
		String customOverridablePty = Application.getClientPreferences().getProperty("MY_CUSTOM_PTY_PROD", "");
		assertEquals("", customOverridablePty);
	}
}
