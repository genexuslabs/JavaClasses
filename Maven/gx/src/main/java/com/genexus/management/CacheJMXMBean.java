package com.genexus.management;

public interface CacheJMXMBean
{
  long getStorageSize();
  long getCurrentSize();
  boolean getEnabled();
  void setEnabled(boolean value);
  int [] getTimeToLive();
  void setTimeToLive(int [] value);  
  int [] getHitsToLive();
  void setHitsToLive(int [] value);  
  
  void restart();
}
