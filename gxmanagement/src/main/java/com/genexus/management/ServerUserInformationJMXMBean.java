package com.genexus.management;

public interface ServerUserInformationJMXMBean
{
  int getId(); 
  String getIP();
  java.util.Date getConnectedTime();
  long getIdleSeconds();
  String getLastSQLStatement();
  String getLastObject();
  java.util.Date getLastSQLStatementTime();
  boolean getWaitingForConnection();
  java.util.Date getWaitingForConnectionTime();
  int getLastConnectionId();
  String getUserId();
  
  void disconnect();
}
