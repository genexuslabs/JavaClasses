package com.genexus.util;

import com.genexus.AndroidLog;
import com.genexus.GXutil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GXHashMap<K, V> extends HashMap<K, V> {

	private static final Gson gson = new Gson();
	private boolean isNumberKey = false;
	private Class<?> valueClass = null;

	public void setHashMap(GXHashMap<K, V> hashMap) {
		if (hashMap.valueClass != null && this.valueClass == null)
			this.valueClass = hashMap.valueClass;
		putAll(hashMap);
	}

	public V put(K key, V value) {
		if (key instanceof Number)
			isNumberKey = true;
		if (value != null && valueClass == null)
			valueClass = value.getClass();
		return super.put(key, value);
	}

	public boolean get(K key, V[] value) {
		if (containsKey(key)) {
			value[0] = get(key);
			return true;
		}
		return false;
	}

	public boolean removeKey(K key) {
		if (containsKey(key)) {
			remove(key);
			return true;
		}
		return false;
	}

	public void removeKeys(Vector<K> keys) {
		for (int i = 0; i < keys.size(); i++) {
			removeKey(keys.get(i));
		}
	}

	public void removeAll(GXHashMap<K, V> hashMap) {
		for (Map.Entry<K, V> entry : hashMap.entrySet()) {
			removeKey(entry.getKey());
		}
	}

	public String toJson() {
		try {
			return gson.toJson(this);
		}
		catch (Exception e) {
			AndroidLog.error("Could not obtain json from Dictionary "+ e.getMessage());
			return "";
		}
	}

	public void fromJson(String json) {
		try {
			JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
			HashMap<K, V> parsed = new HashMap<>();
			// Detectar el tipo de V desde el primer elemento si no lo conocemos
			if (valueClass == null && jsonObject.size() > 0) {
				JsonElement firstValue = jsonObject.entrySet().iterator().next().getValue();
				Object resolved = resolveJsonValue(firstValue);
				if (resolved != null)
					valueClass = resolved.getClass();
			}
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				K key;
				if (isNumberKey) {
					key = (K) parseNumericKey(entry.getKey());
				} else {
					key = (K) entry.getKey();
				}
				V value;
				if (valueClass != null) {
					value = (V) gson.fromJson(entry.getValue(), valueClass);
				} else {
					value = (V) resolveJsonValue(entry.getValue());
				}
				parsed.put(key, value);
			}
			this.clear();
			this.putAll(parsed);
		}
		catch (Exception e) {
			AndroidLog.error("Could not set Dictionary from json " + e.getMessage());
		}
	}

	private Number parseNumericKey(String keyString) {
		try {
			long longValue = Long.parseLong(keyString);
			if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
				return (int) longValue;
			}
			return longValue;
		} catch (NumberFormatException e) {
			return Double.valueOf(keyString);
		}
	}

	/**
	 * Resuelve un JsonElement al tipo Java equivalente,
	 * imitando el comportamiento de Jackson (UntypedObjectDeserializer):
	 * - JSON integer   → Integer (o Long si es grande)
	 * - JSON decimal   → Double
	 * - JSON string    → String
	 * - JSON boolean   → Boolean
	 * - JSON object    → HashMap
	 * - JSON array     → ArrayList
	 * - JSON null      → null
	 */
	private Object resolveJsonValue(JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return null;
		}
		if (element.isJsonPrimitive()) {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (primitive.isBoolean()) {
				return primitive.getAsBoolean();
			}
			if (primitive.isNumber()) {
				Number number = primitive.getAsNumber();
				double doubleValue = number.doubleValue();
				if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
					long longValue = number.longValue();
					if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
						return (int) longValue;
					}
					return longValue;
				}
				return doubleValue;
			}
			return primitive.getAsString();
		}
		if (element.isJsonObject()) {
			HashMap<String, Object> map = new HashMap<>();
			for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
				map.put(entry.getKey(), resolveJsonValue(entry.getValue()));
			}
			return map;
		}
		if (element.isJsonArray()) {
			ArrayList<Object> list = new ArrayList<>();
			for (JsonElement item : element.getAsJsonArray()) {
				list.add(resolveJsonValue(item));
			}
			return list;
		}
		return element.toString();
	}

	public GXHashMap<K, String> dateToCharRest() {
		return convertToCharRest(false);
	}

	public GXHashMap<K, String> timeToCharRest() {
		return convertToCharRest(true);
	}

	private GXHashMap<K, String> convertToCharRest(boolean isDateTime) {
		GXHashMap<K, String> chardMap = new GXHashMap<>();

		for (Map.Entry<K, V> entry : this.entrySet()) {
			K key = entry.getKey();
			V dateValue = entry.getValue();

			String stringValue = isDateTime? GXutil.timeToCharREST((Date)dateValue) : GXutil.dateToCharREST((Date)dateValue);
			chardMap.put(key, stringValue);
		}
		return chardMap;
	}

	public GXHashMap<K, V> charToDateREST(GXHashMap<K, String> charHashMap, boolean isDateTime) {
		clear();

		for (Map.Entry<K, String> entry : charHashMap.entrySet()) {
			K key = entry.getKey();
			String stringValue = entry.getValue();

			V dateValue = isDateTime? (V)GXutil.charToTimeREST(stringValue): (V)GXutil.charToDateREST(stringValue);
			put(key, dateValue);
		}
		return this;
	}
}
