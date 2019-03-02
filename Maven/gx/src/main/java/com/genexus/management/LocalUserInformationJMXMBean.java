package com.genexus.management;

public interface LocalUserInformationJMXMBean
{
  int getId(); 
  String getLastSQLStatement();
  String getLastObject();
  java.util.Date getLastSQLStatementTime();
  boolean getWaitingForConnection();
  java.util.Date getWaitingForConnectionTime();
  int getLastConnectionId();
  
  void disconnect();
}
