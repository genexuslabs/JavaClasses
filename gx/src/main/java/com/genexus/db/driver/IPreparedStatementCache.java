/*
 * Created on Apr 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.genexus.db.driver;

import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author aaguiar
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IPreparedStatementCache {
	public CallableStatement getCallableStatement(
		int handle,
		String index,
		String sqlSentence)
		throws SQLException;
	public int getUsedCursors();
	public int getUsedCursorsJMX();
	public PreparedStatement getStatement(
		int handle,
		String index,
		String sqlSentence,
		boolean currentOf)
		throws SQLException;
	public PreparedStatement getStatement(
		int handle,
		String index,
		String sqlSentence,
		boolean currentOf,
		boolean callable,
                boolean batch)
		throws SQLException;
	/**
	 * Hace un drop de todos los cursores preparados del pool de cursores.
	 */
	public void dropAllCursors();
        public void freeAllCursors();
	/**
	 * Marca un cursor como "Not in use", de modo que pueda ser reutilizado.
	 */
	public void setNotInUse(GXPreparedStatement stmt);
	public void dump(PrintStream out);
	
	public void dropCursor(GXPreparedStatement stmt);
}
