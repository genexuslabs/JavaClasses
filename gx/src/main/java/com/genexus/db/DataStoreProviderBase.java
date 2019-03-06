package com.genexus.db;


import java.sql.SQLException;
import java.util.*;

import com.genexus.db.driver.*;
import com.genexus.*;
import com.genexus.IErrorHandler;
import com.genexus.common.classes.AbstractDataStoreProviderBase;
import com.genexus.common.classes.AbstractGXConnection;

public abstract class DataStoreProviderBase extends AbstractDataStoreProviderBase
{
	
	private IConnectionProvider connectionProvider;
	protected ModelContext context;
	protected int remoteHandle;
	protected Cursor[] cursors;
	protected DataSource dataSource;
	protected GXParameterUnpacker unpacker = new GXParameterUnpacker();
	protected GXParameterPacker packer = new GXParameterPacker();
	protected IErrorHandler errorHandler;
	protected Date beginExecute;
	
	protected GXConnection con;

	protected DataStoreProviderBase(ModelContext context, int remoteHandle)
	{
		this.context = context;
		this.remoteHandle = remoteHandle;
	}

	public GXConnection getConnection() throws SQLException
	{
		return getConnection(getHelper().getDataStoreName(), getHelper().needsReadOnlyConnection());
	}
	protected GXConnection getConnection(String dataStoreName, boolean needsReadOnlyConnection) throws SQLException
	{
		if	(connectionProvider == null)
		{
			connectionProvider = getHelper().getConnectionProvider();
		}
		
		con = (GXConnection) connectionProvider.getConnection(context, remoteHandle, dataStoreName, needsReadOnlyConnection, true);
		return con; 
	}
	protected DataSource getDataSource()
	{
		if (dataSource == null)
			dataSource = Application.getConnectionManager(context).getDataSource(remoteHandle, getHelper().getDataStoreName());

		return dataSource;
	}
	
	protected DataSource getDataSourceNoException()
	{
		if (dataSource == null)
			dataSource = Application.getConnectionManager(context).getDataSourceNoException(remoteHandle, getHelper().getDataStoreName());

		return dataSource;
	}

	public void setErrorHandler(IErrorHandler errorHandler)
	{
		this.errorHandler = errorHandler;
	}

	protected Object [] dynConstraints = null;
	public void dynParam(int cursorId, Object [] dynConstraints)
	{
		this.dynConstraints = dynConstraints;
	}
	
	public Object [] getDynConstraints()
	{
		return (Object[])dynConstraints[0];
	}

	protected CacheValue [] cacheValue;
	protected java.util.Enumeration [] cacheIterator;
		
}