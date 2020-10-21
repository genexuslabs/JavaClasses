
package com.genexus;

import java.util.*;

import com.genexus.common.classes.AbstractGXFile;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.GXDirectory;
import com.genexus.util.GXFileCollection;

import java.io.*;
import java.util.regex.Pattern;


public class URLRouter
{
	public static final ILogger logger = LogManager.getLogger(URLRouter.class);

	static Hashtable<String, String> routerList;
	private static boolean serverRelative = false;
	private static Pattern schemeRegex = Pattern.compile("^([a-z][a-z0-9+\\-.]*):",Pattern.CASE_INSENSITIVE);

	public static String getURLRoute(String key, String[] parms, String[] parmsName, String contextPath, String packageName)
	{
		return getURLRoute(ModelContext.getModelContext().getPreferences().getProperty("UseNamedParameters", "1").equals("1"), key, parms, parmsName, contextPath, packageName);
	}

	public static String getURLRoute(boolean useNamedParameters, String key, String[] parms, String[] parmsName, String contextPath, String packageName)
	{
		if (com.genexus.CommonUtil.isAbsoluteURL(key) || key.startsWith("/") || key.isEmpty() || schemeRegex.matcher(key).find()) {
			return ((parms.length > 0)? key + "?" + String.join(",", parms): key);
		}

		if (contextPath.length() > 0 || ModelContext.getModelContext().getHttpContext().isHttpContextWeb())
		{
			contextPath += "/";
		}


		String lowURL = CommonUtil.lower(key);

		if ((!packageName.equals("") && !lowURL.startsWith(packageName)) || (packageName.equals("") && !lowURL.equals(key)))
		{
			if (!packageName.equals(""))
				packageName += ".";
			try
			{
				SpecificImplementation.Application.getConfigurationClass().getClassLoader().loadClass(packageName + lowURL);
				key = packageName + lowURL;
			}
			catch(java.lang.ClassNotFoundException e)
			{
			}
		}

		//If it is a File contextPath must be added
		if (key.split("\\.").length == 2 && !(!packageName.equals("") && lowURL.startsWith(packageName)))
		{
			if (key.indexOf("?") == -1 || key.indexOf("?") > key.indexOf("."))
			{
				return contextPath + key;
			}
		}

		if	(routerList == null)
		{
			routerList = new Hashtable<>();
			load();
		}

		String [] urlQueryString = key.split("\\?");
		Object[] urlarray = parms;
		if (urlQueryString.length > 1)
		{
			if (urlQueryString[1].contains("="))
				urlarray = getParameters(urlQueryString[1].split("&"));
			else
				urlarray = urlQueryString[1].replaceAll("%2C", ",").split(",");
		}

		String url = routerList.containsKey(urlQueryString[0])? String.format(routerList.get(urlQueryString[0]), urlarray): urlQueryString[0];
		return (serverRelative? contextPath : "") + url + ((urlQueryString.length > 1)? "?" + urlQueryString[1]: convertParmsToQueryString(useNamedParameters, parms, parmsName, routerList.get(urlQueryString[0])));
	}

	private static Object[] getParameters(String[] url)
	{
		Object[] urlParameters = new Object[url.length];
		for (int i = 0; i < url.length; i++)
		{
			urlParameters[i] = "";
			if (url[i].split("=").length > 1)
				urlParameters[i] = url[i].split("=")[1];
		}
		return urlParameters;
	}

	private static String convertParmsToQueryString(boolean useNamedParameters, String[] parms, String[] parmsName, String routerRule)
	{
		if ((routerRule != null && routerRule.contains("%1")) || (parms.length == 0))
		{
			return "";
		}
		String queryString = "?";
		for (int i = 0; i < parms.length; i++)
		{
			if (!useNamedParameters || parms.length != parmsName.length)
				queryString = queryString + parms[i] + ((i < parms.length -1)? "," : "");
			else
				queryString = queryString + parmsName[i] + "=" + parms[i] + ((i < parms.length -1)? "&" : "");
		}
		return queryString;
	}

	private static void load()
	{
		String line;
		InputStream is = null;
		String defaultPath = SpecificImplementation.Application.getModelContext().getHttpContext().getDefaultPath();
		String appPackage = SpecificImplementation.Application.getClientPreferences().getPACKAGE();
		if	(!appPackage.equals(""))
			appPackage = File.separatorChar + appPackage.replace('.', File.separatorChar);
		String classesDirectoryPath = defaultPath + File.separator + "WEB-INF" + File.separatorChar + "classes" + appPackage;
		GXDirectory classesDirectory = new GXDirectory(classesDirectoryPath);
		GXFileCollection rewriteFiles = classesDirectory.getFiles(".rewrite");
		if (rewriteFiles != null)
		{
			for (int i = 1; i <= rewriteFiles.getItemCount(); i++)
			{
				serverRelative = true;
				AbstractGXFile rewriteFile = rewriteFiles.item(i);
				try
				{
					is = SpecificImplementation.Messages.getInputStream(rewriteFile.getName());

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
				catch (UnsupportedEncodingException e)
				{
					logger.error(e.toString(), e);
				}
				catch (FileNotFoundException e)
				{
					logger.info("There is no URLRouter file");
				}
				catch (IOException e)
				{
					logger.error(e.toString(), e);
				}
			}
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

				if (ch != 61 && ch!= 92)
      				builder.append(ch);
      			else if (ch==61 && escaped)
      				builder.append(ch);
      			else if (ch==92 && escaped)
      				builder.append(ch);

      			if (ch == 61 && !escaped)
      			{
      				if (hasEqual)
					{
						builder.append(ch);
					}
      				else {
						code = builder.toString();
						hasEqual = true;
						builder = new StringBuffer();
					}
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
