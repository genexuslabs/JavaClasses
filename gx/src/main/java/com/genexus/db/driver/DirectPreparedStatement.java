package com.genexus.db.driver;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import com.genexus.DebugFlag;

/**
 * $Log: DirectPreparedStatement.java,v $
 * Revision 1.7  2005/11/28 14:14:43  gusbro
 * - Agrego dropCursor
 *
 * Revision 1.6  2005/07/21 15:10:58  iroqueta
 * Implementacion de soporte de JMX
 *
 * Revision 1.5  2005/05/25 15:31:52  iroqueta
 * Se implementa el metodo freeAllCursors para que sea llamado al hacer el disconnect de una conexion en el pool y asegurarnos que todos los contadores que indican si la conexion esta libre queden bien (Programacion defensiva)
 *
 * Revision 1.4  2004/05/25 17:05:09  gusbro
 * - Al hacer un setNotInUse de un CallableStatement no cierro el statement, porque
 *   se pueden pedir los resultados al propio statement. Dejo para cerrarlo en la dropAll
 *
 * Revision 1.3  2004/03/09 18:12:22  gusbro
 * - Cambio un remove por removeElement para que se pueda compilar en J#
 *
 * Revision 1.2  2004/02/25 15:26:27  gusbro
 * - Se cierra el cursor al ejecutarse setNotInUse()
 *
 * Revision 1.1  2003/12/30 22:56:06  gusbro
 * - Esta clase es un replacemente del PreparedStatementeCache para cuando la
 *   conexion es mediante un datasource del motor de servlets. Esta clase no cachea nada
 *   sino que delega el trabajo al datasource
 *
 *
 */
public class DirectPreparedStatement implements IPreparedStatementCache
{
	private static final boolean DEBUG       = DebugFlag.DEBUG;
	private GXConnection jdbcConnection;
	private Vector activeStatements;

	DirectPreparedStatement(GXConnection jdbcConnection)
	{
		this.jdbcConnection = jdbcConnection;
		activeStatements = new Vector();
	}

	public CallableStatement getCallableStatement(int handle, String index, String sqlSentence) throws SQLException
	{
		return (CallableStatement) getStatement(handle, index, sqlSentence, false, true, false);
	}

	public int getUsedCursors()
	{
		return 0;
	}
	
	public int getUsedCursorsJMX()
	{
		return 0;
	}	

	public PreparedStatement getStatement(int handle, String index, String sqlSentence, boolean currentOf) throws SQLException
	{
		return getStatement(handle, index, sqlSentence, currentOf, false, false);
	}

	private GXPreparedStatement createStatement(int handle, String cursorId, String sqlSentence, boolean currentOf, boolean callable) throws SQLException
	{
		GXPreparedStatement stmt = (GXPreparedStatement) (callable?jdbcConnection.prepareCall(sqlSentence):jdbcConnection.prepareStatement(sqlSentence, handle, cursorId));

		// Cosas feas que no deberian ir aqui...
		if	(currentOf && jdbcConnection.getDBMS() instanceof GXDBMSas400)
		{
			stmt.setCursorName(cursorId);
		}

		activeStatements.addElement(stmt);
		return stmt;
	}

	public synchronized PreparedStatement getStatement(int handle, String index, String sqlSentence, boolean currentOf, boolean callable, boolean batch) throws SQLException
	{
		return createStatement(handle, index, sqlSentence, currentOf, callable);
	}

	/**
	 * Hace un drop de todos los cursores preparados del pool de cursores.
	 */
	public synchronized void dropAllCursors()
	{
		for(Enumeration enum1 = activeStatements.elements(); enum1.hasMoreElements(); )
		{
			GXPreparedStatement stmt = (GXPreparedStatement)enum1.nextElement();
			if	(DEBUG)
			{
				jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Drop All/Dropping cursor - " + stmt.getSqlStatement());
			}
			try
			{
				stmt.close();
			}
			catch (SQLException e)
			{
				System.err.println("can't close cursor");
			}
		}
		activeStatements.removeAllElements();
	}

        public synchronized void freeAllCursors()
        {
        }

	/** Elimina un cursor (lo cierra y lo saca de la lista de prepared statements)
	 */	
	public synchronized void dropCursor(GXPreparedStatement stmt)
	{
		if(stmt == null)return;
		setNotInUse(stmt);
	}

	/**
	 * Marca un cursor como "Not in use", de modo que pueda ser reutilizado.
	 * En este caso cierra directamente el cursor.
	 */
	public synchronized void setNotInUse(GXPreparedStatement stmt)
	{
		// Puede venir en null si se llama desde un supplier que nunca
		// abri� el cursor.
		if	(stmt == null)
		{
			return;
		}

		if(stmt.isBatch() || stmt.getSkipSetBlobs())
		{ // Si es un statement batch, despu�s de hacer el setNotInUse se pueden setear los parametros
		  // En este caso no cerramos el statement, sino que lo dejamos para cerrar en la dropAllCursors
		  // que es ejecutada al terminar el request del wbp
			return;
		}

		activeStatements.removeElement(stmt);
		if	(DEBUG)
		{
			jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Marking cursor as unused [" + activeStatements.size() + "] - " + stmt.getSqlStatement());
		}
		try
		{
			stmt.close();
		}
		catch (SQLException e)
		{
			System.err.println("can't close cursor");
		}
	}

	public void dump(java.io.PrintStream out)
	{
	}

}
