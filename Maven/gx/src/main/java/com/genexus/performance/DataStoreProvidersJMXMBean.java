package com.genexus.performance;

public interface DataStoreProvidersJMXMBean
{
	long getTotalSQLStatementCount();
	long getSelectSQLStatementCount();	
	long getUpdateSQLStatementCount();
	long getDeleteSQLStatementCount();
	long getInsertSQLStatementCount();	
	long getStoredProcedureCount();
	long getSQLCommandCount();
	
	void dumpDataStoresInformation();
}
