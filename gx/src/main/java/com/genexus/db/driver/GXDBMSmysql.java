// $Log: GXDBMSmysql.java,v $
// Revision 1.11  2006/12/18 16:34:17  alevin
// - (CMuriando) Implementacion de las propiedades "Lock time-out" y "Lock retry count".
//
// Revision 1.9  2005/08/02 18:08:07  gusbro
// - Saco un mensaje de debug que estaba 'colado' desde rev 1.2
//
// Revision 1.8  2005/07/22 23:52:23  gusbro
// - Agrego caso para ignorar el 'create database' error
//
// Revision 1.7  2005/04/05 17:53:29  iroqueta
// En el metodo ObjectNotFound se agrega tambien el error 1025 para el caso del DROP FOREIGN KEY cuando no existe.
// El error que retorna el MySQL no es del todo correcto no deberia de ser 1025 porque ese se refiere al rename.
//
// Revision 1.6  2004/09/17 21:45:34  dmendez
// Messagelist como estructura
// Soporte de updates optimizados (APC)
//
// Revision 1.5  2004/05/27 20:26:52  gusbro
// - Agrego getId que devuelve un entero identificando el DBMS
//
// Revision 1.4  2004/05/05 17:50:29  dmendez
// El error 1064 aparecia como error de acceso pero tambien es un sintaz error asi que se saco.
//
// Revision 1.3  2004/04/29 20:37:53  dmendez
// ObjectLocked retornaba true haciendo que los errores de update reintentaran indefinidamente.
//
// Revision 1.2  2004/04/28 22:33:19  dmendez
// Se maneja problema con commit/rollback si autocommit=true
//
// Revision 1.1  2004/04/27 21:30:11  dmendez
// soporte mysql
//

package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSmysql implements GXDBMS
{
	private boolean forceAutoCommit;

	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
	{
		return stmt.executeQuery();
	}

	public boolean isAlive(GXConnection con)
	{
		try
		{
			serverDateTime(con);
		}
		catch (SQLException e)
		{
			return false;
		}

		return true;
	}

	private String dbName = "";

	public void setDatabaseName(String dbName)
	{
		this.dbName = dbName;
	}
	public String getDatabaseName()
	{
		return this.dbName;
	}
	
	public void setInReorg()
	{
		forceAutoCommit = true;
	}

	public boolean DataTruncation(SQLException e)
	{
		if (e != null && e.getSQLState() != null)
		{
			return (e.getErrorCode() == 0 && (e.getSQLState().equals("01004") || e.getSQLState().equals("22001")));
		}
		return false;
	}

	private DataSource dataSource;
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean EndOfFile(SQLException e)
	{
		return (e.getErrorCode() == 100);
	}
	
	public boolean ReferentialIntegrity(SQLException e)
	{
		return (e.getErrorCode() == 1216);
	}
	
	public boolean DuplicateKeyValue(SQLException e)
	{
		return (e.getErrorCode() == 1062);
	}

	public boolean MaskedFileNotFound(SQLException e)
	{
		return (e.getErrorCode() == 1051) || (e.getErrorCode() == 1091) || (e.getErrorCode() == 1146);
	}

	public boolean ObjectLocked(SQLException e)
	{
		return false;
	}

	public boolean ObjectNotFound(SQLException e)
 	{
		return (e.getErrorCode() == 1025) || 
			   (e.getErrorCode() == 1051) || 
			   (e.getErrorCode() == 1091) || 
			   (e.getErrorCode() == 1146) ||
			   (e.getErrorCode() == 1006) ||  /* Can't create database */
			   (e.getErrorCode() == 1007) ||  /* Can't create database. database exists */
			   (e.getErrorCode() == 1360);    /* Trigger does not exist */
	}

	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1000, 1, 1, 0, 0, 0);
	}

	public boolean useDateTimeInDate()
	{
		return false;
	}

	public boolean useCharInDate()
	{
		return false;
	}

	public void setConnectionProperties(java.util.Properties props)
	{
		if (forceAutoCommit)
		{
			props.put("relaxAutoCommit", "true" +  dataSource.jdbcAS400Lib);
		}
	}

	public void onConnection(GXConnection con) throws SQLException
	{
	}

	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT NOW()", false).executeQuery();

		rslt.next();
		Date value = rslt.getTimestamp(1);
		rslt.close();

		return value;
	}
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerVERSION_", "SELECT VERSION()", false).executeQuery();
		
		rslt.next();
		String value = rslt.getString(1);
		rslt.close();

		return value;
	}	
	
	public String connectionPhysicalId(GXConnection con)
	{
		try
		{
			ResultSet rslt = con.getStatement("_ConnectionID_", "SELECT CONNECTION_ID()", false).executeQuery();

			rslt.next();
			String value = rslt.getString(1);
			rslt.close();

			return value;
		}
		catch (SQLException e)
		{
			return "";
		}
	}	

	public boolean getSupportsAutocommit()
	{
		return true;
	}

	public void commit(Connection con) throws SQLException
	{
		if (con.getAutoCommit() == false)
			con.commit();
	}

	public void rollback(Connection con) throws SQLException
	{
		if (con.getAutoCommit() == false)
			con.rollback();
	}

	public boolean useReadOnlyConnections()
	{
		return true;
	}

	public boolean ignoreConnectionError(SQLException e)
	{
		return false;
	}
        
        public boolean rePrepareStatement(SQLException e)
        {
            return (e.getErrorCode() == 1615);
        }

	public boolean getSupportsQueryTimeout()
	{
		return true;
	}

	public boolean useStreamsInNullLongVarchar()
	{
		return false;
	}

	public boolean useStreamsInLongVarchar()
	{
		return false;
	}

	public int getId()
	{
		return DBMS_MYSQL;
	}

        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return (e.getSQLState() == "08S01");
	}		
}


