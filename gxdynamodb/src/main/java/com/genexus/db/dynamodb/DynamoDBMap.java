package com.genexus.db.dynamodb;

import com.genexus.db.service.*;

public class DynamoDBMap extends IODataMapName{
//ver DynamoDBMaps.cs
	public DynamoDBMap(String name) {
		super(mapAttributeMap(name));
		needsAttributeMap = _needsAttributeMap(name);
	}

	private static String mapAttributeMap(String name)
	{
		if(_needsAttributeMap(name))
			return name.substring(1);
		else return name;
	}

	private static boolean _needsAttributeMap(String name)
	{
		return name.startsWith("#");
	}

	private final boolean needsAttributeMap;

	public boolean needsAttributeMap() {
		return needsAttributeMap;
	}
}
