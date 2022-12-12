package com.genexus.internet;
import com.genexus.Application;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestHttpClient {

	private static String TEST_URL_ERROR = "https://localhost/My API with Spaces";
	private static String TEST_URL_OK = "https://www.google.com/";
	private static String TEST_URL_OK_WITH_SPACES = "https://www.google.com?q=My API with Spaces";
	@Before
	public void setUp() throws Exception {
		Connect.init();
		Application.init(GXcfg.class);
		LogManager.initialize(".");
	}

	@Test
	public void executeHttpGetJavaLibInvalidURI() {
		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", TEST_URL_ERROR);
		Assert.assertEquals(0, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaOldLibInvalidURI() {
		HttpClientManual httpClient = new HttpClientManual();
		httpClient.setTimeout(1);
		httpClient.execute("GET", TEST_URL_ERROR);
		Assert.assertEquals(0, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaLib() {
		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", TEST_URL_OK);
		Assert.assertEquals(200, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaOldLib() {
		HttpClientManual httpClient = new HttpClientManual();
		httpClient.setTimeout(1);
		httpClient.execute("GET", TEST_URL_OK);
		Assert.assertEquals(200, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaLib2() {
		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", TEST_URL_OK_WITH_SPACES);
		Assert.assertEquals(200, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaOldLib2() {
		HttpClientManual httpClient = new HttpClientManual();
		httpClient.setTimeout(1);
		httpClient.execute("GET", TEST_URL_OK_WITH_SPACES);
		Assert.assertEquals(200, httpClient.getStatusCode());
	}
}
