package com.genexus.db;
import java.util.*;

import com.genexus.Application;
import com.genexus.CommonUtil;
import com.genexus.ICacheService;
import com.genexus.CacheFactory;
import com.genexus.util.*;
import com.genexus.Preferences;

import java.util.concurrent.ConcurrentHashMap;

import com.genexus.management.CacheJMX;
import com.genexus.management.CacheItemJMX;

/** $Log: InProcessCache.java,v $
/** Revision 1.6  2005/07/22 23:03:47  iroqueta
/** Agrego ifdefs para que no de problemas JMX en .NET
/**
/** Revision 1.5  2005/07/21 15:10:38  iroqueta
/** Implementacion de soporte de JMX
/**
/** Revision 1.4  2004/02/20 19:40:59  gusbro
/** - El TTL ahora esta medido en minutos
/**
/** Revision 1.3  2004/02/19 20:47:30  gusbro
/** - La preference STORAGE_SIZE en el cfg esta medida en KBs y yo aca lo mido todo en bytes
/**
/** Revision 1.2  2004/02/19 17:08:56  gusbro
/** .
/**
/** Revision 1.1  2004/02/10 16:41:09  gusbro
/** - Release inicial
/**
 *  Implementa el cache de resultados.
 *  Notas: Deberíamos usar soft references, para que la VM libere automáticamente memoria
 *         cuando la necesite. El problema es que las soft references son JDK1.2+, así que
 *         por ahora no lo metemos para ser compatibles con MS
 */
public class InProcessCache implements ICacheService
{
	protected long cacheStorageSize;
	protected long currentSize;
	protected int cacheDrops;
	protected boolean cacheEnabled;
	protected DoubleLinkedQueue lru = new DoubleLinkedQueue();

	private static final boolean DEBUG = com.genexus.DebugFlag.DEBUG;
	private ConcurrentHashMap<String, CacheValue> cache = new ConcurrentHashMap<String, CacheValue>();
	private Object lockObject = new Object();

	public InProcessCache()
	{
		Preferences prefs = Preferences.getDefaultPreferences();

		cacheStorageSize = prefs.getCACHE_STORAGE_SIZE() * 1024;
		cacheEnabled = prefs.getCACHING();

		//JMX Enabled
		if (Application.isJMXEnabled())
			CacheJMX.CreateCacheJMX(this);
	}

	public boolean isEnabled()
	{
		return cacheEnabled;
	}

	public void setEnabled(boolean value)
	{
		cacheEnabled = value;
	}

	public ConcurrentHashMap<String, CacheValue> getCache()
	{
		return cache;
	}

	public void setCacheStorageSize(long cacheStorageSize)
	{
		this.cacheStorageSize = cacheStorageSize;
	}

	public long getCacheStorageSize()
	{
		return cacheStorageSize;
	}

	public long getCacheCurrentSize()
	{
		return currentSize;
	}

	public int getCacheDrops()
	{
		return cacheDrops;
	}

	public <T> T get(String cacheid, String key, Class<T> type)
	{
		return get(getKey(cacheid, key), type);
	}

	@SuppressWarnings("unchecked")
	private <T> T get(String key, Class<T> type)
	{
		if(DEBUG)
		{
			getStatsFor(key).hits++;
		}
		CacheValue value = cache.get(key);
	
		if(value == null)
		{
			return null;
		}
		else
		{
			if(value.hasExpired())
			{ // Si ha expirado el cache, debo eliminar el value del cache
                            clearKey(key, value);
				return null;
			}
			else
			{
				// If cache limit
				if (cacheStorageSize != 0) {
					lru.moveToStart(value); // Muevo el item al comienzo de la LRU
				}
				value.incHits(); //TODO: this is not thread safe, we can miss hits for a value.
				if(DEBUG)
				{
					getStatsFor(key).hitsAfterFullLoaded++;
				}
	
				if ( type.isInstance(value) ) {
				   return (T)value;
				}
				else
					return type.cast(((CachedIFieldGetter)value.getIterator().nextElement()).<T>getValue(0));
			}
		}


	}
	public void clear() {

		//JMX Remove
		if (Application.isJMXEnabled())
			CacheJMX.DestroyCacheJMX();
	}
	
	public void clearKey(String key, CacheValue value) {
		if (value!=null)
		{
			if(DEBUG)
			{
				getStatsFor(value).removeFromStats(value);
			}
			if (cacheStorageSize != 0) {
				synchronized (lockObject) {
					lru.remove(value);
					currentSize -= value.getSize();
				}
			}
			//JMX Remove
			if (Application.isJMXEnabled())
				CacheItemJMX.DestroyCacheItemJMX(value);
			cache.remove(key);
		}
	}
	
	public void clearKey(String key){
		CacheValue value = cache.get(key);
		clearKey(key, value);
	}
	public <T> void set(String cacheid, String key, T value, int expirationSeconds)
	{
		set(getKey(cacheid, key), value, expirationSeconds);
	}

	private <T> void set(String key, T value, int expirationSeconds)
	{
		if (value instanceof CacheValue)
			add(key, (CacheValue)value);
		else
		{
			CacheValue cvalue = new CacheValue(key, null);
			cvalue.addItem(value);
			cvalue.setExpiryTime(expirationSeconds);
			add(key, cvalue);
		}	
	}

	public <T> void set(String cacheid, String key, T value) {
		set(getKey(cacheid, key), value, Preferences.TTL_NO_EXPIRY);		
	}
	private <T> void set(String key, T value)
	{
		set(key, value, Preferences.TTL_NO_EXPIRY);
	}

	private boolean containsKey(String key) {
            CacheValue value = cache.get(key);
            if(value != null){
                if(value.hasExpired()){
                    clearKey(key, value);
                }
            } 
            return cache.containsKey(key);
	}

	public boolean containtsKey(String cacheid, String key) {
		return containsKey(getKey(cacheid, key));
	}

	public void clear(String cacheid, String key) {
		cache.remove(getKey(cacheid, key));
	}

	public void clearCache(String cacheid) {
		set(cacheid,Long.valueOf(CommonUtil.now().getTime()));
	}

	public void clearAllCaches() {
		cache.clear();
	}

	private String getKey(String cacheid, String key)
	{
		Long prefix = get(cacheid, Long.class);
		if (prefix == null)
		{
			prefix = CommonUtil.now(false,false).getTime();
			set(cacheid, Long.valueOf(prefix));
		}
		return cacheid + prefix + CommonUtil.getHash(key);
	}

	/** Agrega un cacheValue al cache
	 */
	private void add(String key, CacheValue value)
	{
		//JMX Enabled
		if (Application.isJMXEnabled())
			CacheItemJMX.CreateCacheItemJMX(value);

		value.setTimestamp();
		cache.put(key, value);

		synchronized (lockObject) {
			if(DEBUG)
			{
				getStatsFor(value).addToStats(value);
				getStatsFor(value).TTL = value.getExpiryTimeMilliseconds();
			}
			currentSize += value.getSize();
		}
		// Si el tamaño del cache excede el CacheMaximumSize debemos eliminar los mas viejos
		ensureCacheSize();
		if (cacheStorageSize != 0) {
			lru.insert(value); // Agreo el item en la LRU
		}

	}

	private void ensureCacheSize() {
		if (cacheStorageSize > 0) {
			synchronized (lockObject) {
				while (currentSize > cacheStorageSize)
					{
						CacheValue item = (CacheValue)lru.takeFromEnd();
						if(item == null)
						{
							break;
						}
						currentSize -= item.getSize();

						//JMX Remove
						if (Application.isJMXEnabled())
							CacheItemJMX.DestroyCacheItemJMX(item);

						cache.remove(item.getKey().getKey());
						cacheDrops++;
						if(DEBUG)
						{
							getStatsFor(item).removeFromStats(item);
						}
					}	
			}
		}
	
	}

	public void removeExpiredEntries()
	{
		for(Enumeration<CacheValue> enum1 = cache.elements(); enum1.hasMoreElements();)
		{
			CacheValue value = (CacheValue)enum1.nextElement();
			if(value.hasExpired())
			{
				if(DEBUG)
				{
					String key = value.getKey().getKey();
					getStatsFor(value).removeFromStats(value);
				}
				if (cacheStorageSize != 0) {
					lru.remove(value);
				}
				currentSize -= value.getSize();

				//JMX Remove
				if (Application.isJMXEnabled())
					CacheItemJMX.DestroyCacheItemJMX(value);

				cache.remove(value.getKey().getKey());
			}
		}
	}

	public void setTimeToLive(int [] value)
	{
		for(int i = 0; i < Preferences.CANT_CATS; i++)
		{
			Preferences.TTL[i] = value[i];
		}
	}

	public void setHitsToLive(int [] value)
	{
		for(int i = 0; i < Preferences.CANT_CATS; i++)
		{
			Preferences.HTL[i] = value[i];
		}
	}

	//------------------------------------ STATS ---------------

	private ConcurrentHashMap<String, Stats> cacheStats = new ConcurrentHashMap<String, InProcessCache.Stats>();
	public void addStats(String key)
	{
		getStatsFor(key);
	}

	public Stats getStatsFor(String key)
	{
		Stats newStats = new Stats(key);
		Stats stats = cacheStats.putIfAbsent(key, newStats);
		return (stats != null) ? stats : newStats;
	}

	public Stats getStatsFor(CacheValue value)
	{
		String key = value.getKey().toString();
		return getStatsFor(key);
	}

	public ConcurrentHashMap<String, Stats> getStats()
	{
		return cacheStats;
	}

	public String getStatsSentence(String key)
	{
		return getStatsFor(key).sentence;
	}

	public int getStatsHits(String key)
	{
		return getStatsFor(key).hits;
	}

	public int getStatsHitsCached(String key)
	{
		return getStatsFor(key).hitsAfterFullLoaded;
	}

	public int getStatsFullLoaded(String key)
	{
		return getStatsFor(key).fullLoaded;
	}

	public int getStatsCacheSize(String key)
	{
		return getStatsFor(key).cacheSize;
	}

	public long getStatsTTL(String key)
	{
		return getStatsFor(key).TTL / Preferences.SECONDS_IN_ONE_MINUTE;
	}

	class Stats
	{
		public Stats(String sentence)
		{
			this.sentence = sentence;
			TTL = -1;
		}

		public void removeFromStats(CacheValue value)
		{
			fullLoaded--;
			cacheSize -= value.getSize();
		}

		public void addToStats(CacheValue value)
		{
			fullLoaded++;
			cacheSize += value.getSize();
		}

		public String sentence;
		public int hits;
		public int hitsAfterFullLoaded;
		public int fullLoaded;
		public int cacheSize;
		public long TTL;
	}


}
