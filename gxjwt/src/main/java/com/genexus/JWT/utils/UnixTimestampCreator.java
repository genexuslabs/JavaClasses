package com.genexus.JWT.utils;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UnixTimestampCreator  extends SecurityAPIObject {

	public UnixTimestampCreator()
	{
		super();
	}

	public String create(String date)
	{
		Date datef = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {

			datef = dateFormat.parse(date);

		} catch (Exception e) {

			error.setError("UTS01", "Date format error; expected yyyy/MM/dd HH:mm:ss");
			return null;
		}
		return Long.toString(new Long(datef.getTime()/1000));
	}
}
