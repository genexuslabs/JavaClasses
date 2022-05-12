package com.genexus.cache.redis;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.genexus.Application;
import com.genexus.ICacheService2;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;


public class RedisClient implements ICacheService2, Closeable {
	public static final ILogger logger = LogManager.getLogger(RedisClient.class);
	private String keyPattern = "%s_%s_%s"; //Namespace_KEY
	private static int REDIS_DEFAULT_PORT = 6379;
	private JedisPool pool;
	private ObjectMapper objMapper;

	public RedisClient() throws Exception {
		initCache();
	}

	public RedisClient(String hostOrRedisURL, String password, String cacheKeyPattern) throws Exception {
		initCache(hostOrRedisURL, password, cacheKeyPattern);
	}

	private void initCache() throws Exception {
		GXService providerService = Application.getGXServices().get(GXServices.CACHE_SERVICE);
		String addresses = providerService.getProperties().get("CACHE_PROVIDER_ADDRESS");
		String cacheKeyPattern = providerService.getProperties().get("CACHE_PROVIDER_KEYPATTERN");
		String password = providerService.getProperties().get("CACHE_PROVIDER_PASSWORD");
		initCache(addresses, password, cacheKeyPattern);
	}

	private void initCache(String hostOrRedisURL, String password, String cacheKeyPattern) throws Exception {
		keyPattern = isNullOrEmpty(cacheKeyPattern) ? keyPattern : cacheKeyPattern;
		String host = "127.0.0.1";
		hostOrRedisURL = isNullOrEmpty(hostOrRedisURL) ? host: hostOrRedisURL;
		int port = REDIS_DEFAULT_PORT;

		boolean isRedisURIScheme = hostOrRedisURL.startsWith("redis://");
		String sRedisURI = isRedisURIScheme ? hostOrRedisURL : "redis://" + hostOrRedisURL;

		try {
			URI redisURI = new URI(sRedisURI);
			host = redisURI.getHost();
			if (redisURI.getPort() > 0) {
				port = redisURI.getPort();
			}
		} catch (URISyntaxException e) {
			logger.error(String.format("Could not parse Redis URL. Check for supported URLs: %s" + sRedisURI), e);
			throw e;
		}

		password = (!isNullOrEmpty(password)) ? password : null;

		pool = new JedisPool(new JedisPoolConfig(), host, port, redis.clients.jedis.Protocol.DEFAULT_TIMEOUT, password);
		try (Jedis j = pool.getResource()) {
			//Test connection
		}

		objMapper = new ObjectMapper();
		objMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	private boolean isNullOrEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}

	private Boolean containsKey(String key) {
		try (Jedis jedis = pool.getResource()) {
			return jedis.exists(key);
		} catch (Exception e) {
			logger.error("Contains failed", e);
		}
		return false;
	}

	private <T> void set(String key, T value) {
		set(key, value, 0);
	}

	private <T> void set(String key, T value, int expirationSeconds) {
		try (Jedis jedis = pool.getResource()) {
			String valueJSON = objMapper.writeValueAsString(value);
			if (expirationSeconds > 0)
				jedis.setex(key, expirationSeconds, valueJSON);
			else
				jedis.set(key, valueJSON);
		} catch (Exception e) {
			logger.error("Set with TTL failed", e);
		}
	}

	public <T> void setAll(String cacheid, String[] keys, T[] values, int expirationSeconds) {
		try (Jedis jedis = pool.getResource()) {
			if (keys != null && values != null && keys.length == values.length) {
				String[] prefixedKeys = getKey(cacheid, keys);
				Pipeline p = jedis.pipelined();
				int idx = 0;
				for (String key : prefixedKeys) {
					String valueJSON = objMapper.writeValueAsString(values[idx]);
					if (expirationSeconds > 0)
						p.setex(key, expirationSeconds, valueJSON);
					else
						p.set(key, valueJSON);
					idx++;
				}
				p.sync();
			}
		} catch (Exception e) {
			logger.error("SetAll with TTL failed", e);
		}
	}

	private <T> T get(String key, Class<T> type) {
		try (Jedis jedis = pool.getResource()) {
			String json = jedis.get(key);
			if (StringUtils.isNotEmpty(json)) {
				return objMapper.readValue(json, type);
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Get Item failed", e);
		}
		return null;
	}

	public <T> List<T> getAll(String cacheid, String[] keys, Class<T> type) {
		List<T> result = null;
		try (Jedis jedis = pool.getResource()) {
			String[] prefixedKeys = getKey(cacheid, keys);
			List<String> json = jedis.mget(prefixedKeys);
			result = new ArrayList<T>();
			for (String val : json) {
				if (val != null)
					result.add(objMapper.readValue(val, type));
				else
					result.add(null);
			}
			return result;
		} catch (Exception e) {
			logger.error("Get Item failed", e);
		}
		return null;
	}


	public boolean containtsKey(String cacheid, String key) {
		return containsKey(getKey(cacheid, key));
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
		try (Jedis jedis = pool.getResource()) {
			jedis.del(getKey(cacheid, key));
		} catch (Exception e) {
			logger.error("Remove Item failed", e);
		}
	}

	public void clearCache(String cacheid) {
		try (Jedis jedis = pool.getResource()) {
			jedis.incr(cacheid);
		} catch (Exception e) {
			logger.error("clearCache failed", e);
		}
	}

	public void clearKey(String key) {
		try (Jedis jedis = pool.getResource()) {
			jedis.del(key);
		} catch (Exception e) {
			logger.error("Remove Item failed", e);
		}
	}

	public void clearAllCaches() {
		try (Jedis jedis = pool.getResource()) {
			jedis.flushAll();
		} catch (Exception e) {
			logger.error("Clear All Caches failed", e);
		}
	}

	private String getKey(String cacheid, String key) {
		return String.format(keyPattern, cacheid, getKeyPrefix(cacheid), com.genexus.CommonUtil.getHash(key));
	}

	private String[] getKey(String cacheid, String[] keys) {
		Long prefix = getKeyPrefix(cacheid);
		String[] prefixedKeys = new String[keys.length];
		for (int idx = 0; idx < keys.length; idx++) {
			prefixedKeys[idx] = formatKey(cacheid, keys[idx], prefix);
		}
		return prefixedKeys;
	}

	private String formatKey(String cacheid, String key, Long prefix) {
		return String.format(keyPattern, cacheid, prefix, com.genexus.CommonUtil.getHash(key));
	}

	private Long getKeyPrefix(String cacheid) {
		Long prefix = get(cacheid, Long.class);
		if (prefix == null) {
			prefix = new java.util.Date().getTime();
			set(cacheid, Long.valueOf(prefix));
		}
		return prefix;
	}

	@Override
	public void close() throws IOException {
		if (pool != null)
			pool.destroy();
	}

}
