package com.genexus;

public interface ICacheService
{
	boolean containtsKey(String cacheid, String key);
	<T> T get(String cacheid, String key, Class<T> type);
	<T> void set(String cacheid, String key, T value);
	<T> void set(String cacheid, String key, T value, int duration);

	void clear(String cacheid, String key);
	void clearCache(String cacheid);
	void clearKey(String key);
	void clearAllCaches();
}
