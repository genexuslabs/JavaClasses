package com.genexus.JWT.utils;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class UnixTimestampCreator extends SecurityAPIObject {

	public UnixTimestampCreator() {
		super();
	}

	private static final Logger logger = LogManager.getLogger(UnixTimestampCreator.class);

	public String create(String date) {
		logger.debug("create");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			return Long.toString(new Long(dateFormat.parse(date).getTime() / 1000));
		} catch (Exception e) {

			error.setError("UTS01", "Date format error; expected yyyy/MM/dd HH:mm:ss");
			logger.error("create", e);
			return null;
		}
	}
}
