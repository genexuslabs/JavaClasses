// $Log: PDFFont.java,v $
// Revision 1.5  2004/10/25 15:30:05  iroqueta
// Implementacion de los reportes PDFs usando la lib iText
//
// Revision 1.4  2004/09/27 16:46:22  iroqueta
// Ciertos caracteres japoneses no se mostraban en forma correcta...
// A esos caracteres se le agrega un "\" luego del mismo para que se vean en forma correcta.
//
// Revision 1.3  2004/03/03 18:12:22  gusbro
// - Cambios para soportar fonts CJK
//
// Revision 1.2  2003/03/18 18:51:03  gusbro
// - Cambio en la manera de calcular el width de un string, para que no falle con caracteres fuera de rango 0-255, para fonts Type1
//
// Revision 1.1.1.1  2001/08/10 18:27:48  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/08/10 18:27:48  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This class defines a font within a PDF document.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public class PDFFont extends PDFObject implements Serializable
{
    private static int GAP = -3; // Esto es para que quede 'a lo GXReport'
//	private static java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
    private PDFFontDescriptor fontDescriptor = null; // FontDescriptor de este Font (necesario en TrueType Fonts)

    /**
     * The PDF document name of the font
     */
    private String name;

    /**
     * The PDF type of the font, usually /Type1
     */
    private String type;

    /**
     * The font's real name
     */
    private String font;

    /**
     * The name of the equivalent Java font
     */
    private String javaFont;

    /**
     * The PDF Style, ie: BOLD, ITALIC, etc
     */
    private int style;

    // Mantiene los datos del Nombre, style,etc. PERO NO del SIZE. TODOS fonts con el mismo nombre y style comparten el mismo PDFFont, aunque el size sea distinto
    private Font f;
	
	private boolean isCJK = false;

    /**
     * This constructs a default PDFFont. In this case Helvetica
     */
    protected PDFFont() {
	this("/F1","/Type1",new Font("Helvetica",Font.PLAIN, 12));
    }

    /**
     * Constructs a PDFFont. This will attempt to map the font from a known
     * Java font name to that in PDF, defaulting to Helvetica if not possible.
     *
     * @param name The document name, ie /F1
     * @param type The pdf type, ie /Type1
     * @param font The font name, ie Helvetica
     * @param style The java.awt.Font style, ie: Font.PLAIN
     */
    public PDFFont(String name,String type, Font f) // String font,int style) {
    {
		super("/Font");
		this.name = name;
		this.type = type;
		this.style = f.getStyle();
        this.f = f;
        if(type.equalsIgnoreCase("/TrueType"))
        { // Si se trata de un TrueType Font
          this.font = f.getName();
          this.javaFont = this.font;
        }
        else
        {
			String font = f.getName().toLowerCase();
			// default PDF Font name
			this.font = base14[0][1];
			this.javaFont = base14[0][0];

			// attempt to translate the font name from Java to PDF
			boolean done = false;
			for(int i=0;i<base14.length;i++)
			{
				if(base14[i][0].equals(font)) {
					this.javaFont = base14[i][0];
					this.font =  base14[i][1+style];
					done = true;
					break;
				}
			}
			if(!done)
			{ // Veo si es un font Type1 CJK
				for(int i = 0; i < Type1FontMetrics.CJKNames.length; i++)
				{
					if(Type1FontMetrics.CJKNames[i][0].equalsIgnoreCase(font) ||
					   Type1FontMetrics.CJKNames[i][1].equalsIgnoreCase(font))
					{
						this.javaFont = Type1FontMetrics.CJKNames[i][1] + getCJKStyleString(f);
						this.font = "/" + Type1FontMetrics.CJKNames[i][1] + getCJKStyleString(f);
						done = true;
						isCJK = true;
						break;
					}
				}
			}
        }
    }
	
	protected String getCJKStyleString(Font f)
	{
		String ret = "";
		if((f.getStyle() & f.BOLD) != 0)
		{
			ret = ",Bold";
			if((f.getStyle() & f.ITALIC) != 0)
			{
				ret += "Italic";
			}
		}
		else
		{
			if((f.getStyle() & f.ITALIC) != 0)
			{
				ret = ",Italic";
			}
		}
		return ret;
	}
        
    
    /** Obtiene el width de este String en este font en el PDF
     * Si el font NO tiene asociado un fontDescriptor, se toma el stringWidth del font JAVA como valor
     * Si el font SI tiene asociado un fontDescritor, se obtiene el stringWidth calculado de �l
     * @param str String a procesar
     * @param size Size del font a usar
     * @return width del String asociado al font
     */
    public int stringWidth(String str, int size)
    {
		if(isCJK)
		{ // Si el font es un CJK calculamos distinto el width
			double calcWidth = str.length() * Type1FontMetrics.CJK_MISSING_WIDTH * 0.0127f * size * PDFFontDescriptor.WIDTH_SCALE_FACTOR;
/*
			Si los widths fuesen diferenciales (para los caracteres comunes) entonces
			podriamos hacer algo como esto, le da el doble de width a los caracteres chinos que
			a los caracteres comunes
			double calcWidth = 0;
			for(int i = 0; i < str.length(); i++)
			{
				calcWidth += Type1FontMetrics.CJK_MISSING_WIDTH * 0.08f * str.substring(i,i+1).getBytes().length * PDFFontDescriptor.WIDTH_SCALE_FACTOR;
			}
*/
			return (int)(calcWidth);
		}
        if(fontDescriptor == null)
        { // En el caso de ser un font Type1
            double calcWidth = 0;
            int metrics [] = Type1FontMetrics.getType1Metrics(font);
            for (int i = 0; i < str.length(); i++)
			{
				int curMetric;
				try
				{
					curMetric = metrics[str.charAt(i)];
				}catch(ArrayIndexOutOfBoundsException e)
				{ // Podr�}mos querer obtener las m�tricas de un caracter fuera de rango (0-255)
				  // En ese caso dejamos 600
					curMetric = 600;
				}
                calcWidth +=  curMetric * 0.001f * size * PDFFontDescriptor.WIDTH_SCALE_FACTOR;
			}
            return (int)calcWidth;
        }
        else return fontDescriptor.stringWidth(str, size);
    }
    
    
    /** Obtiene el width de este String en este font en el PDF
     * Si el font NO tiene asociado un fontDescriptor, se toma el stringWidth del font JAVA como valor
     * Si el font SI tiene asociado un fontDescritor, se obtiene el stringWidth calculado de �l
     * @param str String a procesar
     * @param size Size del font a usar
     * @return width del String asociado al font
     */
    public int stringWidth(String str, int size, String [] retString, int maxWidth)
    {
        if(fontDescriptor == null)  // Si el font es un Type1
        {
            for(int i = 0; i < str.length(); i++)
            {
                int width = stringWidth(str.substring(0, str.length() - i), size);
                if(width < maxWidth)
                {
                    retString[0] = str.substring(0, str.length() - i);
                    return width;
                }
            }            
            retString[0] = "";
            return 0;
        }
        else return fontDescriptor.stringWidth(str, size, retString, maxWidth);
    }
    
    /** Obtiene el height de este font en el size pasado como par�metro
     * @param size Tama�o del font 
     * @return height del este font en el size especificado
    */     
    public int getHeight(int size)
    {
        // OJO que ahora esta DISTINTO este getHeight, que al principio... hay que verificar que sean equivalentes
//        java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
//        return toolkit.getFontMetrics(new Font(f.getName(), f.getStyle(), size)).getHeight() + toolkit.getFontMetrics(f).getMaxAscent() - toolkit.getFontMetrics(f).getDescent() + GAP;

        // A los fonts Type1 vamos a tener que manejarlos un poco distinto para que el resultado
        // sea el esperado. 
        // Esto es medio el 'hand-tuned' de los par�metros DIV y GAP
        // Si DIV aumenta, suben de posicion los textos. Cuanto m�s size, sube m�s
        // Si el GAP aumenta, bajan todos los textos pod igual
        if(this.getFontDescriptor() == null)
        {
            double DIV = 8.75; 
            int GAP = 4;
            return (int)(((double)Type1FontMetrics.Type1MetricsHeightMapper[5] * size / DIV) / 72) + GAP;
        }
        
          PDFFontMetrics metrics = PDFFontMetrics.getFontMetricsSize(new Font(f.getName(), f.getStyle(), size));
          return metrics.getHeight() + metrics.getMaxAscent() - metrics.getDescent() + GAP;          
    }

    /** Convierte el nombre del TrueType Font al nombre del PDFFont a usar
     * @param font Nombre del TrueType Font
     * @return String con el nombre del Font PDF
     */
    public String convertTrueTypeFontName(String font)
    {
      String retFont = "/";
      int offset = 0;
      int index;
      while((index = font.indexOf(' ', offset)) != -1)
      {
        retFont = retFont + font.substring(offset, index);
        offset = index + 1;
      }
      if(offset < font.length())
        retFont += font.substring(offset);

      return retFont.trim();
    }


    /** Obtiene el nombre del TrueTypeFont cuando NO es embebido
     *  Este nombre tiene un formato especial para ser reconocido por Acrobat
     *  @return String con el nombre del TrueTypeFont que Acrobat entiende
     */
    private String getNotEmbeededFontName()
    {
      String font = convertTrueTypeFontName(f.getName());
      if(f.getStyle() == Font.PLAIN) return font;
      else font += ",";
      if((f.getStyle() & Font.BOLD) != 0)
        font += "Bold";
      if((f.getStyle() & Font.ITALIC) != 0)
        font += "Italic";
      return font;
    }

    /** Setea el FontDescriptor para este font (necesario en fonts TrueType)
     *  @param fontDescriptor asociado a este font
     */
    public void setFontDescriptor(PDFFontDescriptor fontDescriptor)
    {
        this.fontDescriptor = fontDescriptor;
    }

    /** Obtiene el FontDescriptor de este font
     *  @return FontDescriptor de este font o null si no tiene
     */
    public PDFFontDescriptor getFontDescriptor()
    {
      return fontDescriptor;
    }


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
     * This is the most common method to use.
     * @return the Font name within the PDF document.
     */
    public String getName() {
	return name;
    }

    /**
     * @return the Font's PDF type
     */
    public String getType() {
	return type;
    }

    /**
     * @return The PDF Font name
     */
    public String getFont() 
	{
        if(type.equalsIgnoreCase("/Type1"))
		{
  			return font;
		}
        if(fontDescriptor != null &&
           fontDescriptor.isEmbeeded())
             return convertTrueTypeFontName(font);
        else return getNotEmbeededFontName();

    }

    /** Ya hay un getFont, asi que lo llamamos getFontObject -> objeto Font
     *  @return Font con los datos del Font
     */
    public Font getFontObject()
    {
      return f;
    }

    /**
     * @return The real Font name
     */
    public String getRealFontName() 
	{
		return javaFont;
    }


    /**
     * @return the font style.
     * @see java.awt.Font
     */
    public int getStyle()
    {
		return style;
    }

    /**
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public void write(OutputStream os) throws IOException
    {
		writeStart(os);

		if(type.equalsIgnoreCase("/Type1"))
		{
			if(isCJK)
			{
				// Creo un font descriptor para este font
				setFontDescriptor(PDFFontDescriptor.getPDFFontDescriptor());
				fontDescriptor.init(this, false); // El font NO esta embebido!
				fontDescriptor.setMissingWidth(Type1FontMetrics.CJK_MISSING_WIDTH);
				pdf.add((PDFFontDescriptor)fontDescriptor); // Agrego el fontDescriptor y obtengo el SerialID
			}
			writeType1(os);
		}else 
		{
			writeTrueType(os);
		}

		if(fontDescriptor != null)
		{
			os.write(("\r\n/FirstChar 0").getBytes());
			os.write(("\r\n/LastChar 255").getBytes());
			os.write(("\r\n/FontDescriptor " + fontDescriptor.getSerialID() + " 0 R").getBytes());
			os.write(("\r\n/Widths " + fontDescriptor.getWidths()).getBytes());
		}
		
		os.write("\r\n".getBytes());
		writeEnd(os);
    }

    public void writeTrueType(OutputStream os) throws IOException
    {
		// now the objects body
		os.write("/Subtype ".getBytes());
		os.write(type.getBytes());
		os.write("\r\n/Name ".getBytes());
		os.write(name.getBytes());
		os.write("\r\n/BaseFont ".getBytes());
		os.write(getFont().getBytes());
		// The performance problem in Bug#106693 comments out the
		// encoding line, and removes the /WinAnsiEncoding. I'm going
		// to leave them in, as the Encoding fixes another problem.
		os.write("\r\n/Encoding ".getBytes());		
		os.write("/WinAnsiEncoding".getBytes());
		//os.write(encoding.getBytes());
    }

    public void writeType1(OutputStream os) throws IOException
    {		
		// now the objects body
		os.write("/Subtype ".getBytes());
		os.write(type.getBytes());
		os.write("\r\n/Name ".getBytes());
		os.write(name.getBytes());
		os.write("\r\n/BaseFont ".getBytes());
		os.write(font.getBytes());
		// The performance problem in Bug#106693 comments out the
		// encoding line, and removes the /WinAnsiEncoding. I'm going
		// to leave them in, as the Encoding fixes another problem.
		os.write("\r\n/Encoding ".getBytes());
		os.write("/WinAnsiEncoding".getBytes());
		//os.write(encoding.getBytes());
    }
	
    /**
     * This is used by the PDF and PDFPage classes to compare font names
     *
     * @param type The pdf type, ie /Type1
     * @param font The font name, ie Helvetica
     * @param style The java.awt.Font style, ie: Font.PLAIN
     * @return true if this object is identical to this font's spec
     */
    protected boolean equals(String type,String font,int style) {
	return this.type.equals(type)
	    && (this.font.equals(font)
		|| this.javaFont.equalsIgnoreCase(font)) && this.style == style;

	// Removed in fix for Bug#106693
	//&& this.style==style;
    }
	
	public boolean isCJK()
	{
		return isCJK;
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

