package com.genexus.gam.utils;

import com.genexus.GxUserType;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Dictionary {

	private final Map<String, Object> userMap;
	private final Gson gson;

	/********EXTERNAL OBJECT PUBLIC METHODS  - BEGIN ********/

	public Dictionary() {
		this.userMap = new LinkedHashMap<>();
		this.gson = new GsonBuilder().serializeNulls()
			.addSerializationExclusionStrategy(new ExclusionStrategy() {
				@Override
				public boolean shouldSkipField(FieldAttributes f) {
					// exclude only the field 'map' inherited from org.json.JSONObject
					return f.getName().equals("map") &&
						f.getDeclaringClass() == org.json.JSONObject.class;
				}

				@Override
				public boolean shouldSkipClass(Class<?> clazz) {
					return false;
				}
			})
			.registerTypeHierarchyAdapter(Number.class, new TypeAdapter<Number>() {
				@Override
				public void write(JsonWriter out, Number value) throws IOException {
					if (value == null) {
						out.nullValue();
						return;
					}

					double d = value.doubleValue();
					if (d == Math.rint(d)) {
						out.value(value.longValue()); // 1.0 → 1
					} else {
						out.value(d);                 // 1.5 → 1.5
					}
				}

				@Override
				public Number read(JsonReader in) throws IOException {
					return in.nextDouble();
				}
			})
			.create();
	}

	public Object get(String key) {
		return this.userMap.get(key);
	}

	public void set(String key, Object value) {
		objectToMap(key, value);
	}

	public void remove(String key) {
		this.userMap.remove(key);
	}

	public void clear() {
		this.userMap.clear();
	}

	public String toJsonString() {
		return this.gson.toJson(this.userMap);
	}

	/********EXTERNAL OBJECT PUBLIC METHODS  - END ********/

	// Convert a JSON String to Map<String, Object>
	private Map<String, Object> jsonStringToMap(String jsonString) {
		Type type = new TypeToken<Map<String, Object>>() {}.getType();
		return this.gson.fromJson(jsonString, type);
	}

	private Object deepConvert(Object value) {
		if (value == null) return null;

		if (value instanceof Number || value instanceof Boolean) {
			return value;
		}

		if (value instanceof String) {
			try {
				JsonElement parsed = JsonParser.parseString((String) value);
				if (parsed.isJsonObject()) {
					return gson.fromJson(parsed, Map.class);
				}
				if (parsed.isJsonArray()) {
					return gson.fromJson(parsed, List.class);
				}
				return ((JsonPrimitive) parsed).getAsString();
			} catch (Exception e) {
				return value;
			}
		}

		// SDT => Map
		if (value instanceof GxUserType) {
			String s = ((GxUserType) value).toJSonString();
			return jsonStringToMap(s);
		}

		// Map => deep convert values
		if (value instanceof Map) {
			Map<String,Object> newMap = new LinkedHashMap<>();
			((Map<?,?>) value).forEach((k,v) -> newMap.put(k.toString(), deepConvert(v)));
			return newMap;
		}

		// List => deep convert each element
		if (value instanceof List) {
			List<Object> newList = new java.util.ArrayList<>();
			for (Object o : (List<?>) value) newList.add(deepConvert(o));
			return newList;
		}

		// Catch: JSONObjectWrapper
		if (value.getClass().getName().contains("JSONObjectWrapper") ||
			value instanceof org.json.JSONObject) {
			// Convert org.json to Map
			return jsonStringToMap(value.toString());
		}

		return value.toString();
	}

	private void objectToMap(String key, Object value) {
		this.userMap.put(key, deepConvert(value));
	}
}
