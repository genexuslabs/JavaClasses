package com.genexus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.Vector;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.internet.HttpContext;
import com.genexus.internet.StringCollection;
import com.genexus.platform.INativeFunctions;
import com.genexus.platform.NativeFunctions;
import com.genexus.util.*;

import json.org.json.JSONObject;

public final class GXutil
{
	public static boolean Confirmed = false;

	public static void writeLogln( String message)
	{
		CommonUtil.writeLogln(message);
	}
	public static void writeTLogln( String message)
	{
		CommonUtil.writeTLogln(message);
	}

	public static void writeLogRaw( String message, Object obj)
	{
		CommonUtil.writeLogRaw(message, obj);
	}

	public static void writeLog( String message)
	{
		CommonUtil.writeLog(message);
	}

	public static String accessKey(String OCaption)
	{
		return CommonUtil.accessKey(OCaption);
	}

	public static String accessKeyCaption(String OCaption)
	{
		return CommonUtil.accessKeyCaption(OCaption);
	}

	public static Calendar getCalendar()
	{
		return CommonUtil.getCalendar();
	}

	public static String newLine()
	{
		return CommonUtil.newLine();
	}

	public static String getHost(String hostAndPort)
	{
		return CommonUtil.getHost(hostAndPort);
	}

	public static int getPort(String hostAndPort)
	{
		return getPort(hostAndPort, 0);
	}

	public static int getPort(String hostAndPort, int def)
	{
		return CommonUtil.getPort(hostAndPort, def);
	}


	public static String link(String url, int newWindow)
	{
		PrivateUtilities.displayURL(url);
		return "";
	}

	public static byte deleteFile(String fileName)
	{
		return CommonUtil.deleteFile(fileName);
	}

	public static double mod(double value, double div)
	{
		return CommonUtil.mod(value, div);
	}

	public static double random()
	{
		return CommonUtil.random();
	}


	public static int rseed(double[] seed)
	{
		return CommonUtil.rseed(seed);
	}

	public static int rseed(long seed)
	{
		return CommonUtil.rseed(seed);
	}

	public static byte fileExists(String fileName)
	{
		if (!fileName.startsWith("http")){
			File file = new File(fileName);
			return (file.isFile() && file.exists())? (byte)1:0;
		}
		else
		{
			return PrivateUtilities.remoteFileExists(fileName);
		}
	}
	
	public static double rand()
	{
		return CommonUtil.rand();
	}

	public static int aleat()
	{
		return	CommonUtil.aleat();
	}

	public static String format(long value, String picture)
	{
		return CommonUtil.format(value, picture);
	}
	public static String format(double value, String picture)
	{
		return CommonUtil.format(value, picture);
	}
	public static String format(String value, String picture)
	{
		return value;
	}
	public static String format(java.util.Date value, String picture)
	{
		return value.toString();
	}

    public static String formatDateTimeParm(Date date)
    {
        return CommonUtil.formatDateTimeParm(date);
    }

    public static String formatDateParm(Date date)
    {
        return CommonUtil.formatDateParm(date);
    }
	
	public static String delete(String text,char del)
	{
		return (CommonUtil.trimSpaces(text.replace(del,' ')));
	}

	public static boolean isTime(String text)
	{
		return CommonUtil.isTime(text);
	}


	public static String alltrim(String text)
	{
		return CommonUtil.alltrim(text);
	}

	public static String rtrim(String text)
	{
        return CommonUtil.rtrim(text);
	}
	public static String rtrim(String[] text)
	{
		return CommonUtil.rtrim(text[0]);
	}
	public static boolean endsWith(String s1, String s2)
	{
		return CommonUtil.endsWith(s1, s2);
	}
	public static boolean startsWith(String s1, String s2)
	{
		return CommonUtil.startsWith(s1, s2);
	}
	public static boolean contains(String s1, String s2)
	{
		return CommonUtil.contains(s1, s2);
	}
	public static String charAt(String s1, int idx)
	{
		return CommonUtil.charAt(s1, idx);
	}
	public static String substring (String text, int start, int end)
	{
		return CommonUtil.substring(text, start, end);
	}

	public static String substringByte(String text, int lenInBytes, String encoding)
	{
		return CommonUtil.substringByte(text, lenInBytes, encoding);
	}

	public static Date ymdhmsToT_noYL(String yyyymmddhhmmss)
	{
		return CommonUtil.ymdhmsToT_noYL(yyyymmddhhmmss);
	}

	public static Date resetDate(Date date)
	{
		return CommonUtil.resetDate(date);
	}

	public static Date ymdhmsToT_noYL(int year, int month, int day , int hour , int minute , int second)
	{
		return  ymdhmsToT_noYL(year, month, day, hour, minute, second, 0);
	}
	public static Date ymdhmsToT_noYL(int year, int month, int day , int hour , int minute , int second, int millisecond)
	{
		return CommonUtil.ymdhmsToT_noYL(year, month, day, hour, minute, second, millisecond);
	}


	public static Date newNullDate()
	{
		return CommonUtil.newNullDate();
	}

	public static Date nullDate()
	{
		return CommonUtil.nullDate();
	}

	public static Date resetTime(Date dt)
	{
		return CommonUtil.resetTime(dt);
	}

	public static Date resetMillis(Date dt)
	{
		return CommonUtil.resetMillis(dt);
	}

	public static Date now()
	{
		return now(true,false);
	}

	public static Date now(boolean useClientTimeZone)
	{
		return now(useClientTimeZone,false);
	}
		
	public static Date nowms()
	{
		return now(true, true);
	}

	public static Date now(boolean useClientTimeZone, boolean millisecond)
	{
		return SpecificImplementation.GXutil.now(useClientTimeZone, millisecond);
	}
	
	public static short CurrentTimeOffset()
	{
		return getUTCOffset(now());	
	}

   	public static int datecmp( Date left , Date right)
   	{
   		return CommonUtil.datecmp(left, right);
   	}
   	
   	public static boolean dateCompare( Date left , Date right)
   	{
   		return CommonUtil.dateCompare(left, right);
   	}   	

   	public static int strcmp( String left ,
                             String right )
   	{
        return rtrim(left).compareTo(rtrim(right));
   	}

   	public static boolean strcmp2( String left ,
                             String right )
   	{
        return left.compareTo(right) == 0;
   	}

	public static int guidCompare(java.util.UUID guidA, java.util.UUID guidB, int mode)
	{
		return CommonUtil.guidCompare(guidA, guidB, mode);
	}

	public static Date setTime(Date dt, int time)
	{
		return resetTime(dt);
	}

	public static Date today()
	{
		return (resetTime(now()));
	}
	
	public static short getUTCOffset(Date value)
	{
		return CommonUtil.getUTCOffset(value);
	}
	
	public static Date DateTimetoCurrentTime(Date value, int Offset)
	{
		return CommonUtil.DateTimetoCurrentTime(value, Offset);
	}
	
    static public Date DateTimefromTimeZone(Date dt, String sTz)
    {
		return DateTimefromTimeZone( dt, sTz, ModelContext.getModelContext());
    }
	
    static public Date DateTimefromTimeZone(Date dt, String sTz, ModelContext context)
	{
		TimeZone fromTimeZone = TimeZone.getTimeZone( sTz);
        if (fromTimeZone.getID().equals(sTz))
			return ConvertDateTime( dt, fromTimeZone, context.getClientTimeZone());
		return dt;
    }        

	
    public static Date ConvertDateTime(Date dt, TimeZone FromTimezone, TimeZone ToTimezone)
    {
		return DateTimeFromUTC(DateTimeToUTC(dt, FromTimezone), ToTimezone);        
    }

	public static boolean emptyDate( Date value)
	{
		return CommonUtil.emptyDate(value);	
	}

	public static Date DateTimeToUTC(Date value)
	{
		if ( emptyDate( value))
			return value;
		if (ModelContext.getModelContext() == null)
		{
			TimeZone tz = (TimeZone)com.genexus.GXutil.threadTimeZone.get();
			if (tz != null)
				return DateTimeToUTC(value, tz);
			else
				return DateTimeToUTC(value, TimeZone.getDefault());
		}
		else
			return DateTimeToUTC(value, ModelContext.getModelContext().getClientTimeZone());
	}

	public static Date DateTimeToUTC(Date value, TimeZone tz)
	{
		return CommonUtil.DateTimeToUTC(value, tz);
	}

	public static Date DateTimeFromUTC(Date value)
	{
		return CommonUtil.DateTimeFromUTC( value, TimeZone.getDefault());
	}

	public static Date DateTimeFromUTC(Date value, TimeZone tz)
	{
		return CommonUtil.DateTimeFromUTC(value, tz);
	}

	public static Date DateToUTC(Date value)
	{
		return CommonUtil.resetTime(DateTimeToUTC(value));
	}
	
	public static IThreadLocal threadTimeZone = GXThreadLocal.newThreadLocal();

	public static void setThreadTimeZone(TimeZone tz)
	{
		threadTimeZone.set(tz);
	}


	public static String formatLong(long number)
	{
		return CommonUtil.formatLong(number);
	}

	public static String booltostr(boolean val){
		
		return CommonUtil.booltostr(val);
	}
	
	public static boolean strtobool(String val)
	{
		return CommonUtil.strtobool(val);	
	}	

	public static String str(long val, int digits, int decimals)
	{

		return CommonUtil.str(val, digits, decimals);

	}


	public static String str(java.math.BigDecimal value, int length, int decimals)
	{
		return CommonUtil.str(value, length, decimals, true);
	}

	public static String strNoRound(java.math.BigDecimal value, int length, int decimals)
	{
		return CommonUtil.str(value, length, decimals, false);
	}

			// Esto es que hizo str(_, 2, 1) o str(_, 3, 2), todas cosas
			// invalidas que implican que decimals = 0




		// Aca el numero tiene el valor redondeado y tiene los decimales correctos. Ahora
		// hay que empezar a achicarlo si no entra en el espacio indicado.

	public static String str(int[] value, int length, int decimals)
	{
		return str(value[0], length, decimals);
	}

	public static String str(double value, int length, int decimals)
	{
		return CommonUtil.str(value, length, decimals);
	}

	public static String strNoRound(double value, int length, int decimals)
	{
		return CommonUtil.strNoRound(value, length, decimals);
	}

	public static String strori(double val, int digits, int decimals)
	{
		if	(decimals < 0) decimals = 0;
		if	(digits < 0)   digits   = 0;

		StringBuffer b = new StringBuffer();
		boolean hasSign = (val < 0);

		if	(hasSign)
		{
			b.append('-');
			val = -val;
		}

		val = round(val, decimals);

		int intDigits = 0;
		if	(val < 1)
		{
			intDigits = val == 0?1:(int) log10(val) ;
		}
		else
		{
			intDigits = val == 0?1:(int) log10(val) + 1;
		}

		if	(intDigits + (hasSign?1:0) > digits)
			return CommonUtil.replicate("*", digits);

		if (intDigits <= 0)
		{
			int realDigits = -intDigits;
			realDigits = realDigits > decimals?decimals:realDigits;
			b.append("0." + CommonUtil.replicate("0", realDigits));
		}

		for (int i = intDigits -1; i >= -decimals ; i--)
		{
			double divi =  Math.pow(10, i);
			int cur   = (int) ( (val / divi) + 0.000001);

			//BigDecimal divi2 = new BigDecimal(Math.pow(10, i));
			//int cur = unexponentString(Double.toString(val)).divide(divi2, 0).intValue();


			val = round(val - (divi * cur), decimals);

			if	(b.length() < digits)
			{
				if	(i == 0 && decimals > 0)
				{
					if (digits > b.length() + 2)
					{
						b.append( (int) cur);
						b.append('.');
					}
					else
					{
						int cur_tmp = (int) ( (val / Math.pow(10, i-1)) + 0.000001);
						b.append( (int) cur + (cur_tmp >= 5?1:0) );
						break;
					}
				}
				else
				{
					b.append( (int) cur);
				}
			}
		}

		return padl(b.toString(), digits, " ");
	}

	public static double log10(double val)
	{
		return CommonUtil.log10(val);
	}

	public static int strSearch(String a, String b, int from)
	{
		return CommonUtil.strSearch(a, b, from);
	}

	public static int strSearch(String a, String b)
	{
		return CommonUtil.strSearch(a, b, 0);
	}

	public static int strSearchRev(String a, String b)
	{
		return CommonUtil.strSearchRev(a, b);
	}

	public static int strSearchRev(String a, String b, int from)
	{
		return CommonUtil.strSearchRev(a, b, from);
	}


	public static String strReplace(String s, String subString, String replacement)
	{
		return CommonUtil.strReplace(s, subString, replacement);
	}

	public static String padstr(int number, int length)
	{
		return CommonUtil.padl(CommonUtil.ltrim(str(number, length, 0)), length, "0");
	}

	public static String padr(String text, int size, String fill)
	{
		return CommonUtil.padr(text, size, fill);
	}

	public static String padl(String text, int size, String fill)
	{
	  return CommonUtil.padl(text, size, fill);
	}

	public static String right(String text, int size)
	{
		return CommonUtil.right(text, size);
	}

	public static String left(String text, int size)
	{
		return CommonUtil.left(text, size);
	}

	public static String replicate(char character, int size)
	{
		return CommonUtil.replicate(character, size);
	}

	public static String replicate(String character, int size, int a)
	{
		return CommonUtil.replicate(character, size, a);
	}

	public static final String replicate(String character, int size)
	{
		return CommonUtil.replicate(character, size);
    }

	public static String ltrim(String text)
	{
		return CommonUtil.ltrim(text);
	}
	public static String ltrimstr(long val, int digits, int decimals)
	{
		return CommonUtil.ltrimstr(val, digits, decimals);
	}

	public static String ltrimstr(java.math.BigDecimal value, int length, int decimals)
	{
		return CommonUtil.ltrimstr(value, length, decimals);
	}
	public static String ltrimstr(double value, int length, int decimals)
	{
		return CommonUtil.ltrimstr(value, length, decimals);
	}

	public static String time()
	{
		return CommonUtil.time();
	}

	public static Date addmth(Date date, int cnt)
	{
		return CommonUtil.addmth(date, cnt);
	}

	public static long lval(String text)
	{
		return CommonUtil.lval(text);
	}

	public static double val(String text)
	{
       return CommonUtil.val(text);
    }

	public static BigDecimal val(String text, String sDSep)
	{
		return CommonUtil.decimalVal(text, sDSep);
	}

	public static boolean notNumeric(String value)
	{
		return CommonUtil.notNumeric(value);
	}
	
	public static boolean boolval(String text)
	{
		return CommonUtil.boolval(text);
	}

	public static Date eomdate(Date date)
	{
		return CommonUtil.eomdate(date);
	}

	public static Date eom(Date date)
	{
		return CommonUtil.eom(date);
		
	}

	public static Date dtadd(Date date, int seconds)
	{
		return CommonUtil.dtadd(date, seconds);
	}

	public static Date dtaddms(Date date, double seconds)
	{
		return CommonUtil.dtaddms(date, seconds);
	}


	public static Date dadd(Date date, int cnt)
	{
		return CommonUtil.dadd(date, cnt);
	}

	public static long dtdiff(Date dateStart, Date dateEnd)
	{
		return CommonUtil.dtdiff(dateStart, dateEnd);
	}

	public static double dtdiffms(Date dateStart, Date dateEnd)
	{
		return CommonUtil.dtdiffms(dateStart, dateEnd);
	}

	public static int ddiff(Date dateStart, Date dateEnd)
	{
		return (int) round(dtdiff(dateStart, dateEnd) / 86400.0, 0);
	}

	public static String concat(String first, String second)
	{
		return CommonUtil.concat(first, second);
	}

	public static String concat(String first, String second, String separator)
	{
		return CommonUtil.concat(first, second, separator);
	}

	public static void msg(Date date)
	{
		PrivateUtilities.msg(Application.getClientLocalUtil().dtoc(date));
	}

	public static void msg(int sText)
	{
		PrivateUtilities.msg("" + sText);
	}

	public static void msg(String sText)
	{
		PrivateUtilities.msg(sText);
	}

	public static void msg(Object panel, String sText)
	{
		CommonUtil.msg(panel, sText);
	}

	public static void error(Object panel, String sText)
	{
		CommonUtil.error(panel, sText);
	}


	static public boolean like( String str, String ptrn)
	{
		return CommonUtil.like(str, ptrn);
	}
	
	static public boolean like( String str, String ptrn, char escape)
	{
		return CommonUtil.like(str, ptrn, escape);
	}		

	public static Date addyr(Date date, int yr)
	{
		return CommonUtil.addyr(date, yr);
	}

	public static int age(Date fn, Date today)
	{
		return CommonUtil.age(fn, today);
	}

	public static int age(Date dateStart)
	{
		return CommonUtil.age(dateStart);
	}

	public static int hour(Date date )
	{
		return CommonUtil.hour(date);
	}

	public static int minute(Date date )
	{
		return CommonUtil.minute(date);
	}

	public static int second(Date date )
	{
		return CommonUtil.second(date);
	}

	public static int millisecond(Date date )
	{
		return CommonUtil.millisecond(date);
	}

	public static int day(Date date)
	{
		return CommonUtil.day(date);
	}

	public static int month(Date date)
	{
		return CommonUtil.month(date);
	}

	public static int  year(Date date)
	{
		return CommonUtil.year(date);
	}

	public static String getYYYYMMDD(Date date)
	{
		return CommonUtil.getYYYYMMDD(date);
	}

	public static String getYYYYMMDDHHMMSS_nosep(Date date)
	{
		return CommonUtil.getYYYYMMDDHHMMSS_nosep(date);
	}

	public static String getYYYYMMDDHHMMSSmmm_nosep(Date date)
	{
		return CommonUtil.getYYYYMMDDHHMMSSmmm_nosep(date);
	}

	public static String getYYYYMMDDHHMMSS(Date date)
	{
		return CommonUtil.getYYYYMMDDHHMMSS(date);
	}

	public static String getYYYYMMDDHHMMSSmmm(Date date)
	{
		return CommonUtil.getYYYYMMDDHHMMSSmmm(date);
	}

	public static String getMMDDHHMMSS(Date date)
	{
		return CommonUtil.getMMDDHHMMSS(date);
	}

	public static int len(String text)
	{
		return CommonUtil.len(text);
	}

	public static int byteCount(String text, String encoding)
	{
		return CommonUtil.byteCount(text, encoding);
	}

	public static String space(int n)
	{
		return CommonUtil.space(n);
	}

	public static String trim(String text)
	{
		return CommonUtil.trim(text);
	}

	public static long Int(double num)
	{
		return CommonUtil.Int(num);
	}

	public static BigDecimal roundToEven(BigDecimal in, int decimals)
        {
          return CommonUtil.roundToEven(in, decimals);
	}

	public static BigDecimal roundDecimal(BigDecimal in, int decimals)
	{
		return CommonUtil.roundDecimal(in, decimals);
	}

	public static double round(double in, int decimals)
	{
		return CommonUtil.round(in, decimals);
	}

	public static BigDecimal truncDecimal(BigDecimal num , int decimals)
	{
		return CommonUtil.truncDecimal(num, decimals);
	}

	public static double trunc(double num , int decimals)
	{
		return truncDecimal(DecimalUtil.unexponentString(Double.toString(num)), decimals).doubleValue();
	}


	public static byte dow(Date date)
	{
		return CommonUtil.dow(date);
	}

	protected static boolean in(String text , char c)
	{
		return CommonUtil.in(text, c);
	}

	public static String getTimeFormat(String time)
	{
		return CommonUtil.getTimeFormat(time);
	}

	public static String chr(int asciiValue)
	{
		return CommonUtil.chr(asciiValue);
	}

	public static int asc(String value)
	{
		return CommonUtil.asc(value);
	}

	public static String wrkst()
	{
		return 	NativeFunctions.getInstance().getWorkstationName();
	}

	public static int dbmsVersion(ModelContext context, int handle, String dataSource)
	{
		String version = Application.getDBMSVersion(context, handle, dataSource);
		return Integer.valueOf(version.substring(0, version.indexOf(".")));
	}	
	
	public static boolean isSQLSERVER2005(ModelContext context, int handle, String dataSource)
	{
		return (dbmsVersion(context, handle, dataSource) >= 9);
	}
	
	public static String databaseName(ModelContext context, int handle, String dataSource)
	{
		return SpecificImplementation.GXutil.getDatabaseName(context, handle, dataSource);
	}	


	/** 
	* @deprecated use serverNow(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore);
	* */
	public static Date serverNow(ModelContext context, int handle, String dataSource)
	{
		return SpecificImplementation.GXutil.serverNow(context, handle, dataSource);
	}
		

	public static Date serverNow(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore)
	{
		return serverNow( context, handle, dataStore, false);
	}	

	public static Date serverNowMs(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore)
	{
		return serverNow( context, handle, dataStore, true);
	}	

	public static Date serverNow(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore, boolean millisecond)
	{
		return SpecificImplementation.GXutil.serverNow(context, handle, dataStore, millisecond);
	}

	/** 
	* @deprecated use serverTime(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore);
	* */ 
	public static String serverTime(ModelContext context, int handle, String dataSource)
	{
		SimpleDateFormat time_df = new java.text.SimpleDateFormat("HH:mm:ss");
		time_df.setTimeZone(CommonUtil.defaultTimeZone);
		return SpecificImplementation.GXutil.serverTime(time_df, context, handle, dataSource);
	}

	public static String serverTime(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore)
	{
		SimpleDateFormat time_df = new java.text.SimpleDateFormat("HH:mm:ss");
		time_df.setTimeZone(CommonUtil.defaultTimeZone);
		return SpecificImplementation.GXutil.serverTime(time_df, context, handle, dataStore);
	}


	/** 
	* @deprecated use serverDate(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore);
	* */
	public static Date serverDate(ModelContext context, int handle, String dataSource)
	{
		return SpecificImplementation.GXutil.serverDate(context, handle, dataSource);
	}

	public static Date serverDate(ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore)
	{
		return SpecificImplementation.GXutil.serverDate(context, handle, dataStore);
	}

	public static String userId(String key, ModelContext context, int handle, com.genexus.db.IDataStoreProvider dataStore)
	{
		if	(!key.equalsIgnoreCase("server"))
		{
			try
			{
				return upper(System.getProperty("user.name"));
			}
			catch (SecurityException e)
			{
			}
		}

		String user = dataStore.userId();

		if	(user == null)
		{
			System.err.println("Warning - userId('server') returned null");
			return "";
		}

		return upper(user);
	}

	/** 
	* @deprecated use userId(String key, int handle, com.genexus.db.IDataStoreProvider dataStore);
	* */
	public static String userId(String key, ModelContext context, int handle, String dataSource)
	{
		if	(!key.equalsIgnoreCase("server"))
		{
			try
			{
				return upper(System.getProperty("user.name"));
			}
			catch (SecurityException e)
			{
			}
		}

		String user = Application.getDBMSUser(context, handle, dataSource);

		if	(user == null)
		{
			System.err.println("Warning - userId('server') returned null");
			return "";
		}

		return upper(user);
	}

	public static byte openDocument(final String document)
	{
		try
		{
			NativeFunctions.getInstance().executeWithPermissions(
				new Runnable() {
					public void run()
					{
						int ret = NativeFunctions.getInstance().openDocument(document, 1);
						if (ret != 0)
							throw new RuntimeException();

					}
				}, INativeFunctions.ALL);
		}
		catch (RuntimeException e)
		{
			return 1;
		}

		return 0;
	}

	public static byte openPrintDocument(String document)
	{
		return (byte) NativeFunctions.getInstance().shellExecute(document, "print");
	}

    /** Hace un Shell modal. Ejecuta en la misma 'consola' que su parent
     * @param command Comando a ejecutar
     * @return true si el comando se pudo ejecutar
     */
    public static boolean shellModal(String command)
    {
        return NativeFunctions.getInstance().executeModal(command, true);
    }

	public static byte shell(String cmd, int modal)
	{
		if	(modal == 1)
			return shellModal(cmd)? 0 : (byte) 1;

		try
		{
			Runtime.getRuntime().exec(cmd);
		}
		catch (Exception e)
        {
        	System.err.println("e " + e);
			return 1;
		}

		return 0;
	}

	public static byte shell(String cmd)
	{
		return shell(cmd, 0);
	}

	public static String getClassName(String pgmName)
	{
		// Esta la usa el developerMenu, que saca el package del client.cfg
		String classPackage = Application.getClientContext().getClientPreferences().getPACKAGE();

		if	(!classPackage.equals(""))
			classPackage += ".";

		return classPackage + pgmName.replace('\\', '.').trim();
	}

	public static String getObjectName(String packageName, String objectName)
	{
		return CommonUtil.getObjectName(packageName, objectName);
	}


	public static String classNameNoPackage(Class cls)
        {
			return CommonUtil.classNameNoPackage(cls);
		}

	public static byte sleep(long time)
	{
		return CommonUtil.sleep(time);
	}

	public static void errorHandler(String text, Exception ex)
	{
		PrivateUtilities.errorHandler(text, ex);
	}

	public static int gxmlines(String text, int lineLength)
	{
		return CommonUtil.gxmlines(text, lineLength);
	}

	public static String gxgetmli(String text, int line, int lineLength)
	{
		return CommonUtil.gxgetmli(text, line, lineLength);
	}

  	public static String lower(String str)
  	{
  		return CommonUtil.lower(str);
  	}

  	public static String upper(String str)
  	{
  		return CommonUtil.upper(str);
  	}

  	public static String URLDecode(String s)
  	{
  		try
  		{
			return Codecs.decode(s, "UTF8");
		}
		catch(  UnsupportedEncodingException e)
		{
			return s;
		}
	}

	public static String URLEncode(String s)
	{
		// Ponemos este wraper porque en JDK1.4+ esta deprecated y no queremos que
		// al compilar el código generado por GX nos de deprecations
		try
		{
			return Codecs.encode(s, "UTF8");
		}
		catch(  UnsupportedEncodingException e)
		{
			return s;
		}
	}

	public static String getDefaultFontName(String language, String defValue)
	{
		return CommonUtil.getDefaultFontName(language, defValue);
	}
	
	public static String ianaEncodingName(String encoding)
	{
		return CommonUtil.ianaEncodingName(encoding);
	}
	
	//Transforma un encoding name en nombre canónico (establecido por iana, domain Encoding en genexus) al nombre esperado por jdk
	public static String normalizeEncodingName(String enc)
	{
		return CommonUtil.normalizeEncodingName(enc);
	}

	public static String normalizeEncodingName(String enc, String defaultEncoding)
	{
		return CommonUtil.normalizeEncodingName(enc, defaultEncoding);
	}

	
	//Transforma un encoding name en nombre canónico (establecido por iana, domain Encoding en genexus) al nombre esperado por jdk
	public static String normalizeSupportedEncodingName(String enc) throws Throwable
	{
		return CommonUtil.normalizeEncodingName(enc);
	}


	public static void refClasses(Class cls) { ; }
	public static String toValueList(String DBMS, GXSimpleCollection col, String prefix, String tail)
	{
		return CommonUtil.toValueList(DBMS, col, prefix, tail);
	}

	public static String toValueList(String DBMS, Object arr, String prefix, String tail)
	{
		return CommonUtil.toValueList(DBMS, arr, prefix, tail);
  	}

    private static String toValueList(String DBMS, Vector vec, String prefix, String tail)
    {
    	return CommonUtil.toValueList(DBMS, vec, prefix, tail);
    }

	public static String dateToString(Date date, String DBMS)
	{
		return CommonUtil.dateToString(date, DBMS);
	}

	public static boolean contains(BigDecimal []arr, BigDecimal obj)
	{
		return CommonUtil.contains(arr, obj);
	}

	public static boolean contains(byte [] arr, double item)
	{
		return CommonUtil.contains(arr, item);

	}

	public static boolean contains(char [] arr, double item)
	{
		return CommonUtil.contains(arr, item);
	}

	public static boolean contains(short [] arr, double item)
	{
		return CommonUtil.contains(arr, item);
	}

	public static boolean contains(int [] arr, double item)
	{
		return CommonUtil.contains(arr, item);
	}

	public static boolean contains(long [] arr, double item)
	{
		return CommonUtil.contains(arr, item);
	}

	public static boolean contains(float [] arr, double item)
	{
		return CommonUtil.contains(arr, item);
	}

	public static boolean contains(double [] arr, double item)
	{
		return CommonUtil.contains(arr, item);
	}

	public static boolean contains(String []arr, String item)
	{
		return CommonUtil.contains(arr, item);
	}

	public static boolean contains(Object []arr, Object item)
	{
		return CommonUtil.contains(arr, item);
	}
	public static String format(String value, String v1, String v2, String v3, String v4, String v5, String v6, String v7, String v8, String v9)
	{
		return CommonUtil.format(value, v1, v2, v3, v4, v5, v6, v7, v8, v9);
	}

	public static String getFileName( String sFullFileName)
	{
		return CommonUtil.getFileName(sFullFileName);
	}

	public static String getFileType( String sFullFileName)
	{
		return CommonUtil.getFileType(sFullFileName);
	}
	
	public static String getRelativeURL(String path)
	{
		return CommonUtil.getRelativeURL(path);
	}
	
	public static String getRelativeBlobFile(String path)
	{		
		return CommonUtil.getRelativeBlobFile(path);					
	}

	public static String getAbsoluteBlobFile(String path)
	{
		if (path.equals(""))
			return "";
		String blobPath = path;
		if (path.lastIndexOf('.') < 0)
			blobPath = blobPath + ".tmp";
		blobPath = blobPath.replace(com.genexus.Preferences.getDefaultPreferences().getProperty("CS_BLOB_PATH", "").trim(), "");
		return com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH() + blobPath;
	}
	public static final String FORMDATA_REFERENCE = "gxformdataref:";
	public static final String UPLOADPREFIX = "gxupload:";	
	public static final int UPLOAD_TIMEOUT = 10;	
	
	public static String cutUploadPrefix(String value)
	{
		String uploadValue = value.replace(UPLOADPREFIX, "");
		uploadValue = SpecificImplementation.GXutil.getUploadValue(value, uploadValue);
	
		//hack para salvar el caso de gxooflineeventreplicator que llegan los path de los blobs sin \ porque fueron sacadas por el FromJsonString
		String blobPath = com.genexus.Preferences.getDefaultPreferences().getProperty("CS_BLOB_PATH", "");
		if(uploadValue.indexOf(':') == 1 && uploadValue.indexOf(blobPath) != -1 )
		{
			uploadValue = uploadValue.substring(uploadValue.indexOf(blobPath) + blobPath.length());
			uploadValue = blobPath + "\\" + uploadValue;
			if (value.indexOf("WEB-INF") != -1)
			{
				uploadValue = "WEB-INF" + "\\" + uploadValue; 
			}			 
		}
		return uploadValue;
	}
	
	public static boolean isUploadPrefix(String value)
	{
		return CommonUtil.isUploadPrefix(value);
	}
	
	public static String dateToCharREST(Date value)
	{
		return CommonUtil.dateToCharREST(value);
	}
	
	public static String timeToCharREST(Date value)
	{
		String nullFormat = "0000-00-00T00:00:00";
		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (CommonUtil.millisecond(value)>0)
			dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return timeToCharREST(value, nullFormat, dateFormat);
	}	

	public static String timeMsToCharREST(Date value)
	{
		String nullFormat = "0000-00-00T00:00:00.000";
		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return timeToCharREST(value, nullFormat, dateFormat);
	}	

	public static String timeToCharREST(Date value, String emptyString, SimpleDateFormat dateFormat)
	{
		if ( nullDate().equals(value) )
		{
			return emptyString;
		}
		else
		{
 			boolean isRest = ModelContext.getModelContext() == null || ((HttpContext)ModelContext.getModelContext().getHttpContext()).isRestService();
			isRest = CommonUtil.shouldConvertDateTime(value, isRest);
			Date valueAux = Application.getClientPreferences().useTimezoneFix() ? (isRest ? DateTimeToUTC(value): value ): value;
			return dateFormat.format(valueAux).replace(" ", "T");		
		}
	}

	public static Date charToDateREST(String value)
	{		
		return CommonUtil.charToDateREST(value);
	}

	public static Date charToTimeREST(String value)
	{
		try
		{
			if (value == null || value.equals("0000-00-00T00:00:00") || value.equals("0000-00-00T00:00:00.000"))
			{
				return nullDate();
			}
			else
			{
				if (value.indexOf('/') >= 0)
				{
					return Application.getClientLocalUtil().ctot(value);
				}
				else
				{
					String valuetmp = value.replace("T", " ");
					// has milliseconds ?
					int milIndex = valuetmp.length();
					SimpleDateFormat dateFormat = null;				
					if (( milIndex == 23) && (valuetmp.substring(milIndex - 4, milIndex - 3).equals(".")))
					{
						dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					}
					else
					{
						dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					}
										
					Date valueAux = dateFormat.parse(valuetmp);
					if (Application.getClientPreferences().useTimezoneFix())
					{
						boolean isRest = !ModelContext.getModelContext().isTimezoneSet() || ((HttpContext)ModelContext.getModelContext().getHttpContext()).isRestService();
						isRest = CommonUtil.shouldConvertDateTime(valueAux, isRest);
						if (isRest)
						{
							if (ModelContext.getModelContext().isTimezoneSet())
								valueAux = CommonUtil.ConvertDateTime( valueAux, TimeZone.getTimeZone("GMT"), ModelContext.getModelContext().getClientTimeZone());
						}
					}
					return valueAux;
				}
			}
		}
		catch (ParseException ex)
		{
			return nullDate();
		}		
	}

	

	public static Object testNumericType(Object obj, int type)
	{
		return CommonUtil.testNumericType(obj, type);
	}

	public static BigDecimal calculate(String expression, String vars, byte [] err, String [] errMsg, ModelContext context, int handle)
	{
		return ExpressionEvaluator.eval(context, handle, expression, err, errMsg, vars);
	}

				public static boolean compare(Comparable operand1, String op, Comparable operand2)
				{
						return CommonUtil.compare(operand1, op, operand2);
				}

        public static Object convertObjectTo(Object obj, Class toClass) throws Exception
        {
            return CommonUtil.convertObjectTo( obj, toClass);
        }
	public static Object convertObjectTo(Object obj, Class toClass, boolean fail) throws Exception
        {

            //Parameters in URL are always in Invariant Format


	    	return CommonUtil.convertObjectTo(obj, toClass, fail);
	}
	
	
        public static Class mapTypeToClass(int type)
        {
           return CommonUtil.mapTypeToClass(type);
        }
        public static Object convertObjectTo(Object obj, int type)
        {
          return CommonUtil.convertObjectTo(obj, type);
        }

	public static boolean toBoolean(int value)
	{
		return CommonUtil.toBoolean(value);
	}
	
	public static String blobToBase64(String filePath)
	{
		com.genexus.util.GXFile gxFile = new com.genexus.util.GXFile(filePath);
		return gxFile.toBase64();
		
	}
	
	public static byte[] blobToBytes(String filePath)
	{
		com.genexus.util.GXFile gxFile = new com.genexus.util.GXFile(filePath);
		return gxFile.toBytes();
		
	}	
	
	public static String replaceLast(String string, String toReplace, String replacement) {
		return CommonUtil.replaceLast(string, toReplace, replacement);
	}

	public static String blobFromBase64(String base64String)
	{
		if (base64String.equals(""))
		{
			return "";
		}
		String filePath = Preferences.getDefaultPreferences().getBLOB_PATH() + com.genexus.PrivateUtilities.getTempFileName("tmp");
		com.genexus.util.GXFile gxFile = new com.genexus.util.GXFile(filePath);
		gxFile.fromBase64(base64String);
		com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(filePath);
		return filePath;
	}
	
	public static String blobFromBytes(byte[] bytesString)
	{
		String filePath = Preferences.getDefaultPreferences().getBLOB_PATH() + com.genexus.PrivateUtilities.getTempFileName("tmp");
		com.genexus.util.GXFile gxFile = new com.genexus.util.GXFile(filePath);
		gxFile.fromBytes(bytesString);
		com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(filePath);
		return filePath;
	}	
	
	public static java.util.UUID strToGuid(String value)
	{
		return CommonUtil.strToGuid(value);
	}
	


	public static int setLanguage(String language, ModelContext context,
                                  com.genexus.db.UserInformation ui) {
		HttpContext httpContext = (HttpContext) context.getHttpContext();
        int res = httpContext.setLanguage(language);
        ui.setLocalUtil(httpContext.getLanguageProperty(
                "decimal_point").charAt(0),
                        context.getHttpContext().getLanguageProperty("date_fmt"),
                        context.getHttpContext().getLanguageProperty("time_fmt"),
                        context.getClientPreferences().getYEAR_LIMIT(),
                        context.getHttpContext().getLanguageProperty("code"));
        return res;
	}

	
	public static GxJsonArray stringCollectionsToJsonObj(StringCollection gxdynajaxctrlcodr, StringCollection gxdynajaxctrldescr)
	{
		return new GxJsonArray(stringCollectionsToJson(gxdynajaxctrlcodr, gxdynajaxctrldescr));
	}
	
	public static String stringCollectionsToJson( StringCollection gxdynajaxctrlcodr, StringCollection gxdynajaxctrldescr)
	{
		if (gxdynajaxctrlcodr.getCount() == 0 && gxdynajaxctrldescr.getCount() > 0)
			return stringCollectionToJson(gxdynajaxctrldescr);		
		
		String result = "[";
		int index = 1 ;
		while ( index <= gxdynajaxctrlcodr.getCount() )
		{
			result = result + "[" + JSONObject.quote(gxdynajaxctrlcodr.item(index)) + "," + JSONObject.quote(gxdynajaxctrldescr.item(index)) + "]";
			if (index < gxdynajaxctrlcodr.getCount())
			{
				result = result + ",";
			}			
			index = index + 1;
		}
		result = result + "]";
		return result;
	}
	
	public static String stringCollectionToJson(StringCollection gxdynajaxctrldescr)
	{
		String result = "[";
		int index = 1 ;
		while ( index <= gxdynajaxctrldescr.getCount() )
		{
			result = result + "" + JSONObject.quote(gxdynajaxctrldescr.item(index)) + "";
			if (index < gxdynajaxctrldescr.getCount())
			{
				result = result + ",";
			}
			index = index + 1;
		}
		result = result + "]";
		return result;
	}

	public static boolean checkSignature( String sgn, String txt)
	{
		return CommonUtil.checkSignature(sgn, txt);
	}


	public static String getMD5Hash( String s)
	{
		return CommonUtil.getMD5Hash(s);
	}
	
	public static String getHash( String s)
	{
		return CommonUtil.getHash(s, "SHA1");
	}
	
	public static String getHash( String s, String hashAlgorithm)
	{
		return CommonUtil.getHash(s, hashAlgorithm);
	}

	public static boolean isAbsoluteURL(String url)
	{
		return CommonUtil.isAbsoluteURL(url);
	}

	public static void ErrorToMessages(String errorId, String errorDescription, GXBaseCollection<SdtMessages_Message> messages)
	{
		if (messages != null)
		{
			StructSdtMessages_Message struct = new StructSdtMessages_Message();
			struct.setId(errorId);
			struct.setDescription(errorDescription);
			struct.setType((byte)1); //error
			SdtMessages_Message msg = new SdtMessages_Message(struct);
			messages.add(msg);
		}
	}
	
	public static String encodeJSON(String in)
	{
		return CommonUtil.encodeJSON(in);
	}

	public static String removeDiacritics(String s)
	{
		return CommonUtil.removeDiacritics(s);
	}

	public static String CssPrettify(String uglyCSS)
	{
		return CommonUtil.CssPrettify(uglyCSS);
	}

	public static int getColor(int r, int g, int b)
	{
		return CommonUtil.getColor(r, g, b);
	}

	public static <T> T[] concatArrays(T[] first, T[] second) {
		return CommonUtil.concatArrays(first, second);
	}

	
	public static String pagingSelect(String select)
	{
		return CommonUtil.pagingSelect(select);
	}

	public static String getEncryptedSignature( String value, String key)
	{
		return Encryption.encrypt64(CommonUtil.getHash( com.genexus.security.web.WebSecurityHelper.StripInvalidChars(value), com.genexus.cryptography.Constants.SECURITY_HASH_ALGORITHM), key);
	}
	public static boolean checkEncryptedSignature( String value, String hash, String key)
	{
		return CommonUtil.getHash( com.genexus.security.web.WebSecurityHelper.StripInvalidChars(value), com.genexus.cryptography.Constants.SECURITY_HASH_ALGORITHM).equals(Encryption.decrypt64(hash, key));
	}

	public static String buildWSDLFromHttpClient(com.genexus.internet.HttpClient GXSoapHTTPClient, String wsdlURL)
	{
		if (GXSoapHTTPClient.getWSDLURL() != null && !GXSoapHTTPClient.getWSDLURL().isEmpty())
		{
			return GXSoapHTTPClient.getWSDLURL();
		}
		else
		{
			return wsdlURL;
		}
	}

	public static String buildURLFromHttpClient(com.genexus.internet.HttpClient GXSoapHTTPClient, String serviceName, javax.xml.ws.BindingProvider bProvider)
	{
		if (!GXSoapHTTPClient.getProxyServerHost().equals(""))
		{
			bProvider.getRequestContext().put("https.proxyHost", GXSoapHTTPClient.getProxyServerHost());
		}

		if (GXSoapHTTPClient.getProxyServerPort() != 80)
		{
			bProvider.getRequestContext().put("https.proxyPort", GXSoapHTTPClient.getProxyServerPort());
		}

		String scheme = "http";
		if (GXSoapHTTPClient.getSecure() == 1)
		{
			scheme = "https";
		}
		java.net.URI url = null;
		try
		{
			url = new java.net.URI(scheme, null, GXSoapHTTPClient.getHost(), GXSoapHTTPClient.getPort(), GXSoapHTTPClient.getBaseURL() + serviceName, null, null);
		}
		catch(java.net.URISyntaxException e)
		{
			return "";
		}
		return url.toString();
	}

}
