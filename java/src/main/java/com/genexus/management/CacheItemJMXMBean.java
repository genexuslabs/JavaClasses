package com.genexus.management;

public interface CacheItemJMXMBean
{
  String getSQLSentence();
  Object [] getParemeters();
  long getSize();
  int getHitCount();
  int getExpiryHitsCount();
  java.util.Date getExpiryTime();
  java.util.Date getTimeCreated();

}
