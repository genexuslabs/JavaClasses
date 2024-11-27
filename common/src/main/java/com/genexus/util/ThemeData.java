package com.genexus.util;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import com.genexus.json.JSONObjectWrapper;

import java.util.ArrayList;

public class ThemeData
{
	String name;
	String[] baseLibraryCssReferences;
	
	public static ThemeData fromJson(String json) throws JSONException
	{
		JSONObjectWrapper jsonObj = new JSONObjectWrapper(json);
		
		ThemeData themeData = new ThemeData();
		themeData.name = jsonObj.getString("name");
		themeData.baseLibraryCssReferences = jsonArrayToStringArray(jsonObj.getJSONArray("baseLibraryCssReferences"));

		return themeData;
	}
	
	private static String[] jsonArrayToStringArray(JSONArray jsonArr) throws JSONException
	{
		List<String> list = new ArrayList<String>();
		for (int i=0; i<jsonArr.length(); i++) {
			list.add(jsonArr.getString(i));
		}
		String[] stringArray = list.toArray(new String[list.size()]);
		return stringArray;
	}
}
