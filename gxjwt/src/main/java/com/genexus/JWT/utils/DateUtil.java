package com.genexus.JWT.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.genexus.commons.DateUtilObject;

/***** DEPRECATED OBJECT SINCE GeneXus 16 upgrade 11 ******/

public final class DateUtil extends DateUtilObject {

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	/**
	 * @deprecated DateUtil object is deprecated. Use GeneXus DateTime data type
	 *             instead
	 *             https://wiki.genexus.com/commwiki/servlet/wiki?7370,DateTime%20data%20type
	 */
	@Deprecated
	public String getCurrentDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

		return dtf.format(now);
	}

	/**
	 * @deprecated DateUtil object is deprecated. Use GeneXus DateTime data type
	 *             instead
	 *             https://wiki.genexus.com/commwiki/servlet/wiki?7370,DateTime%20data%20type
	 */
	@Deprecated
	public String currentPlusSeconds(long seconds) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime aux = now.plusSeconds(seconds);
		return dtf.format(aux);
	}

	/**
	 * @deprecated DateUtil object is deprecated. Use GeneXus DateTime data type
	 *             instead
	 *             https://wiki.genexus.com/commwiki/servlet/wiki?7370,DateTime%20data%20type
	 */
	@Deprecated
	public String currentMinusSeconds(long seconds) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime aux = now.minusSeconds(seconds);
		return dtf.format(aux);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

}
