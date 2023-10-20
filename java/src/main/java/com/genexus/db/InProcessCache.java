package com.genexus.db;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.Application;
import com.genexus.CommonUtil;
import com.genexus.ICacheService2;
import com.genexus.Preferences;
import com.genexus.util.DoubleLinkedQueue;


public class InProcessCache implements ICacheService2
{
	protected long cacheStorageSize;
	protected long currentSize;
	protected int cacheDrops;
	protected boolean cacheEnabled;
	protected DoubleLinkedQueue lru = new DoubleLinkedQueue();

	private static final boolean DEBUG = com.genexus.DebugFlag.DEBUG;
	private ConcurrentHashMap<String, CacheValue> cache = new ConcurrentHashMap<String, CacheValue>();
	private Object lockObject = new Object();

	private Class<?> cacheItemJMXClass;
	private Class<?> cacheJMXClass;

	public InProcessCache()
	{
		Preferences prefs = Preferences.getDefaultPreferences();

		cacheStorageSize = prefs.getCACHE_STORAGE_SIZE() * 1024;
		cacheEnabled = prefs.getCACHING();

		try
		{
			Class<?> cacheItemJMXClass = Class.forName("com.genexus.management.CacheItemJMX");
			Class<?> cacheJMXClass = Class.forName("com.genexus.management.CacheJMX");
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Failed to get CacheJMX and CacheItemJMX classes");
			e.printStackTrace();
		}

		//JMX Enabled
		if (Application.isJMXEnabled())
		{
			try
			{
				Method method = cacheJMXClass.getMethod("CreateCacheJMX");
				method.invoke(this);
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
			{
				System.out.println("Failed to create JMX cache");
				e.printStackTrace();
			}
		}
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

	@Override
	public <T> List<T> getAll(String cacheid, String[] keys, Class<T> type) {
		String[] prefixedKeys = getKey(cacheid, keys);
		List<T> values = new ArrayList<T>();
		for (String key : prefixedKeys) {
			values.add(get(key, type));
		}
		return values;
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
		{
			try
			{
				Method method = cacheJMXClass.getMethod("DestroyCacheJMX");
				method.invoke(null);
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
			{
				System.out.println("Failed to destroy JMX cache");
				e.printStackTrace();
			}
		}
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
			{
				try
				{
					Method method = cacheItemJMXClass.getMethod("DestroyCacheItemJMX");
					method.invoke(value);
				}
				catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
				{
					System.out.println("Failed to destroy JMX item cache");
					e.printStackTrace();
				}
			}
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
	
	private Long getKeyPrefix(String cacheid)
	{
		Long prefix = get(cacheid, Long.class);
		if (prefix == null)
		{
			prefix = CommonUtil.now(false,false).getTime();
			set(cacheid, Long.valueOf(prefix));
		}
		return prefix;
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
	/** Agrega un cacheValue al cache
	 */
	private void add(String key, CacheValue value)
	{
		//JMX Enabled
		if (Application.isJMXEnabled())
		{
			try
			{
				Method method = cacheItemJMXClass.getMethod("CreateCacheItemJMX");
				method.invoke(value);
			}
			catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
			{
				System.out.println("Failed to create JMX item cache");
				e.printStackTrace();
			}
		}

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
						{
							try
							{
								Method method = cacheItemJMXClass.getMethod("DestroyCacheItemJMX");
								method.invoke(item);
							}
							catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
							{
								System.out.println("Failed to destroy JMX item cache");
								e.printStackTrace();
							}
						}

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
				{
					try
					{
						Method method = cacheItemJMXClass.getMethod("DestroyCacheItemJMX");
						method.invoke(value);
					}
					catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
					{
						System.out.println("Failed to destroy JMX item cache");
						e.printStackTrace();
					}
				}

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
