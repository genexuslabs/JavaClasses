package com.genexus.util;


import java.util.regex.Pattern;

public class CSSHelper
{
	private static Pattern multiSemicolonRegex = Pattern.compile(";{2,}");

	public static String Prettify(String uglyCSS)
	{
		return CleanupSemicolons(uglyCSS);
	}

	public static String CleanupSemicolons(String uglyCSS)
	{
		if (uglyCSS.length() > 1)
		{
			String betterCSS = multiSemicolonRegex.matcher(uglyCSS).replaceAll(";");
			return betterCSS.charAt(0) == ';' ? betterCSS.substring(1) : betterCSS;
		}
		return uglyCSS;
	}
}
