
package com.genexus;

import java.util.*;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import java.io.*;


public class URLRouter
{
	public static final ILogger logger = LogManager.getLogger(URLRouter.class);

	static Hashtable<String, String> routerList;
	static final String RESOURCE_NAME = "urlrouter.txt";

	public static String getURLRoute(String key, String[] parms, String contextPath)
	{
		if	(routerList == null)
		{
			routerList = new Hashtable<>();
			load(RESOURCE_NAME);
		}

		String [] urlQueryString = key.split("\\?");
		Object[] urlarray = parms;
		if (urlQueryString.length > 1)
		{
			if (urlQueryString[1].contains("&"))
				urlarray = getParameters(urlQueryString[1].split("&"));
			else
				urlarray = urlQueryString[1].replaceAll("%2C", ",").split(",");
		}

		String url = routerList.containsKey(urlQueryString[0])? String.format(routerList.get(urlQueryString[0]), urlarray): urlQueryString[0];
		return contextPath + "/" + url + ((urlQueryString.length > 1)? "?" + urlQueryString[1]: convertParmsToQueryString(parms, routerList.get(urlQueryString[0])));
	}

	private static Object[] getParameters(String[] url)
	{
		Object[] urlParameters = new Object[url.length];
		for (int i = 0; i < url.length; i++)
		{
			urlParameters[i] = url[i].split("=")[1];
		}
		return urlParameters;
	}

	private static String convertParmsToQueryString(String[] parms, String routerRule)
	{
		if ((routerRule != null && routerRule.contains("/%1")) || (parms.length == 0))
		{
			return "";
		}
		String queryString = "?";
		for (int i = 0; i < parms.length; i++)
		{
			queryString = queryString + parms[i] + ((i < parms.length -1)? "," : "");
		}
		return queryString;
	}

	private static void load(String resourceName)
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
            logger.error(e.toString(), e);
          }
          catch(FileNotFoundException e)
          {
			  logger.info("There is no URLRouter file");
          }
          catch (IOException e)
          {
			  logger.error(e.toString(), e);
          }
        }

        private static void parseLine(String line)
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
    			routerList.put(code, builder.toString());
    		}
        }
}
