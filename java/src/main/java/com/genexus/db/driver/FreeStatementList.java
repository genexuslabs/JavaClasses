/*
 * Created on Apr 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.genexus.db.driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.collections4.map.LinkedMap;

import com.genexus.DebugFlag;

/**
 * @author aaguiar
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FreeStatementList {

	/***
	 * A sequence of sql statements strings with a list of Statements for each string sentence. Be aware this list is not threa safe
	 * so we MUST use objectLock to ensure right concurrency
	 */
	private LinkedMap<String, StatementList> freeCache;
	/***
	 * the underline connection
	 */
	private GXConnection jdbcConnection;
	/***
	 * The number of Statements for all lists
	 */
	private int statementCount;
	private Object objectLock = new Object();
	private static final boolean DEBUG       = DebugFlag.DEBUG;

	FreeStatementList(int maxSize, GXConnection jdbcConnection)
	{
		freeCache = new LinkedMap<>(maxSize);
		this.jdbcConnection = jdbcConnection;
		this.statementCount = 0;
	}

	int size()
	{
		synchronized (objectLock) {
			return statementCount;
		}
	}

  
	void add(GXPreparedStatement stmt)
	{
		String sqlStatement = stmt.getCacheId(jdbcConnection);
		synchronized (objectLock) {
			StatementList sl = freeCache.get(sqlStatement);
			boolean firstsql=false;
			if (sl == null)
			{
				firstsql = true;
				sl = new StatementList();
				freeCache.put(sqlStatement, sl);
			}
			if (!stmt.isBatch() || firstsql)
			{
				sl.push(stmt);
				statementCount++;
			}		
		}
	}
	
	void remove(GXPreparedStatement stmt)
	{
		String sqlStatement = stmt.getCacheId(jdbcConnection);
		synchronized (objectLock) {
			StatementList sl = freeCache.get(sqlStatement);
			if (sl != null) {
				statementCount -= sl.size();
		        freeCache.remove(sqlStatement);
			}
		}
	}

	void removeOlder()
	{
		// First remove from the cache and after close the statements to reduce the lock time
		StatementList statementList = null;
		synchronized (objectLock) {
			// Asume que el size de freeCache > 0.
			statementList = freeCache.get(freeCache.firstKey());
			freeCache.remove(statementList.peek().getCacheId(jdbcConnection));
			statementCount -= statementList.size();
		}
		statementList.popAndCloseStatements();
	}
	private GXPreparedStatement popSentence(StatementList sl)
	{
		synchronized (objectLock) {
			GXPreparedStatement stmt = sl.pop();
			statementCount --;
			if (sl.size() == 0)
			{
	            freeCache.remove(stmt.getCacheId(jdbcConnection));
			}
			return stmt;	
		}
	}

	GXPreparedStatement get(String sqlSentence)
	{
		synchronized (objectLock) {
			StatementList sl = freeCache.get(sqlSentence);
			return (sl == null? null : popSentence(sl));
		}
	}

	void closeAll() throws SQLException
	{
		ArrayList<StatementList> lists = new ArrayList<StatementList>();
		synchronized (objectLock) {
			for (Iterator i = freeCache.values().iterator(); i.hasNext();)
			{
				lists.add((StatementList) i.next());
			}
			freeCache.clear();
			statementCount = 0;		
		}
		for (StatementList list : lists) {
			list.popAndCloseStatements();
		}

	}

	class StatementList extends Stack<GXPreparedStatement>
	{
		void popAndCloseStatements() {
			while (!isEmpty())
			{
				GXPreparedStatement stmt = pop();
				try
				{
					stmt.close();
					if	(DEBUG)
						jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Dropping older cursor free " + stmt.getSqlStatement());
				}
				catch (SQLException e)
				{
					if	(DEBUG)
						jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Cannot close cursor " + stmt.getSqlStatement());
				}
			}

		}
	}
}
