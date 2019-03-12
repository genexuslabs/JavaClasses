package com.genexus.db;
import java.sql.SQLException;

import com.genexus.Application;
import com.genexus.db.driver.DataSource;
import com.genexus.util.ReorgSubmitThreadPool;

public class DBConnection
{
	protected DataSource dataSource;
	private int handle;
	private String errMsg = "";
	private int errCode = 0;
	private short showPrompt = 3;

	public static DBConnection getDataStore(String dataSourceName, int handle)
	{
		return new DBConnection(DBConnectionManager.getInstance().getDataSource(handle, dataSourceName), handle);
	}

	public DBConnection()
	{
		errMsg = "Invalid DBConnection instance";
		errCode = 1;
	}

	public DBConnection(DataSource dataSource, int handle)
	{
		if (dataSource == null)
		{
			errMsg = "Invalid DBConnection instance";
			errCode = 1;			
		}
		else
		{
			this.dataSource = dataSource;
			this.handle = handle;
		}
	}

	public String getJdbcdrivername()
	{
		clearErr();
		return dataSource.jdbcDriver;
	}

	public String getDatabasename()
	{
		return dataSource.jdbcDBName;
	}
	public void setDatabasename(String dbName)
	{
		String url = getJdbcdriverurl();
		if (url != null)
		{
			
			int index = 0;
			
				index = url.lastIndexOf(dataSource.jdbcDBName);
			
			if (index >= 0)
			{
				url = url.substring(0, index) + dbName + url.substring(index + dataSource.jdbcDBName.length());
				setJdbcdriverurl(url);
			}
		}
		dataSource.jdbcDBName = dbName;
		dataSource.dbms.setDatabaseName(dbName);
	}

	public void setJdbcdrivername(String driverName)
	{
		dataSource.jdbcDriver = driverName;
	}

	public String getJdbcdriverurl()
	{
		clearErr();
		return dataSource.jdbcUrl;
	}

	public void setJdbcdriverurl(String url)
	{
		dataSource.jdbcUrl = url;
	}

        public boolean getUseexternaldatasource()
        {
          return false;
        }

        public void setUseexternaldatasource(int value)
        {
        }

        public String getExternaldatasourcename()
        {
          return dataSource.jdbcDataSource;
        }

        public void setExternaldatasourcename(String value)
        {
          dataSource.jdbcDataSource = value;
        }

	public String getDatastorename()
	{
		clearErr();
		return dataSource.name;
	}

	private void clearErr()
	{
		errMsg = "";
		errCode = 0;
	}

	public void setUserName(String userName)
	{
		clearErr();
		dataSource.defaultUser = userName;
	}

	public String getUserName()
	{
		clearErr();
		return dataSource.defaultUser;
	}

	public void setUserpassword(String userPassword)
	{
		clearErr();
		dataSource.defaultPassword = userPassword;
	}

	public String getUserpassword()
	{
		clearErr();
		return dataSource.defaultPassword;
	}

	public void setShowprompt(short showPrompt)
	{
		clearErr();
		this.showPrompt = showPrompt;

		dataSource.loginInServer = (showPrompt == 2);
	}

	public short getShowprompt()
	{
		return showPrompt;
	}

	public String getErrDescription()
	{
		return errMsg;
	}

	public short getErrCode()
	{
		return (short)errCode;
	}

	public short disconnect()
	{
		clearErr();
		try
		{
			DBConnectionManager.getInstance().getUserInformation(handle).disconnect();
		}catch(SQLException e)
		{
			errMsg = e.getMessage();
			errCode = e.getErrorCode();
		}
		return (short)errCode;
	}

	public short connectDontShowErrors()
	{
		Application.setShowConnectError(false);
		short errCode = connect();
		Application.setShowConnectError(true);
		return errCode;
	}
	
	public short connect()
	{
		if (ReorgSubmitThreadPool.hasAnyError())
		{
			errCode = 3;
			return (short)errCode;
		}
				
		clearErr();
		try
		{
			UserInformation info = DBConnectionManager.getInstance().getUserInformation(handle);
			if (info instanceof LocalUserInformation)
			{
				((UserInformation)info).createConnectionBatch(null, this);
			}
		}catch(SQLException e)
		{
			errMsg = e.getMessage() + " - code:" + e.getErrorCode();
			errCode = 3;
		}catch(RuntimeException e)
		{
			errMsg = e.getMessage();
			errCode = 3;
		}
		
		if (errCode != 0 && Application.getShowConnectError())
		{
			ReorgSubmitThreadPool.setAnError();
		}
		
		return (short)errCode;
	}

        public DataSource getDataSource()
        {
          return dataSource;
        }

	// Metodos que solo aplican a AS/400

	public void setConnectiondata(String asLibName)
	{
		clearErr();
		dataSource.jdbcDBName = asLibName;
		dataSource.dbms.setDatabaseName(asLibName);
	}
	public String getConnectiondata()
	{
		return dataSource.jdbcDBName;
	}

	// Metodos que no aplican a Java

	public void setConnectionmethod(short method) { ; }
	public short getConnectionmethod(){ return 0; }
	public void setOdbcdatasourcename(String ds){ ; }
	public String getOdbcdatasourcename(){ return ""; }
	public void setOdbcdrivername(String dn){ ; }
	public String getOdbcdrivername(){ return ""; }
	public void setOdbcfiledatasourcename(String ds){ ; }
	public String getOdbcfiledatasourcename(){ return ""; }
}
