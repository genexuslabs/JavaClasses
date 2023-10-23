package com.genexus.db;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.genexus.*;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXCallableStatement;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.driver.GXDBMS;
import com.genexus.db.driver.GXResultSet;
import com.genexus.performance.DataStoreProviderInfo;
import com.genexus.performance.DataStoreProviderJMX;
import com.genexus.performance.DataStoreProvidersJMX;
import com.genexus.performance.SentenceInfo;

public class DataStoreProvider extends DataStoreProviderBase implements
		IDataStoreProvider {
	protected ILocalDataStoreHelper helper;

	// JMX Properties
	private static AtomicLong sentenceCount = new AtomicLong(0);
	private static AtomicLong sentenceSelectCount = new AtomicLong(0);
	private static AtomicLong sentenceUpdateCount = new AtomicLong(0);
	private static AtomicLong sentenceDeleteCount = new AtomicLong(0);
	private static AtomicLong sentenceInsertCount = new AtomicLong(0);
	private static AtomicLong sentenceCallCount = new AtomicLong(0);
	private static AtomicLong sentenceDirectSQLCount = new AtomicLong(0);
	private static ConcurrentHashMap<String, DataStoreProviderInfo> dataStoreProviders = new ConcurrentHashMap<String, DataStoreProviderInfo>();

	private static AtomicBoolean firstTime = new AtomicBoolean(true);

	public DataStoreProvider(ModelContext context, int remoteHandle)
	{
		super(context, remoteHandle);
	}

	public DataStoreProvider(ModelContext context, int remoteHandle, ILocalDataStoreHelper helper, Object[] buffers)
	{
		super(context, remoteHandle);

		//JMX Enabled
		if (Application.isJMXEnabled())
			if (firstTime.get())
			{
				DataStoreProvidersJMX.CreateDataStoreProvidersJMX();
				firstTime.set(false);
			}

		this.helper = helper;
		this.cursors = helper.getCursors();
		setOutputBuffers(buffers);

		//JMX
		addDataStoreProviderInfo(helper.getClass().getName());

	}

        public void setErrorBuffers(int cursorIdx, Object[] buffers)
        {
            ((BatchUpdateCursor)cursors[cursorIdx]).errorRecords = buffers;
        }
	private void setOutputBuffers(Object[] buffers)
	{
		for (int i = 0; i < cursors.length; i++)
		{
			if	(cursors[i] instanceof IForEachCursor)
				((IForEachCursor) cursors[i]).setOutputBuffers((Object []) buffers[i]);
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

                if	(parms != null && (cacheIterator == null || cacheIterator[cursorIdx] == null))
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

						cursor.setBatchSize(batchSize);
						cursor.onCommitEvent(instance, method);
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
		
		if (ApplicationContext.getInstance().getReorganization() && (cursor instanceof ForEachCursor || cursor instanceof UpdateCursor) && GXReorganization.executedBefore(cursor.mCursorId))
		{ 
			return;
		}
		

		//JMX Counter
		if (Application.isJMXEnabled())
		{
			beginExecute = new Date();
			incSentencesCount(helper.getClass().getName(), cursor);
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
		//JMX Counter
		if (Application.isJMXEnabled())
			beginExecute();

		if	(context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_IGNORE)
		{
			dynConstraints = null;
			return;
		}

		int retries = 0;
		boolean retryExecute;
		do {
			retryExecute = false;
			if (parms != null
					&& (cacheIterator == null || cacheIterator[cursorIdx] == null)) {
				retryCount = 0;
				do {
					setParameters(cursorIdx, parms, cursor, hasValues);
					retryCount = retryCount + 1;
				} while (context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY);
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
				executeSavePointOperation("SAVEPOINT gxselectforupdate", cursor);				
				cursor.postExecute(this, getDataSource());
				executeSavePointOperation("RELEASE SAVEPOINT gxselectforupdate", cursor);				
			}
			catch (SQLException e)
			{
                            if (getDataSource().dbms.connectionClosed(e))
                            {
			                    try
                                {
									if (!this.getConnection().getUncommitedChanges())
									{
										this.getConnection().setError();
										this.execute(cursorIdx, parms);
										return;
									}
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
					  
					
				    if (maxRetryCount != 0 && retryCount == maxRetryCount -1)
				    {
				    	cursor.mPreparedStatement.notInUse();
				    	context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_CANCEL;
				    	DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
				    }					  
				    else
				    {

				    	if (ds.waitRecord > 0  && context.globals.Gx_eop == DefaultExceptionErrorHandler.ERROPT_RETRY)
				    	{
					    	try
					    	{
					    		Thread.sleep(ds.waitRecord * 1000);
					    	}
								catch (InterruptedException ex)
								{
								}
							}
							executeSavePointOperation("ROLLBACK TO SAVEPOINT gxselectforupdate", cursor);
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
			cursor.mPreparedStatement.notInUse();
			dynConstraints = null;
			return;
		}

		if	(cursor instanceof IForEachCursor)
		{
			// Este if estaria de mas.
			if	(cursor.status != Cursor.LOCKED)
			{
				readNext(cursorIdx);
				if	(cursor.status == Cursor.EOF)
				{
					((IForEachCursor) cursor).clearBuffers();
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

		//JMX Counter
		if (Application.isJMXEnabled())
			endExecute(helper.getClass().getName(), cursor);
	}
	
		private void setParameters(int cursorIdx, Object[] parms, Cursor cursor,
			byte[] hasValues) {
		context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
		try {
			if (hasValues != null) {
				Object[] parmsNew = new Object[parms.length + hasValues.length];
				for (int i = 0; i < hasValues.length; i++) {
					parmsNew[i] = new Byte(hasValues[i]);
				}
				System.arraycopy(parms, 0, parmsNew, hasValues.length,
						parms.length);
				try {
					helper.setParameters(cursorIdx, (IFieldSetter) cursor.mPreparedStatement,
							parmsNew);
				} catch (Exception e) {
					System.err.println("Set dynamic parameters warning "
							+ e.getMessage());
				}
			} else {
				helper.setParameters(cursorIdx, (IFieldSetter) cursor.mPreparedStatement,
						parms);
			}
		} catch (SQLException e) {
			DefaultExceptionErrorHandler.handleSQLError(errorHandler, e,
					context, remoteHandle, helper, cursors[cursorIdx]);
		}
	}
	
	
	private void executeSavePointOperation(String operation, Cursor cursor) 
	{
		if (getDataSource().dbms.getId() == GXDBMS.DBMS_POSTGRESQL && GXutil.dbmsVersion( context, remoteHandle, getDataSource().name) > 7 && cursor instanceof ForEachCursor && cursor.isCurrentOf())
		{
	    	try
	    	{
	    		SentenceProvider.executeStatement(this, operation);
	    	}
	    	catch (Exception ex)
	    	{
	    		System.err.println(operation + " error " + ex.getMessage());
	    		ex.printStackTrace();
	    	}			
		}
	}	

	static Object lock = new Object();
	
	public void readNext(int cursorIdx)
	{
		IForEachCursor cursor = (IForEachCursor) cursors[cursorIdx];

		if(cacheIterator != null && cacheIterator[cursorIdx] != null)
		{ // Si estoy utilizando datos cacheados
			if(!cacheIterator[cursorIdx].hasMoreElements())
			{
				cursor.setStatus(Cursor.EOF);
				return;
			}
			try
			{
					synchronized (lock)
					{
						IFieldGetter cacheIter = (IFieldGetter)cacheIterator[cursorIdx].nextElement();
						cacheIter.resetWasNullHits();
						helper.getResults(cursorIdx, cacheIter, cursor.getBuffers());
					}
			}catch(SQLException e)
			{ // No deber�a pasar nunca...
				e.printStackTrace();
			}
			return;
		}
                int retryCount = 0;
                DataSource ds = getDataSourceNoException();
				int maxRetryCount = (ds == null)?1:ds.lockRetryCount;

		do
		{
			context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			try {
				GXResultSet result = null;
				if (cursor.hasResult())
				{
					result = (GXResultSet) cursor.getResultSet();
				}
				if ((result != null) && (cursor.next(dataSource))) {
					if (cursor.getStatus() == 0) {
							helper.getResults(cursorIdx, result, cursor.getBuffers());
						if (cacheValue != null && cacheValue[cursorIdx] != null) { // Si
																					// estoy
																					// cacheando
																					// resultados
							TimeZone cachedValueTimeZone = context.getClientTimeZone();
							if (cachedValueTimeZone != null)
								cacheValue[cursorIdx].setTimeZone(cachedValueTimeZone);
							cacheValue[cursorIdx].addItem(cursor.getBuffers(),
									result.getResultRegBytes());
						}
					}
				} else {
					cursor.setStatus(Cursor.EOF);
					if(cacheValue != null && cacheValue[cursorIdx] != null)
					{ // Si llegamos al final del resultSet, y estabamos cacheando, metemos
					  // el CacheValue al cache
						CacheFactory.getInstance().set(CacheFactory.CACHE_DB, cacheValue[cursorIdx].getKey().toString(),  cacheValue[cursorIdx]);
					}
				}
			}
			catch (SQLException e)
			{
				DefaultExceptionErrorHandler.handleSQLError(errorHandler, e, context, remoteHandle, helper, cursors[cursorIdx]);
				if (context.globals.Gx_eop != DefaultExceptionErrorHandler.ERROPT_RETRY)
					cursor.setStatus(Cursor.EOF);
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
			if (!context.inErrorHandler)
			{
				context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
			}
			try {
					cursor.close();
				if (cursor.isForFirst() && cursor.status != Cursor.EOF
						&& cacheValue != null && cacheValue[cursorIdx] != null) { // Si
																					// estoy
																					// cacheando
																					// resultados,
																					// estoy
																					// en
																					// un
																					// cursor
																					// ForFirst
																					// y
																					// cierran
																					// el
																					// cursor,
																					// meto
																					// el
																					// CacheValue
																					// al
																					// cache
					CacheFactory.getInstance().set(CacheFactory.CACHE_DB,
							cacheValue[cursorIdx].getKey().toString(),
							cacheValue[cursorIdx],
							cacheValue[cursorIdx].getExpiryTimeSeconds());
					cacheValue[cursorIdx] = null;
				}
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
		if (Application.getConnectionManager(context).isConnected(remoteHandle, dataSourceName)){
			do
			{
				context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
				try {
					try {
						DBConnectionManager connection = Application.getConnectionManager(context);
						if (connection !=null)
							connection.rollback(context, remoteHandle, dataSourceName);
					}catch(RuntimeException ex){
						//Can't find connection information for datastore GAM, because Datastore is not defined in cfg.
						System.err.println("rollback: " + ex.getMessage());
					}
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
	}
    
    public void commit(String dataSourceName) {
		if (Application.getConnectionManager(context).isConnected(remoteHandle, dataSourceName)){
			do
			{
				context.globals.Gx_eop = DefaultExceptionErrorHandler.ERROPT_DEFAULT;
				try {
					try{
						GXConnection connection = this.getConnection(dataSourceName, false);
						if (connection !=null)
							connection.commit();
					}catch(RuntimeException ex){
						//Can't find connection information for datastore GAM, because Datastore is not defined in cfg.
						System.err.println("commit: " + ex.getMessage());
					}
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
		// Este es local, no hace nada en el release
	}

////////////////////////////////////////JMX Operations/////////////////////////////////////
	public static void addDataStoreProviderInfo(String key) {
		DataStoreProviderInfo dsInfo = new DataStoreProviderInfo(key);
		DataStoreProviderInfo dsInfoPrev = dataStoreProviders.putIfAbsent(key,
				dsInfo);
		if (dsInfoPrev == null) {
			if (Application.isJMXEnabled())
				DataStoreProviderJMX.CreateDataStoreProviderJMX(key);
		}
	}

	public static DataStoreProviderInfo getDataStoreProviderInfo(String key)
	{
		return (DataStoreProviderInfo) dataStoreProviders.get(key);
	}

	void beginExecute()
	{
		if (con != null)
		{
			con.setLastObjectExecuted(getHelper().getClass().getName());
			con.setFinishExecute(false);
		}

		((UserInformation)DBConnectionManager.getInstance().getUserInformation(remoteHandle)).setLastObjectExecuted(getHelper().getClass().getName());
	}

	void incSentencesCount(String key, Cursor cursor)
	{

		DataStoreProviderInfo dsInfo = getDataStoreProviderInfo(key);
		dsInfo.incSentenceCount();
		SentenceInfo sInfo;
		if (cursor.dynStatement) {
			sInfo = dsInfo.addSentenceInfo(key + "_" + cursor.mCursorId, key
					+ "_" + cursor.mCursorId + "_" + cursor.mSQLSentence);
		} else {
			sInfo = dsInfo.addSentenceInfo(key + "_" + cursor.mCursorId,
					cursor.mSQLSentence);
		}
		sInfo.incSentenceCount();

		String sqlSnt = cursor.mSQLSentence;
		sentenceCount.incrementAndGet();

		while (true) {
			if (cursor instanceof DirectStatement) {
				sentenceDirectSQLCount.incrementAndGet();
				dsInfo.incSentenceDirectSQLCount();
				break;
			}
			if (cursor instanceof CallCursor) {
				sentenceCallCount.incrementAndGet();
				dsInfo.incSentenceCallCount();
				break;
			}
			if (cursor.dynStatement
					|| sqlSnt.toUpperCase().startsWith("SELECT")) {
				sentenceSelectCount.incrementAndGet();
				dsInfo.incSentenceSelectCount();
				break;
			}
			if (sqlSnt.toUpperCase().startsWith("UPDATE")) {
				sentenceUpdateCount.incrementAndGet();
				dsInfo.incSentenceUpdateCount();
				break;
			}
			if (sqlSnt.toUpperCase().startsWith("DELETE")) {
				sentenceDeleteCount.incrementAndGet();
				dsInfo.incSentenceDeleteCount();
				break;
			}
			if (sqlSnt.toUpperCase().startsWith("INSERT")) {
				sentenceInsertCount.incrementAndGet();
				dsInfo.incSentenceInsertCount();
				break;
			}
			break;
		}
	}

	protected void endExecute(String key, Cursor cursor) {
		if (con != null)
			con.setFinishExecute(true);

		DataStoreProviderInfo dsInfo = getDataStoreProviderInfo(key);
		SentenceInfo sInfo = dsInfo.getSentenceInfo(key + "_"
				+ cursor.mCursorId);
		sInfo.setTimeExecute((System.currentTimeMillis() - this.beginExecute
				.getTime()));
	}

	public static long getSentenceCount() {
		return sentenceCount.get();
	}

	public static long getSentenceSelectCount() {
		return sentenceSelectCount.get();
	}

	public static long getSentenceUpdateCount() {
		return sentenceUpdateCount.get();
	}

	public static long getSentenceDeleteCount() {
		return sentenceDeleteCount.get();
	}

	public static long getSentenceInsertCount() {
		return sentenceInsertCount.get();
	}

	public static long getSentenceCallCount() {
		return sentenceCallCount.get();
	}

	public static long getSentenceDirectSQLCount() {
		return sentenceDirectSQLCount.get();
	}

	public static void dumpTxt() {
		try {
			PrintStream out = new PrintStream(
					new FileOutputStream(
							"DataStoreProviders_"
									+ CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date())
									+ ".log", true));
			out.println("DataStoreProviders Information");
			out.println("");
			out.println("Number of sentences : " + sentenceCount.get());
			out.println("Number of select sentences : " + sentenceSelectCount.get());
			out.println("Number of update sentences : " + sentenceUpdateCount.get());
			out.println("Number of delete sentences : " + sentenceDeleteCount.get());
			out.println("Number of insert sentences : " + sentenceInsertCount.get());
			out.println("Number of CALL sentences : " + sentenceCallCount.get());
			out.println("Number of direct SQL sentences : " + sentenceDirectSQLCount.get());
			out.println("");
			out.println("");
			for (Enumeration en = dataStoreProviders.elements(); en.hasMoreElements(); )
			{
				DataStoreProviderInfo dsInfo = (DataStoreProviderInfo) en.nextElement();
				dsInfo.dump(out);
				out.println("");
				out.println("");
			}
			out.close();
		}
		catch (java.io.IOException e)
		{
		}
	}

	public static void dump() {
		String fileName = "DataStoreProviders_"
				+ CommonUtil.getYYYYMMDDHHMMSS_nosep(new java.util.Date()) + ".xml";
		com.genexus.xml.XMLWriter writer = new com.genexus.xml.XMLWriter();
		writer.xmlStart(fileName);
		writer.writeStartElement("DataStoreProviders_Information");
		writer.writeElement("Total_SQLStatementCount", sentenceCount.get());
		writer.writeElement("Select_SQLStatementCount",
				sentenceSelectCount.get());
		writer.writeElement("Update_SQLStatementCount",
				sentenceUpdateCount.get());
		writer.writeElement("Delete_SQLStatementCount",
				sentenceDeleteCount.get());
		writer.writeElement("Insert_SQLStatementCount",
				sentenceInsertCount.get());
		writer.writeElement("StoredProcedureCount", sentenceCallCount.get());
		writer.writeElement("SQLCommandCount", sentenceDirectSQLCount.get());
		for (Enumeration<DataStoreProviderInfo> en = dataStoreProviders
				.elements(); en.hasMoreElements();) {
			DataStoreProviderInfo dsInfo = (DataStoreProviderInfo) en
					.nextElement();
			dsInfo.dump(writer);
		}
		writer.writeEndElement();
		writer.close();
	}
}
