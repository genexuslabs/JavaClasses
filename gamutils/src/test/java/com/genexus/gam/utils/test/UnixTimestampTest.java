package com.genexus.gam.utils.test;

import com.genexus.gam.GamUtilsEO;
import org.junit.Assert;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UnixTimestampTest {

	@Test
	public void testCreate() {
		Date one = createDate("2024/02/02 02:02:02"); //1706839322
		Date two = createDate("2023/03/03 03:03:03"); //1677812583
		Date three = createDate("2022/04/04 04:04:04"); //1649045044
		Date four = createDate("2020/02/02 02:22:22"); //1580610142
		Date five = createDate("2010/05/05 05:05:05"); //1273035905
		Date six = createDate("2000/05/05 05:05:05"); //957503105

		Date[] arrayDates = new Date[]{one, two, three, four, five, six};
		long[] arrayStamps = new long[]{1706839322L, 1677812583L, 1649045044L, 1580610142L, 1273035905L, 957503105L};

		for (int i = 0; i < arrayDates.length; i++) {
			Assert.assertEquals("testCreate", GamUtilsEO.createUnixTimestamp(arrayDates[i]), arrayStamps[i]);
		}

	}

	private static Date createDate(String date) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			return dateFormat.parse(date);
		} catch (Exception e) {

			e.printStackTrace();
			return null;
		}
	}
}
