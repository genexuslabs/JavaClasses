package com.genexus;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.*;

import json.org.json.JSONObject;

import java.math.BigDecimal;
import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.*;
import java.security.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import com.genexus.common.interfaces.SpecificImplementation;

public final class CommonUtil
{
	public static boolean Confirmed = false;

	static final char ASTER = '%';
	static final char QMARK = '_';
	static final int SECONDS_IN_DAY = 86400;

	public static TimeZone 	defaultTimeZone;
	public static Locale	defaultLocale;

	static TimeZone 	originalTimeZone;
	public static IThreadLocal threadCalendar;

	public static final String [][] ENCODING_JAVA_IANA;
	private static Random random;
	private static Date nullDate;

	public static final ILogger logger = LogManager.getLogger(CommonUtil.class);

	static
	{
		try
		{
			originalTimeZone = GXTimeZone.getDefaultOriginal();
			defaultTimeZone = GXTimeZone.getDefault();
			defaultLocale   = Locale.getDefault();

			threadCalendar = GXThreadLocal.newThreadLocal(new IThreadLocalInitializer()
			{
				public Object initialValue()
				{
					return new GregorianCalendar(CommonUtil.defaultTimeZone, CommonUtil.defaultLocale);
				}
			});
			random = new Random();
			nullDate = newNullDate();

			ENCODING_JAVA_IANA = new String [][]
				{
					{"Cp942C","x-IBM942C"},
					{"Cp943C","x-IBM943C"},
					{"Cp949C","x-IBM949C"},
					{"Cp850","IBM850"},
					{"DBCS_ASCII","DBCS_ASCII"},
					{"DBCS_EBCDIC","DBCS_EBCDIC"},
					{"Default","Default"},
					{"DoubleByte","DoubleByte"},
					{"EUC","EUC"},
					{"EUC_CN","GB2312"},
					{"EUC_JP","EUC-JP"},
					{"EUC_KR","EUC-KR"},
					{"EUC_TW","EUC-TW"},
					{"GBK","GBK"},
					{"GB18030","GB18030"},
					{"ISO2022","ISO-2022-CN"},
					{"ISO2022CN_CNS","ISO2022CN_CNS"},//?
					{"ISO2022CN_GB","ISO2022CN_GB"},//?
					{"ISO2022JP", "ISO-2022-JP"},
					{"ISO2022KR","ISO-2022-KR"},
					{"ISO8859_1","ISO-8859-1"},
					{"ISO8859_2","ISO-8859-2"},
					{"ISO8859_3","ISO-8859-3"},
					{"ISO8859_4","ISO-8859-4"},
					{"ISO8859_5","ISO-8859-5"},
					{"ISO8859_6","ISO-8859-6"},
					{"ISO8859_7","ISO-8859-7"},
					{"ISO8859_8","ISO-8859-8"},
					{"ISO8859_9","ISO-8859-9"},
					{"ISO8859_15_FDIS","ISO8859_15_FDIS"}, //?
					{"JIS0201","JIS0201"},//?
					{"JIS0208","JIS0208"},//?
					{"JIS0212","JIS0212"},//?
					{"Johab","Johab"},//?
					{"KOI8_R","KOI8-R"},
					{"MacArabic","MacArabic"},//?
					{"MacCentralEurope","MacCentralEurope"},//?
					{"MacCroatian","MacCroatian"},//?
					{"MacCyrillic","MacCyrillic"},//?
					{"MacDingbat","MacDingbat"},//?
					{"MacGreek","MacGreek"},//?
					{"MacHebrew","MacHebrew"},//?
					{"MacIceland","MacIceland"},//?
					{"MacRoman","MacRoman"},//?
					{"MacRomania","MacRomania"},//?
					{"MacSymbol","MacSymbol"},//?
					{"MacThai","MacThai"},//?
					{"MacTurkish","MacTurkish"},//?
					{"MacUkraine","MacUkraine"},//?
					{"MS874","windows-874"},
					{"MS932","windows-31j"},
					{"MS932DB","MS932DB"},//?
					{"MS936","MS936"},//?
					{"MS949","MS949"},//?
					{"MS950","MS950"},//?
					{"SingleByte","SingleByte"},//?
					{"SJIS","Shift_JIS"},
					{"TIS620","TIS-620"},
					{"Unicode","Unicode"},//?
					{"UnicodeBig","UnicodeBig"},//?
					{"UnicodeBigUnmarked","UnicodeBigUnmarked"},//?
					{"UnicodeLittle","UnicodeLittle"},//?
					{"UnicodeLittleUnmarked","UnicodeLittleUnmarked"},//?
					{"UTF8","UTF-8"},
					{"UTF-16","UTF-16"},
					{"UTF-16BE","UnicodeBigUnmarked"},
					{"UTF-16LE","UnicodeLittleUnmarked"},
					{"ASCII","US-ASCII"},
					{"Big5","Big5"},
					{"Big5_HKSCS","Big5-HKSCS"},
					{"EncodingWrapper","EncodingWrapper"}
				};
		}
		catch (Exception e)
		{
			logger.error("GXUtil static constructor error: ", e);
			throw new ExceptionInInitializerError("GXUtil static constructor error: " + e.getMessage());
		}
	}
	
	public static String removeAllQuotes(String fileName)
	{
		StringBuffer out = new StringBuffer();
		int len = fileName.length();
		for (int i = 0; i < len; i++)
			if	(fileName.charAt(i) != '"')
				out.append(fileName.charAt(i));

		return out.toString();
		/*


		if	(fileName.length() > 0)
		{
			if	(fileName.charAt(0) == '"')
				return fileName.substring(1, fileName.length() -1 );
		}

		return fileName;*/
	}
	
	public static String getPackageName(Class packageClass)
	{
		return getPackageName(packageClass.getName());
	}
	
	public static String getPackageName(String className)
	{
		if	(className.indexOf('.') < 0)
			return "";

		return className.substring(0, className.lastIndexOf('.'));
	}
	
	public static void InputStreamToFile(InputStream source, String fileName)
	{
		if	(source == null)
		{
			throw new IllegalArgumentException("InputStreamToFile -> Input stream can't be null");
		}

		byte[] buffer;
		int bytes_read;
		OutputStream destination = null;
		try 
		{
			destination = new BufferedOutputStream(new FileOutputStream(fileName));
			buffer = new byte[1024];
		 
			while (true) 
			{
				bytes_read = source.read(buffer);
				if (bytes_read == -1) break;
				destination.write(buffer, 0, bytes_read);
			}
		}
		catch (IOException e)
		{
			logger.debug("Error writing file " + fileName + ":" + e.getMessage());
		}
		finally 
		{
			if (source != null) 
				try { source.close(); } catch (IOException e) { ; }
			if (destination != null) 
				try {destination.close(); } catch (IOException e) { ; }
		}
	}


	public static void writeLogInfo( String message)
	{
		logger.info( message);
	}

	public static void writeLogError( String message)
	{
		logger.error( message);
	}

	public static void writeLogln( String message)
	{
		logger.debug( message);
	}

	public static void writeTLogln( String message)
	{
		logger.trace(message);
	}

	public static void writeLogRaw( String message, Object obj)
	{
		logger.debug(message);
		logger.debug(obj.toString());
	}

	public static void writeLog( String message)
	{
		logger.debug(message, new Throwable());
	}

	public static String accessKey(String OCaption)
	{
		String AccessKey = new String();
		if (OCaption.indexOf('&') != -1)
		{
			for (int i = 0; i < OCaption.length() - 1; i++)
			{
				if (OCaption.charAt(i) == '&')
                                {
                                  if (OCaption.charAt(i + 1) == '&')
                                  {
                                    i++;
                                  }
                                  else
                                  {
					AccessKey = AccessKey + OCaption.charAt(i + 1);
					break;
                                  }
                              }
			}
		}
		return AccessKey;
	}

	public static String accessKeyCaption(String OCaption)
	{
		String DCaption = new String();
		if (OCaption.indexOf('&') == -1)
                  return OCaption;
                for (int i = 0; i < OCaption.length() - 1; i++)
                {
                  if (OCaption.charAt(i) == '&')
                  {
                    if (OCaption.charAt(i + 1) == '&')
                    {
                      DCaption += OCaption.charAt(i);
                      i++;
                    }
                    else
                    {
                        DCaption += OCaption.substring(i + 1);
                        break;
                    }
                  }
                  else
                    DCaption += OCaption.charAt(i);
                }
		return DCaption;
	}

	public static Calendar getCalendar()
	{
		return (Calendar)threadCalendar.get();
	}

	public static String newLine()
	{
		return chr(13) + chr(10);
	}

	public static String getHost(String hostAndPort)
	{
		int pos = hostAndPort.indexOf(':');

		if	(pos >= 0)
		{
			return hostAndPort.substring(0, pos);
		}

		return hostAndPort;
	}

	public static int getPort(String hostAndPort)
	{
		return getPort(hostAndPort, 0);
	}

	public static int getPort(String hostAndPort, int def)
	{
		int pos = hostAndPort.indexOf(':');

		if	(pos >= 0 && pos + 1 < hostAndPort.length())
		{
			return Integer.parseInt(hostAndPort.substring(pos + 1));
		}

		return def;
	}


	public static byte deleteFile(String fileName)
	{
		return (new File(fileName).delete()?(byte) 1: 0);
	}

	public static double mod(double value, double div)
	{
		return value % div;
	}

	public static double random()
	{
		return random.nextDouble();
	}


	public static int rseed(double[] seed)
	{
		return rseed((long) seed[0]);
	}

	public static int rseed(long seed)
	{
		random.setSeed(seed);
		return 0;
	}

	public static double rand()
	{
		return Math.abs(Math.random());
	}

	public static int aleat()
	{
		return (int) (Math.random() * Short.MAX_VALUE);
	}

	public static String format(long value, String picture)
	{
		return new Long(value).toString();
	}
	public static String format(double value, String picture)
	{
		return new Double(value).toString();
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
                if (date.equals(CommonUtil.nullDate()))
                        return "";

                return CommonUtil.getYYYYMMDDHHMMSS_nosep(date);
        }

		public static String formatDateTimeParmMS(Date date)
        {
                if (date.equals(CommonUtil.nullDate()))
                        return "";

                return CommonUtil.getYYYYMMDDHHMMSSmmm_nosep(date);
		}
		
        public static String formatDateParm(Date date)
        {
                if (date.equals(CommonUtil.nullDate()))
                        return "";

                return CommonUtil.getYYYYMMDD(date);
        }


	public static String alltrim(String text)
	{
		return rtrim(ltrim(text));
	}

	public static String rtrim(String text)
	{
          if (text != null)
          {
            int anchor = 0, i;
			int len = text.length();

			if (len==0 || (len > 0 && text.charAt(len - 1) != ' '))
				return text;
			else
			  {
					for (i = len - 1; i >= 0; i--) {
					  if (text.charAt(i) != ' ') {
						anchor = i;
						break;
					  }
					}
					if (i < 0) {
					  return ("");
					}
					else {
					  // The substring begins at the specified beginIndex and
					  // extends to the character at index endIndex - 1.
					  return (text.substring(0, anchor + 1));
					}
			  }
          }
          else
            return "";
	}
	public static boolean endsWith(String s1, String s2)
	{
		if (s1 == null || s2 == null)
			return false;
		else
			return s1.endsWith(s2);
	}
	public static boolean startsWith(String s1, String s2)
	{
		if (s1 == null || s2 == null)
			return false;
		else
			return s1.startsWith(s2);
	}
	public static boolean contains(String s1, String s2)
	{
		if (s1 == null || s2 == null)
			return false;
		else
			return s1.contains(s2);
	}
	public static String charAt(String s1, int idx)
	{
		if (StringUtils.isEmpty(s1) || s1.length() < idx || idx <= 0)
			return "";
		else
			return String.valueOf(s1.charAt(idx-1));
	}
	public static String substring (String text, int start, int end)
	{
		try
		{
			if (end < 0)
				return text.substring(start - 1);

			if	(start > text.length())
				return "";

			if (start + end - 1 > text.length() )
				return (text.substring(start - 1));

			return (text.substring(start - 1, start + end - 1));
		}catch(StringIndexOutOfBoundsException e)
		{
			return "";
		}
	}

	public static String substringByte (String text, int lenInBytes, String encoding)
	{
		String result = text==null ? "" : text;
		try
		{
                if (byteCount(result, encoding) < lenInBytes)
                {
                    return result;
                }
                else if (result.length() > lenInBytes)//Cada caracter ocupa por lo menos un byte, si se pasa se hace un primer corte
                {
                    result = result.substring(0, lenInBytes);
                }

                while (byteCount(result, encoding) > lenInBytes && result.length() > 0) //Mientras el largo en bytes sea mayor que el requerido
                    result = result.substring(0, result.length() - 1); //se trunca el ultimo char

                return result;
		}catch(StringIndexOutOfBoundsException e)
		{
			return result;
		}
	}


	public static Date ymdhmsToT_noYL(String yyyymmddhhmmss)
	{

		int mil = (yyyymmddhhmmss.trim().length() >= 17)? (int)Integer.parseInt(yyyymmddhhmmss.substring(14, 17)):0;

		int year    = Integer.parseInt(yyyymmddhhmmss.substring( 0,  4));
		int month   = Integer.parseInt(yyyymmddhhmmss.substring( 4,  6));
		int day     = Integer.parseInt(yyyymmddhhmmss.substring( 6,  8));
		int hour    = Integer.parseInt(yyyymmddhhmmss.substring( 8, 10));
		int minute  = Integer.parseInt(yyyymmddhhmmss.substring(10, 12));
		int seconds = Integer.parseInt(yyyymmddhhmmss.substring(12, 14));

		return ymdhmsToT_noYL(year, month, day, hour, minute, seconds, mil);
	}

	public static Date resetDate(Date date)
	{
		Date nd = newNullDate();
                Calendar cal = getCalendar();
                synchronized (cal)
                {
                  Calendar cal2 = new GregorianCalendar(CommonUtil.defaultTimeZone, CommonUtil.defaultLocale);
                  cal2.setTime(date);
                  cal.setTime(nd);
                  cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
                  cal.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
                  cal.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
			cal.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
                  return cal.getTime();
                }
	}

	public static Date ymdhmsToT_noYL(int year, int month, int day , int hour , int minute , int second)
	{
		return  ymdhmsToT_noYL(year, month, day, hour, minute, second, 0);
	}
	public static Date ymdhmsToT_noYL(int year, int month, int day , int hour , int minute , int second, int millisecond)
	{
		if	(year == 0 && month == 0 && day == 0 && (hour != 0 || minute != 0 || second != 0 || millisecond !=0))
		{
			Date nd = newNullDate();
                        Calendar cal = getCalendar();
                        synchronized (cal)
                        {
                          cal.setTime(nd);
                          cal.set(Calendar.HOUR_OF_DAY, hour);
                          cal.set(Calendar.MINUTE, minute);
                          cal.set(Calendar.SECOND, second);
						  cal.set(Calendar.MILLISECOND, millisecond);
                          return cal.getTime();
                        }
		}

		Calendar cal = getCalendar();
		boolean lenient = cal.isLenient();
		try
		{
			synchronized (cal)
			{
				cal.setLenient(false);
				cal.clear();
				cal.set(year, month - 1, day, hour, minute, second);
				if (millisecond > 0)
				{
					cal.add(Calendar.MILLISECOND, millisecond);
				}
				Date date = cal.getTime();
				cal.setLenient(lenient);

				return date;
			}
		}
		catch (IllegalArgumentException e)
		{
			if (hour == 0)
			{
				hour = 1;
				while (hour < 24) {
					try {
						synchronized (cal) {
							cal.setLenient(false);
							cal.clear();
							cal.set(year, month - 1, day, hour, minute, second);
							if (millisecond > 0) {
								cal.add(cal.MILLISECOND, millisecond);
							}
							Date date = cal.getTime();
							cal.setLenient(lenient);
							return date;
						}
					} catch (IllegalArgumentException ex) {
						hour ++;
					}
				}
				return nullDate();
			}
			else
			{
				return nullDate();
			}
		}
		catch (Exception e)
		{
			return nullDate();
		}
	}


	public static Date newNullDate()
	{
		if(nullDate != null)
		{
			return new Date(nullDate.getTime());
		}
		/*
			Obtengo la fecha de esta manera:

			GregorianCalendar nullDateCal  = new GregorianCalendar(defaultTimeZone, defaultLocale);
			nullDateCal.set(Calendar.YEAR, 2); // El año es 2 y NO 1 porque en el año 0001 tenemos problemas con los 'daylight time savings'
			nullDateCal.set(Calendar.MONTH, 0);
			nullDateCal.set(Calendar.DATE, 1);
			nullDateCal.set(Calendar.HOUR_OF_DAY, 0);
			nullDateCal.set(Calendar.MINUTE, 0);
			nullDateCal.set(Calendar.SECOND, 0);
			nullDateCal.set(Calendar.MILLISECOND, 0);
			return nullDateCal.getTime();

			Pero luego para optimizar obtenemos el valor nullDateCal.getTime().getTime()
			y creamos un date con ese valor (-62104226400000L).
			Notas: Utilizamos el año 0002 en vez del 0001, porque para el año 0002 tenemos
			problemas con los timezones que tienen 'daylight time savings'

			@Nota 2: ->	Luego de haber hecho el workaround de desactivar los daylight time savings
						volvemos para atrás este cambio, porque en J# estan usando por otro lado
						harcoded la fecha 01/01/0001
		*/

//		return resetTime(PrivateUtilities.getTimeAsDate( -62104226400000L));// Antes: -62135758799651L -> antes: -62230464000000L
//		return resetTime(PrivateUtilities.getTimeAsDate( -62135758799651L));// Antes: -62135758799651L -> antes: -62230464000000L

		Date theDate = resetTime(DateUtils.getTimeAsDate(-62135758799651L));

                Calendar cal = getCalendar();
                synchronized (cal)
                {
                  cal.setTime(theDate);
                  cal.get(Calendar.DAY_OF_WEEK);

                  // @HACK: date: 09/03/04
                  // Este hack es bastante feo, pero aparentemente explica algunos de los problemas que tuvimos
                  // con los dates.
                  // Las VM tienen algun lío con la fecha 1/1/0001 con algunos timezones, por lo que en algunos
                  // casos te dejan como fecha 31/dic/0001 en vez de 1/ene/0001
                  // Lo que voy a hacer poner 'a mano' 1 de enero
                  // El tema es que las VMs tienen varios 1/1/0001, si al time() correspondiente al 1/1/0001
                  // le resto la cantidad de milisegundos equivalentes a 1 año, entonces tengo de nuevo 1/1/0001
                  // Solo que esas 2 fechas NO son la misma (en particular tienen distinto dia de la semana) y
                  // su equals no da true
                  // El bug en el cual caíamos entonces era doble, porque ademas de dejarnos en el 31/dic/0001, el time()
                  // era menor que el del primer 1/1/0001, as?que si seteabamos a prepo el mes y el dia obteníamos un date distinto
                  // Lo que hacemos para obtener el mismo 1/1/0001 es sumarle 360 días al nulo que obtuve de ahi y
                  // luego s?seteo el mes y el day para que sean 1/1
                  if (cal.get(Calendar.MONTH) != 0 || cal.get(Calendar.DAY_OF_MONTH) != 1)
                  {
                    theDate.setTime(theDate.getTime() + 31104000000L); // Sumo 360 días para estar seguros que estoy en el primer año
                  }
                  while (cal.get(Calendar.MONTH) != 0)
                  {
                    cal.set(Calendar.MONTH, 0);
                  }
                  while (cal.get(Calendar.DAY_OF_MONTH) != 1)
                  {
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                  }
                  return cal.getTime();
                }
	}

	public static Date nullDate()
	{
		// Antes se retornaba siempre la misma instancia de Date
		// Esto es peligroso porque alguien puede hacer un date.setTime(xxx) y te
		// cambia el valor del nulo!. En 2 capas no es muy grave pero en 3 capas si
		// porque te queda desfasado el nulo del cliente con el del server

		if (nullDate == null)
		{
			nullDate = newNullDate();
		}

		return new Date(nullDate.getTime());
	}

	public static Date resetTime(Date dt)
	{
		Calendar cal = getCalendar();
		synchronized (cal)
		{
			cal.setTime(dt);
			cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			try
			{
				return cal.getTime();
			}
			catch (IllegalArgumentException e)
			{
				int hour = 1;
				while (hour < 24) {
					cal.set(Calendar.HOUR_OF_DAY, hour);
					try {
						return cal.getTime();
					}
					catch (IllegalArgumentException e1) {
						hour ++;
					}
				}
				return dt;
			}
		}
	}

	/** Resetea los milisegundos de un date
	 */
	public static Date resetMillis(Date dt)
	{
		return new Date(dt.getTime() - (dt.getTime()%1000));
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
		if	(left.after(right))  return 1;
		if	(left.before(right)) return -1;
		return 0;
   	}

   	public static boolean dateCompare( Date left , Date right)
   	{
   		if (left == null)
   		{
   			left = nullDate();
   		}
   		if (right == null)
   		{
   			right = nullDate();
   		}
   		return left.equals(right);
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
		if (mode == 1) // Compare as SQLServer does
		{
			String[] itemsA = guidA.toString().split("-");
			String[] itemsB = guidB.toString().split("-");

			for (int i=4; i>=0; i--)
			{
				int comparison = itemsA[i].compareTo(itemsB[i]);
				if (comparison != 0)
					return comparison;
			}

			return 0;
		}

		return strcmp(guidA.toString(), guidB.toString());
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
		TimeZone tz = TimeZone.getDefault();
		int ret = tz.getRawOffset();
		return (short)(ret / 60000); //mili -> min
	}

	public static Date DateTimetoCurrentTime(Date value, int Offset)
	{
		TimeZone tz = TimeZone.getDefault();
		Date ret = new Date( value.getTime() - Offset * 60000 + CurrentTimeOffset() * 60000 );

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



    public static Date ConvertDateTime(Date dt, TimeZone FromTimezone, TimeZone ToTimezone)
    {
		return DateTimeFromUTC(DateTimeToUTC(dt, FromTimezone), ToTimezone);
    }

	public static boolean emptyDate( Date value)
	{
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(value);
		Calendar nullCalendar = GregorianCalendar.getInstance();
		nullCalendar.setTime(nullDate);
		return calendar.get(Calendar.YEAR) == nullCalendar.get(Calendar.YEAR) && calendar.get(Calendar.MONTH) == nullCalendar.get(Calendar.MONTH) && calendar.get(Calendar.DAY_OF_MONTH) == nullCalendar.get(Calendar.DAY_OF_MONTH);
	}


	public static Date DateTimeToUTC(Date value, TimeZone tz)
	{
		return SpecificImplementation.GXutil.DateTimeToUTC(value, tz);
	}



	public static Date DateTimeFromUTC(Date value)
	{
		return DateTimeFromUTC( value, TimeZone.getDefault());
	}

	public static Date DateTimeFromUTC(Date value, TimeZone tz)
	{
		return SpecificImplementation.GXutil.DateTimeFromUTC(value, tz);
	}





	public static String formatLong(long number)
	{
		return Long.toString(number); //"" + number;
	}


	public static String booltostr(boolean val){

		if (val)
		{
			return "true";
		}
		else
		{
			return "false";
		}
	}

	public static boolean strtobool(String val)
	{
		return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("1");
	}


	public static double log10(double val)
	{
		return Math.log(val) / Math.log(10.0);
	}

	public static int strSearch(String a, String b, int from)
	{
		if	(from <= 0)
			return 0;

		return a.indexOf(b, from - 1) + 1;
	}

	public static int strSearch(String a, String b)
	{
		return strSearch(a, b, 0);
	}

	public static int strSearchRev(String a, String b)
	{
		if	(b.length() == 0)
			return a.length();

		return a.lastIndexOf(b) + 1;
	}

	public static int strSearchRev(String a, String b, int from)
	{
		if (from <= 0)
		{
			if	(from == -1)
				return strSearchRev(a, b);

			return 0;
		}

		return a.lastIndexOf(b, from - 1) + 1;
	}


	public static String strReplace(String s, String subString, String replacement)
	{
		if	(subString.length() == 0)
			return s;

		int index, start, subLength;
		StringBuffer buf = new StringBuffer();
		subLength = subString.length();

		for (start = 0, index = s.indexOf(subString, start); index >= 0; start = index + subLength, index = s.indexOf(subString, start))
		{
			buf.append(s.substring(start, index));
			buf.append(replacement);
		}
		buf.append(s.substring(start));
		return buf.toString();
	}


	public static String padr (String text, int size, String fill)
	{
		return left(rtrim(text) + replicate(fill, size), size);
	}

	public static String padl (String text, int size, String fill)
	{
	   	String trimText = new String(ltrim(text));
		int len = trimText.length();

		if (size > len)
		{
			int cantRep = ((size - len) / fill.length()) + 1;
			String head = left (replicate(fill, cantRep), size - len);
			return (left (head + trimText, size));
		}

		return (left (trimText, size));
	}

	public static String right (String text, int size)
	{
		int length     = text.length();
		int leftMargin = length - size;

		return text.substring( leftMargin<0? 0: leftMargin, length);
	}

	public static String left (String text, int size)
	{
		if	(text == null) return "";
		int length     = text.length();

		if 	(length < size || size < 0)
			return text;

		return text.substring(0, size<length? size :length );
	}

	public static String replicate (char character, int size)
	{
		if	(size <= 0)
			return "";

		StringBuffer ret = new StringBuffer(size);

		for (int i = 0; i < size; i++)
		{
			ret.append(character);
		}

		return ret.toString();
	}

	public static String replicate (String character, int size, int a)
	{
		if	(size <= 0)
			return "";

		StringBuffer ret = new StringBuffer(size);

		for (int i = 0; i < size; i++)
		{
			ret.append(character);
		}

		return ret.toString();
	}

	public static final String replicate (String character, int size)
	{
		if (size <= 0) return "";
		if (character.length() == 0) return "";

		char c = character.charAt(0);

		char[] buf = new char[size];

		for (int i = 0; i < size; i++)
		{
			buf[i] = c;
		}

        return new String(buf);
    }
	public static String ltrimstr(double value, int length, int decimals){
		return ltrim(str(value, length, decimals));
	}

	public static String ltrimstr(BigDecimal value, int length, int decimals){
		return ltrim(str(value, length, decimals, true));
	}

	public static String ltrimstr(long val, int digits, int decimals) {
		return ltrim(str(val, digits, decimals));
	}

	public static String ltrim (String text)
	{
		if (text!=null)
		{
			int start;
			int len = text.length();
			if (len==0 || (len > 0 && text.charAt(0) != ' '))
				return text;
			else
			{
				for (start = 0; start < text.length() && text.charAt(start) == ' ' ;start++);
				return (text.substring(start, text.length()));
			}
		}
		else
			return "";
	}

	public static String time()
	{
		SimpleDateFormat time_df = new java.text.SimpleDateFormat("HH:mm:ss");
		time_df.setTimeZone(defaultTimeZone);
		return time_df.format(now());
	}

	public static Date addmth(Date date, int cnt)
	{
		GregorianCalendar appGregorianCalendar = new GregorianCalendar(defaultTimeZone, defaultLocale);
		appGregorianCalendar.setTime(date);

		int day = appGregorianCalendar.get(appGregorianCalendar.DATE);

		// Es diferente para fechas > 28 porque el addmth de 31/12 + 2 tiene que dar 28/2 o 29/2
		if (day <= 28)
		{
			appGregorianCalendar.add(appGregorianCalendar.MONTH, cnt);
			return appGregorianCalendar.getTime();
		}

		int	mth = appGregorianCalendar.get(appGregorianCalendar.MONTH);
		int newYr ,newMth , newDay;

		if (cnt >= 0)
		{
			newYr  = appGregorianCalendar.get(appGregorianCalendar.YEAR) + ((mth + cnt )/12);
			newMth = ((12 + mth + cnt) % 12 ) ;
		}
		else
		{
			newYr  = appGregorianCalendar.get(appGregorianCalendar.YEAR) + cnt /12;
			if (-cnt % 12 > mth)
			{
				newYr --;
			}
			newMth = ((12 + mth + (cnt % 12) ) % 12 ) ;
		}

		newDay = day;

		if (newMth == 1 )
		{ // Si es bisiesto, el mayor dia de febrero es 29, sino es 28
			newDay = appGregorianCalendar.isLeapYear(newYr) ? 29 : 28;
		}
		else if ( (day == 31) && ((newMth == 3) || (newMth == 5) || (newMth == 8) || (newMth == 10)))
		{ // En abril, junio, setiembre y noviembre hay 30 días
			newDay = 30;
		}

		appGregorianCalendar.set(newYr, newMth, newDay);
		return (appGregorianCalendar.getTime());
	}

	//Debe ignorar los caracteres invalidos
	public static long lval(String text)
	{
		if (text == null)
			return 0;

		text = text.trim();
		try
		{
			return new Long(text).longValue();
		}
		catch (Exception e)
		{
			StringBuffer out = new StringBuffer();

			boolean first = true;
			int len = text.length();

			for (int i = 0; i < len; i++)
			{
				char c = text.charAt(i);

				if	(c >= '0' && c <= '9')
				{
					out.append(c);
				}
				else if	(c == '-' && first)
				{
					out.append('-');
					first = false;
				}
				else
				{
					break;
				}
			}

			try
			{
				return new Long(out.toString()).longValue();
			}
			catch (Exception ex)
			{
				return 0;
			}
		}
	}

	public static double val(String text)
	{
            return val(text, ".");
        }

	public static BigDecimal decimalVal(String text, String sDSep)
	{
		if (text == null)
			return BigDecimal.ZERO;

		text = text.trim();

		try
		{
			return new BigDecimal(text);
		}
		catch (Exception e)
		{
			try
			{
				return new BigDecimal(extractNumericStringValue(text, sDSep).toString());
			}
			catch (Exception ex)
			{
			}
		}
		return BigDecimal.ZERO;
	}

	private static StringBuffer extractNumericStringValue(String text, String sDSep) {
		StringBuffer out = new StringBuffer();

		char dSep = (sDSep.length() > 0) ? sDSep.charAt(0) : '.';
		boolean point = false;
		boolean first = true;
		int len = text.length();

		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);

			if (c >= '0' && c <= '9') {
				out.append(c);
			} else if (c == dSep && !point) {
				out.append('.');
				point = true;
			} else if (c == '-' && first) {
				out.append('-');
				first = false;
			} else {
				break;
			}
		}
		return out;
	}

	public static double val(String text, String sDSep)
	{
		if (text == null)
			return 0;

		text = text.trim();

		try
		{
			java.lang.Double d = new java.lang.Double(text);
			return d.doubleValue();
		}
		catch (Exception e)
		{
			try
			{
				java.lang.Double d = new java.lang.Double(extractNumericStringValue(text, sDSep).toString());
				return d.doubleValue();
			}
			catch (Exception ex)
			{
			}
		}
		return 0;
	}

	public static boolean notNumeric(String value)
	{
		if (value.isEmpty())
			return false;
		try
		{
			Number num = java.text.NumberFormat.getInstance().parse(value);
			return false;
		}
		catch(ParseException pe)
		{
			return true;
		}
	}

	public static boolean boolval(String text)
	{
        if (text == null || text.trim().length() == 0)
            return false;
        else
            return (text.toLowerCase().equals("true") || text.trim().equals("1"));
	}
	private static int daysOfMonth(int month, int year)
	{
		int daysOfMonth = 31;

		if (month == 4 || month == 6 || month == 9 || month == 11)
		{
			daysOfMonth = 30 ;
		}
		else if (month == 2)
		{
			daysOfMonth = 28 ;
			if ( (year % 400 == 0) || ((year % 4 == 0) && ((year % 100 )!= 0) ))
			{
				daysOfMonth = 29;
			}
		}

		return daysOfMonth;
	}

	public static Date eomdate(Date date)
	{
		return resetTime(eom(date));
	}

	public static Date eom(Date date)
	{
		GregorianCalendar appGregorianCalendar = new GregorianCalendar(defaultTimeZone, defaultLocale);
		appGregorianCalendar.setTime(date);

		int month	 = appGregorianCalendar.get(appGregorianCalendar.MONTH);
		int year  	 = appGregorianCalendar.get(appGregorianCalendar.YEAR);

		appGregorianCalendar.set(year, month, daysOfMonth(month + 1, year), 23, 59, 59);
		return (appGregorianCalendar.getTime());
	}

	public static Date dtadd(Date date, int seconds)
	{
		if (seconds % SECONDS_IN_DAY == 0)
		{
			return dadd(date, seconds / SECONDS_IN_DAY);
		}
		GregorianCalendar appGregorianCalendar = new GregorianCalendar(defaultTimeZone, defaultLocale);
		appGregorianCalendar.setTime(date);
		appGregorianCalendar.add(appGregorianCalendar.SECOND, seconds);

		return (appGregorianCalendar.getTime());
	}

	public static Date dtaddms(Date date, double seconds)
	{
		GregorianCalendar appGregorianCalendar = new GregorianCalendar(defaultTimeZone, defaultLocale);
		appGregorianCalendar.setTime(date);
		appGregorianCalendar.add(appGregorianCalendar.MILLISECOND, (int)Math.round(seconds * 1000));
		return (appGregorianCalendar.getTime());
	}


	public static Date dadd(Date date, int cnt)
	{
		GregorianCalendar appGregorianCalendar = new GregorianCalendar(defaultTimeZone, defaultLocale);
		appGregorianCalendar.setTime(date);
		appGregorianCalendar.add(appGregorianCalendar.DATE, cnt);

		return (appGregorianCalendar.getTime());
	}

// diferencia en segundos

	public static long dtdiff(Date dateStart, Date dateEnd)
	{
		boolean startDT = defaultTimeZone.inDaylightTime(dateStart);
		boolean endDT   = defaultTimeZone.inDaylightTime(dateEnd  );

		long ret = (dateStart.getTime() - dateEnd.getTime()) / 1000;

		if	(startDT && !endDT)
			ret += 3600;

		if  (!startDT && endDT)
			ret -= 3600;

		return ret;
	}

	public static double dtdiffms(Date dateStart, Date dateEnd)
	{
		boolean startDT = defaultTimeZone.inDaylightTime(dateStart);
		boolean endDT   = defaultTimeZone.inDaylightTime(dateEnd);

		double ret = ((dateStart.getTime() - dateEnd.getTime()) / 1000.0) ;

		if	(startDT && !endDT)
			ret += 3600;

		if  (!startDT && endDT)
			ret -= 3600;

		return ret;
	}
	
	

	public static String concat(String first, String second)
	{
		return (rtrim(first) + second);
	}

	public static String concat(String first, String second, String separator)
	{
		return (rtrim(first) + separator + second );
	}


	public static void msg(Object panel, String sText)
	{
		logger.debug("msg: " + sText);
	}

	public static void error(Object panel, String sText)
	{
		logger.error("err: " + sText);
	}


	static public boolean like( String str, String ptrn)
	{
		return like(str, ptrn, ' ');
	}

	static public boolean like( String str, String ptrn, char escape)
	{
		boolean	found;

		int		WildLen,
				AsterPos,
				WildPtr,
                Backplaces,
				SrchPtr,
				SrchLen,
				ScapeCount;

		char	WildChr,
				SrchChr;

		found	= true;
		SrchLen	= str.length() - 1;
		WildLen	= ptrn.length() - 1;
		AsterPos	= -1;
		WildPtr	= 0;
		SrchPtr	= 0;
		ScapeCount = 0;

		WildChr	= ' ';
		SrchChr	= ' ';

		boolean useEscape = escape != ' ';
		boolean isEscape = false;
		boolean applyEscape = false;

		while ( WildPtr <= WildLen)
		{
			WildChr = ptrn.charAt(WildPtr);
			if (useEscape && !isEscape)
				isEscape = WildChr == escape;
			else
				isEscape = false;

			if (!isEscape)
			{
				if (SrchPtr <= SrchLen)
					SrchChr = str.charAt(SrchPtr-ScapeCount);
				else
					SrchChr = ' ';
			}

			if (isEscape)
			{
				applyEscape = true;
				WildPtr++;
				SrchPtr++;
				ScapeCount++;
			}
			else if (SrchChr == WildChr || ((!applyEscape && WildChr == QMARK) && SrchPtr <= SrchLen))
			{
				found	= true;
				SrchPtr++;
				if (WildPtr != WildLen || SrchPtr > SrchLen)
					WildPtr++;
			}
			else if (!applyEscape && WildChr == ASTER)
			{
				found		= true;
				AsterPos	= WildPtr;
				WildPtr++;
			}
			else
			{
				found		= false;
				if (AsterPos == -1 || SrchPtr > SrchLen)
					break;
				else if (AsterPos == WildPtr - 1)
					SrchPtr++;
				else
				{
					Backplaces = WildPtr - (AsterPos + 1);
					WildPtr = AsterPos + 1; //Se reposiciona backplaces atras en el ptrn.
					SrchPtr = SrchPtr - Backplaces + 1; //Se reposiciona (backplaces + 1) atras en el str.
				}
			}

			if (!isEscape)
			{
				applyEscape = false;
			}
		}

		return (found && (SrchPtr > SrchLen || (!applyEscape && WildChr == ASTER)));
	}

	public static Date addyr(Date date, int yr)
	{
		GregorianCalendar appGregorianCalendar = new GregorianCalendar(defaultTimeZone, defaultLocale);
		appGregorianCalendar.setTime(date);
		appGregorianCalendar.add(appGregorianCalendar.YEAR, yr);

		return (appGregorianCalendar.getTime());
	}

	public static int age(Date fn, Date today)
	{
		Date fnDate    = resetTime(fn);
		Date todayDate = resetTime(today);

		int age = 0;
		int	multiplier = 1;

		if (!fn.equals(nullDate()) && !today.equals(nullDate()))
		{
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(fn);
			Calendar todayCalendar = GregorianCalendar.getInstance();
			calendar.setTime(today);
			int fnTime 		= calendar.get(Calendar.HOUR)    * 10000 + calendar.get(Calendar.MINUTE)    * 100 + calendar.get(Calendar.SECOND);
			int todayTime 	= todayCalendar.get(Calendar.HOUR) * 10000 + todayCalendar.get(Calendar.MINUTE) * 100 + todayCalendar.get(Calendar.SECOND);

			GregorianCalendar gage1, gage2;
			gage1 = new GregorianCalendar(defaultLocale);
			gage2 = new GregorianCalendar(defaultLocale);
			gage1.setTimeZone(defaultTimeZone);
			gage2.setTimeZone(defaultTimeZone);

			if	(!fnDate.before(todayDate))
			{
				multiplier = -1;
				Date aux = fn;
				fn = today;
				today = aux;
			}

			gage1.setTime(fn);
			gage2.setTime(today);

			int m = gage1.get(gage1.MONTH);
			int d = gage1.get(gage1.DATE);
			int y = gage1.get(gage1.YEAR);

			int m1 = gage2.get(gage2.MONTH);
			int d1 = gage2.get(gage2.DATE);
			int y1 = gage2.get(gage2.YEAR);

			age = (y1 - y);

			if (age > 0)
			{
				if ((m > m1) ||
					 (m == m1 && d > d1) ||
					 (m == m1 && d == d1 && fnTime > todayTime))
				{
					age--;
				}
			}

		}

		return multiplier * age;
/*

		if ((m > m1) ||
			 (m == m1 && d > d1) ||
			 (m == m1 && d == d1 && gage1.getTime().after(gage2.getTime())))
			{
				age--;
			}

		return age;
*/
	}

	public static int age(Date dateStart)
	{
		return (age(dateStart, new Date()));
	}

	public static int hour(Date date )
	{
		SimpleDateFormat format = new java.text.SimpleDateFormat("H");
		format.setTimeZone(defaultTimeZone);
		return  Integer.parseInt(format.format(date));
	}

	public static int minute(Date date )
	{
		SimpleDateFormat format = new java.text.SimpleDateFormat("m");
		format.setTimeZone(defaultTimeZone);
		return  Integer.parseInt(format.format(date));
	}

	public static int second(Date date )
	{
		SimpleDateFormat format = new java.text.SimpleDateFormat("s");
		format.setTimeZone(defaultTimeZone);
		return  Integer.parseInt(format.format(date));
	}

	public static int millisecond(Date date )
	{
		SimpleDateFormat format = new java.text.SimpleDateFormat(SpecificImplementation.MillisecondMask);
		format.setTimeZone(defaultTimeZone);
		return  Integer.parseInt(format.format(date));
	}

	public static int day(Date date)
	{
		if	(date == null || date.equals(nullDate()))
			return 0;

		SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("d");
		dayFormat.setTimeZone(defaultTimeZone);
		return  Integer.parseInt(dayFormat.format(date));
	}

	public static int month(Date date)
	{
		if	(date == null || date.equals(nullDate()))
			return 0;

		SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("M");
		monthFormat.setTimeZone(defaultTimeZone);
		return  Integer.parseInt(monthFormat.format(date));
	}

	public static int  year(Date date)
	{
		if	(date == null || date.equals(nullDate()))
			return 0;

		SimpleDateFormat yearFormat = new java.text.SimpleDateFormat("yyyy");
		yearFormat.setTimeZone(defaultTimeZone);

		return  Integer.parseInt(yearFormat.format(date));
	}

	public static String getYYYYMMDD(Date date)
	{
		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
		dateFormat.setTimeZone(defaultTimeZone);

		return dateFormat.format(date);
	}

	public static String getYYYYMMDDHHMMSS_nosep(Date date)
	{
		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		dateFormat.setTimeZone(defaultTimeZone);

		return dateFormat.format(date);
	}

	public static String getYYYYMMDDHHMMSSmmm_nosep(Date date)
	{
		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
		dateFormat.setTimeZone(defaultTimeZone);

		return dateFormat.format(date);
	}

	public static String getYYYYMMDDHHMMSS(Date date)
	{
		// Esto se usa para grabar dates en el AS

		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		dateFormat.setTimeZone(defaultTimeZone);

		return dateFormat.format(date) + ".0";
	}

	public static String getYYYYMMDDHHMMSSmmm(Date date)
	{
		// Esto se usa para grabar dates en el AS

		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		dateFormat.setTimeZone(defaultTimeZone);

		return dateFormat.format(date) + ".0";
	}

	public static String getMMDDHHMMSS(Date date)
	{
		// Esto se usa para los logs JDBC

		SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMdd_HHmmss");
		dateFormat.setTimeZone(defaultTimeZone);

		return dateFormat.format(date);
	}

	public static int len(String text)
	{
		return (rtrim(text).length());
	}

	public static int byteCount(String text, String encoding)
	{
		try
		{
			return text.getBytes(normalizeEncodingName(encoding)).length;
		}
		catch(UnsupportedEncodingException e)
		{
			logger.debug(e.toString());
			return 0;
		}
	}

	public static String space(int n)
	{
		return (replicate(" ",n));
	}

	public static String trim(String text)
	{
		return alltrim(text);
	}

	public static long Int(double num)
	{
		return (long) num;
	}
	

	public static BigDecimal roundToEven(BigDecimal in, int decimals)
        {
          return roundDecimal(in, decimals, BigDecimal.ROUND_HALF_EVEN);
	}

	public static BigDecimal roundDecimal(BigDecimal in, int decimals, int roundType)
	{
		if(decimals >= 0)
		{
			return in.setScale(decimals, roundType);
		}
		else
		{ // Si la cantidad de decimales despues de la coma es un numero negativo, tenemos que hacerlo a mano
		  // Por ej: roundDecimal(12678, -2, BigDecimal.ROUND_HALF_UP) = 12700
			return in.divide(BigDecimal.valueOf((long)Math.pow(10, -decimals)), 0, roundType).multiply(BigDecimal.valueOf((long)Math.pow(10, -decimals)));
		}
	}

	public static BigDecimal roundDecimal(BigDecimal in, int decimals)
	{
		return roundDecimal(in, decimals, BigDecimal.ROUND_HALF_UP);
	}

	public static BigDecimal truncDecimal(BigDecimal num , int decimals)
	{
		return roundDecimal(num, decimals, BigDecimal.ROUND_DOWN);
	}


	public static byte dow(Date date)
	{
		Calendar cal = getCalendar();
		synchronized (cal)
		{
			cal.setTime(date);
			return (byte) cal.get(Calendar.DAY_OF_WEEK);
		}
	}

	protected static boolean in(String text , char c)
	{
		return (text.indexOf(c) != -1);
	}

	public static String getTimeFormat(String time)
	{
		StringBuffer ret = new StringBuffer(time);
		char hora;
		boolean app = false;
		char append = ' ';
		int i;
	//	time = GXFont.trimSpaces(time);

		if (in(time,'A') || in(time,'P') )
		{
			hora = 'h';
			app = true;
			append = 'a';
		}
		else
			hora = 'H';


		for (i = 0 ;( i < 2 && i < time.length() && ret.charAt(i) != ':') ; i++)
		{
			ret.setCharAt(i,hora);
		}
		if (i < time.length()  && ret.charAt(i) == ':')
		{
			for (i = 3 ; (i < 5 && i < time.length() && ret.charAt(i) != ':' ); i++)
			{
				ret.setCharAt(i,'m');
			}
		}
		if (i < time.length() && ret.charAt(i) == ':')
		{
			for (i = 6 ;(i < 8 && i < time.length() && ret.charAt(i) != ':') ; i++)
			{
				ret.setCharAt(i,'s');
			}
		}
		String retorno = ret.toString().substring(0,i);
		if (app)
			retorno = retorno + append;
		return (retorno);
	}

	public static String chr(int asciiValue)
	{
		return (new Character((char) asciiValue)).toString();
	}

	public static int asc(String value)
	{
		if	(value.length() == 0)
			return 32;

		return (int) value.charAt(0);
	}


	public static String getObjectName(String packageName, String objectName)
	{
		if	(!packageName.equals("") && objectName.startsWith(packageName))
		{
			return objectName;
		}

		return packageName + objectName.trim().toLowerCase();
	}


	public static String classNameNoPackage(Class cls)
        {
			String name = cls.getName();
            Package pkg = cls.getPackage();
			if (pkg != null && pkg.getName().length()>0)
			{
				name = name.substring(pkg.getName().length()+1);
			}
			return name;
		}

	public static byte sleep(long time)
	{
		try
		{
			Thread.sleep(time * 1000);
		}
		catch (Exception e)
		{
		}

		return 0;
	}

	public static int gxmlines(String text, int lineLength)
	{
		return MemoLines.gxmlines(text, lineLength);
	}

	public static String gxgetmli(String text, int line, int lineLength)
	{
		return MemoLines.gxgetmli(text, line, lineLength);
	}

  /** This method runs about 20 times faster than
   * java.lang.String.toLowerCase (and doesn't waste any storage when
   * the result is equal to the input).  Warning: Don't use
   * this method when your default locale is Turkey.
   * java.lang.String.toLowerCase is slow because (a) it uses a
   * StringBuffer (which has synchronized methods), (b) it initializes
   * the StringBuffer to the default size, and (c) it gets the default
   * locale every time to test for name equal to "tr".
   * @author Peter Norvig **/

  	public static String lower(String str)
  	{
    	int len = str.length();
    	int different = -1;

    	// See if there is a char that is different in lowercase
    	for( int i = len-1; i >= 0; i--)
    	{
      		char ch = str.charAt(i);
      		if (Character.toLowerCase(ch) != ch)
      		{
        		different = i;
        		break;
      		}
    	}

    	// If the string has no different char, then return the string as is,
    	// otherwise create a lowercase version in a char array.
    	if (different == -1)
		{
      		return str;
		}

  		char[] chars = new char[len];
  		str.getChars(0, len, chars, 0);

  		// (Note we start at different, not at len.)
  		for (int j = different; j >= 0; j--) {
    		chars[j] = Character.toLowerCase(chars[j]);
  		}

  		return new String(chars);
  }

  /** This method runs about 20 times faster than
   * java.lang.String.toUpperCase (and doesn't waste any storage when
   * the result is equal to the input).  Warning: Don't use
   * this method when your default locale is Turkey.
   * java.lang.String.toLowerCase is slow because (a) it uses a
   * StringBuffer (which has synchronized methods), (b) it initializes
   * the StringBuffer to the default size, and (c) it gets the default
   * locale every time to test for name equal to "tr".
   * @author Peter Norvig **/

  	public static String upper(String str)
  	{

    	int len = str.length();
    	int different = -1;

    	// See if there is a char that is different in lowercase
    	for( int i = len-1; i >= 0; i--)
    	{
      		char ch = str.charAt(i);
      		if (Character.toUpperCase(ch) != ch)
      		{
        		different = i;
        		break;
      		}
    	}

    	// If the string has no different char, then return the string as is,
    	// otherwise create a lowercase version in a char array.
    	if (different == -1)
		{
      		return str;
		}

  		char[] chars = new char[len];
  		str.getChars(0, len, chars, 0);

  		// (Note we start at different, not at len.)
  		for (int j = different; j >= 0; j--) {
    		chars[j] = Character.toUpperCase(chars[j]);
  		}

  		return new String(chars);
  }


	public static String getDefaultFontName(String language, String defValue)
	{
		try
		{
			if(language.equalsIgnoreCase("jap"))
			{
				return new String("\uFF2D\uFF33\u0020\u660e\u671d");
			}
		}catch(Exception e){ ; }
		return defValue;
	}

	public static String ianaEncodingName(String encoding)
	{
		String encUpper = encoding.toUpperCase();
		if(encUpper.endsWith(" BOM"))
		{
			encoding = encUpper.substring(0, encUpper.indexOf(" BOM"));
		}
		else if(encUpper.startsWith("ISO8859_"))
		{
			encoding = "ISO-8859-" + encUpper.substring(8);
		}
		if(encUpper.startsWith("8859_"))
		{
			encoding= "ISO-8859-" + encUpper.substring(5);
		}
		if(encUpper.equals("UTF8"))
		{
			encoding = "UTF-8";
		}
		return encoding;
	}

	//Transforma un encoding name en nombre canónico (establecido por iana, domain Encoding en genexus) al nombre esperado por jdk
	public static String normalizeEncodingName(String enc)
	{
		// when no encoding find use is0-8859 as default in java std, used in several places.
		return normalizeEncodingName(enc, "ISO-8859-1");
	}

	public static String normalizeEncodingName(String enc, String defaultEncoding)
	{
		try
		{
			return normalizeSupportedEncodingName(enc);
		}catch(Throwable e)
		{
			return defaultEncoding;
		}
	}


	//Transforma un encoding name en nombre canónico (establecido por iana, domain Encoding en genexus) al nombre esperado por jdk
	public static String normalizeSupportedEncodingName(String enc) throws Throwable
	{
		String encoding = ianaEncodingName(enc.trim());
		if(!encoding.equals(""))
		{
			for(int i = 0; i < ENCODING_JAVA_IANA.length; i++)
			{
				if(ENCODING_JAVA_IANA[i][1].equalsIgnoreCase(encoding))
				{
					encoding = ENCODING_JAVA_IANA[i][0];
					break;
				}
			}
		}

		SpecificImplementation.GXutil.checkEncoding(encoding);
		return encoding;
	}


	public static void refClasses(Class cls) { ; }

	public static String toValueList(String DBMS, Object arr, String prefix, String tail)
	{

               if (!arr.getClass().isArray())
               {
                 Vector vec = (Vector) arr;
                 return toValueList(DBMS, vec, prefix, tail);
               }
		boolean isNumber = arr.getClass().getComponentType().isPrimitive() || arr.getClass().getComponentType().isAssignableFrom(BigDecimal.class);
                int cantElements = Array.getLength(arr);

		if(cantElements == 0)
		{ // Si no hay elementos, pongo como condicion 1 = 0 para que de siempre falso
			return "1 = 0";
		}
		String ret = prefix;
		boolean isFirstItem = true;
		for(int i = 0; i < cantElements; i++)
		{
			if(isFirstItem)
			{
				isFirstItem = false;
			}
			else
			{
				ret +=", ";
			}
			Object item = Array.get(arr, i);

			if(item instanceof Date)
			{
				ret += dateToString((Date)item, DBMS);
			}
			else
			{
				if(isNumber)
				{
					ret += item.toString();
				}
				else
				{
					ret += "'" + item.toString().replaceAll("'", "''") + "'";
				}
			}
		}
		return ret + tail;
	}

        private static String toValueList(String DBMS, Vector vec, String prefix, String tail)
        {
                if(vec.size() == 0)
                { // Si no hay elementos, pongo como condicion 1 = 0 para que de siempre falso
                        return "1 = 0";
                }
                Class a;

                String ret = prefix;
                boolean isFirstItem = true;
                for(int i = 0; i < vec.size(); i++)
                {
                        if(isFirstItem)
                        {
                                isFirstItem = false;
                        }
                        else
                        {
                                ret +=", ";
                        }
                        Object item = vec.elementAt(i);

                        if(item instanceof Date)
                        {
                                ret += dateToString((Date)item, DBMS);
                        }
                        else
                        {
                                if(item instanceof Number)
                                {
                                        ret += item.toString();
                                }
                                else
                                {
                                        ret += "'" + item.toString().replaceAll("'", "''") + "'";
                                }
                        }
                }
                return ret + tail;
        }

	public static String dateToString(Date date, String DBMS)
	{
		if(DBMS.equalsIgnoreCase("SQLServer"))
		{
			if(date.before(CommonUtil.ymdhmsToT_noYL(1753, 1, 1, 0, 0, 1)))
		    {
				return "convert(DATETIME, '17530101', 112)";
			}
			SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(defaultTimeZone);
			return "convert(DATETIME, '" + dateFormat.format(date)+"', 120)";
		}else if(DBMS.equalsIgnoreCase("DB2") || DBMS.equalsIgnoreCase("AS400"))
		{
			SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
			dateFormat.setTimeZone(defaultTimeZone);
			return "TIMESTAMP('" + dateFormat.format(date)+"')";
		}else if(DBMS.equalsIgnoreCase("Oracle7"))
		{
			SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(defaultTimeZone);
			return "to_date('" + dateFormat.format(date)+"','YYYY-MM-DD HH24:MI:SS')";
		}else if(DBMS.equalsIgnoreCase("Informix"))
		{
			SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(defaultTimeZone);
			return "DATETIME(" + dateFormat.format(date)+") YEAR TO SECOND";
		}
		else
		{
			SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
			dateFormat.setTimeZone(defaultTimeZone);
			return "'" + dateFormat.format(date) + "'";
		}
	}

	public static boolean contains(BigDecimal []arr, BigDecimal obj)
	{
		if(obj == null)return false;
		double item = obj.doubleValue();
		for(int i = 0; i < arr.length; i++)
		{
			// Tenemos que comparar los doublevalues, porque si comparamos los BigDecimals
			// esta viniendo por ejemplo 1.0 contra 1.00 y da false!
			if(arr[i] != null && arr[i].doubleValue() == obj.doubleValue())
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(byte [] arr, double item)
	{
		for(int i = 0; i < arr.length; i++)if((double)arr[i] == item)return true;
		return false;
	}

	public static boolean contains(char [] arr, double item)
	{
		for(int i = 0; i < arr.length; i++)if((double)arr[i] == item)return true;
		return false;
	}

	public static boolean contains(short [] arr, double item)
	{
		for(int i = 0; i < arr.length; i++)if((double)arr[i] == item)return true;
		return false;
	}

	public static boolean contains(int [] arr, double item)
	{
		for(int i = 0; i < arr.length; i++)if((double)arr[i] == item)return true;
		return false;
	}

	public static boolean contains(long [] arr, double item)
	{
		for(int i = 0; i < arr.length; i++)if((double)arr[i] == item)return true;
		return false;
	}

	public static boolean contains(float [] arr, double item)
	{
		for(int i = 0; i < arr.length; i++)if((double)arr[i] == item)return true;
		return false;
	}

	public static boolean contains(double [] arr, double item)
	{
		for(int i = 0; i < arr.length; i++)if(arr[i] == item)return true;
		return false;
	}

	public static boolean contains(String []arr, String item)
	{
		if(item == null) return false;
		item = item.trim(); // Le hacemos trim() porque esta viniendo de la BD con espacios al final
		for(int i = 0; i < arr.length; i++)
		{
			if(arr[i] != null && arr[i].trim().equals(item))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(Object []arr, Object item)
	{
		for(int i = 0; i < arr.length; i++)
		{
			if(arr[i] != null && arr[i].equals(item))
			{
				return true;
			}
		}
		return false;
	}
	public static String format(String value, String v1, String v2, String v3, String v4, String v5, String v6, String v7, String v8, String v9)
	{
		String[] vs = {v1, v2, v3, v4, v5, v6, v7, v8, v9};
		StringBuffer stringBuilder = new StringBuffer();
		if (value != null && !value.equals(""))
		{
			StringTokenizer tokenizer = new StringTokenizer(value, "%", false);
			int lastIndex = 0;
			int index = 0;
			int idx = 0;
			while((index = value.indexOf('%', lastIndex)) != -1)
			{
				if(index > 0 && value.charAt(index - 1) == '\\')
			    {
					stringBuilder.append(value.substring(lastIndex, index-1));
					stringBuilder.append("%");
					lastIndex = index + 1;
				}
				else
				{
					try
					{
						stringBuilder.append(value.substring(lastIndex, index));
						lastIndex = index + 2;
						stringBuilder.append(vs[Integer.parseInt("" + value.charAt(index+1))-1]);
					}catch(NumberFormatException e)
					{ // Si el value tiene algo tipo %a, dejo el %a
						stringBuilder.append("%").append(value.charAt(index+1));
					}catch(StringIndexOutOfBoundsException e)
					{ // Si el value termina con un %, lo ignoro
						 lastIndex--;
					}
				}
			}
			stringBuilder.append(value.substring(lastIndex));
		}

		String res = stringBuilder.toString();
		if (value!=null && res.equals(value))
		{
			 //Parameters format {0}, {1}, ...
			 Object[] args = new Object[] {v1, v2, v3, v4, v5, v6, v7, v8, v9};
			 MessageFormat form = new MessageFormat(res);
			 res = form.format(args);
		}

		return res;
	}

	public static String getFileName( String sFullFileName)
	{
		String fileName = new File( sFullFileName).getName();
		int indexOf = fileName.lastIndexOf('.');
		if	(indexOf < 0)
			return fileName;
		return fileName.substring(0, indexOf);
	}

	public static String getFileType( String sFullFileName)
	{
		String fileName = new File( sFullFileName).getName();
		int indexOf = fileName.lastIndexOf('.');
		if	(indexOf < 0)
			return "";
		fileName = fileName.substring(indexOf + 1);

		indexOf = fileName.lastIndexOf('?');
		if (indexOf > 0)
			fileName = fileName.substring(0, indexOf);

		return fileName;
	}

	public static String getRelativeURL(String path)
	{
		return SpecificImplementation.GXutil.getRelativeURL(path);
	}

	public static String getRelativeBlobFile(String path)
	{
		return path.replace(com.genexus.ApplicationContext.getInstance().getServletEngineDefaultPath() + File.separator, "").replace(File.separator, "/");
	}

	public static final String FORMDATA_REFERENCE = "gxformdataref:";
	public static final String UPLOADPREFIX = "gxupload:";
	public static final int UPLOAD_TIMEOUT = 10;


	public static boolean isUploadPrefix(String value)
	{
		return value.startsWith(UPLOADPREFIX);
	}

	public static String dateToCharREST(Date value)
	{
		if ( nullDate().equals(value) )
		{
			return "0000-00-00";
		}
		else
		{
			SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
			return dateFormat.format(value);
		}
	}


	public static Date charToDateREST(String value)
	{
		try
		{
			if (value == null || value.equals("0000-00-00"))
			{
				return nullDate();
			}
			else
			{
				SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
				return dateFormat.parse(value);
			}
		}
		catch (ParseException ex)
		{
			return nullDate();
		}
	}


	public static boolean shouldConvertDateTime(Date value, boolean isRest)
	{
		return SpecificImplementation.GXutil.shouldConvertDateTime(value, isRest);
	}

	private static final double EPS = 0.000000000000001;

	public static Object testNumericType(Object obj, int type)
	{
		if(obj instanceof BigDecimal)
		{
			return obj;
		}
		if(!(obj instanceof Number))
		{
			throw new IllegalArgumentException(obj.getClass().getName() + " is not a numeric type");
		}
		double value = Math.abs(((Number)obj).doubleValue());
		boolean error = false;
		if(type != TypeConstants.FLOAT && type != TypeConstants.DOUBLE && (value - (long)value) > EPS)
		{
			throw new IllegalArgumentException("Numeric value " + ((Number)obj).doubleValue() + " exceeds storage class size");
		}

		switch(type)
		{
			case TypeConstants.BYTE:	if(value > Byte.MAX_VALUE) error = true; break;
			case TypeConstants.SHORT:   if(value > Short.MAX_VALUE) error = true; break;
			case TypeConstants.INT:		if(value > Integer.MAX_VALUE) error = true; break;
			case TypeConstants.LONG:	if(value > Long.MAX_VALUE) error = true; break;
			case TypeConstants.FLOAT:	if(value > Float.MAX_VALUE) error = true; break;
		}
		if(error)
		{
			throw new IllegalArgumentException("Numeric value " + ((Number)obj).doubleValue() + " exceeds storage class size");
		}
		return obj;
	}


				@SuppressWarnings("unchecked")
				public static boolean compare(Comparable operand1, String op, Comparable operand2)
				{
						int compareValue;
						if(operand1 instanceof String && operand2 instanceof String)
						{
							String sop1 = rtrim((String)operand1);
							String sop2 = rtrim((String)operand2);
							if(op.equals("like"))
								return like(sop1, padr(sop2, sop1.length(), "%"));
							else compareValue = sop1.compareTo(sop2);
						}else if(operand1.getClass().equals(operand2.getClass()))
							compareValue = operand1.compareTo(operand2);
						else if(operand1 instanceof Number && operand2 instanceof Number)
									compareValue = Double.compare(((Number)operand1).doubleValue(), ((Number)operand2).doubleValue());
						else compareValue = operand1.compareTo(operand2);

				    if(op.equals("="))return compareValue == 0;
				    else if(op.equals(">"))return compareValue > 0;
				    else if(op.equals(">="))return compareValue >= 0;
				    else if(op.equals("<"))return compareValue < 0;
				    else if(op.equals("<="))return compareValue <= 0;
				    else if(op.equals("<>"))return compareValue != 0;
				    else return compareValue == 0;
				}

  
 		public static String strUnexponentString(String num)
   		{
   			int epos = num.indexOf('E');
   			if	(epos == -1){
    			epos = num.indexOf('e');
   			}
   			if (epos != -1)
   			{
   				String exponent;
   				int scaleAdj = 0;
    			if (num.charAt(epos + 1) == '+')
    			{
     				// Skip '+' in exponent
     				exponent = num.substring(epos+2);
    			}
    			else
    			{
     				exponent = num.substring(epos+1);
    			}

    			scaleAdj = Integer.parseInt(exponent);
    			// Strip exponent
    			num = num.substring(0,epos);

   				int point = num.indexOf('.');
   				int scale = num.length() - (point == -1 ? num.length () : point + 1) - scaleAdj;

  				StringBuffer val = new StringBuffer(point == -1 ? num : num.substring(0, point) + num.substring (point + 1));

  				// correct for negative scale as per BigDecimal javadoc
  				for(; scale<0; scale++)
  				{
    				val.append("0");
   				}
				return val.toString();
  			}else
  			{
  				return num;
  			}

		}

        public static Class mapTypeToClass(int type)
        {
            switch(type)
            {
                  case TypeConstants.SHORT:
                      return Short.class;
                  case TypeConstants.BYTE:
                      return Byte.class;
                  case TypeConstants.INT:
                      return Integer.class;
                  case TypeConstants.DOUBLE:
                      return Double.class;
                  case TypeConstants.FLOAT:
                      return Float.class;
                  case TypeConstants.LONG:
                      return Long.class;
                  case TypeConstants.BOOLEAN:
                      return Boolean.class;
                  case TypeConstants.DATE:
                      return java.util.Date.class;
                  case TypeConstants.DECIMAL:
                      return java.math.BigDecimal.class;
                  case TypeConstants.UUID:
                      return java.util.UUID.class;
            }
            return String.class;
        }
      

	public static boolean toBoolean(int value)
	{
		return (value != 0);
	}


	public static String replaceLast(String string, String toReplace, String replacement) {
		int pos = string.lastIndexOf(toReplace);
		if (pos > -1) {
			return string.substring(0, pos)
				 + replacement
				 + string.substring(pos + toReplace.length(), string.length());
		} else {
			return string;
		}
	}


	public static java.util.UUID strToGuid(String value)
	{
		if (value.length() == 0)
		{
			return java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");
		}
		try
		{
			return java.util.UUID.fromString(value);
		}
		catch(IllegalArgumentException e)
		{
			return new UUID(0,0);
		}
	}

	public static boolean checkSignature( String sgn, String txt)
	{
		return sgn.endsWith(getMD5Hash( txt));
	}


	public static String getMD5Hash( String s)
	{
		return getHash(s, "MD5");
	}

	public static String getHash( String s)
	{
		return getHash(s, "SHA1");
	}

	public static String getHash(String s, String hashAlgorithm)
	{
		MessageDigest md = null;
		byte[] bytesOfMessage = null;
		try
		{
			bytesOfMessage = s.getBytes("ASCII");
			md = MessageDigest.getInstance(hashAlgorithm);
		}
		catch (Exception ex)
		{
			logger.error("getHash - " + s + " - " + hashAlgorithm + " - " +  ex.toString());
		}
		if (bytesOfMessage != null)
		{
			byte[] data = md.digest(bytesOfMessage);
			BigInteger number = new BigInteger(1, data);
			return number.toString(16);
		}
		return "";
	}

	public static boolean isAbsoluteURL(String url)
	{
		return url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://") || url.startsWith("sd:");
	}

	public static boolean hasUrlQueryString(String url)
	{
		return url.indexOf("?") >= 0;
	}

	public static String encodeJSON(String in)
	{
		String encoded = JSONObject.quote(in).replaceAll("'", "\\\\u0027");
		return encoded.substring(1, encoded.length() - 1);
	}

	public static String removeDiacritics(String s)
	{
		return SpecificImplementation.GXutil.removeDiacritics(s);
	}

	public static String CssPrettify(String uglyCSS)
	{
		return CSSHelper.Prettify(uglyCSS);
	}

	public static int getColor(int r, int g, int b)
	{
		return (r * 256 * 256 ) + (g * 256) + b;
	}

	public static <T> T[] concatArrays(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}


	public static String pagingSelect(String select)
	{
		String pagingSelect = ltrim(select);
		// Quita distinct
		if(pagingSelect.startsWith("DISTINCT"))
			pagingSelect = pagingSelect.substring(9);
		// Renombra referencias a tablas por GXICTE
		pagingSelect = pagingSelect.replaceAll("T\\d+\\.", "GX_ICTE.");
		return pagingSelect;
	}

	public static Object convertObjectTo(Object obj, Class toClass, boolean fail) throws Exception {
        String objStr = obj.toString();
        String className = toClass.getName();

        if (obj instanceof Boolean && !className.equals("boolean") && !className.equals("java.lang.Boolean"))
        {
        	objStr = (Boolean.valueOf(objStr).booleanValue()) ? "1" : "0";
        }
        //Parameters in URL are always in Invariant Format

        if (className.equals("short") || className.equals("java.lang.Short") || className.equals("[S"))
        {
            try
            {
           		if (objStr.isEmpty())
					objStr = "0";
				else 
				{
					int i = objStr.indexOf(".");
					if (i >= 0)
            			objStr =  objStr.substring(0, i);  
				}         	
                return Short.valueOf(objStr);
            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new Short((short)0);
            }
        }
        if (className.equals("byte") || className.equals("java.lang.Byte") || className.equals("[B"))
        {
            try
            {
				if (objStr.isEmpty())
					objStr ="0";
				else 
				{
					int i = objStr.indexOf(".");
					if	(i >= 0)
            			objStr =  objStr.substring(0, i);  
				}               	              	
                return Byte.valueOf(objStr);
            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new Byte((byte)0);
            }
        }
        else if (className.equals("int") || className.equals("java.lang.Integer") || className.equals("[I"))
        {
            try
            {
            	if (objStr.isEmpty())
					objStr ="0";
				else 
				{
					int i = objStr.indexOf(".");
					if	(i >= 0)
            			objStr =  objStr.substring(0, i);  
				}       	
                return new Integer(objStr);
            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new Integer(0);
            }
        }
        else if (className.equals("string") || className.indexOf("java.lang.String") != -1)
        {
            return objStr;
        }
        else if (className.equals("double") || className.equals("java.lang.Double") || className.equals("[D"))
        {
            try
            {
            	if (objStr.isEmpty())
            		objStr = "0";                	
                return Double.valueOf(objStr);
            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new Double(0);
            }
        }
        else if (className.equals("float") || className.equals("java.lang.Float") || className.equals("[F"))
        {
            try
            {
            	if (objStr.isEmpty())
            		objStr = "0";                	
                return Float.valueOf(objStr);
            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new Float(0);
            }
        }
        else if (className.equals("long") || className.equals("java.lang.Long") || className.equals("[J"))
        {
            try
            {
            	
				if (objStr.isEmpty())
					objStr ="0";
				else 
				{
					int i = objStr.indexOf(".");
					if	(i >= 0)
            			objStr =  objStr.substring(0, i);  
				}
                return Long.valueOf(CommonUtil.strUnexponentString(objStr));

            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new Long(0);
            }
        }
        else if (className.equals("boolean") || className.equals("java.lang.Boolean") || className.equals("[Z"))
        {
            try
            {
                return Boolean.valueOf(objStr);
            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new Boolean(false);
            }
        }
        else if (className.indexOf("java.math.BigDecimal") != -1)
        {
            try
            {
            		if (objStr.isEmpty())
            			objStr = "0";                	
                return DecimalUtil.stringToDec(objStr);
            }
            catch(Exception e)
            {
                if (fail)
                    throw e;
                return new java.math.BigDecimal(0);
            }
        }
        else if (className.indexOf("java.util.Date") != -1)
        {
            try {
				if (objStr.indexOf('/')>0) //Json CallAjax
					return LocalUtil.getISO8601Date(objStr);
				else
					return new java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz Z").parse(objStr);
            } catch (ParseException ex) {
                return nullDate();
            }
        }
        else if(className.indexOf("java.util.UUID") != -1)
        {
			try {
				return UUID.fromString(objStr);
			}catch(IllegalArgumentException e)
			{
				if(fail)
					throw e;
				return new UUID(0,0);
			}
        }
        Object objSerializable = SpecificImplementation.GXutil.convertObjectTo(toClass, objStr);
        if (objSerializable != null)
        	return objSerializable;
        return obj;
	}

	public static String str(long val, int digits, int decimals) {
		if	(decimals == 0)
		{
			String sVal = Long.toString(val);

			if	(sVal.length() > digits)
				return CommonUtil.replicate("*", digits);
			else
				return padl(sVal, digits, " ");
		}

		return str((double) val, digits, decimals);
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
	
	public static String str(double value, int length, int decimals)
	{
		return str(DecimalUtil.unexponentString(Double.toString(value)), length, decimals, true);
	}

	public static String strNoRound(double value, int length, int decimals)
	{
		return str(DecimalUtil.unexponentString(Double.toString(value)), length, decimals, false);
	}


	static String str(BigDecimal value, int length, int decimals, boolean round) {
			if	(length - 1 <= decimals)
			{
				// Esto es que hizo str(_, 2, 1) o str(_, 3, 2), todas cosas
				// invalidas que implican que decimals = 0
				decimals = 0;
			}

			if	(round)
			{
				value = CommonUtil.roundDecimal(value, decimals, BigDecimal.ROUND_HALF_UP);
			}
			else
			{
				value = CommonUtil.roundDecimal(value, decimals, BigDecimal.ROUND_DOWN);
			}

			String base = value.toString();
			String decimalStr = "";
			int decimalPos = base.indexOf('.');

			if	(decimals > 0 && base.indexOf('E') == -1)
			{
				if	(decimalPos  < 0)
				{
						base += "." + replicate("0", decimals);
				}
				else
				{
					base = (decimalPos == 0?"0":base.substring(0, decimalPos)) + padr(base.substring(decimalPos), decimals + 1, "0");
				}
			}

			// Aca el numero tiene el valor redondeado y tiene los decimales correctos. Ahora
			// hay que empezar a achicarlo si no entra en el espacio indicado.

			if	(base.length() <= length)
			{
				return padl(base, length, " ");
			}

			decimalPos = base.indexOf('.');
			if	(decimalPos > 0)
			{
				String integerPart = base.substring(0, decimalPos);
				if	(integerPart.length() <= length)
				{
				if (SpecificImplementation.KeepDecimals) {
					int decimalsToKeep = Math.max(0, length-integerPart.length()-1);
					decimalsToKeep = Math.min(decimals, decimalsToKeep);
					return str(decimalVal(base, "."), length, decimalsToKeep, round);
				}
				else
					return str(decimalVal(base, "."), length, 0, round);
				}
	 		}

			return replicate("*", length);
		}

	public static double round(double in, int decimals) {
		return roundDecimal(DecimalUtil.unexponentString(Double.toString(in)), decimals).doubleValue();
	}

	public static Object convertObjectTo(Object obj, Class toClass) throws Exception{
		return convertObjectTo(obj, toClass, true);
	}

	public static Object convertObjectTo(Object obj, int type) {
		  Class cls = mapTypeToClass(type);
          try
          {
              return convertObjectTo(obj, cls, false);
          }
          catch (Exception e)
          {
              return obj;
          }
	}

	public static String trimSpaces(String text) {
		String trimText  = text.trim();
		String stringRet = "";

		int i = trimText.indexOf(' ',1);

		while ( i != -1 )
		{
			stringRet += trimText.substring(0, i);
			trimText = trimText.substring(i + 1, trimText.length()).trim();
			i = trimText.indexOf(' ', 1);		
		}

		return (stringRet + trimText);
	}

	public static boolean isTime(String text) {
		if (((! in(text,':') )&& text.length() > 2) || text.length() > 8)
			return (false);
		else
		{
			text = delete(text,':');
			if (text.length() == 0 )
				return (true);
			if (text.charAt(0) > '2')
				return (false);
			for (int i = 0; i < text.length() ; i = i + 2)
				if (!Character.isDigit(text.charAt(i)) || text.charAt(i) > '5')
					return (false);
		}
		return (true);	
	}
	
	public static String delete(String text,char del)
	{
		return (CommonUtil.trimSpaces(text.replace(del,' ')));
	}

	public static String addLastChar(String dir, String lastChar) {
		if	(!dir.equals("") && !dir.endsWith(lastChar))
			return dir + lastChar;
		
		return dir;
	}

	public static String addLastPathSeparator(String dir) {
		return CommonUtil.addLastChar(dir, File.separator);
	}

	public static String quoteString(String in, boolean entities8bit, boolean encodeQuotes) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < in.length(); i++)
		{
			char currentChar = in.charAt(i);

			switch (currentChar)
			{
				case (char) 34:
					if (encodeQuotes)
					{
						out.append("&quot;");
					}
					else
					{
						out.append( (char) currentChar);
					}
					break;
				case (char) 38:
					out.append("&amp;");
					break;
				case (char) 60:
					out.append("&lt;");
					break;
				case (char) 62:
					out.append("&gt;");
					break;
				case (char) '\'':
					out.append("&apos;");
					break;
				default:
					if	(entities8bit && currentChar > (char) 127)
					{
						 out.append("&#" + ((int) currentChar) + ";");
					} 
					else
					{
						out.append( (char) currentChar);
					}
			}
		}

		return out.toString();
	}

	public static byte[] readToByteArray(InputStream in) throws IOException
	{
     	byte[] output    = new byte[0];
     	byte[] newBuffer = new byte[2048];
		int size;
		
		while ((size = readFully(in, newBuffer, 0, 2048)) > 0)
		{
	    	byte tmpBuffer[] = new byte[output.length + size];

			// Paso del realbuffer al temp
		    System.arraycopy(output, 0, tmpBuffer, 0, output.length);

			// Paso del nuevo al temp
		    System.arraycopy(newBuffer, 0,  tmpBuffer, output.length, size);

			// Dejo el temp en realBuffer
			output = tmpBuffer;
		}

		return output;
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
	
	/**
 	A fixed version of java.io.InputStream.read(byte[], int, int).  The
 	standard version catches and ignores IOExceptions from below.
 	This version sends them on to the caller.
*/

	public static int read( InputStream in, byte[] b, int off, int len ) throws IOException
    {
	    if ( len <= 0 )
	        return 0;
	    int c = in.read();
	    if ( c == -1 )
	        return -1;
	    if ( b != null )
	        b[off] = (byte) c;
	    int i;
	    for ( i = 1; i < len ; ++i )
	        {
	        c = in.read();
	        if ( c == -1 )
	            break;
	        if ( b != null )
	            b[off + i] = (byte) c;
	        }
	    return i;
    }
	
	public final static String URLEncode(String parm, String encoding)
    {
		try {
			if (encoding.equals(""))
				return java.net.URLEncoder.encode(parm, "UTF8");
			else
				return java.net.URLEncoder.encode(parm, encoding);
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			System.out.println("URLEncoder unsupported encoding: " + encoding);
			return "";
		}
    }
	
	public static byte remoteFileExists(String URLName){
	    try {	      
	      HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK)?(byte)1:0;
	    }
	    catch (Exception e) {	       
	       return 0;
	    }
	  }

    /** A version of read that reads the entire requested block, instead
     	of sometimes terminating early.
     	@return -1 on EOF, otherwise len
	*/

    public static int readFully( InputStream in, byte[] b, int off, int len ) throws IOException
    {
		int l, r;

		for ( l = 0; l < len; )
	    {
	    	r = read( in, b, l, len - l );
	    	if ( r == -1 )
				return l;

	    	l += r;
	    }

		return len;
	}


	public static byte fileExists(String fileName) {
		if (!fileName.startsWith("http")){
			return new File(fileName).exists()? (byte)1:0;
		}
		else
		{
			return CommonUtil.remoteFileExists(fileName);
		}

	}

    private static Boolean isWindows;

	public static boolean isWindows() {
		// Chequeo que la property 'os.name' tenga 'indows'... NOTA: Es 'indows' y NO 'windows'
        // porque sino el index puede ser 0, por ejemplo para 'os.name=Windows 2000'
        if(isWindows == null)
        {
            isWindows = (System.getProperty("os.name", "NONE").toLowerCase().indexOf("windows") >= 0);
        }
        return isWindows.booleanValue();	
    }


	public static String getStackTraceAsString(Throwable e) {
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(bytes, true);
	    e.printStackTrace(writer);
	    return bytes.toString();
	}

	public static String getClassName(String pgmName) {
		// Esta la usa el developerMenu, que saca el package del client.cfg
		String classPackage = SpecificImplementation.Application.getClientPreferences().getPACKAGE();

		if	(!classPackage.equals(""))
			classPackage += ".";

		return classPackage + pgmName.replace('\\', '.').trim();
	}
}
