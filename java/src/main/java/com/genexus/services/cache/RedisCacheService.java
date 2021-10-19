package com.genexus.services.cache;

import com.genexus.Application;
import com.genexus.ICacheService2;
import com.genexus.cache.redis.RedisClient;
import com.genexus.services.ServiceHelper;
import com.genexus.services.ServiceConfigurationException;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;

import java.util.List;

public class RedisCacheService implements ICacheService2 {
	static String NAME = "CACHE_PROVIDER";

	private RedisClient client;

	public RedisCacheService() throws ServiceConfigurationException {

		GXService providerService = Application.getGXServices().get(GXServices.CACHE_SERVICE);
		ServiceHelper sHelper = new ServiceHelper(null, "", NAME);
		String hostName = sHelper.getPropertyValue("ADDRESS", "");
		String keyPattern = sHelper.getPropertyValue("KEYPATTERN", "");
		String password = sHelper.getPropertyValue("PASSWORD", "");

		client = new RedisClient(hostName, -1, password, keyPattern);
	}

	@Override
	public <T> List<T> getAll(String cacheId, String[] keys, Class<T> type) {
		return client.getAll(cacheId, keys, type);
	}

	public <T> void setAll(String cacheId, String[] keys, T[] values, int expirationSeconds) {
		client.setAll(cacheId, keys, values, expirationSeconds);
	}

	@Override
	public boolean containtsKey(String cacheId, String key) {
		return client.containsKey(cacheId, key);
	}

	@Override
	public <T> T get(String cacheId, String key, Class<T> type) {
		return client.get(cacheId, key, type);
	}

	@Override
	public <T> void set(String cacheId, String key, T value) {
		client.set(cacheId, key, value);
	}

	@Override
	public <T> void set(String cacheId, String key, T value, int duration) {
		client.set(cacheId, key, value, duration);
	}

	@Override
	public void clear(String cacheId, String key) {
		client.clear(cacheId, key);
	}

	@Override
	public void clearCache(String cacheId) {
		client.clearCache(cacheId);
	}

	@Override
	public void clearKey(String key) {
		client.clearKey(key);
	}

	@Override
	public void clearAllCaches() {
		client.clearAllCaches();
	}
}
