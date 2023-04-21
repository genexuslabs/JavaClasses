package com.genexus.specific.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import json.org.json.JSONException;
import json.org.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.genexus.Application;
import com.genexus.PrivateUtilities;
import com.genexus.common.interfaces.IExtensionGXutil;
import com.genexus.db.IDataStoreProvider;
import com.genexus.util.CacheAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GXutil implements IExtensionGXutil {
	private static Logger logger = LogManager.getLogger(GXutil.class);

	private ZonedDateTime getZonedDateTime(Date value, TimeZone tz){
		ZonedDateTime zdt;
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(value);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			int nanoOfSecond = calendar.get(Calendar.MILLISECOND);

			zdt = ZonedDateTime.of(LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond), tz.toZoneId());
		}
		catch(Exception e) {
			zdt = ZonedDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
			logger.error(String.format("Failed to find TimeZone: %s. Using default Timezone", tz.getID()), e);
		}
		return zdt;
	}

	@Override
	public Date DateTimeToUTC(Date value, TimeZone tz) {
		if (tz.getID() == "GMT")
			return value;

		ZonedDateTime zdt = getZonedDateTime(value, tz);
		return Timestamp.valueOf(zdt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
	}

	@Override
	public Date DateTimeFromUTC(Date value, TimeZone tz) {
		if (tz.getID() == "GMT")
			return value;

		if (com.genexus.CommonUtil.emptyDate(value))
			return value;

		ZonedDateTime zdtUTC = getZonedDateTime(value, TimeZone.getTimeZone("UTC"));
		return Timestamp.valueOf(zdtUTC.withZoneSameInstant(ZoneId.of(tz.getID())).toLocalDateTime());
	}

	@Override
	public Calendar getCalendar()
	{
		return CommonUtil.getCalendar();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Date now(boolean useClientTimeZone, boolean millisecond) {
		ModelContext context = ModelContext.getModelContext();
		Calendar cal = getCalendar();
		synchronized (cal) {
			cal.setTime(new Date());
			if (!millisecond)
				cal.set(Calendar.MILLISECOND, 0);
			if (Application.getClientPreferences().useTimezoneFix() && useClientTimeZone)
				return context.toContextTz(cal.getTime());
			return cal.getTime();
		}
	}

	@Override
	public String serverTime(SimpleDateFormat time_df, Object context, int handle, String dataSource) {
		return time_df.format(Application.getServerDateTime((ModelContext) context, handle, dataSource));
	}

	@Override
	public String serverTime(SimpleDateFormat time_df, Object context, int handle, Object dataStore) {
		return time_df.format(((IDataStoreProvider) dataStore).serverNow());
	}

	@Override
	public String removeDiacritics(String s) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern
				.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

		try {
			int i;
			Class<?> normalizerClass = Class.forName("java.text.Normalizer");
			Class<?> normalizerFormClass = null;
			Class<?>[] nestedClasses = normalizerClass.getDeclaredClasses();
			for (i = 0; i < nestedClasses.length; i++) {
				Class<?> nestedClass = nestedClasses[i];
				if (nestedClass.getName().equals("java.text.Normalizer$Form")) {
					normalizerFormClass = nestedClass;
				}
			}
			assert normalizerFormClass.isEnum();
			Method methodNormalize = normalizerClass.getDeclaredMethod("normalize", CharSequence.class,
					normalizerFormClass);
			Object nfcNormalization = null;
			Object[] constants = normalizerFormClass.getEnumConstants();
			for (i = 0; i < constants.length; i++) {
				Object constant = constants[i];
				if (constant.toString().equals("NFD")) {
					nfcNormalization = constant;
				}
			}
			s = (String) methodNormalize.invoke(null, s, nfcNormalization);

			return pattern.matcher(s).replaceAll("");
		} catch (Exception e) {
			return s;
		}

	}

	@Override
	public boolean shouldConvertDateTime(Date value, boolean isRest) {
		return isRest;
	}

	@Override
	public String getRelativeURL(String path) {
		if (com.genexus.CommonUtil.isAbsoluteURL(path)) {
			return path;
		} else {
			String baseName = org.apache.commons.io.FilenameUtils.getBaseName(path);
			String relativeUrl = com.genexus.CommonUtil.getRelativeBlobFile(path);
			return com.genexus.CommonUtil.replaceLast(relativeUrl, baseName, PrivateUtilities.encodeFileName(baseName));
		}
	}

	@Override
	public Date serverDate(Object context, int handle, String dataSource) {
		return com.genexus.CommonUtil
				.resetTime(Application.getServerDateTime((ModelContext) context, handle, dataSource));
	}

	@Override
	public Date serverDate(Object context, int handle, Object dataStore) {
		if (dataStore == null)
			return serverDate(context, handle, "DEFAULT");
		else
			return com.genexus.CommonUtil.resetTime(((IDataStoreProvider) dataStore).serverNow());
	}

	@Override
	public void checkEncoding(String encoding) throws Throwable {
		if (!java.nio.charset.Charset.isSupported(encoding)) {
			throw new Throwable("Invalid encoding");
		}
	}

	@Override
	public String getUploadValue(String value) {
		String uploadValue = getUploadValue(value, "path");
		if (uploadValue == null || uploadValue.isEmpty())
			return value;

		return uploadValue;
	}

	@Override
	public String getUploadExtensionValue(String value) {
		return getUploadValue(value, "fileExtension");
	}

	@Override
	public String getUploadNameValue(String value) {
		return getUploadValue(value, "fileName");
	}

	public String getUploadValue(String value, String fieldName) {
		String uploadId = value.replace(CommonUtil.UPLOADPREFIX, "");
		if (com.genexus.CommonUtil.isUploadPrefix(value) && CacheAPI.files().contains(uploadId)) {
			String uploadValueJson = CacheAPI.files().get(uploadId);
			try {
				JSONObject json = new JSONObject(uploadValueJson);
				value = (String)json.get(fieldName);
			}
			catch (JSONException e) {
				org.apache.logging.log4j.LogManager.getLogger(GXutil.class).debug("Error Getting Upload Value", e);
			}
		}
		else
			value = "";

		return value;
	}

	@Override
	public Date serverNow(Object context, int handle, String dataSource) {
		Date d = Application.getServerDateTime((ModelContext) context, handle, dataSource);
		if (Application.getClientPreferences().useTimezoneFix())
			d = ((ModelContext) context).toContextTz(d);
		return com.genexus.CommonUtil.resetMillis(d);
	}

	@Override
	public Date serverNow(Object context, int handle, Object dataStore, boolean millisecond) {
		Date d = ((IDataStoreProvider) dataStore).serverNow();
		if (Application.getClientPreferences().useTimezoneFix())
			d = ((ModelContext) context).toContextTz(d);
		if (millisecond)
			return d;
		else
			return com.genexus.CommonUtil.resetMillis(d);
	}

	@Override
	public String getDatabaseName(Object context, int handle, String dataSource) {
		return Application.getDatabaseName((ModelContext) context, handle, dataSource);
	}

	@Override
	public Object convertObjectTo(Class<?> toClass, String objStr) {

		try {
			if (com.genexus.internet.IGxJSONSerializable.class.isAssignableFrom(toClass)) {
				com.genexus.internet.IGxJSONSerializable parmObj = null;
				if (com.genexus.xml.GXXMLSerializable.class.isAssignableFrom(toClass)) {
					parmObj = (com.genexus.xml.GXXMLSerializable) toClass
							.getConstructor(new Class[] { ModelContext.class })
							.newInstance(new Object[] { ModelContext.getModelContext(Application.gxCfg) });
				} else {
					parmObj = (com.genexus.internet.IGxJSONSerializable) toClass.getConstructor(new Class[] {})
							.newInstance(new Object[] {});
				}
				parmObj.fromJSonString(objStr);
				return parmObj;
			}
		} catch (Exception ex) {
			return null;
		}
		return null;
	}

	@Override
	public String URLDecode(String url) {
		return com.genexus.GXutil.URLDecode(url);
	}

	@Override
	public Date charToTimeREST(String string) {
		return com.genexus.GXutil.charToTimeREST(string);
	}

	@Override
	public String timeToCharREST(Date prop) {
		return com.genexus.GXutil.timeToCharREST(prop);
	}

	@Override
	public byte[] toByteArray(InputStream input) throws IOException {
		return IOUtils.toByteArray(input);
	}

	@Override
	public String readFileToString(File themesJsonFile, String normalizeEncodingName) throws IOException {
		return FileUtils.readFileToString(themesJsonFile, normalizeEncodingName);
	}

	@Override
	public String getClassName(String lowerCase) {
		return com.genexus.GXutil.getClassName(lowerCase);
	}

	@Override
	public Date DateTimefromTimeZone(Date d, String id, ModelContext modelContext) {
		return com.genexus.GXutil.DateTimefromTimeZone(d, id, (ModelContext) modelContext);
	}

	@Override
	public String getTempFileName(String extension) {
		return PrivateUtilities.getTempFileName(extension);
	}

}
