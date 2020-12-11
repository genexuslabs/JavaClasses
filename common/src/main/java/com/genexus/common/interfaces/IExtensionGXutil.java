package com.genexus.common.interfaces;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;


public interface IExtensionGXutil {

	Date DateTimeToUTC(Date value, TimeZone tz);

	Date DateTimeFromUTC(Date value, TimeZone tz);

	Calendar getCalendar();

	Date now(boolean useClientTimeZone, boolean millisecond);

	String serverTime(SimpleDateFormat time_df, Object context, int handle, String dataSource);

	String serverTime(SimpleDateFormat time_df, Object context, int handle, Object dataStore);

	String removeDiacritics(String s);

	boolean shouldConvertDateTime(Date value, boolean isRest);

	String getRelativeURL(String path);

	Date serverDate(Object context, int handle, String dataSource);

	Date serverDate(Object context, int handle, Object dataStore);

	void checkEncoding(String encoding) throws Throwable;

	String getUploadValue(String value);

	String getUploadNameValue(String value);

	String getUploadExtensionValue(String value);

	Date serverNow(Object context, int handle, String dataSource);

	Date serverNow(Object context, int handle, Object dataStore, boolean millisecond);

	String getDatabaseName(Object context, int handle, String dataSource);

	Object convertObjectTo(Class<?> toClass, String objStr);

	String URLDecode(String url);

	Date charToTimeREST(String string);

	String timeToCharREST(Date prop);

	byte[] toByteArray(InputStream input) throws IOException; 

	String readFileToString(File themesJsonFile, String normalizeEncodingName) throws IOException;

	String getClassName(String lowerCase);

	Date DateTimefromTimeZone(Date d, String id, ModelContext modelContext);

	String getTempFileName(String subtype); 

}
