package com.genexus.db;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractModelContext;
import com.genexus.db.driver.GXConnection;

public class OracleConnectionProvider implements IConnectionProvider
{
	public GXConnection getConnection(AbstractModelContext context, int remoteHandle, String dataStore, boolean readOnly, boolean sticky) throws SQLException
	{
		return new GXConnection(com.genexus.Application.getConnectionManager((ModelContext) context).getUserInformation(remoteHandle).getNamespace().getDataSource(dataStore), 
								DriverManager.getConnection("jdbc:default:connection:"));
	}
}