package com.genexus.db.cosmosdb;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.db.Cursor;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.driver.GXConnection;
import com.genexus.db.service.Query;
import com.genexus.db.service.QueryType;
import com.genexus.db.service.ServicePreparedStatement;
import com.genexus.db.service.VarValue;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.NameValuePair;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		getContainer(query.tableName);
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
				if (isQueryByPK(query) && keyCondition != null)
				{
					if (keyCondition.containsKey("id") && keyCondition.containsKey(query.getPartitionKey())) {
						int[] statusCode = new int[1];
						resultSet.iterator = executeReadByPK(keyCondition.get("id").toString(),keyCondition.get(query.getPartitionKey()), statusCode);
						if (statusCode != null && statusCode[0] == 404)
							return Cursor.EOF;
						return 0;
					}
				}
				String sqlQuery = CosmosDBHelper.createCosmosQuery(query, cursor, parms);
				resultSet.iterator = queryWithPaging(sqlQuery, new CosmosQueryRequestOptions());
				return 0;
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
	private void getContainer(String containerName) throws SQLException {
		container = getDatabase().getContainer(containerName);
	}

	private boolean isQueryByPK(CosmosDBQuery query){
		//Find out if the query is by PK and by equality
		if (query.filters.length == 1)
		{
			String equalFilterPattern = "^\\(\\(id = :([a-zA-Z0-9]+):\\) and \\(([a-zA-Z0-9]+) = :([a-zA-Z0-9]+):\\)\\)";
			Matcher matcher = Pattern.compile(equalFilterPattern).matcher(query.filters[0]);
			if (matcher.matches()) {
				String attItem = matcher.group(2);
				String pkParmValue;
				if (attItem.equals(query.getPartitionKey()))
				{
					pkParmValue = matcher.group(3);
					getVarValuesFromQuery(pkParmValue,attItem,query);

					String idParmValue = matcher.group(1);
					getVarValuesFromQuery(idParmValue,"id",query);
					return true;
				}
			}
		}
		return false;
	}
	private Iterator<HashMap<String, Object>> executeReadByPK(String idValue, Object partitionKey, int[] statusCode) throws Exception {
		// Read document by ID
		Iterator<HashMap<String, Object>> iterator = null;
		if (container != null) {
			AtomicReference<JsonNode> itemRef = new AtomicReference<>();
			List<HashMap<String, Object>> hashMapList = new ArrayList<>();

			try {
				container.readItem(idValue, toPartitionKey(partitionKey), new CosmosItemRequestOptions(), JsonNode.class)
					.map(CosmosItemResponse::getItem)
					.doOnNext(item -> {
						itemRef.set(item);
					})
					.block(); // Wait for the read to complete

				JsonNode item = itemRef.get();
				HashMap<String, Object> pageResult = CosmosDBHelper.jsonNodeToHashMap(item);
				hashMapList.add(pageResult);
				if (!hashMapList.isEmpty())
					return iterator = hashMapList.iterator();
				else
					return null;
			}
			catch (NotFoundException ex) {
				statusCode[0] = 404;
			}
		}
		else
			throw new Exception("CosmosDB Read By PK Execution failed. Container not found.");
		return null;
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
				})
				.doOnError(error -> {
					//logger.error(String.format("Fail: %1",error.getMessage()));
					if (error.getMessage().toString().contains("404"))
						statusCode[0] = 404;
					latch.countDown();
				});
			Disposable d = cosmosItemResponseMono.subscribe();
			latch.await();
			d.dispose();
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
			jsonStr = jsonStr.replaceAll("\\\"(null)\\\"", "$1");

			JsonNode jsonNode = mapper.readTree(jsonStr);
			CountDownLatch latch = new CountDownLatch(1);
	
			if (jsonNode != null) {
				Mono<CosmosItemResponse<JsonNode>> cosmosItemResponseMono = container.createItem(jsonNode, toPartitionKey(partitionKey), new CosmosItemRequestOptions())
					.doOnSuccess((response) -> {
						logger.debug(String.format("Inserted document: %1",response.getItem().toString()));
						logger.debug(String.format("Status Code: %1",response.getStatusCode()));
						statusCode[0] = response.getStatusCode();
					})
					.doOnError(error -> {
						//logger.error(String.format("Fail: %1",error.getMessage()));
						if (error.getMessage().toString().contains("409"))
							statusCode[0] = 409;
						latch.countDown();
					});

				Disposable d = cosmosItemResponseMono.subscribe();
				latch.await();
				d.dispose();
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
		String jsonStr = jsonObject.toString();
		jsonStr = jsonStr.replaceAll("\\\"(null)\\\"", "$1");

		JsonNode jsonNode = mapper.readTree(jsonStr);
		CountDownLatch latch = new CountDownLatch(1);

		Mono<CosmosItemResponse<JsonNode>> cosmosItemResponseMono = container.replaceItem(jsonNode, idValue, toPartitionKey(partitionKey), new CosmosItemRequestOptions())
			.doOnSuccess(itemResponse -> {

				logger.debug(String.format("Replaced document- id: %1 partitionkey: %2",idValue,partitionKey.toString()));
				logger.debug(String.format("Status Code: %1",itemResponse.getStatusCode()));
				statusCode[0] = itemResponse.getStatusCode();
			})
			.doOnError(error -> {
				//logger.error(String.format("Fail: %1",error.getMessage()));
				if (error.getMessage().toString().contains("404"))
					statusCode[0] = 404;
				latch.countDown();
			});

			Disposable d = cosmosItemResponseMono.subscribe();
			latch.await();
			d.dispose();
			return statusCode;
		}
		else {
			throw new Exception("CosmosDB Replace Execution failed. Container not found.");
		}
	}
	private void getVarValuesFromQuery(String varName, String name, Query query)
	{
		varName = varName.substring(1, varName.length() - 1);
		String varNameM = ":" + varName;
		VarValue varValue = null;
		for (Map.Entry<String, VarValue> entry : query.getVars().entrySet()) {
			if (entry.getKey().toString().equals(varNameM)) {
				varValue = entry.getValue();
				break;
			}
		}
		if (varValue != null)
			keyCondition.put(name, varValue.value);
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

		Iterable<String> allFilters = Stream.concat(query.getKeyFilters().stream(), Arrays.asList(query.filters).stream())
			.collect(Collectors.toList());

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

		if (Double.class.isInstance(value))
			return new PartitionKey((double)value);
		if (value instanceof Boolean)
			return new PartitionKey((boolean)value);
		if (String.class.isInstance(value))
			return new PartitionKey((String)value);
		if (value instanceof BigDecimal) {
			try {
				BigDecimal valueDecimal = (BigDecimal) value;
				double doubleValue = Double.parseDouble(valueDecimal.toString());
				BigDecimal convertedDecimalValue = new BigDecimal(Double.toString(doubleValue));
				if (valueDecimal.compareTo(convertedDecimalValue) != 0) {
					throw new Exception("Loss of precision converting from decimal to double. Partitionkey should be double.");
				} else
					return new PartitionKey(doubleValue);

				} catch (Exception ex) {
					throw new Exception("Loss of precision converting from decimal to double. Partitionkey should be double.");
			}
		}
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
