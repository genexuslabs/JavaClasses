package com.genexus.db;

import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.common.classes.IGXPreparedStatement;
import java.sql.SQLException;

public class CallCursor extends Cursor
{
	Object[] buffers;

	public CallCursor(String cursorId, String sqlSentence, int errMask, int fetchSize)
	{
		super(cursorId, sqlSentence, errMask);
	}
	
	public CallCursor(String cursorId, String sqlSentence, int errMask)
	{
		this(cursorId, sqlSentence, errMask, 0);
	}

	public CallCursor(String cursorId, String sqlSentence)
	{
		this(cursorId, sqlSentence, 0);
	}

	public void setOutputBuffers(Object[] buffers)
	{
		this.buffers = buffers;
	}

	byte[] preExecute(int cursorNum, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds, Object [] params) throws SQLException
	{
		mPreparedStatement = (IGXPreparedStatement) SentenceProvider.getCallableStatement(connectionProvider, mCursorId, mSQLSentence);
		return null;
	}

	void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
	{
		mPreparedStatement.execute();
	}
}
