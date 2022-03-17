package com.genexus.db.dynamodb;

import com.genexus.db.service.VarValue;
import json.org.json.JSONArray;
import json.org.json.JSONObject;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamoDBHelper
{
    public static AttributeValue toAttributeValue(VarValue var) throws SQLException
    {
		Object value = var.value;
		AttributeValue.Builder builder = AttributeValue.builder();
		switch (var.type)
		{
			case Number:
			case Int16:
			case Int32:
			case Int64:
			case Decimal:
				return builder.n(value.toString()).build();
			case Date:
			case DateTime:
			case DateTime2:
				return builder.s(value.toString()).build();
			case Boolean:
			case Byte:
				return builder.bool((Boolean) value).build();
			case Char:
			case VarChar:
			case LongVarChar:
			case Text:
			case NChar:
			case NVarChar:
			case NText:

			// Unused datatypes
			case UniqueIdentifier:
			case Xml:
			case Geography:
			case Geopoint:
			case Geoline:
			case Geopolygon:
				return builder.s(value.toString()).build();
			case NClob:
			case Clob:
			case Raw:
			case Blob:
			case Undefined:
			case Image:
			case DateAsChar:
			default:
				throw new SQLException(String.format("DynamoDB unsupported type (%s)", var.type));
		}
    }

    public static String getString(AttributeValue attValue)
    {
		if(attValue == null)
			return null;
		String value = attValue.s();
		if (value != null)
			return value;
		else if (!attValue.ns().isEmpty())
			return setToString(attValue.ns());
		else if (!attValue.ss().isEmpty())
			return setToString(attValue.ss());
		else if (attValue.hasM())
			return new JSONObject(convertToDictionary(attValue.m())).toString();
		else if (attValue.hasL())
			return new JSONArray(attValue.l().stream().map(DynamoDBHelper::getString).collect(Collectors.toList())).toString();
		return null;
	}

	private static HashMap<String, String> convertToDictionary(Map<String, AttributeValue> m)
	{
		HashMap<String, String> dict = new HashMap<>();
		for (Map.Entry<String, AttributeValue> keyValues : m.entrySet())
		{
			dict.put(keyValues.getKey(), getString(keyValues.getValue()));
		}
		return dict;
	}

	private static String setToString(List<String> nS)
	{
		return String.format("[ %s ]", String.join(", ", nS));
	}

    public static boolean addAttributeValue(String parmName, HashMap<String, AttributeValue> values, VarValue parm) throws SQLException
    {
		if(parm == null)
			return false;
		AttributeValue value = toAttributeValue(parm);
		if (value != null)
		{
			values.put(parmName, value);
			return true;
		}
		return false;

    }
}
