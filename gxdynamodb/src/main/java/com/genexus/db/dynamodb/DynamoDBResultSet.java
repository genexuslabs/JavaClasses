package com.genexus.db.dynamodb;

import com.genexus.db.service.IOServiceContext;
import com.genexus.db.service.ServiceResultSet;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;

public class DynamoDBResultSet extends ServiceResultSet<AttributeValue>
{
	private final DynamoDBPreparedStatement stmt;
	public DynamoDBResultSet(DynamoDBPreparedStatement stmt) throws SQLException
	{
		this.stmt = stmt;
	}

	@Override
	public boolean next()
	{
		if(iterator.hasNext())
		{
			currentEntry = iterator.next();
			return true;
		}
		return false;
	}

	private static final IOServiceContext SERVICE_CONTEXT = null;

	private boolean lastWasNull;
	@Override
	public boolean wasNull()
	{
		return value == null || lastWasNull;
	}

	private AttributeValue getAttValue(int columnIndex)
	{
		value = (AttributeValue)stmt.query.selectList[columnIndex-1].getValue(SERVICE_CONTEXT, currentEntry);
		return value;
	}

	private long getNumeric(int columnIndex)
	{
		AttributeValue value = getAttValue(columnIndex);
		if(value != null)
		{
			lastWasNull = false;
			String sNumber = value.n();
			if (sNumber != null)
				return Long.parseLong(sNumber);
			else if (value.bool() != null)
				return value.bool() ? 1 : 0;
		}
		lastWasNull = true;
		return 0;
	}

	private double getDecimal(int columnIndex)
	{
		AttributeValue value = getAttValue(columnIndex);
		if(value != null)
		{
			lastWasNull = false;
			String sNumber = value.n();
			if (sNumber != null)
				return Double.parseDouble(sNumber);
		}
		lastWasNull = true;
		return 0;
	}

	private Instant getInstant(int columnIndex) throws SQLException
	{
		String value = getString(columnIndex);
		if(value == null)
		{
			lastWasNull = true;
			return Instant.EPOCH;
		}
		return Instant.parse(value);
	}

	@Override
	public <T> T getAs(Class<T> reference, int columnIndex, T defaultValue)
	{
		throw new UnsupportedOperationException(String.format("Data Type: %s", reference.getName()));
	}

	@Override
	public String getString(int columnIndex)
	{
		String value = DynamoDBHelper.getString(getAttValue(columnIndex));
		lastWasNull = value == null;
		return value;
	}

	@Override
	public boolean getBoolean(int columnIndex)
	{
		AttributeValue value = getAttValue(columnIndex);
		if(value != null)
		{
			Boolean boolValue = value.bool();
			if(boolValue != null)
			{
				lastWasNull = false;
				return boolValue;
			}
		}
		lastWasNull = true;
		return false;
	}

	@Override
	public byte getByte(int columnIndex)
	{
		return (byte)getNumeric(columnIndex);
	}

	@Override
	public short getShort(int columnIndex)
	{
		return (short)getNumeric(columnIndex);
	}

	@Override
	public int getInt(int columnIndex)
	{
		return (int)getNumeric(columnIndex);
	}

	@Override
	public long getLong(int columnIndex)
	{
		return getNumeric(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex)
	{
		return (float)getDecimal(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex)
	{
		return getDecimal(columnIndex);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex)
	{
		AttributeValue value = getAttValue(columnIndex);
		if(value != null)
		{
			String sNumber = value.n();
			if (sNumber != null)
			{
				lastWasNull = false;
				return new BigDecimal(sNumber);
			}
		}
		lastWasNull = true;
		return BigDecimal.ZERO;
	}

	@Override
	public java.sql.Date getDate(int columnIndex) throws SQLException
	{
		return getAs(java.sql.Date.class, columnIndex, new java.sql.Date(0));
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException
	{
		return getAs(Time.class, columnIndex, new Time(0));
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException
	{
		return java.sql.Timestamp.from(getInstant(columnIndex));
	}

	// JDK8
	@Override
	public <T> T getObject(int columnIndex, Class<T> type)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
