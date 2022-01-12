package com.genexus.internet;
import java.util.Hashtable;
import com.genexus.*;
import java.util.StringTokenizer;
import java.util.Enumeration;

public class LocationCollection
{
	Hashtable<String, Location> table = new Hashtable<>();

	public void add(Location value, String key)
	{
		table.put(CommonUtil.upper(key), value);
	}

	public void removeAllItems()
	{
		table.clear();
	}

	public void clear()
	{
		table.clear();
	}

	public Location item(String key)
	{
		return table.get(CommonUtil.upper(key));
	}

	public int getCount()
	{
		return table.size();
	}

}

