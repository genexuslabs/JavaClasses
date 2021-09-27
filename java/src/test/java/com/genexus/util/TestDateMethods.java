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
		Date testDate1 = CommonUtil.nullDate();
		Date testDate2 = CommonUtil.nullDate();

		String pattern = "dd/MM/yy";

		GXSimpleDateFormat df = new GXSimpleDateFormat(pattern);
		df.setTimeZone(TimeZone.getDefault());
		try
		{
			testDate1 = localUtil.applyYearLimit(df.parse("12/12/30"), pattern);
			testDate2 = localUtil.applyYearLimit(df.parse("08/05/76"), pattern);
		}
		catch (ParseException e)
		{

		}
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(testDate1);
		Assert.assertTrue(calendar.get(Calendar.YEAR) == 1930);
		calendar.setTime(testDate2);
		Assert.assertTrue(calendar.get(Calendar.YEAR) == 1976);
	}
}