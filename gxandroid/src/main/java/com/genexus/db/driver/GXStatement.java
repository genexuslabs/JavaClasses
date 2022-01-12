package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import com.genexus.DebugFlag;

public class GXStatement implements Statement
{
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	private Statement stmt;
	protected GXConnection con;
	protected int handle;

	GXStatement(Statement stmt, GXConnection con, int handle)
	{
		this.stmt = stmt;
		this.con  = con ;
		this.handle = handle;
	}

	/**
	* Dos threads podrían querer ejecutar el siguiente método al mismo tiempo
	* Podria asumir que stmt.executeQuery está synchronized y no pasaría nada
	* pero prefiero marcarlo como synchronized.
	*/
    public synchronized ResultSet executeQuery(String sql) throws SQLException
	{			
		// Consideraciones especiales : 
		// el .incOpenCursor hay que hacerlo despues de obtener el resultset, porque
		// sino, si hay una SQLException deberia dar marcha atrás.

		ResultSet result = null;

		if	(DEBUG)
		{
			log("executeQuery - sql: " + sql);
			try
			{
				result = new GXResultSet(stmt.executeQuery(sql), this, con, handle);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			result = new GXResultSet(stmt.executeQuery(sql), this, con, handle);
		}

		return result;
	}

	/**
	* No pasa que 2 threads lo intenten ejecutar al mismo tiempo
	*/
    public int executeUpdate(String sql) throws SQLException
	{
		
		int ret = 0;
		SQLException sqlException = null;

		try
		{
			if	(DEBUG)
			{
				log("executeUpdate - sql: " + sql);
				ret = stmt.executeUpdate(sql);
			}
			else
			{
				ret = stmt.executeUpdate(sql);
			}
		}
		catch (SQLException e)
		{
			if	(DEBUG)			
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), e);

			sqlException = e;			
		}

		//con.setNotInUse(this);

		if	(sqlException != null)
			throw sqlException;

		return ret;
	}

	/**
	* No pasa que 2 threads lo intenten ejecutar al mismo tiempo
	*/
    public boolean execute(String sql) throws SQLException
	{
		
		if	(DEBUG)
		{
			log("execute - sql: " + sql);
			try
			{
				boolean ret = stmt.execute(sql);
				//con.setNotInUse(this);
				return ret;
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			boolean ret = stmt.execute(sql);
			//con.setNotInUse(this);
			return ret;
		}
	}

	/**
	* Esto se llama solo cuando se cierra la conexión real, o cuando se llego al maximo
	* de statements preparados. En el primer caso, no se ejecuta desde 2 threads al mismo
	* tiempo. En el segundo, podria darse el caso, por lo que se marca como synchronized.
	*/
    public synchronized void close() throws SQLException
	{
		if	(DEBUG)
		{
			log("close stmt");
			try
			{
				stmt.close();
//				con.setNotInUse(this);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.close();
//			con.setNotInUse(this);
		}
	}

    public SQLWarning getWarnings() throws SQLException
	{
		if	(DEBUG)
		{
			log("getWarnings");
			try
			{
				return stmt.getWarnings();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			return stmt.getWarnings();
		}
	}

    public void clearWarnings() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: clearWarnings");
			try
			{
				stmt.clearWarnings();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.clearWarnings();
		}
	}

    public ResultSet getResultSet() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: getResultSet");
			try
			{
				return new GXResultSet(stmt.getResultSet(), this, con, handle);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			return new GXResultSet(stmt.getResultSet(), this, con, handle);
		}
	}

    public int getMaxFieldSize() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: getMaxFieldSize");
			try
			{
				return stmt.getMaxFieldSize();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			return stmt.getMaxFieldSize();
		}
	}

    public void setMaxFieldSize(int max) throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: setMaxFieldSize - max: " + max);
			try
			{
				stmt.setMaxFieldSize(max);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setMaxFieldSize(max);
		}
	}

    public int getMaxRows() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: getMaxRows");
			try
			{
				return stmt.getMaxRows();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			return stmt.getMaxRows();
		}
	}

    public void setMaxRows(int max) throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: setMaxRows - max:" + max);
			try
			{
				stmt.setMaxRows(max);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setMaxRows(max);
		}
	}

    public void setEscapeProcessing(boolean enable) throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: setEscapeProcessing - enable: " + enable);
			try
			{
				stmt.setEscapeProcessing(enable);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setEscapeProcessing(enable);
		}
	}

    public int getQueryTimeout() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: getQueryTimeout");
			try
			{
				return stmt.getQueryTimeout();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			return stmt.getQueryTimeout();
		}
	}

    public void setQueryTimeout(int seconds) throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: setQueryTimeout - seconds:" + seconds);
			try
			{
				stmt.setQueryTimeout(seconds);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setQueryTimeout(seconds);
		}
	}

    public void cancel() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: cancel");
			try
			{
				stmt.cancel();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.cancel();
		}
	}

    public void setCursorName(String name) throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: setCursorName - name:" + name);
			try
			{
				stmt.setCursorName(name);
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			stmt.setCursorName(name);
		}
	}

    public int getUpdateCount() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: getUpdateCount");
			try
			{
				return stmt.getUpdateCount();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			return stmt.getUpdateCount();
		}
	}

    public boolean getMoreResults() throws SQLException
	{
		if	(DEBUG)
		{
			log("Warning: getMoreResults");
			try
			{
				return stmt.getMoreResults();
			}
			catch (SQLException sqlException)
			{
				if	(con.isLogEnabled()) con.logSQLException(con.getHandle(), sqlException);
				throw sqlException;
			}
		}
		else
		{
			return stmt.getMoreResults();
		}
	}

	private void log(String text)
	{
		if	(DEBUG)	
			con.log(GXDBDebug.LOG_MIN, text);
	}

	// JDBC 2.0

    public int[] executeBatch() throws SQLException
	{
		return stmt.executeBatch();
	}

    public Connection getConnection()  throws SQLException
	{
		return con;
	}
    
    public void addBatch( String sql ) throws SQLException
	{
		stmt.addBatch(sql);
	}
    
    public void clearBatch() throws SQLException
	{
		stmt.clearBatch();
	}
    
    public int getResultSetConcurrency() throws SQLException
	{
		return stmt.getResultSetConcurrency();
	}
    
    public int getResultSetType()  throws SQLException
	{
		return stmt.getResultSetType();
	}
    
    public void setFetchSize(int rows) throws SQLException
	{
		try
		{
			if(com.genexus.ModelContext.getModelContext().getPreferences().getProperty("DontSetFetchSize", "0").equals("0"))
			{
				stmt.setFetchSize(rows);
			}
		}
		catch(java.lang.AbstractMethodError e)
		{
			//Si el driver no soporte el metodo no se hace nada y se deja el valor default.
		}
	}

    public int getFetchSize() throws SQLException
	{
		return stmt.getFetchSize();
	}

    public void setFetchDirection(int direction) throws SQLException
	{
		stmt.setFetchDirection(direction);
	}

    public int getFetchDirection() throws SQLException
	{
		return stmt.getFetchDirection();
	}
	

	// Metodos agregados en JDK1.4	
    public boolean getMoreResults(int x) throws SQLException
	{
		return stmt.getMoreResults(x);
	}
	
	public ResultSet getGeneratedKeys() throws SQLException
	{
		return stmt.getGeneratedKeys();
	}
	
	
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException 
	{
		return stmt.executeUpdate(sql, autoGeneratedKeys);
	}
	
	public int executeUpdate(String sql, int [] c) throws SQLException 
	{
		return stmt.executeUpdate(sql, c);
	}
	
	public int executeUpdate(String sql, String [] c) throws SQLException 
	{
		return stmt.executeUpdate(sql, c);
	}
	
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException 
	{
		return stmt.execute(sql, autoGeneratedKeys);
	}
	
	public boolean execute(String sql, int [] c) throws SQLException 
	{
		return stmt.execute(sql, c);
	}
	
	public boolean execute(String sql, String [] c) throws SQLException 
	{
		return stmt.execute(sql, c);
	}
	
	public int getResultSetHoldability() throws SQLException 
	{
		return stmt.getResultSetHoldability();
	}

	/*** New methods in Java's 1.6 Statement interface. These are stubs. ***/
	public boolean isClosed() throws SQLException {return true;}
    public void setPoolable(boolean poolable) throws SQLException {}
    public boolean isPoolable() throws SQLException {return true;}
    public boolean isWrapperFor(Class<?> iface) throws SQLException {return true;}
    public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
	/*** End of new methods. ***/

	/** New methods in Java 7 **/
	public void closeOnCompletion() throws SQLException { }

	public boolean isCloseOnCompletion() throws SQLException {
		return false;
	}
	/** End of new methods **/
}

