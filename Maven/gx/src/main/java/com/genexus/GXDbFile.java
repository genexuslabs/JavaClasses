package com.genexus;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class GXDbFile
{
	public static String getScheme()
	{
		return "gxdbfile";
	}

	public static String getMultimediaDirectory()
	{
		return "multimedia";
	}

	private static Pattern schemeRegex = Pattern.compile("(^" + getScheme() + ":)(.+)");

	public static String getFileName(String uri)
	{
		try
		{
			return CommonUtil.getFileName(uri);
		}
		catch (Exception e)		
		{
			System.err.println("Bad File URI " + uri + " : " + e.getMessage());
			return uri;
		}
	}
	
	public static String getFileType(String uri)
	{
		try
		{
			return CommonUtil.getFileType(uri);
		}
		catch (Exception e)		
		{
			System.err.println("Bad File URI " + uri + " : " + e.getMessage());
			return uri;
		}
	}

	public static String addTokenToFileName(String name, String type)
	{
		if (name.trim().length() == 0)
			return "";
		String guid = java.util.UUID.randomUUID().toString().replaceAll("-", "");
		return name + "_" + guid + (type.trim().length() == 0 ? "" : ".") + type;
	}

	public static String removeTokenFromFileName(String fileName)
	{
		if (fileName.trim().length() == 0)
			return "";

		String name = getFileName(fileName);
		String type = getFileType(fileName);
		String cleanName = name;
		int sepIdx = name.lastIndexOf('_');
		if (sepIdx > 0)
		{
			Pattern pattern = Pattern.compile("(_[0-9a-zA-Z]{32})$");
			Matcher matcher = pattern.matcher(name.substring(sepIdx));

			if (matcher.find())
				cleanName = name.substring(0, sepIdx); 
		}

		return (type.trim().length() == 0) ? cleanName : cleanName + "." + type;
	}

	public static Boolean hasToken(String fileName)
	{
		File file = new File(fileName);
		return (file.getName().compareTo(GXDbFile.removeTokenFromFileName(file.getName())) != 0);
	}

	public static String getFileNameFromUri(String uriString)
	{
		if (uriString.trim().length() == 0)
			return "";

		Matcher matcher = schemeRegex.matcher(uriString);
		if (matcher.matches())
			return matcher.group(2);
		
		return uriString;
	}

	public static String resolveUri(String uriString)
	{
		if (uriString.trim().length() == 0)
			return "";
		
		Matcher matcher = schemeRegex.matcher(uriString);
		if (matcher.matches())
		{
			String fileName = matcher.group(2);
			File file = new File(Preferences.getDefaultPreferences().getMultimediaPath(), fileName);
			return pathToUrl(file.getPath());
		}

		return uriString;
	}
	
	public static String generateUri(String file, Boolean addToken, Boolean addScheme)
	{
		String name = getFileName(file);
		String type = getFileType(file);
		name = PrivateUtilities.encodeFileName(name);
		name = PrivateUtilities.checkFileNameLength(file.replace(name, ""), name, type);
				
		if (!addScheme)
		{
			return GXDbFile.addTokenToFileName(name, type);
		}
		else
		{
			if (addToken)
				return GXDbFile.getScheme() + ":" + GXDbFile.addTokenToFileName(name, type);
			else
				return GXDbFile.getScheme() + ":" +  name + (type.trim().length() == 0 ? "" : ".") + type;
		}
	}

	public static String getNameFromURL(String uri)
	{
		return getFileName(uri) + "." + getFileType(uri);
	}
	
	public static boolean isFileExternal(String uriString)
	{
		if (uriString.trim().length() == 0)
			return true;

		if (schemeRegex.matcher(uriString).matches())
			return false;
		
		return true;
	}

	private static String getUriFromFile(String name, String type)
	{
		String cleanName = name.trim();
		String cleanType = type.trim();
		return (cleanName.length() == 0 ? "multimedia-file" : cleanName) + "." + (cleanType.length() == 0 ? "tmp" : cleanType);
	}
	
	public static String getUriFromFile(String name, String type, String path)
	{
		if (name.length() == 0 && type.length() == 0 && path.length() != 0)
		{
			String fromPathType = getFileType(path);
			if (fromPathType.length() != 0 && !fromPathType.equals("tmp"))
			{
				return new File(path).getName();
			}
		}
		return getUriFromFile(name, type);
	}
	
	public static String getUrlFromUri(String blob, String uri)
	{
		return (blob.trim().length() == 0) ? uri : "";
	}
	
	public static String pathToUrl(String path, com.genexus.internet.HttpContext webContext)
	{
		if (path.startsWith("http:") || path.startsWith("https:") || path.startsWith("data:"))
			return path;

		if (webContext instanceof com.genexus.internet.HttpContextNull)
		{
			try
			{
				File file = new File(path);
				return file.toURL().toString();
			}
			catch (Exception e)
			{
			}
			
			return path;
		}

		return webContext.getResource(webContext.convertURL(path));
	}
	
	public static String pathToUrl(String path)
	{
		ModelContext context = ModelContext.getModelContext();
		com.genexus.internet.HttpContext webContext = context.getHttpContext();
		return pathToUrl(path, webContext);
	}

}