package com.genexus.db.cosmosdb;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.db.Cursor;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.service.QueryType;
import com.genexus.db.service.ServicePreparedStatement;
import com.genexus.db.service.VarValue;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.NameValuePair;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.commons.collections.ListUtils;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CosmosDBPreparedStatement extends ServicePreparedStatement
{
	public static final ILogger logger = LogManager.getLogger(CosmosDBPreparedStatement.class);
	final CosmosDBQuery query;
	final ServiceCursorBase cursor;
	private static final int PAGE_SIZE = 100; // Find a way to customize this property
	CosmosAsyncContainer container = null;
	HashMap<String,Object> keyCondition= new HashMap<String, Object>();

	CosmosDBPreparedStatement(Connection con, CosmosDBQuery query, ServiceCursorBase cursor, Object[] parms, GXConnection gxCon) throws SQLException {
		super(con, parms, gxCon);
		this.query = query;
		this.cursor = cursor;
		getCointainer(query.tableName);
	}
	@Override
	public ResultSet executeQuery() throws SQLException
	{
		CosmosDBResultSet resultSet = new CosmosDBResultSet(this);

		try {
			_executeQuery(resultSet);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return resultSet;
	}
	private int _executeQuery(CosmosDBResultSet resultSet) throws Exception {
		query.initializeParms(parms);
		boolean isUpdate = query.getQueryType() == QueryType.UPD;
		JSONObject jsonObject = setUpJsonPayload(isUpdate);

		switch (query.getQueryType())
		{
			case QUERY:
			{
				if (querybyPK(query) && keyCondition != null)
				{
					if (keyCondition.isEmpty() || !keyCondition.containsKey("id") || !keyCondition.containsKey(query.getPartitionKey())) {
						AtomicInteger statusCode = executeReadByPK(keyCondition.get("id").toString(),keyCondition.get(query.getPartitionKey()));
						if (statusCode != null && statusCode.get() == 404)
							return Cursor.EOF;
					}
				}
				else
				{
					String sqlQuery = CosmosDBHelper.createCosmosQuery(query, cursor, parms);
					resultSet.iterator = queryWithPaging(sqlQuery, new CosmosQueryRequestOptions());
					return 0;
				}
			}
			case INS:
			{
				if (keyCondition != null) {
					if (keyCondition.isEmpty() || !keyCondition.containsKey(query.getPartitionKey())) {
						throw new Exception("Insert item failed: error parsing the query.");
					} else {
						int[] statusCode = createDocument(jsonObject, keyCondition.get(query.getPartitionKey()));
						if (statusCode != null && statusCode[0] == 409)
							return Cursor.DUPLICATE;
					}
				}
				else
					throw new Exception("Insert item failed: error parsing the key values of the query.");
				break;
			}
			case UPD:
			{
				if (keyCondition != null) {
					if (keyCondition.isEmpty() || !keyCondition.containsKey("id") || !keyCondition.containsKey(query.getPartitionKey())) {
						throw new Exception("Update item failed: error parsing the query.");
					} else {
						int[] statusCode = replaceDocument(jsonObject, keyCondition.get("id").toString(), keyCondition.get(query.getPartitionKey()));
						if (statusCode != null && statusCode[0] == 404)
							return Cursor.EOF;
					}
				}
				else
					throw new Exception("Update item failed: error parsing the key values of the query.");
				break;

			}
			case DLT:
			{
				if (keyCondition != null) {
					if (keyCondition.isEmpty() || !keyCondition.containsKey("id") || !keyCondition.containsKey(query.getPartitionKey())) {
						throw new Exception("Delete item failed: error parsing the query.");
					} else {
						int[] statusCode = deleteDocument(keyCondition.get("id").toString(), keyCondition.get(query.getPartitionKey()));
						if (statusCode != null && statusCode[0] == 404)
							return Cursor.EOF;
					}
					break;
				}
				throw new Exception("Delete item failed: error parsing the key values of the query.");
			}
			default: throw new UnsupportedOperationException(String.format("Invalid query type: %s", query.getQueryType()));
		}
		return 0;
	}
	CosmosAsyncDatabase getDatabase() throws SQLException {
		return ((CosmosDBConnection)getConnection()).cosmosDatabase;
	}
	private void getCointainer(String containerName) throws SQLException {
		container = getDatabase().getContainer(containerName);
	}

	private boolean querybyPK(CosmosDBQuery query){
		if (query.filters.length > 0)
		{
			String equalFilterPattern = "\\((.*) = :(.*)\\) and \\((.*) = :(.*)\\)";
			//ToDo
		}
		return false;
	}

	private AtomicInteger executeReadByPK(String idValue, Object partitionKey) throws Exception {
		//  Read document by ID
		AtomicInteger statusCode = null;
		if (container != null) {
			CountDownLatch latch = new CountDownLatch(1);

			Mono<CosmosItemResponse<JsonNode>> itemResponseMono = container.readItem(idValue, toPartitionKey(partitionKey), JsonNode.class);

			itemResponseMono.doOnSuccess((response) -> {
					latch.countDown(); // signal completion
				})
				.doOnError(Exception.class, exception -> {
					latch.countDown(); // signal completion
					logger.error(String.format("Fail: %1",exception.getMessage()));
					if (exception instanceof CosmosException && ((CosmosException) exception).getStatusCode() == 404)
						statusCode.set(404);
				}).subscribe();

			latch.await(); // wait for completion
		}
		else {
			throw new Exception("CosmosDB Insert By PK Execution failed. Container not found.");
		}
		return statusCode;
	}
	private Iterator<HashMap<String, Object>> queryWithPaging(String sqlQuery, CosmosQueryRequestOptions options) throws Exception {

		int currentPageNumber = 1;
		String continuationToken = null;
		Iterator<HashMap<String, Object>> iterator = null;
		do {
			CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

			Iterable<FeedResponse<JsonNode>> feedResponseIterator =
				container.queryItems(sqlQuery, queryOptions, JsonNode.class).byPage(continuationToken, PAGE_SIZE).toIterable();

			List<HashMap<String, Object>> hashMapList = new ArrayList<>();
			for (FeedResponse<JsonNode> page : feedResponseIterator) {

				List<JsonNode> pageResults = page.getResults();

				for ( JsonNode json: pageResults
					 ) {
					HashMap<String,Object> pageResult = CosmosDBHelper.jsonNodeToHashMap(json);
					hashMapList.add(pageResult);
				}
				if (!hashMapList.isEmpty())
					iterator = hashMapList.iterator();

				continuationToken = page.getContinuationToken();
				currentPageNumber++;
			}
		} while (continuationToken != null);
		return iterator;

	}
	private int[] deleteDocument(String idValue, Object partitionKey) throws Exception {
		int[] statusCode = new int[1];
		if (container != null) {
			CountDownLatch latch = new CountDownLatch(1);

			Mono<CosmosItemResponse<Object>> cosmosItemResponseMono = container.deleteItem(idValue, new PartitionKey(partitionKey), new CosmosItemRequestOptions())
				.doOnSuccess(itemResponse -> {

					logger.debug(String.format("Deleted document- id: %1 partitionkey: %2",idValue,partitionKey.toString()));
					logger.debug(String.format("Status Code: %1",itemResponse.getStatusCode()));
					statusCode[0] = itemResponse.getStatusCode();
					//latch.countDown();
				})
				.doOnError(error -> {
					//logger.error(String.format("Fail: %1",error.getMessage()));
					if (error.getMessage().toString().contains("404"))
						statusCode[0] = 404;
					latch.countDown();
				});
			cosmosItemResponseMono.subscribe();
			latch.await();
			return statusCode;
		}
		else
		{
			throw new Exception("CosmosDB Delete Execution failed. Container not found.");
		}
	}
	private int[] createDocument(JSONObject jsonObject, Object partitionKey) throws Exception {

		int[] statusCode = new int[1];
		if (container != null) {
			ObjectMapper mapper = new ObjectMapper();
			String jsonStr = jsonObject.toString();
			//Parse string to extract nulls
			jsonStr = jsonStr.replaceAll("\\\"(null)\\\"", "$1");

			JsonNode jsonNode = mapper.readTree(jsonStr);
			CountDownLatch latch = new CountDownLatch(1);
	
			if (jsonNode != null) {
				Mono<CosmosItemResponse<JsonNode>> cosmosItemResponseMono = container.createItem(jsonNode, toPartitionKey(partitionKey), new CosmosItemRequestOptions())
					.doOnSuccess((response) -> {
						logger.debug(String.format("Inserted document: %1",response.getItem().toString()));
						logger.debug(String.format("Status Code: %1",response.getStatusCode()));
						statusCode[0] = response.getStatusCode();
						//latch.countDown();
					})
					.doOnError(error -> {
						//logger.error(String.format("Fail: %1",error.getMessage()));
						if (error.getMessage().toString().contains("409"))
							statusCode[0] = 409;
						latch.countDown();
					});

				cosmosItemResponseMono.subscribe();
				latch.await();

				return statusCode;
			} else {
				throw new Exception("CosmosDB Insert Execution failed. Invalid json payload.");
			}
		} else {
			throw new Exception("CosmosDB Insert Execution failed. Container not found.");
		}
	}
	private int[] replaceDocument(JSONObject jsonObject, String idValue , Object partitionKey) throws Exception {
		int[] statusCode = new int[1];
		if (container != null)
		{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(jsonObject.toString());
		CountDownLatch latch = new CountDownLatch(1);

		Mono<CosmosItemResponse<JsonNode>> cosmosItemResponseMono = container.replaceItem(jsonNode, idValue, toPartitionKey(partitionKey), new CosmosItemRequestOptions())
			.doOnSuccess(itemResponse -> {

				logger.debug(String.format("Replaced document- id: %1 partitionkey: %2",idValue,partitionKey.toString()));
				logger.debug(String.format("Status Code: %1",itemResponse.getStatusCode()));
				statusCode[0] = itemResponse.getStatusCode();
				//latch.countDown();
			})
			.doOnError(error -> {
				//logger.error(String.format("Fail: %1",error.getMessage()));
				if (error.getMessage().toString().contains("404"))
					statusCode[0] = 404;
				latch.countDown();
			});

			cosmosItemResponseMono.subscribe();
			latch.await();
			return statusCode;
		}
		else {
			throw new Exception("CosmosDB Replace Execution failed. Container not found.");
		}
	}
	private JSONObject setUpJsonPayload(boolean isUpdate) throws JSONException, SQLException {
		// Setup the json payload to execute the insert or update query.

		HashMap<String, Object> values = null;
		JSONObject jsonObject = null;

		for (Iterator<NameValuePair> it = query.getAssignAtts(); it.hasNext(); ) {
			NameValuePair asg = it.next();
			String name = asg.name;
			String parmName = asg.value.substring(0,asg.value.length()-1);
			jsonObject = CosmosDBHelper.addItemValue(name, query.getParm(parmName), jsonObject);

			if (name.equals(query.getPartitionKey())) {
				keyCondition.put(query.getPartitionKey(), query.getParm(parmName).value);
			}
		}
		// Get the values for id and partitionKey
		String regex1 = "\\(([^\\)\\(]+)\\)";
		String regex2 = "(.*)[^<>!=]\\s*(=|!=|<|>|<=|>=|<>)\\s*(:.*:)";

		String keyFilterS;
		String condition = "";
		Iterable<String> keyFilterQ = Collections.emptyList();
		Iterable<String> allFilters = ListUtils.union(query.getKeyFilters(), Arrays.asList(query.filters));

		for (String keyFilter : allFilters) {
			keyFilterS = keyFilter;
			condition = keyFilter;
			Matcher matcher = Pattern.compile(regex1).matcher(keyFilterS);
			while (matcher.find()) {
				String cond = matcher.group(1);
				Matcher matcher2 = Pattern.compile(regex2).matcher(cond);
				if (matcher2.matches()) {
					String varName = matcher2.group(3);
					varName = varName.substring(1, varName.length() - 1);
					String name = matcher2.group(1);

					String varNameM = ":" + varName;
					VarValue varValue = null;
					for (Map.Entry<String, VarValue> entry : query.getVars().entrySet()) {
						if (entry.getKey().toString().equals(varNameM)) {
							varValue = entry.getValue();
							break;
						}
					}
					if (varValue != null) {
						keyCondition.put(name, varValue.value);
						if (isUpdate && name.equals("id")) {
							jsonObject.put(name, varValue.value);
						}
						if (isUpdate && name.equals(query.getPartitionKey()) && !query.getPartitionKey().equals("id")) {
							jsonObject.put(name, varValue.value);
						}
					}
				}
			}
		}
		return jsonObject;
	}

	private PartitionKey toPartitionKey(Object value) throws Exception {
		if (Double.class.isInstance(value)) //Double.valueOf(value) instanceof Double)
			return new PartitionKey((double)value);
		if (value instanceof Boolean)
			return new PartitionKey((boolean)value);
		if (String.class.isInstance(value))
			return new PartitionKey((String)value);
			else throw new Exception("Partitionkey can be double, bool or string.");
	}

	@Override
	public int executeUpdate() throws SQLException
	{
		try {
			return _executeQuery(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setBinaryStream(int parameterIndex, InputStream x, int length)
	{
	//	parms[parameterIndex-1] = SdkBytes.fromInputStream(x);
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
