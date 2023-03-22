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
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(testDate1);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 1930);
        calendar.setTime(testDate2);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 2023);
        calendar.setTime(testDate3);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 2200);

        testDate1 = CommonUtil.nullDate();
        testDate2 = CommonUtil.nullDate();
        testDate3 = CommonUtil.nullDate();
        try {
            testDate1 = localUtil.ctotex("29-01-01", 0);
            testDate2 = localUtil.ctotex("30-01-01T00", 0);
            testDate3 = localUtil.ctotex("31-12-31T00:00", 0);
        } catch (Exception e) {
        }

        calendar = GregorianCalendar.getInstance();
        calendar.setTime(testDate1);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 2029);
        calendar.setTime(testDate2);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 1930);
        calendar.setTime(testDate3);
        Assert.assertTrue(calendar.get(Calendar.YEAR) == 1931);
    }

    @Test
    public void testValidTimezone() {
        Connect.init();

        String dateTime = "2023-03-22 15:00:00"; // input DateTime
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		TimeZone timezone = TimeZone.getTimeZone("America/Montevideo");

        try {
            Date mvdDate = dateFormat.parse(dateTime); // convert String to Date
			Date utcDate = CommonUtil.DateTimeToUTC(mvdDate, timezone);

            long diff = utcDate.getTime() - mvdDate.getTime();
            long expectedDiff = 10800000;
            Assert.assertEquals("Timezone offset invalid", expectedDiff, diff);
        } catch (Exception e) {
			Assert.fail();
        }
    }

    /**
     * DateTimeToUTC must not fail if the Timezone does not exists.
     */
    @Test
    public void testInvalidTimezone() {
        Connect.init();

        String dateTime = "2023-03-22 15:00:00"; // input DateTime
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        TimeZone timezone = TimeZone.getTimeZone("America/DoesnotExists");

        try {
            Date mvdDate = dateFormat.parse(dateTime); // convert String to Date
            Date utcDate = CommonUtil.DateTimeToUTC(mvdDate, timezone);

            long diff = utcDate.getTime() - mvdDate.getTime();
            long expectedDiff = 0;
            Assert.assertEquals("Timezone offset invalid", expectedDiff, diff);
        } catch (Exception e) {
            Assert.fail();
        }
    }
}