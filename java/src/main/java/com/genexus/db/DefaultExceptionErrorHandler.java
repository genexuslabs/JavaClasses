package com.genexus.db;

import java.sql.SQLException;

import com.genexus.GXRuntimeException;
import com.genexus.IErrorHandler;
import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractGXConnection;
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
		try {
			AbstractGXConnection conn = helper.getConnectionProvider().getConnection(context, remoteHandle, helper.getDataStoreName(), true, true);
			handleSQLError1(errorHandler, e, context, remoteHandle, conn, helper.getDataStoreName(), cursor);
		}
		catch (SQLException exc) {
			throw new GXRuntimeException(e);
		}
	}

	public static void handleSQLError1(IErrorHandler errorHandler, SQLException e, ModelContext context, int remoteHandle, AbstractGXConnection conn, String datastoreName, Cursor cursor)
	{
		cursor.status = mapErrorToStatus(DBConnectionManager.getInstance().getUserInformation(remoteHandle).getNamespace().getDataSource(datastoreName).dbms, e);
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
					context.globals.Gx_dbsqlstate = e.getSQLState();

					context.inErrorHandler = true;

					try {
						errorHandler.handleError();
					}
					finally {
						context.inErrorHandler = false;
					}
			}

			if	(context.globals.Gx_eop == ERROPT_DEFAULT)
			{
				defaultSQLErrorHandler(context, cursor);
			}

			if	(context.globals.Gx_eop == ERROPT_CANCEL)
			{
				com.genexus.Application.rollback(context, remoteHandle, datastoreName, null);
				conn.setError();
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
	
	public static void defaultSQLErrorHandler(ModelContext context, Cursor cursor)
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