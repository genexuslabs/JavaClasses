package com.genexus.db;

import java.sql.SQLException;

public interface IFieldGetter
{
	boolean wasNull() throws SQLException;
        void resetWasNullHits();
	String getLongVarchar(int columnIndex) throws SQLException;
	String getVarchar(int columnIndex) throws SQLException;
	String getString(int columnIndex, int length) throws SQLException;
	byte getByte(int columnIndex) throws SQLException;
	short getShort(int columnIndex) throws SQLException;
	int getInt(int columnIndex) throws SQLException;
	long getLong(int columnIndex) throws SQLException;
	float getFloat(int columnIndex) throws SQLException;
	double getDouble(int columnIndex) throws SQLException;
	java.util.Date getGXDateTime(int columnIndex, boolean hasMilliSeconds) throws SQLException;
	java.util.Date getGXDateTime(int columnIndex) throws SQLException;
	java.util.Date getGXDate(int columnIndex) throws SQLException;
	String getString(int columnIndex) throws SQLException;
	boolean getBoolean(int columnIndex) throws SQLException;
	java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException;
	byte[] getBytes(int columnIndex) throws SQLException;
	java.sql.Date getDate(int columnIndex) throws SQLException;
	java.sql.Time getTime(int columnIndex) throws SQLException;
	java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException;

	String getBLOBFile(int columnIndex) throws SQLException;
	String getBLOBFile(int columnIndex, String extension) throws SQLException;
	String getBLOBFile(int columnIndex, String extension, String name) throws SQLException;
	String getMultimediaFile(int columnIndex, String name) throws SQLException;
	String getMultimediaUri(int columnIndex) throws SQLException;
	java.util.UUID getGUID(int columnIndex) throws SQLException;
}
