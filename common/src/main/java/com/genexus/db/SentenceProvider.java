package com.genexus.db;

import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.classes.IGXPreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SentenceProvider
{
	public static AbstractGXConnection acquireConnection(AbstractDataStoreProviderBase connectionProvider) throws SQLException {
		return connectionProvider.getConnection();
	}
	public static IGXPreparedStatement getPreparedStatement(AbstractDataStoreProviderBase connectionProvider, String cursorId, String sqlSentence, boolean currentOf) throws SQLException
	{
		return (IGXPreparedStatement) (acquireConnection(connectionProvider).getStatement(cursorId, sqlSentence, currentOf));
	}

        public static IGXPreparedStatement getPreparedStatement(AbstractDataStoreProviderBase connectionProvider, String cursorId, String sqlSentence, boolean currentOf, boolean batch) throws SQLException
        {
            return (IGXPreparedStatement) (acquireConnection(connectionProvider).getStatement(cursorId, sqlSentence, currentOf, batch));
        }

	public static IGXCallableStatement getCallableStatement(AbstractDataStoreProviderBase connectionProvider, String cursorId, String sqlSentence) throws SQLException
	{
		return (IGXCallableStatement) acquireConnection(connectionProvider).getCallableStatement(cursorId, sqlSentence);
	}

	public static void executeStatement(AbstractDataStoreProviderBase connectionProvider, String sqlSentence) throws SQLException
	{
		Statement stmt = acquireConnection(connectionProvider).createStatement();
		try
		{
			stmt.executeUpdate(sqlSentence);
		}
		catch (SQLException e)
		{
			stmt.close();
			throw e;
		}

		stmt.close();
	}
}