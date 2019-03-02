/*
 * Created on Apr 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.genexus.db.driver;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import com.genexus.DebugFlag;
import com.genexus.db.*;

/**
 * @author aaguiar
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PreparedStatementCache {

	private static final boolean DEBUG       = DebugFlag.DEBUG;

	private HashMap usedCache;
	private FreeStatementList freeStatementList;

	private GXConnection jdbcConnection;

	private int maxSize;
	private boolean unlimitedSize;

	PreparedStatementCache(int maxSize, GXConnection jdbcConnection)
	{
		this.freeStatementList = new FreeStatementList(maxSize, jdbcConnection);
		this.usedCache = new HashMap(maxSize);
		this.jdbcConnection = jdbcConnection;
		this.maxSize = maxSize;
		this.unlimitedSize = (maxSize == -1);

		if	(DEBUG)
		{
			jdbcConnection.log(GXDBDebug.LOG_MED, "Creating Java 2 statement cache: Maximum size " + maxSize);
		}
	}

	public CallableStatement getCallableStatement(int handle, String index, String sqlSentence) throws SQLException
	{
		return (CallableStatement) getStatement(handle, index, sqlSentence, false, true, false);
	}

	public synchronized int getUsedCursors()
	{
		return usedCache.size();
	}
	
	public int getUsedCursorsJMX()
	{
		return usedCache.size();
	}	

	public PreparedStatement getStatement(int handle, String index, String sqlSentence, boolean currentOf) throws SQLException
	{
		return getStatement(handle, index, sqlSentence, currentOf, false, false);
	}

	private GXPreparedStatement createStatement(int handle, String cursorId, String sqlSentence, boolean currentOf, boolean callable) throws SQLException
	{
		GXPreparedStatement stmt = (GXPreparedStatement) (callable?jdbcConnection.prepareCall(sqlSentence):jdbcConnection.prepareStatement(sqlSentence, handle, cursorId, currentOf));

		if	(DEBUG)
			jdbcConnection.log(GXDBDebug.LOG_MED, "Preparing new cursor " + cursorId + " free " + freeStatementList.size() + " used " + usedCache.size() + " - " + sqlSentence);

		return stmt;
	}

	public synchronized PreparedStatement getStatement(int handle, String index, String sqlSentence, boolean currentOf, boolean callable, boolean batch) throws SQLException
	{
		if (handle != -1 && !index.equals("_ConnectionID_"))
			((UserInformation)DBConnectionManager.getInstance().getUserInformation(handle)).setLastSQL(index + " " + sqlSentence);
		
		// Busco un statement en la lista de free
                GXPreparedStatement out = null;
                if (batch)
                  //En este caso no puedo poner la entrada por la sentencia sino por el nro de cursor.
                  out = freeStatementList.get(index);
                else
                  out = freeStatementList.get(sqlSentence);

		// Si no existe el statement en la lista de frees
		if (out == null)
		{
			if (!unlimitedSize && (freeStatementList.size() + usedCache.size()) >= maxSize)
			{
				// Si no hay free quiere decir que estamos tope. En ese caso agrandamos el pool
				if	(freeStatementList.size() == 0)
				{
					maxSize ++;
					if	(DEBUG)
						jdbcConnection.log(GXDBDebug.LOG_MED, "Expanding prepared statement pool to " +  maxSize);
				}
				else
				{
					freeStatementList.removeOlder();
				}
			}

			out = createStatement(handle, index, sqlSentence, currentOf, callable);
		}
		else
		{
			if	(DEBUG)
				jdbcConnection.log(GXDBDebug.LOG_MED, "Reusing cursor " + index + " free " + freeStatementList.size() + " used " + usedCache.size() + " - " + sqlSentence);
		}

		usedCache.put(out, null);
		out.setHandle(handle);
		return out;
	}

	/**
	 * Hace un drop de todos los cursores preparados del pool de cursores.
	 */
	public synchronized void dropAllCursors()
	{
		if	(DEBUG)
			jdbcConnection.log(GXDBDebug.LOG_MED, "Dropping all cursors. Used :" + usedCache.size() + " free " + freeStatementList.size());

		try
		{
			for (Iterator i = usedCache.keySet().iterator(); i.hasNext(); )
			{
				GXPreparedStatement stmt = ((GXPreparedStatement) i.next());
				stmt.close();
				if	(DEBUG)
					jdbcConnection.log(GXDBDebug.LOG_MED, "Dropping used cursor " + stmt.getSqlStatement());
			}

			freeStatementList.closeAll();
		}
		catch (SQLException e)
		{
			if	(DEBUG)
			{
				jdbcConnection.log(GXDBDebug.LOG_MED, "Can't drop cursors");
				jdbcConnection.logSQLException(-1, e);
			}
		}

		usedCache.clear();
	}

        public synchronized void freeAllCursors()
        {
          for (Iterator i = usedCache.keySet().iterator(); i.hasNext(); )
          {
            GXPreparedStatement stmt = ((GXPreparedStatement) i.next());
            setNotInUse(stmt);
          }

          usedCache.clear();
        }

	/** Elimina un cursor (lo cierra y lo saca de la lista de prepared statements)
	 */	
	public synchronized void dropCursor(GXPreparedStatement stmt)
	{
		if(stmt == null)return;
		try
		{
			stmt.close();
		}catch (SQLException e){ ; }
		usedCache.remove(stmt);
		freeStatementList.remove(stmt);
		return;
	}

	/**
	 * Marca un cursor como "Not in use", de modo que pueda ser reutilizado.
	 */
	public synchronized void setNotInUse(GXPreparedStatement stmt)
	{
		// Puede venir en null si se llama desde un supplier que nunca
		// abri� el cursor.

		if	(stmt != null)
		{
			try
			{
				usedCache.remove(stmt);
				freeStatementList.add(stmt);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
	}

	public void dump(java.io.PrintStream out)
	{
		out.println("Used statements");
		for (Iterator i = usedCache.keySet().iterator(); i.hasNext(); )
		{
			GXPreparedStatement stmt = (GXPreparedStatement) i.next();
			out.println(stmt.getCreationTime() + " : " + stmt.getSqlStatement());
		}
	}

}
