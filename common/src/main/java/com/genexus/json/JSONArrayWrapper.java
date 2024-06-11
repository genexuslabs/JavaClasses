package com.genexus.json;

import org.json.JSONArray;

import java.util.LinkedHashMap;

public class JSONArrayWrapper extends JSONArray implements java.io.Serializable{

	public JSONArrayWrapper() {
		super();
	}

	public JSONArrayWrapper(String string) {
		super(string);
	}

	public JSONArrayWrapper(JSONArray array) {
		super(array);
	}
}
