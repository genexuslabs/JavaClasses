package com.genexus.sd.store.validation;

import java.util.Date;

public class Util {
	
	public static Date FromUnixTime(long unixMillisecondsTime)
	{		
		return new Date(unixMillisecondsTime);		
	}
}
