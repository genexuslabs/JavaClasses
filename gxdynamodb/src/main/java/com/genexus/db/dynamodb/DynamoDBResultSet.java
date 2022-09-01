package com.genexus.db.dynamodb;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.db.service.IOServiceContext;
import com.genexus.db.service.ServiceResultSet;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;

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
		try
		{
			if(iterator.hasNext())
			{
				currentEntry = iterator.next();
				return true;
			}
			return false;
		}catch(DynamoDbException e)
		{
			AwsErrorDetails details = e.awsErrorDetails();
			if(details != null && details.errorCode().equals(DynamoDBErrors.ValidationException) &&
			  details.errorMessage().contains(DynamoDBErrors.ValidationExceptionMessageKey))
				return false; // Handles special case where a string key attribute is filtered with an empty value which is not supported on DynamoDB but should yield a not record found in GX
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
		AttributeValue value = getAttValue(columnIndex);
		if(value != null)
		{
			SdkBytes bytes = value.b();
			if(bytes != null)
				return bytes.asInputStream();
		}
		lastWasNull = true;
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
}
