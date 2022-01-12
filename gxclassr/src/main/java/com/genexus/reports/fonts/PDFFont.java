
package com.genexus.reports.fonts;

import java.io.Serializable;

public class PDFFont implements Serializable
{

    /** Chequea a ver si el Font es un Type1
     *  @param fontName nombre del Font
     *  @return true si el font se mapea a un Type1
     */
    public static boolean isType1(String fontName)
    {
		String f = fontName.toLowerCase();
		for(int i=0;i<base14.length;i++)
		{
			if(base14[i][0].equals(f))
			{
				return true;
			}
		}
		for(int i = 0; i < Type1FontMetrics.CJKNames.length; i++)
		{
			if(Type1FontMetrics.CJKNames[i][0].equalsIgnoreCase(f) ||
			   Type1FontMetrics.CJKNames[i][1].equalsIgnoreCase(f))
			{
				return true;
			}			
		}
		return false;
    }

    /**
     * This maps the standard JDK1.1 font names and styles to
     * the base 14 PDF fonts
     */
    public static String[][] base14 = {
	// TTF name	      NORMAL		BOLD		ITALIC			BOLD+ITALIC
	{"sansserif",	"/Helvetica",	"/Helvetica-Bold","/Helvetica-Oblique",	"/Helvetica-BoldOblique"},
	{"monospaced",	"/Courier",	"/Courier-Bold","/Courier-Oblique",	"/Courier-BoldOblique"},
	{"timesroman",	"/Times-Roman",	"/Times-Bold",	"/Times-Italic",	"/Times-BoldItalic"},
	{"courier",	    "/Courier",	"/Courier-Bold","/Courier-Oblique",	"/Courier-BoldOblique"},
	{"helvetica",	"/Helvetica",	"/Helvetica-Bold","/Helvetica-Oblique",	"/Helvetica-BoldOblique"},
	{"dialog",	    "/Courier",	"/Courier-Bold","/Courier-Oblique",	"/Courier-BoldOblique"},
	{"dialoginput",	"/Courier",	"/Courier-Bold","/Courier-Oblique",	"/Courier-BoldOblique"},
    {"symbol",      "/Symbol",     "/Symbol", "/Symbol",          "/Symbol"},
    {"times",       "/Times-Roman", "/Times-Bold", "/Times-Italic", "/Times-BoldItalic"}, 
    {"zapfdingbats", "/ZapfDingBats", "/ZapfDingBats", "/ZapfDingBats", "/ZapfDingBats"}
    };
}

