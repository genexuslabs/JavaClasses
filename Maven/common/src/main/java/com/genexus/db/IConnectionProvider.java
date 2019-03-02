package com.genexus.db;


import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.classes.AbstractModelContext;
import java.sql.SQLException;

public interface IConnectionProvider
{
	public AbstractGXConnection getConnection(AbstractModelContext context, int remoteHandle, String dataStore, boolean readOnly, boolean sticky) throws SQLException;
}