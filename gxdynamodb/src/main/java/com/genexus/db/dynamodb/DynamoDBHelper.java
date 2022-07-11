package com.genexus.db.dynamodb;

import com.genexus.db.service.VarValue;
import json.org.json.JSONArray;
import json.org.json.JSONObject;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamoDBHelper
{
	private static final SimpleDateFormat ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    public static AttributeValue toAttributeValue(VarValue var) throws SQLException
    {
		if(var == null)
			return null;
		Object value = var.value;
		if(value == null)
			return null;
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
				if(value instanceof java.util.Date)
					return builder.s(ISO_DATE_FORMATTER.format(value)).build();
				else return builder.s(((java.sql.Date) value).toLocalDate().toString()).build();
			case DateTime:
			case DateTime2:
				Timestamp valueTs;
				if(value instanceof java.sql.Timestamp)
					valueTs = (java.sql.Timestamp)value;
				else valueTs = new java.sql.Timestamp(((java.util.Date) value).getTime());
				return builder.s(valueTs.toLocalDateTime().atOffset(ZoneOffset.UTC).toString()).build();
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
				return builder.b((SdkBytes) value).build();
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
		else if(attValue.bool() != null)
			return attValue.bool().toString();
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
		}
		return true;
    }
}
