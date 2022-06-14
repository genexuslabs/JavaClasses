package com.genexus.db.driver;

import java.io.InputStream;
import java.sql.*;
import java.util.Date;

import com.genexus.CommonUtil;

public class GXDBMSdameng implements GXDBMS {

	public void setDatabaseName(String dbName) {
	}

	public String getDatabaseName() {
		return "";
	}

	public void setInReorg() {
	}

	public ResultSet executeQuery(PreparedStatement stmt, boolean hold) throws SQLException {
		return stmt.executeQuery();
	}

	public int executeUpdate(PreparedStatement stmt) throws SQLException {
		return stmt.executeUpdate();
	}

	public boolean execute(PreparedStatement stmt) throws SQLException {
		return stmt.execute();
	}

	public int[] executeBatch(Statement stmt) throws SQLException {
		return stmt.executeBatch();
	}

	public boolean isAlive(GXConnection con) {
		try {
			serverDateTime(con);
		}
		catch (SQLException e) {
			return false;
		}

		return true;
	}


	public boolean DataTruncation(SQLException e) {
		return false;
	}

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean useReadOnlyConnections() {
		return true;
	}

	public boolean EndOfFile(SQLException e) {
		return	(e.getErrorCode() == 1403 || e.getErrorCode() == 100);
	}
	
	public boolean ReferentialIntegrity(SQLException e) {
		return (e.getErrorCode() == -6607);
	}
	
	public boolean DuplicateKeyValue(SQLException e) {
		return	(e.getErrorCode() == -6602);
	}

	public boolean ObjectLocked(SQLException e) {
		return	(	e.getErrorCode() == 54);
	}

	public boolean ObjectNotFound(SQLException e) {
		return	(	e.getErrorCode() == 942	 || 
					e.getErrorCode() == 950	 ||
					e.getErrorCode() == 1418 ||
					e.getErrorCode() == 1432 ||
					e.getErrorCode() == 2289 ||
					e.getErrorCode() == 2443 ||
					e.getErrorCode() == 4080);
	}

	public java.util.Date nullDate() {
		return CommonUtil.ymdhmsToT_noYL(1, 1, 1, 0, 0, 0);
	}

	public boolean useDateTimeInDate() {
		return false;
	}

	public boolean useCharInDate() {
		return false;
	}

	public void setConnectionProperties(java.util.Properties props) {
		props.put("fixedString", "true");
	}

	public void onConnection(GXConnection con) {
	}
	
	public java.util.Date serverDateTime(GXConnection con) throws SQLException {
		ResultSet rslt = con.getStatement("_ServerDT_", "SELECT LOCALTIMESTAMP()", false).executeQuery();
		
		rslt.next();
		Date value = rslt.getTimestamp(1);
		rslt.close();

		return value;
	}
	
	public String serverVersion(GXConnection con) throws SQLException {
		return "";
	}	
	
	public String connectionPhysicalId(GXConnection con) {
		try {
			ResultSet rslt = con.getStatement("_ConnectionID_", "SELECT SESSID() AS S", false).executeQuery();
		
			rslt.next();
			int value = rslt.getInt(1);
			rslt.close();

			return String.valueOf(value);
		}
		catch (SQLException e) {
			return "";
		}
	}

	public boolean getSupportsAutocommit() {
		return true;
	}

	public void commit(Connection con) throws SQLException {
		//Las reorgs corren en modo autocommit con lo cual no se debe hacer commit ni rollback implicitos.
		if	(!com.genexus.ApplicationContext.getInstance().getReorganization()) {
			con.commit();
		}
	}

	public void rollback(Connection con) throws SQLException {
		//Las reorgs corren en modo autocommit con lo cual no se debe hacer commit ni rollback implicitos.
		if	(!com.genexus.ApplicationContext.getInstance().getReorganization()) {
			con.rollback();
		}
	}

	public boolean ignoreConnectionError(SQLException e) {
		return false;
	}

	public boolean rePrepareStatement(SQLException e) {
		return false;
	}

	public boolean getSupportsQueryTimeout() {
		return true;
	}

	public boolean useStreamsInNullLongVarchar() {
		return false;
	}

	public boolean useStreamsInLongVarchar() {
		return true;
	}

	private static java.lang.reflect.Method PUTBYTES;
	
	/** Setea los datos de un blob
	 * El parametro blob debe ser una instancia de java.sql.Blob o descendiente
	 * Esta puesto como Object para que compile sin problemas en JSharp
	 */
	public void setBlobData(Object blob, InputStream stream, int length)throws Exception {
		try {
			byte [] bytes = new byte[length];
			com.genexus.PrivateUtilities.readFully(stream, bytes, 0, length);
			if(PUTBYTES == null) {
				PUTBYTES = Class.forName("java.sql.Blob").getMethod("setBytes", new Class[]{long.class, byte[].class});
			}
			PUTBYTES.invoke(blob, new Object[]{new Long(1), bytes});
		}
		catch(Exception e) {
			System.err.println(e.toString());
			throw e;
		}
	}
	
	public int getId() {
		return DBMS_DAMENG;
	}

	public int getLockRetryCount(int lockRetryCount, int waitRecord){
          return lockRetryCount * waitRecord * 2;
	}
		
	public boolean connectionClosed(SQLException e) {
		return (e.getErrorCode() == 17002);
	}
}
