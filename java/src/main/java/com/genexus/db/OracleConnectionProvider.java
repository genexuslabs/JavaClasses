package com.genexus.db;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.genexus.ModelContext;
import com.genexus.ModelContext;
import com.genexus.db.driver.GXConnection;

public class OracleConnectionProvider implements IConnectionProvider
{
	public GXConnection getConnection(ModelContext context, int remoteHandle, String dataStore, boolean readOnly, boolean sticky) throws SQLException
	{
		return new GXConnection(com.genexus.Application.getConnectionManager((ModelContext) context).getUserInformation(remoteHandle).getNamespace().getDataSource(dataStore),
								DriverManager.getConnection("jdbc:default:connection:"));
	}
}