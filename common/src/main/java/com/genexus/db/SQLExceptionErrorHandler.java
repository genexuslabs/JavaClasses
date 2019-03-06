package com.genexus.db;

import java.sql.SQLException;
import com.genexus.common.classes.AbstractModelContext;
import com.genexus.IErrorHandler;
import com.genexus.GXRuntimeException;

public class SQLExceptionErrorHandler
{
	public void handleError(IErrorHandler errorHandler, SQLException e, AbstractModelContext context, int remoteHandle, DataStoreHelperBase helper, int cursorStatus, String table, int oper)
	{
		if	(errorHandler == null)
			throw new GXRuntimeException(e);


		
	}

}