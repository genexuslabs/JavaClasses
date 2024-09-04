package com.genexus.specific.android;

import com.genexus.common.interfaces.IExtensionJSONSerialization;
import com.genexus.json.JSONObjectWrapper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class AndroidJSONSerialization implements IExtensionJSONSerialization {
	@Override
	public Iterator<Map.Entry<String, Object>> getJSONObjectIterator(JSONObjectWrapper obj) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (Iterator<String> it = obj.keys(); it.hasNext(); ) {
			String k = it.next();
			map.put(k, null); // value is not used for now, so we just set it as null
		}

		return map.entrySet().iterator();
	}
}