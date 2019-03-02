package com.genexus.util;

import java.io.ByteArrayOutputStream;

public class Encoder
{
	/**
	* Codifica una URL. Asume que los parametros (lo que va despues del ?) ya esta codificado,
	* por lo que no codifica los caracteres que significan algo en el file system.
	*/

    static final int caseDiff = ('a' - 'A');
	public static String encodeURL(String url)
	{
		StringBuffer out = new StringBuffer();

		char ch;
		int  length = url.length();

		for (int i = 0; i < length; i++)
		{
			char p = url.charAt(i);

			if ((p >= 'a' && p <= 'z') ||
				(p >= 'A' && p <= 'Z') ||
				(p >= '0' && p <= '9') ||
				(p == '_') ||
				(p == '-') ||
				(p == '\\') ||
				(p == '/') ||
				(p == ':') ||
				(p == '?') ||
				(p == '.') ||
				(p == '*'))
			{
				out.append(p);
			}
			else
			{
		    	out.append('%');
		    	ch = Character.forDigit((p >> 4) & 0xF, 16);
		    	if (Character.isLetter(ch)) 
		    	{
					ch -= caseDiff;
		    	}
		    	out.append(ch);

		    	ch = Character.forDigit(p & 0xF, 16);
		    	if (Character.isLetter(ch)) 
		    	{
					ch -= caseDiff;
			    }
		    	out.append(ch);
			}
		}

		return out.toString();
	}

	public static String encodeParm(String parm)
	{
		StringBuffer out = new StringBuffer();

		char ch;
		int  length = parm.length();
		for (int i = 0; i < length; i++)
		{
			char p = parm.charAt(i);

			if (p != ' ')
			{
				out.append(p);
			}
			else
			{
		    	out.append('+');
			}
		}

		return out.toString();
	}

  	public static String decodeURL(String s) 
  	{
  
    	ByteArrayOutputStream out = new ByteArrayOutputStream(s.length());
  
    	for (int i = 0; i < s.length(); i++) 
    	{
      		int c = (int) s.charAt(i);
      		if (c == '+') {
        		out.write(' ');
      		}
      		else if (c == '%') 
      		{
		        int c1 = Character.digit(s.charAt(++i), 16);
		        int c2 = Character.digit(s.charAt(++i), 16);
		        out.write((char) (c1 * 16 + c2));
      		}
      		else 
      		{
        		out.write(c);
      		}
    	} // end for

    	return out.toString();
  
	}

}