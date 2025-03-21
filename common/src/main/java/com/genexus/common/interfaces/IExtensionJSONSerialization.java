package com.genexus.common.interfaces;

import com.genexus.json.JSONObjectWrapper;
import org.json.JSONTokener;

import java.util.Iterator;
import java.util.Map;

public interface IExtensionJSONSerialization {
	Iterator<Map.Entry<String, Object>> getJSONObjectIterator(JSONObjectWrapper obj);
	JSONTokener getJSONTokener(String s);
}
