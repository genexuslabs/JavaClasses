package com.genexus.performance;

import com.genexus.db.IDataStoreProvider;

public class DataStoreProvidersJMX implements DataStoreProvidersJMXMBean{

	private IDataStoreProviderInfo dataStoreProviderInfo;
  public DataStoreProvidersJMX(IDataStoreProviderInfo dataStoreProviderInfo)
  {
	  this.dataStoreProviderInfo = dataStoreProviderInfo;
  }

  static public void CreateDataStoreProvidersJMX(IDataStoreProviderInfo dataStoreProviderInfo)
  {
    try
    {
	  MBeanUtils.createMBeanDataStoreProviders(dataStoreProviderInfo);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register DataStoreProviders MBean."+e.toString());
    }
  }
  
  public long getTotalSQLStatementCount()
  {
	  return IDataStoreProviderInfo.getSentenceCount();
  }
  
  public long getSelectSQLStatementCount()
  {
	  return IDataStoreProviderInfo.getSentenceSelectCount();
  }
  
  public long getUpdateSQLStatementCount()
  {
	  return IDataStoreProviderInfo.getSentenceUpdateCount();
  }
  
  public long getDeleteSQLStatementCount()
  {
	  return IDataStoreProviderInfo.getSentenceDeleteCount();
  }
  
  public long getInsertSQLStatementCount()
  {
	  return IDataStoreProviderInfo.getSentenceInsertCount();
  }
  
  public long getStoredProcedureCount()
  {
	  return IDataStoreProviderInfo.getSentenceCallCount();
  }  
  
  public long getSQLCommandCount()
  {
	  return IDataStoreProviderInfo.getSentenceDirectSQLCount();
  }
	
  public void dumpDataStoresInformation()
  {
	  IDataStoreProviderInfo.dump();
  }
}
