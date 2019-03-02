// $Log: PDFFontDescriptor.java,v $
// Revision 1.4  2005/03/11 22:12:26  iroqueta
// Arreglo para que no se consuma memoria cuando se quiere usar un font TrueType
//
// Revision 1.3  2004/10/25 15:30:05  iroqueta
// Implementacion de los reportes PDFs usando la lib iText
//
// Revision 1.2  2004/03/03 18:12:22  gusbro
// - Cambios para soportar fonts CJK
//
// Revision 1.1.1.1  2002/06/24 20:39:36  gusbro
// Entran los fuentes al CVS
//
// Revision 1.2  2002/06/24 20:39:36  gusbro
// *** empty log message ***
//
// Revision 1.1.1.1  2001/08/22 18:20:14  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;
import java.awt.*;
import java.io.*;

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
 public abstract class PDFFontDescriptor extends PDFObject implements IPDFFontDescriptor
 {
//  protected FontMetrics metrics;// Contiene la metricas para este font
  protected PDFFontMetrics metrics; // Contiene las metricas para este font
  protected boolean embeed = false;  // Indica si se debe embeber el Font
  protected String fontFileLocation = null; // Indica la ubicaci�n del FontFile

  protected PDFFont font;       // PDFFont del Font a describir
  protected PDFFontStream embeededFont = null;   // Contiene el Stream del Font embebido si es que se va a embeber
  protected boolean done = false; // Indica si ya se ha buscado el Font de este FontDescriptor
  protected static boolean mappingsSearched = false; // Indica si ya se han buscado los mappings en esta ejecuci�n (global a todos los FontDescriptors)

  public PDFFontDescriptor()
  {
    super("/FontDescriptor");
  }
  /** Indica si se ha podido embeber el font
   *  @return boolean True si el font ser� embebido
   */
  public boolean isEmbeeded()
  {
    return embeed && (getEmbeededFontStream() != null);
  }

  /** Retorna la ubicacion del FontFile
   *  @return String con la ubicaci�n del FontFile o null si no se encontr�
   */
  public String getFontFileLocation()
  {
    return fontFileLocation;
  }

  /** Convierte las m�tricas Java a las m�tricas PDF
   *  @param value valor en M�tricas Java
   *  @return int con el valor en M�tricas PDF
   */
  public static int convertMetrics(int value)
  {
    return  (int) ((1000 * value) / 72);
  }

  /** Aplica la inversa a la funcion 'convertMetrics'
   *  NOTA: Como trabajamos con valores discretor, f'�f(x) <= x
   * @param metricas PDF
   * @return metricas JAVA
   */
  public static int revertMetrics(int value)
  {
      return (int) (value * .072);
  }

  public static double WIDTH_SCALE_FACTOR = 1.33; // Factor de escalamiento para el width

    /** Calcula el width de un String
     * El width se calcula como la suma de los convertMetrics(width de cada caracter), a los
     *  que se los escala al size del font, y se lo multiplica por un factor constante
     * @param str String a medir su width en PDF
     * @param size tama�o del fuente
     */
    public int stringWidth(String str, int size)
    {
        double calcWidth = 0;

        for (int i = 0; i < str.length(); i++)
            calcWidth +=  convertMetrics(metrics.charWidth(str.charAt(i)));
        return  (int)(calcWidth * 0.001f * size * WIDTH_SCALE_FACTOR);
    }


    /** Calcula el width de un String pero con width m�ximo
     * Se calcula el width del String siempre y cuando el width sea menor que el width m�ximo, sino se
     * calcula el width del string cortado para que no pase el width m�ximo. Se retorna el retString[0]
     * el strig cortado.
     * El width se calcula como la suma de los convertMetrics(width de cada caracter), a los
     *  que se los escala al size del font, y se lo multiplica por un factor constante
     * @param str String a medir su width en PDF
     * @param size tama�o del fuente
     * @param retString[0] va a contener el String croppeado
     * @param maxWidth tama�o m�ximo del width deseado
     */
    public int stringWidth(String str, int size, String [] retString, int maxWidth)
    {
        double calcWidth = 0;

        for (int i = 0; i < str.length(); i++)
        {
            double calcWidthChar =  convertMetrics(metrics.charWidth(str.charAt(i))) * 0.001f * size * WIDTH_SCALE_FACTOR;
            if(calcWidth + calcWidthChar > maxWidth)
            {
                try
                {
                    retString[0] = str.substring(0, i - 1);
                }catch(ArrayIndexOutOfBoundsException firstChar)
                { // Si no entra ning�n caracter
                    retString[0] = "";
                }
                return (int)calcWidth;
            }
            else calcWidth += calcWidthChar;
        }

        retString[0] = str;
        return  (int)calcWidth;
    }
	
	private int missingWidth = 0;
	public void setMissingWidth(int missingWidth)
	{
	  this.missingWidth = missingWidth;
	}


  /** Obtiene la lista de par�metros de las M�tricas PDF
   *  @return String con la representaci�n de las metricas PDF
   */
  public String getWidths()
  {
      String widths = "\r\n[";
      int values [] = metrics.getWidths();
      for(int i = 0; i < values.length; i++)
        widths = widths + convertMetrics(values[i]) + (i % 16 == 15 ? "\r\n " : " ");
      widths = widths.substring(0, widths.length() - 1) + "]";
      return widths;
  }

 public void write(OutputStream os) throws java.io.IOException
  {
	writeStart(os);

	os.write(("\n/FontName " + font.getFont()).getBytes());
        if(isEmbeeded())os.write(("\n/ItalicAngle " + (((font.getStyle() & Font.ITALIC) == Font.ITALIC) ? "15" : "0")).getBytes());
	else os.write(("\n/ItalicAngle 0").getBytes());

    java.awt.Rectangle BBox = new java.awt.Rectangle(0, metrics.getMaxDescent(), metrics.getMaxAdvance(), metrics.getMaxAscent()); // Ver esto
    os.write(("\n/FontBBox[" + (convertMetrics(BBox.x)) + " " + (convertMetrics(BBox.y)) + " " + (convertMetrics(BBox.width)) + " " + (convertMetrics(BBox.height)) + "]").getBytes());
//    El BBox comentado es mejor, pero necesita usar las metricas del java.awt
//    java.awt.geom.Rectangle2D BBox = metrics.getMaxCharBounds(BBoxConfig);
//    os.write(("\n/FontBBox[" + (convertMetrics( (int)BBox.getMinX())) + " " + (convertMetrics( (int)BBox.getMinY())) + " " + (convertMetrics( (int)BBox.getMaxX())) + " " + (convertMetrics( (int)BBox.getMaxY())) + "]").getBytes());

	os.write("\n/Flags 96".getBytes());
	os.write(("\n/Descent -" + convertMetrics(metrics.getDescent())).getBytes());
	os.write(("\n/Ascent " + convertMetrics(metrics.getAscent())).getBytes());
	os.write(("\n/CapHeight " + convertMetrics(metrics.getHeight())).getBytes());
//	os.write("\n/StemV 60".getBytes());
	if(embeededFont != null) // Si debo embeber el FontFile
	{
		os.write(("\n/FontFile2 " + embeededFont.getSerialID() + " 0 R").getBytes());
	}
	if(missingWidth != 0)
	{
		os.write(("\n/MissingWidth " + convertMetrics(missingWidth)).getBytes());
	}

	os.write("\n".getBytes());
	writeEnd(os);
  }

  /** Obtiene el PDFFontStream del Font embebido
   *
   */
  public PDFFontStream getEmbeededFontStream()
  {
    if(!embeed)return null;
    if(done)return embeededFont;
    done = true;
    fontFileLocation = getTrueTypeFontLocation();
    if(fontFileLocation == null)
    {
      return null;
    }
    try
    {
      embeededFont = new PDFFontStream();
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(fontFileLocation));
      OutputStream out = embeededFont.getOutputStream();
      byte chunk[] = new byte[2048];
      int thisChunk;
      while((thisChunk = in.read(chunk)) != -1)
        out.write(chunk, 0, thisChunk);
      in.close();
      return embeededFont;
    }catch(IOException e)
    {
        return null;
    }
  }

  protected abstract String getTrueTypeFontLocation(); // {  return null;  } // Este m�todo debe ser 'overriden' en las clases derivadas;
  public abstract String getTrueTypeFontLocation(String fontName);
  public abstract String getTrueTypeFontLocation(String fontName, com.genexus.reports.ParseINI props);
  
  
  public static PDFFontDescriptor getPDFFontDescriptor()
  {
	  PDFFontDescriptor fontDescriptor = null;
	  try
	  {
	  	fontDescriptor = (PDFFontDescriptor)Class.forName("uk.org.retep.pdf.SunPDFFontDescriptor").newInstance();
	  }catch(Exception e)
	  {
		  System.out.println("ERROR: " + e);
		  e.printStackTrace();
		  throw new RuntimeException();
	  }
	  return fontDescriptor;
  }

 }
