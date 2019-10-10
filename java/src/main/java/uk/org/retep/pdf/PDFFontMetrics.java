
package uk.org.retep.pdf;
import java.awt.Font;
import java.util.Hashtable;

/** Esta clase contiene toda la info sobre las m�tricas de un font
 * Se accede a ella mediante m�todos est�ticos
 *  -> addFontMetrics para agregar un FontMetrics
 *  -> getFontMetrics para obtener un FontMetrics
 * 
 */
public class PDFFontMetrics
{
    private static Hashtable<FontName, PDFFontMetrics> container = new Hashtable<>();
    
    static
    { // Antes que nada vamos a insertar las m�tricas de los fonts Type1
        for(int i = 0; i < Type1FontMetrics.Type1MetricsNameMapper.length ; i++)
        {
            addFontMetrics(Type1FontMetrics.Type1MetricsJavaNameMapper[i],
                           Type1FontMetrics.Type1MetricsBoldMapper[i],
                           Type1FontMetrics.Type1MetricsItalicMapper[i],
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.Type1MetricsAscentMapper[i] + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.Type1MetricsDescentMapper[i] + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.Type1MetricsHeightMapper[i] + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.Type1MetricsMaxAdvanceMapper[i] + 300)),
                           Type1FontMetrics.Type1MetricsMapper[i]);
        }              

		// Ahora agrego las m�tricas de los CJK
		// Por ahora son monospaced y todas iguales
		for(int i = 0; i < Type1FontMetrics.Type1CJKMetricsJavaNameMapper.length ; i++)
        {
            addFontMetrics(Type1FontMetrics.Type1CJKMetricsJavaNameMapper[i], false, false,
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_ASCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_DESCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_HEIGHT  + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_MAXADVANCE + 300)),
                           Type1FontMetrics.TYPE1_METRICS_CJK);
            addFontMetrics(Type1FontMetrics.Type1CJKMetricsJavaNameMapper[i] + ",Italic", false, true,
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_ASCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_DESCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_HEIGHT  + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_MAXADVANCE + 300)),
                           Type1FontMetrics.TYPE1_METRICS_CJK);
            addFontMetrics(Type1FontMetrics.Type1CJKMetricsJavaNameMapper[i] + ",Bold", true, false,
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_ASCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_DESCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_HEIGHT  + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_MAXADVANCE + 300)),
                           Type1FontMetrics.TYPE1_METRICS_CJK);
            addFontMetrics(Type1FontMetrics.Type1CJKMetricsJavaNameMapper[i] + ",BoldItalic", true, true,
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_ASCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_DESCENT + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_HEIGHT  + 300)),
                           (int)(PDFFontDescriptor.revertMetrics(Type1FontMetrics.CJK_MAXADVANCE + 300)),
                           Type1FontMetrics.TYPE1_METRICS_CJK);
        }              
    }
    
    /** Agrega las metricas para un font/style
     * @param fontName Nombre l�gico del font
     * @param bold indica si son datos del font con bold
     * @param italic indica si son datos del font con intalic
     * @param ascent el Ascent
     * @param descent el Descent
     * @param height el Height
     * @param maxAdvance el MaxAdvance
     * @param sizes un array con los sizes de este font para este style
     */
    public static void addFontMetrics(String fontName, boolean bold, boolean italic, int ascent, int descent, int height, int maxAdvance, int[] sizes)
    {
		FontName fontData = new FontName(fontName, bold, italic);
        container.put(fontData, new PDFFontMetrics(fontData, ascent, descent, height, maxAdvance, sizes));
    }
    
    /** Obtiene las metricas de un font con style
     * Si NO se encuentran en la tabla, se intenta obtenerlas del AWT
     * @param fontName Nombre del font
     * @param bold indica si el font es bold
     * @param italic indica si el font es italic
     * @return PDFFontMetrics con las m�tricas o null si no se tienen estas m�tricas
     */
    public static PDFFontMetrics getFontMetrics(String fontName, boolean bold, boolean italic)
    {
        PDFFontMetrics fontMetrics = container.get(new FontName(fontName, bold, italic));
        if(fontMetrics == null)
            fontMetrics = getAWTFontMetrics(fontName, bold, italic);
        return fontMetrics;
    }
    
    /** Obtiene las metricas de un Font
     *  Si NO se encuentran en la tabla se intenta obtenerlas del AWT
     * @param font el Font a obtener sus m�tricas
     * @return PDFFontMetrics con las metricas o null si NO se pueden obtener
     */
    public static PDFFontMetrics getFontMetrics(Font font)
    {
        return getFontMetrics(font.getName(), (font.getStyle() & Font.BOLD) == Font.BOLD, (font.getStyle() & Font.ITALIC) == Font.ITALIC);
    }
    
    /** Obtiene las metricas de un Font con el SIZE especificado en el font
     */
    public static PDFFontMetrics getFontMetricsSize(Font font)
    {
        return getFontMetrics(font.getName(), (font.getStyle() & Font.BOLD) == Font.BOLD, (font.getStyle() & Font.ITALIC) == Font.ITALIC).scaleTo(font.getSize());
    }
    
    /** Obtiene el width del String pasado como par�metro
     * @param str String a calcular su width
     * @return el width 
     */
    public int stringWidth(String str)
    {
        int width = 0;
        for(int i = 0; i < str.length(); i++)
            width += charWidth(str.charAt(i));
        return width;
    }
    
    /** Escala un PDFFontMetrics al size especificado
     * @param size que se desea obtener las metricas
     * @return PDFFontMetrics con las m�tricas escaladas al size
     */
    private PDFFontMetrics scaleTo(int size)
    {
        return new PDFFontMetrics(fontData, scaleTo (ascent, size), scaleTo(descent, size), scaleTo(height, size), scaleTo(maxAdvance, size), scaleTo(sizes, size));
    }
        
    /** Obtiene las m�tricas del AWT, o null si no se pueden obtener del AWT
     * @param fontName nombre del font
     * @param bold Indica si es bold
     * @param italic Indica si es italic
     * @return PDFFontMetrics con las m�tricas calculadas a partir del AWT, o null en caso de no poder obtenerlas
     */
    private static PDFFontMetrics getAWTFontMetrics(String fontName, boolean bold, boolean italic)
    {
        try
        {
            System.err.println("Obtaining AWT FontMetrics for: " + fontName);
            java.awt.FontMetrics metrics = java.awt.Toolkit.getDefaultToolkit().getFontMetrics(new java.awt.Font(fontName, java.awt.Font.PLAIN + (bold ? java.awt.Font.BOLD : 0) + (italic ? java.awt.Font.ITALIC : 0), 72));
            return new PDFFontMetrics(new FontName(fontName, bold, italic), metrics.getMaxAscent(), metrics.getMaxDecent(), metrics.getHeight(), metrics.getMaxAdvance(), metrics.getWidths());
        }catch(Throwable e) 
        { // Si hubo error devolvemos null 
            System.err.println("Cannot obtain AWT FontMetrics for: " + fontName);
            return null;
        } 
    }

    /** Helper */
    private int scaleTo(int value, int size)
    {
        return (int)(((double)value * size) / (double)72);
    }
    
    /** Helper */
    private int [] scaleTo(int [] values, int size)
    {
        int [] ret = new int[values.length];
        for(int i = 0; i < values.length; i++)
            ret[i] = scaleTo(values[i], size);
        return ret;
    }    
    
	private FontName fontData;
	private int ascent;
    private int descent;
    private int height;
    private int maxAdvance;
    private int [] sizes;
    private PDFFontMetrics(FontName fontData, int ascent, int descent, int height, int maxAdvance, int[] sizes)
    {
		this.fontData = fontData;
        this.ascent = ascent;
        this.descent = descent;
        this.height = height;
        this.sizes = sizes;
    }               
    
    /** Obtiene el Ascent */
    public int getAscent() { return ascent; }
    public int getMaxAscent() { return ascent; }

    /** Obtiene el Descent */
    public int getDescent() { return descent; }
    public int getMaxDescent() { return descent; }

    /** Obtiene el height */
    public int getHeight() { return height; }

    /** Obtiene el MaxAdvance */
    public int getMaxAdvance() { return maxAdvance; }
    
    /** Obtiene el array de sizes */
    public int [] getWidths() { return sizes; }            
    
    /** Obtiene el width de un char 
     *  @param car Caracter a obtener su width
     *  @return el width
     */ 
    public int charWidth(char car)
    {
		try
		{
			return sizes[car];
		}catch(ArrayIndexOutOfBoundsException e)
		{
			int index = ((int)car);
			if( !alerted && index > 255 )
			{				
				System.err.println("Trying to use a character out of bounds (" + index + ")");
				System.err.println("You might need to define a font substitution for font '" + fontData.getFontName() + "'");
				alerted = true;
			}
			return maxAdvance;
		}
    }
	private boolean alerted = false;
        
}

    /** Este es un container de FontName-style
     */
    class FontName
    {
        private String fontName;
        private boolean bold;
        private boolean italic;
        public FontName(String fontName, boolean bold, boolean italic)
        {
            this.fontName = fontName;
            this.bold = bold;
            this.italic = italic;
        }
        
        public boolean equals(Object obj)
        {
            if(obj instanceof FontName)
            {
                FontName f = (FontName)obj;
                return this.fontName.equalsIgnoreCase(f.fontName) && 
                       this.bold == f.bold && 
                       this.italic == f.italic;
            }
            else return false;
            
        }
        
        public int hashCode() 
        {
            return fontName.hashCode();
        }
		
		public String getFontName()
		{
			return fontName;
		}
		
		public boolean getBold()
		{
			return bold;
		}
		
		public boolean getItalic()
		{
			return italic;
		}
    }
