package com.genexus.JWT.claims;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeaderParameters {
	/*
	 * Cannot avoid typ=JWT because of RFC 7519 https://tools.ietf.org/html/rfc7519
	 * https://github.com/auth0/java-jwt/issues/369
	 */

	private Map<String, Object> map;

	public HeaderParameters() {
		map = new HashMap<String, Object>();
	}

	public void setParameter(String name, String value) {
		map.put(name, value);
	}

	public Map<String, Object> getMap() {
		return this.map;
	}

	public List<String> getAll() {
		return new ArrayList<String>(map.keySet());
	}

	public boolean isEmpty()
	{
		if (getAll().size() == 0)
		{
			return true;
		}
		return false;
	}

}
