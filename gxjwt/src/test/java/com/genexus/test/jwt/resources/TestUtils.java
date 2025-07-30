package com.genexus.test.jwt.resources;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TestUtils {

	public static String generateGUID() {
		return UUID.randomUUID().toString();
	}

	public static String getCurrentDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		return dtf.format(now);
	}

	public static String currentPlusSeconds(long seconds) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime aux = now.plusSeconds(seconds);
		return dtf.format(aux);
	}

	public static String currentMinusSeconds(long seconds) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime aux = now.minusSeconds(seconds);
		return dtf.format(aux);
	}
}
