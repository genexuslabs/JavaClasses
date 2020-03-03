package com.genexus.reports;
import java.awt.Color;
/**
 * Formato del archivo PDFReport.INI
 * ---------------------------------
 *
 * GeneralProperties:
 *  - Embeed Fonts -> booleano que indica si embeber los fonts o no (ver Seccion 'Embeed Fonts')
 *  - SearchNewFonts -> booleano que indica si se deben buscar los fonts si no estan en el INI al embeberlos
 *  - SearchNewFontsOnce -> booleano que indica buscar por única vez los fonts si no se encuentran
 *  - Version -> Indica la version del PDFReport (formato a.b.c.d)
 *  - FontsLocation -> Indica la ubicación de los fonts
 *  - LeftMargin -> Indica el margen izquierdo asociado al documento (en centúŠetros)
 *  - TopMargin -> Indica el margen arriba asociado al documento (en centúŠetros)
 *  - DEBUG -> Indica que se quiere mostrar DEBUG por la stdout
 *
 * Seccion 'Embeed Fonts':
 *  - Para cada nombre de font se le asocia un booleano que indica si embeber el font o no (para granularidad más fina de la GeneralProperty)
 *    Para embeber un font, debe estar en 'true' la generalProperty y la property de esta seccion
 *    Para setear que fonts embeber, se puede ejecutar el 'com.genexus.reports.PDFReportConfig'
 *
 * Seccion 'Fonts Location (MS)' y 'Fonts Location (Sun)'
 *  - Se almacenan los mappings 'FontName= ubiacion del .ttf asociado'. Estos mappings son distintos para MS y Sun
 *    Estos mappings son creados automaticamente
 *
 * Seccion 'Fonts Substitutions'
 *  - Se almacenan pares 'Font= Font' que mapean un font en otro.
 *    Por ejemplo, se puede poner 'Impact= Courier', para mapear un TrueTypeFont en otro
 *    También se puede mapear un font en un Type1, por ej: 'Impact= Helvetica'
 *    Estos mappings los puede realizar el usuario
 *
 * Seccion 'Font Metrics'
 *  - Se almacenan pares 'Font= metricas' que indican las metricas de un font
 *    Esto es definido por el usuario, es decir estas métricas hacen un override de las
 *    metricas que se utilizarú}n en otro caso
 *	  Las metricas se definen mediante rules separadas por ';':
 *	  FontName= rule1;rule2;...;ruleN
 *    donde cada rule puede ser de este estilo:
 *      - monospaced(XXX), que indica que se utilizan las mismas metricas para todos los caracteres
 *	    - range(NN,XX0,XX1,XX2,XX3,XX4....,XXN, en el que se enumeran las metricas para cada
 *     caracter comenzando desde el caracter NN. En este caso si se indican unas pocas metricas, solo
 *     se hace el override de la interseccion.
 *		- move(HH,VV), que indica que los textos con dicho font se deben mover HH y VV pixels
 *	   horizantal y verticalmente
 *	  Nota: no se puede especificar si el font es bold y/o italic, es decir que estas metricas
 *    van a aplicar para todas las combinaciones de bold/italic para dicho font
 *
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.platform.INativeFunctions;
import com.genexus.platform.NativeFunctions;
import com.genexus.util.TemporaryFiles;
import com.genexus.webpanels.HttpContextWeb;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.Barcode;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import uk.org.retep.pdf.PDFFont;
import uk.org.retep.pdf.Type1FontMetrics;

public class PDFReportItext implements IReportHandler
{
    private int lineHeight, pageLines;

    private com.lowagie.text.Rectangle pageSize;   // Contiene las dimensiones de la página
    private int pageOrientation;  // Indica la orientacion de las páginas
    private Font font;
	private BaseFont baseFont;
	private Barcode barcode = null;
	private boolean fontUnderline;
	private boolean fontStrikethru;
	private int fontSize;
	private boolean fontBold = false;
	private boolean fontItalic = false;
    private Color backColor, foreColor;
    public static PrintStream DEBUG_STREAM = System.out;
    private OutputStream outputStream = null; // Contiene el OutputStream de salida del reporte
    //private Point pageMargin = new Point(0,0); // Contiene el margen [left, top] de cada página
    private static ParseINI props = new ParseINI();
	private ParseINI printerSettings;
	private String form;
    private Vector stringTotalPages; // Contiene la lista de locations del parámetros {{Pages}}
    private boolean isPageDirty; // Indica si la pagina NO se debe 'dispose()'ar pues se le va a agregar cosas al terminar el PDF
    private int outputType = -1; // Indica el tipo de Output que se desea para el documento
    private int printerOutputMode = -1; // Indica si se debe mostrar el cuadro de Impresion para la salida por impresora
    private boolean modal = false; // Indica si el diálogo debe ser modal o no modal en
    private String docName = "PDFReport.pdf"; // Nombre del documento a generar (se cambia con GxSetDocName)
    private static INativeFunctions nativeCode = NativeFunctions.getInstance();
    private static Hashtable<String, String> fontSubstitutes = new Hashtable<>(); // Contiene la tabla de substitutos de fonts (String <--> String)
	private static String configurationFile = null;
	private static String configurationTemplateFile = null;
	private static String defaultRelativePrepend = null; // En aplicaciones web, contiene la ruta al root de la aplicación para ser agregado al inicio de las imagenes con path relativo
	private static String defaultRelativePrependINI = null;
	private static String webAppDir = null;
	//private boolean containsSpecialMetrics = false;
	//private Hashtable fontMetricsProps = new Hashtable();
	public static boolean DEBUG = false;
    private Document document;
	private PdfWriter writer;
	private Paragraph chunk;
	private int currLine;
	private int lastLine = 0;
	private static String predefinedSearchPath = ""; // Contiene los predefinedSearchPaths
	private float leftMargin;
	private float topMargin;
	private float bottomMargin; //If Margin Bottom is not specified 6 lines are assumed (nlines =6).
	private PdfTemplate template;
	private BaseFont templateFont;
	private int templateFontSize;
	private boolean backFill = true;
	private Color templateColorFill;
	private int pages=0;
	private boolean templateCreated = false;
	public static float DASHES_UNITS_ON = 10;
	public static float DASHES_UNITS_OFF = 10;
	public static float DOTS_UNITS_OFF = 3;
	public static float DOTS_UNITS_ON = 1;	
	public boolean lineCapProjectingSquare = true;
	public boolean barcode128AsImage = true;
	ConcurrentHashMap<String, Image> documentImages;
	public int runDirection = PdfWriter.RUN_DIRECTION_LTR;
	public int justifiedType;

	
	private HttpContext httpContext = null;
	float[] STYLE_SOLID = new float[]{1,0};//0
	float[] STYLE_NONE = null;//1
	float[] STYLE_DOTTED, //2
		STYLE_DASHED, //3
		STYLE_LONG_DASHED, //4
		STYLE_LONG_DOT_DASHED; //5
	int STYLE_NONE_CONST=1;

	private enum VerticalAlign{
		TOP(0),
		MIDDLE(1),
		BOTTOM(2);
		private int intValue;
		VerticalAlign(int val)
		{
			this.intValue=val;
		}
		public int value(){
			return intValue;
		}
	}

    /** Setea el OutputStream a utilizar
     *  @param outputStream Stream a utilizar
     */
    public void setOutputStream(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

    /** Busca la ubicación del Acrobat. Si no la encuentra tira una excepción
     */
    private static String getAcrobatLocation() throws Exception
    {
    	ParseINI props;
    	try
    	{
    		props = new ParseINI(Const.INI_FILE);
				if(new File(Const.INI_FILE).length() == 0)
					new File(Const.INI_FILE).delete();
			}
			catch(IOException e)
			{
				props = new ParseINI(); 
			}
			
      // Primero debo obtener la ubicación + ejecutable del Acrobat
      String acrobatLocation = props.getGeneralProperty(Const.ACROBAT_LOCATION); // Veo si esta fijada la ubicación del Acrobat en la property
      if(acrobatLocation == null)
      {
				if(NativeFunctions.isUnix())
				{ // Si estoy en Unix no puedo ir a buscar el registry ;)
					throw new Exception("Try setting Acrobat location & executable in property '" + Const.ACROBAT_LOCATION + "' of PDFReport.ini");
				}
      }
      return acrobatLocation;
    }

    /** Manda a imprimir el reporte a la impresora
     * Si en las properties del PDFReport esta definida una GeneralProperty 'Acrobat Location' se
     * utiliza esta property para obtener la ubicación + ejecutable del Acrobat, sino se busca en el Registry
     * @param pdfFilename Nombre del reporte a imprimir (con extensión)
     * @param silent Booleano que indica si se va a imprimir sin diálogo
     * @exception Excepcion si no se puede realizar la operación
     */
    public static void printReport(String pdfFilename, boolean silent) throws Exception
    {
		if(NativeFunctions.isWindows())
		{ // En Windows obtenemos el full path
		  // En Linux esto no anda bien
			pdfFilename = "\"" + new File(pdfFilename).getAbsolutePath() + "\"";
		}
		
		String [] cmd = {};
		String acrobatLocation = null;
    try
    {
        // Primero debo obtener la ubicación + ejecutable del Acrobat
        acrobatLocation = getAcrobatLocation();
    }catch(Exception acrobatNotFound)
    {
			throw new Exception("Acrobat cannot be found in this machine: " + acrobatNotFound.getMessage());
    }
		
		//Se genera el PostScript
		nativeCode.executeModal(acrobatLocation + " -toPostScript " + pdfFilename, false);
			
		//Se manda a imprimir a la impresora default
		int pos = pdfFilename.lastIndexOf(".");
		pdfFilename = pdfFilename.substring(0, pos) + ".ps";
		cmd = new String[] { "lp", pdfFilename};
		Runtime.getRuntime().exec(cmd);
    }

    /** Muestra el reporte en pantalla
     * @param filename nombre del PDF a mostrar
     * @param modal indica si el PDF se va a mostrar en diálogo modal
     * @exception Si no se puede encontrar el Acrobat
     */
    public static void showReport(String filename, boolean modal) throws Exception
    {
		if(NativeFunctions.isWindows())
		{ // En Windows obtenemos el full path
		  // En Linux esto no anda bien
			filename = "\"" + new File(filename).getAbsolutePath() + "\"";
		}
        String acrobatLocation;
        try
        {
            // Primero debo obtener la ubicación + ejecutable del Acrobat
            acrobatLocation = getAcrobatLocation();
        }catch(Exception acrobatNotFound)
        {
			throw new Exception("Acrobat cannot be found in this machine: " + acrobatNotFound.getMessage());
         }

        if(modal)
		{
            nativeCode.executeModal(acrobatLocation + " " + filename, true);
		}
        else 
		{
			Runtime.getRuntime().exec(new String[] { acrobatLocation, filename});
		}
    }

	private static char alternateSeparator = File.separatorChar == '/' ? '\\' : '/';
	public PDFReportItext(ModelContext context)
    {
        document = null;
		pageSize = null;
		stringTotalPages = new Vector();
		documentImages = new ConcurrentHashMap<String, Image>();
		httpContext = (HttpContext) context.getHttpContext();

        if(defaultRelativePrepend == null)
		{
			defaultRelativePrepend = httpContext.getDefaultPath();
            if(defaultRelativePrepend == null || defaultRelativePrepend.trim().equals(""))
				defaultRelativePrepend = "";
			else
				defaultRelativePrepend = defaultRelativePrepend.replace(alternateSeparator, File.separatorChar) + File.separatorChar;
			defaultRelativePrependINI = defaultRelativePrepend;
			if(new File(defaultRelativePrepend + Const.WEB_INF).isDirectory())
			{
				configurationFile = defaultRelativePrepend + Const.WEB_INF + File.separatorChar + Const.INI_FILE; // Esto es para que en aplicaciones web el PDFReport.INI no quede visible en el server
				configurationTemplateFile = defaultRelativePrepend + Const.WEB_INF + File.separatorChar + Const.INI_TEMPLATE_FILE;
			}
			else
			{
				configurationFile = defaultRelativePrepend + Const.INI_FILE;
				configurationTemplateFile = defaultRelativePrepend + Const.INI_TEMPLATE_FILE;
			}
			webAppDir = defaultRelativePrepend;

			if(httpContext instanceof HttpContextWeb || !httpContext.getDefaultPath().isEmpty())
			{
				// @cambio: 23/07/03
				// Para los reportes en el web, debemos tener en cuenta la preference 'Static Content Base URL' del modelo
				// Pero SOLO la tenemos en cuenta si es un directorio relativo
				// O sea, si la preference dice algo tipo /pepe, entonces vamos a buscar las
				// imagenes relativas a %WebApp%/pepe, pero si la preference dice algo tipo
				// 'http://otroServer/xxxyyy' entonces ahi no le damos bola a la preference!
				// Además, para mantener compatibilidad con las aplicaciones hasta ahora, si la imagen
				// no se encuentra all\uFFFDElo que hacemos es ir a buscarla a %WebApp%
				
				// @cambio: 12/09/17
				// Esto tambien se tiene que hacer para los reportes web que se llaman con submit 
				// (ese es el caso en el cual el getDefaultPath no es Empty)

				String staticContentBase = httpContext.getStaticContentBase();
				if(staticContentBase != null)
				{
					staticContentBase = staticContentBase.trim();
					if(staticContentBase.indexOf(':') == -1)
					{ // Si la staticContentBase es una ruta relativa
						staticContentBase = staticContentBase.replace(alternateSeparator, File.separatorChar);
						if(staticContentBase.startsWith(File.separator))
						{
							staticContentBase = staticContentBase.substring(1);
						}
						if(!staticContentBase.equals(""))
						{
							defaultRelativePrepend += staticContentBase;
							if(!defaultRelativePrepend.endsWith(File.separator))
							{
								defaultRelativePrepend += File.separator;
							}
						}
					}
				}
			}
		}
		if (firstTime)
		{
			loadProps();
			firstTime = false;
		}
    }
	
	private static boolean firstTime = true;
	
	private void loadPrinterSettingsProps(String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) 
	{
		if(new File(defaultRelativePrependINI + Const.WEB_INF).isDirectory())
		{
			iniFile = defaultRelativePrependINI + Const.WEB_INF + File.separatorChar + iniFile;
		}
		else
		{
			iniFile = defaultRelativePrependINI + iniFile;
		}
		
		try
		{
			this.form = form;
			printerSettings = new ParseINI(iniFile);
		}
		catch(IOException e){ printerSettings = new ParseINI(); }
		
		mode = (mode==2)?3:0;
		
		printerSettings.setupProperty(form, Const.PRINTER, printer);
		printerSettings.setupProperty(form, Const.MODE, mode + "");
		printerSettings.setupProperty(form, Const.ORIENTATION, orientation+ "");
		printerSettings.setupProperty(form, Const.PAPERSIZE, pageSize+ "");
		printerSettings.setupProperty(form, Const.PAPERLENGTH, pageLength+ "");
		printerSettings.setupProperty(form, Const.PAPERWIDTH, pageWidth+ "");
		printerSettings.setupProperty(form, Const.SCALE, scale+ "");
		printerSettings.setupProperty(form, Const.COPIES, copies+ "");
		printerSettings.setupProperty(form, Const.DEFAULTSOURCE, defSrc+ "");
		printerSettings.setupProperty(form, Const.PRINTQUALITY, quality+ "");
		printerSettings.setupProperty(form, Const.COLOR, color+ "");
		printerSettings.setupProperty(form, Const.DUPLEX, duplex+ "");
	}

	private void loadProps()
	{
        try{
            props = new ParseINI(configurationFile, configurationTemplateFile);
        }catch(IOException e){ props = new ParseINI(); }
		
        props.setupGeneralProperty(Const.PDF_REPORT_INI_VERSION_ENTRY, Const.PDF_REPORT_INI_VERSION);
        props.setupGeneralProperty(Const.EMBEED_SECTION, Const.EMBEED_DEFAULT);
		props.setupGeneralProperty(Const.EMBEED_NOT_SPECIFIED_SECTION, Const.EMBEED_DEFAULT);
        props.setupGeneralProperty(Const.SEARCH_FONTS_ALWAYS, "false");
        props.setupGeneralProperty(Const.SEARCH_FONTS_ONCE, "true");
		props.setupGeneralProperty(Const.SERVER_PRINTING, "false");
		props.setupGeneralProperty(Const.ADJUST_TO_PAPER, "true");
		props.setupGeneralProperty(Const.LINE_CAP_PROJECTING_SQUARE, Const.DEFAULT_LINE_CAP_PROJECTING_SQUARE);
		props.setupGeneralProperty(Const.BARCODE128_AS_IMAGE, Const.DEFAULT_BARCODE128_AS_IMAGE);
        props.setupGeneralProperty("DEBUG", "false");
        props.setupGeneralProperty(Const.LEFT_MARGIN, Const.DEFAULT_LEFT_MARGIN);
        props.setupGeneralProperty(Const.TOP_MARGIN, Const.DEFAULT_TOP_MARGIN);
		props.setupGeneralProperty(Const.MARGINS_INSIDE_BORDER, Const.DEFAULT_MARGINS_INSIDE_BORDER);
		props.setupGeneralProperty(Const.BOTTOM_MARGIN, Const.DEFAULT_BOTTOM_MARGIN);
		props.setupGeneralProperty(Const.OUTPUT_FILE_DIRECTORY, ".");
		props.setupGeneralProperty(Const.LEADING, "2");
		props.setupGeneralProperty(Const.RUN_DIRECTION, Const.RUN_DIRECTION_LTR);
		props.setupGeneralProperty(Const.JUSTIFIED_TYPE_ALL, "false");

		props.setupGeneralProperty(Const.STYLE_DOTTED, Const.DEFAULT_STYLE_DOTTED);
		props.setupGeneralProperty(Const.STYLE_DASHED, Const.DEFAULT_STYLE_DASHED);
		props.setupGeneralProperty(Const.STYLE_LONG_DASHED, Const.DEFAULT_STYLE_LONG_DASHED);
		props.setupGeneralProperty(Const.STYLE_LONG_DOT_DASHED, Const.DEFAULT_STYLE_LONG_DOT_DASHED);

        loadSubstituteTable(); // Cargo la tabla de substitutos de fonts

        if(props.getBooleanGeneralProperty("DEBUG", false))
		{
			DEBUG = true;
            DEBUG_STREAM = System.out;
		}
        else
		{
			DEBUG = false;
			DEBUG_STREAM = new PrintStream(new com.genexus.util.NullOutputStream());
		}

        uk.org.retep.pdf.Utilities.addPredefinedSearchPaths(new String[]{System.getProperty("java.awt.fonts", "c:\\windows\\fonts"),
                                           System.getProperty("com.ms.windir", "c:\\windows") + "\\fonts"});
	}

	public static final void addPredefinedSearchPaths(String [] predefinedPaths)
	{
		String predefinedPath = "";
		for(int i = 0; i < predefinedPaths.length; i++)
			predefinedPath += predefinedPaths[i] + ";";
		predefinedSearchPath = predefinedPath + predefinedSearchPath; // SearchPath= los viejos más los nuevos
	}

	public static final String getPredefinedSearchPaths()
	{
		return predefinedSearchPath;
	}

	private void init()
    {
      Document.compress = true;
	  try {
      writer = PdfWriter.getInstance(document, outputStream);
	  }
	  catch(DocumentException de) {
            System.err.println(de.getMessage());
      }
      document.addAuthor(Const.AUTHOR);
      document.addCreator(Const.CREATOR);
      document.open();
    }


    public void GxRVSetLanguage(String lang)
    {
    }

    public void GxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength)
    {
    }
	
	private float[] parsePattern(String patternStr)
	{
		if (patternStr!=null)
		{
			StringTokenizer st = new StringTokenizer(patternStr.trim(), ";");
			int length = st.countTokens();
			if (length>0)
			{
				int i = 0;
				float[] pattern = new float[length];
				while(st.hasMoreTokens())
				{
					pattern[i] = Float.parseFloat(st.nextToken());
					i++;
				}
				return pattern;
			}
		}
		return null;
	}

	private float [] getDashedPattern(int style)
	{
		switch(style)
		{
			case 0: return STYLE_SOLID;
			case 1: return STYLE_NONE;
			case 2: return STYLE_DOTTED;
			case 3: return STYLE_DASHED;
			case 4: return STYLE_LONG_DASHED;
			case 5: return STYLE_LONG_DOT_DASHED;
			default:
				return STYLE_SOLID;
		}
	}
	
	/**
	 * @param hideCorners indica si se deben ocultar los triangulos de las esquinas cuando el lado que los une esta oculto.
	 */
	private void drawRectangle(PdfContentByte cb, float x, float y, float w, float h, 
		int styleTop, int styleBottom, int styleRight, int styleLeft, 
		float radioTL, float radioTR, float radioBL, float radioBR, float penAux, boolean hideCorners)
	{

	
		float[] dashPatternTop = getDashedPattern(styleTop);
		float[] dashPatternBottom = getDashedPattern(styleBottom);
		float[] dashPatternLeft = getDashedPattern(styleLeft);
		float[] dashPatternRight = getDashedPattern(styleRight);

		//-------------------bottom line---------------------
		if (styleBottom!=STYLE_NONE_CONST)
		{
			cb.setLineDash(dashPatternBottom, 0);
		}

		float b = 0.4477f;
		if (radioBL>0)
		{
			cb.moveTo(x + radioBL, y);
		}
		else
		{
			if (hideCorners && styleLeft==STYLE_NONE_CONST && radioBL==0)
			{
				cb.moveTo(x + penAux, y);
			}
			else
			{
				cb.moveTo(x, y);
			}
		}

		//-------------------bottom right corner---------------------

		if (styleBottom!=STYLE_NONE_CONST)//si es null es Style None y no traza la linea
		{
			if (hideCorners && styleRight==STYLE_NONE_CONST && radioBR==0)
			{
				cb.lineTo(x + w - penAux, y);
			}
			else
			{
				cb.lineTo(x + w - radioBR, y);
			}
			if (radioBR>0 && styleRight!=STYLE_NONE_CONST)
			{
				cb.curveTo(x + w - radioBR * b, y, x + w, y + radioBR * b, x + w, y + radioBR);
			}
		}

		//-------------------right line---------------------

		if (styleRight!=STYLE_NONE_CONST && dashPatternRight!=dashPatternBottom)
		{
			cb.stroke();
			cb.setLineDash(dashPatternRight, 0);
			if (hideCorners && styleBottom==STYLE_NONE_CONST && radioBR==0)
			{
				cb.moveTo(x + w, y + penAux);
			}
			else
			{
				cb.moveTo(x + w, y + radioBR);
			}
		}

		//-------------------top right corner---------------------
		if (styleRight!=STYLE_NONE_CONST)
		{
			if (hideCorners && styleTop==STYLE_NONE_CONST && radioTR==0)
			{
				cb.lineTo (x + w, y + h - penAux);
			}
			else
			{
				cb.lineTo (x + w, y + h - radioTR);
			}
			if (radioTR>0 && styleTop!=STYLE_NONE_CONST)
			{
				cb.curveTo(x + w, y + h - radioTR * b, x + w - radioTR * b, y + h, x + w - radioTR, y + h);
			}
		}

		//-------------------top line---------------------

		if (styleTop!=STYLE_NONE_CONST && dashPatternTop!=dashPatternRight)
		{
			cb.stroke();
			cb.setLineDash(dashPatternTop, 0);
			if (hideCorners && styleRight==STYLE_NONE_CONST && radioTR==0)
			{
				cb.moveTo(x + w - penAux, y + h);
			}
			else
			{
				cb.moveTo(x + w - radioTR, y + h);
			}
		}

		//-------------------top left corner---------------------
		if (styleTop!=STYLE_NONE_CONST)
		{
			if (hideCorners && styleLeft==STYLE_NONE_CONST && radioTL==0)
			{
				cb.lineTo(x + penAux, y + h);
			}
			else
			{
				cb.lineTo(x + radioTL, y + h);
			}
			if (radioTL>0 && styleLeft!=STYLE_NONE_CONST)
			{
				cb.curveTo(x + radioTL * b, y + h, x, y + h - radioTL * b, x, y + h - radioTL);
			}
		}
            
		//-------------------left line---------------------

		if (styleLeft!=STYLE_NONE_CONST  && dashPatternLeft!=dashPatternTop)
		{
			cb.stroke();
			cb.setLineDash(dashPatternLeft, 0);
			if (hideCorners && styleTop==STYLE_NONE_CONST && radioTL==0)
			{
				cb.moveTo(x, y + h - penAux);
			}
			else
			{
				cb.moveTo(x, y + h - radioTL);
			}
		}
            
		//-------------------bottom left corner---------------------
		if (styleLeft!=STYLE_NONE_CONST)
		{
			if (hideCorners && styleBottom==STYLE_NONE_CONST && radioBL==0)
			{
				cb.lineTo(x, y + penAux);
			}
			else
			{
				cb.lineTo(x, y + radioBL);
			}
			if (radioBL>0 && styleBottom!=STYLE_NONE_CONST)
			{
				cb.curveTo(x, y + radioBL * b, x + radioBL * b, y, x + radioBL, y);
			}
		}
		cb.stroke();
            
	}
	private void roundRectangle(PdfContentByte cb, float x, float y, float w, float h, 
		float radioTL, float radioTR, float radioBL, float radioBR)
	{

		//-------------------bottom line---------------------

		float b = 0.4477f;
		if (radioBL>0)
		{
			cb.moveTo(x + radioBL, y);
		}
		else
		{
			cb.moveTo(x, y);
		}

		//-------------------bottom right corner---------------------

		cb.lineTo(x + w - radioBR, y);
		if (radioBR>0)
		{
			cb.curveTo(x + w - radioBR * b, y, x + w, y + radioBR * b, x + w, y + radioBR);
		}
		

		cb.lineTo (x + w, y + h - radioTR);
		if (radioTR>0)
		{
			cb.curveTo(x + w, y + h - radioTR * b, x + w - radioTR * b, y + h, x + w - radioTR, y + h);
		}

		cb.lineTo(x + radioTL, y + h);
		if (radioTL>0)
		{
			cb.curveTo(x + radioTL * b, y + h, x, y + h - radioTL * b, x, y + h - radioTL);
		}
		cb.lineTo(x, y + radioBL);
		if (radioBL>0)
		{
			cb.curveTo(x, y + radioBL * b, x + radioBL * b, y, x + radioBL, y);
		}
            
	}
	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue)
	{
		GxDrawRect(left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue, 0, 0);
	}
	
    public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, int style, int cornerRadius)
    {
		GxDrawRect(left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue, style, style, style, style, cornerRadius, cornerRadius, cornerRadius, cornerRadius);
	}
	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, 
		int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR)
	{

		PdfContentByte cb = writer.getDirectContent();

		float penAux = (float)convertScale(pen);
		float rightAux = (float)convertScale(right);
		float bottomAux = (float)convertScale(bottom);
		float leftAux = (float)convertScale(left);
		float topAux = (float)convertScale(top);
		
		cb.saveState();
		
		float x1, y1, x2, y2;
		x1 = leftAux + leftMargin;
		y1 = pageSize.getTop() - bottomAux - topMargin -bottomMargin;
		x2 = rightAux + leftMargin;
		y2 = pageSize.getTop() - topAux - topMargin -bottomMargin;

		cb.setLineWidth(penAux);		
		cb.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE);

		if (cornerRadioBL==0 && cornerRadioBR==0 && cornerRadioTL==0 && cornerRadioTR==0 && styleBottom==0 && styleLeft==0 && styleRight==0 && styleTop==0)
		{
			//Tengo que hacer eso para que el borde quede del mismo color que el fill si se indica que no se quiere borde,
			//porque no funciona el setLineWidth
			if (pen > 0)
				cb.setRGBColorStroke(foreRed, foreGreen, foreBlue);
			else
				cb.setRGBColorStroke(backRed, backGreen, backBlue);

			cb.rectangle(x1, y1, x2 - x1, y2 - y1);

			if (backMode!=0)
			{
				cb.setColorFill(new Color(backRed, backGreen, backBlue));
				cb.fillStroke();
			}
			cb.closePathStroke();
		}
		else
		{
			float w = x2 - x1;
			float h = y2 - y1;
			if (w < 0)
			{
				x1 += w;
				w = -w;
			}
			if (h < 0)
			{
				y1 += h;
				h = -h;
			}

			float cRadioTL = (float)convertScale(cornerRadioTL);
			float cRadioTR = (float)convertScale(cornerRadioTR);
			float cRadioBL = (float)convertScale(cornerRadioBL);
			float cRadioBR = (float)convertScale(cornerRadioBR);

			// Scale the radius if it's too large or to small to fit.
			int max = (int)Math.min(w, h);
			cRadioTL = Math.max(0, Math.min(cRadioTL, max/2));
			cRadioTR = Math.max(0, Math.min(cRadioTR, max/2));
			cRadioBL = Math.max(0, Math.min(cRadioBL, max/2));
			cRadioBR = Math.max(0, Math.min(cRadioBR, max/2));

			if (backMode!=0)
			{
				//Interior del rectangulo
				cb.setRGBColorStroke(backRed, backGreen, backBlue);
				cb.setLineWidth(0);		
					roundRectangle(cb, x1, y1, w, h,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR);
				cb.setColorFill(new Color(backRed, backGreen, backBlue));
				cb.fillStroke();
				cb.setLineWidth(penAux);		
			}
			if (pen > 0)
			{
				//Bordes del rectangulo
				cb.setRGBColorStroke(foreRed, foreGreen, foreBlue);
					drawRectangle(cb, x1, y1, w, h, 
						styleTop, styleBottom, styleRight, styleLeft,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR, penAux, false);
			}
		}
		cb.restoreState();
		
		if(DEBUG)DEBUG_STREAM.println("GxDrawRect -> (" + left + "," + top + ") - (" + right + "," + bottom + ")  BackMode: " + backMode + " Pen:" + pen);
    }

	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue)
	{
		GxDrawLine(left, top,      right, bottom, width, foreRed, foreGreen, foreBlue, 0);
	}
	
    public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style)
    {
		PdfContentByte cb = writer.getDirectContent();

		float widthAux = (float)convertScale(width);
		float rightAux = (float)convertScale(right);
		float bottomAux = (float)convertScale(bottom);
		float leftAux = (float)convertScale(left);
		float topAux = (float)convertScale(top);
		
        if(DEBUG)DEBUG_STREAM.println("GxDrawLine -> (" + left + "," + top + ") - (" + right + "," + bottom + ") Width: " + width);

		float x1, y1, x2, y2;

		x1 = leftAux + leftMargin;
		y1 = pageSize.getTop() - bottomAux - topMargin -bottomMargin;
		x2 = rightAux + leftMargin;
		y2 = pageSize.getTop() - topAux - topMargin -bottomMargin;

		cb.saveState();
		cb.setRGBColorStroke(foreRed, foreGreen, foreBlue);
		cb.setLineWidth(widthAux);

		if (lineCapProjectingSquare)
		{
			cb.setLineCap(PdfContentByte.LINE_CAP_PROJECTING_SQUARE); //Hace que lineas de width 10 por ejemplo que forman una esquina no quedan igual que en disenio porque "rellena" la esquina.
		}
		if (style!=0)
		{
			float[] dashPattern = getDashedPattern(style);
			cb.setLineDash(dashPattern, 0);
		}
		cb.moveTo(x1, y1);
		cb.lineTo(x2, y2);			
		cb.stroke();
			
		cb.restoreState();
    }

	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom)	
	{
		GxDrawBitMap(bitmap, left, top, right, bottom, 0);		
	}
	
    public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio)
    {
		try
		{
			//java.awt.Image image;
			com.lowagie.text.Image image;
			com.lowagie.text.Image imageRef;
			try
			{
				if (documentImages != null && documentImages.containsKey(bitmap))
				{
					image = documentImages.get(bitmap);
				}
				else
				{

					if (!NativeFunctions.isWindows() && new File(bitmap).isAbsolute() && bitmap.startsWith(httpContext.getStaticContentBase()))
					{
						bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
					}				
				
					if(!new File(bitmap).isAbsolute() && !bitmap.toLowerCase().startsWith("http"))
					{ 
						if (bitmap.startsWith(httpContext.getStaticContentBase()))
						{
							bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
						}					
						// Si la ruta a la imagen NO es absoluta, en aplicaciones Web le agregamos al comienzo la ruta al root de la aplicación
					// más la staticContentBaseURL si ésta es relativa.
						image = com.lowagie.text.Image.getInstance(defaultRelativePrepend + bitmap);
						//image = com.genexus.uifactory.awt.AWTUIFactory.getImageNoWait(defaultRelativePrepend + bitmap);
						if(image == null)
						{ // Si all\uFFFDEno se encuentra la imagen, entonces la buscamos bajo el webAppDir (para mantener compatibilidad)
							bitmap = webAppDir + bitmap;
							image = com.lowagie.text.Image.getInstance(bitmap);
							//image = com.genexus.uifactory.awt.AWTUIFactory.getImageNoWait(bitmap);
						}
						else
						{
							bitmap = defaultRelativePrepend + bitmap;
						}
					}
					else
					{
						image = com.lowagie.text.Image.getInstance(bitmap);
						//image = com.genexus.uifactory.awt.AWTUIFactory.getImageNoWait(bitmap);
					}
				}
			}
			catch(java.lang.IllegalArgumentException ex)//Puede ser una url absoluta
			{
				java.net.URL url= new java.net.URL(bitmap);
				image = com.lowagie.text.Image.getInstance(url);
			}

			if (documentImages == null)
			{
				documentImages = new ConcurrentHashMap<String, Image>();
			}
			documentImages.putIfAbsent(bitmap, image);


			if(DEBUG)DEBUG_STREAM.println("GxDrawBitMap -> '" + bitmap + "' [" + left + "," + top + "] - Size: (" + (right - left) + "," + (bottom - top) + ")");

	        if(image != null)
			{ // Si la imagen NO se encuentra, no hago nada
				float rightAux = (float)convertScale(right);
				float bottomAux = (float)convertScale(bottom);
				float leftAux = (float)convertScale(left);
				float topAux = (float)convertScale(top);

				image.setAbsolutePosition(leftAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin);
				if (aspectRatio == 0)
					image.scaleAbsolute(rightAux - leftAux , bottomAux - topAux);
				else
					image.scaleToFit(rightAux - leftAux , bottomAux - topAux);
				PdfContentByte cb = writer.getDirectContent();
				cb.addImage(image);
			}
		}
		catch(DocumentException de) 
		{
			System.err.println(de.getMessage());
		}
		catch(IOException ioe) 
		{
			System.err.println(ioe.getMessage());
		}
		catch(Exception e) 
		{
			System.err.println(e.getMessage());
		}
    }
    
    public String getSubstitute(String fontName)
    {
		Vector<String> fontSubstitutesProcessed = new Vector<>();
    	String newFontName = fontName;
    	while( fontSubstitutes.containsKey(newFontName))
    	{
			if (!fontSubstitutesProcessed.contains(newFontName))
			{
				fontSubstitutesProcessed.addElement(newFontName);
    			newFontName = fontSubstitutes.get(newFontName);
			}
			else
			{
				return fontSubstitutes.get(newFontName);
			}
    	}
    	return newFontName;
    }

    public void GxAttris(String fontName, int fontSize, boolean fontBold, boolean fontItalic, boolean fontUnderline, boolean fontStrikethru, int Pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue)
    {
		boolean isCJK = false;
		boolean embeedFont = isEmbeddedFont(fontName);
		String originalFontName = fontName;
		if (!embeedFont)
		{
			fontName = getSubstitute(fontName); // Veo si hay substitutos solo si el font no va a ir embebido
		}
        if(DEBUG)
		{
			String fontSubstitute = "";
			if (!originalFontName.equals(fontName))
			{
				fontSubstitute = "Original Font: " + originalFontName + " Substitute";
			}
			DEBUG_STREAM.println("GxAttris: ");
			DEBUG_STREAM.println("\\-> " + fontSubstitute + "Font: " + fontName + " (" + fontSize + ")" + (fontBold ? " BOLD" : "") + (fontItalic ? " ITALIC" : "") + (fontStrikethru ? " Strike" : ""));
			DEBUG_STREAM.println("\\-> Fore (" + foreRed + ", " + foreGreen + ", " + foreBlue + ")");
			DEBUG_STREAM.println("\\-> Back (" + backRed + ", " + backGreen + ", " + backBlue + ")");
		}

		if (barcode128AsImage && fontName.toLowerCase().indexOf("barcode 128") >= 0 || fontName.toLowerCase().indexOf("barcode128") >= 0)
		{
			barcode = new Barcode128();
			barcode.setCodeType(Barcode128.CODE128);
		}
		else
		{
			barcode = null;
		}
		this.fontUnderline = fontUnderline;        
		this.fontStrikethru = fontStrikethru;        
        this.fontSize = fontSize;
        this.fontBold = fontBold;
        this.fontItalic = fontItalic;
		foreColor = new Color(foreRed, foreGreen, foreBlue);
        backColor = new Color(backRed, backGreen, backBlue);
		
		backFill = (backMode != 0);
		try {
			if (PDFFont.isType1(fontName))
			{
				//Me fijo si es un Asian font
				for(int i = 0; i < Type1FontMetrics.CJKNames.length; i++)
				{
					if(Type1FontMetrics.CJKNames[i][0].equalsIgnoreCase(fontName) ||
					   Type1FontMetrics.CJKNames[i][1].equalsIgnoreCase(fontName))
					{
						String style = "";
						if (fontBold && fontItalic)
							style = "BoldItalic";
						else
						{
							if (fontItalic)
								style = "Italic";
							if (fontBold)
								style = "Bold";
						}
						setAsianFont(fontName, style);
						isCJK = true;
						break;
					}
				}
				if (!isCJK)
				{
					int style = 0;
					if (fontBold && fontItalic)
						style = style + 3;
					else
					{
						if (fontItalic)
							style = style + 2;
						if (fontBold)
							style = style + 1;
					}
					for(int i=0;i<PDFFont.base14.length;i++)
					{
						if(PDFFont.base14[i][0].equalsIgnoreCase(fontName))
						{
							fontName =  PDFFont.base14[i][1+style].substring(1);
							break;
						}
					}
					baseFont = BaseFont.createFont(fontName, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
				}
			}
			else
			{//Si el Font es true type
				String style = "";
				if (fontBold && fontItalic)
					style = ",BoldItalic";
				else
				{
					if (fontItalic)
						style = ",Italic";
					if (fontBold)
						style = ",Bold";
				}

				fontName = fontName + style;
				String fontPath = getFontLocation(fontName);
				boolean foundFont = true;
				if (fontPath.equals(""))
				{
					uk.org.retep.pdf.PDFFontDescriptor fontDescriptor = uk.org.retep.pdf.PDFFontDescriptor.getPDFFontDescriptor();
					fontPath = fontDescriptor.getTrueTypeFontLocation(fontName, props);
					if (fontPath.equals(""))
					{
						baseFont = BaseFont.createFont("Helvetica", BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
						foundFont = false;
					}
				}
				if (foundFont)
				{
					if (isEmbeddedFont(fontName))
					{
						baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
					}
					else
					{//No se embebe el font
						baseFont = BaseFont.createFont(fontPath + style, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
					}
				}
			}
		}
		catch(DocumentException de) {
            System.err.println(de.getMessage());
        }
        catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

	private String getFontLocation(String fontName)
	{
		String fontPath = props.getProperty(Const.MS_FONT_LOCATION, fontName, "");
		if (fontPath.equals(""))
		{
			fontPath = props.getProperty(Const.SUN_FONT_LOCATION, fontName, "");
		}
		return fontPath;
	}
	@SuppressWarnings("unchecked")
	private Hashtable getFontLocations()
	{
		Hashtable msLocations = props.getSection(Const.MS_FONT_LOCATION);
		Hashtable sunLocations = props.getSection(Const.SUN_FONT_LOCATION);
		Hashtable locations = new Hashtable();
		if (msLocations != null)
		{
		   	for (Enumeration e = msLocations.keys(); e.hasMoreElements() ;) 
		   	{
				Object key = e.nextElement();
				locations.put(key, msLocations.get(key));
			}
		}
		if (sunLocations != null)
		{
		   	for (Enumeration e = sunLocations.keys(); e.hasMoreElements() ;) 
		   	{
				Object key = e.nextElement();
				locations.put(key, sunLocations.get(key));
			}
		}
		return locations;
	}

	private boolean isEmbeddedFont(String realFontName) {
		boolean generalEmbeedFont = props.getBooleanGeneralProperty(Const.EMBEED_SECTION, false);
		boolean generalEmbeedNotSpecified = props.getBooleanGeneralProperty(Const.EMBEED_NOT_SPECIFIED_SECTION, false);
		return generalEmbeedFont && props.getBooleanProperty(Const.EMBEED_SECTION, realFontName, generalEmbeedNotSpecified);
	}

	public void setAsianFont(String fontName, String style)
	{
		try
		{
			if (style.equals(""))
			{
				if (fontName.equals("Japanese"))
					baseFont = BaseFont.createFont("HeiseiMin-W3", "UniJIS-UCS2-HW-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Japanese2"))
					baseFont = BaseFont.createFont("HeiseiKakuGo-W5", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("SimplifiedChinese"))
					baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("TraditionalChinese"))
					baseFont = BaseFont.createFont("MHei-Medium", "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Korean"))
					baseFont = BaseFont.createFont("HYSMyeongJo-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
			}
			else
			{
				if (fontName.equals("Japanese"))
					baseFont = BaseFont.createFont("HeiseiMin-W3," + style, "UniJIS-UCS2-HW-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Japanese2"))
					baseFont = BaseFont.createFont("HeiseiKakuGo-W5," + style, "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("SimplifiedChinese"))
					baseFont = BaseFont.createFont("STSong-Light," + style, "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("TraditionalChinese"))
					baseFont = BaseFont.createFont("MHei-Medium," + style, "UniCNS-UCS2-H", BaseFont.NOT_EMBEDDED);
				if (fontName.equals("Korean"))
					baseFont = BaseFont.createFont("HYSMyeongJo-Medium," + style, "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED);
			}
		}
		catch(DocumentException de) {
            System.err.println(de.getMessage());
        }
        catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
	}
    /**
    * @deprecated
    */
    public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align)
    {
		GxDrawText(sTxt, left, top, right, bottom, align, 0);
	}
    public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat)
    {
		GxDrawText(sTxt, left, top, right, bottom, align, htmlformat, 0);
	}	
    public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border)
    {
		GxDrawText(sTxt, left, top, right, bottom, align, htmlformat, border, 0);
    }
    public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border, int valign)
    {
    	boolean printRectangle = false;
    	if (props.getBooleanGeneralProperty(Const.BACK_FILL_IN_CONTROLS, true))
    		printRectangle = true;
    		
    	if (printRectangle && (border == 1 || backFill))
    	{
    		GxDrawRect(left, top, right, bottom, border, foreColor.getRed(), foreColor.getGreen(), foreColor.getBlue(), backFill ? 1 : 0, backColor.getRed(), backColor.getGreen(), backColor.getBlue(), 0, 0);
    	}    	
		
        PdfContentByte cb = writer.getDirectContent();
		sTxt = CommonUtil.rtrim(sTxt);
		
		Font font = new Font(baseFont, fontSize);
		cb.setFontAndSize(baseFont, fontSize);
		cb.setColorFill(foreColor);
		int arabicOptions = 0;
		float captionHeight = 	baseFont.getFontDescriptor(baseFont.CAPHEIGHT, fontSize);
		float rectangleWidth = baseFont.getWidthPoint(sTxt, fontSize); 
		float lineHeight = baseFont.getFontDescriptor(baseFont.BBOXURY, fontSize) - baseFont.getFontDescriptor(baseFont.BBOXLLY, fontSize);
		float textBlockHeight = (float)convertScale(bottom-top);
		int linesCount =   (int)(textBlockHeight/lineHeight);
		int bottomOri = bottom;
		int topOri = top;
		
		//Si se tiene un campo con mas de una linea y no tiene wrap, no es justify, y no es html, se simula que el campo tiene una sola linea
		//asignando al bottom el top mas el lineHeight
		if (linesCount >= 2 && !((align & 16) == 16) && htmlformat != 1)
		{
			if (valign == VerticalAlign.TOP.value())
				bottom = top + (int)reconvertScale(lineHeight);
			else if (valign == VerticalAlign.BOTTOM.value())
				top = bottom - (int)reconvertScale(lineHeight);
			//if valign == middle, no se cambia ni top ni bottom
		}

		float bottomAux = (float)convertScale(bottom) - ((float)convertScale(bottom-top) - captionHeight)/2;   
		//Al bottom de los textos se le resta espacio entre el texto y el borde del textblock, 
		//porque en el reporte genexus la x,y de un
		//text es la x,y del cuadro que contiene el texto, y la api de itext espera la x,y del texto en si.
		//Generalmente el cuadro es mas grande que lo que ocupa el texto realmente (depende del tipo de font)
		//captionHeight esta convertido, bottom y top no.
		float topAux = (float)convertScale(top) + ((float)convertScale(bottom-top) - captionHeight)/2;   


		float startHeight = bottomAux - topAux - captionHeight;
		
		float leftAux = (float)convertScale(left);
		float rightAux = (float)convertScale(right);
		int alignment = align & 3;
		boolean autoResize = (align & 256) == 256;
		
        if (htmlformat == 1)
        {
            StyleSheet styles = new StyleSheet();
			Hashtable locations = getFontLocations();
			for (Enumeration e = locations.keys(); e.hasMoreElements() ;) 
		   	{
				String fontName = (String)e.nextElement();
				String fontPath = (String)locations.get(fontName);
				if (fontPath.equals(""))
				{
					uk.org.retep.pdf.PDFFontDescriptor fontDescriptor = uk.org.retep.pdf.PDFFontDescriptor.getPDFFontDescriptor();
					fontPath = fontDescriptor.getTrueTypeFontLocation(fontName, props);
				}
				if (!fontPath.equals(""))
				{
					FontFactory.register(fontPath, fontName);
					styles.loadTagStyle("body", "face", fontName);

					if (isEmbeddedFont(fontName)){
						styles.loadTagStyle("body", "encoding", BaseFont.IDENTITY_H);
					}
					else{
						styles.loadTagStyle("body", "encoding", BaseFont.WINANSI);
					}
				}
			}


            //Bottom y top son los absolutos, sin considerar la altura real a la que se escriben las letras.
            bottomAux = (float)convertScale(bottom);
            topAux = (float)convertScale(top);

            ColumnText Col = new ColumnText(cb);
            //Col.setSimpleColumn(llx, lly, urx, ury);
			Col.setSimpleColumn(leftAux + leftMargin,
                    0,//(float)this.pageSize.getTop() - bottomAux - topMargin - bottomMargin,
                    rightAux + leftMargin,
                    (float)this.pageSize.getTop() - topAux - topMargin - bottomMargin);
                                        
			Col.setYLine((float)this.pageSize.getTop() - topAux - topMargin - bottomMargin);
            try
            {
                ArrayList objects = HTMLWorker.parseToList(new StringReader(sTxt), styles);
                for (int k = 0; k < objects.size(); ++k)
                    Col.addElement((Element)objects.get(k));
            }
            catch (Exception ex1)
            {
                sTxt = ex1.getMessage();
                try
                {
                    ArrayList objects = HTMLWorker.parseToList(new StringReader(sTxt), styles);
                    for (int k = 0; k < objects.size(); ++k)
                        Col.addElement((Element)objects.get(k));
                }
                catch(Exception de1) {  }
            }

			try{
				Col.go();
			}catch(DocumentException de){
				System.out.println("ERROR printing HTML text " + de.getMessage());
			}
		}
		else 
		if (barcode!=null)
		{
			if(DEBUG)DEBUG_STREAM.println("Barcode: --> " + barcode.getClass().getName());
			try
			{
				barcode.setCode(sTxt);
				barcode.setTextAlignment(alignment);
				com.lowagie.text.Rectangle rectangle = new com.lowagie.text.Rectangle(0, 0);
				//El rectangulo tiene tamaño ok.
				switch (alignment)
				{
					case 1: // Center Alignment
						rectangle = new com.lowagie.text.Rectangle((leftAux + rightAux) / 2 + leftMargin - rectangleWidth / 2,
							(float)this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							(leftAux + rightAux) / 2 + leftMargin + rectangleWidth / 2,
							(float)this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin);
						break;
					case 2: // Right Alignment
						rectangle = new com.lowagie.text.Rectangle(rightAux + leftMargin - rectangleWidth,
							(float)this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							rightAux + leftMargin,
							(float)this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin);
						break;
					case 0: // Left Alignment
						rectangle = new com.lowagie.text.Rectangle(leftAux + leftMargin,
							(float)this.pageSize.getTop() - (float)convertScale(bottom) - topMargin - bottomMargin,
							leftAux + leftMargin + rectangleWidth,
							(float)this.pageSize.getTop() - (float)convertScale(top) - topMargin - bottomMargin);
						break;
				}
				barcode.setAltText("");
				barcode.setBaseline(0);
				//barcode.Size = 0;

				if (fontSize < Const.LARGE_FONT_SIZE)
					barcode.setX(Const.OPTIMAL_MINIMU_BAR_WIDTH_SMALL_FONT);
				else
					barcode.setX(Const.OPTIMAL_MINIMU_BAR_WIDTH_LARGE_FONT);

				Image imageCode = barcode.createImageWithBarcode(cb, backFill ? backColor : null, foreColor);
				imageCode.setAbsolutePosition(leftAux + leftMargin, rectangle.getBottom());
				barcode.setBarHeight(rectangle.getHeight());
				imageCode.scaleToFit(rectangle.getWidth(), rectangle.getHeight());
				document.add(imageCode);
			}
			catch (Exception ex)
			{
				if(DEBUG)DEBUG_STREAM.println("Error generating Barcode: --> " + barcode.getClass().getName() + ex.getMessage());
				if(DEBUG)ex.printStackTrace ();
			}
		}
		else
		{

			if(backFill)
			{
				com.lowagie.text.Rectangle rectangle = new com.lowagie.text.Rectangle(0,0);
				//Si el texto tiene background lo dibujo de esta forma
				switch(alignment)
				{
				case 1: // Center Alignment
					rectangle = new com.lowagie.text.Rectangle((leftAux + rightAux)/2 + leftMargin - rectangleWidth/2, (float)this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin , (leftAux + rightAux)/2 + leftMargin + rectangleWidth/2, (float)this.pageSize.getTop() - topAux - topMargin -bottomMargin);				
					break;
				case 2: // Right Alignment
					rectangle = new com.lowagie.text.Rectangle(rightAux + leftMargin - rectangleWidth, (float)this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin , rightAux + leftMargin, (float)this.pageSize.getTop() - topAux - topMargin -bottomMargin);				
					break;
				case 0: // Left Alignment
					rectangle = new com.lowagie.text.Rectangle(leftAux + leftMargin, (float)this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin , leftAux + leftMargin + rectangleWidth, (float)this.pageSize.getTop() - topAux - topMargin -bottomMargin);				
					break;
				}
				rectangle.setBackgroundColor(backColor);
				try
				{
					document.add(rectangle);
				}
				catch(DocumentException de) 
				{
					System.err.println(de.getMessage());
				}
			}
			
			float underlineSeparation = lineHeight / 5;//Separacion entre el texto y la linea del subrayado
			int underlineHeight = (int)underlineSeparation + (int)(underlineSeparation/4);
			com.lowagie.text.Rectangle underline;
			
			//Si el texto esta subrayado
			if (fontUnderline)
			{
				underline = new com.lowagie.text.Rectangle(0,0);
				
				switch(alignment)
				{
				case 1: // Center Alignment
						underline = new com.lowagie.text.Rectangle( 
							(leftAux + rightAux)/2 + leftMargin - rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation, 
							(leftAux + rightAux)/2 + leftMargin + rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight);				
					break;
				case 2: // Right Alignment
						underline = new com.lowagie.text.Rectangle( rightAux + leftMargin - rectangleWidth , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation, 
							rightAux + leftMargin, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);				
					break;
				case 0: // Left Alignment
						underline = new com.lowagie.text.Rectangle( leftAux + leftMargin , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation, 
							leftAux + leftMargin + rectangleWidth, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);				
					break;
				}
				underline.setBackgroundColor(foreColor);
				try
				{
					document.add(underline);
				}
				catch(DocumentException de) 
				{
					System.err.println(de.getMessage());
				}
			}
			
			//Si el texto esta tachado
			if (fontStrikethru)
			{
				underline = new com.lowagie.text.Rectangle(0,0);
				float strikethruSeparation = lineHeight / 2;
				
				switch(alignment)
				{
				case 1: // Center Alignment
						underline = new com.lowagie.text.Rectangle( 
							(leftAux + rightAux)/2 + leftMargin - rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation, 
							(leftAux + rightAux)/2 + leftMargin + rectangleWidth/2, 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);				
					break;
				case 2: // Right Alignment
						underline = new com.lowagie.text.Rectangle( rightAux + leftMargin - rectangleWidth , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation, 
							rightAux + leftMargin, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);				
					break;
				case 0: // Left Alignment
						underline = new com.lowagie.text.Rectangle( leftAux + leftMargin , 
							this.pageSize.getTop() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation, 
							leftAux + leftMargin + rectangleWidth, 
							this.pageSize.getTop() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);				
					break;
				}
				underline.setBackgroundColor(foreColor);
				try
				{
					document.add(underline);
				}
				catch(DocumentException de) 
				{
					System.err.println(de.getMessage());
				}
			}
			
			if(sTxt.trim().equalsIgnoreCase("{{Pages}}"))
			{// Si el texto es la cantidad de páginas del documento
				if (!templateCreated)
				{
					template = cb.createTemplate(right - left, bottom - top);
					templateCreated = true;
				}
				cb.addTemplate(template, leftAux + leftMargin, this.pageSize.getTop() -  bottomAux - topMargin -bottomMargin);
				templateFont = baseFont;
				templateFontSize = fontSize;
				templateColorFill = foreColor;
				return;
			}

			float textBlockWidth = rightAux - leftAux;
			float TxtWidth = baseFont.getWidthPoint(sTxt, fontSize);
			boolean justified = (alignment == 3) && textBlockWidth < TxtWidth;
			boolean wrap = ((align & 16) == 16);

			//Justified
            if (wrap || justified)
            {
                //Bottom y top son los absolutos, sin considerar la altura real a la que se escriben las letras.
                bottomAux = (float)convertScale(bottomOri);
                topAux = (float)convertScale(topOri);

                //La constante 2 para LEADING indica la separacion que se deja entre un renglon y otro. (es lo que mas se asemeja a la api vieja).
                float leading = (float)(Double.valueOf(props.getGeneralProperty(Const.LEADING)).doubleValue());
                Paragraph p = new Paragraph(sTxt, font);

				float llx = leftAux + leftMargin;
				float lly = (float)this.pageSize.getTop() - bottomAux - topMargin - bottomMargin;
				float urx = rightAux + leftMargin;
				float ury = (float)this.pageSize.getTop() - topAux - topMargin - bottomMargin;

                try{
					DrawColumnText(cb, llx, lly, urx, ury, p, leading, runDirection, valign, alignment);
                }
			    catch (DocumentException ex)
			    {
			    	ex.printStackTrace(System.err);
			    }
            }
			else //no wrap
			{
				startHeight=0;
				if (!autoResize)
				{
					//Va quitando el ultimo char del texto hasta que llega a un string cuyo ancho se pasa solo por un caracter
					//del ancho del textblock ("se pasa solo por un caracter": esto es porque en el caso general es ese el texto que
					//mas se parece a lo que se disenia en genexus).
					String newsTxt = sTxt;
					while(TxtWidth > textBlockWidth && (newsTxt.length()-1>=0))
					{
						sTxt = newsTxt;
						newsTxt = newsTxt.substring(0, newsTxt.length()-1);
						TxtWidth = baseFont.getWidthPoint(newsTxt, fontSize);
					}
				}

				Phrase phrase = new Phrase(sTxt, font);
				switch(alignment)
				{
				case 1: // Center Alignment
					ColumnText.showTextAligned(cb, cb.ALIGN_CENTER, phrase, ((leftAux + rightAux) / 2) + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin + startHeight, 0, runDirection, arabicOptions);
					break;
				case 2: // Right Alignment
					ColumnText.showTextAligned(cb, cb.ALIGN_RIGHT, phrase, rightAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin + startHeight, 0, runDirection, arabicOptions);
					break;
				case 0: // Left Alignment
				case 3: // Justified, only one text line
					ColumnText.showTextAligned(cb, cb.ALIGN_LEFT, phrase, leftAux + leftMargin, this.pageSize.getTop() - bottomAux - topMargin - bottomMargin + startHeight, 0, runDirection, arabicOptions);
					break;
				}
			}
		}
    }
	ColumnText SimulateDrawColumnText(PdfContentByte cb, Rectangle rect, Paragraph p, float leading, int runDirection, int alignment) throws DocumentException
	{
		ColumnText Col = new ColumnText(cb);
		Col.setRunDirection(runDirection);
		Col.setAlignment(alignment);
		Col.setLeading(leading, 1);
		Col.setSimpleColumn(rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop());
		Col.addText(p);
		Col.go(true);
		return Col;
	}
	void DrawColumnText(PdfContentByte cb, float llx, float lly, float urx, float ury, Paragraph p, float leading, int runDirection, int valign, int alignment) throws DocumentException
	{
		Rectangle rect = new Rectangle(llx, lly, urx, ury);
		ColumnText ct = SimulateDrawColumnText(cb, rect, p, leading, runDirection, alignment);//add the column in simulation mode
		float y = ct.getYLine();
		int linesCount = ct.getLinesWritten();

		//calculate a new rectangle for valign = middle 
		if (valign == VerticalAlign.MIDDLE.value())
			ury = ury - ((y - lly) / 2) + leading;
		else if (valign == VerticalAlign.BOTTOM.value())
			ury = ury - (y - lly- leading);
		else if (valign == VerticalAlign.TOP.value())
			ury = ury + leading/2;

		rect = new Rectangle(llx, lly, urx, ury); //Rectangle for new ury

		ColumnText Col = new ColumnText(cb);
		Col.setRunDirection(runDirection);
		if (linesCount <= 1)
			Col.setLeading(0, 1);
		else
			Col.setLeading(leading, 1);
		Col.setSimpleColumn(rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop());

		if (alignment == Element.ALIGN_JUSTIFIED)
			Col.setAlignment(justifiedType);
		else
			Col.setAlignment(alignment);
		Col.addText(p);
		Col.go();
	}    
    public void GxClearAttris()
    {
    }

	public static final double PAGE_SCALE_Y = 20; // Indica la escala de la página
    public static final double PAGE_SCALE_X = 20; // Indica la escala de la página
	public static final double GX_PAGE_SCALE_Y_OLD = 15.45;
    public static final double GX_PAGE_SCALE_Y = 14.4; // Indica la escala de la página, GeneXus lleva otra escala para el tamaño de la hoja, (variando este parametro, se agranda o achica el tamaño imprimible por GeneXus)
    //Por ejemplo: si en genexus se tiene un reporte con Paper Height de 1169 (A4) centésimos de pulgada (1/100 inch), 
    //en el parámetro pageLength llega 16834 que esta en Twips (16834 = 1169*14.4). 1 twip = 1/1440 inch.
    //Con el valor anterior 15.45 estaba quedando un margen bottom fijo que no se podia eliminar (incluso seteando mb 0).
	private static double TO_CM_SCALE =28.6;  // Escala CM -> metricas PDF (utilizado en el pageMargin)
    public boolean GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex)
    {
		PPP = gxYPage[0];
		loadPrinterSettingsProps(iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);
		
        if(outputStream != null)
		{
			if (output.equalsIgnoreCase("PRN"))
				outputType = Const.OUTPUT_STREAM_PRINTER;
			else
				outputType = Const.OUTPUT_STREAM;
		}
        else
        {
            if(output.equalsIgnoreCase("SCR"))
                outputType = Const.OUTPUT_SCREEN;
            else if(output.equalsIgnoreCase("PRN"))
                outputType = Const.OUTPUT_PRINTER;
            else outputType = Const.OUTPUT_FILE;

            if(outputType == Const.OUTPUT_FILE)
                TemporaryFiles.getInstance().removeFileFromList(docName);
            else
            {
                String tempPrefix = docName;
                String tempExtension = "pdf";
                int tempIndex = docName.lastIndexOf('.');
                if(tempIndex != -1)
                {
                    tempPrefix = docName.substring(0, tempIndex);
                    tempExtension = ((docName + " ").substring(tempIndex + 1)).trim();
                }
                docName = TemporaryFiles.getInstance().getTemporaryFile(tempPrefix, tempExtension);
            }
            try
            {
                setOutputStream(new FileOutputStream(docName));
            }catch(IOException accessError)
            { // Si no se puede generar el archivo, muestro el stackTrace y seteo el stream como NullOutputStream
                accessError.printStackTrace(System.err);
                outputStream = new com.genexus.util.NullOutputStream();
                outputType = Const.OUTPUT_FILE; // Hago esto para no tener lios con el Acrobat
            }
        }
        printerOutputMode = mode;
			
			boolean ret;
            ret = props.setupGeneralProperty(Const.LEFT_MARGIN, Const.DEFAULT_LEFT_MARGIN);
            ret = props.setupGeneralProperty(Const.TOP_MARGIN, Const.DEFAULT_TOP_MARGIN);
			ret = props.setupGeneralProperty(Const.BOTTOM_MARGIN, Const.DEFAULT_BOTTOM_MARGIN);
			leftMargin = (float) (TO_CM_SCALE * Double.valueOf(props.getGeneralProperty(Const.LEFT_MARGIN)).doubleValue());
			topMargin = (float) (TO_CM_SCALE * Double.valueOf(props.getGeneralProperty(Const.TOP_MARGIN)).doubleValue());
			bottomMargin = (float) (Double.valueOf(props.getGeneralProperty(Const.BOTTOM_MARGIN)).doubleValue()); 

			lineCapProjectingSquare = props.getGeneralProperty(Const.LINE_CAP_PROJECTING_SQUARE).equals("true");
			barcode128AsImage = props.getGeneralProperty(Const.BARCODE128_AS_IMAGE).equals("true");
		STYLE_DOTTED = parsePattern(props.getGeneralProperty(Const.STYLE_DOTTED));
		STYLE_DASHED = parsePattern(props.getGeneralProperty(Const.STYLE_DASHED));
		STYLE_LONG_DASHED = parsePattern(props.getGeneralProperty(Const.STYLE_LONG_DASHED));
		STYLE_LONG_DOT_DASHED = parsePattern(props.getGeneralProperty(Const.STYLE_LONG_DOT_DASHED));
		
		runDirection = Integer.valueOf(props.getGeneralProperty(Const.RUN_DIRECTION)).intValue();

		if (props.getBooleanGeneralProperty(Const.JUSTIFIED_TYPE_ALL, false))
			justifiedType = Element.ALIGN_JUSTIFIED_ALL;
		else
			justifiedType = Element.ALIGN_JUSTIFIED;

		//Se ignora el parametro orientation para el calculo del pageSize, los valores de alto y ancho ya vienen invertidos si Orientation=2=landscape.
        this.pageSize = computePageSize(leftMargin, topMargin, pageWidth, pageLength, props.getBooleanGeneralProperty(Const.MARGINS_INSIDE_BORDER, false));
        gxXPage[0] = (int)this.pageSize.getRight();
		if (props.getBooleanGeneralProperty(Const.FIX_SAC24437, true))
			gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y); // Cuanto menor sea GX_PAGE_SCALE_Y, GeneXus imprime mayor parte de cada hoja
		else
			gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y_OLD); // Cuanto menor sea GX_PAGE_SCALE_Y, GeneXus imprime mayor parte de cada hoja


        //Ahora chequeamos que el margen asociado en el PDFReport.INI sea correcto, y si es inválido, asociamos el que sea por defecto
        //if(leftMargin > this.pageSize.width || topMargin > this.pageSize.height)
        //{ // Si el margen asociado es mayor que el tamaño de la página, entonces asociamos los márgenes por default

			//float leftMargin = (float) (TO_CM_SCALE * Double.valueOf(Const.DEFAULT_LEFT_MARGIN).doubleValue());
			//float topMargin = (float) (TO_CM_SCALE * Double.valueOf(Const.DEFAULT_TOP_MARGIN).doubleValue());

			//float rightMargin = 0;
			//float bottomMargin = 0;
            //System.err.println("Invalid page Margin... Resetting to defaults");
        //}


		document = new Document(this.pageSize,0,0,0,0);

        init();

        //if(DEBUG)DEBUG_STREAM.println("GxPrintInit ---> Size:" + this.pageSize + " Orientation: " + (pageOrientation == PDFPage.PORTRAIT ? "Portrait" : "Landscape"));

        return true;
    }

	private com.lowagie.text.Rectangle computePageSize(float leftMargin, float topMargin, int width, int length, boolean marginsInsideBorder)
	{
		if ((leftMargin == 0 && topMargin == 0)||marginsInsideBorder)
		{
			if (length == 23818 && width == 16834)
				return PageSize.A3;
			else if (length == 16834 && width == 11909)
				return PageSize.A4;
			else if (length == 11909 && width == 8395)
				return PageSize.A5;
			else if (length == 20016 && width == 5731)
				return PageSize.B4;
			else if (length == 14170 && width == 9979)
				return PageSize.B5;
			else if (length == 15120 && width == 10440)
				return PageSize.EXECUTIVE;
			else if (length == 20160 && width == 12240)
				return PageSize.LEGAL;
			else if (length == 15840 && width == 12240)
				return PageSize.LETTER;
			else
				return new com.lowagie.text.Rectangle((int)(width / PAGE_SCALE_X) , (int)(length / PAGE_SCALE_Y) );
		}
		return new com.lowagie.text.Rectangle((int)(width / PAGE_SCALE_X) + leftMargin, (int)(length / PAGE_SCALE_Y) + topMargin);
	}

    public int getPageLines()
    {
        if(DEBUG)DEBUG_STREAM.println("getPageLines: --> " + pageLines);
        return pageLines;
    }
    public int getLineHeight()
    {
        if(DEBUG)DEBUG_STREAM.println("getLineHeight: --> " + this.lineHeight);
        return this.lineHeight;
    }
    public void setPageLines(int P_lines)
    {
        if(DEBUG)DEBUG_STREAM.println("setPageLines: " + P_lines);
        pageLines = P_lines;
    }
    public void setLineHeight(int lineHeight)
    {

        if(DEBUG)DEBUG_STREAM.println("setLineHeight: " + lineHeight);
		this.lineHeight = lineHeight;
    }
	
	int M_top ;
	int M_bot ;
	
	public	int getM_top()
	{
		return M_top;
	}
	
    public int getM_bot()
	{
		return M_bot;
	}
	
	public void setM_top(int M_top)
	{
		this.M_top = M_top;
	}

	public void setM_bot(int M_bot)
	{
		this.M_bot = M_bot;
	}	

    public void GxEndPage()
    {
        //if(DEBUG)DEBUG_STREAM.println("GxEndPage");

        //if(document != null && !isPageDirty)
        //    document.dispose();
//        if(document == null) GxStartPage(); // Si la página esta vacú}, la agrego primero al PDF. Esto hace que haya una hoja vacú} al final, as\uFFFDEque lo saco
//        document = null; // La nueva página est\uFFFDEvacú}
    }

    public void GxEndDocument()
    {
      if(document.getPageNumber() == 0)
      { // Si no hay ninguna página en el documento, agrego una vacia}
        writer.setPageEmpty(false);
      }

        //Ahora proceso los comandos GeneXus {{Pages}}
	    if (template != null)
		{
	  		template.beginText();
			template.setFontAndSize(templateFont, templateFontSize);
			template.setTextMatrix(0,0);
			template.setColorFill(templateColorFill);
			template.showText(String.valueOf(pages));
			template.endText();
		}
		int copies = 1;
		try
		{
			copies = Integer.parseInt(printerSettings.getProperty(form, Const.COPIES));
			if(DEBUG)DEBUG_STREAM.println("Setting number of copies to " + copies);
			writer.addViewerPreference(PdfName.NUMCOPIES, new PdfNumber(copies));

			int duplex= Integer.parseInt(printerSettings.getProperty(form, Const.DUPLEX));
			PdfName duplexValue;
			switch (duplex){
				case 1: duplexValue = PdfName.SIMPLEX; break;
				case 2: duplexValue = PdfName.DUPLEX; break;
				case 3: duplexValue = PdfName.DUPLEXFLIPSHORTEDGE;break;
				case 4: duplexValue = PdfName.DUPLEXFLIPLONGEDGE;break;
				default: duplexValue = PdfName.NONE;
			}
			if(DEBUG)DEBUG_STREAM.println("Setting duplex to " + duplexValue.toString());
				writer.addViewerPreference(PdfName.DUPLEX, duplexValue);
		}
		catch(Exception ex)
		{
			ex.printStackTrace(System.err);
		}

		String serverPrinting = props.getGeneralProperty(Const.SERVER_PRINTING);
		boolean fit= props.getGeneralProperty(Const.ADJUST_TO_PAPER).equals("true");
		if ((outputType==Const.OUTPUT_PRINTER || outputType==Const.OUTPUT_STREAM_PRINTER) && (httpContext instanceof HttpContextWeb && serverPrinting.equals("false")))
		{
			//writer.addJavaScript("if (this.external)\n");//Specifies whether the current document is being viewed in the Acrobat application or in an external window (such as a web browser).
			//writer.addJavaScript("app.alert('SI es externa' + this.external);");

			
				writer.addJavaScript("var pp = this.getPrintParams();\n");
				//writer.addJavaScript("pp.interactive = pp.constants.interactionLevel.automatic;\n");
				String printerAux=printerSettings.getProperty(form, Const.PRINTER);
				String printer = replace(printerAux, "\\", "\\\\");

				if (printer!=null && !printer.equals(""))
				{
	                writer.addJavaScript("pp.printerName = \"" + printer + "\";\n");
				}

			if (fit)
			{
				writer.addJavaScript("pp.pageHandling = pp.constants.handling.fit;\n");
			}
			else
			{
				writer.addJavaScript("pp.pageHandling = pp.constants.handling.none;\n");
			}

				if (printerSettings.getProperty(form, Const.MODE, "3").startsWith("0"))//Show printer dialog Never
				{
					writer.addJavaScript("pp.interactive = pp.constants.interactionLevel.automatic;\n");
					//No print dialog is displayed. During printing a progress monitor and cancel
					//dialog is displayed and removed automatically when printing is complete.
					for(int i = 0; i < copies; i++)
					{
						writer.addJavaScript("this.print(pp);\n");
					}
				}
				else //Show printer dialog is sent directly to printer | always
				{
					writer.addJavaScript("pp.interactive = pp.constants.interactionLevel.full;\n");
					//Displays the print dialog allowing the user to change print settings and requiring
					//the user to press OK to continue. During printing a progress monitor and cancel
					//dialog is displayed and removed automatically when printing is complete.
					writer.addJavaScript("this.print(pp);\n");
				}
				
		}

		document.close();

        if(DEBUG)DEBUG_STREAM.println("GxEndDocument!");
        //showInformation();
        //ParseINI props = pdf.getPDF().props;
        //pdf.end();

		try{ props.save(); } catch(IOException e) { ; }


        // OK, ahora que ya terminamos el PDF, vemos si tenemos que mostrarlo en pantalla
        switch(outputType)
        {
        case Const.OUTPUT_SCREEN:
            try{ outputStream.close(); } catch(IOException e) { ; } // Cierro el archivo
            try{ showReport(docName, modal); } catch(Exception e)
            { // Si no se puede mostrar el reporte
                e.printStackTrace();
            }
//  Comento la próxima lú‹ea, porque por manejo interno del Acrobat, si ya habú} una instancia del
//  Acrobat corriendo, el modal no funciona (x que el proceso levantado le avisa al que ya estaba abierto qu\uFFFDE
//  archivo abrir y luego se mata automáticamente)
//            if(modal)if(new File(docName).delete())TemporaryFiles.getInstance().removeFileFromList(docName); // Intento eliminar el docName aqu\uFFFDE
            break;
        case Const.OUTPUT_PRINTER:
            try{ outputStream.close(); } catch(IOException e) { ; } // Cierro el archivo
            try{
				if (!(httpContext instanceof HttpContextWeb) || !serverPrinting.equals("false"))
				{
					printReport(docName, this.printerOutputMode == 1); 
				}
			} catch(Exception e){ // Si no se puede mostrar el reporte
                e.printStackTrace(); 
            }
            break;
        case Const.OUTPUT_FILE:
            try{ outputStream.close(); } catch(IOException e) { ; } // Cierro el archivo
            break;
        case Const.OUTPUT_STREAM:
		case Const.OUTPUT_STREAM_PRINTER:
        default: break;
        }
        outputStream = null;
    }

    public void GxEndPrinter()
    {
        //DEBUG.println("Processing...");
    }
    public void GxStartPage()
    {
		//try
		//{
			boolean ret = document.newPage();
			pages = pages +1;
		//}
		//catch(DocumentException de) {
        //    System.err.println(de.getMessage());
        //}
	}

    public void GxStartDoc()
    {
        //DEBUG.println("Processing...");
    }
    public void GxSetDocFormat(String format)
    {
        //DEBUG.println("Processing...");
    }

    public void GxSetDocName(String docName)
    {
        this.docName = docName.trim();
        if(this.docName.indexOf('.') < 0)
            this.docName += ".pdf";        
		if(!new File(docName).isAbsolute())
		{ // Si el nombre del documento es relativo, veo si hay que agregarle el outputDir
			String outputDir = props.getGeneralProperty(Const.OUTPUT_FILE_DIRECTORY, "").replace(alternateSeparator, File.separatorChar).trim();
			if(!outputDir.equalsIgnoreCase("") && !outputDir.equalsIgnoreCase("."))
			{
				if(!outputDir.endsWith(File.separator))
				{
					outputDir += File.separator;
				}
				new File(outputDir).mkdirs();
				this.docName = outputDir + this.docName;
			}
			else
			{
				if (ModelContext.getModelContext() != null) 
				{
					HttpContext webContext = (HttpContext) ModelContext.getModelContext().getHttpContext();
					if ((webContext != null) && (webContext instanceof HttpContextWeb))
					{
						outputDir = com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator;
						this.docName = outputDir + this.docName;
					}
				}
			}			
		}
        if(this.docName.indexOf('.') < 0)
            this.docName += ".pdf";
        if(DEBUG)DEBUG_STREAM.println("GxSetDocName: '" + this.docName + "'");
    }

    public boolean GxPrTextInit(String ouput, int nxPage[], int nyPage[], String psIniFile, String psForm, String sPrinter, int nMode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines)
    {
        //DEBUG.println("Processing...");
        return true;
    }

    public boolean GxPrnCfg( String ini )
    {
        //DEBUG.println("Processing...");
        return true;
    }

    public boolean GxIsAlive()
    {
        return false;
    }

    public boolean GxIsAliveDoc()
    {
        return true;
    }


    private int page;
    public int getPage()
    {
        return page;
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public boolean getModal()
    {
        return modal;
    }
    public void setModal(boolean modal)
    {
        this.modal = modal;
    }
	
	public void cleanup() {}

    public void setMetrics(String fontName, boolean bold, boolean italic, int ascent, int descent, int height, int maxAdvance, int[] sizes)
    {
    }


    /** Carga la tabla de substitutos
     */
    private void loadSubstituteTable()
    {
        // Primero leemos la tabla de substitutos del Registry
        Hashtable<String, Vector<String>> tempInverseMappings = new Hashtable<>();
        
        // Seteo algunos Mappings que Acrobat toma como Type1
        for(int i = 0; i < Const.FONT_SUBSTITUTES_TTF_TYPE1.length; i++)
        	fontSubstitutes.put(Const.FONT_SUBSTITUTES_TTF_TYPE1[i][0], Const.FONT_SUBSTITUTES_TTF_TYPE1[i][1]);

        // Ahora inserto los mappings extra del PDFReport.INI (si es que hay)
        // Los font substitutes del PDFReport.INI se encuentran bajo la seccion
        // indicada por Const.FONT_SUBSTITUTES_SECTION y son pares oldFont -> newFont
        Hashtable otherMappings = props.getSection(Const.FONT_SUBSTITUTES_SECTION);
        if(otherMappings != null)
            for(Enumeration enumera = otherMappings.keys(); enumera.hasMoreElements();)
            {
                String fontName = (String)enumera.nextElement();
                fontSubstitutes.put(fontName, (String)otherMappings.get(fontName));
                if(tempInverseMappings.containsKey(fontName)) // Con esto solucionamos el tema de la recursión de Fonts -> Fonts
                {                                             // x ej: Si tenú} Font1-> Font2, y ahora tengo Font2->Font3, pongo cambio el 1º por Font1->Font3
                    String fontSubstitute = (String)otherMappings.get(fontName);
                    for(Enumeration<String> enum2 = tempInverseMappings.get(fontName).elements(); enum2.hasMoreElements();)
                        fontSubstitutes.put(enum2.nextElement(), fontSubstitute);
                }
            }
    }


    /** Estos métodos no hacen nada en este contexto
     */
    public void GxPrintMax() { ; }
    public void GxPrintNormal() { ; }
    public void GxPrintOnTop() { ; }
    public void GxPrnCmd(String cmd)  { ; }


    public void showInformation()
    {
    }

	public static final double SCALE_FACTOR = 72;
	private double PPP = 96;
	private double convertScale(int value)
	{
		double result = value * SCALE_FACTOR / PPP;
		return result;
	}
		
	private double convertScale(double value)
	{
		double result = value * SCALE_FACTOR / PPP;
		return result;
	}
	
	private float reconvertScale(float value)
	{
		float result = value / (float)(SCALE_FACTOR / PPP);
		return result;
	}		

	class FontProps
	{
		public int horizontal;
		public int vertical;
	}
	
    /**
     * Helper method for toString()
     * @param s source string
     * @param f string to remove
     * @param t string to replace f
     * @return string with f replaced by t
     */
    private static String replace(String s,String f,String t) {
	StringBuffer b = new StringBuffer();
	int p = 0, c=0;

	while(c>-1) {
	    if((c = s.indexOf(f,p)) > -1) {
		b.append(s.substring(p,c));
		b.append(t);
		p=c+1;
	    }
	}

	// include any remaining text
	if(p<s.length())
	    b.append(s.substring(p));

	return b.toString();
    }	

    /** Contiene un state de una pagina (atributos, etc) + un String a ingresar
     *  La idea es que se pueda agregar Strings en cualquier página al final del proceso del PDF
     *//*
    class TextProperties
    {
        public final PDFGraphics document;
        public final Font font;
        public final Color foreColor;
        public final Color backColor;
        public final boolean fontUnderline;
        public final boolean fontStrikeThru;
        public final String text;
        public final int left, top, right, bottom, align;
        public TextProperties(PDFGraphics document, Font font, Color foreColor, Color backColor,
                              boolean fontUnderline, boolean fontStrikeThru, String text, int left,
                              int top, int right, int bottom, int align)
        {
            this.document = document;
            this.font = font;
            this.foreColor = foreColor;
            this.backColor = backColor;
            this.fontUnderline = fontUnderline;
            this.fontStrikeThru = fontStrikeThru;
            this.text = text;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.align = align;
        }
    }*/

}
