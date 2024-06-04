package com.genexus.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.GXutil;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GXHashMap<K, V> extends HashMap<K, V> {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GXHashMap.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private boolean isNumberKey = false;

	public void setHashMap(GXHashMap<K, V> hashMap) {
		putAll(hashMap);
	}

	public V put(K key, V value) {
		if (key instanceof Number)
			isNumberKey = true;
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
			return objectMapper.writeValueAsString(this);
		}
		catch (JsonProcessingException e) {
			log.error("Could not obtain json form Dictionary", e);
			return "";
		}
	}

	public void fromJson(String json) {
		Type type = new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return ((ParameterizedType) getClass().getEnclosingClass().getGenericSuperclass()).getActualTypeArguments();
			}

			@Override
			public Type getRawType() {
				return HashMap.class;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};

		try {
			this.clear();
			HashMap<K, V> fromJsonHashMap = objectMapper.readValue(json, objectMapper.getTypeFactory().constructType(type));
			if (!isNumberKey)
				this.putAll(fromJsonHashMap);
			else {
				for (Map.Entry<K, V> entry : fromJsonHashMap.entrySet()) {
					K key = entry.getKey();
					V value = entry.getValue();

					this.put((K) java.text.NumberFormat.getInstance().parse((String) key), value);
				}
			}
		}
		catch (Exception e) {
			log.error("Could not set Dictionary from json", e);
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
