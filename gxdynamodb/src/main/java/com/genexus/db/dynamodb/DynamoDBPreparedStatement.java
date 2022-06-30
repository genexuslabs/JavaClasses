package com.genexus.db.dynamodb;

import com.genexus.db.Cursor;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.service.IODataMap;
import com.genexus.db.service.QueryType;
import com.genexus.db.service.ServicePreparedStatement;
import com.genexus.db.service.VarValue;
import com.genexus.util.NameValuePair;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DynamoDBPreparedStatement extends ServicePreparedStatement
{
	final DynamoQuery query;
	final ServiceCursorBase cursor;

	DynamoDBPreparedStatement(Connection con, DynamoQuery query, ServiceCursorBase cursor, Object[] parms, GXConnection gxCon)
	{
		super(con, parms, gxCon);
		this.query = query;
		this.cursor = cursor;
	}

	@Override
	public ResultSet executeQuery() throws SQLException
	{
		DynamoDBResultSet resultSet = new DynamoDBResultSet(this);
		_executeQuery(resultSet);
		return resultSet;
	}

	@Override
	public int executeUpdate() throws SQLException
	{
		return _executeQuery(null);
	}

	private static final Pattern FILTER_PATTERN = Pattern.compile("\\((.*) = :(.*)\\)");
	private static final Pattern VAR_PATTERN = Pattern.compile(".*(:.*)\\).*");
	private int _executeQuery(DynamoDBResultSet resultSet) throws SQLException
	{
		query.initializeParms(parms);
		boolean isInsert = query.getQueryType() == QueryType.INS;
		DynamoDbClient client = getClient();

		HashMap<String, AttributeValue> values = new HashMap<>();

		if(query.getQueryType() == QueryType.QUERY)
		{
			for (VarValue var : query.getVars().values())
			{
				values.put(var.name, DynamoDBHelper.toAttributeValue(var));
			}
		}

		HashMap<String, AttributeValue> keyCondition = new HashMap<>();
		HashMap<String, String> expressionAttributeNames = null;
		HashSet<String> mappedNames = null;

		for (Iterator<NameValuePair> it = query.getAssignAtts(); it.hasNext(); )
		{
			NameValuePair asg = it.next();
			String name = asg.name;
			if(asg.name.startsWith("#"))
			{
				name = trimSharp(asg.name);
				if (!isInsert)
				{
					if(expressionAttributeNames == null)
					{
						expressionAttributeNames = new HashMap<>();
						mappedNames = new HashSet<>();
					}
					expressionAttributeNames.put(asg.name, name);
					mappedNames.add(name);
				}
			}
			String parmName = asg.value;
			if(!DynamoDBHelper.addAttributeValue(isInsert ? name : ":" + name, values, query.getParm(parmName)))
				throw new SQLException(String.format("Cannot assign attribute value (name: %s)", parmName));
		}

		String keyItemForUpd = query.getPartitionKey();
		if(keyItemForUpd != null && keyItemForUpd.startsWith("#"))
		{
			if(expressionAttributeNames == null)
			{
				expressionAttributeNames = new HashMap<>();
				mappedNames = new HashSet<>();
			}
			String keyName = keyItemForUpd.substring(1);
			expressionAttributeNames.put(keyItemForUpd, keyName);
			mappedNames.add(keyName);
		}

		for (Iterator<String> it = Arrays.stream(query.selectList)
			.filter(selItem -> ((DynamoDBMap) selItem).needsAttributeMap())
			.map(IODataMap::getName).iterator(); it.hasNext(); )
		{
			if(expressionAttributeNames == null)
			{
				expressionAttributeNames = new HashMap<>();
				mappedNames = new HashSet<>();
			}
			String mappedName = it.next();
			String key = "#" + mappedName;
			expressionAttributeNames.put(key, mappedName);
			mappedNames.add(mappedName);
		}

		if(query.getQueryType() != QueryType.QUERY)
		{
			for (String keyFilter : query.getAllFilters().collect(Collectors.toList()))
			{
				Matcher match = FILTER_PATTERN.matcher(keyFilter);
				if (match.matches() && match.groupCount() > 1)
				{
					String varName = String.format(":%s", match.group(2));
					String name = trimSharp(match.group(1));
					AttributeValue value = DynamoDBHelper.toAttributeValue(query.getParm(varName));
					if(value == null)
						throw new SQLException(String.format("Cannot assign attribute value (name: %s)", varName));
					keyCondition.put(name, value);
				}
			}
		}

		switch (query.getQueryType())
		{
			case QUERY:
			{
				boolean issueScan = query instanceof DynamoScan;
				if (!issueScan)
				{ // Check whether a query has to be demoted to scan due to empty parameters
					for (String keyFilter : query.keyFilters)
					{
						Matcher match = VAR_PATTERN.matcher(keyFilter);
						if (match.matches())
						{
							String varName = match.group(1);
							VarValue varValue = query.getParm(varName);
							if (varValue != null && varValue.value.toString().isEmpty())
							{
								issueScan = true;
								break;
							}
						}
					}
				}

				Iterator<HashMap<String, Object>> iterator;
				if(issueScan)
				{
					ScanRequest.Builder builder = ScanRequest.builder()
						.tableName(query.tableName)
						.projectionExpression(String.join(",", query.projection));
					String filterString = query.getAllFilters().collect(Collectors.joining(" AND "));
					if(!filterString.isEmpty())
						builder.filterExpression(filterString).expressionAttributeValues(values);
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
						.keyConditionExpression(String.join(" AND ", query.keyFilters))
						.expressionAttributeValues(values)
						.projectionExpression(String.join(", ", query.projection))
						.indexName(query.getIndex())
						.scanIndexForward(query.isScanIndexForward());
					if(query.filters.length > 0)
						builder.filterExpression(String.join(" AND ", query.filters));
					if(expressionAttributeNames != null)
						builder.expressionAttributeNames(expressionAttributeNames);

					iterator = client.queryPaginator(builder.build())
						.stream()
						.flatMap(response -> response.items()
							.stream()
							.map(map -> new HashMap<String, Object>(map)))
						.iterator();
				}
				resultSet.iterator = iterator;
				return 0;
			}
			case INS:
			{
				PutItemRequest.Builder builder =  PutItemRequest.builder()
					.tableName(query.tableName)
					.item(values)
					.conditionExpression(String.format("attribute_not_exists(%s)", keyItemForUpd));
				if(expressionAttributeNames != null)
					builder.expressionAttributeNames(expressionAttributeNames);
				PutItemRequest request = builder.build();
				try
				{
					client.putItem(request);
				}catch(ConditionalCheckFailedException recordAlreadyExists)
				{
					return Cursor.DUPLICATE;
				}
				break;
			}
			case UPD:
			{
				UpdateItemRequest request = UpdateItemRequest.builder()
					.tableName(query.tableName)
					.key(keyCondition)
					.updateExpression(toAttributeUpdates(keyCondition, values, mappedNames))
					.conditionExpression(String.format("attribute_exists(%s)", keyItemForUpd))
					.expressionAttributeNames(expressionAttributeNames)
					.expressionAttributeValues(values)
					.build();
				try
				{
					client.updateItem(request);
				}catch(ConditionalCheckFailedException recordNotFound)
				{
					return Cursor.EOF;
				}
				break;
			}
			case DLT:
			{
				DeleteItemRequest.Builder builder = DeleteItemRequest.builder()
					.tableName(query.tableName)
					.key(keyCondition)
					.conditionExpression(String.format("attribute_exists(%s)", keyItemForUpd));
				if(expressionAttributeNames != null)
					builder.expressionAttributeNames(expressionAttributeNames);

				DeleteItemRequest request = builder.build();
				try
				{
					client.deleteItem(request);
				}catch(ConditionalCheckFailedException recordNotFound)
				{
					return Cursor.EOF;
				}
				break;
			}
			default: throw new UnsupportedOperationException(String.format("Invalid query type: %s", query.getQueryType()));
		}
		return 0;
	}

	private String toAttributeUpdates(HashMap<String, AttributeValue> keyConditions, HashMap<String, AttributeValue> values, HashSet<String> mappedNames)
	{
		StringBuilder updateExpression = new StringBuilder();
		for(Map.Entry<String, AttributeValue> item : values.entrySet())
		{
			String keyName = item.getKey().substring(1);
			if (!keyConditions.containsKey(keyName) && !keyName.startsWith("AV"))
			{
				if (mappedNames != null && mappedNames.contains(keyName))
					keyName = "#" + keyName;
				updateExpression.append(updateExpression.length() == 0 ? "SET " : ", ");
				updateExpression.append(keyName).append(" = ").append(item.getKey());
			}
		}
		return updateExpression.toString();
	}

	private static String trimSharp(String name)
	{
		return name.startsWith("#") ? name.substring(1) : name;
	}

	DynamoDbClient getClient() throws SQLException
	{
		return ((DynamoDBConnection)getConnection()).mDynamoDB;
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
	{
		parms[parameterIndex-1] = SdkBytes.fromInputStream(x);
	}

	/// JDK8
	@Override
	public void closeOnCompletion()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isCloseOnCompletion()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
