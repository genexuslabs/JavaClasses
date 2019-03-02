package com.genexus.performance;

import com.genexus.db.*;

public class DataStoreProvidersJMX implements DataStoreProvidersJMXMBean{

  public DataStoreProvidersJMX()
  {
  }

  static public void CreateDataStoreProvidersJMX()
  {
    try
    {
	  MBeanUtils.createMBeanDataStoreProviders();
    }
    catch(Exception e)
    {
      System.err.println("Cannot register DataStoreProviders MBean."+e.toString());
    }
  }
  
  public long getTotalSQLStatementCount()
  {
	  return DataStoreProvider.getSentenceCount();
  }
  
  public long getSelectSQLStatementCount()
  {
	  return DataStoreProvider.getSentenceSelectCount();
  }
  
  public long getUpdateSQLStatementCount()
  {
	  return DataStoreProvider.getSentenceUpdateCount();
  }
  
  public long getDeleteSQLStatementCount()
  {
	  return DataStoreProvider.getSentenceDeleteCount();
  }
  
  public long getInsertSQLStatementCount()
  {
	  return DataStoreProvider.getSentenceInsertCount();
  }
  
  public long getStoredProcedureCount()
  {
	  return DataStoreProvider.getSentenceCallCount();
  }  
  
  public long getSQLCommandCount()
  {
	  return DataStoreProvider.getSentenceDirectSQLCount();
  }
	
  public void dumpDataStoresInformation()
  {
	  DataStoreProvider.dump();
  }
}
