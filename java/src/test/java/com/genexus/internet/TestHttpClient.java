package com.genexus.internet;
import com.genexus.Application;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestHttpClient {

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
		httpClient.execute("GET", "https://localhost/My API with Spaces");
		Assert.assertEquals(0, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaLib() {
		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "https://www.google.com/");
		Assert.assertEquals(200, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaLib2() {
		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "https://www.google.com?q=My API with Spaces");
		Assert.assertEquals(200, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaLib3() {
		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "https://www.google.com/test api");
		Assert.assertEquals(404, httpClient.getStatusCode());
	}

	@Test
	public void executeHttpGetJavaLib4() {
		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "https://www.google.com/test api?q=hello world");
		Assert.assertEquals(404, httpClient.getStatusCode());
	}
}
