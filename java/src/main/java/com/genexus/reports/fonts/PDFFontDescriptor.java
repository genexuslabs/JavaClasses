package com.genexus.reports.fonts;
import java.io.File;
import java.util.Enumeration;

import com.genexus.reports.Const;
import com.genexus.reports.ParseINI;

/**
 *  Esta clase maneja los FontDescriptors de un Font (para los TrueType Fonts)
 */
 public class PDFFontDescriptor
 {
 	public static String getTrueTypeFontLocation(String fontName, ParseINI props)
	{
		  String stripedFontName = fontName;
		  if (fontName.indexOf(",") != -1)
		  {
			  stripedFontName = fontName.substring(0, fontName.indexOf(","));
		  }

		String fontLocation = props.getProperty(Const.SUN_FONT_LOCATION, fontName);
		if(fontLocation != null)
		  return fontLocation;

		// Si quiero buscar nuevos fonts, obtengo la lista completa de Fonts
		//sun.awt.font.NativeFontWrapper fontMapper = new sun.awt.font.NativeFontWrapper();
			Enumeration enumera = Utilities.parseLine(Utilities.getPredefinedSearchPaths(), ";").elements();
			while(enumera.hasMoreElements())
			{
			  File dir = new File((String)enumera.nextElement());
			  File [] files = dir.listFiles();
			  if(files != null)
			   for(int i = 0; i < files.length; i++)
			   {
				if(files[i].getName().toUpperCase().endsWith(".TTF") || files[i].getName().toUpperCase().endsWith(".TTC"))
				{
				  String absolutePath = files[i].getAbsolutePath();
						  String mapped = TrueTypeFontCache.getFontFamilyName(files[i], 0, false);
						  if (mapped != null && mapped.equalsIgnoreCase(stripedFontName)) {
							if (files[i].getName().toUpperCase().endsWith(".TTC"))
							  absolutePath = absolutePath + ",0";
							props.setProperty(Const.SUN_FONT_LOCATION, fontName,
											  absolutePath);
							return absolutePath;
							//props.setProperty(Const.SUN_FONT_LOCATION, fontName, absolutePath);
							//  return props.getProperty(Const.SUN_FONT_LOCATION, fontName);
						  }
				}
			  }
			}
			return "";
	}
}
