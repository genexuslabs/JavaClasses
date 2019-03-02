package com.genexus.common.classes;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IGXPreparedStatement {

	int getHandle();

	Connection getConnection() throws SQLException;

	int[] executeBatch() throws SQLException;

	void notInUse();

	void addBatch(Object[] parms) throws SQLException;

	int getBatchSize();

	void setBatchSize(int size);

	void setOnCommitInstance(Object instance);

	void setOnCommitMethod(String method);

	int getRecordCount();

	void resetRecordCount();

	Object getOnCommitInstance();

	String getOnCommitMethod();

	void skipSetBlobs(boolean b);

	int executeUpdate() throws SQLException;

	void setString(int i, String rowId) throws SQLException;

	ResultSet executeQuery(boolean hold) throws SQLException;
	ResultSet executeQuery() throws SQLException;
		

	String[] getBlobFiles();

	void setBLOBFile(Blob blob, String substring) throws SQLException;

	boolean execute() throws SQLException;

	Object[] getBatchRecords();

	void clearParameters() throws SQLException;

	void setFetchSize(int fetchSize) throws SQLException;

}
