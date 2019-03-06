package com.genexus;

import java.io.IOException;

import com.genexus.util.GXService;
import com.genexus.util.GXServices;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

public class Memcached implements ICacheService {

	MemcachedClient _cache;

	public Memcached() throws IOException {
		_cache = InitCache();
	}

	MemcachedClient InitCache() throws IOException {
		GXService providerService = Application.getGXServices().get(GXServices.CACHE_SERVICE);
		String addresses = providerService.getProperties().get("CACHE_PROVIDER_ADDRESS");
		String username = providerService.getProperties().get("CACHE_PROVIDER_USER");
		String password = providerService.getProperties().get("CACHE_PROVIDER_PASSWORD");
		if (addresses == null || addresses.isEmpty())
			addresses = "127.0.0.1:11211";

		if (username == null || username.isEmpty())			
		{
			return new MemcachedClient(new BinaryConnectionFactory(),
					AddrUtil.getAddresses(addresses));
		}
		else
		{
			AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"},
									new PlainCallbackHandler(username, password));

			return new MemcachedClient(new ConnectionFactoryBuilder().setProtocol(Protocol.BINARY)
						.setAuthDescriptor(ad)
						.build(),
						AddrUtil.getAddresses(addresses));
		}
	}

	private boolean containtsKey(String key) {
		return _cache.get(key) != null;
	}

	private <T> void set(String key, T value) {
		_cache.set(key, 0, value);

	}

	private <T> void set(String key, T value, int expirationSeconds) {
		_cache.set(key, expirationSeconds, value);

	}

	@SuppressWarnings("unchecked")
	private <T> T get(String key, Class<T> type) {
		T value = (T) _cache.get(key);
		return value;
	}

	public boolean containtsKey(String cacheid, String key) {
		return containtsKey(getKey(cacheid, key));
	}

	public <T> T get(String cacheid, String key, Class<T> type) {
		return get(getKey(cacheid, key), type);
	}

	public <T> void set(String cacheid, String key, T value) {
		set(getKey(cacheid, key), value);
	}

	public <T> void set(String cacheid, String key, T value, int duration) {
		
		set(getKey(cacheid, key), value, duration);
	}

	public void clear(String cacheid, String key) {
		_cache.delete(getKey(cacheid, key));
		
	}

	public void clearCache(String cacheid) {
		_cache.incr(cacheid, 1);
	}

	public void clearKey(String key) {
		_cache.delete(key);
	}

	public void clearAllCaches() {
		_cache.flush();
	}
	
	private String getKey(String cacheid, String key)
	{
		Long prefix = get(cacheid, Long.class);
		if (prefix == null)
		{
			prefix = CommonUtil.now(false, false).getTime();
			set(cacheid, Long.valueOf(prefix));
		}
		return cacheid + prefix + CommonUtil.getHash(key);
	}
}