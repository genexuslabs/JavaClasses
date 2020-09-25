package com.genexus.common.classes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import com.genexus.db.Cursor;

public abstract class AbstractGXConnection {

	public abstract PreparedStatement getStatement(String cursorId, String sqlSentence, boolean currentOf) throws SQLException;

	public abstract PreparedStatement getStatement(String cursorId, String sqlSentence, boolean currentOf, boolean batch) throws SQLException;

	public abstract CallableStatement getCallableStatement(String cursorId, String sqlSentence) throws SQLException;

	public abstract Statement createStatement() throws SQLException;

	public abstract boolean getAutoCommit() throws SQLException;

	public abstract void dropCursor(IGXPreparedStatement selStmt) throws SQLException;

	public abstract void rePrepareStatement(Cursor cursor) throws SQLException ;

	public abstract void setError() ;

	public abstract void commit() throws SQLException ;

	public abstract void flushBatchCursors(java.lang.Object o) throws SQLException;

	public abstract String getUserName() throws SQLException;

	public abstract Date getDateTime() throws SQLException ;

	public abstract void close() throws SQLException;

	public abstract void dropAllCursors();

}
