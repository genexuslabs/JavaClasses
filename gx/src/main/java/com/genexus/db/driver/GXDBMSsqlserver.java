// $Log: GXDBMSsqlserver.java,v $
// Revision 1.12  2006/12/18 16:34:17  alevin
// - (CMuriando) Implementacion de las propiedades "Lock time-out" y "Lock retry count".
//
// Revision 1.11  2006/03/14 17:54:05  iroqueta
// No se hace rollback si se esta en autocommit y se esta usando el driver de jtds o el driver de Ms para SQL 2005
//
// Revision 1.10  2005/11/28 17:26:49  iroqueta
// Hago lo mismo que el put 1.6 para cuando se usan los drivers de Ms sobre SQLServer 2005
//
// Revision 1.9  2005/09/23 12:33:41  iroqueta
// Estaba mal el nombre de la clase del driver en el put anterior
//
// Revision 1.8  2005/09/22 20:41:26  iroqueta
// Se usan metodos de datetime para setear fechas en SQLServer cuando se usa el driver jdbc de Merant
//
// Revision 1.7  2005/07/22 23:52:23  gusbro
// - Agrego caso para ignorar el 'create database' error
//
// Revision 1.6  2005/04/28 17:03:01  gusbro
// - Con los drivers jtds si tengo autocommit en la conexion no hago commit
//
// Revision 1.5  2004/09/17 21:45:34  dmendez
// Messagelist como estructura
// Soporte de updates optimizados (APC)
//
// Revision 1.4  2004/05/27 20:26:52  gusbro
// - Agrego getId que devuelve un entero identificando el DBMS
//
// Revision 1.3  2002/12/06 19:13:32  aaguiar
// - Se agrego el isAlive. Por ahora hace el getservertime()
//
// Revision 1.2  2002/11/25 20:15:00  aaguiar
// - Se usan metodos de datetime para setear fechas en SQLServer cuando se usa el driver jdbc de Microsoft. Ese driver pone un valor en la parte hora de los datetimes cuando se hace un setDate
//
// Revision 1.1.1.1  2002/05/15 21:15:34  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/05/15 21:15:34  gusbro
// GeneXus Java Olimar
//
//
//   Rev 1.6   23 Sep 1998 19:48:14   AAGUIAR
//
//   Rev 1.5   May 28 1998 10:00:08   DMENDEZ
//Sincro28May1998

package com.genexus.db.driver;

import com.genexus.CommonUtil;
import java.sql.*;
import java.util.Date;

public class GXDBMSsqlserver implements GXDBMS
{	
	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
	{
		return stmt.executeQuery();
	}

	public void setDatabaseName(String dbName)
	{
	}
	public String getDatabaseName()
	{
		return "";
	}	
	public void setInReorg()
	{
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


	public boolean DataTruncation(SQLException e)
	{
		return false;
	}

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public boolean useReadOnlyConnections()
	{
		return true;
	}

	public boolean EndOfFile(SQLException e)
	{
		return	(	e.getErrorCode() == 100
				);
	}

	public boolean ReferentialIntegrity(SQLException e)
	{
		return (e.getErrorCode() == 547);
	}

	public boolean DuplicateKeyValue(SQLException e)
	{
		return	(	e.getErrorCode() == 2627 ||		/*	Violation of %s constraint '%.*s': Attempt to insert duplicate key in object '%.*s'.*/
 					e.getErrorCode() == 2601 			/* Attempt to insert duplicate key row in object '%.*s' with unique index '%.*s'*/
				);
	}

	public boolean ObjectLocked(SQLException e)
	{
		return	(	
					e.getErrorCode() == 1000 ||  /* "Lock request time-out period exceeded"  */
					e.getErrorCode() == 1222 ||  /* "Lock request time-out period exceeded"  */
					e.getErrorCode() == 903 
				);
	}

	public boolean ObjectNotFound(SQLException e)
	{
		return	(	e.getErrorCode() == 3704 ||	/* Table not found */
					e.getErrorCode() == 3703 ||	/* Index not found */
					e.getErrorCode() == 3701 ||	/* Index not found */
					e.getErrorCode() == 4902 ||	/* Table not found in Alter statement */
					e.getErrorCode() == 3728 ||	/*  .. is not a constraint.*/
					e.getErrorCode() == 3727 ||	/* Constraint not found */
					e.getErrorCode() == 1801 || /* Database already exists */
					e.getErrorCode() == 15032   /* Database already exists */
				);
	}

	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1753, 1, 1, 0, 0, 0);
	}

	public boolean useDateTimeInDate()
	{
		if	(dataSource.jdbcDriver.startsWith("com.microsoft.jdbc.sqlserver") || 
			 dataSource.jdbcDriver.startsWith("com.ddtek.jdbc.sqlserver.SQLServerDriver"))
			return true;

		return false;
	}

	public boolean useCharInDate()
	{
		return false;
	}

	public void setConnectionProperties(java.util.Properties props)
	{
	}

	public boolean allowsCommitWithAutocommit = true;
	public void onConnection(GXConnection con) throws SQLException
	{
		if	(dataSource.waitRecord > 0)
		{
			Statement s1 = con.createStatement();
			s1.executeUpdate("SET LOCK_TIMEOUT " + (dataSource.waitRecord * 1000));
			s1.close();
			con.setCommitedChanges();
		}
																															
		if(con.getDataSource().jdbcDriver.startsWith("net.sourceforge.jtds.") || con.getDataSource().jdbcDriver.startsWith("com.microsoft.sqlserver.jdbc.SQLServerDriver"))
		{
			// @gusbro 28/04/05
			// Los drivers de jtds y los de Ms para SQLServer 2005 no permiten ejecutar un commit si autocommit esta en on			
			// Nota: por ahora no podemos directamente NO hacer el commit en todos los casos
			// porque por ejemplo con los drivers de I-net (una2000) al crear una tabla nueva en 
			// la reorg si no hacemos un commit (aunque haya autocommit) NO anda!
			allowsCommitWithAutocommit = false;
		}
	}
	
	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT GETDATE()", false).executeQuery();
		
		rslt.next();
		Date value = rslt.getTimestamp(1);
		rslt.close();

		return value;
	}
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerVERSION_", "SELECT CAST(SERVERPROPERTY('ResourceVersion') AS VARCHAR(20)), CAST(SERVERPROPERTY('productversion') AS VARCHAR(20))", false).executeQuery();
		
		rslt.next();
		String value = rslt.getString(1);
		if (rslt.wasNull())
		{
			value = rslt.getString(2);
		}
		rslt.close();

		return value.replaceAll("10.", "9.");
	}
	
	public String connectionPhysicalId(GXConnection con)
	{
		try
		{
			ResultSet rslt = con.getStatement("_ConnectionID_", "SELECT @@spid", false).executeQuery();

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
		if(allowsCommitWithAutocommit || !con.getAutoCommit())
		{
			con.commit();
		}		
	}

	public void rollback(Connection con) throws SQLException
	{
		if(allowsCommitWithAutocommit || !con.getAutoCommit())
		{
			con.rollback();
		}
	}

	public boolean ignoreConnectionError(SQLException e)
	{
		return false;
	}
        
        public boolean rePrepareStatement(SQLException e)
        {
            return false;
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
		return DBMS_SQLSERVER;
	}
        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return (e.getSQLState() == "08S01");
	}		
}
