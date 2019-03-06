package com.genexus.db;

import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractModelContext;
import com.genexus.db.driver.GXConnection;
import java.sql.SQLException;
import java.sql.DriverManager;

public class OracleConnectionProvider implements IConnectionProvider
{
	public GXConnection getConnection(AbstractModelContext context, int remoteHandle, String dataStore, boolean readOnly, boolean sticky) throws SQLException
	{
		return new GXConnection(com.genexus.Application.getConnectionManager((ModelContext) context).getUserInformation(remoteHandle).getNamespace().getDataSource(dataStore), 
								DriverManager.getConnection("jdbc:default:connection:"));
	}
}