package com.genexus.management;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import com.genexus.db.*;
import com.genexus.CacheFactory;
import com.genexus.Preferences;

public class CacheJMX implements CacheJMXMBean{
	
	private InProcessCache resultCache;
	

  public CacheJMX(InProcessCache resultCache)
  {
    this.resultCache = resultCache;
  }

  static public void CreateCacheJMX(InProcessCache resultCache)
  {
    try
    {
      MBeanUtils.createMBean(resultCache);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register Cache MBean."+e.toString());
    }
  }
  
  static public void DestroyCacheJMX()
  {
    try
    {
      MBeanUtils.destroyMBeanCache();
    }
    catch(Exception e)
    {
      System.err.println("Cannot destroy Cache MBean."+e.toString());
    }
  }  

  public long getStorageSize()
  {
	  return resultCache.getCacheStorageSize();
  }
  
  public long getCurrentSize()
  {
	  return resultCache.getCacheCurrentSize();
  }
  
  public boolean getEnabled()
  {
	  return resultCache.isEnabled();
  }
  
  public void setEnabled(boolean value)
  {
	  resultCache.setEnabled(value);
  }
  
  public int [] getTimeToLive()
  {
	  return Preferences.TTL;
  }
  
  public void setTimeToLive(int [] value)
  {
	resultCache.setTimeToLive(value);  
  }  
  
  public int [] getHitsToLive()
  {
	  return Preferences.HTL;
  }
  
  public void setHitsToLive(int [] value)
  {
	resultCache.setHitsToLive(value);
  }  
  
  public void restart()
  {
	  ConcurrentHashMap  cache = resultCache.getCache();
	  for(Enumeration enumera = cache.elements(); enumera.hasMoreElements();)
	  {
		  CacheValue value = (CacheValue)enumera.nextElement();
		  CacheItemJMX.DestroyCacheItemJMX(value);
	  }	  
	  CacheFactory.restartCache();
  }
}
