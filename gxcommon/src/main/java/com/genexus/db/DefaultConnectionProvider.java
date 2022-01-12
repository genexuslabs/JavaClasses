package com.genexus.db;

import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;

import java.sql.SQLException;

public class DefaultConnectionProvider implements IConnectionProvider
{
	public AbstractGXConnection getConnection(ModelContext context, int remoteHandle, String dataStore, boolean readOnly, boolean sticky) throws SQLException
	{
		return SpecificImplementation.Application.getConnection(context, remoteHandle, dataStore, readOnly, sticky);
	}
}
