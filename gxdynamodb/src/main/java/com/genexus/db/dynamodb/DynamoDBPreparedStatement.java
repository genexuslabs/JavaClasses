package com.genexus.db.dynamodb;

import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.service.IODataMap;
import com.genexus.db.service.QueryType;
import com.genexus.db.service.ServicePreparedStatement;
import com.genexus.db.service.VarValue;
import com.genexus.util.NameValuePair;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

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
		query.initializeParms(parms);
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

	private static final Pattern FILTER_PATTERN = Pattern.compile("\\((.*) = (:.*)\\)");
	private int _executeQuery(DynamoDBResultSet resultSet) throws SQLException
	{
		DynamoDbClient client = getClient();

		HashMap<String, AttributeValue> values = new HashMap<>();

		for(Iterator<VarValue> it = query.getVars(); it.hasNext();)
		{
			VarValue var = it.next();
			values.put(var.name, DynamoDBHelper.toAttributeValue(var));
		}

		for (Iterator<NameValuePair> it = query.getAssignAtts(); it.hasNext(); )
		{
			NameValuePair asg = it.next();
			String name = trimSharp(asg.name);
			String parmName = asg.value;
			if(!DynamoDBHelper.addAttributeValue(name, values, query.getParm(parmName)))
				throw new SQLException(String.format("Cannot assign attribute value (name: %s)", parmName));
		}
		String pattern = "\\((.*) = :(.*)\\)";
		HashMap<String, AttributeValue> keyCondition = new HashMap<>();
		ArrayList<String> filters = new ArrayList<>();

		HashMap<String, String> expressionAttributeNames = null;

		for (Iterator<String> it = Arrays.stream(query.selectList)
			.filter(selItem -> ((DynamoDBMap) selItem).needsAttributeMap())
			.map(IODataMap::getName).iterator(); it.hasNext(); )
		{
			if(expressionAttributeNames == null)
				expressionAttributeNames = new HashMap<>();
			String mappedName = it.next();
			String key = "#" + mappedName;
			String value = mappedName;
			expressionAttributeNames.put(key, value);
		}

		if(query.getQueryType() != QueryType.QUERY)
		{
			for (String keyFilter : query.filters)
			{
				Matcher match = FILTER_PATTERN.matcher(keyFilter);
				if (match.matches() && match.groupCount() > 1)
				{
					String varName = match.group(2);
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
				resultSet.iterator = iterator;
				return 0;
			}
			case INS:
			{
				PutItemRequest request = PutItemRequest.builder()
					.tableName(query.tableName)
					.item(values)
					.build();
				 client.putItem(request);
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
				DeleteItemRequest request = DeleteItemRequest.builder()
					.tableName(query.tableName)
					.key(keyCondition)
					.build();
				client.deleteItem(request);
				break;
			}
			default: throw new UnsupportedOperationException(String.format("Work in progress: %s", query.getQueryType()));
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
