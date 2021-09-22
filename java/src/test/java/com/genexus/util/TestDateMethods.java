package com.genexus.util;

import com.genexus.Application;
import com.genexus.CommonUtil;
import com.genexus.LocalUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Test;

public class TestDateMethods {

	@Test
	public void testYearLimit(){
		Connect.init();
		LocalUtil localUtil = new LocalUtil('.', "MDY", "24", 30, "eng");
		Date testDate = CommonUtil.nullDate();

		String pattern = "dd/MM/yy";

		GXSimpleDateFormat df = new GXSimpleDateFormat(pattern);
		df.setTimeZone(TimeZone.getDefault());
		try
		{
			testDate = localUtil.applyYearLimit(df.parse("12/12/30"), pattern);
		}
		catch (ParseException e)
		{

		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(testDate);
		Assert.assertTrue(calendar.get(Calendar.YEAR) == 1930);
	}
}