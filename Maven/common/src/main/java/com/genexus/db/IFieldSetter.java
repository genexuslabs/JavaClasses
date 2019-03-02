package com.genexus.db;

import java.sql.SQLException;

public interface IFieldSetter
{
    public void setNull(int index, int sqlType) throws SQLException;
    public void setBoolean(int index, boolean value) throws SQLException;
    public void setByte(int index, byte value) throws SQLException;
    public void setShort(int index, short value) throws SQLException;
    public void setInt(int index, int value) throws SQLException;
    public void setLong(int index, long value) throws SQLException;
    public void setFloat(int index, float value) throws SQLException;
    public void setDouble(int index, double value) throws SQLException;
    public void setBigDecimal(int index, java.math.BigDecimal value, int decimals) throws SQLException;
    public void setBigDecimal(int index, double value, int decimals) throws SQLException;
	public void setVarchar(int index, String value) throws SQLException;
	public void setVarchar(int index, String value, int length) throws SQLException;
	public void setLongVarchar(int index, String value) throws SQLException;
	public void setNLongVarchar(int index, String value) throws SQLException;
	public void setLongVarchar(int index, String value, int maxLength) throws SQLException;
    public void setString(int index, String value, int length) throws SQLException;
    public void setString(int index, String value) throws SQLException;
    public void setGXDbFileURI(int index, String fileName, String blobPath, int length) throws SQLException;
    public void setBytes(int index, byte value[]) throws SQLException;
	public void setDateTime(int index, java.util.Date value, boolean onlyTime) throws SQLException;
    public void setDateTime(int index, java.util.Date value, boolean onlyTime, boolean onlyDate, boolean hasmilliseconds) throws SQLException;
    public void setDate(int index, java.util.Date value) throws SQLException;
    public void setDate(int index, java.sql.Date value) throws SQLException;
    public void setTime(int index, java.sql.Time value) throws SQLException;
    public void setTimestamp(int index, java.sql.Timestamp value) throws SQLException;
    public void setBLOBFile(int index, String fileName) throws SQLException;

	public void setVarchar(int index, String value, int length, boolean allowsNull) throws SQLException;
	public void setLongVarchar(int index, String value, boolean allowsNull) throws SQLException;
	public void setNLongVarchar(int index, String value, boolean allowsNull) throws SQLException;
	public void setLongVarchar(int index, String value, int maxLength, boolean allowsNull) throws SQLException;
	public void setGUID(int index, java.util.UUID value) throws SQLException;
    public void setParameterRT(String name, String value);

}
