package com.genexus.management;

import com.genexus.db.driver.DataSource;

public class DataSourceJMX implements DataSourceJMXMBean{
	
	private DataSource dataSource;
	

  public DataSourceJMX(DataSource dataSource)
  {
    this.dataSource = dataSource;
  }

  static public void CreateDataSourceJMX(DataSource dataSource)
  {
    try
    {
      MBeanUtils.createMBean(dataSource);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register Datasource MBean."+e.toString());
    }
  }

  public String getName()
  {
	  return dataSource.name;
  }
  
  public String getUserName()
  {
	  return dataSource.defaultUser;
  }
  
  public String getJDBCDriver()
  {
	  return dataSource.jdbcDriver;
  }
  
  public String getJDBCURL()
  {
	  return dataSource.jdbcUrl;
  }
  
  public int getMaxCursors()
  {
	  return dataSource.maxCursors;
  }

  /*
  public int getROPoolUsers()
  {
	  return dataSource.roPoolUsers;
  }
  
  public boolean getROPoolEnabled()
  {
	  return dataSource.getROPoolEnabled();
  }
  
  public boolean getROPoolRecycle()
  {
	  return dataSource.getROPoolRecycle();
  }
  
  public int getROPoolRecycleMins()
  {
	  return dataSource.getROPoolRecycleMins();
  }
  */
  
  public boolean getPoolEnabled()
  {
	  return dataSource.getRWPoolEnabled();
  }
  
  public boolean getPoolRecycleEnabled()
  {
	  return dataSource.getRWPoolRecycle();
  }
  
  public int getPoolRecyclePeriod()
  {
	  return dataSource.getRWPoolRecycleMins();
  }
  
  public boolean getConnectAtStartup()
  {
	  return dataSource.connectStartup;
  }
  
  /*
  public void ROPoolRecycle()
  {
	  dataSource.ROPoolRecycle();
  }
  */
  
  public void RecyclePool()
  {
	  dataSource.RWPoolRecycle();
  }
 
}
