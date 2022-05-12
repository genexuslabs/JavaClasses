package com.genexus.cache.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRedisCacheClient {
	public static final Logger logger = LogManager.getLogger(TestRedisCacheClient.class);

	@Before
	public void beforeTest() {
		Connect.init();
		com.genexus.specific.java.LogManager.initialize(".");
	}

	@Test
	public void connect_full_url()
	{
		RedisClient redis = getRedisClient("redis://localhost:6379", "");
		Assert.assertNotNull("Redis instance could not be created", redis);
	}

	@Test
	public void connect_error_full_url()
	{
		RedisClient redis = getRedisClient("redis://localhost:1111", "");
		Assert.assertNull("Redis instance must fail", redis);
	}

	@Test
	public void connect_host_and_port()
	{
		RedisClient redis = getRedisClient("localhost:6379", "");
		Assert.assertNotNull("Redis instance could not be created", redis);
	}

	@Test
	public void connect_hostname_only()
	{
		RedisClient redis = getRedisClient("localhost", "");
		Assert.assertNotNull("Redis instance could not be created", redis);
	}

	@Test
	public void connect_error_wrong_password()
	{
		RedisClient redis = getRedisClient("localhost", "1231");
		Assert.assertNull("Redis instance could not be created", redis);
	}


	@Test
	public void connect_default_host_port()
	{
		RedisClient redis = getRedisClient("", "");
		Assert.assertNotNull("Redis instance could not be created", redis);
	}


	private RedisClient getRedisClient(String hostOrUrl, String password) {
		RedisClient redis = null;

		try {
			redis = new RedisClient(hostOrUrl, password, "UNIT");
		} catch (Exception e) {
			logger.debug("failed to create redis client", e);
		}

		return redis;
	}


	@Test
	public void testSetRedis()
	{
		Connect.init();

		RedisClient redis = getRedisClient("", "");
		Assert.assertNotNull("Redis instance could not be created", redis);

		String cacheId = "TEST";
		String cacheKey = "TEST_KEY";
		String cacheValue = "KeyValue";
		redis.set(cacheId, cacheKey, cacheValue);

		String obtainedValue = redis.get(cacheId, cacheKey, String.class);

		Assert.assertEquals(obtainedValue, cacheValue);



	}

}
