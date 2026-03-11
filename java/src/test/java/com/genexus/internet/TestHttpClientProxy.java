package com.genexus.internet;

import com.genexus.Application;
import com.genexus.sampleapp.GXcfg;
import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestHttpClientProxy {

	private static final String PROXY_HOST_PROP = "http.proxyHost";
	private static final String PROXY_PORT_PROP = "http.proxyPort";
	private static final String HTTPS_PROXY_HOST_PROP = "https.proxyHost";
	private static final String HTTPS_PROXY_PORT_PROP = "https.proxyPort";
	private static final String NON_PROXY_HOSTS_PROP = "http.nonProxyHosts";

	private String origHttpProxyHost;
	private String origHttpProxyPort;
	private String origHttpsProxyHost;
	private String origHttpsProxyPort;
	private String origNonProxyHosts;

	@Before
	public void setUp() throws Exception {
		Connect.init();
		Application.init(GXcfg.class);
		LogManager.initialize(".");

		origHttpProxyHost = System.getProperty(PROXY_HOST_PROP);
		origHttpProxyPort = System.getProperty(PROXY_PORT_PROP);
		origHttpsProxyHost = System.getProperty(HTTPS_PROXY_HOST_PROP);
		origHttpsProxyPort = System.getProperty(HTTPS_PROXY_PORT_PROP);
		origNonProxyHosts = System.getProperty(NON_PROXY_HOSTS_PROP);
	}

	@After
	public void tearDown() {
		restoreProperty(PROXY_HOST_PROP, origHttpProxyHost);
		restoreProperty(PROXY_PORT_PROP, origHttpProxyPort);
		restoreProperty(HTTPS_PROXY_HOST_PROP, origHttpsProxyHost);
		restoreProperty(HTTPS_PROXY_PORT_PROP, origHttpsProxyPort);
		restoreProperty(NON_PROXY_HOSTS_PROP, origNonProxyHosts);
	}

	private void restoreProperty(String key, String originalValue) {
		if (originalValue == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, originalValue);
		}
	}

	@Test
	public void httpRequestUsesProxyFromSystemProperties() {
		// Set proxy to a non-existent proxy server (localhost on an unlikely port).
		// If the client respects the system property, it will try to connect through
		// this proxy and fail, resulting in a non-200 status code.
		System.setProperty(PROXY_HOST_PROP, "127.0.0.1");
		System.setProperty(PROXY_PORT_PROP, "19999");

		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "http://www.google.com/");

		// The request should fail because it tried to route through the non-existent proxy
		Assert.assertNotEquals("Request should fail when routed through non-existent proxy",
			200, httpClient.getStatusCode());
	}

	@Test
	public void httpsRequestUsesProxyFromSystemProperties() {
		// Set HTTPS proxy to a non-existent proxy server
		System.setProperty(HTTPS_PROXY_HOST_PROP, "127.0.0.1");
		System.setProperty(HTTPS_PROXY_PORT_PROP, "19999");

		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "https://www.google.com/");

		// The request should fail because it tried to route through the non-existent proxy
		Assert.assertNotEquals("HTTPS request should fail when routed through non-existent proxy",
			200, httpClient.getStatusCode());
	}

	@Test
	public void requestSucceedsWithoutProxySystemProperties() {
		// Ensure no proxy system properties are set
		System.clearProperty(PROXY_HOST_PROP);
		System.clearProperty(PROXY_PORT_PROP);
		System.clearProperty(HTTPS_PROXY_HOST_PROP);
		System.clearProperty(HTTPS_PROXY_PORT_PROP);

		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "https://www.google.com/");

		// Without proxy, direct connection should succeed
		Assert.assertEquals("Direct request without proxy should succeed",
			200, httpClient.getStatusCode());
	}

	@Test
	public void explicitProxyOverridesSystemProperties() {
		// Set system properties to a valid-looking proxy (that we won't actually hit)
		System.setProperty(PROXY_HOST_PROP, "10.255.255.1");
		System.setProperty(PROXY_PORT_PROP, "3128");

		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);

		// Explicit proxy should take precedence over system properties
		httpClient.setProxyServerHost("127.0.0.1");
		httpClient.setProxyServerPort(19999);
		httpClient.execute("GET", "http://www.google.com/");

		// Should fail because the explicit proxy (127.0.0.1:19999) doesn't exist
		Assert.assertNotEquals("Explicit proxy should be used instead of system property proxy",
			200, httpClient.getStatusCode());
	}

	@Test
	public void nonProxyHostsBypassesProxy() {
		// Set proxy to a non-existent proxy
		System.setProperty(HTTPS_PROXY_HOST_PROP, "127.0.0.1");
		System.setProperty(HTTPS_PROXY_PORT_PROP, "19999");
		// But exclude google.com from proxy
		System.setProperty(NON_PROXY_HOSTS_PROP, "*.google.com");

		HttpClientJavaLib httpClient = new HttpClientJavaLib();
		httpClient.setTimeout(1);
		httpClient.execute("GET", "https://www.google.com/");

		// Should succeed because google.com is in the non-proxy list
		Assert.assertEquals("Request to non-proxy host should bypass proxy and succeed",
			200, httpClient.getStatusCode());
	}
}
