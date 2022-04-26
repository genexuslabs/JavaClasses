package com.genexus.db.dynamodb;

import com.genexus.db.Cursor;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.service.*;
import com.genexus.util.NameValuePair;
import com.genexus.xml.ws.Service;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
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
	private int _executeQuery(DynamoDBResultSet resultSet) throws SQLException
	{
		query.initializeParms(parms);
		DynamoDbClient client = getClient();

		HashMap<String, AttributeValue> values = new HashMap<>();

		if(query.getQueryType() == QueryType.QUERY)
		{
			for (Iterator<VarValue> it = query.getVars().values().iterator(); it.hasNext(); )
			{
				VarValue var = it.next();
				values.put(var.name, DynamoDBHelper.toAttributeValue(var));
			}
		}

		for (Iterator<NameValuePair> it = query.getAssignAtts(); it.hasNext(); )
		{
			NameValuePair asg = it.next();
			String name = trimSharp(asg.name);
			String parmName = asg.value;
			if(!DynamoDBHelper.addAttributeValue(name, values, query.getParm(parmName)))
				throw new SQLException(String.format("Cannot assign attribute value (name: %s)", parmName));
		}
		HashMap<String, AttributeValue> keyCondition = new HashMap<>();
		HashMap<String, String> expressionAttributeNames = null;

		String keyItemForUpd = query.getPartitionKey();
		if(keyItemForUpd != null && keyItemForUpd.startsWith("#"))
		{
			expressionAttributeNames = new HashMap<>();
			expressionAttributeNames.put(keyItemForUpd, keyItemForUpd.substring(1));
		}

		for (Iterator<String> it = Arrays.stream(query.selectList)
			.filter(selItem -> ((DynamoDBMap) selItem).needsAttributeMap())
			.map(IODataMap::getName).iterator(); it.hasNext(); )
		{
			if(expressionAttributeNames == null)
				expressionAttributeNames = new HashMap<>();
			String mappedName = it.next();
			String key = "#" + mappedName;
			expressionAttributeNames.put(key, mappedName);
		}

		if(query.getQueryType() != QueryType.QUERY)
		{
			for (String keyFilter : query.filters)
			{
				Matcher match = FILTER_PATTERN.matcher(keyFilter);
				if (match.matches() && match.groupCount() > 1)
				{
					String varName = String.format(":%s", match.group(2));
					String name = trimSharp(match.group(1));
					if (!DynamoDBHelper.addAttributeValue(name, values, query.getParm(varName)))
						throw new SQLException(String.format("Cannot assign attribute value (name: %s)", varName));
					keyCondition.put(name, values.get(name));
				}
			}
		}

		switch (query.getQueryType())
		{
			case QUERY:
			{
				Iterator<HashMap<String, Object>> iterator;
				if(query instanceof DynamoScan)
				{
					ScanRequest.Builder builder = ScanRequest.builder()
						.tableName(query.tableName)
						.projectionExpression(String.join(",", query.projection));
					if(query.filters.length > 0)
					{
						builder.filterExpression(String.join(" AND ", query.filters))
							.expressionAttributeValues(values);
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
					.attributeUpdates(toAttributeUpdates(keyCondition, values))
					.build();
				client.updateItem(request);
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

	private HashMap<String, AttributeValueUpdate> toAttributeUpdates(HashMap<String, AttributeValue> keyConditions, HashMap<String, AttributeValue> values)
	{
		HashMap<String, AttributeValueUpdate> updates = new HashMap<>();
		for(Map.Entry<String, AttributeValue> item : values.entrySet())
		{
			if (!keyConditions.containsKey(item.getKey()) && !item.getKey().startsWith("AV"))
			{
				updates.put(item.getKey(), AttributeValueUpdate.builder()
					.value(item.getValue())
					.action(AttributeAction.PUT)
					.build());
			}
		}
		return updates;
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
	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
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
