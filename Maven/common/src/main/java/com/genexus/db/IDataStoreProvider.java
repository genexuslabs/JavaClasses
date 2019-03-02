package com.genexus.db;

import com.genexus.GXRuntimeException;
import com.genexus.IErrorHandler;
import java.util.Date;

/**
*	Esta es la interface que usan los programas generados para recorrer los forechs
*	sin importar si son locales o remotos.
*
*/
public interface IDataStoreProvider
{
	void execute(int cursor) throws GXRuntimeException;
	void execute(int cursor, Object[] parms) throws GXRuntimeException;
	
	void readNext(int cursor) throws GXRuntimeException;
	void close(int cursor) throws GXRuntimeException;

	int getStatus(int cursor);
	void release();

	void setErrorHandler(IErrorHandler errorHandler);
	void dynParam(int cursorId, Object [] args);
	void executeBatch(int cursor);
	void addRecord(int cursor, Object[] recordValues);
	void initializeBatch(int cursor, int batchSize, Object instance, String method);
	int getBatchSize(int cursor);
	int readNextErrorRecord(int cursor);
	void setErrorBuffers(int cursor, Object[] errorBuffers);
	int recordCount(int cursor);
	Date serverNow();
	String userId();
	void commit(String dataSourceName);
	void rollback(String dataSourceName);
}
