package com.genexus;

import java.util.List;

public interface ICacheService
{
	boolean containtsKey(String cacheid, String key);
	<T> T get(String cacheid, String key, Class<T> type);
	<T> List<T> getAll(String cacheid, String[] keys, Class<T> type);
	<T> void set(String cacheid, String key, T value);
	<T> void set(String cacheid, String key, T value, int duration);
	<T> void setAll(String cacheid, String[] keys, T[] values, int expirationSeconds);
	void clear(String cacheid, String key);
	void clearCache(String cacheid);
	void clearKey(String key);
	void clearAllCaches();
	void close();
}
