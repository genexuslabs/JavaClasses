package com.genexus.webpanels;

import com.genexus.GXutil;
import com.genexus.internet.IGxJSONSerializable;
import json.org.json.JSONArray;

import java.util.HashSet;
import java.util.Hashtable;

public class DynAjaxEventContext implements GXWebPanel.IDynAjaxEventContext {
	JSONArray inParmsMetadata;
	HashSet<String> inParmsMetadataHash = new HashSet<String>();
	JSONArray outParmsMetadata;
	HashSet<String> outParmsMetadataHash = new HashSet<String>();
	private Hashtable<String, String> inParmsHashValue = new Hashtable<String, String>();

	public void ClearParmsMetadata() {
		inParmsMetadata = new JSONArray();
		inParmsMetadataHash = new HashSet<String>();
		outParmsMetadata = new JSONArray();
		outParmsMetadataHash = new HashSet<String>();
	}

	public boolean isInputParm(String key) {
		return inParmsMetadataHash.contains(key);
	}

	public void Clear() {
		inParmsHashValue.clear();
	}

	public void SetParmHash(String fieldName, Object value) {
		IGxJSONSerializable jsonValue = (value instanceof IGxJSONSerializable) ? (IGxJSONSerializable) value : null;
		if (jsonValue != null) {
			inParmsHashValue.put(fieldName, GXutil.getHash(jsonValue.toJSonString()));
		}
	}

	public boolean isParmModified(String fieldName, Object value) {
		IGxJSONSerializable jsonValue = (value instanceof IGxJSONSerializable) ? (IGxJSONSerializable) value : null;
		if (value != null) {
			if (!inParmsHashValue.containsKey(fieldName))
				return true;
			return !GXutil.getHash(jsonValue.toJSonString()).equals(inParmsHashValue.get(fieldName));
		}
		return true;
	}
}
