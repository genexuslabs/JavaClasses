// $Log: GXDBMSdb2.java,v $
// Revision 1.7  2006/12/18 16:34:17  alevin
// - (CMuriando) Implementacion de las propiedades "Lock time-out" y "Lock retry count".
//
// Revision 1.6  2004/09/17 21:45:34  dmendez
// Messagelist como estructura
// Soporte de updates optimizados (APC)
//
// Revision 1.5  2004/05/27 20:26:52  gusbro
// - Agrego getId que devuelve un entero identificando el DBMS
//
// Revision 1.4  2002/12/06 19:13:32  aaguiar
// - Se agrego el isAlive. Por ahora hace el getservertime()
//
// Revision 1.3  2002/11/25 20:16:49  aaguiar
// - No se usa el java.sq.timestamp para los dates porque no funcionaba en algunos timezones
//
// Revision 1.2  2002/08/28 14:17:14  aaguiar
// - Se usa java.sql.Timestamp para obtener el nullvalue de la fecha en DB2
//
// Revision 1.1.1.1  2002/05/15 21:15:16  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/05/15 21:15:16  gusbro
// GeneXus Java Olimar
//
//
//   Rev 1.6   23 Sep 1998 19:48:14   AAGUIAR
//
//   Rev 1.5   May 28 1998 10:00:04   DMENDEZ
//Sincro28May1998

package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSdb2 implements GXDBMS
{	
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

	public boolean DataTruncation(SQLException e)
	{
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
		return (e.getErrorCode() == -530);
	}

	public boolean DuplicateKeyValue(SQLException e)
	{
		return (e.getErrorCode() == -803);
	}
	
	public boolean MaskedFileNotFound(SQLException e)
	{
		return (e.getErrorCode() == -204);
	}	

	public boolean ObjectLocked(SQLException e)
	{
		// Quiza es -911 .. asi est� en VB, averiguar.
		return (e.getErrorCode() == -903);
	}
	
	public boolean ObjectNotFound(SQLException e)
 	{
		return (e.getErrorCode() == -204);
	}

	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1, 1, 1, 0, 0, 0);
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
	}

	public void onConnection(GXConnection con) throws SQLException
	{
	}
	
	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT CURRENT TIMESTAMP FROM SYSIBM.SYSTABLES WHERE NAME = 'SYSTABLES' AND CREATOR = 'SYSIBM'", false).executeQuery();
		
		rslt.next();
		Date value = rslt.getTimestamp(1);
		rslt.close();

		return value;
	}
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		return "";		
	}	

	public String connectionPhysicalId(GXConnection con)
	{
		return "";
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
		return DBMS_DB2;
	}

        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return (e.getSQLState() == "08S01");
	}		
}


