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

import org.apache.commons.collections.SequencedHashMap;

import com.genexus.DebugFlag;

/**
 * @author aaguiar
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FreeStatementList {

	// freeCache tiene una lista de sentencias SQL, y para cada una tiene una lista de statements
	// preparados.
	SequencedHashMap freeCache;
	GXConnection jdbcConnection;
	int statementCount;

	private static final boolean DEBUG       = DebugFlag.DEBUG;

	FreeStatementList(int maxSize, GXConnection jdbcConnection)
	{
		freeCache = new SequencedHashMap(maxSize);
		this.jdbcConnection = jdbcConnection;
		this.statementCount = 0;
	}

	int size()
	{
		return statementCount;
	}

        String getCacheId( GXPreparedStatement stmt)
        {
            return stmt.getSqlStatement();
        }

	void add(GXPreparedStatement stmt)
	{
		String sqlStatement = getCacheId(stmt);
		StatementList sl = (StatementList) freeCache.get(sqlStatement);

		if (sl == null)
		{
			sl = new StatementList();
			freeCache.put(sqlStatement, sl);
		}

		sl.push(stmt);
		statementCount ++;
	}
	
	void remove(GXPreparedStatement stmt)
	{
		String sqlStatement = getCacheId(stmt);
		StatementList sl = (StatementList) freeCache.get(sqlStatement);
		if(sl == null)
		{
			return;
		}
		statementCount -= sl.size();
        freeCache.remove(sqlStatement);
	}

	void removeOlder()
	{
		GXPreparedStatement stmt = null;
		// Asume que el size de freeCache > 0.
		StatementList statementList = (StatementList) freeCache.getFirstValue();
			
		for (Iterator j = statementList.iterator(); j.hasNext(); )
		{
			stmt = (GXPreparedStatement) j.next();
			statementCount --;
			
			try
			{
				stmt.close();
				if	(DEBUG)
					jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Dropping older cursor free " + statementCount + " - " + stmt.getSqlStatement());
			}
			catch (SQLException e)
			{
				if	(DEBUG)
					jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Cannot close cursor " + stmt.getSqlStatement());
			}
		}
		freeCache.remove(getCacheId( stmt));
	}

	private GXPreparedStatement popSentence(StatementList sl)
	{
		GXPreparedStatement stmt = sl.pop();
		statementCount --;

		if (sl.size() == 0)
		{
            freeCache.remove(getCacheId( stmt));
		}

		return stmt;
	}

	GXPreparedStatement get(String sqlSentence)
	{
		StatementList sl = (StatementList) freeCache.get(sqlSentence);

		return (sl == null? null : popSentence(sl));
	}

	void closeAll() throws SQLException
	{
		for (Iterator i = freeCache.values().iterator(); i.hasNext();)
		{
			for (Iterator j = ((StatementList) i.next()).iterator(); j.hasNext(); )
			{
				GXPreparedStatement stmt = (GXPreparedStatement) j.next();

				stmt.close();

				if	(DEBUG)
					jdbcConnection.log(GXDBDebug.LOG_MED, "GX: Dropping free cursor " + stmt.getSqlStatement());
			}
		}

		freeCache.clear();
		statementCount = 0;
	}

	class StatementList
	{
		private ArrayList stmts = new ArrayList();

		int size()
		{
			return stmts.size();
		}
		void push(GXPreparedStatement stmt)
		{
			stmts.add(stmt);
		}

		GXPreparedStatement pop()
		{
			return (GXPreparedStatement) (stmts.remove(stmts.size() - 1));
		}

		Iterator iterator()
		{
			return stmts.iterator();
		}
	}
}
