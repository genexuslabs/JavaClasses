package com.genexus.db;

import java.sql.SQLException;
import java.util.*;
import java.io.PrintStream;
import java.io.FileOutputStream;

import com.genexus.*;
import com.genexus.db.driver.*;
import com.genexus.ModelContext;
import com.genexus.GXRuntimeException;

public class DataStoreProvider extends DataStoreProviderBase implements IDataStoreProvider
{
	protected ILocalDataStoreHelper helper;

	private static boolean firstTime = true;

	public DataStoreProvider(ModelContext context, int remoteHandle, ILocalDataStoreHelper helper, Object[] buffers)
	{
		super(context, remoteHandle);

		this.helper = helper;
		this.cursors = helper.getCursors();
		setOutputBuffers(buffers);

	}

        public void setErrorBuffers(int cursorIdx, Object[] buffers)
        {
            ((BatchUpdateCursor)cursors[cursorIdx]).errorRecords = buffers;
        }
	private void setOutputBuffers(Object[] buffers)
	{
		for (int i = 0; i < cursors.length; i++)
		{
			if	(cursors[i] instanceof ForEachCursor)
				((ForEachCursor) cursors[i]).setOutputBuffers((Object []) buffers[i]);
		 	else if	(cursors[i] instanceof CallCursor)
				((CallCursor) cursors[i]).setOutputBuffers((Object []) buffers[i]);
		}
	}

	public int getStatus(int cursorIdx)
	{
		Cursor cursor = cursors[cursorIdx];
		
		if (ApplicationContext.getInstance().getReorganization() && cursor instanceof ForEachCursor && GXReorganization.executedBefore(cursor.mCursorId))
		{
			return Cursor.EOF;
		}		
		
		return cursor.status;
	}

	public void addRecord(int cursorIdx, Object[] parms)
	{
		BatchUpdateCursor cursor = (BatchUpdateCursor)cursors[cursorIdx];

                if	(parms != null)
                {
                    do {
                        context.globals.Gx_eop = DefaultExceptionErrorHandler.
                                                 ERROPT_DEFAULT;
                        try {
                            helper.setParameters(cursorIdx, (IFieldSetter) cursor.mPreparedStatement, parms);
                        } catch (SQLException e) {
                            DefaultExceptionErrorHandler.handleSQLError(errorHandler, e,
                                    context, remoteHandle, helper, cursors[cursorIdx]);
                        }
                    } while (context.globals.Gx_eop ==
                             DefaultExceptionErrorHandler.ERROPT_RETRY);
                }

                do
                {
                    context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
                    try {
                        if (cursor.getBatchSize()>0){
                            cursor.addBatch(parms);
                        }
                    } catch (SQLException e) {DefaultExceptionErrorHandler.handleSQLError(errorHandler, e,
                                context, remoteHandle, helper, cursors[cursorIdx]);
                    }
                } while (context.globals.Gx_eop ==DefaultExceptionErrorHandler.ERROPT_RETRY);

	}
	public void initializeBatch(int cursorIdx, int batchSize, Object instance, String method)
	{
		BatchUpdateCursor cursor = (BatchUpdateCursor)cursors[cursorIdx];
		if (cursor.getBatchSize() == 0)
		{

                        do
                        {
                                context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
                                try
                                {
                                        cursor.preExecute(cursorIdx, this, getDataSource(), null);
                                }
                                catch (SQLException e)
                                {
                                        DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
                                }
                        }while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY);

                        if (cursor.getBatchSize() == 0){

                            cursor.setBatchSize(batchSize);
                            cursor.onCommitEvent(instance, method);
                        }
                        cursor.notInUse();
                        try {
                            Application.getConnectionManager(context).getConnection(
                                    context,
                                    remoteHandle, helper.getDataStoreName(), false, true).
                                    addBatchUpdate(cursor);

                        } catch (SQLException e) {
                            DefaultExceptionErrorHandler.handleSQLError(
                                    errorHandler, e, context, remoteHandle, helper,
                                    cursors[cursorIdx]);
                        }
		}
	}
	public int getBatchSize(int cursorIdx)
	{
		BatchUpdateCursor cursor = (BatchUpdateCursor)cursors[cursorIdx];
		return cursor.getBatchSize();
	}
	public int readNextErrorRecord(int cursorIdx)
	{
		BatchUpdateCursor cursor = (BatchUpdateCursor)cursors[cursorIdx];
		int res=cursor.readNextErrorRecord();

                if (res == 1) {
                    do {
                        context.globals.Gx_eop = DefaultExceptionErrorHandler.
                                                 ERROPT_DEFAULT;
                        try {
                            helper.getErrorResults(cursorIdx,
                                    new BufferIFieldGetter((Object[]) cursor.
                                    getBlockRecords()[cursor.errorRecordIndex]),
                                    cursor.errorRecords);
                        } catch (SQLException e) {
                            DefaultExceptionErrorHandler.handleSQLError(
                                    errorHandler, e,
                                    context, remoteHandle, helper,
                                    cursors[cursorIdx]);
                        }
                    } while (context.globals.Gx_eop ==
                             DefaultExceptionErrorHandler.ERROPT_RETRY);
                }
                return res;

	}
	public int recordCount(int cursorIdx)
	{
		BatchUpdateCursor cursor = (BatchUpdateCursor)cursors[cursorIdx];
		return cursor.getRecordCount();
	}
	public void execute(int cursorIdx)
	{
		execute(cursorIdx, null);
	}
	public void executeBatch(int cursorIdx)
	{
            execute(cursorIdx, null, false);
	}
	public synchronized void execute(int cursorIdx, Object[] parms)
	{
            execute(cursorIdx, parms, true);
        }

	private void execute(int cursorIdx, Object[] parms, boolean preExecute)
	{
		
		Cursor cursor = cursors[cursorIdx];
		byte[] hasValues = null;
		
		if (ApplicationContext.getInstance().getReorganization() && cursor instanceof ForEachCursor && GXReorganization.executedBefore(cursor.mCursorId))
		{ 
			return;
		}
		
                int retryCount=0;
                DataSource ds = getDataSourceNoException();
				int maxRetryCount = (ds == null)?1:ds.lockRetryCount;
               if (preExecute) {
                    do {
                        context.globals.Gx_eop = DefaultExceptionErrorHandler.
                                                 ERROPT_DEFAULT;
                        try {
                            hasValues = cursor.preExecute(cursorIdx, this, getDataSource(), parms);
                        } catch (SQLException e) {
                            DefaultExceptionErrorHandler.handleSQLError(errorHandler, e,
                                    context, remoteHandle, helper, cursors[cursorIdx]);
                        }
                        retryCount = retryCount + 1;
                    } while (context.globals.Gx_eop ==
                             DefaultExceptionErrorHandler.ERROPT_RETRY);
                }

		if	(context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_IGNORE)
		{
			dynConstraints = null;
			return;
		}

		int retries = 0;
        boolean retryExecute;
        do
        {
			retryExecute = false;
		if	(parms != null)
		{
			                retryCount=0;
			do
			{
				context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
				try
				{
					if (hasValues != null)
					{
						Object[] parmsNew = new Object[parms.length+hasValues.length];
						for (int i = 0; i < hasValues.length; i++)
						{
							parmsNew[i] = new Byte(hasValues[i]);
						}
						System.arraycopy(parms, 0, parmsNew, hasValues.length, parms.length);
						try
						{
							helper.setParameters(cursorIdx, (IFieldSetter) cursor.mPreparedStatement, parmsNew);
						}catch(Exception e)
						{
								System.err.println("Set dynamic parameters warning " + e.getMessage());
						}
					}
					else
					{
						helper.setParameters(cursorIdx, (IFieldSetter) cursor.mPreparedStatement, parms);
					}
				}
				catch (SQLException e)
				{
					DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
				}
			                        retryCount = retryCount+1;
			}
			while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY);
		}

		if	(context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_IGNORE)
		{
			dynConstraints = null;
			return;
		}
			        retryCount=0;
		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			cursor.status = 0;
			try
			{
				cursor.postExecute(this, getDataSource());
			}
			catch (SQLException e)
			{
                            if (getDataSource().dbms.connectionClosed(e))
                            {
                                try
                                {
										this.getConnection().setError();
										this.execute(cursorIdx, parms);
										return;
                                }
                                catch (SQLException exc)
                                {
                                    DefaultExceptionErrorHandler.handleSQLError(errorHandler, exc, context, remoteHandle, helper, cursors[cursorIdx]);
                                }
                            }				
                            if (getDataSource().dbms.rePrepareStatement(e))
                            {
                                try
                                {
                                    this.getConnection().rePrepareStatement(cursor);
                                    this.execute(cursorIdx, parms);
                                    return;
                                }
                                catch (SQLException exc)
                                {
                                    DefaultExceptionErrorHandler.handleSQLError(errorHandler, exc, context, remoteHandle, helper, cursors[cursorIdx]);
                                }
                            }
					if ((retries < 2) && (cursor instanceof UpdateCursor) && ((UpdateCursor) cursor).getRetryExecute())
					{
                          retryExecute = true;
                          try
                          {
                            cursor.mPreparedStatement.clearParameters();
                          }
                          catch(SQLException e1)
						  {
                            DefaultExceptionErrorHandler.handleSQLError(errorHandler, e1, context, remoteHandle, helper, cursors[cursorIdx]);
                          }
                      }
                      else
					  {
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
					  }
				}
			    retryCount = retryCount + 1;
			}
			while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY  && (maxRetryCount == 0 || retryCount < maxRetryCount));
			retries++;
		}
        while(retryExecute);

		if	(context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_IGNORE)
		{
			dynConstraints = null;
			return;
		}

		if	(cursor instanceof ForEachCursor)
		{
			// Este if estaria de mas.
			if	(cursor.status != cursor.LOCKED)
			{
				readNext(cursorIdx);
				if	(cursor.status == cursor.EOF)
				{
					((ForEachCursor) cursor).clearBuffers();
				}
			}
		}
		else if	(cursor instanceof CallCursor)
		{
                        retryCount = 0;
			do
			{
				context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
				try
				{
					helper.getResults(cursorIdx, (GXCallableStatement) cursor.mPreparedStatement, ((CallCursor) cursor).buffers);
				}
				catch (SQLException e)
				{
					DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
				}
                                finally
                                {
                                    ((GXCallableStatement) cursor.mPreparedStatement).setNotInUse();
                                }
                                retryCount = retryCount + 1;
			}
			while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY  && (maxRetryCount == 0 || retryCount < maxRetryCount));
		}
		dynConstraints = null;

	}

	static Object lock = new Object();
	
	public void readNext(int cursorIdx)
	{
		ForEachCursor cursor = (ForEachCursor) cursors[cursorIdx];
                int retryCount=0;
                DataSource ds = getDataSourceNoException();
				int maxRetryCount = (ds == null)?1:ds.lockRetryCount;

                retryCount = 0;
		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			try
			{
				if ( (cursor.rslt != null) && (cursor.next(dataSource)) )
				{
					if	(cursor.status == 0)
					{
						helper.getResults(cursorIdx, cursor.rslt, cursor.buffers);
					}
				}
				else
				{
					cursor.status = Cursor.EOF;
				}
			}
			catch (SQLException e)
			{
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
			}
                        retryCount = retryCount + 1;
		}
		while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY  && (maxRetryCount == 0 || retryCount < maxRetryCount));
	}

	public void close(int cursorIdx)
	{
		Cursor cursor = (Cursor) cursors[cursorIdx];
		
		if (ApplicationContext.getInstance().getReorganization() && cursor instanceof ForEachCursor && GXReorganization.executedBefore(cursor.mCursorId))
		{
			return;
		}
          int retryCount=0;
          DataSource ds = getDataSourceNoException();
		  int maxRetryCount = (ds == null)?1:ds.lockRetryCount;
		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			try
			{
				cursor.close();
			}
			catch (SQLException e)
			{
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
			}
		}
		while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY && (maxRetryCount == 0 || retryCount < maxRetryCount));
		
		if (ApplicationContext.getInstance().getReorganization() && cursor instanceof ForEachCursor)
		{
			GXReorganization.addExecutedStatement(cursor.mCursorId);
		}		
	}
   
    public void rollback(String dataSourceName) {
		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			try {
				Application.getConnectionManager(context).rollback(context, remoteHandle, dataSourceName);
			}
			catch (SQLException e)
			{
				Cursor dummyCursor = new ForEachCursor("rollback", "rollback",false, DataStoreHelperBase.GX_NOMASK + DataStoreHelperBase.GX_MASKLOOPLOCK, false, getHelper(),1,0,true );
				dummyCursor.status = Cursor.UNEXPECTED_DBMS_ERROR;
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, dummyCursor);
			}
		}
		while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY);
	}
    
    public void commit(String dataSourceName) {
		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			try {
				this.getConnection().commit();
			}
			catch (SQLException e)
			{
				Cursor dummyCursor = new ForEachCursor("commit", "commit",false, DataStoreHelperBase.GX_NOMASK + DataStoreHelperBase.GX_MASKLOOPLOCK, false, getHelper(),1,0,true );
				dummyCursor.status = Cursor.UNEXPECTED_DBMS_ERROR;
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, dummyCursor);
			}
		}
		while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY);
	}

	public String userId()
	{
		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			try {

				return this.getConnection().getUserName();
			}
			catch (SQLException e)
			{
				Cursor dummyCursor = new ForEachCursor("userId", "userId",false, DataStoreHelperBase.GX_NOMASK + DataStoreHelperBase.GX_MASKLOOPLOCK, false, getHelper(),1,0,true );
				dummyCursor.status = Cursor.UNEXPECTED_DBMS_ERROR;
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, dummyCursor);
				return "";
			}
		}
		while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY);
	}

	public Date serverNow()
	{
		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			try {

				return this.getConnection().getDateTime();
			}
			catch (SQLException e)
			{
				Cursor dummyCursor = new ForEachCursor("serverNow", "serverNow",false, DataStoreHelperBase.GX_NOMASK + DataStoreHelperBase.GX_MASKLOOPLOCK, false, getHelper(),1,0,true );
				dummyCursor.status = Cursor.UNEXPECTED_DBMS_ERROR;
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, dummyCursor);
				return CommonUtil.nullDate();
			}
		}
		while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY);
	}
	

	public IDataStoreHelper getHelper()
	{
		return helper;
	}

	public void release()
	{
	}
}
