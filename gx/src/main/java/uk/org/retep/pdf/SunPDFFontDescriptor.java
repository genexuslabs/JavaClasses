package uk.org.retep.pdf;
import java.awt.*;
import java.io.*;
import java.util.*;
import com.genexus.reports.ParseINI;
import com.genexus.reports.Const;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: ARTECH
 * @author gb
 * @version 1.0
 */

/**
 *  Esta clase maneja los FontDescriptors de un Font (para los TrueType Fonts)
 */
 public class SunPDFFontDescriptor extends PDFFontDescriptor implements IPDFFontDescriptor
 {

//  private static Graphics2D BBoxConfig = new java.awt.image.BufferedImage(1,1, java.awt.image.BufferedImage.TYPE_INT_BGR).createGraphics();

  private ParseINI props = null;

  /** Inicializa un PDFObject FontDescriptor con Font embebido
   *  @param font PDFFont a describir
   */
  public void init(PDFFont font, boolean embeed)
  {
    this.font = font;
//    metrics = Toolkit.getDefaultToolkit().getFontMetrics(new Font(font.getRealFontName(), font.getStyle(), 72));
    metrics = PDFFontMetrics.getFontMetrics(font.getRealFontName(), (font.getStyle() & Font.BOLD) == Font.BOLD, (font.getStyle() & Font.ITALIC) == Font.ITALIC);
    this.embeed = embeed;
    props = font.getPDF().props;
  }

  protected String getTrueTypeFontLocation()
  {
    String fontLocation = props.getProperty(Const.SUN_FONT_LOCATION, font.getFontObject().getFontName());
    if(fontLocation != null || mappingsSearched)
      return fontLocation;
    mappingsSearched = true;

    if(props.getBooleanGeneralProperty(Const.SEARCH_FONTS_ALWAYS, false) ||
       props.getBooleanGeneralProperty(Const.SEARCH_FONTS_ONCE, false))
    {
      props.setGeneralProperty(Const.SEARCH_FONTS_ONCE, "false");

      // Si quiero buscar nuevos fonts, obtengo la lista completa de Fonts
      Enumeration enumera = Utilities.parseLine(Utilities.getPredefinedSearchPaths(), ";").elements();
      while(enumera.hasMoreElements())
      {
        File dir = new File((String)enumera.nextElement());
        File [] files = dir.listFiles();
        if(files != null)
         for(int i = 0; i < files.length; i++)
         {
          if(files[i].getName().toUpperCase().endsWith(".TTF"))
          {
            String absolutePath = files[i].getAbsolutePath();

            String mapped = TrueTypeFontCache.getFontFamilyName(files[i], 0, false);

            if(mapped != null)
              props.setProperty(Const.SUN_FONT_LOCATION, mapped, absolutePath);
          }
        }
       }
    }

    return props.getProperty(Const.SUN_FONT_LOCATION, font.getFontObject().getFontName());
 }

  public String getTrueTypeFontLocation(String fontName)
  {
	  return getTrueTypeFontLocation(fontName, null);
  }

  public String getTrueTypeFontLocation(String fontName, ParseINI props)
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
