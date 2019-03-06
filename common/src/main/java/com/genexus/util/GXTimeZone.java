package com.genexus.util;

import java.util.*;

public class GXTimeZone
{
	private static TimeZone originalTimeZone;

	public static TimeZone getDefaultOriginal()
	{
		if (originalTimeZone == null)
		{
			return GXTimeZone.getDefault();
		}
		else
		{
			return originalTimeZone;
		}
	}


	public static TimeZone getDefault()
	{
		TimeZone tz = 	TimeZone.getDefault();
		return tz;
	}
}
