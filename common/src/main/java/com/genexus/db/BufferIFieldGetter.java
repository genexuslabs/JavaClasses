package com.genexus.db;

import java.sql.*;

public class BufferIFieldGetter implements IFieldGetter {
    private Object[] value;
    public BufferIFieldGetter(Object[] value) {
        this.value = value;
    }

    public Object[] getValue() {
        return value;
    }

    public boolean wasNull() throws SQLException {
        return value == null;
    }

    public void resetWasNullHits()
    {
    	}

    public String getLongVarchar(int columnIndex) throws SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public String getVarchar(int columnIndex) throws SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public String getString(int columnIndex, int length) throws SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return ((Byte) value[columnIndex - 1]).byteValue();
    }

    public short getShort(int columnIndex) throws SQLException {
        return ((Short) value[columnIndex - 1]).shortValue();
    }

    public int getInt(int columnIndex) throws SQLException {
        return ((Integer) value[columnIndex - 1]).intValue();
    }

    public long getLong(int columnIndex) throws SQLException {
        return ((Long) value[columnIndex - 1]).longValue();
    }

    public float getFloat(int columnIndex) throws SQLException {
        return ((Float) value[columnIndex - 1]).floatValue();
    }

    public double getDouble(int columnIndex) throws SQLException {
        return ((Double) value[columnIndex - 1]).doubleValue();
    }

    public java.util.Date getGXDateTime(int columnIndex) throws SQLException {
        return ((java.util.Date) value[columnIndex - 1]);
    }

    public java.util.Date getGXDateTime(int columnIndex, boolean hasMilliSeconds) throws SQLException {
        return ((java.util.Date) value[columnIndex - 1]);
    }

    public java.util.Date getGXDate(int columnIndex) throws SQLException {
        return ((java.util.Date) value[columnIndex - 1]);
    }

    public String getString(int columnIndex) throws SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return ((Boolean) value[columnIndex - 1]).booleanValue();
    }

    public java.math.BigDecimal getBigDecimal(int columnIndex, int scale) throws
            SQLException {
        return ((java.math.BigDecimal) value[columnIndex - 1]);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return ((byte[]) value[columnIndex - 1]);
    }

    public java.sql.Date getDate(int columnIndex) throws SQLException {
        return ((java.sql.Date) value[columnIndex - 1]);
    }

    public java.sql.Time getTime(int columnIndex) throws SQLException {
        return ((java.sql.Time) value[columnIndex - 1]);
    }

    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
        return ((java.sql.Timestamp) value[columnIndex - 1]);
    }

    public String getBLOBFile(int columnIndex) throws SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public String getBLOBFile(int columnIndex, String extension) throws
            SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public String getBLOBFile(int columnIndex, String extension, String name) throws
            SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public String getMultimediaFile(int columnIndex, String name) throws
    		SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public String getMultimediaUri(int columnIndex) throws SQLException {        
        return getMultimediaUri(columnIndex, true);
    }

    public String getMultimediaUri(int columnIndex, boolean absPath) throws SQLException {
        return ((String) value[columnIndex - 1]);
    }

    public java.util.UUID getGUID(int columnIndex) throws SQLException {
        return ((java.util.UUID) value[columnIndex - 1]);
    }
}

