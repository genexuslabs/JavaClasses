package com.genexus.service.redis;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

public class RedisClient implements Closeable {
	static final ILogger logger = LogManager.getLogger(RedisClient.class);
	static int REDIS_DEFAULT_PORT = 6379;
	static String REDIS_DEFAULT_HOSTNAME = "127.0.0.1";

	private JedisPool pool;
	private ObjectMapper objMapper;
	private String keyPattern = "%s_%s_%s"; //Namespace_KEY

	public RedisClient(String hostName, int port, String password, String cacheKeyPattern) {
		initCache(hostName, port, password, cacheKeyPattern);
	}

	public RedisClient(String hostName) {
		initCache(hostName, REDIS_DEFAULT_PORT, null, "");
	}

	private void initCache(String hostNameWithOptionalPort, int port, String password, String cacheKeyPattern) {
		objMapper = new ObjectMapper();
		objMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objMapper.enable(SerializationFeature.INDENT_OUTPUT);

		keyPattern = !isNullOrEmpty(cacheKeyPattern)? cacheKeyPattern: keyPattern;

		if (hostNameWithOptionalPort.startsWith("redis://")) {
			pool = new JedisPool(new JedisPoolConfig(), hostNameWithOptionalPort);
			return;
		}

		port = (port > 0) ? port: REDIS_DEFAULT_PORT;
		hostNameWithOptionalPort = (isNullOrEmpty(hostNameWithOptionalPort))? REDIS_DEFAULT_HOSTNAME: hostNameWithOptionalPort;

		try {
			URI redisUri = new URI(hostNameWithOptionalPort);
			if (redisUri.getPort() > 0) {
				port = redisUri.getPort();
			}
			hostNameWithOptionalPort = redisUri.getHost();
		} catch (java.net.URISyntaxException ex) {
			logger.error("Invalid redis uri " + hostNameWithOptionalPort, ex);
		}

		pool = new JedisPool(new JedisPoolConfig(), hostNameWithOptionalPort, port);
		if (!isNullOrEmpty(password)) {
			pool.getResource().auth(password);
		}
	}

	private boolean isNullOrEmpty(String s) {

		return s == null || s.trim().length() == 0;
	}

	private Boolean containsKey(String key) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			return jedis.exists(key);
		} catch (Exception e) {
			logger.error("Contains Key failed", e);
		} finally {
			close(jedis);
		}
		return false;
	}

	private void close(Jedis jedis) {
		if (jedis != null) {
			jedis.close();
		}
	}

	private <T> void set(String key, T value) {
		set(key, value, 0);
	}

	private <T> void set(String key, T value, int expirationSeconds) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			String valueJSON = objMapper.writeValueAsString(value);
			if (expirationSeconds > 0)
				jedis.setex(key, expirationSeconds, valueJSON);
			else
				jedis.set(key, valueJSON);
		} catch (Exception e) {
			logger.error("Set with TTL failed", e);
		} finally {
			close(jedis);
		}
	}

	public <T> void setAll(String cacheId, String[] keys, T[] values, int expirationSeconds) {
		Jedis jedis = null;
		try {
			if (keys != null && values != null && keys.length == values.length) {
				String[] prefixedKeys = getKey(cacheId, keys);
				jedis = pool.getResource();
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
		} finally {
			close(jedis);
		}
	}

	private <T> T get(String key, Class<T> type) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			String json = jedis.get(key);
			if (StringUtils.isNotEmpty(json)) {
				return objMapper.readValue(json, type);
			} else {
				return null;
			}
		} catch (Exception e) {
			logger.error("Get Item failed", e);
		} finally {
			close(jedis);
		}
		return null;
	}

	public <T> List<T> getAll(String cacheId, String[] keys, Class<T> type) {
		List<T> result = null;
		Jedis jedis = null;
		try {
			String[] prefixedKeys = getKey(cacheId, keys);
			jedis = pool.getResource();
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
		} finally {
			close(jedis);
		}
		return null;
	}


	public boolean containsKey(String cacheId, String key) {
		return containsKey(getKey(cacheId, key));
	}

	public <T> T get(String cacheId, String key, Class<T> type) {
		return get(getKey(cacheId, key), type);
	}


	public <T> void set(String cacheId, String key, T value) {
		set(getKey(cacheId, key), cacheId);
	}

	public <T> void set(String cacheId, String key, T value, int duration) {
		set(getKey(cacheId, key), value, duration);
	}

	public void clear(String cacheId, String key) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.del(getKey(cacheId, key));
		} catch (Exception e) {
			logger.error("Remove Item failed", e);
		} finally {
			close(jedis);
		}
	}

	public void clearCache(String cacheId) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.incr(cacheId);
		} catch (Exception e) {
			logger.error("clearCache failed", e);
		} finally {
			close(jedis);
		}
	}

	public void clearKey(String key) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.del(key);
		} catch (Exception e) {
			logger.error("Remove Item failed", e);
		} finally {
			close(jedis);
		}
	}

	public void clearAllCaches() {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.flushAll();
		} catch (Exception e) {
			logger.error("Clear All Caches failed", e);
		} finally {
			close(jedis);
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
