package com.genexus.performance;

public interface DataStoreProviderJMXMBean
{
	long getTotalSQLStatementCount();
	long getSelectSQLStatementCount();	
	long getUpdateSQLStatementCount();
	long getDeleteSQLStatementCount();
	long getInsertSQLStatementCount();	
	long getStoredProcedureCount();
	long getSQLCommandCount();
	
	void dumpDataStoreInformation();
}
