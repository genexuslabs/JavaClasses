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

	private void objectToMap(String key, Object value) {
		if (value == null) {
			this.userMap.put(key, null);
		} else if (value instanceof Number || value instanceof Boolean || value instanceof Map || value instanceof List) {
			this.userMap.put(key, value);
		} else if (value instanceof GxUserType) {
			this.userMap.put(key, jsonStringToMap(((GxUserType) value).toJSonString()));
		} else if (value instanceof String) {
			String str = (String) value;

			// Try to parse as JSON
			try {
				JsonElement parsed = JsonParser.parseString(str);
				if (parsed.isJsonObject()) {
					this.userMap.put(key, this.gson.fromJson(parsed, Map.class));
				} else if (parsed.isJsonArray()) {
					this.userMap.put(key, this.gson.fromJson(parsed, List.class));
				} else if (parsed.isJsonPrimitive()) {
					JsonPrimitive primitive = parsed.getAsJsonPrimitive();
					if (primitive.isBoolean()) {
						this.userMap.put(key, primitive.getAsBoolean());
					} else if (primitive.isNumber()) {
						this.userMap.put(key, primitive.getAsNumber());
					} else if (primitive.isString()) {
						this.userMap.put(key, primitive.getAsString());
					}
				}
			} catch (JsonSyntaxException e) {
				// Invalid JSON: it is left as string
				this.userMap.put(key, str);
			}
		} else {
			// Any other object: it is converted to string
			this.userMap.put(key, value.toString());
		}
	}
}
