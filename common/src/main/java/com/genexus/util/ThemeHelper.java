package com.genexus.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import com.genexus.diagnostics.Log;

import json.org.json.JSONObject;

import com.genexus.CommonUtil;
import com.genexus.common.interfaces.SpecificImplementation;
import org.springframework.core.io.ClassPathResource;

public class ThemeHelper
{
	private static Map<String, ThemeData> m_themes = Collections.synchronizedMap(new HashMap<String, ThemeData>());
	
	private static ThemeData createDefaultThemeData(String themeName)
	{
		ThemeData themeData = new ThemeData();
		themeData.name = themeName;
		themeData.baseLibraryCssReferences = new String[] { };
		return themeData;
	}
	
	private static ThemeData getThemeData(String themeName)
	{
		if (!m_themes.containsKey(themeName))
		{
			File baseDirectory = new File(com.genexus.ApplicationContext.getInstance().getServletEngineDefaultPath());
			File themesDirectory = new File(baseDirectory, "themes");
			File themesJsonFile = new File(themesDirectory, themeName + ".json");
			ThemeData themeData;
			try
			{
				String json;
				if (com.genexus.ApplicationContext.getInstance().isSpringBootApp())
					json = new ClassPathResource("themes/" + themeName + ".json").getContentAsString(StandardCharsets.UTF_8);
				else
					json =  SpecificImplementation.GXutil.readFileToString(themesJsonFile, CommonUtil.normalizeEncodingName("UTF-8"));
				themeData = ThemeData.fromJson(json);
			}
			catch (Exception ex)
			{
				Log.warning("Unable to load theme metadata (" + themeName + "). Using an empty default one.", "", ex);
				themeData = createDefaultThemeData(themeName);
			}
			m_themes.put(themeName, themeData);
		}

		return m_themes.get(themeName);
	}
	
	public static String[] getThemeCssReferencedFiles(String themeName)
	{
		ThemeData themeData = getThemeData(themeName);
		return themeData.baseLibraryCssReferences;
	}
}