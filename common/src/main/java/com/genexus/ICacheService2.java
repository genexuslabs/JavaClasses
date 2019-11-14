package com.genexus;

import java.util.List;

public interface ICacheService2 extends ICacheService
{
	<T> List<T> getAll(String cacheid, String[] keys, Class<T> type);
	<T> void setAll(String cacheid, String[] keys, T[] values, int expirationSeconds);
}
