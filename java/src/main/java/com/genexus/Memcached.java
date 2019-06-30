package com.genexus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

	@Override
	public <T> List<T> getAll(String cacheid, String[] keys, Class<T> type) {
		String[] prefixedKeys = getKey(cacheid, keys);
		Map<String, T> value = (Map<String, T>) _cache.getBulk(prefixedKeys);
		List<T> values = new ArrayList<T>();
		for(String key:prefixedKeys ){
			values.add(value.get(key));
		}
		return values;
	}

	public <T> void set(String cacheid, String key, T value) {
		set(getKey(cacheid, key), value);
	}

	public <T> void set(String cacheid, String key, T value, int duration) {
		
		set(getKey(cacheid, key), value, duration);
	}

	@Override
	public <T> void setAll(String cacheid, String[] keys, T[] values, int expirationSeconds) {
		if (keys!=null && values!=null && keys.length == values.length) {
			String[] prefixedKeys = getKey(cacheid, keys);
			int idx = 0;
			for (String key : prefixedKeys) {
				set(key, values[idx], expirationSeconds);
				idx++;
			}
		}
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

	@Override
	public void close() {
		_cache.shutdown();
	}

	private String getKey(String cacheid, String key)
	{
		return formatKey(cacheid, key, getKeyPrefix(cacheid));
	}
	private String[] getKey(String cacheid, String[] keys)
	{
		Long prefix = getKeyPrefix(cacheid);
		String[] prefixedKeys = new String[keys.length];
		for (int idx =0; idx<keys.length; idx++){
			prefixedKeys[idx] = formatKey(cacheid, keys[idx], prefix);
		}
		return prefixedKeys;
	}
	private String formatKey(String cacheid, String key, Long prefix)
	{
		return cacheid + prefix + CommonUtil.getHash(key);
	}

	private Long getKeyPrefix(String cacheid) {
		Long prefix = get(cacheid, Long.class);
		if (prefix == null)
		{
			prefix = CommonUtil.now(false, false).getTime();
			set(cacheid, Long.valueOf(prefix));
		}
		return prefix;
	}
}