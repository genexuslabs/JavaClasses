package com.genexus.performance;

import java.io.FileOutputStream;
import java.io.PrintStream;

import com.genexus.CommonUtil;

public class DataStoreProviderJMX implements DataStoreProviderJMXMBean{
	
	DataStoreProviderInfo dataStoreProvider;
	
  public DataStoreProviderJMX(String name, IDataStoreProviderInfo dataStoreProviderInfo)
  {
	  dataStoreProvider = dataStoreProviderInfo.getDataStoreProviderInfo(name);
  }
  
  static public void CreateDataStoreProviderJMX(String name, IDataStoreProviderInfo dataStoreProviderInfo)
  {
    try
    {
      MBeanUtils.createMBeanDataStoreProvider(name, dataStoreProviderInfo);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register DataStoreProvider MBean."+e.toString());
    }
  }
  
  public long getTotalSQLStatementCount()
  {
	  return dataStoreProvider.getSentenceCount();
  }
  
  public long getSelectSQLStatementCount()
  {
	  return dataStoreProvider.getSentenceSelectCount();
  }
  
  public long getUpdateSQLStatementCount()
  {
	  return dataStoreProvider.getSentenceUpdateCount();
  }
  
  public long getDeleteSQLStatementCount()
  {
	  return dataStoreProvider.getSentenceDeleteCount();
  }
  
  public long getInsertSQLStatementCount()
  {
	  return dataStoreProvider.getSentenceInsertCount();
  }
  
  public long getStoredProcedureCount()
  {
	  return dataStoreProvider.getSentenceCallCount();
  }  
  
  public long getSQLCommandCount()
  {
	  return dataStoreProvider.getSentenceDirectSQLCount();
  }
	
  public void dumpTxt()
  {
	  try
	  {
		PrintStream out = new PrintStream(new FileOutputStream("DataStoreProvider_" + dataStoreProvider.getName() +  CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date()) + ".log", true));			  
		dataStoreProvider.dump(out);
		
		out.close();
	  }
	  catch (java.io.IOException e)
	  {
	  }
  }
  
  public void dumpDataStoreInformation()  
  {
	  String fileName = "DataStoreProvider_" + dataStoreProvider.getName() +  CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date()) + ".xml";
	  com.genexus.xml.XMLWriter writer = new com.genexus.xml.XMLWriter();
	  writer.xmlStart(fileName);
	  dataStoreProvider.dump(writer);
	  writer.close();
  }
}
