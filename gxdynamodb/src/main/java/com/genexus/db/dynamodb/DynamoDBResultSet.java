package com.genexus.db.dynamodb;

import com.genexus.db.service.IOServiceContext;
import com.genexus.db.service.ServiceResultSet;
import com.genexus.db.service.VarValue;
import com.genexus.util.NameValuePair;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamoDBResultSet extends ServiceResultSet<AttributeValue>
{
	private DynamoDBPreparedStatement stmt;
	public DynamoDBResultSet(DynamoDBPreparedStatement stmt) throws SQLException
	{
		this.stmt = stmt;
//		execute(stmt.query, stmt.parms);
	}
/*
	private static final Pattern FILTER_PATTERN = Pattern.compile("\\((.*) = :(.*)\\)");
	private void execute(DynamoQuery query, Object[] parms) throws SQLException
	{
		DynamoDbClient client = stmt.getClient();

		HashMap<String, AttributeValue> values = new HashMap<String, AttributeValue>();

		for(Iterator<VarValue> it = query.getVars(); it.hasNext();)
		{
			VarValue var = it.next();
			values.put(var.name, DynamoDBHelper.ToAttributeValue(var));
		}

		for (Iterator<NameValuePair> it = query.getAssignAtts(); it.hasNext(); )
		{
			NameValuePair asg = it.next();
			String name = trimSharp(asg.name);
			String parmName = asg.value.substring(1);
//@todo
//			if(!DynamoDBHelper.addAttributeValue(name, values, parms, parmName))
//				throw new SQLException(String.format("Cannot assign attribute value (name: %s)", parmName);
		}
		String pattern = "\\((.*) = :(.*)\\)";
		HashMap<String, AttributeValue> keyCondition = new HashMap<>();
		ArrayList<String> filters = new ArrayList<String>();

		HashMap<String, String> expressionAttributeNames = null;

		for (Iterator<String> it = Arrays.stream(query.selectList)
			.filter(selItem -> ((DynamoDBMap) selItem).needsAttributeMap())
			.map(selItem -> selItem.getName()).iterator(); it.hasNext(); )
		{
			if(expressionAttributeNames == null)
				expressionAttributeNames = new HashMap<>();
			String mappedName = it.next();
			String key = "#" + mappedName;
			String value = mappedName;
			expressionAttributeNames.put(key, value);
		}

		for(String keyFilter : query.filters)
		{
			Matcher match = FILTER_PATTERN.matcher(keyFilter);
			if(match.matches() && match.groupCount() > 1)
			{
				String varName = match.group(2);
				String name = trimSharp(match.group(1));
//@todo
//				if(!DynamoDBHelper.addAttributeValue(name, values, parms[varName]as ServiceParameter);
//					throw new SQLException(String.format("Cannot assign attribute value (name: %s)", parmName);
				keyCondition.put(name, values.get(name));
			}
		}

		switch (query.getQueryType())
		{
			case QUERY:
			{
				if(query instanceof DynamoScan)
				{
					ScanRequest.Builder builder = ScanRequest.builder()
						.tableName(query.tableName)
						.projectionExpression(String.join(",", query.projection));
					if(query.filters.length > 0)
					{
						builder.filterExpression(String.join(" AND ", query.filters)).expressionAttributeValues(values);
					}
					if(expressionAttributeNames != null)
						builder.expressionAttributeNames(expressionAttributeNames);

					iterator = client.scanPaginator(builder.build())
						.stream()
						.flatMap(response -> response.items()
							.stream()
							.map(map -> new HashMap<String, Object>(map)))
						.iterator();
				}else
				{
					QueryRequest.Builder builder = QueryRequest.builder()
						.tableName(query.tableName)
						.keyConditionExpression(String.join(" AND ", query.filters))
						.expressionAttributeValues(values)
						.projectionExpression(String.join(", ", query.projection))
						.indexName(query.getIndex())
						.scanIndexForward(query.isScanIndexForward());
					if(expressionAttributeNames != null)
						builder.expressionAttributeNames(expressionAttributeNames);

					iterator = client.queryPaginator(builder.build())
						.stream()
						.flatMap(response -> response.items()
							.stream()
							.map(map -> new HashMap<String, Object>(map)))
						.iterator();
				}
				break;
			}
			default: throw new UnsupportedOperationException(String.format("Work in progress: %s", query.getQueryType()));
		}
	}

	private static String trimSharp(String name)
	{
		return name.startsWith("#") ? name.substring(1) : name;
	}
	*/

	@Override
	public boolean next() throws SQLException
	{
		if(iterator.hasNext())
		{
			currentEntry = (HashMap<String, Object>) iterator.next();
			return true;
		}
		return false;
	}

	private static final IOServiceContext SERVICE_CONTEXT = null;

	private boolean lastWasNull;
	@Override
	public boolean wasNull() throws SQLException
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
				return value.bool().booleanValue() ? 1 : 0;
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
	public <T> T getAs(Class<T> reference, int columnIndex, T defaultValue) throws SQLException
	{
		throw new UnsupportedOperationException(String.format("Data Type: %s", reference.getName()));
	}

	@Override
	public String getString(int columnIndex) throws SQLException
	{
		String value = DynamoDBHelper.getString(getAttValue(columnIndex));
		lastWasNull = value == null;
		return value;
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException
	{
		AttributeValue value = getAttValue(columnIndex);
		if(value != null)
		{
			Boolean boolValue = value.bool();
			if(boolValue != null)
			{
				lastWasNull = false;
				return boolValue.booleanValue();
			}
		}
		lastWasNull = true;
		return false;
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException
	{
		return (byte)getNumeric(columnIndex);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException
	{
		return (short)getNumeric(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException
	{
		return (int)getNumeric(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException
	{
		return (long)getNumeric(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException
	{
		return (float)getDecimal(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException
	{
		return (double)getDecimal(columnIndex);
	}

	@Override
	@Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
	{
		return getBigDecimal(columnIndex).setScale(scale);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException
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
