package com.genexus.util;

import com.genexus.CacheFactory;
import com.genexus.ICacheService;

public class CacheAPI 
{
	static final String DEFAULT_CACHE_ID = "DefaultCache";

    public static CacheAPI database()
    {
        return new CacheAPI(CacheFactory.CACHE_DB);
    } 
    
    public static CacheAPI smartdevices()
    {
        return new CacheAPI(CacheFactory.CACHE_SD);
    }
    
	public static CacheAPI files()
    {
        return new CacheAPI(CacheFactory.CACHE_FL);
    }

    private static ICacheService cache = CacheFactory.getInstance();
    
    private String cacheId;
    
    public CacheAPI() {cacheId = DEFAULT_CACHE_ID;}
    
    public CacheAPI(String name)
    {
            cacheId = name;
    }

    public static CacheAPI getCache(String name)
    {
        return new CacheAPI(name);
    }
    
    public static void clearAllCaches()
    {
        cache.clearAllCaches();
    }
        
    public void set(String key, String value, int durationMinutes)
    {
        cache.set(cacheId, key, value, durationMinutes*60);
    }
    
    public boolean contains(String key)
    {   
        return cache.containtsKey(cacheId, key);
    }
    
    public String get(String key)
    {   
        return cache.get(cacheId, key, String.class);
    }

    public void remove(String key)
    {
        if (cacheId == CacheFactory.CACHE_SD && key!=null && !key.equals(""))
            key = key.toLowerCase();
        cache.clear(cacheId, key);
    }

    public void clear()
    {
        cache.clearCache(cacheId);
    }
}

