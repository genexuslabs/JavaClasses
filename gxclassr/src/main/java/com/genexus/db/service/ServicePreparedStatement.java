package com.genexus.db.service;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import com.genexus.db.driver.GXConnection;
import com.genexus.db.driver.GXDBDebug;

public abstract class ServicePreparedStatement implements IServicePreparedStatement
{
    public Object[] parms;
    public Connection con;
    public GXDBDebug log;
    public int handle;

    public ServicePreparedStatement(Connection con, Object[] parms, GXConnection gxCon)
    {
        this.parms = parms;
        this.con = con;
        this.log = gxCon.getLog();
        this.handle = gxCon.getHandle();
    }

    @Override
    public int executeUpdate() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException
    {
        parms[parameterIndex-1] = null;
    }

    @Override
    public void setBoolean(int parameterIndex, boolean value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setByte(int parameterIndex, byte value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setShort(int parameterIndex, short value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setInt(int parameterIndex, int value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setLong(int parameterIndex, long value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setFloat(int parameterIndex, float value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setDouble(int parameterIndex, double value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setString(int parameterIndex, String value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setBytes(int parameterIndex, byte[] value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setDate(int parameterIndex, Date value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setTime(int parameterIndex, Time value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
	@Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void clearParameters() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setObject(int parameterIndex, Object value, int targetSqlType) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setObject(int parameterIndex, Object value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public boolean execute() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void addBatch() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setRef(int parameterIndex, Ref value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setBlob(int parameterIndex, Blob value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setClob(int parameterIndex, Clob value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setArray(int parameterIndex, Array value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setDate(int parameterIndex, Date value, Calendar cal) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setTime(int parameterIndex, Time value, Calendar cal) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp value, Calendar cal) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        parms[parameterIndex-1] = null;
    }

    @Override
    public void setURL(int parameterIndex, URL value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setRowId(int parameterIndex, RowId value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setObject(int parameterIndex, Object value, int targetSqlType, int scaleOrLength) throws SQLException
    {
        parms[parameterIndex-1] = value;
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream value) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream value) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int executeUpdate(String sql) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void close() throws SQLException
    {
        parms = null;
        con = null;
    }

    @Override
    public int getMaxFieldSize() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setMaxFieldSize(int mavalue) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getMaxRows() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setMaxRows(int max) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getQueryTimeout() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void cancel() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void clearWarnings() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setCursorName(String name) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean execute(String sql) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getResultSet() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getUpdateCount() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean getMoreResults() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getFetchDirection() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    private int mDummyFetchSize;
    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        mDummyFetchSize = rows;
    }

    @Override
    public int getFetchSize() throws SQLException
    {
        return mDummyFetchSize;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getResultSetType() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void addBatch(String sql) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void clearBatch() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int[] executeBatch() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return con;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getResultSetHoldability() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isClosed() throws SQLException
    {
        return con == null;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isPoolable() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
}
