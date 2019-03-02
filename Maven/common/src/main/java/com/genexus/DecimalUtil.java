// $Log: DecimalUtil.java,v $
// Revision 1.5  2006/03/09 17:30:20  alevin
// - En el unexponentString hago un trim del String recibido.
//
// Revision 1.4  2005/11/01 13:00:03  alevin
// - Agrego el metodo unscientificString para convertir un BigDecimal a un String sin
//   notacion cientifica.
//
// Revision 1.3  2002/09/13 22:34:31  aaguiar
// - La unexponentString devuelve 0 si falla la conversion a numerico.
//
// Revision 1.2  2002/07/05 17:02:17  aaguiar
// - Se definio la pow para BigDecimals
//
// Revision 1.1.1.1  2002/03/01 15:20:56  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import java.math.BigDecimal;

public class DecimalUtil
{
	public static final BigDecimal ZERO = new BigDecimal(0);

	public static int compareTo(BigDecimal left, BigDecimal right)
	{
		if (left == null)
		{
			left = ZERO;
		}
		if (right == null)
		{
			right = ZERO;
		}		
		return left.compareTo(right);
	}
	
	public static BigDecimal pow(BigDecimal base, BigDecimal p)
	{
		return doubleToDec(Math.pow(decToDouble(base), decToDouble(p)));
	}

	public static java.math.BigDecimal doubleToDec(double d)
	{
		return unexponentString(Double.toString(d));
	}
	
	public static java.math.BigDecimal doubleToDec(long l)
	{
		return unexponentString(Long.toString(l));
	}	

	public static java.math.BigDecimal stringToDec(String d)
	{
		
		return unexponentString(d);
	}

	public static String decToString(java.math.BigDecimal d)
	{
		return d.toString();
	}

	public static java.math.BigDecimal doubleToDec(double d, int len, int dec)
	{
		return new java.math.BigDecimal(CommonUtil.ltrim(CommonUtil.str(d, len, dec)));
	}

	public static double decToDouble(java.math.BigDecimal decimal)
	{
		return decimal.doubleValue();
	}

 	public static BigDecimal unexponentString(String num) 
   	{
		num = num.trim();
		
   		String exponent;
   		int scaleAdj = 0;
   		int epos = num.indexOf('E');
   		if	(epos == -1){
    		epos = num.indexOf('e');
   		}

   		if (epos != -1)
   		{
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
   		}

   		int point = num.indexOf('.');
   		int scale = num.length() - (point == -1 ? num.length () : point + 1) - scaleAdj;
  		
  		StringBuffer val = new StringBuffer(point == -1 ? num : num.substring(0, point) + num.substring (point + 1));
  
  		// correct for negative scale as per BigDecimal javadoc
  		for(; scale<0; scale++)
  		{
    		val.append("0");
   		}

		try
		{
			return new BigDecimal(val.toString()).movePointLeft(scale);
		}
		catch (java.lang.NumberFormatException e)
		{
			return ZERO;
		}
    }


}