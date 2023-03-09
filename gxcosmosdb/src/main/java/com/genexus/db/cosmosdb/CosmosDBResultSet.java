package com.genexus.db.cosmosdb;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.db.service.IOServiceContext;
import com.genexus.db.service.ServiceResultSet;

import com.azure.cosmos.CosmosException;
import com.genexus.db.service.IOServiceContext;
import com.genexus.db.service.ServiceResultSet;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;
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
			return value.toString().trim();
		}
		lastWasNull = true;
		return "";
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

	private Instant getInstant(int columnIndex)
	{
		String value = getString(columnIndex);
		if(value == null || value.trim().isEmpty())
		{
			lastWasNull = true;
			return CommonUtil.nullDate().toInstant();
		}

		TemporalAccessor accessor = null;

		try
		{
			accessor = ISO_DATE_TIME_OR_DATE.parseBest(value, LocalDateTime::from, LocalDate::from);
		}catch(DateTimeParseException dtpe)
		{
			for(DateTimeFormatter dateTimeFormatter:DATE_TIME_FORMATTERS)
			{
				try
				{
					accessor = dateTimeFormatter.parseBest(value, LocalDateTime::from, LocalDate::from);
					break;
				}catch(Exception ignored){ }
			}
			if(accessor == null)
			{
				return CommonUtil.resetTime(CommonUtil.nullDate()).toInstant();
			}
		}
		if(accessor instanceof  LocalDateTime)
		{
			ModelContext ctx = ModelContext.getModelContext();
			TimeZone tz = ctx != null ? ctx.getClientTimeZone() : TimeZone.getDefault();
			return ((LocalDateTime) accessor).atZone(tz.toZoneId()).toInstant();
		}
		else return LocalDate.from(accessor).atStartOfDay().toInstant(ZoneOffset.UTC);
	}
	@Override
	public java.sql.Date getDate(int columnIndex)
	{
		return java.sql.Date.valueOf(getTimestamp(columnIndex).toInstant().atOffset(ZoneOffset.UTC).toLocalDate());
	}

	@Override
	public Time getTime(int columnIndex)
	{
		return getAs(Time.class, columnIndex, new Time(0));
	}

	@Override
	public Timestamp getTimestamp(int columnIndex)
	{
		return java.sql.Timestamp.from(getInstant(columnIndex));
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
}
