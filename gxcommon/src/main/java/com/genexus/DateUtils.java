package com.genexus;

import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import com.genexus.util.GXTimeZone;

public final class DateUtils {
	
	private static final long SEC_MIN = 60;
	private static final long MIN_HOUR = 60;
	private static final long MILLI_HOUR = SEC_MIN * MIN_HOUR * 1000;
		
	private static TimeZone defaultTimeZone;
	private static long    timeZoneOffset;
	private static boolean useDaylightTime;

	static
	{
		try
		{
			defaultTimeZone = GXTimeZone.getDefault();
			timeZoneOffset   = defaultTimeZone.getRawOffset();
			useDaylightTime  = defaultTimeZone.useDaylightTime();
		}
		catch (Exception e)
		{	
			System.err.println("PrivateUtilities static constructor error:" + e.getMessage());
			throw new ExceptionInInitializerError("PrivateUtilities static constructor error:" + e.getMessage()); 
		}	

	}

	public static long getDateAsTime(Date date)
	{
		long baseTime = date.getTime() + timeZoneOffset;

		if	(useDaylightTime && defaultTimeZone.inDaylightTime(date))
		{
			baseTime += MILLI_HOUR;
		}

		return baseTime;
	}

	public static Date getTimeAsDate(long time)
	{
		long baseTime = time - timeZoneOffset;

		if	(useDaylightTime && defaultTimeZone.inDaylightTime(new Date(baseTime)))
		{
			baseTime -= MILLI_HOUR;
		}

		return new Date(baseTime);
	}

}
