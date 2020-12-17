package com.genexus;

import java.math.RoundingMode;
import java.util.*;
import java.text.*;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.GXSimpleDateFormat;
public class LocalUtil
{
	private static final int ANSI = 0;
	private static final int YMD  = 1;
	private static final int MDY  = 2;
	private static final int DMY  = 3;

	private static final int MDY4 = 4;
	private static final int DMY4 = 5;
	private static final int Y4MD = 6;

	private static final String GXLANG_SPA = "spa";
	private static final String GXLANG_ENG = "eng";
	private static final String GXLANG_ITA = "ita";
	private static final String GXLANG_POR = "por";
	private static final String GXLANG_CN  = "chs";
	private static final String GXLANG_CT  = "cht";
	private static final String GXLANG_TW  = "tw";
	private static final String GXLANG_JAP  = "jap";

	private static final String JLANG_SPA  = "es";
	private static final String JLANG_ENG  = "en";
	private static final String JLANG_ITA  = "it";
	private static final String JLANG_POR  = "pt";
	private static final String JLANG_CN   = "cn";
	private static final String JLANG_CT   = "ct";
	private static final String JLANG_TW   = "tw";
	private static final String JLANG_JP   = "ja";

	private static Hashtable<String, LocalUtil> listLocalUtil = new Hashtable<>();

	private GXSimpleDateFormat ttoc_2_df10;
	private GXSimpleDateFormat ttoc_2_df8 ;
	private GXSimpleDateFormat ttoc_1_df  ;
	private GXSimpleDateFormat ttoc_3_5_df;
	private GXSimpleDateFormat ttoc_3_2_df;
	private GXSimpleDateFormat ttoc_3_8_df;
	private GXSimpleDateFormat ttoc_3_12_df;
	private GXSimpleDateFormat dtoc_df;
	//private GXSimpleDateFormat time_df;
	private GXSimpleDateFormat millisecond_df;
	private GXSimpleDateFormat second_df;
	private GXSimpleDateFormat minute_df;
	private GXSimpleDateFormat hour_df;
	private GXSimpleDateFormat day_df;
	private GXSimpleDateFormat month_df;
	private GXSimpleDateFormat year_df;
	private GXSimpleDateFormat dateOk_df;

	private TimeZone 			defaultTimeZone;
	private Locale	 			defaultLocale;

	private GregorianCalendar 	ymdhmstot_gc;
	private DecimalFormat     	str_df;

	private Calendar			ddiff_cal1, ddiff_cal2;
	private Locale			 	appLocale;
	private GregorianCalendar 	appGregorianCalendar;

	public  char 	 decimalPoint;
	private String 	 dateFormat;
	private String 	 timeFormat;
	public  int 	 firstYear2K;
	private String 	 language;
	private Locale 	 locale;
	private Messages msgs;
	private char dateSeparator;

	private boolean  startsYearDate = false;

	public static LocalUtil getLocalUtil(char decimalPoint, String dateFormat, String timeFormat, int firstYear2K, String language)
	{
		String key = getKey(decimalPoint, dateFormat, timeFormat, firstYear2K, language);

		LocalUtil ret = listLocalUtil.get(key);

		if	(ret == null)
		{
			ret = new LocalUtil(decimalPoint, dateFormat, timeFormat, firstYear2K, language);
			listLocalUtil.put(key, ret);
		}

		return ret;
	}

	public String 	 _dateFormat;
	public String 	 _timeFormat;
	public String 	 _language;

	public LocalUtil(char decimalPoint, String dateFormat, String timeFormat, int firstYear2K, String language)
	{
		this.decimalPoint = decimalPoint ;

		_language = language;
		this.language	  = getLANGUAGE(language.toLowerCase());

		_dateFormat = dateFormat;
		this.dateFormat	  =	mapCTODFormatToPattern(mapDateFormat(dateFormat), "/", false);
		if(this.dateFormat.substring(0,2).equalsIgnoreCase("YY"))
		{
			startsYearDate = true;
		}

		_timeFormat = timeFormat;
		this.timeFormat	  = getTIME_FMT(timeFormat);

		this.firstYear2K  = firstYear2K  ;

		initialize();
	}

	public static void endLocalUtil()
	{
		listLocalUtil = null;
	}

	public String getTimeFormat()
	{
		return timeFormat;
	}

    public String getDateFormat()
    {
		return dateFormat;
    }

	public static String getKey(char decimalPoint, String dateFormat, String timeFormat, int firstYear2K, String language)
	{
		return dateFormat + timeFormat + language + decimalPoint + firstYear2K;
	}

	public Messages getMessages()
	{
		return msgs;
	}

	private String getLANGUAGE(String language)
	{
		if (language.equalsIgnoreCase(GXLANG_SPA))
			return JLANG_SPA;
		else if (language.equalsIgnoreCase(GXLANG_ITA))
			return JLANG_ITA;
		else if (language.equalsIgnoreCase(GXLANG_POR))
			return JLANG_POR;
		else if (language.equalsIgnoreCase(GXLANG_CN))
			return JLANG_CN;
		else if (language.equalsIgnoreCase(GXLANG_TW))
			return JLANG_TW;
		else if (language.equalsIgnoreCase(GXLANG_CT))
			return JLANG_CT;
		else if (language.equalsIgnoreCase(GXLANG_JAP))
			return JLANG_JP;
		else if (language.equalsIgnoreCase(GXLANG_ENG))
			return JLANG_ENG;
		else
		{
			String lng;

			lng = SpecificImplementation.LocalUtil.getLanguage(language);

			if (lng != null && lng.length() >= 2)
				return lng.toLowerCase().substring(0, 2);
			else
				return language;
		}
	}

	public String getLanguageID(String lang)
	{
		//lang = GXutil.lower(lang); Se utiliza el id con el casing original.

		if (CommonUtil.rtrim(lang).equals(""))
			lang = language;

		return getLANGUAGE(lang);
	}

	private String getTIME_FMT(String aux)
	{
		if 	  	(aux.equals("12") || (aux.equals("") && language.equals(JLANG_ENG)))
			return ("hh:mm:ss a");
		else
		 	return ("HH:mm:ss");
	}

	public int mapTimeFormat(String dateFmt)
	{
		if (dateFmt.equals("12")) return 1;
		else return 0;
	}

	public int mapDateTimeFormat(String dateFmt)
	{
		if (dateFmt.equalsIgnoreCase("ANSI"))
			return ANSI;
		else if (dateFmt.equalsIgnoreCase("MDY") || dateFmt.equalsIgnoreCase("MDY4"))
			return MDY;
		else if (dateFmt.equalsIgnoreCase("DMY") || dateFmt.equalsIgnoreCase("DMY4"))
			return DMY;
		else if (dateFmt.equalsIgnoreCase("YMD") || dateFmt.equalsIgnoreCase("Y4MD"))
			return YMD;
		else return MDY;
	}

	public int mapDateFormat(String dateFmt)
    {		//Same mapping used in map_date_format_l/prolog.
        	if (dateFmt.equalsIgnoreCase("ANSI"))
                    return ANSI;
            else if (dateFmt.equalsIgnoreCase("MDY"))
                    return MDY;
            else if (dateFmt.equalsIgnoreCase("DMY"))
                    return DMY;
            else if (dateFmt.equalsIgnoreCase("MDY4"))
                    return MDY4;
            else if (dateFmt.equalsIgnoreCase("DMY4"))
                    return DMY4;
            else if (dateFmt.equalsIgnoreCase("YMD"))
                    return YMD;
            else if (dateFmt.equalsIgnoreCase("Y4MD"))
                    return Y4MD;
    		else return DMY4;
    }

	private static String mapCTODFormatToPattern(int format)
	{
		switch (format)
		{
			case MDY	:
					return "MM/dd/yy";
			case ANSI	:
					return "yyyy-MM-dd";
			case DMY  	:
					return "dd/MM/yy";
			case MDY4 	:
					return "MM/dd/yyyy";
			case DMY4 	:
					return "dd/MM/yyyy";
			case YMD  	:
					return "yy/MM/dd";
			case Y4MD 	:
					return "yyyy/MM/dd";
		}
		throw new InternalError("Bad Format for DTOC " + format);
	}

	private static String mapCTODFormatToPattern(int format, String dSep, boolean longYear)
	{
		switch (format)
		{
			case DMY  	:
					return "dd" + dSep + "MM" + dSep + "yy" + (longYear?"yy":"");
			case MDY	:
					return "MM" + dSep + "dd" + dSep + "yy" + (longYear?"yy":"");
			case YMD  	:
					return "yy" + (longYear?"yy":"") + dSep + "MM" + dSep + "dd";
			case DMY4 	:
					return "dd" + dSep + "MM" + dSep + "yyyy";
			case MDY4 	:
					return "MM" + dSep + "dd" + dSep + "yyyy";
			case Y4MD 	:
					return "yyyy" + dSep + "MM" + dSep + "dd";
			case ANSI	:
					return "yy" + (longYear?"yy":"") + dSep + "MM" + dSep + "dd";
		}

		throw new InternalError("Bad Format for DTOC " + format);
	}

	private static String getNullDateFormat(String date, int gxformat)
	{

		String format;
		String separator = getSeparator(date);
		if (!separator.isEmpty())
		{
			format = mapCTODFormatToPattern(gxformat, separator, hasLongYear(date));
		}
		else
		{
			format = mapCTODFormatToPattern(gxformat);
		}
		StringBuffer out = new StringBuffer();

		int len = format.length();
		for (int i = 0; i < len; i++)
		{
			out.append(format.charAt(i) == '/'?'/':' ');
		}

		return out.toString();
	}

	private void initialize()
	{
		String country     = decimalPoint == '.'? "US" : "ES";
		defaultTimeZone = TimeZone.getDefault();

		ttoc_2_df10 = new GXSimpleDateFormat(dateFormat.substring(0, 8) + "yy");
		ttoc_2_df10.setTimeZone(defaultTimeZone);

		ttoc_2_df8  = new GXSimpleDateFormat(dateFormat.substring(0, 8));
		ttoc_2_df8.setTimeZone(defaultTimeZone);

		ttoc_1_df = new GXSimpleDateFormat( dateFormat + ' ' + timeFormat);
		ttoc_1_df.setTimeZone(defaultTimeZone);

		ttoc_3_5_df = new GXSimpleDateFormat("HH:mm");
		ttoc_3_5_df.setTimeZone(defaultTimeZone);

		ttoc_3_2_df = new GXSimpleDateFormat("HH");
		ttoc_3_2_df.setTimeZone(defaultTimeZone);

		ttoc_3_8_df = new GXSimpleDateFormat("HH:mm:ss");
		ttoc_3_8_df.setTimeZone(defaultTimeZone);

		ttoc_3_12_df = new GXSimpleDateFormat("HH:mm:ss.SSS");
		ttoc_3_12_df.setTimeZone(defaultTimeZone);
		//time_df = new GXSimpleDateFormat("HH:mm:ss");
		//time_df.setTimeZone(defaultTimeZone);

		defaultLocale = Locale.getDefault();
		ymdhmstot_gc  = new GregorianCalendar(defaultLocale);
		ymdhmstot_gc.setTimeZone(TimeZone.getDefault());

		appLocale     = new Locale (language, country);
		appGregorianCalendar = new GregorianCalendar(appLocale);
		appGregorianCalendar.setTimeZone(defaultTimeZone);

		str_df = (DecimalFormat) getNumberFormat(Locale.US);

		dtoc_df = new GXSimpleDateFormat(dateFormat);
		dtoc_df.setTimeZone(defaultTimeZone);

		ddiff_cal1 = Calendar.getInstance(defaultLocale);
		ddiff_cal1.setTimeZone(defaultTimeZone);

		ddiff_cal2 = Calendar.getInstance(defaultLocale);
		ddiff_cal2.setTimeZone(defaultTimeZone);

		day_df    = new GXSimpleDateFormat("d");
		day_df.setTimeZone(defaultTimeZone);

		year_df   = new GXSimpleDateFormat("yyyyy");
		year_df.setTimeZone(defaultTimeZone);

		month_df  = new GXSimpleDateFormat("M");
		month_df.setTimeZone(defaultTimeZone);

		hour_df   = new GXSimpleDateFormat("k");
		hour_df.setTimeZone(defaultTimeZone);

		minute_df = new GXSimpleDateFormat("m");
		minute_df.setTimeZone(defaultTimeZone);

		second_df = new GXSimpleDateFormat("s");
		second_df.setTimeZone(defaultTimeZone);

		millisecond_df = new GXSimpleDateFormat("S");
		millisecond_df.setTimeZone(defaultTimeZone);

		dateOk_df = new GXSimpleDateFormat(dateFormat.length()<10?dateFormat + "yy":dateFormat);
		dateOk_df.setTimeZone(defaultTimeZone);

		/*Locale currentLocale;
		if	(language.equals(JLANG_ENG))
		{
			currentLocale = new Locale (language, "US");
		}
		else if(language.equals(JLANG_CN) ||
				language.equals(JLANG_TW) ||
				language.equals(JLANG_CT))
		{
			currentLocale = new Locale ("zh", language.toUpperCase());
		}
		else
		{
			currentLocale = new Locale (language, language.toUpperCase());
		}*/

                String resourceName = "messages." + _language + ".txt";

		if (SpecificImplementation.Application.getModelContextPackageClass() != null)
		{
			msgs = Messages.getMessages(resourceName, getLocale());
		}
		dateSeparator = dateFormat.charAt(2);
	}

	private String getCountry()
	{
		return decimalPoint == '.'? "US" : "ES";
	}

	public Locale getLocale()
	{
		if	(locale == null)
		{
			if	(decimalPoint == ',')
				locale = new Locale("es", getCountry());
			else
				locale = new Locale("en", getCountry());
		}

		return locale;
	}

	private Locale getMessagesLocale()
	{
		if	(language.equals(JLANG_ENG))
			return new Locale (language, "US");

		if	(language.equals(JLANG_CN))
			return Locale.SIMPLIFIED_CHINESE;

		if	(language.equals(JLANG_TW))
			return Locale.TRADITIONAL_CHINESE;

		return new Locale (language, language.toUpperCase());
	}

	public final String ttoc(Date d1)
	{
		Date d = CommonUtil.ymdhmsToT_noYL(2001, 1, 1, 0, 30, 0);
		if	(d.equals(CommonUtil.nullDate()))
			return justSeparators(ttoc_1_df.toPattern());

		return (ttoc_1_df.gxFormat(d).trim());
	}

	private String justSeparators(String in)
	{
		int len = in.length();
		StringBuffer out = new StringBuffer(len);
		for (int i = 0; i < len; i++)
		{
			char val = in.charAt(i);
			if (!getBLANK_EMPTY_DATE() && (val == '-' || val == '/' || val == ':'))
			{
				out.append(val);
			}
			else
			{
				out.append(' ');
			}
		}

		return out.toString();
	}

	static boolean getBLANK_EMPTY_DATE()
	{
		return SpecificImplementation.LocalUtil.IsBlankEmptyDate();
	}

	protected String ttoc(Date d , int M)
	{
		switch (M)
		{
			case 0:
				return "";
			case 8 :
				if	(d.equals(CommonUtil.nullDate()))
					return justSeparators(ttoc_2_df8.toPattern());

				return ttoc_2_df8.gxFormat(d);
		}

		if	(d.equals(CommonUtil.nullDate()))
			return justSeparators(ttoc_2_df10.toPattern());

		return ttoc_2_df10.gxFormat(d);
	}

	public String getDateFormatPattern(int dateLength, int timeLength, int ampm, int format, String dSep, String tSep, String dtSep   ){
		String dateFormatText = mapCTODFormatToPattern(format, dSep, dateLength > 8);
		String timeFormatText = getTimeFormatPattern(timeLength, ampm,format, tSep);

		return  (dateLength > 0?dateFormatText:"") +
				(dateLength > 0 && timeLength > 0?dtSep:"") +
				(timeLength > 0?timeFormatText:"");
	}

	private String getTimeFormatPattern(int timeLength, int ampm, int format, String tSep  ){
		String formatHour  = ampm == 0?"HH":"hh";
		String formatMin   = "mm";
		String formatSec   = "ss";
		String formatMil   = "SSS";
		String milSep      = ".";
		String formatAMPM  = ampm == 0?""  :" a";

		String timeFormatText = formatHour;

		if	(timeLength > 2)
			timeFormatText += tSep + formatMin;

		if 	(timeLength > 5)
			timeFormatText += tSep + formatSec;

		if	(timeLength > 8)
			timeFormatText += milSep + formatMil;

		timeFormatText += formatAMPM;

		return timeFormatText;
	}

	static String formatEmptyDate(String pic)
	{
		String emptyDT = pic.replaceAll("d|M|y", " ");

		if (getBLANK_EMPTY_DATE())
		{
			emptyDT = emptyDT.replaceAll("/|:|m|s|S|\\.", " ");
			if (emptyDT.indexOf('a') == -1 )	// 24 horas
			{
				emptyDT = emptyDT.replaceAll("h|H", " ");
			}
			else								// 12 horas
			{
				emptyDT = emptyDT.replaceAll("hh|HH|a", "  ");
			}
		}
		else
		{
			emptyDT = emptyDT.replaceAll("m|s|S", "0");
			if (emptyDT.indexOf('a') == -1 )	// 24 horas
			{
				emptyDT = emptyDT.replaceAll("h|H", "0");
			}
			else								// 12 horas
			{
				emptyDT = emptyDT.replaceAll("hh|HH", "12");
				emptyDT = emptyDT.replaceAll("a", "AM");
			}
		}
		return emptyDT;
	}

	public String ttoc(Date d, int dateLength, int timeLength, int ampm, int format, String dSep, String tSep, String dtSep)
	{
		String dFormatText = mapCTODFormatToPattern(format, dSep, dateLength > 8);
		String timeFormatText = getTimeFormatPattern(timeLength, ampm, format, tSep);
		GXSimpleDateFormat dateFormat = new GXSimpleDateFormat(dFormatText);
		GXSimpleDateFormat timeFormat = new GXSimpleDateFormat(timeFormatText);
		String sep = (dateLength > 0 && timeLength > 0 ? dtSep : "");

		if	(d.equals(CommonUtil.nullDate()))
		{
			if (dateLength == 0)
				dFormatText = "";
			return  formatEmptyDate(dFormatText + sep + timeFormatText);
		}

		return  (dateLength > 0?dateFormat.gxFormat(d):"") +
				(dateLength > 0 && timeLength > 0?dtSep:"") +
				(timeLength > 0?timeFormat.gxFormat(d):"");
	}
/*
		if	(M == 10 && N == 8)
		{
			dateFormat.setTimeZone(defaultTimeZone);
			timeFormat.setTimeZone(defaultTimeZone);
			return dateFormat.gxFormat(d) + dtSep + timeFormat.gxFormat(d);
		}

		return ttoc(d , M , N);

	}
*/

	public String ttoc(Date d , int M , int N)
	{
		if ( M == 0 && N == 0 )
		{
			M = 10 ;
			N =  8 ;
		}

		String day = ttoc(d, M);

		switch (N)
		{
			case 0 :
				return CommonUtil.ltrim(day) ;
			case 2 :
				if	(d.equals(CommonUtil.nullDate()))
					return CommonUtil.ltrim(day + ' ' + justSeparators(ttoc_3_2_df.toPattern()));
				return CommonUtil.ltrim(day + ' ' + ttoc_3_2_df.gxFormat(d));
			case 5 :
				if	(d.equals(CommonUtil.nullDate()))
					return CommonUtil.ltrim(day + ' ' + justSeparators(ttoc_3_5_df.toPattern()));
				return CommonUtil.ltrim(day + ' ' + ttoc_3_5_df.gxFormat(d));
			case 12 :
				if	(d.equals(CommonUtil.nullDate()))
					return CommonUtil.ltrim(day + ' ' + justSeparators(ttoc_3_12_df.toPattern()));
				return CommonUtil.ltrim(day + ' ' + ttoc_3_12_df.gxFormat(d));
		}


		if	(d.equals(CommonUtil.nullDate()))
			return CommonUtil.ltrim(day + ' ' + justSeparators(ttoc_3_8_df.toPattern()));

		return CommonUtil.ltrim(day + ' ' + ttoc_3_8_df.gxFormat(d));
	}

	public String getYear(String year)
	{
		if (year.length() == 3 || year.length() == 1)
			year = year + '0';
		else
			if (year.length() == 0)
				year = "90";

		if (CommonUtil.val(year) <  firstYear2K)
			year = "20" + year;
		else
			year = "19" + year;
		return (year);
	}

	public String getDate(String dateIn)
	{
		Date d ;
		String dateFormat = this.dateFormat.length() < 10 ? this.dateFormat + "yy" : this.dateFormat;

		int len   = dateIn.length();
		String da = CommonUtil.trimSpaces(dateIn);

		if (len == 10 && da.length() == 8 )
		{
			dateIn = da;
			len = 8;
		}

		if (da.equals("--") || da.equals("//"))
		{
			char c = da.charAt(0);
			dateIn = "01" + c + "01" + c  + "90" ;
		}

		if (len == 8 )
		{
			String  year = CommonUtil.right(dateIn,2);

			if (CommonUtil.val(year) <  firstYear2K)
				year = "20" + year;
			else
				year = "19" + year;

			dateIn = CommonUtil.left(dateIn,6) + year;
		}
		else if (len == 10)
		{
			if (dateFormat.length() == 8)
			{
				dateIn = CommonUtil.left(dateIn,6) + CommonUtil.right(dateIn,2);
			}
		}

		return dateIn;
	}

	public boolean dateOk(String dateIn)
	{
		Date d ;

		String 	date = CommonUtil.trimSpaces(dateIn);
		if (dateIn.length() > 8 && dateIn.charAt(8) == ' ')
		{
				if (! CommonUtil.isTime(dateIn.substring(9)))
					return (false);
		}

		if (date.equals("--") || date.equals("//"))
		{
			return true;
		}
		else
		{
			int len = dateIn.length();
			if ( len != 10 && len != 8 )
			{
				if (date.length() < 10 && date.length() != 8)
				{
					return (false);
				}
				dateIn = CommonUtil.trimSpaces(CommonUtil.left(date, 10 < date.length()? 10:date.length()));
				len = dateIn.length();
				if (len != 10 && len != 8 )
					return (false);
			}
			try
			{
				dateIn = getDate(dateIn);

  				d = dateOk_df.parse(dateIn);
				String s = dateOk_df.gxFormat(d);

				return (s.equals(dateIn));
			}
			catch (java.text.ParseException f)
			{
				return false;
			}
		}
	}

	private boolean isDateTimeValue(String value)
	{
		// Ya se que es un campo datetime o date

		return (value.indexOf(':') > 0 ||
				value.toUpperCase().endsWith("M") ||
				value.length() == 2);
	}
	/**
	* Validate a date in a String. It supports dates in the format specified in the 'date format'
	* preference as well as dates in the same format but without separators. It uses the value
	* of the preference 'First Year of 20th Century' to convert dates with 2-digits years.
	*/
	public int vcdate(String date, int f)
	{
		if	(isDateTimeValue(date))
		{
			return vcdate_time(date, f);
		}

		try
		{
				String aux = CommonUtil.trimSpaces(date);
				if (aux.equals("--") || aux.equals("//") || aux.equals("") || aux.equals("00"))
				{
					return 1;
				}
				GXSimpleDateFormat df = new GXSimpleDateFormat(dateFormat);
				df.setTimeZone(TimeZone.getDefault());
				df.setLenient(false);
				df.parse(date);
				return 1;
		}
		catch (InternalError e)
		{
			return 0;
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	public int vcdate_time(String date, int f)
	{
		return vcdtime(date, 0, f);
	}
	private static String fixUnparseableEmptyTime(String value)
	{
		value = value.toLowerCase();
		if (value.endsWith(" 00 am") || value.endsWith(" 00:00 am") ||
			value.endsWith(" 00:00 pm") || value.endsWith(" 00 pm") ||
			value.endsWith("00:00:00 am") || value.endsWith("00:00:00 pm") ||
			value.endsWith("00:00:00.000 am") || value.endsWith("00:00:00.000 pm"))
		return
			value.substring(0, value.length() - 3);
		else
			return value;
	}
	private static boolean isNullTimeValue(String value, boolean isAMPM)
	{
		if	(isAMPM)
		{
			return 	(
					 value.equals("12 ") ||
					 value.equals("12:00 ") ||
					 value.equals("12:00:00 ") ||
					 value.equals("12 AM") ||
					 value.equals("12:00 AM") ||
					 value.equals("12:00:00 AM") ||
					 value.equals("12:00:00.000 AM")
					 );
		}
		else
		{
			return 	(
					 value.equals("00 ") ||
					 value.equals("00:00 ") ||
					 value.equals("00:00:00 ") ||
					 value.equals("00:00:00.000 ")
					 );
		}
	}

	public static void main(String arg[])
	{
		System.out.println(isNullDateTime("  /  /   12 AM", 2, 1));
		System.out.println(isNullDateTime("  /  /   12:00 AM", 2, 1));
		System.out.println(isNullDateTime("  /  /   12:00:00 AM", 2, 1));
		System.out.println("->");
		System.out.println(isNullDateTime("  /  /   12:00:00 AM", 2, 0));
		System.out.println(isNullDateTime("  /  /   00", 2, 0));
		System.out.println(isNullDateTime("  /  /   00:00", 2, 0));
		System.out.println(isNullDateTime("  /  /   00:00:00", 2, 0));
	}

	private static boolean isNullDateTime(String date, int format, int am)
	{
		// Viene siempre con fecha/hora

		String nullFormat = getNullDateFormat(date, format);
		if	(date.startsWith(nullFormat))
		{
			if	(date.length() > nullFormat.length())
			{
				String timeValue = date.substring(nullFormat.length() - 1).trim();

				return isNullTimeValue(timeValue, am > 0);
			}
		}

		return false;
	}
	public int vcdtime(String date, int format, int am)
	{
		// Aca viene siempre con fecha y hora

		if	(isNullDateTime(date, format, am))
			return 1;

		if (ctotex(date, format) == null)
		{
			return 0;
		}

		return 1;
	}

	public Date ymdtod (int year, int month, int day)
	{
		return (ymdhmsToT(year, month, day, 0, 0, 0));
	}

	public Date ymdhmsToT (int year, int month, int day , int hour , int minute , int second)
	{
		if (year < 100 && !(year == 0 && month == 0 && day == 0))
			year += year > firstYear2K?1900:2000;

		return CommonUtil.ymdhmsToT_noYL(year, month, day, hour , minute , second);
	}
	public Date ymdhmsToT (int year, int month, int day, int hour, int minute, int second, int milliseconds)
	{
		if (year < 100 && !(year == 0 && month == 0 && day == 0))
			year += year > firstYear2K?1900:2000;

		return CommonUtil.ymdhmsToT_noYL(year, month, day, hour, minute, second, milliseconds);
	}
	public Date ymdhmsToT (int year, int month, int day , int hour)
	{
		return ymdhmsToT(year, month, day, hour, 0, 0);
	}

	public Date ymdhmsToT (int year, int month, int day )
	{
		return ymdhmsToT(year, month, day, 0, 0, 0);
	}

	public Date ymdhmsToT (int year, int month, int day , int hour , int minute)
	{
		return ymdhmsToT(year, month, day, hour, minute, 0);
	}
	public String dtoc(Date date, int formatId, String separator)
	{
		String format = mapCTODFormatToPattern(formatId);
		if	(separator.equals("-"))
			format = format.replace('/', '-');
		else
			format = format.replace('-', '/');

		if (separator.equals("") && formatId == 0)
			format = "yyyyMMdd";

		GXSimpleDateFormat dateFormat = new GXSimpleDateFormat(format);
		dateFormat.setTimeZone(defaultTimeZone);

		if	(date.equals(CommonUtil.nullDate()))
			return justSeparators(dateFormat.toPattern());
		else
		{
			return dateFormat.gxFormat(date);
		}
	}
	public Date parseDateParm(String valueString)
	{
		if (valueString.trim().length() == 0 || !Character.isDigit(valueString.trim().charAt(0)))
			return CommonUtil.nullDate();
		if (valueString.indexOf('/') != -1)
			return ctod(valueString);
		return CommonUtil.ymdhmsToT_noYL((int)CommonUtil.val(valueString.substring(0, 4)),
									 (int)CommonUtil.val(valueString.substring(4, 6)),
									 (int)CommonUtil.val(valueString.substring(6, 8)),
									 0,
									 0,
									 0);
	}
	public Date parseDTimeParm(String valueString)
	{
		if (valueString.trim().length() == 0)
			return CommonUtil.nullDate();
		if (valueString.indexOf('/') != -1)
			return ctod(valueString);
		int msVal =  (valueString.trim().length() > 14)? ((int)CommonUtil.val(valueString.substring(14, 17))) : 0;
		return CommonUtil.ymdhmsToT_noYL((int)CommonUtil.val(valueString.substring(0, 4)),
									 (int)CommonUtil.val(valueString.substring(4, 6)),
									 (int)CommonUtil.val(valueString.substring(6, 8)),
									 (int)CommonUtil.val(valueString.substring(8, 10)),
									 (int)CommonUtil.val(valueString.substring(10, 12)),
									 (int)CommonUtil.val(valueString.substring(12, 14)),
									 msVal);
	}

	public String dtoc(Date date)
	{
		if	(date.equals(CommonUtil.nullDate()))
			return justSeparators(dtoc_df.toPattern());
		else
			return dtoc_df.gxFormat(date);
	}

	public String cdow (Date date)
	{
		Locale l = new Locale(language, getCountry());
		GXSimpleDateFormat df = new GXSimpleDateFormat(SpecificImplementation.cdowMask, l);
		df.setTimeZone(TimeZone.getDefault());
		return (df.gxFormat(date));
	}

	public String cdow(Date date , String language )
	{
		Locale l = new Locale(getLanguageID(language), getCountry());
		GXSimpleDateFormat df = new GXSimpleDateFormat(SpecificImplementation.cdowMask, l);
		df.setTimeZone(TimeZone.getDefault());

		StringBuffer sb = new StringBuffer(df.gxFormat(date));
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}

	public String cmonth(Date date)
	{
		return cmonth(date, language);
	}

	public String cmonth(Date date, String language)
	{
		Locale l = new Locale(getLanguageID(language), getCountry());
		GXSimpleDateFormat df = new GXSimpleDateFormat(SpecificImplementation.cdowMask.replace("E","M"), l);
		df.setTimeZone(TimeZone.getDefault());

		StringBuffer sb = new StringBuffer(df.gxFormat(date));
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		return sb.toString();
	}

	public Date ctod(String date, int format)
	{
		if (PictureFormatter.isDateEmpty(date))
			return CommonUtil.nullDate();

		String separator = getSeparator(date);
		if (!separator.isEmpty())
		{
			String pattern = mapCTODFormatToPattern(format, separator, hasLongYear(date));

			GXSimpleDateFormat df = new GXSimpleDateFormat(pattern);
			df.setTimeZone(TimeZone.getDefault());
			try
			{
				return applyYearLimit(df.parse(date), pattern);
			}
			catch (ParseException e)
			{
				return CommonUtil.nullDate();
			}
		}
		else
		{
			return CommonUtil.nullDate();
		}
	}

	public Date ctod(String date )
	{
		// Este se usa en los webpanels, para que 'adivine' el formato con
		// que lo ingres?
		return ctot(date);
	}

	public Date ctot(String date, int format)
	{
		Date result = ctotex(date, format);
		if (result != null)
		{
			return result;
		}
		else
		{
			return CommonUtil.nullDate();
		}
	}

	private static String getSeparator(String date)
	{
		if (date.indexOf('-') >= 0)
			return "-";
		if (date.indexOf('/') >= 0)
			return "/";

		return "";
	}

	private static boolean hasLongYear(String date)
	{
		int blankPos = date.indexOf(' ');
		if (blankPos > 0)
		{
			return blankPos == 10;
		}

		if (!getBLANK_EMPTY_DATE() && date.indexOf("    ") != -1)
			return true;

		return date.length() == 10 || date.length() == 19;
	}

	public Date ctotex(String date, int format)
	{
		if (PictureFormatter.isDateEmpty(date))
			return CommonUtil.nullDate();

		String patternDate = "";

		if	(date.indexOf('-') >= 0 || date.indexOf('/') >= 0)
		{
			String separator = getSeparator(date);
			if (!separator.isEmpty())
			{
				patternDate = mapCTODFormatToPattern(format, separator, hasLongYear(date));
			}
			else
			{
				return null;
			}
		}

		String patternTime = "";
		if	(date.length() > patternDate.length())
		{
			if	(patternDate.length() > 0)
			{
				date = date.replace('T', ' ');
				patternTime = " " + getDateTimePicture(date.substring(date.indexOf(' ') + 1));
				if (patternTime.indexOf('-') >= 0 || patternTime.indexOf('/') >= 0)
				{
					patternDate = "";
				}
			}
			else
			{
				patternTime = getDateTimePicture(date);
			}
		}

		GXSimpleDateFormat df = new GXSimpleDateFormat(patternDate + patternTime);
		df.setTimeZone(TimeZone.getDefault());
		df.setLenient(false);
		try
		{
			return applyYearLimit( df.parse(date), patternDate + patternTime);
		}
		catch (ParseException e)
		{
			df.setLenient(true);
			try
			{
				return applyYearLimit( df.parse(date), patternDate + patternTime);
			}
			catch (ParseException ex)
			{
				return null;
			}
		}
	}

	public Date ctot(String date)
	{
		if (PictureFormatter.isDateEmpty(date))
			return CommonUtil.nullDate();

		try
		{
			if (isISO8601( date))
				return getISO8601Date( date);

			if (isGXJsonDate( date))
				return getISO8601Date( date);

			String picture = getDateTimePicture(date);
			GXSimpleDateFormat df = new GXSimpleDateFormat(picture);
			df.setTimeZone(defaultTimeZone);
			df.setLenient(false);

			return applyYearLimit(df.parse(date), picture);
		}
		catch (ParseException e)
		{
			return CommonUtil.nullDate();
		}
	}

	boolean isGXJsonDate( String date)
	{
		if (date.length() == 19){
			if (date.charAt(4) == '/' && date.charAt(7) == '/' && date.charAt(13) == ':' && date.charAt(16) == ':' )
					return true;
		}
		return false;
	}

	boolean isISO8601( String date)
	{
		if (date.indexOf( "T") != -1)
			return true;
		if (date.length() == 10)
		{
			if (date.charAt(4) == '-' && date.charAt(7) == '-' && date.length() == 10)
				return true;
		}
		return false;
	}

	static Date getISO8601Date( String date)
	{
		if (date.length() > 10)
		{
			int year    = Integer.parseInt(date.substring( 0,  4));
			int month   = Integer.parseInt(date.substring( 5,  7));
			int day     = Integer.parseInt(date.substring( 8,  10));
			int hour    = Integer.parseInt(date.substring( 11, 13));
			int minute  = Integer.parseInt(date.substring(14, 16));
			int seconds = Integer.parseInt(date.substring(17, 19));
			int milliseconds = 0;
			if (date.length() > 20)
			{
				 milliseconds = Integer.parseInt(date.substring(20, 23));
				 return CommonUtil.ymdhmsToT_noYL(year, month, day, hour, minute, seconds, milliseconds);
			}
			else
				return CommonUtil.ymdhmsToT_noYL(year, month, day, hour, minute, seconds);
		}
		else
		{
			int year    = Integer.parseInt(date.substring( 0,  4));
			int month   = Integer.parseInt(date.substring( 5,  7));
			int day     = Integer.parseInt(date.substring( 8,  10));
			return CommonUtil.ymdhmsToT_noYL(year, month, day, 0, 0, 0);
		}
	}

	public String yearTo4Digits(String date, String[] picture, Date dateSet, int type)
	{
		if	(type == GXTypeConstants.DATETIME )
		{
			if	(!PictureFormatter.isDateInPicture(picture[0]))
				return date;

			// es '99/99/99 time' o '99/99/9999 time'
			//     					 012345678

			if	(picture[0].length() > 8 && picture[0].charAt(8) == '9')
				// año con 4 digitos
				return date;
		}
		else
		{
			if	(picture[0].length() > 8)
				return date;
		}

		String year = "00";
		if(startsYearDate)
		{
			if(date.length() >= 2) year = date.substring(0, 2);
		}
		else
		{
			if(date.length() >= 8) year = date.substring(6, 8);
		}

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dateSet);
		if	(calendar.get(Calendar.YEAR) % 100 == (int) CommonUtil.val(year))
		{
			// Si la fecha no cambi?los 2 digitos del año, entonces dejo el siglo que
			// estaba antes
			year = CommonUtil.str(1900 + calendar.get(Calendar.YEAR), 4, 0);
		}
		else
		{
			year = getYear(year);
		}

		String newDate = "";

		if	(startsYearDate)
		{
			newDate = year + date.substring(2);
			picture[0] = "99" + picture[0];
		}
		else
		{
			newDate = (date.length() > 6?date.substring(0, 6): "") + year + (date.length() > 8?date.substring(8):"");
			picture[0] = (picture[0].length() > 8 ? picture[0].substring(0, 8): picture[0]) + "99" + (picture[0].length() > 8 ? picture[0].substring(8): "");
		}

		return newDate;
	}

	public Date dateOk1(String date, String picture)
	{
		Date ret = null;

		if(PictureFormatter.isTimeInPicture(picture))
		{
			if	(picture.substring(picture.length() - 2, picture.length()).equals("AM"))
				if	(!date.substring(date.length() - 2, date.length()).equals("AM") &
					 !date.substring(date.length() - 2, date.length()).equals("PM") )
			{
					return null;
			}
		}

		String sDateFmt = pictureToDateFormat(picture);
		GXSimpleDateFormat df = new GXSimpleDateFormat(sDateFmt);
		df.setTimeZone(defaultTimeZone);
		df.setLenient(false);

		try
		{
			String dateIn;
			// Si tiene 4 digitos en el año normalizo la date para que el año vaya de 4 digitos en caso de que venga de dos
			if	(sDateFmt.indexOf("yyyy") >= 0)
				dateIn = getDate(date);		// esto pasa el year a 4 digitos
			else
				dateIn = date;

			ret = df.parse(dateIn);
		}
		catch (ParseException e)
		{
		}
		catch (IllegalArgumentException e)
		{
		}

		return ret;
	}


	private NumberFormat getNumberFormat() {
		return getNumberFormat(getLocale());
	}

	private NumberFormat getNumberFormat(Locale l) {
		NumberFormat nFormat = NumberFormat.getInstance(l);
		nFormat.setRoundingMode(RoundingMode.DOWN); //MIZUHO Special Request
		return nFormat;
	}

	public String pictureToDateFormat(String picture)
	{
	/*
		"99"
		"99:99"
		"99:99:99"
		"99/99/9999 99:99:99"
		"99/99/9999 99:99"
		"99/99/9999 99"
		"99/99/9999"
		"99/99/99 99:99:99"
		"99/99/99 99:99"
		"99/99/99 99"
		"99/99/99"
	*/
		boolean isAMPM = false;

		if	(picture.length() > 1 && picture.substring(picture.length() - 2).equals("AM"))
		{
			isAMPM = true;
			picture = picture.substring(0, picture.length() - 3);
		}

		StringBuffer output = new StringBuffer(picture.length());

		if	(PictureFormatter.isDateInPicture(picture))
		{
			// Has date part
			if (picture.indexOf("9999") >=0)
			{
				if	(startsYearDate)
				{
					output.append("yy");
					output.append(dateFormat);
				}
				else
				{
					output.append(dateFormat);
					output.append("yy");
				}
			}else
				output.append(dateFormat);

		}

		if	(PictureFormatter.isTimeInPicture(picture))
		{
			if	(output.length() > 0)
				output.append(" ");

			output.append(timeFormat.substring(0, 2));

			// Time part
			int timeStart = picture.indexOf(' ');
			timeStart = timeStart < 0?0:timeStart;

			if	((picture.length() - timeStart) > 3)
			{
				output.append(":mm");
				 // minutes
			}
			if	((picture.length() - timeStart) > 6)
			{
				output.append(":ss");
				 // seconds
			}

			if	((picture.length() - timeStart) > 9 )
			{
				output.append(".SSS");
				 // milliseconds
			}

			if	(isAMPM || timeFormat.substring(timeFormat.length()-1).equals("a")) //isAMPM)
				output.append(" a");
		}

		return output.toString();
	}

	String alignAndPad(String text, char pad, String picture, boolean floating, NumberFormat numberFormat)
	{
		DecimalFormat df = (DecimalFormat) numberFormat;

		char decimalSeparator  = df.getDecimalFormatSymbols().getDecimalSeparator();

		text = text.trim();
		if (text.equals(""))
			text = "0";

		int posSep  = picture.indexOf('.');

		if (posSep == -1)
		{
			if (floating)
			{
				return (text);
			}
			else
			{
				return (alignRight(text, picture.length()));
			}
		}

		int	posText = text.indexOf(decimalSeparator);

		StringBuffer sText = new StringBuffer(text);

		if (!CommonUtil.in(text, decimalSeparator) )
		{
			sText.append(decimalSeparator);
			posText = sText.length() - 1;
		}

		while (posText++ < posSep )
			sText.insert(0, ' ');

		/*
		Los Z de la derecha del separador decimal se toman como 9, asi esta definido en genexus, y asi lo esperan ahora las pruebas del fullgx.
		for (int i = sText.length(); i<picture.length() && picture.charAt(i) == '9';i++ ){
			sText.append(pad);
		}*/

		while (sText.length() < picture.length() )
			sText.append(pad);


		if (floating)
		{
			return CommonUtil.ltrim(sText.toString());
		}
		else
		{
			return sText.toString();
		}
	}

	private boolean isAllZ(String picture)
	{
		if	(!picture.trim().equals("@Z"))
		{
			int len = picture.length();
			for (int i = 0; i < len; i++)
			{
				if	(picture.charAt(i) == '9')
					return false;
			}
		}

		return true;

	}

	public String format(long value, String picture)
    {
        return format(value, picture, getNumberFormat());
    }

	String format(long value, String picture, NumberFormat numberFormat)
	{
		String formatted = "";
		String originalPicture = picture;
		int originalPictLength = picture.length();
		picture = takeSymbolsFromPicture(picture);
		int newPictLength = picture.length();
		int j = 0;
		String valueStr = String.valueOf(value);
		int valueStrLength = valueStr.length();
		boolean continua = true;

		if (picture.indexOf('.')==-1 && picture.indexOf(',')==-1 && picture.indexOf('Z')==-1)
		{
		 for (int i = 0; i < picture.length(); i++) {
			char a = picture.charAt(i);
			switch (a) {
			  case '9':
				 if (value == 0)
					formatted = formatted + " ";
				 else
				 {
					if (j < valueStrLength)
					  formatted = formatted + valueStr.charAt(j);
					j++;
				 }
				 break;
			  default:
				 formatted = formatted + a;
				 continua = false;
				 break;
			}
		 }
		}

		String preffix = picturePreffix(picture);
		if (preffix.startsWith("("))
		{
			if (value < 0)
			{
				value = - value;
			}
			else
			{
				picture = picture.replace('(', 'Z').substring(0, picture.length() - 1);
				preffix = "";
			}
		}
		boolean floating = (preffix.trim().length() > 1 && preffix.replace(preffix.charAt(0), new String(" ").charAt(0)).trim().equals(""));

		String withoutSuffixPicture = removePictureSuffix("%", removePicturePreffix(preffix,picture));

	    if	((!isAllZ(picture) || value != 0) && continua)
		{
			DecimalFormat df = (DecimalFormat) numberFormat;
			df.applyPattern(PictureFormatter.pictureToNumberFormat(withoutSuffixPicture.replace('9','0').replace('Z','#')));
			if ( CommonUtil.in(picture, '.') || CommonUtil.in(picture, ','))
			{
				formatted = alignAndPad(df.format(value), '0', withoutSuffixPicture, floating, numberFormat);
			}
			else
			{
				formatted = df.format(value);
			}
		}

		if( originalPictLength > newPictLength && !floating) formatted = addSymbolsToText(formatted, originalPicture);
		int negativeSign = (value < 0) ? 1 : 0;
		if (floating)
		{
			return alignRight(preffix.substring(preffix.length() -1) + addPictureSuffix("%", originalPicture, addPicturePreffix(preffix, originalPicture, formatted)), originalPictLength + negativeSign);
		}
		else
		{
			return addPictureSuffix("%", originalPicture, addPicturePreffix(preffix, originalPicture, alignRight(formatted, originalPictLength + negativeSign)));
		}
	}

	private String takeSymbolsFromPicture(String picture)
	{
		StringBuffer pictureWithoutSymbols = new StringBuffer();
		boolean dotRemove = false;

        //Si la picture tiene mas de un . entonces es considerado un simbolo mas (p.ej pictures para C.I.)
		if (picture.lastIndexOf('.') != picture.indexOf('.'))
			dotRemove = true;
		for (int i = 0; i < picture.length(); i++)
		{
			char a = picture.charAt(i);
			if( (a=='Z') || (a=='9') || (a==',')  || (a=='%') || (a==')') || (a=='('))
			{
				pictureWithoutSymbols.append(a);
			}
			else if (a=='.' && !dotRemove)
			{
				pictureWithoutSymbols.append(a);
			}
		}
		return pictureWithoutSymbols.toString();
	}

	private String addSymbolsToText(String text, String originalPicture)
	{
		StringBuffer formattedText = new StringBuffer();
		int textIdx = text.length() - 1;

		//Si la picture tiene mas de un . entonces es considerado un simbolo mas (p.ej pictures para C.I.)
		boolean dotAsLiteral = false;
		if (originalPicture.lastIndexOf('.') != originalPicture.indexOf('.'))
			dotAsLiteral = true;

		for (int i = originalPicture.length() - 1; i >= 0;)
		{
			char a = originalPicture.charAt(i--);
			while( (a!='Z') && (a!='9') && (a!=',') && ((a!='.')  || (a=='.' && dotAsLiteral)) && (a!='%') && (a!=')') && (a!='('))
			{
				formattedText.append(a);
				if( i >= 0) a = originalPicture.charAt(i--);
				else break;
			}
			if( textIdx >= 0 ) formattedText.append(text.charAt(textIdx--));
		}
		while( textIdx >= 0 ) formattedText.append(text.charAt(textIdx--));
		return formattedText.reverse().toString();
	}

	public String format(java.math.BigDecimal value, String picture)
	{
		if ( CommonUtil.in(picture, '.') || CommonUtil.in(picture, ','))
        {
			int decimalIndex = picture.indexOf('.');
			if (decimalIndex == -1)
             decimalIndex = picture.indexOf(',');
			return formatBigDecimal(CommonUtil.truncDecimal(value, picture.substring(decimalIndex).length()-1), picture);
		}
		else
		{
			return formatBigDecimal(value, picture);
		}
	}

    public String formatBigDecimal(java.math.BigDecimal value, String picture)
    {
        return formatBigDecimal(value, picture, getNumberFormat());
    }

	private String formatBigDecimal(java.math.BigDecimal value, String picture, NumberFormat numberFormat)
	{
		String formatted = "";
		String originalPicture = picture;
		int originalPictLength = picture.length();
		picture = takeSymbolsFromPicture(picture);
		int newPictLength = picture.length();
		int j = 0;
		String valueStr = String.valueOf(value);
		int valueStrLength = valueStr.length();
		boolean continua = true;

		if (picture.indexOf('.')==-1 && picture.indexOf(',')==-1 && picture.indexOf('Z')==-1)
		{
		 for (int i = 0; i < picture.length(); i++) {
			char a = picture.charAt(i);
			switch (a) {
			  case '9':
				 if (value.compareTo(java.math.BigDecimal.ZERO) == 0)
					formatted = formatted + " ";
				 else
				 {
					if (j < valueStrLength)
					  formatted = formatted + valueStr.charAt(j);
					j++;
				 }
				 break;
			  default:
				 formatted = formatted + a;
				 continua = false;
				 break;
			}
		 }
		}

		String preffix = picturePreffix(picture);
		if (preffix.startsWith("("))
		{
			if (value.compareTo(java.math.BigDecimal.ZERO) == -1)
			{
				value = value.negate();
			}
			else
			{
				picture = picture.replace('(', 'Z').substring(0, picture.length() - 1);
				preffix = "";
			}
		}
		boolean floating = (preffix.trim().length() > 1 && preffix.replace(preffix.charAt(0), new String(" ").charAt(0)).trim().equals(""));

		String withoutSuffixPicture = removePictureSuffix("%", removePicturePreffix(preffix,picture));

	    if	((!isAllZ(picture) || value.compareTo(java.math.BigDecimal.ZERO) != 0) && continua)
		{
			DecimalFormat df = (DecimalFormat) numberFormat;
			df.applyPattern(PictureFormatter.pictureToNumberFormat(withoutSuffixPicture.replace('9','0').replace('Z','#')));
			if ( CommonUtil.in(picture, '.') || CommonUtil.in(picture, ','))
			{
				formatted = alignAndPad(df.format(value), '0', withoutSuffixPicture, floating, numberFormat);
			}
			else
			{
				formatted = df.format(value);
			}
		}
		if( originalPictLength > newPictLength && !floating) formatted = addSymbolsToText(formatted, originalPicture);
		int negativeSign = (value.signum() == -1) ? 1 : 0;
		if (floating)
		{
			return alignRight(preffix.substring(preffix.length() -1) + addPictureSuffix("%", originalPicture, addPicturePreffix(preffix, originalPicture, formatted)), originalPictLength + negativeSign);
		}
		else
		{
			return addPictureSuffix("%", originalPicture, addPicturePreffix(preffix, originalPicture, alignRight(formatted, originalPictLength + negativeSign)));
		}
	}

    public String format(double value, String picture)
    {
        return format(value, picture, getNumberFormat());
    }
	private String format(double value, String picture, NumberFormat numberFormat)
	{
		String formatted = "";

		String preffix = picturePreffix(picture);
		if (preffix.startsWith("("))
		{
			if (value < 0)
			{
				value = - value;
			}
			else
			{
				picture = picture.replace('(', 'Z').substring(0, picture.length() - 1);
				preffix = "";
			}
		}
		boolean floating = (preffix.trim().length() > 1 && preffix.replace(preffix.charAt(0), new String(" ").charAt(0)).trim().equals(""));

		String withoutSuffixPicture = removePictureSuffix("%", removePicturePreffix(preffix,picture));

		int negativeSign = (value < 0) ? 1 : 0;
		if	(!isAllZ(picture) || value != 0)
		{
			DecimalFormat df = (DecimalFormat) numberFormat;
			df.applyPattern(PictureFormatter.pictureToNumberFormat(withoutSuffixPicture.replace('9','0').replace('Z','#')));
			if ( CommonUtil.in(picture, '.') || CommonUtil.in(picture, ','))
			{
				if (floating)
				{
					return alignRight(preffix.substring(preffix.length() -1) + addPictureSuffix("%", picture, addPicturePreffix(preffix, picture, alignAndPad(df.format(value), '0', withoutSuffixPicture, floating, numberFormat))), picture.length()+negativeSign);
				}
				else
				{
					return addPictureSuffix("%", picture, addPicturePreffix(preffix, picture, alignAndPad(df.format(value), '0', withoutSuffixPicture, floating, numberFormat)));
				}
			}
			formatted = df.format(value);
		}

		if (floating)
		{
			return alignRight(preffix.substring(preffix.length() -1) + addPictureSuffix("%", picture, addPicturePreffix(preffix, picture, formatted)), picture.length()+negativeSign);
		}
		else
		{
			return addPictureSuffix("%", picture, addPicturePreffix(preffix, picture, alignRight(formatted, picture.length()+negativeSign)));
		}
	}

	private String removePictureSuffix(String suffix, String picture)
	{
		if	(picture.endsWith(suffix))
		{
			return picture.substring(0, picture.length() - suffix.length());
		}

		return picture;
	}

	private String addPictureSuffix(String suffix, String originalPicture, String value)
	{
		if (originalPicture.endsWith(suffix))
		{
			return value + suffix;
		}

		return value;
	}

		private String picturePreffix(String picture)
		{
			int i = 0;
			int len = picture.length();
			String preffix = "";

			while(i < len)
			{
				if (GXPicture.isSeparator(picture.charAt(i)))
				{
					preffix = preffix + picture.charAt(i);
				}
				else
				{
					return preffix;
				}
				i++;
			}
			return preffix;
		}

        private String removePicturePreffix(String suffix, String picture)
        {
                if	(picture.startsWith(suffix))
                {
                        return picture.substring(suffix.length(), picture.length());
                }

                return picture;
        }

        private String addPicturePreffix(String suffix, String originalPicture, String value)
        {
                if (originalPicture.startsWith(suffix))
                {
                        return suffix + value;
                }

                return value;
        }

	public long ctol(String value)
	{
		if(value == null)return 0;

		return tryParseLong(value.trim());
	}
	public long ctol(String value, String decSep, String thousandsSep)
	{
		return ctol(normalize(value, decSep.charAt(0), thousandsSep.charAt(0)));
	}

	public java.math.BigDecimal ctond(String value)
	{
		DecimalFormat df = (DecimalFormat) getNumberFormat();

		return DecimalUtil.stringToDec(normalize(value,
		df.getDecimalFormatSymbols().getDecimalSeparator(),
		df.getDecimalFormatSymbols().getGroupingSeparator()));
	}

	public double cton(String value, String decSep, String thousandsSep)
	{
		return CommonUtil.val(
		normalize(value, decSep.charAt(0), thousandsSep.charAt(0))
		);
	}

	// Parsea un string que contiene un numero, ignora los caracteres invalidos
	// El resultado se normaliza para que el punto decimal siempre sea '.'
	private static String normalize(String value, char decSepChar, char thousandsSepChar)
	{
		StringBuffer buffer = new StringBuffer();
		boolean first = true;

		//Si el numero tiene mas de un . entonces no es separador decimal (es parte de la picture, p.ej. C.I.) se remueven todos.
		boolean dotAsLiteral = (decSepChar=='.') && (value.lastIndexOf('.') != value.indexOf('.'));

		for (int i = 0; i < value.length(); i++)
		{
			if (value.charAt(i) == decSepChar)
			{
				if (!dotAsLiteral)
				{
					buffer.append('.');
					first=false;
				}
			}
			else if (value.charAt(i) == '-' && first)
			{
				buffer.append(value.charAt(i));
				first=false;
			}
			else if (value.charAt(i) >= '0' && value.charAt(i) <= '9')
			{
				buffer.append(value.charAt(i));
				first=false;
			}
		}
		return buffer.toString();
	}

	public String ntoc(long value, int digits, int decimals, String decSep, String thousandsSep)
	{
		String picture = getPicture(digits, decimals, thousandsSep);

        String result = format(value, picture, getNumberFormat(Locale.ENGLISH));//invariant
        if ((decimals == 0 && thousandsSep.length() == 0))
            return result;
        else
            return replaceSeparators(result, decSep, thousandsSep);
	}

	public String ntoc(double value, int digits, int decimals, String decSep, String thousandsSep)
	{
		String picture = getPicture(digits, decimals, thousandsSep);

        String result = format(value, picture, getNumberFormat(Locale.ENGLISH));//invariant
        if ((decimals == 0 && thousandsSep.length() == 0))
            return result;
        else
            return replaceSeparators(result, decSep, thousandsSep);
	}

	public String ntoc(java.math.BigDecimal value, int digits, int decimals, String decSep, String thousandsSep)
	{
		String picture = getPicture(digits, decimals, thousandsSep);

        String result = formatBigDecimal(value, picture, getNumberFormat(Locale.ENGLISH));//invariant
        if ((decimals == 0 && thousandsSep.length() == 0))
            return result;
        else
            return replaceSeparators(result, decSep, thousandsSep);
	}

    private String replaceSeparators(String number, String decSep, String thousandsSep)
	{
		StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < number.length(); i++)
		{
            char c = number.charAt(i);
			if (c == '.')
				sb.append(decSep);
			else if (c == ',')
				sb.append(thousandsSep);
			else
				sb.append(c);
		}
		return sb.toString();
	}



	private String getPicture(int digits, int decimals, String thousandsSep)
	{
		String out;

		if	(thousandsSep.equals(""))
		{
			out = CommonUtil.replicate("Z", digits - 1) + "9";
		}
		else
		{
			StringBuffer integerThousands = new StringBuffer();

			for (int i = 0; i < digits; i++)
			{											 //0 1 2
				integerThousands.append("Z");
				if ( (i + 1) % 3 == 0)
				{
					integerThousands.append(",");
				}

			}

			int lastZ = integerThousands.toString().indexOf('Z');
			if(lastZ != -1)
			{
				integerThousands.setCharAt(lastZ, '9');
			}

			out = integerThousands.reverse().toString();
		}

		if (decimals > 0)
			out = out + "." + CommonUtil.replicate("9", decimals);

		return out;
	}

	static String alignRight(String text, int length)
	{
		return (CommonUtil.padl(text, length, " "));
	}


	public String format(String value, String picture)
	{
		return PictureFormatter.format(value, picture);
	}

	public String format(java.util.Date value, String picture)
	{
		GXSimpleDateFormat dateFormatter;

		if	(CommonUtil.nullDate().equals(value) || CommonUtil.nullDate().equals(CommonUtil.resetTime(value)))
		{
			if	(PictureFormatter.isTimeInPicture(picture))
			{
				String timePicture = PictureFormatter.getTimePictureInPicture(picture);

				String datePicture = picture.substring(0, picture.length() - timePicture.length());

				if	(timeFormat.endsWith("a"))
				{
					timePicture += " AM";
				}

				if (CommonUtil.nullDate().equals(value) && getBLANK_EMPTY_DATE())
				{
					return "        ";
				}
				else
				{

					dateFormatter = new GXSimpleDateFormat(pictureToDateFormat(timePicture));
					dateFormatter.setTimeZone(CommonUtil.defaultTimeZone);
					dateFormatter.setLenient(false);

					return PictureFormatter.getNullMask(datePicture) + dateFormatter.gxFormat(value);
				}
			}
			else
			{
				return PictureFormatter.getNullMask(picture);
			}
		}
/*
		if	(GXutil.nullDate().equals(GXutil.resetTime(value)) && PictureFormatter.isTimeInPicture(picture))
		{

				String timePicture = PictureFormatter.getTimePictureInPicture(picture);
				String datePicture = picture.substring(0, picture.length() - timePicture.length());

				dateFormatter = new GXSimpleDateFormat(pictureToDateFormat(timePicture));
				dateFormatter.setTimeZone(GXutil.defaultTimeZone);
				dateFormatter.setLenient(false);

				return PictureFormatter.getNullMask(datePicture) + dateFormatter.gxFormat(value);
		}
*/

		dateFormatter = new GXSimpleDateFormat(pictureToDateFormat(picture));
		dateFormatter.setTimeZone(CommonUtil.defaultTimeZone);
		dateFormatter.setLenient(false);

		return dateFormatter.gxFormat(value);
	}

	/*
		"99"
		"99:99"
		"99:99:99"
		"99/99/9999 99:99:99"
		"99/99/9999 99:99"
		"99/99/9999 99"
		"99/99/9999"
		"99/99/99 99:99:99"
		"99/99/99 99:99"
		"99/99/99 99"
		"99/99/99"

		"99 AM"
		"99:99 AM"
		"99:99:99 AM"
		"99/99/9999 99:99:99 AM"
		"99/99/9999 99:99 AM"
		"99/99/9999 99 AM"
		"99/99/9999 AM"
		"99/99/99 99:99:99 AM"
		"99/99/99 99:99 AM"
		"99/99/99 99 AM"
		"99/99/99 AM"

	*/


	public Date applyYearLimit(Date dateTime, String picture)
	{
		// Tiene año de 4 digitos
		if	(picture.indexOf("yyyy") >= 0)
		{
			return dateTime;
		}

		int posYear = picture.indexOf("yy");

		// No tiene año
		if	(posYear < 0)
		{
			return 	dateTime;
		}

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(dateTime);
		if	(calendar.get(Calendar.YEAR) % 100 < firstYear2K)
		{
			if (calendar.get(Calendar.YEAR) < 100)
				calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 100);
		}
		else if (calendar.get(Calendar.YEAR) > 100)
		{
			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 100);
		}

		return dateTime;
	}

	public String getDateTimePicture(String dateTime)
	{
		String AMPM = "";
		String picture;

		dateTime = CommonUtil.rtrim(dateTime);
		dateTime = fixUnparseableEmptyTime(dateTime);

		if	(dateTime.endsWith("M") || dateTime.endsWith("m"))
		{
			// Si termina con AM o PM, le saco el AM/PM.
			// me fijo si termina con 'NNxM' o NN xM'
			if	(dateTime.charAt(dateTime.length() - 3) == ' ')
			{
				dateTime = dateTime.substring(0, dateTime.length() - 3);
				AMPM = " a";
			}
			else
			{
				dateTime = dateTime.substring(0, dateTime.length() - 2);
				AMPM = "a";
			}
		}

		String hour = AMPM.equals("")?"HH":"hh";

        dateTime = fixTimeExpresion(dateTime);

		// Intento averiguar si tiene date, datetime o solo time
		if	(dateTime.indexOf(' ') < 0)
		{
			// Tiene o solo date o solo time

			/*	 "99"
				 "99:99"
				 "99:99:99"
				 "99/99/9999"
				 "99/99/99"
			*/

			switch (dateTime.length())
			{
				case 2 :
						// "99"
						picture = hour;
						break;
				case 5 :
						// "99:99"
						picture = hour + ":mm";
						break;
				case 8 :
						// "99:99:99"
						// "99/99/99"
						if	(dateTime.indexOf(':') < 0)
							picture = dateFormat;
						else
							picture = hour + ":mm:ss";
						break;
				case 12 :
						picture = hour +  ":mm:ss.SSS";
						break;
//				case 10:
				default:
					   // "99/99/9999"
						picture = addYearToPicture(dateFormat);
						break;
			}
		}
		else
		{
			// Tiene date y time
/*
		"99/99/99 99"
		"99/99/9999 99"
		"99/99/99 99:99"
		"99/99/9999 99:99"
		"99/99/99 99:99:99"
		"99/99/9999 99:99:99"
*/

			switch (dateTime.length())
			{
				case 11:
					//		"99/99/99 99"
					picture = dateFormat + " " + hour;
					break;
				case 13:
					//		"99/99/9999 99"
					picture = addYearToPicture(dateFormat) + " " + hour;
					break;
				case 14:
					//		"99/99/99 99:99"
					picture = dateFormat + " " + hour + ":mm";
					break;
				case 16:
					//		"99/99/9999 99:99"
					picture = addYearToPicture(dateFormat) + " " + hour + ":mm";
					break;
				case 17:
					//		"99/99/99 99:99:99"
					picture = dateFormat + " " + hour + ":mm:ss";
					break;
				case 21:
					//		"99/99/99 99:99:99.999"
					picture = dateFormat + " " + hour + ":mm:ss.SSS";
					break;
				case 23:
					//		"99/99/9999 99:99:99.999"
					picture = addYearToPicture(dateFormat) + " " + hour + ":mm:ss.SSS";
					break;
//				case 19:
				default:
					//		"99/99/9999 99:99:99"
					picture = addYearToPicture(dateFormat) + " " + hour + ":mm:ss";
			}

		}

		return picture + AMPM;

	}

    private static String fixTimeExpresion(String dateTime)
    {//Este metodo rellena el Time en caso de que le falten digitos
     //para que quede en un formato valido
		int spaceIdx = dateTime.indexOf(' ');
		String fixedStr = "";
		String timeStr = "";

		if (spaceIdx > 0) { //Tiene Date y Time, lo separo en dos strings para que sea mas facil
			fixedStr = dateTime.substring(0, spaceIdx + 1);
			timeStr = dateTime.substring(spaceIdx + 1);
		}
		else { //Tiene solo Time
			timeStr = dateTime;
		}

		int firstDotsIdx = timeStr.indexOf(":");
		int secondDotsIdx = timeStr.lastIndexOf(":");
		if(firstDotsIdx != -1) {
			if (firstDotsIdx < 2) { //Le falta un digito a la hora
				fixedStr += 0;
			}
			fixedStr += timeStr.substring(0, firstDotsIdx + 1);
			if((secondDotsIdx != -1) && (secondDotsIdx != firstDotsIdx)) {
				if((secondDotsIdx - firstDotsIdx) <= 2) { //Le falta un digito a los minutos
					fixedStr += 0;
				}
				fixedStr += timeStr.substring(firstDotsIdx + 1, secondDotsIdx + 1);
				if((timeStr.length() - secondDotsIdx) <= 2) { //Le falta un digito a los segundos
					fixedStr += 0;
				}
				fixedStr += timeStr.substring(secondDotsIdx + 1);
			}
			else { //No tiene segundos
				if((timeStr.length() - firstDotsIdx) <= 2) { //Le falta un digito a los minutos
					fixedStr += 0;
				}
				fixedStr += timeStr.substring(firstDotsIdx + 1);
			}
		}
		else { //Tiene solo hora
			if(timeStr.length() < 2) { //Le falta un digito a la hora
				fixedStr += 0;
			}
			fixedStr += timeStr;
		}

		return fixedStr;
    }

	private static String addYearToPicture(String picture)
	{
		int index = picture.indexOf('y');
		if(index == -1)
		{
			return picture + "yy";
		}
		else
		{
			return picture.substring(0, index) + "yy" + picture.substring(index);
		}
	}

	public long tryParseLong(String string)
	{
		int radix = 10;

	    if (string.isEmpty())
	    {
	      return 0;
	    }

	    boolean negative = string.charAt(0) == '-';
	    int index = negative ? 1 : 0;
	    if (index == string.length())
	    {
	      return 0;
	    }
	    int digit = AsciiDigits.digit(string.charAt(index++));
	    if (digit < 0 || digit >= radix)
	    {
	      return 0;
	    }
	    long accum = -digit;

	    long cap = Long.MIN_VALUE / radix;

	    while (index < string.length())
	    {
	      digit = AsciiDigits.digit(string.charAt(index++));
	      if (digit < 0 || digit >= radix || accum < cap)
	      {
	        return 0;
	      }
	      accum *= radix;
	      if (accum < Long.MIN_VALUE + digit)
	      {
	        return 0;
	      }
	      accum -= digit;
	    }

	    if (negative)
	    {
	      return accum;
	    }
	    else if (accum == Long.MIN_VALUE)
	    {
	      return 0;
	    }
	    else
	    {
	      return -accum;
	    }
	}

	static final class AsciiDigits
	{
	    private AsciiDigits() {}

	    private static final byte[] asciiDigits;

	    static
	    {
	      byte[] result = new byte[128];
	      Arrays.fill(result, (byte) -1);
	      for (int i = 0; i <= 9; i++)
	      {
	        result['0' + i] = (byte) i;
	      }
	      for (int i = 0; i <= 26; i++)
	      {
	        result['A' + i] = (byte) (10 + i);
	        result['a' + i] = (byte) (10 + i);
	      }
	      asciiDigits = result;
	    }

	    static int digit(char c)
	    {
	      return (c < 128) ? asciiDigits[c] : -1;
	    }
	 }

}
