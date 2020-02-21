package com.genexus.specific.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.genexus.ModelContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.genexus.Application;
import com.genexus.PrivateUtilities;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.IExtensionGXutil;
import com.genexus.util.GXTimeZone;

public class GXutil implements IExtensionGXutil {

	@Override
	public Date DateTimeToUTC(Date value, TimeZone tz) {
		Date ret = new Date(value.getTime() - tz.getRawOffset());
		
		if (tz.inDaylightTime(ret))
		{
			Date dstDate = new Date(ret.getTime() - tz.getDSTSavings());
			if (tz.inDaylightTime(dstDate))
			{
				ret = dstDate;
			}
		}
		return ret;
	}

	@Override
	public Date DateTimeFromUTC(Date value, TimeZone tz) {
		if ( com.genexus.CommonUtil.emptyDate( value))
			return value;
		Date ret = new Date(value.getTime() + tz.getRawOffset());
		
		if (tz.inDaylightTime(ret))
		{
			Date dstDate = new Date(ret.getTime() + tz.getDSTSavings());
			if (tz.inDaylightTime(dstDate))
			{
				ret = dstDate;
			}
		}
		return ret;	}

	@Override
	public Calendar getCalendar() {
		return new GregorianCalendar(GXTimeZone.getDefault(), com.genexus.CommonUtil.defaultLocale);
	}

	@Override
	public Date now(boolean useClientTimeZone, boolean millisecond) {
		Calendar cal = getCalendar();
		synchronized (cal)
		{
			cal.setTime(new Date());
			if (!millisecond)
				cal.set(Calendar.MILLISECOND, 0);
			//Convert to UTC if needed
			//if (com.artech.base.services.AndroidContext.ApplicationContext.getUseUtcConversion())
			//{
			//	// Convert From Local Time to UTC
			//	long offset = TimeZone.getDefault().getOffset(cal.getTime().getTime());
			//	cal.setTime(new Date( cal.getTime().getTime() - offset ) );
			//}
			return cal.getTime();
		}
	}

	@Override
	public String serverTime(SimpleDateFormat time_df, Object context, int handle, String dataSource) {
		return time_df.format(com.genexus.CommonUtil.now());
	}

	@Override
	public String serverTime(SimpleDateFormat time_df, Object context, int handle, Object dataStore) {
		return time_df.format(com.genexus.CommonUtil.now());
	}

	@Override
	public String removeDiacritics(String s) {
		return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	@Override
	public boolean shouldConvertDateTime(Date value, boolean isRest) {
		if (isRest)
		{
			Calendar cal = getCalendar();
			cal.setTime(value);
			cal.set(Calendar.MILLISECOND, 0);
		
			// if null/empty date , is only time, not convert
			//if (cal.get(Calendar.DAY_OF_MONTH)==1 && cal.get(Calendar.MONTH)==0  && cal.get(Calendar.YEAR)==1 )
			if (cal.get(Calendar.YEAR)==1 || cal.get(Calendar.YEAR)==0)
				isRest = false;
		}
		return isRest;
	}

	@Override
	public String getRelativeURL(String path) {
		String servletEnginePath = com.genexus.ApplicationContext.getInstance().getServletEngineDefaultPath();
		if (servletEnginePath!=null && servletEnginePath.length()>0)
			return com.genexus.CommonUtil.getRelativeBlobFile(path);
		else
			return path;
	}

	@Override
	public Date serverDate(Object context, int handle, String dataSource) {
		return com.genexus.CommonUtil.now();
	}

	@Override
	public Date serverDate(Object context, int handle, Object dataStore) {
		return com.genexus.CommonUtil.now();
	}

	@Override
	public void checkEncoding(String encoding) throws Throwable {
	}

	@Override
	public String getUploadValue(String value, String uploadValue) {
		return uploadValue;
	}

	@Override
	public Date serverNow(Object context, int handle, String dataSource) {
		return com.genexus.CommonUtil.now();
	}

	@Override
	public Date serverNow(Object context, int handle, Object dataStore, boolean millisecond) {
		return com.genexus.CommonUtil.now();
	}

	@Override
	public String getDatabaseName(Object context, int handle, String dataSource) {
		return "";
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

	public Date DateTimefromTimeZone(Date d, String id, ModelContext modelContext) {
		return com.genexus.GXutil.DateTimefromTimeZone(d, id, (ModelContext) modelContext);
	}

	public String getTempFileName(String subtype) {
		return PrivateUtilities.getTempFileName(subtype);
	}

}
