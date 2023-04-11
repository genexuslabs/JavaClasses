package com.genexus.db.cosmosdb;

import com.genexus.CommonUtil;
import com.genexus.db.service.IOServiceContext;
import com.genexus.db.service.ServiceResultSet;

import com.azure.cosmos.CosmosException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import static java.lang.Boolean.valueOf;

public class CosmosDBResultSet extends ServiceResultSet<Object>
{
	private final CosmosDBPreparedStatement stmt;
	public CosmosDBResultSet(CosmosDBPreparedStatement stmt) throws SQLException
	{
		this.stmt = stmt;
	}

	@Override
	public boolean next()
	{
		try
		{
			if (iterator != null) {
				if (iterator.hasNext()) {
					currentEntry = iterator.next();
					return true;
				}
			return false;
			}
			return false;
		}catch(CosmosException e)
		{
			throw e;
		}
	}

	private static final IOServiceContext SERVICE_CONTEXT = null;
	private static final Timestamp TIMESTAMP_NULL_VALUE = java.sql.Timestamp.from(CommonUtil.nullDate().toInstant());
	private static final java.util.Date DATE_NULL_VALUE = Date.from(CommonUtil.nullDate().toInstant());
	private boolean lastWasNull;
	@Override
	public boolean wasNull()
	{
		return value == null || lastWasNull;
	}

	private Object getAttValue(int columnIndex)
	{
		value = stmt.query.selectList[columnIndex-1].getValue(SERVICE_CONTEXT, currentEntry);
		return value;
	}

	private long getNumeric(int columnIndex)
	{
		Object value = getAttValue(columnIndex);
		if(value != null)
		{
			lastWasNull = false;
			String strvalue = value.toString().trim();
			if (strvalue != null) {
				try {
					return Long.parseLong(strvalue);
				} catch (NumberFormatException ex)
				{
					if (strvalue.equalsIgnoreCase("true")||strvalue.equalsIgnoreCase("false"))
						return valueOf(strvalue) ? 1:0;
				}
			}
		}
		lastWasNull = true;
		return 0;
	}

	private double getDecimal(int columnIndex)
	{
		Object value = getAttValue(columnIndex);
		if(value != null)
		{
			lastWasNull = false;
			String strValue = value.toString().trim();
			if (strValue != null)
				return Double.parseDouble(strValue);
		}
		lastWasNull = true;
		return 0;
	}
	@Override
	public <T> T getAs(Class<T> reference, int columnIndex, T defaultValue)
	{
		throw new UnsupportedOperationException(String.format("Data Type: %s", reference.getName()));
	}

	@Override
	public String getString(int columnIndex)
	{
		Object value = getAttValue(columnIndex);
		if(value != null)
		{
			lastWasNull = false;
			return value.toString().trim();
		}
		lastWasNull = true;
		return null;
	}

	@Override
	public boolean getBoolean(int columnIndex)
	{
		Object value = getAttValue(columnIndex);
		if(value != null)
		{
			String varValuestr = value.toString().toLowerCase();
			lastWasNull = false;
			return varValuestr.equals("true") ? true : false;

		}
		lastWasNull = true;
		return false;
	}

	@Override
	public byte getByte(int columnIndex) { return (byte)getNumeric(columnIndex);}

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
		Object value = getAttValue(columnIndex);
		if(value != null)
		{
			String strValue = value.toString().trim();
			if (strValue != null)
			{
				lastWasNull = false;
				return new BigDecimal(strValue);
			}
		}
		lastWasNull = true;
		return BigDecimal.ZERO;
	}

	@Override
	public java.sql.Date getDate(int columnIndex) throws SQLException {
		Timestamp ts = getTimestamp(columnIndex);
		if (!ts.toString().equals(TIMESTAMP_NULL_VALUE.toString()))
		{
			return java.sql.Date.valueOf(ts.toInstant().atOffset(ZoneOffset.UTC).toLocalDate());
		}
		String strDate = getString(columnIndex);
		for(String dateFormatter:DATE_FORMATTERS) {
			try {
				DateFormat dateFormat = new SimpleDateFormat(dateFormatter);
				java.util.Date date = dateFormat.parse(strDate);
				return new java.sql.Date(date.getTime());
			} catch (Exception ignored) {
			}
		}
		return new java.sql.Date(DATE_NULL_VALUE.getTime());
	}

	@Override
	public Time getTime(int columnIndex)
	{
		return getAs(Time.class, columnIndex, new Time(0));
	}

	@Override
	public Timestamp getTimestamp(int columnIndex)
	{
		String datetimeString = getString(columnIndex);
		if(datetimeString == null || datetimeString.trim().isEmpty())
		{
			lastWasNull = true;
			return TIMESTAMP_NULL_VALUE;
		}
		LocalDateTime localDateTime = null;
		try {
			localDateTime = LocalDateTime.parse(datetimeString, ISO_DATE_TIME_OR_DATE);
			ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
			return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
		}
		catch (DateTimeParseException dateTimeParseException)
		{
			for(DateTimeFormatter dateTimeFormatter:DATE_TIME_FORMATTERS) {
				try {
					localDateTime = LocalDateTime.parse(datetimeString, dateTimeFormatter);
					ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
					return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
				}
				catch (Exception ignored)
				{}
			}
			if (localDateTime == null)
			{return TIMESTAMP_NULL_VALUE;}
		}
		return TIMESTAMP_NULL_VALUE;
	}

	@Override
	public InputStream getBinaryStream(int columnIndex)
	{
		//ToDo
		return null;
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
	private static final DateTimeFormatter ISO_DATE_TIME_OR_DATE = new DateTimeFormatterBuilder()
		.parseCaseInsensitive()
		.append(DateTimeFormatter.ISO_LOCAL_DATE)
		.optionalStart()
		.optionalStart()
		.appendLiteral('T')
		.optionalEnd()
		.optionalStart()
		.appendLiteral(' ')
		.optionalEnd()
		.append(DateTimeFormatter.ISO_LOCAL_TIME)
		.optionalStart()
		.appendOffsetId()
		.optionalStart()
		.appendLiteral('[')
		.parseCaseSensitive()
		.appendZoneRegionId()
		.appendLiteral(']').toFormatter();

	private static final DateTimeFormatter [] DATE_TIME_FORMATTERS = new DateTimeFormatter[]
		{
			DateTimeFormatter.ofPattern("M/d/yyyy[ H:mm:ss]"),
			DateTimeFormatter.ofPattern("M/d/yyyy[ h:mm:ss a]"),
			DateTimeFormatter.ofPattern("yyyy-M-d[ H:mm:ss.S]"),
			DateTimeFormatter.ofPattern("yyyy-M-d[ H:mm:ss.S a]")
		};
	private static final String [] DATE_FORMATTERS = new String[]
		{
			"yyyy-MM-dd",
			"MM/dd/yyyy",
			"EEE, dd MMM yyyy",
			"EEEE, dd MMMM yyyy",
			"Month D, Yr",
			"Yr, Month D",
			"D Month, Yr",
			"M/D/YY",
			"YY/M/D",
			"Mon-DD-YYYY",
			"DD-Mon-YYYY",
			"YYYYY-Mon-DD",
			"Mon DD, YYYY",
			"DD Mon, YYYY",
			"YYYY, Mon DD"
		};
}
