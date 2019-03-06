package com.genexus.db;

import java.sql.SQLException;

import com.genexus.GXRuntimeException;
import com.genexus.IErrorHandler;
import com.genexus.ModelContext;
import com.genexus.db.driver.GXDBMS;
import com.genexus.util.ReorgSubmitThreadPool;


public class DefaultExceptionErrorHandler
{
	static final int FETCH 		= 1;
	static final int EXECUTE 	= 2;
	static final int CLOSE 		= 3;

	static final int ERROPT_IGNORE  = 0; // Continue processing - The error is ignored.
	static final int ERROPT_RETRY   = 1; // Retry the operation.
	static final int ERROPT_CANCEL  = 2; // Cancel application execution
	static final int ERROPT_DEFAULT = 3; // Perform default GENEXUS processing 

	public static void handleSQLError(IErrorHandler errorHandler, SQLException e, ModelContext context, int remoteHandle, IDataStoreHelper helper, Cursor cursor)
	{
		cursor.status = mapErrorToStatus(DBConnectionManager.getInstance().getUserInformation(remoteHandle).getNamespace().getDataSource(helper.getDataStoreName()).dbms, e);
		if (cursor.status != Cursor.DUPLICATE)
		{
			ReorgSubmitThreadPool.setAnError();
		}	
		if (!context.inErrorHandler) 
		{
			if	(errorHandler != null)
			{
					context.globals.Gx_err = (short) cursor.status;
					context.globals.Gx_dbe = e.getErrorCode();
					context.globals.Gx_dbt = e.getMessage();

					context.inErrorHandler = true;
					errorHandler.handleError();
					context.inErrorHandler = false;
			}

			if	(context.globals.Gx_eop == ERROPT_DEFAULT)
			{
				defaultSQLErrorHandler(errorHandler, e, context, remoteHandle, helper, cursor);
			}

			if	(context.globals.Gx_eop == ERROPT_CANCEL)
			{
				com.genexus.Application.rollback(context, remoteHandle, helper.getDataStoreName(), null);
				throw new GXRuntimeException(e);
			}
		}
	}

	private static int mapErrorToStatus(GXDBMS dbms, SQLException e)
	{
		if	(dbms.ObjectLocked(e))
		{
			return Cursor.LOCKED;
		}

		if	(dbms.EndOfFile(e))
		{
			return Cursor.EOF;
		}
		if	(dbms.DuplicateKeyValue(e))
		{
			return Cursor.DUPLICATE;
		}
		
		if  	(dbms.ReferentialIntegrity(e))
		{
			return Cursor.REFERENTIAL;
		}
		if	(dbms.DuplicateKeyValue(e))
		{
			return Cursor.DUPLICATE;
		}

		if	(dbms.ObjectNotFound(e))
		{
			return Cursor.OBJECT_NOT_FOUND;
		}
		
		if	(dbms.getId() == GXDBMS.DBMS_MYSQL && dbms.DataTruncation(e))
		{
			return Cursor.DATA_TRUNCATION;
		}		

		return Cursor.UNEXPECTED_DBMS_ERROR;
	}
	
	public static void defaultSQLErrorHandler(IErrorHandler errorHandler, SQLException e, ModelContext context, int remoteHandle, IDataStoreHelper helper, Cursor cursor)
	{
		context.globals.Gx_eop = ERROPT_CANCEL;

		if	(cursor.status == Cursor.LOCKED)
		{		
			if	( (cursor.errMask & DataStoreHelperBase.GX_MASKLOOPLOCK) > 0)
			{
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException ex)
				{
				}
				context.globals.Gx_eop = ERROPT_RETRY;
			}
			else
			{
				context.globals.Gx_eop = ERROPT_IGNORE;
			}
		}
		
		if (cursor.status == Cursor.REFERENTIAL)
		{
			if ( (cursor.errMask & DataStoreHelperBase.GX_MASKFOREIGNKEY) > 0)
				context.globals.Gx_eop = ERROPT_IGNORE;
		}
		
		// Para registro duplicado el valor por defecto es ignorarlo (lo maneja la aplicacion generada)
		if	(cursor.status == Cursor.DUPLICATE)
		{		
			context.globals.Gx_eop = ERROPT_IGNORE;
		}
		
		if	(cursor.status == Cursor.DATA_TRUNCATION)
		{		
			context.globals.Gx_eop = ERROPT_IGNORE;
		}		
	}
}