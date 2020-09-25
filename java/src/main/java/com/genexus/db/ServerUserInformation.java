package com.genexus.db;

import java.sql.SQLException;
import java.util.Date;
//import com.genexus.*;
import java.util.Enumeration;

import com.genexus.Application;
import com.genexus.ApplicationContext;
import com.genexus.ModelContext;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXConnection;

public final class ServerUserInformation extends UserInformation
{
  //	private static final boolean DEBUG       = DebugFlag.DEBUG;

	private String IP = "";
	private byte protocol;
	private long connectionTime;
	
	private static final Object lock = new Object();

	
	public ServerUserInformation(Namespace namespace)
	{
		super(namespace);
		this.connectionTime = System.currentTimeMillis();
		setTimestamp();
		if(ApplicationContext.getInstance().getReorganization())
		{
			String lang = ModelContext.getModelContext().getPreferences().getProperty("LANGUAGE", "eng");
			setLocalUtil('.', "", "", 40, lang);
		}
		else
		{
			if (!ApplicationContext.getInstance().isApplicationServer() && ModelContext.getModelContext().getHttpContext() != null){
				setLocalUtil(   Application.getClientPreferences().getDECIMAL_POINT(),
							Application.getClientPreferences().getDATE_FMT(),
							Application.getClientPreferences().getTIME_FMT(),
							Application.getClientPreferences().getYEAR_LIMIT(),
							Application.getClientPreferences().getLANGUAGE());
			}else
			{
				setLocalUtil('.', "", "", 40, "eng");
			}
		}
	}

	public void disconnect() throws SQLException
	{
                SQLException disconnectException = null;
		for	(Enumeration<DataSource> en = namespace.getDataSources(); en.hasMoreElements(); )
                {
                  try
                  {
                    en.nextElement().disconnect(handle);
                  }
                  catch (SQLException e)
                  {
                    disconnectException = e;
                  }
				  catch (Throwable e)
				  {
				  }
		}


		if (disconnectException != null)
			throw disconnectException;
	}

	public void flushBuffers(java.lang.Object o) throws SQLException
	{
		for	(Enumeration<DataSource> en = namespace.getDataSources(); en.hasMoreElements(); ) {
			en.nextElement().flushBuffers(handle, o);
		}
	}

	public boolean isConnected(String dataSourceName)
	{
		ConnectionInformation info = (ConnectionInformation) connections.get(dataSourceName);
		return info != null && info.rwConnection != null;
	}

	public void disconnectOnException() throws SQLException
	{
		for	(Enumeration<DataSource> en = namespace.getDataSources(); en.hasMoreElements(); )
			en.nextElement().disconnectOnException(handle);

	}
	

	// La sincronizacion de esto est� en DBConnectionManager...
 	GXConnection getConnection(ModelContext context, String dataSourceName, boolean readOnly, boolean sticky) throws SQLException
	{
		setTimestamp();

	 	ConnectionInformation info = (ConnectionInformation) connections.get(dataSourceName);

		if	(info == null)
		{
			throw new RuntimeException("Can't find connection information for datastore " + dataSourceName);
		}

	
		DataSource dataSource = null;
		DataSource dataSourceAux;
		String defaultUser = null;
		String defaultPassword = null;
		String jdbcUrl = null;
		boolean usesJdbcDataSource = false;

		synchronized (lock) 
		{
			dataSourceAux = (DataSource) context.beforeGetConnection(handle, getNamespace().getDataSource(dataSourceName));
			if (dataSourceAux != null)
			{
				dataSource = dataSourceAux.copy();
				defaultUser = dataSource.defaultUser;
				defaultPassword = dataSource.defaultPassword;
				jdbcUrl = dataSource.jdbcUrl;
				usesJdbcDataSource = dataSource.usesJdbcDataSource();
			}
		}

		GXConnection con;
		if (dataSourceAux == null)
		{
			con = (GXConnection) getNamespace().getDataSource(dataSourceName).getConnectionPool().checkOut(context, null, handle, getUser(dataSourceName), getPassword(dataSourceName), readOnly, sticky);
			if (con.getPreviousHandle() != handle)
			{
				context.afterGetConnection(handle, getNamespace().getDataSource(dataSourceName));			
			}
		}
		else
		{
			if (usesJdbcDataSource)
			{
				con = (GXConnection) dataSource.getConnectionPool().checkOut(context, dataSource, handle, defaultUser, defaultPassword, readOnly, sticky);				
			}
			//Si se ejecuto el evento before connect y se use el pool de GX tengo que hacer un Hash
			//de pools indexando por el jdbcURL + User que son los parametros que se pueden cambiar
			else
			{
				con = (GXConnection) dataSource.getConnectionPool(jdbcUrl + defaultUser + defaultPassword).checkOut(context, dataSource, handle, defaultUser, defaultPassword, readOnly, sticky);								
			}
			if (con.getPreviousHandle() != handle)
			{
				context.afterGetConnection(handle, dataSource);			
			}
		}
		putConnection(dataSourceName, readOnly?(GXConnection) info.rwConnection:con, readOnly?con:(GXConnection) info.roConnection, info.user, info.password);

		return con;
	}

	public synchronized long getConnectionTime()
	{
		return connectionTime;
	}

	public String getIP()
	{
		return IP;
	}

	public void setIP(String IP)
	{
		this.IP = IP;
	}

	public byte getProtocol()
	{
		return protocol;
	}

	public void setProtocol(byte protocol)
	{
		this.protocol = protocol;
	}
	
///////////////////////////////////////JMX operations//////////////////////////////////////	
	public Date getConnectedSince()
	{
		return new Date(getConnectionTime());
	}
  
	public long getIdleSeconds()
	{
		return (System.currentTimeMillis() - getTimestamp()) / 1000;
	}		
  
	public void disconnectUser()
	{
	  try
	  {
		DBConnectionManager.getInstance().disconnect(getHandle());
	  }
	  catch(java.sql.SQLException e)
	  {
		  System.out.println(e.toString());
	  }
	}
}
