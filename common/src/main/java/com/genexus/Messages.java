
package com.genexus;

import java.text.*;
import java.util.*;

import com.genexus.common.interfaces.SpecificImplementation;

import java.io.*;


public class Messages
{
	Properties messages;
	MessageFormat  formatter;
	static Hashtable messageList = new Hashtable();

	private Messages(String resourceName, Locale formatterLocale)
	{
		messages = new Properties();
		load(resourceName);

		formatter = new MessageFormat("");
		formatter.setLocale(formatterLocale);
	}

	public static Messages getMessages(String resourceName, Locale formatterLocale)
	{
		Messages msg = (Messages) (messageList.get(resourceName));
		if	(msg == null)
		{
			msg = new Messages(resourceName, formatterLocale);
			messageList.put(resourceName, msg);
		}

		return msg;
	}

	public static void endMessages()
	{
		messageList = null;
	}

	public String getMessage(String key1)
	{
		boolean trimSpaces = key1 != null && key1.length() > 0 && (key1.charAt(0) == ' ' || key1.charAt(key1.length() - 1) == ' ');
		String key = trimSpaces ? key1.trim() : key1;
		String value = messages.getProperty(key);
		return (value != null)? (trimSpaces ? copySpaces(key1, value) : value) :key1;
	}

	public String getMessage(String key1, Object[] arguments)
	{
		boolean trimSpaces = key1 != null && key1.length() > 0 && (key1.charAt(0) == ' ' || key1.charAt(key1.length() - 1) == ' ');
		String key = trimSpaces ? key1.trim() : key1;
		String strFormat = messages.getProperty(key);
        if (strFormat == null)
        {
            strFormat = key1;
        }
		else
		{
			synchronized(this)
			{
				formatter.applyPattern(strFormat);
				strFormat = formatter.format(arguments);
			}
            if (trimSpaces)
                strFormat = copySpaces(key1, strFormat);
		}

		return strFormat;
	}

		private String copySpaces(String strId1, String strFormat)
		{
			StringBuffer str = new StringBuffer();
			for (int i = 0; i < strId1.length(); i++)
			{
				if (strId1.charAt(i) == ' ')
				{
					str.append(' ');
				}
				else
				{
					str.append(strFormat);
					break;
				}
			}
			for (int i = strId1.length()-1; i >=0; i--)
			{
				if (strId1.charAt(i) == ' ')
				{
					str.append(' ');
				}
				else
				{
					break;
				}
			}
			return str.toString();
		}

	protected static final String getTab()
	{
		return "                ";
	}

	private void load(String resourceName)
	{
		String line;
		InputStream is = null;
		try
		{
			is = SpecificImplementation.Messages.getInputStream(resourceName);

            if (is != null)
            {
              BufferedReader bufread = new BufferedReader(new InputStreamReader(is, "UTF8"));
              line = bufread.readLine();
              while (line != null)
              {
                parseLine(line);
                line = bufread.readLine();
              }
              bufread.close();
            }
          }
          catch(UnsupportedEncodingException e)
          {
            System.err.println(e.toString());
          }
          catch(FileNotFoundException e)
          {
            System.err.println(e.toString());
          }
          catch (IOException e)
          {
            System.err.println(e.toString());
          }
        }

        private void parseLine(String line)
        {
    		int len = line.length();
    		boolean escaped = false;
    		StringBuffer builder = new StringBuffer();
    		String code = null;
    		boolean hasEqual = false;

    		for( int i = 0; i < len; i++)
    		{
      			char ch = line.charAt(i);

				if (ch != 61 && ch!= 92) //caracter que no es signo de igual ni barra
      				builder.append(ch);
      			else if (ch==61 && escaped) //signo de igual escapeado
      				builder.append(ch);
      			else if (ch==92 && escaped) //barra escapeada
      				builder.append(ch);

      			if (ch == 61 && !escaped) //signo de igual no escapeado: separador de codigo=descripcion
      			{
      				code = builder.toString(); //guarda codigo y resetea builder
      				hasEqual = true;
      				builder = new StringBuffer();
      			}

      			if (escaped)
      			{
      				escaped=false;
      			}
      			else if (ch == 92)
      			{
      				escaped = true;
      			}
    		}
    		if (len>0 && code!=null && hasEqual && builder.length()>0)
    		{
    			messages.put(code, builder.toString());
    		}
        }
}
