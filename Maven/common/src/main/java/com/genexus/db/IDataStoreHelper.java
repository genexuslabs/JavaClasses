package com.genexus.db;

import com.genexus.IHttpContext;
import com.genexus.common.classes.AbstractModelContext;


public interface IDataStoreHelper 
{
	Cursor[] getCursors();
	IConnectionProvider getConnectionProvider();
	boolean needsReadOnlyConnection();
	String getDataStoreName();
	Object[] getDynamicStatement( int cursor ,  AbstractModelContext context, int remoteHandle, IHttpContext httpContext, Object [] dynConstraints );
	void setParametersRT(int cursor, IFieldSetter stmt, Object[] buffers);
}
