package com.genexus.util;

import java.util.*;

public class GXTimeZone
{
	private static TimeZone originalTimeZone;

	public static TimeZone getDefaultOriginal()
	{
		return GXTimeZone.getDefault();
	}

	public static TimeZone getDefault()
	{
		TimeZone tz = TimeZone.getDefault();
		return tz;
	}
}
