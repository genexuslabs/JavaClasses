package com.genexus.db;


import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.ModelContext;
import java.sql.SQLException;

public interface IConnectionProvider
{
	public AbstractGXConnection getConnection(ModelContext context, int remoteHandle, String dataStore, boolean readOnly, boolean sticky) throws SQLException;
}