// $Log: GXDBMSas400.java,v $
// Revision 1.11  2006/12/18 16:34:17  alevin
// - (CMuriando) Implementacion de las propiedades "Lock time-out" y "Lock retry count".
//
// Revision 1.10  2005/11/30 20:13:04  iroqueta
// Hago que la lib que se puso como databaseName sea la primera en la lista de library list.
// Asi se usa como schema default (segun la docum del driver).
//
// Revision 1.9  2005/11/17 18:17:20  iroqueta
// Agrego a las propiedades de conexion el date format en iso.
// Sino los campos de tipo fecha que quedan grabados con la fecha nula se recuperan como NULL en lugar de Empty.
//
// Revision 1.8  2005/09/02 21:59:46  gusbro
// - En la serverDateTime faltaba hacer un setNotInUse dado que no se
//   hace mas en el GXCallableStatement
//
// Revision 1.7  2005/03/09 17:39:22  iroqueta
// Comento la linea props.put("package cache", "true"); porque puede traer problemas de memoria en el cliente
//
// Revision 1.6  2004/09/17 21:45:34  dmendez
// Messagelist como estructura
// Soporte de updates optimizados (APC)
//
// Revision 1.5  2004/05/27 20:26:52  gusbro
// - Agrego getId que devuelve un entero identificando el DBMS
//
// Revision 1.4  2004/03/16 16:38:51  gusbro
// - Coloco las libs en la reorg
//
// Revision 1.3  2003/10/17 20:06:13  gusbro
// - Cambios para soportar date datatype=DATE en as400
//
// Revision 1.2  2002/12/06 19:13:32  aaguiar
// - Se agrego el isAlive. Por ahora hace el getservertime()
//
// Revision 1.1.1.1  2002/05/15 21:15:06  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/05/15 21:15:06  gusbro
// GeneXus Java Olimar
//
//
//   Rev 1.6   23 Sep 1998 19:48:14   AAGUIAR
//
//   Rev 1.5   May 28 1998 10:00:04   DMENDEZ
//Sincro28May1998

package com.genexus.db.driver;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.genexus.CommonUtil;

public class GXDBMSas400 implements GXDBMS
{	
	private boolean useCharInDate = true;
	private DataSource dataSource;
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
		useCharInDate = !dataSource.getAS400DateType().equalsIgnoreCase("Date");
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
		return stmt.executeQuery();
	}

	public boolean DataTruncation(SQLException e)
	{
		return (e.getErrorCode() == 01004 );
	}

	public boolean ReferentialIntegrity(SQLException e)
	{
		return false;
	}

	public boolean EndOfFile(SQLException e)
	{
		return (e.getErrorCode() == 100);
	}
	public boolean DuplicateKeyValue(SQLException e)
	{
		return (e.getErrorCode() == -803);
	}
	public boolean ObjectLocked(SQLException e)
	{
		return (e.getErrorCode() == -913);
	}
	public boolean ObjectNotFound(SQLException e)
 	{
		// Porque est� el -913 ac�? Esto implica que si hago el LOCK TABLE no cancele si est� en uso...
		// Por ahora lo dejo porque es bastante inofensivo (el error da despues), pero habria que
		// ver porque est�.
		return (e.getErrorCode() == -204 || MaskedDatabaseAlreadyExists(e) || e.getErrorCode() == -913 || e.getErrorCode() == 7905);
	}
	public java.util.Date nullDate()
	{
		return CommonUtil.ymdhmsToT_noYL(1, 1, 1, 0, 0, 0);
	}

	public boolean MaskedFileNotFound(SQLException e)
	{
		return (e.getErrorCode() == -204);
	}	

	public boolean MaskedDatabaseAlreadyExists(SQLException e)
	{
		return (e.getErrorCode() == -601);
	}	
	
	public boolean useDateTimeInDate()
	{
		return false;
	}

	public boolean useCharInDate()
	{
		return useCharInDate;
	}

	private static String ibmDrivers = "com.ibm.";
	private boolean inReorg = false;
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
		inReorg = true;		
	}

	public void setConnectionProperties(java.util.Properties props)
	{
		if	(dataSource.jdbcDriver.startsWith(ibmDrivers))
		{
			//Si el string de conexion no dice nada del date format se lo pongo en iso
			if (dataSource.jdbcUrl.indexOf("date format") == -1)
			{
				props.put("date format", "iso");
			}
			props.put("naming", "system");
			props.put("extended dynamic", "true");
			props.put("package", dataSource.getAS400Package());
			//props.put("package cache", "true");

			if	(com.genexus.ApplicationContext.getInstance().getReorganization())
			//if	(inReorg)
			{
				props.put("libraries", "*LIBL " +  dataSource.jdbcAS400Lib);
//				props.put("libraries", dbName);
//				System.out.println(" LIB " + dbName);
			}
			else
			{
				// Esto es para que no de el error de 'libraries not added to the list'
				if	(dataSource.jdbcAS400Lib.indexOf(dbName) >= 0)
				{
					props.put("libraries", "*LIBL " + dataSource.jdbcAS400Lib);
				}
				else
				{
					props.put("libraries", dbName + " " + "*LIBL " + dataSource.jdbcAS400Lib);
				}
			}

		}
	}

	public void onConnection(GXConnection con) throws SQLException
	{
		/* Al momento de realizar la conexi�n con el AS/400 es necesario ejecutar 
		   en forma inmediata las sentencias "CREATE TABLE QTEMP/GX (C CHAR)" y 
		   "INSERT INTO QTEMP/GX (C) VALUES ('1')". Esta tabla es temporaria y 
		   �nica por conexi�n (caracter�sticas de la biblioteca QTEMP del AS/400). 
		
		try
		{
			Statement s1 = con.createStatement();
			s1.executeUpdate("CREATE TABLE QTEMP/GX (C CHAR)");
			s1.close();

			Statement s2 = con.createStatement();
			s2.executeUpdate("INSERT INTO QTEMP/GX (C) VALUES ('1')");
			s2.close();
		}
		catch (SQLException e)
		{
		}
		*/
	}

	private static boolean serverTimeProcedureCreated = false;	
	public java.util.Date serverDateTime(GXConnection con) throws SQLException
	{
		boolean uncommitedChanges = con.getUncommitedChanges();

		try
		{
			if	(!serverTimeProcedureCreated)
			{
				Statement stmt = con.createStatement();

				try
				{
					stmt.executeUpdate("CREATE PROCEDURE QWCCVTDT (IN CHAR(10), IN CHAR(17), IN CHAR(10), INOUT CHAR(17), INOUT CHARACTER(1)) (EXTERNAL NAME QSYS/QWCCVTDT GENERAL)");
				}
				catch (SQLException sqlException)
				{
					// Ignoro cualquier error. Si me tenia que dar, me va a dar un error en alguno de
					// los siguientes.
				}
				finally 
				{
					// Esto lo hago tanto si da error como no, dado que no tiene sentido volver a hacerlo.
					serverTimeProcedureCreated = true;
					if	(stmt != null)
					{
						try
						{
							stmt.close();
						}
						catch (SQLException e)
						{
						}
					}
				}
			}

			CallableStatement cstmt = con.getCallableStatement("_serverDT", "CALL QWCCVTDT (?,?,?,?,?)");

		    cstmt.registerOutParameter( 4 , Types.CHAR);
		    cstmt.registerOutParameter( 5 , Types.CHAR);

			cstmt.setString(1, "*CURRENT");
			cstmt.setString(2, " ");
			cstmt.setString(3, "*YYMD");
			cstmt.setString(4, "");
			cstmt.setString(5, "");

			cstmt.execute();

			if	(!uncommitedChanges)
			{
				con.setCommitedChanges();
			}

			String result = cstmt.getString(4);
			// Indico que la sentencia no esta en uso
			// Esto antes se hacia automaticamente en el execute del GXCallableStatement 
			// pero desde el 20/04/05 no se hace m�s alli
			((GXCallableStatement)cstmt).setNotInUse(); 
			
			return CommonUtil.ymdhmsToT_noYL(result);
		}
		catch (SQLException e)
		{							 
			con.setError();
			throw e;
		}
	}
	
	public String serverVersion(GXConnection con) throws SQLException
	{
		return "";		
	}	
	
	public String connectionPhysicalId(GXConnection con)
	{
		try
		{
			Class c = Class.forName("com.ibm.as400.access.AS400JDBCConnection");
			Method m = c.getMethod("getServerJobIdentifier", (Class[])null);
			return (String)m.invoke(con.getJDBCConnection(), (Object[])null);
		}
		catch(Exception e)
		{
			System.out.println(e);
			return "";
		}
	}	

	public boolean getSupportsAutocommit()
	{
		return true;
	}								   

	public void commit(Connection con) throws SQLException
	{
		con.commit();
	}

	public void rollback(Connection con) throws SQLException
	{
		con.rollback();
	}
	
	public boolean useReadOnlyConnections()
	{
		return false;
	}

	public boolean ignoreConnectionError(SQLException e)
	{
		return false;
//		return (GXSQLCODE == 1301);
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
		return DBMS_AS400;
	}
        public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount;
        }
		
	public boolean connectionClosed(SQLException e)
	{
		return (e.getSQLState() == "08S01");
	}	
}


