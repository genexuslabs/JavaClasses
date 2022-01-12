package com.genexus;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.internet.*;
import java.text.*;
import java.util.*;
import java.io.*;

public class ImagesPath
{
	static Hashtable<String, String> imageId = new Hashtable<String, String>();
	static Hashtable<String, String> loadedTXT = new Hashtable<>();
	static Hashtable<String, HashSet<String>> imagesDensity = new Hashtable<String, HashSet<String>>();
	static Hashtable<String, String> imageList = null;
	public static final String RESOURCENAME = "Images.txt";

	public static String getImagePath(String image, String theme, String KBId, IHttpContext httpContext)
	{
		if (loadedTXT.get(KBId) == null)
		{
			loadImageList(KBId);
			loadedTXT.put(KBId, KBId);
		}

		String realImage = imageId.get(image);
		if (realImage != null)
		{
			image = realImage;
		}

		return imageList.get(KBId + getHashKey(image, theme, httpContext));
	}

	public static String getImageSrcSet(IHttpContext httpContext, String baseImage)
	{
		if (baseImage != null && baseImage.trim().length() > 0)
		{
			String staticContentBase = httpContext.getStaticContentBase();
			String key = baseImage;
			int pos = baseImage.lastIndexOf(staticContentBase);
			if (pos >= 0)
			{
				key = baseImage.substring(pos + staticContentBase.length());
			}

			if (imagesDensity.containsKey(key))
			{
				HashSet<String> densities = imagesDensity.get(key);
				String srcSet = "";
				Boolean first = true;
				String fileNameWithoutExtension = CommonUtil.getFileName(baseImage);
				String fileExtension = CommonUtil.getFileType(baseImage);
				String completeFileName = fileNameWithoutExtension + "." + fileExtension;
				String basePath = baseImage.substring(0, baseImage.lastIndexOf(completeFileName));
				for (String density : densities)
				{
					if (!first) {
						srcSet += ",";
					}

					srcSet += httpContext.convertURL(basePath + fileNameWithoutExtension + "-" + density.replace('.', '-') + "." + fileExtension) + " " + density;

					first = false;
				}
				return srcSet;
			}
		}

		return "";
	}

	private static String getHashKey(String imageId, String theme, IHttpContext httpContext)
	{
		//3=English,Default,
		String language = httpContext.getLanguage();
		return imageId + "=" + language + "," + theme;
	}

	private static void loadImageList(String KBId)
	{
			if (imageList == null)
			{
				imageList = new Hashtable<String, String>();
			}
			String line;
			try
			{
				InputStream is = SpecificImplementation.ImagesPath.getInputStream(KBId);
				if (is != null)
				{
					parseLocations = false;
					BufferedReader bufread = new BufferedReader(new InputStreamReader(is, "UTF8"));
					line = bufread.readLine();
					while (line != null)
					{
						parseLine(line, KBId);
						line = bufread.readLine();
					}
					bufread.close();
				}
			}
			catch (UnsupportedEncodingException e)
			{
				System.err.println(e.toString());
			}
			catch (FileNotFoundException e)
			{
				System.err.println(e.toString());
			}
			catch (IOException e)
			{
				System.err.println(e.toString());
			}
	}

	private static boolean parseLocations = false;

	private static void parseLine(String line, String KBId)
	{
		int separator;
		int finish = line.trim().length();
		String key;
		String imageType;
		String value;

		if (finish == 0)
		{
				return;
		}
		if (line.equals("[IdName]"))
		{
			parseLocations = false;
			return;
		}
		if (line.equals("[Location]"))
		{
			parseLocations = true;
			return;
		}
		if (parseLocations)
		{
			String[] parts = line.split(",");
			if (parts.length >= 4)
			{
				key = parts[0] + "," + parts[1];
				imageType = parts[2];
				if (imageType.equals("I"))
				{
					value = KBId + "Resources/" + parts[3];
				}
				else
				{
					value = parts[3];
				}

				imageList.put(KBId + key, value);

				if (parts.length > 4 && parts[4].trim().length() > 0)
				{
					String[] densitiesList = parts[4].split("\\|");
					for (String density : densitiesList)
					{
						if (density.trim().length() > 0)
						{
							HashSet<String> imgDensities;
							if (!imagesDensity.containsKey(value))
							{
								imgDensities = new HashSet<String>();
								imagesDensity.put(value, imgDensities);
							}
							else
							{
								imgDensities = imagesDensity.get(value);
							}
							imgDensities.add(density.substring(1));
						}
					}
				}
			}
		}
		else
		{
			separator = line.lastIndexOf("=");
			if (separator > 0)
			{
				value = line.substring(0, separator);
				key = line.substring(separator + 1, line.length());
				imageId.put(key, value);
			}
		}
	}
}
