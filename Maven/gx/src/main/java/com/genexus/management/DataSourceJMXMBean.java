package com.genexus.management;

public interface DataSourceJMXMBean
{ 
  String getName();
  String getUserName();
  String getJDBCDriver();
  String getJDBCURL();
  int getMaxCursors();
  //int getROPoolUsers();
  //boolean getROPoolEnabled();
  //boolean getROPoolRecycle();
  //int getROPoolRecycleMins();
  boolean getPoolEnabled();
  boolean getPoolRecycleEnabled();
  int getPoolRecyclePeriod();
  boolean getConnectAtStartup();
  
  //void ROPoolRecycle();
  void RecyclePool();
}
