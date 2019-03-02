// $Log: GXDBMSinformix.java,v $
// Revision 1.10  2007/10/12 18:53:17  cmurialdo
// No se estaba respetando la propiedad lock timeout en informix.
//
// Revision 1.9  2007/07/01 18:54:05  iroqueta
// Implemento el getDBMSId para Oracle y dejo todo pronto para que se puede implementar en la clase correspondiente a cada DBMS.
//
// Revision 1.8  2006/12/18 16:34:17  alevin
// - (CMuriando) Implementacion de las propiedades "Lock time-out" y "Lock retry count".
//
// Revision 1.7  2004/09/20 11:35:09  iroqueta
// Comento el setCommitedChanges, porque causaba un error de cast cuando se ejecutaba como logged....
// Igual el setCommitedChanges se hace en el GXConnection.
//
// Revision 1.6  2004/09/17 21:45:34  dmendez
// Messagelist como estructura
// Soporte de updates optimizados (APC)
//
// Revision 1.5  2004/05/27 20:26:52  gusbro
// - Agrego getId que devuelve un entero identificando el DBMS
//
// Revision 1.4  2003/11/04 14:44:31  gusbro
// - Faltaba un caso duplicate al actualizar un campo unique
//
// Revision 1.3  2002/12/06 19:13:32  aaguiar
// - Se agrego el isAlive. Por ahora hace el getservertime()
//
// Revision 1.2  2002/07/04 21:54:40  gusbro
// Cambios para que compile en J#
//
// Revision 1.1.1.1  2002/05/15 21:43:58  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/05/15 21:43:58  gusbro
// GeneXus Java Olimar
//
package com.genexus.db.driver;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.Date;

import com.genexus.CommonUtil;
import com.genexus.CommonUtil;

public class GXDBMSinformix implements GXDBMS
{
	private static final boolean DEBUG       = com.genexus.DebugFlag.DEBUG;

	private boolean logged    = true;
	private boolean notlogged = true;
	private DataSource dataSource;

	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
		logged    = (dataSource.getInformixDB().equals(DataSource.INFORMIX_DB_LOGGED));
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


	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException
	{
		try
		{
			Method m = Class.forName("com.informix.jdbc.IfxPreparedStatement").getMethod("executeQuery", new Class[]{boolean.class});
			return (ResultSet)m.invoke(stmt, new Object[]{hold});
		}
		catch (Throwable e)
		{
			if (e instanceof SQLException)
			{
				throw (SQLException) e;
			}
		}

		return stmt.executeQuery();
	}

	public boolean DataTruncation(SQLException e)
	{
		return false;
	}

	public boolean useReadOnlyConnections()
	{
		return true;
	}

	public boolean EndOfFile(SQLException e)
	{
		return	(	e.getErrorCode() == 100		/* End of file */
				);
	}

	public boolean ReferentialIntegrity(SQLException e)
	{
		return false;
	}

	public boolean DuplicateKeyValue(SQLException e)
	{
		return	(	e.getErrorCode() == -239 ||	/* Could not insert row. Duplicate value in unique index column */
					e.getErrorCode() == -268 ||	/* Unique constraint violated */
					(e.getErrorCode() == -346 && e.getNextException() != null && /* Could not update row in table, en este caso veo el ISAM error  */
					 e.getNextException().getErrorCode() == -100) /* ISAM error: duplicate value for a record with unique key. */
				);
	}
	public boolean ObjectLocked(SQLException e)
	{
		return	(	e.getErrorCode() == -243  ||	/* Could not position within a table */
					e.getErrorCode() == -244  ||	/* Could not do a physical-order read to fetch next row */
					e.getErrorCode() == -245		/* Could not position within a file via an index */
				);
	}
	public boolean ObjectNotFound(SQLException e)
	{
		return	(	e.getErrorCode() == -206  ||	/* The specified table is not in the database */
					e.getErrorCode() == -319  ||	/* Index does not exist in ISAM file */
					e.getErrorCode() == -623		/* Unable to find constraint */
				);
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

	public void commit(Connection con) throws SQLException
	{
		con.commit();
		if	(logged)
		{
			if	(DEBUG)
				dataSource.getLog().log(0, "BEGIN WORK");

			Statement s1 = con.createStatement();
			s1.executeUpdate("BEGIN WORK");
			s1.close();
			//((GXConnection) con).setCommitedChanges();
		}
	}

	public void rollback(Connection con) throws SQLException
	{
		con.rollback();
		if	(logged)
		{
			if	(DEBUG)
				dataSource.getLog().log(0, "BEGIN WORK");

			Statement s1 = con.createStatement();
			s1.executeUpdate("BEGIN WORK");
			s1.close();
			//((GXConnection) con).setCommitedChanges();
		}
	}


	public void onConnection(GXConnection con) throws SQLException
	{
		// If it is using a Logged Informix DB, it must execute a Begin-Work to start each
		// transaction.

		// If it's running a reorganization and I do the 'Begin Work', it won't work, as I don't
		// execute the 'commit' at the end because is using autocommit.

		if	(dataSource.getInformixDB().equals(DataSource.INFORMIX_DB_LOGGED))// && !dataSource.forceAutocommit)
		{
			Statement s1 = con.createStatement();
			s1.executeUpdate("BEGIN WORK");
			s1.close();
			//((GXConnection) con).setCommitedChanges();
		}
		if	(dataSource.waitRecord > 0)
		{
			Statement s1 = con.createStatement();
			s1.executeUpdate("SET LOCK MODE TO WAIT " + (dataSource.waitRecord));
			s1.close();
			con.setCommitedChanges();
		}
		else
		{
			Statement s1 = con.createStatement();
			s1.executeUpdate("SET LOCK MODE TO WAIT");
			s1.close();
			con.setCommitedChanges();
		}
	}

	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT CURRENT YEAR TO SECOND FROM informix.SYSTABLES WHERE tabname = 'systables'", false).executeQuery();

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
		return !dataSource.getInformixDB().equals(DataSource.INFORMIX_DB_ANSI);
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
		return false;
	}

/* --- From WebLogic's Kona driver documentation

Using autocommit mode

Unlike other database system attributes, the autocommit mode of an Informix
database is not dynamically settable. It is defined when the database is
created. You can't change it with a call to the Connection.setAutoCommit
method. Only non-ANSI, non-logged databases support changing autocommit.

The JDBC spec says that the autocommit mode should be true by default, but
it is not possible to do this with Informix. All that you can do is
determine what the autocommit mode is. To change the autocommit state, you
must rebuild your database (for more information, see the information on
"CREATE DATABASE" in the Informix docs).

This affects how transactions and locking work. A JDBC program could behave
very differently depending on how the Informix database is created.

Before you depend on autocommit, you should know the setting of autocommit
for the database you'll be using. You can check its state with the
Connection.getAutoCommit() method, which returns true if autocommit is
enabled. For Informix, this method returns false by default for ANSI
databases; it may return true or false, depending on how the database was
created, for a non-ANSI database.

Here is what is supported in jdbcKona/Informix4 when you call the
Connection.setAutoCommit() method:

For ANSI databases, only autocommit=false is supported.
For non-ANSI databases, autocommit can be set to either true or false.
For non-ANSI databases without logging, only autocommit=true is supported.
Your program must then operate in accordance with the state of your Informix
database.

If you are using a non-ANSI database and you set autocommit to false, all
transactional SQL must be carried out using the Connection.commit() or
Connection.rollback() methods. You should never execute the explicit
transaction controls BEGIN WORK, COMMIT WORK, or ROLLBACK WORK calls on a
Statement, since jdbcKona/Informix4 uses transaction commands internally to
simulate an autocommit=false status. You should always control a transaction
using commit() and rollback() methods in the Connection class.

For non-ANSI databases without logging, autocommit=false cannot be
supported, since transactions are not supported. Consequently, only
autocommit=true is supported for use with such databases.

*/
	public boolean useStreamsInNullLongVarchar()
	{
		return true;
	}

	public boolean useStreamsInLongVarchar()
	{
		return true;
	}

	public int getId()
	{
		return DBMS_INFORMIX;
	}
        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }

	public boolean connectionClosed(SQLException e)
	{
		return (e.getErrorCode() == -79716);
	}		
}


