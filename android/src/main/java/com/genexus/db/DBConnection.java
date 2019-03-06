package com.genexus.db;
/** Encapsula el tipo de dato DBConnection de GX
 * $Log: DBConnection.java,v $
 * Revision 1.7  2005/05/04 21:46:31  iroqueta
 * Hago llegar el contexto al constructor del GXConnection
 *
 * Revision 1.6  2005/05/04 20:00:06  iroqueta
 * Se agregan las propiedades ExternalDatasourceName y UseExternalDatasource para el tipo de datos DBConnection.
 * Ademas se puse el metodo getDataSource para poder ser llamado desde el GXConnection en el beforeConnect
 *
 * Revision 1.5  2005/02/16 19:35:43  iroqueta
 * Paso el LoginInServer al datastore, antes estaba en el namespace
 *
 * Revision 1.4  2003/05/22 20:33:43  gusbro
 * - Agrego soporte para poder cambiar de DB con el AS
 *
 * Revision 1.3  2003/05/08 16:39:11  gusbro
 * - Faltaban 2 metodos y se cambio el tipo de dato de retorno de int a short
 *
 * Revision 1.2  2003/05/06 17:28:33  gusbro
 * - Los metodos connect() y disconnect() ahora retornan el codigo de error o 0 si la operacion tuvo �xito
 *
 * Revision 1.1  2003/04/30 22:37:09  gusbro
 * - Clase para encapsular el tipo de dato DBConnection de GX
 *
 */

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
