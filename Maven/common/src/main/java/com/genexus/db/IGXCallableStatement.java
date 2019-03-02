package com.genexus.db;

import java.sql.SQLException;

public interface IGXCallableStatement {

	void registerOutParameter(int i, int c) throws SQLException;

	String getString(int i) throws SQLException;

}
