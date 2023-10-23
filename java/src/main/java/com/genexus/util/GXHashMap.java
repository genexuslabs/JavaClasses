package com.genexus.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class GXHashMap<K, V> extends HashMap<K, V> {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GXHashMap.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public void setHashMap(GXHashMap<K, V> hashMap) {
		putAll(hashMap);
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
			this.putAll(objectMapper.readValue(json, objectMapper.getTypeFactory().constructType(type)));
		}
		catch (JsonProcessingException e) {
			log.error("Could not set Dictionary from json", e);
		}
	}
}
