package com.genexus.json;

import java.util.*;

import java.util.Map.Entry;

import com.genexus.common.interfaces.SpecificImplementation;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectWrapper extends JSONObject implements java.io.Serializable{
	private Map<String, Object> map;

	public JSONObjectWrapper() {
		super();
		if (map == null)
			map = new LinkedHashMap<String, Object>();
	}

	public JSONObjectWrapper(String string) {
		super(SpecificImplementation.JsonSerialization != null
			? SpecificImplementation.JsonSerialization.getJSONTokener(string)
			: new JSONTokenerWrapper(string)
		);
		initMap();
	}

	public JSONObjectWrapper(JSONTokenerWrapper token) {
		super(token);
		initMap();
	}

	public JSONObjectWrapper(Map<?,?> m) {
		super(m);
		map = new LinkedHashMap<String, Object>(m.size());
		for (final Entry<?, ?> e : m.entrySet()) {
			if (e.getKey() == null) {
				throw new NullPointerException("Null key.");
			}
			final Object value = e.getValue();
			if (value != null)
				this.map.put(String.valueOf(e.getKey()), value);
		}
	}

	public JSONObjectWrapper(JSONObject jsonObject) {
		this(jsonObject.toMap());
	}

	private void initMap() {
		if (map == null)
		{
			// this is a workaround for the Android implementation not loading the map in the JsonObject constructor
			if (SpecificImplementation.JsonSerialization != null)
				map = SpecificImplementation.JsonSerialization.getJSONObjectMap(this);
			else
				map = new LinkedHashMap<String, Object>();	
		}
	}

	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	public void clear() {
		super.clear();
		map.clear();
	}

	public JSONObjectWrapper put(String key, Object value) throws JSONException {
		if (value == null)
			super.put(key, JSONObject.NULL);
		else
			super.put(key, value);
		if (map == null)
			map = new LinkedHashMap<String, Object>();
		if (map.containsKey(key))
			map.replace(key, value);
		else
			map.put(key, value);
		return this;
	}

	public Object remove(String key) {
		if (map.containsKey(key))
			map.remove(key);
		return super.remove(key);
	}
}
