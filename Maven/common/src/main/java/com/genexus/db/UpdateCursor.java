package com.genexus.db;

import com.genexus.ApplicationContext;
import com.genexus.common.classes.AbstractDataSource;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.common.interfaces.SpecificImplementation;

import java.sql.SQLException;

public class UpdateCursor extends Cursor
{
	protected boolean retryExecute;
	public UpdateCursor(String cursorId, String sqlSentence, int errMask, String tableName)
	{
		this(cursorId, sqlSentence, errMask);
	}

	public UpdateCursor(String cursorId, String sqlSentence, int errMask)
	{
		super(cursorId, sqlSentence, errMask);
		this.retryExecute = false;
	}

	public UpdateCursor(String cursorId, String sqlSentence)
	{
		this(cursorId, sqlSentence, 0);
	}

	@Override
	byte[] preExecute(int cursorNum, AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds, Object [] params) throws SQLException
	{
		mPreparedStatement = SentenceProvider.getPreparedStatement(connectionProvider, mCursorId, mSQLSentence, false);
		return null;
	}

	void postExecute(AbstractDataStoreProviderBase connectionProvider, AbstractDataSource ds) throws SQLException
	{
		try
		{
			retryExecute = false;
			int res = mPreparedStatement.executeUpdate();
			if (res > 0) {
				status = 0;
			}else{
				status = Cursor.EOF;
			}
			if (ApplicationContext.getInstance().getReorganization())
			{
				SpecificImplementation.Application.addExecutedStatement(mCursorId);
			}			
		}
		catch (SQLException e)
		{
			try
			{
				if ((errMask & DataStoreHelperBase.GX_ROLLBACKSAVEPOINT) > 0)
				{
					SentenceProvider.executeStatement(connectionProvider, "ROLLBACK TO SAVEPOINT gxupdate");
				}
			}
			catch (Exception ex)
			{
				System.err.println("ROLLBACK TO SAVEPOINT error " + ex.getMessage());
				ex.printStackTrace();
            }
			throw e;
		}
	}

	public boolean getRetryExecute()
	{
		return retryExecute;
	}
}
