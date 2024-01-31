package com.genexus.util;

import com.genexus.CommonUtil;
import com.genexus.LocalUtil;
import com.genexus.specific.java.Connect;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TestDateMethods {

    @Test
    public void testYearLimit() {
        Connect.init();
        LocalUtil localUtil = new LocalUtil('.', "MDY", "24", 30, "eng");
        Date testDate1 = CommonUtil.nullDate();
        Date testDate2 = CommonUtil.nullDate();

        String pattern = "dd/MM/yy";

        GXSimpleDateFormat df = new GXSimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getDefault());
        try {
            testDate1 = localUtil.applyYearLimit(df.parse("12/12/30"), pattern);
            testDate2 = localUtil.applyYearLimit(df.parse("08/05/76"), pattern);
        } catch (ParseException e) {

        }
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(testDate1);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 1930);
        calendar.setTime(testDate2);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 1976);
    }

    @Test
    public void testCtotex() {
        Connect.init();

        LocalUtil localUtil = new LocalUtil('.', "MDY", "24", 30, "eng");
        Date testDate1 = CommonUtil.nullDate();
        Date testDate2 = CommonUtil.nullDate();
        Date testDate3 = CommonUtil.nullDate();
        try {
            testDate1 = localUtil.ctotex("1930-01-01T00:00", 0);
            testDate2 = localUtil.ctotex("2023-01-01T00:00:00", 0);
            testDate3 = localUtil.ctotex("2200-12-31T00:00:00.000", 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(testDate1);
        Assert.assertEquals(1930, calendar.get(Calendar.YEAR));
        calendar.setTime(testDate2);
        Assert.assertEquals(2023, calendar.get(Calendar.YEAR));
        calendar.setTime(testDate3);
        Assert.assertEquals(2200, calendar.get(Calendar.YEAR));

        testDate1 = CommonUtil.nullDate();
        testDate2 = CommonUtil.nullDate();
        testDate3 = CommonUtil.nullDate();
        try {
            testDate1 = localUtil.ctotex("29-01-01", 0);
            testDate2 = localUtil.ctotex("30-01-01T00", 0);
            testDate3 = localUtil.ctotex("31-12-31T00:00", 0);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        calendar = GregorianCalendar.getInstance();
        calendar.setTime(testDate1);
        Assert.assertEquals(2029, calendar.get(Calendar.YEAR));
        calendar.setTime(testDate2);
        Assert.assertEquals(1930, calendar.get(Calendar.YEAR));
        calendar.setTime(testDate3);
        Assert.assertEquals(1931, calendar.get(Calendar.YEAR));
    }

    @Test
    public void testDateTimeToUTC() {
        Connect.init();

		TimeZone timezone = TimeZone.getTimeZone("America/Montevideo");

        String dateTime = "2023-03-22 15:00:00"; // input DateTime
		long expectedDiff = 10800000;
		ConvertDateTime(dateTime, timezone, expectedDiff, true);

		dateTime = "2011-02-20 1:00:00"; // input DateTime during summer time
		expectedDiff = 7200000;
		ConvertDateTime(dateTime, timezone, expectedDiff, true);
    }

	@Test
	public void DateTimeFromUTC() {
		Connect.init();

		TimeZone timezone = TimeZone.getTimeZone("America/Montevideo");

		String dateTime = "2023-02-22 18:00:00"; // input DateTime
		long expectedDiff = -10800000;
		ConvertDateTime(dateTime, timezone, expectedDiff, false);

		dateTime = "2011-02-20 3:00:00"; // input DateTime during summer time
		expectedDiff = -7200000;
		ConvertDateTime(dateTime, timezone, expectedDiff, false);
	}

    /**
     * DateTimeToUTC must not fail if the Timezone does not exists.
     */
    @Test
    public void testInvalidTimezone() {
        Connect.init();

		TimeZone timezone = TimeZone.getTimeZone("America/DoesnotExists");

        String dateTime = "2023-03-22 15:00:00"; // input DateTime
		long expectedDiff = 0;
		ConvertDateTime(dateTime, timezone, expectedDiff, true);
		ConvertDateTime(dateTime, timezone, expectedDiff, false);
    }

	private void ConvertDateTime(String dateTime, TimeZone timezone, long expectedDiff, boolean toUTC) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date mvdDate = dateFormat.parse(dateTime); // convert String to Date
			Date dateConverted;
			if (toUTC)
				dateConverted = CommonUtil.DateTimeToUTC(mvdDate, timezone);
			else
				dateConverted = CommonUtil.DateTimeFromUTC(mvdDate, timezone);

			long diff = dateConverted.getTime() - mvdDate.getTime();
			Assert.assertEquals("Timezone offset invalid", expectedDiff, diff);
		} catch (Exception e) {
			Assert.fail();
		}
	}

}