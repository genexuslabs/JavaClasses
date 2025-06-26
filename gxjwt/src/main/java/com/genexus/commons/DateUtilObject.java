package com.genexus.commons;


import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class DateUtilObject extends SecurityAPIObject {

	public DateUtilObject() {
		super();

	}

	public abstract String getCurrentDate();

	public abstract String currentPlusSeconds(long seconds);

	@SuppressWarnings("unused")
	public abstract String currentMinusSeconds(long seconds);
}
