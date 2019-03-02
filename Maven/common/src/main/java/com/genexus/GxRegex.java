package com.genexus;

import java.util.Vector;
import java.util.regex.*;
import com.genexus.internet.*;

public class  GxRegex
{
	static int lastErrCode;
	static String lastErrDescription;
	static void resetError()
	{
		lastErrCode = 0;
		lastErrDescription = "";
	}
	static void setError(int errCode, String errDescription)
	{
		lastErrCode = errCode;
		lastErrDescription = errDescription;
	}
	public static boolean IsMatch(String txt, String rex)
	{
		Pattern p;
		resetError();
		try
		{
			if (txt.indexOf(CommonUtil.newLine()) > 0)
				p = Pattern.compile(rex, Pattern.MULTILINE);
			else
				p = Pattern.compile(rex);
		}
		catch(PatternSyntaxException e)
		{
			setError(1, e.getMessage());
			return false;
		}
		Matcher m = p.matcher(txt);
		//return m.matches();
		return m.find();
	}
	
	public static String Replace(String txt , String rex, String replace)
	{
		Pattern p;
		resetError();
		try
		{
			if (txt.indexOf(CommonUtil.newLine()) > 0)
				p = Pattern.compile(rex, Pattern.MULTILINE);
			else
				p = Pattern.compile(rex);
		}
		catch(PatternSyntaxException e)
		{
			setError(1, e.getMessage());
			return txt;
		}
		Matcher m = p.matcher(txt);
		return m.replaceAll(replace);		
	}
	
	public static Vector Split(String txt ,String rex)
	{
		Vector result = new Vector();
		Pattern p;
		resetError();
		try
		{
			if (txt.indexOf(CommonUtil.newLine()) > 0)
				p = Pattern.compile(rex, Pattern.MULTILINE);
			else
				p = Pattern.compile(rex);
		}
		catch(PatternSyntaxException e)
		{
			setError(1, e.getMessage());
			return result;
		}
		String[] stringCollection = p.split(txt);
		int i = 0;
		while (i < stringCollection.length)
		{
			result.addElement(stringCollection[i]);	
			i ++;
		}
		return result;
	}
	
	public static GxUnknownObjectCollection Matches(String txt, String rex)
	{
		GxUnknownObjectCollection result = new GxUnknownObjectCollection();
		Pattern p;
		try
		{
			resetError();
			if (txt.indexOf(CommonUtil.newLine()) > 0)
				p = Pattern.compile(rex, Pattern.MULTILINE);
			else
				p = Pattern.compile(rex);
		}
		catch(PatternSyntaxException e)
		{
			setError(1, e.getMessage());
			return result;
		}
		Matcher m = p.matcher(txt);
		while(m.find())
		{
			String value = m.group(0);
			StringCollection groups = new StringCollection();
			for(int i= 1; i <= m.groupCount(); i++ )
			{
				if (m.group(i) == null)
				{
					groups.add("");
				}
				else
				{
					groups.add(m.group(i));
				}
			}
			result.add(new GxRegexMatch(value, groups));
		}
		
		return result;
	}
	public static int GetLastErrCode()
	{
		return lastErrCode;
	}
	public static String GetLastErrDescription()
	{
		return lastErrDescription;
	}

}
