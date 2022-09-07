package com.genexus.db.service;

import java.util.HashMap;

public class IOMap implements IODataMap{
	private final String name;

	public IOMap(String name)
	{
		this.name = name;
	}

	@Override
	public Object getValue(IOServiceContext context, HashMap<String, Object> currentEntry) {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setValue(HashMap<String, Object> currentEntry, Object value) {

	}
}
