package com.genexus.db;

import java.sql.SQLException;
import com.genexus.ModelContext;
import com.genexus.IErrorHandler;
import com.genexus.GXRuntimeException;

public class SQLExceptionErrorHandler
{
	public void handleError(IErrorHandler errorHandler, SQLException e, ModelContext context, int remoteHandle, DataStoreHelperBase helper, int cursorStatus, String table, int oper)
	{
		if	(errorHandler == null)
			throw new GXRuntimeException(e);


		
	}

}