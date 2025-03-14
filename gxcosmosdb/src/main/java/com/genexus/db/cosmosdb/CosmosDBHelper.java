package com.genexus.db.cosmosdb;

import com.fasterxml.jackson.databind.JsonNode;
import com.genexus.db.ServiceCursorBase;
import com.genexus.db.service.GXType;
import com.genexus.db.service.VarValue;
import org.json.JSONException;
import com.genexus.json.JSONObjectWrapper;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CosmosDBHelper {

	private static final String TABLE_ALIAS = "t";
	private static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'00:00:00.000'Z'";
	public static boolean formattedAsStringGXType(GXType gXType)
	{
		return (gXType == GXType.VarChar || gXType == GXType.DateAsChar || gXType == GXType.NVarChar || gXType == GXType.LongVarChar || gXType == GXType.NChar ||  gXType == GXType.Char || gXType == GXType.Text || gXType == GXType.NText);
	}
	public static boolean formattedAsStringDateGXType(GXType gXType)
	{
		return (gXType == GXType.Date || gXType == GXType.DateTime || gXType == GXType.DateTime2);
	}
	private static String setupQuery(String projectionList, String filterExpression, String tableName, String orderbys) throws Exception {
		String sqlSelect = "";
		String sqlFrom = "";
		String sqlWhere = "";
		String sqlOrder = "";

		String SELECT_TEMPLATE = "select %s";
		String FROM_TEMPLATE = "from %s t";
		String WHERE_TEMPLATE = "where %s";
		String ORDER_TEMPLATE = "order by %s";

		if (projectionList != null && !projectionList.isEmpty()) {
			sqlSelect = String.format(SELECT_TEMPLATE, projectionList);
		} else {
			throw new Exception("Error setting up the query. Projection list is empty.");
		}

		if (tableName != null && !tableName.isEmpty()) {
			sqlFrom = String.format(FROM_TEMPLATE, tableName);
		} else {
			throw new Exception("Error setting up the query. Table name is empty.");
		}

		if (filterExpression != null && !filterExpression.isEmpty()) {
			sqlWhere = String.format(WHERE_TEMPLATE, filterExpression);
		}

		if (orderbys != null && !orderbys.isEmpty()) {
			sqlOrder = String.format(ORDER_TEMPLATE, orderbys);
		}

		return sqlSelect + " " + sqlFrom + " " + sqlWhere + " " + sqlOrder;
	}

	private static String GetEscapedProperty(Pattern regexPattern, String key)
	{
		Matcher matcher = regexPattern.matcher(key);
		if (matcher.find())
			return TABLE_ALIAS + "[\"" + key + "\"]";
		else
			return TABLE_ALIAS + "." + key;
	}
	public static String createCosmosQuery(CosmosDBQuery query, ServiceCursorBase cursorDef, Object[] parms) throws Exception {
		String tableName = query.tableName;
		String[] projection = query.projection;
		String element;
		String projectionList = "";
		String pattern = "[^a-zA-Z0-9]+"; //Special characters supported for property names
		Pattern regexPattern = Pattern.compile(pattern);

		for (String key : projection) {
			Matcher matcher = regexPattern.matcher(key);
			element = GetEscapedProperty(regexPattern,key);
			if (!projectionList.isEmpty())
				projectionList = element + "," + projectionList;
			else
				projectionList = element;
		}

		List<String> allFilters = Stream.concat(query.getKeyFilters().stream(), Arrays.asList(query.filters).stream())
			.collect(Collectors.toList());

		List<String> allFiltersQuery = new ArrayList<>();
		List<String> keyFilterQ = new ArrayList<>();

		for (String keyFilter : allFilters) {
			String filterProcess = keyFilter.toString();
			filterProcess = filterProcess.replace("[", "(");
			filterProcess = filterProcess.replace("]", ")");

			for (Map.Entry<String, VarValue> entry : query.getVars().entrySet()) {
				String entryKey = entry.getKey();
				VarValue entryValue = entry.getValue();
				String varValuestr = "";
				if (filterProcess.contains(entryValue.name + ":")) {

					if (formattedAsStringGXType(entryValue.type))
						varValuestr = '"' + entryValue.value.toString()  + '"';
					else
					{
						if (formattedAsStringDateGXType(entryValue.type))
						{
							DateTimeFormatter dtf = DateTimeFormatter.ofPattern(ISO_DATETIME_FORMAT);
							try {
								java.sql.Timestamp ts = (java.sql.Timestamp)entryValue.value;
								varValuestr = '"' + ts.toLocalDateTime().format(dtf) + '"';
							}
							catch (Exception ex)
							{
								java.sql.Date sqlDate = (java.sql.Date)entryValue.value;
								SimpleDateFormat outputDateFormat = new SimpleDateFormat(ISO_DATE_FORMAT);
								outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
								varValuestr = '"' + outputDateFormat.format(sqlDate) + '"';
							}
						}
						else {
							varValuestr = entryValue.value.toString();
							varValuestr = varValuestr.equals("True") ? "true" : varValuestr;
							varValuestr = varValuestr.equals("False") ? "false" : varValuestr;
						}
					}
					filterProcess = filterProcess.replace(entryValue.name + ":", varValuestr);
				}
			}
			filterProcess = filterProcess.replace("Func.", "");
			for (String d : projection) {
				String wholeWordPattern = String.format("\\b%s\\b", d);
				Matcher matcher = regexPattern.matcher(wholeWordPattern);
				if (matcher.find())
					filterProcess = filterProcess.replaceAll(wholeWordPattern, TABLE_ALIAS + "[\"" + d + "\"]");
				else
					filterProcess = filterProcess.replaceAll(wholeWordPattern, TABLE_ALIAS + "." + d);
			}
			keyFilterQ = Arrays.asList(filterProcess);
			allFiltersQuery.addAll(keyFilterQ);
		}
		String filterExpression = allFiltersQuery.isEmpty() ? null : String.join(" AND ", allFiltersQuery);
		List<String> orderExpressionList = new ArrayList<>();
		String expression = "";

		for (String orderAtt : query.orderBys) {
			Matcher matcher = regexPattern.matcher(orderAtt);
			if (!matcher.find())
				expression = orderAtt.startsWith("(") ? TABLE_ALIAS + "." + orderAtt.substring(1, orderAtt.length() - 1) + " DESC"
					: TABLE_ALIAS + "." + orderAtt + " ASC";
			else
				expression = orderAtt.startsWith("(") ? TABLE_ALIAS + "[\"" + orderAtt.substring(1, orderAtt.length() - 1) + "\"]" + " DESC"
				: TABLE_ALIAS + "[\"" + orderAtt + "\"]" + "ASC";
			orderExpressionList.add(expression);
		}
		String orderExpression = String.join(",", orderExpressionList);
		return setupQuery(projectionList, filterExpression, tableName, orderExpression);
	}

	public static HashMap<String, Object> jsonNodeToHashMap(JsonNode jsonNode) {

		HashMap<String, Object> result = new HashMap<>();
		if (jsonNode != null) {
			jsonNode.fields().forEachRemaining(entry -> {
				String key = entry.getKey();
				JsonNode value = entry.getValue();
				if (value.isObject()) {
					result.put(key, jsonNodeToHashMap(value));
				} else if (value.isArray()) {
					result.put(key, jsonNodeToList(value));
				} else if (value.isValueNode()) {
					if (value.isNull())
						result.put(key, null);
					else
						result.put(key, value.asText());
				}
			});
		}
		return result;
	}

	public static List<Object> jsonNodeToList(JsonNode jsonNode) {
		List<Object> result = new ArrayList<>();
		jsonNode.forEach(value -> {
			if (value.isObject()) {
				result.add(jsonNodeToHashMap(value));
			} else if (value.isArray()) {
				result.add(jsonNodeToList(value));
			} else if (value.isValueNode()) {
				result.add(value.asText());
			}
		});
		return result;
	}
	private static boolean tryConvertToDateISOFormat(VarValue parm, String[] dateStr) {
		Object value = parm.value;
		if (parm.type == GXType.DateTime || parm.type == GXType.DateTime2)
		{
			Timestamp valueTs = (java.sql.Timestamp)value;
			dateStr[0] = valueTs.toLocalDateTime().atOffset(ZoneOffset.UTC).toString();
			return true;
		}
		else if (parm.type == GXType.Date) {

			java.sql.Date sqlDate = (java.sql.Date)value;
			SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'");
			outputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			dateStr[0] = outputDateFormat.format(sqlDate);
			return true;
		}
		return false;
	}
	public static JSONObjectWrapper addItemValue(String name, VarValue parm, JSONObjectWrapper jsonObject) throws JSONException, SQLException {
		if (parm == null) {
			throw new SQLException(String.format("Cannot assign attribute value (name: %s)", name));
		}
		if (jsonObject == null)
			jsonObject = new JSONObjectWrapper();
		if (parm.value!= null) {
			String[] dateStr = new String[1];
			if (tryConvertToDateISOFormat(parm, dateStr))
			{
				if (dateStr[0] != "")
					jsonObject.put(name, dateStr[0]);
				else
					//Couldn´t be converted. Insert in the original format
					jsonObject.put(name,parm.value);
			}
			else
				jsonObject.put(name, parm.value);
		}
		else
			jsonObject.put(name, JSONObjectWrapper.NULL);
		return jsonObject;
	}
}
