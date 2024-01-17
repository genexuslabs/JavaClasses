package com.mockdata;

import com.genexus.Application;
import com.genexus.mock.GXMockProvider;
import com.genexus.mock.IGXMock;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMockData {
	private void initialize()
	{
		Connect.init();
		LogManager.initialize(".");
		Application.init(GXcfg.class);
	}

	@Test
	public void testWithoutMock() {
		initialize();

		GXMockProvider.setProvider(null);
		TestOriginalClass mockClass = new TestOriginalClass(-1);
		short GXv_int1[] = new short[1];
		mockClass.execute((short)1, "Hello!", GXv_int1);

		assertEquals((short)3, GXv_int1[0]);
	}

	@Test
	public void testWithMockData() {
		initialize();

		IGXMock mockProvider = new TestMockDataProvider();
		GXMockProvider.setProvider(mockProvider);

		TestOriginalClass mockClass = new TestOriginalClass(-1);
		short GXv_int1[] = new short[1];
		mockClass.execute((short)1, "Hello!", GXv_int1);

		assertEquals((short)4, GXv_int1[0]);
	}
}
