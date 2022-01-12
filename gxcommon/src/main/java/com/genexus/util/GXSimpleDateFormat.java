package com.genexus.util;

import java.text.*;
import java.util.*;


public class GXSimpleDateFormat extends SimpleDateFormat
{
	public GXSimpleDateFormat(String format)
	{
		this(format, Locale.ENGLISH);
	}

	public GXSimpleDateFormat(String format, Locale locale)
	{
		super(format, locale);
		setLenient(false);
	}


	public String gxFormat(Date date) //tenemos que usar un nombre nuevo porque format es 'final'
	{
		return super.format(date);
	}
}
