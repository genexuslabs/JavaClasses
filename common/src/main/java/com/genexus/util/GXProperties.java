package com.genexus.util;

import java.util.*;

import com.genexus.internet.IGxJSONSerializable;

import com.genexus.json.JSONObjectWrapper;
import org.json.JSONException;

import com.genexus.CommonUtil;
import com.genexus.SdtMessages_Message;
import com.genexus.GXBaseCollection;

public class GXProperties implements IGxJSONSerializable {
	private LinkedHashMap < String, GXProperty > properties = new LinkedHashMap < > ();
	private boolean eof;
	private int lastElement;

	public GXProperties() {}

	public void set(String name, String value) {
		this.put(name, value);
	}

	public void add(String name, String value) { this.put(name, value); }

	public void put(String name, String value) {
		String lowerName = name.toLowerCase();
		properties.put(lowerName, new GXProperty(name, value));
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (GXProperty property: properties.values())
			builder.append(property.getValue());
		return builder.toString();
	}

	public String get(String name) {
		name = name.toLowerCase();
		return containsKey(name) ? properties.get(name).getValue() : "";
	}

	public void remove(String name) {
		name = name.toLowerCase();
		properties.remove(name);
	}

	public boolean containsKey(String name) {
		name = name.toLowerCase();
		return properties.containsKey(name);
	}

	public GXProperty item(int i) {
		int counter = 0;
		for (Map.Entry < String, GXProperty > entry: properties.entrySet()) {
			if (counter++ == i) {
				return entry.getValue();
			}
		}
		throw new IndexOutOfBoundsException("The provided index is larger than the amount of items stored");
	}

	public int getCount() {
		return count();
	}

	public int count() {
		return properties.size();
	}

	public void clear() {
		properties.clear();
	}

	public GXProperty first() {
		eof = false;
		if (count() > 0) {
			lastElement = 0;
			return properties.entrySet().iterator().next().getValue();
		} else {
			eof = true;
			return null;
		}
	}

	public boolean eof() {
		return eof;
	}

	public GXProperty next() {
		lastElement++;
		if (count() > lastElement) {
			return item(lastElement);
		} else {
			eof = true;
			return null;
		}
	}

	public Object GetJSONObject() {
		JSONObjectWrapper jObj = new JSONObjectWrapper();
		int i = 0;
		while (count() > i) {
			GXProperty prop = item(i);
			try {
				jObj.put(prop.getKey(), prop.getValue());
			} catch (JSONException e) {}
			i++;
		}
		return jObj;
	}

	public String toJSonString() {
		JSONObjectWrapper jObj = (JSONObjectWrapper) GetJSONObject();
		return jObj.toString();
	}

	public ArrayList<GXProperty> getList() {
		ArrayList<GXProperty> list = new ArrayList<>();
		int i = 0;
		while (count() > i) {
			list.add(item(i));
			i++;
		}
		return list;
	}

	public boolean fromJSonString(String s) {
		return fromJSonString(s, null);
	}

	public boolean fromJSonString(String s, GXBaseCollection < SdtMessages_Message > messages) {
		this.clear();
		if (!s.isEmpty()) {
			try {
				JSONObjectWrapper jObj = new JSONObjectWrapper(s);
				for (Map.Entry<String, Object> e : jObj.entrySet()) {
					String key = e.getKey();
					this.put(key, jObj.get(key).toString());
				}
				return true;
			} catch (JSONException ex) {
				CommonUtil.ErrorToMessages("fromjson error", ex.getMessage(), messages);
				return false;
			}
		} else {
			CommonUtil.ErrorToMessages("fromjson error", "empty string", messages);
			return false;
		}
	}
}
