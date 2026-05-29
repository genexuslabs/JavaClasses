package com.genexus.util;

import com.genexus.AndroidLog;
import com.genexus.GXutil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GXHashMap<K, V> extends HashMap<K, V> {

	private static final Gson gson = new Gson();
	private boolean isNumberKey = false;
	private Class<?> valueClass = null;

	public void setValueClass(Class<?> valueClass) {
		this.valueClass = valueClass;
	}

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
			if (remove(key) != null)
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
			AndroidLog.error("Could not obtain json form Dictionary "+ e.getMessage());
			return "";
		}
	}

	public void fromJson(String json) {
		try {
			this.clear();
			JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				K key;
				if (isNumberKey) {
					key = (K) java.text.NumberFormat.getInstance().parse(entry.getKey());
				} else {
					key = (K) entry.getKey();
				}
				V value;
				if (valueClass != null) {
					value = (V) gson.fromJson(entry.getValue(), valueClass);
				} else {
					value = (V) gson.fromJson(entry.getValue(), Object.class);
				}
				this.put(key, value);
			}
		}
		catch (Exception e) {
			AndroidLog.error("Could not set Dictionary from json " + e.getMessage());
		}
	}

	public GXHashMap<K, String> dateToCharRest() {
		return convertToCharRest(this, false);
	}

	public GXHashMap<K, String> timeToCharRest() {
		return convertToCharRest(this, true);
	}

	private GXHashMap<K, String> convertToCharRest(GXHashMap<K, V> thisHashMap, boolean isDateTime) {
		GXHashMap<K, String> chardMap = new GXHashMap<>();

		for (Map.Entry<K, V> entry : thisHashMap.entrySet()) {
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
