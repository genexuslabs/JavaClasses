package com.genexus.management;

import com.genexus.db.CacheValue;

public class CacheItemJMX implements CacheItemJMXMBean{
	
	private CacheValue cacheValue;
	

  public CacheItemJMX(CacheValue cacheValue)
  {
    this.cacheValue = cacheValue;
  }

  static public void CreateCacheItemJMX(CacheValue cacheValue)
  {
    try
    {
      MBeanUtils.createMBean(cacheValue);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register Cache Item MBean."+e.toString());
    }
  }
  
  static public void DestroyCacheItemJMX(CacheValue cacheValue)
  {
    try
    {
      MBeanUtils.destroyMBean(cacheValue);
    }
    catch(Exception e)
    {
      System.err.println("Cannot destroy cacheValue MBean."+e.toString());
    }
  }  

  public String getSQLSentence()
  {
	  return cacheValue.getKey().getKey();
  }
  
  public Object [] getParemeters()
  {
	  return cacheValue.getKey().getParameters();
  }
  
  public long getSize()
  {
	  return cacheValue.getSize();
  }
  
  public int getHitCount()
  {
	  return cacheValue.getHitCount();
  }
  
  public int getExpiryHitsCount()
  {
	  return cacheValue.getExpiryHits();
  }
  
  public java.util.Date getExpiryTime()
  {
	  if (cacheValue.getExpiryTimeMilliseconds() == 0)
		  return null;
	  else
		return new java.util.Date(cacheValue.getTimestamp() + cacheValue.getExpiryTimeMilliseconds());
  }

  public java.util.Date getTimeCreated()
  {
	  return cacheValue.getTimeCreated();
  }
}
