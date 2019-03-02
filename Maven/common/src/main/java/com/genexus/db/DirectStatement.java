package com.genexus.db;

import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import java.sql.SQLException;


public class DirectStatement extends Cursor
{
	public DirectStatement(String sqlSentence, int errMask, int fetchSize)
	{
		super("", sqlSentence, errMask);
	}

	public DirectStatement(String sqlSentence, int errMask)
	{
		this(sqlSentence, errMask, 0);
	}
	
	public DirectStatement(String sqlSentence)
	{
		this(sqlSentence, 0);
	}	

	byte[] preExecute(int cursorIdx, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds, Object [] params) throws SQLException
	{
		return null;
	}

	void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
	{
		SentenceProvider.executeStatement(connectionProvider, mSQLSentence);
	}
}
