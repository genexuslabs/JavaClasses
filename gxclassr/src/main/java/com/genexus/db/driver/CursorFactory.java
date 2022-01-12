
package com.genexus.db.driver;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Manejo de pool de cursores preparados para una conexi�n.
 * <p>
 * Se puede definir el m�ximo numero de cursores por conexi�n.
 * <p>
 * Cada conexion a un data source tiene su CursorFactory.
 *
 * @author 	Andres Aguiar
 * @see     com.genexus.ConnectionManager#getCursorFactory
 */
import com.genexus.DebugFlag;

final class CursorFactory implements IPreparedStatementCache
{
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	String			arrayIds[];
	SQLCursorData 	data[];
	long			timeStamps[];
	int				maxSize;
	GXConnection    jdbcConnection;

	Hashtable<PreparedStatement, SQLCursorData>	    preparedStatements;
    private int usedCursors;

	//Hashtable	    statementsPerHandle;


    /**
     * Construye un nuevo CursorFactory con un tama�o m�ximo dado
     * para una conexi�n JDBC dada.
     */

	CursorFactory(int maxSize, GXConnection jdbcConnection)
	{
		if (maxSize > 2)
			maxSize --;

        usedCursors = 0;
		arrayIds   = new String[maxSize];
		timeStamps = new long[maxSize];
		data	   = new SQLCursorData[maxSize];

		this.maxSize = maxSize;

		preparedStatements = new Hashtable<>();
		//statementsPerHandle = new Hashtable();

		this.jdbcConnection = jdbcConnection;
	}

    public synchronized int getUsedCursors()
    {
        return usedCursors;
    }
	
    public int getUsedCursorsJMX()
    {
        return usedCursors;
    }	

	/**
	 * Hace un drop del cursor que hace mas tiempo no se usa.
	 */
	private int dropOlderCursor() throws SQLException
	{
		long minTimeStamp = Long.MAX_VALUE;
		int  index = -1;

		for(int i = maxSize - 1; i >= 0; i--)
		{
			if 	(timeStamps[i] < minTimeStamp && timeStamps[i] != 0 && !data[i].isInUse())
			{
				minTimeStamp = timeStamps[i];
				index        = i;
			}
		}

		if	(index == -1)
		{
			System.err.println("Error can't prepare more cursors \n You should increase the Maximum cursors per connection preference");
			if	(DEBUG)
				jdbcConnection.log(GXDBDebug.LOG_MED, "Error can't prepare more cursors \n You should increase the Maximum cursors per connection preference");

			throw new InternalError("Error can't prepare more cursors \n You should increase the Maximum cursors per connection preference");
		}

		// Hago el drop del cursor y reseteo elementos de los arrays.
//		try
//		{
			data[index].getPreparedStatement().close();
			preparedStatements.remove(data[index].getPreparedStatement());
//		}
//		catch (SQLException e)
//		{
//			PrivateUtilities.errorHandler("SQL Exception CursorFactory/dropOlderCursor " + e.getErrorCode() + "-" + e.getSQLState(), e);
//		}

		if	(DEBUG)
			jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Dropping cursor " + arrayIds[index] + " - " + data[index].getSQLSentence());

		data[index]       = null;
		arrayIds[index]   = null;
		timeStamps[index] = 0;

		return index;
	}

	/**
	 * Agrega un nuevo cursor en el pool.
	 */
	private PreparedStatement addElement(int handle, String cursorId, String sqlSentence, boolean currentOf, boolean callable) throws SQLException
	{
		int i;
		PreparedStatement statement = null;

		if	(maxSize == 0)
		{
			if	(callable)
				return jdbcConnection.prepareCall(sqlSentence);
			else
				return jdbcConnection.prepareStatement(sqlSentence, handle, cursorId);
		}

		// Obtengo el siguiente valor con clave = 0.
		for(i = 0; i < maxSize && arrayIds[i] != null; ++i);

		// Si no hay ninguno, hago drop del cursor mas viejo y me quedo con su �ndice.
		if	(i == maxSize)
		{
			i = dropOlderCursor();
		}

		if	(callable)
			statement     = jdbcConnection.prepareCall(sqlSentence);
		else
			statement     = jdbcConnection.prepareStatement(sqlSentence, handle, cursorId);

		arrayIds[i]   = cursorId;
		timeStamps[i] = System.currentTimeMillis();
		data[i]       = new SQLCursorData(sqlSentence, statement);

		preparedStatements.put(statement, data[i]);

		if	(DEBUG)
			jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Preparing new cursor " + cursorId + " - " + sqlSentence);

		if	(currentOf && jdbcConnection.getDBMS() instanceof GXDBMSas400)
                {
                  try
                  {
                    statement.setCursorName(cursorId);
                  }
                  catch(SQLException e)
                  {
                    data[i]       = null;
                    arrayIds[i]   = null;
                    timeStamps[i] = 0;
                    preparedStatements.remove(statement);
                    throw e;
                  }
                }

        usedCursors++;

		((GXPreparedStatement) statement).setHandle(handle);

		return statement;
	}
/*
	void addStatementToHandle(int handle, ResultSet rslt)
	{
		Hashtable statements = (Hashtable) statementsPerHandle.get(new Integer(handle));

		if	(statements == null)
		{
			statements = new Hashtable();
			statementsPerHandle.put(new Integer(handle), statements);
		}

		statements.put(rslt, "");
	}

	void removeStatementFromHandle(int handle, ResultSet rslt)
	{
		Hashtable statements = (Hashtable) statementsPerHandle.get(new Integer(handle));
		statements.remove(rslt);
	}
*/
	public CallableStatement getCallableStatement(int handle, String index, String sqlSentence) throws SQLException
	{
		return (CallableStatement) getStatement(handle, index, sqlSentence, false, true, false);
	}

	/**
	 * Devuelve una sentencia preparada para el indice pasado como
	 * par�metro. Si ya esta preparada, la devuelve, sino la prepara
	 * y la devuelve
	 */

	public PreparedStatement getStatement(int handle, String index, String sqlSentence, boolean currentOf) throws SQLException
	{
		return getStatement(handle, index, sqlSentence, currentOf, false, false);
	}

	private void reprepareStatement(int handle, String index, String sqlSentence, boolean currentOf, boolean callable, int arrIdx) throws SQLException
	{
		data[arrIdx].getPreparedStatement().close();

		timeStamps[arrIdx] = System.currentTimeMillis();
		data[arrIdx]       = new SQLCursorData(sqlSentence,
							callable?jdbcConnection.prepareCall(sqlSentence):jdbcConnection.prepareStatement(sqlSentence, handle, index));

        usedCursors++;

		if	(DEBUG)
			jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Re preparing new cursor " + index+ " - " + sqlSentence);
	}

	public synchronized PreparedStatement getStatement(int handle, String index, String sqlSentence, boolean currentOf, boolean callable, boolean batch) throws SQLException
	{
		int i;

		// Busco secuencialmente por el Id.
		for (i = 0; i < maxSize && (arrayIds[i] == null || !(arrayIds[i].equals(index) && data[i].getSQLSentence().equals(sqlSentence)) || data[i].isInUse()); i++);
		if	(i == maxSize)
		{
			return addElement(handle, index, sqlSentence, currentOf, callable);
		}

		// Si la sentencia SQL que esta en el array es distinta a la que quiero preparar,
		// es porque tengo el mismo ID para otra sentencia, lo que no deberia ser posible.
		if (!data[i].getSQLSentence().equals(sqlSentence))
		{
			reprepareStatement(handle, index, sqlSentence, currentOf, callable, i);
/*
			Desde 1/10/02 la sentencia puede cambiar por causa de contraints o orders condicionales
			if	(com.genexus.ApplicationContext.getInstance().isServletEngine())
			{
				System.err.println("Warning - Repreparing the statement " + index);
				reprepareStatement(handle, index, sqlSentence, currentOf, callable, i);
			}
			else
			{
				System.err.println("The prepared cursor has a different SQL statement. You are probably \nmixing sources from different GeneXus KB\n");
				if	(DEBUG)
					jdbcConnection.log(GXDBDebug.LOG_MED, "The prepared cursor has a different SQL statement. You are probably \nmixing sources from different GeneXus KB\n");
				throw new InternalError();
			}
*/
		}

		if	(DEBUG)
			jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Reusing cursor " + index + " - " + sqlSentence + " MaxSize " + maxSize);

		// Actualizo el timestamp
		timeStamps[i] = System.currentTimeMillis();

        usedCursors++;
		((GXPreparedStatement) data[i].getPreparedStatement()).setHandle(handle);

		return data[i].getPreparedStatement();
	}

	/**
	 * Devuelve una sentencia preparada para la tabla/operacion/sentencia
	 * pasada como par�metro. Si ya esta preparada, la devuelve, sino
	 * la prepara y la devuelve
	 */
	public synchronized PreparedStatement getStatement(int handle, int tableId, int operationId, String sqlSentence) throws SQLException
	{
		// Busco secuencialmente por el Id y por sentencia SQL.
		int i;
		String index = CursorFactoryConstants.calculateCursorId(tableId, operationId);

		for (i = 0; i < maxSize && (arrayIds[i] == null || !(arrayIds[i].equals(index) && data[i].getSQLSentence().equals(sqlSentence) && !data[i].isInUse())); i++);

		if	(i == maxSize)
		{
			return addElement(handle, index, sqlSentence, false, false);
		}

		// Actualizo el timestamp
		timeStamps[i] = System.currentTimeMillis();

		if	(DEBUG)
			jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Reusing cursor TBL " + index + " - " + sqlSentence);

        usedCursors++;
		((GXPreparedStatement) data[i].getPreparedStatement()).setHandle(handle);
		return data[i].getPreparedStatement();
	}

	/**
	 * Hace un drop de todos los cursores preparados del pool de cursores.
	 */
	public synchronized void dropAllCursors()
	{
          for (int i = maxSize - 1; i >= 0; i--)
          {
            if 	(arrayIds[i] != null)
            {
              if(DEBUG)
                jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Drop All/Dropping cursor " + arrayIds[i] + " - " + data[i].getSQLSentence());

              try
              {
                data[i].getPreparedStatement().close();
              }
              catch (SQLException e)
              {
                if(DEBUG)
                {
                  jdbcConnection.log(GXDBDebug.LOG_MED, "Error dropping cursors " + arrayIds[i] + " - " + data[i].getSQLSentence());
                  jdbcConnection.logSQLException(-1, e);
                }
              }

              data[i] = null;
              arrayIds[i]   = null;
              timeStamps[i] = 0;
            }
          }

          preparedStatements.clear();
          usedCursors = 0;
	}

        public synchronized void freeAllCursors()
        {
          for (int i = maxSize - 1; i >= 0; i--)
          {
            if 	(arrayIds[i] != null && data[i].isInUse())
            {
                data[i].setNotInUse();
            }
          }

          usedCursors = 0;
        }
		

	/** Elimina un cursor (lo cierra y lo saca de la lista de prepared statements)
	 */	
	public synchronized void dropCursor(GXPreparedStatement stmt)
	{
		if(stmt == null)return;
		setNotInUse(stmt);
		try
		{
			stmt.close();
		}
		catch (SQLException e){ ; }
		preparedStatements.remove(stmt);
		return;
	}


	/**
	 * Marca un cursor como "Not in use", de modo que pueda ser reutilizado.
	 */
	public synchronized void setNotInUse(GXPreparedStatement stmt)
	{
		// Puede venir en null si se llama desde un supplier que nunca
		// abri� el cursor.
		if	(stmt == null)
			return;

		if	(maxSize == 0)
		{
			try
			{
				stmt.close();
			}
			catch (SQLException e)
			{
				System.err.println("can't close cursor");
			}

			return;
		}

		SQLCursorData data = preparedStatements.get(stmt);
		if	(data != null)
		{
			data.setNotInUse();
            usedCursors --;

			if	(DEBUG)
				jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Setting cursor as unused " + data.getSQLSentence() + "/" + data.statement);
		}
		else
		{
			if	(DEBUG)
				jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Warning, trying to mark as unused an inexistent cursor");
		}
	}


	public void dump(PrintStream out)
	{
		out.println("\tPreparedStatements : " + usedCursors + "/" + maxSize);
		for (int i = 0; i < maxSize; i++)
		{
			if	(arrayIds[i] != null)
			{
				out.println("\t\t" + i + " id: " + data[i].statement.hashCode() + " - gxid: " + arrayIds[i] + " - inuse: " + data[i].inUse);
			}
		}
	}

	private class SQLCursorData
	{
		PreparedStatement statement;
	 	String			  sqlSentence;
	 	boolean		  	  inUse;

		SQLCursorData (String sqlSentence, PreparedStatement statement)
		{
			this.sqlSentence = sqlSentence;
			this.statement   = statement;
			this.inUse       = true;
		}

		String getSQLSentence()
		{
			return sqlSentence;
		}

		PreparedStatement getPreparedStatement()
		{
			inUse = true;
			return statement;
		}

		boolean isInUse()
		{
			return inUse;
		}

		void setNotInUse ()
		{
			inUse = false;
		}
	}

}

