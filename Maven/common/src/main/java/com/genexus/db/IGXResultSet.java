package com.genexus.db;

import java.sql.Blob;
import java.sql.SQLException;

public interface IGXResultSet {

	boolean next() throws SQLException;

	Blob getBlob(int index) throws SQLException;

	void close() throws SQLException;

}
