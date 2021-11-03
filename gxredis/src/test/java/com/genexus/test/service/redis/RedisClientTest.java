package com.genexus.test.service.redis;

import com.genexus.specific.java.Connect;
import com.genexus.specific.java.LogManager;
import com.genexus.service.redis.RedisClient;
import org.junit.*;

public class RedisClientTest {

	private RedisClient client;
	private String cacheId;

	@Before
	public void initialize() {
		Connect.init();
		LogManager.initialize(".");

		String hostName = getHostName();
		int port = 6379;

		client = new RedisClient(hostName, port);
		cacheId = "testcache";
	}

	private String getHostName() {
		String hostName = System.getenv("TEST_REDIS_CACHE_HOST");
		if (hostName == null) {
			hostName = "localhost";
		}
		return hostName;
	}

	@Test
	public void testEnsureSetData() {
		String key = getRandomKey("test-set-data");
		String value = getRandomKey("test-set-value");
		client.set(cacheId, key, value);

		String cacheValue = client.get(cacheId, key, String.class);

		Assert.assertEquals(value, cacheValue);
	}

	@Test
	public void testEnsureClearData() {
		String key = getRandomKey("test-set-data");
		String value = getRandomKey("test-set-value");
		client.set(cacheId, key, value);
		Assert.assertTrue(client.containsKey(cacheId, key));
		client.clear(cacheId, key);
		Assert.assertFalse(client.containsKey(cacheId, key));
		String cacheValue = client.get(cacheId, key, String.class);
		Assert.assertEquals(null, cacheValue);
	}

	@Test
	public void testEnsureContainsKey() {
		String key = getRandomKey("test-set-data");
		String value = getRandomKey("test-set-value");
		client.set(cacheId, key, value);
		Assert.assertTrue(client.containsKey(cacheId, key));
	}

	@Test
	public void testEnsureSchemaUrl() {

		//redis://clientid:password@localhost:6380
		String portS = "6379";

		client = new RedisClient(String.format("redis://%s:%s", getHostName(), portS));
		String key = getRandomKey("test-set-data");
		String value = getRandomKey("test-set-value");
		client.set(cacheId, key, value);
		Assert.assertTrue(client.containsKey(cacheId, key));
	}


	private String getRandomKey(String keyPrefix) {
		return keyPrefix + "_" + java.util.UUID.randomUUID().toString().substring(0,8);
	}
}
