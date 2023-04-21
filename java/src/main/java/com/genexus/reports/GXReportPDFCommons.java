package com.genexus.reports;

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

import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.platform.INativeFunctions;
import com.genexus.platform.NativeFunctions;
import com.genexus.reports.fonts.Utilities;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.util.TemporaryFiles;

import java.awt.*;
import java.io.*;
import java.util.*;

public abstract class GXReportPDFCommons implements IReportHandler{
	protected int lineHeight, pageLines;
	protected int pageOrientation;
	protected boolean fontUnderline;
	protected boolean fontStrikethru;
	protected int fontSize;
	protected boolean fontBold = false;
	protected boolean fontItalic = false;
	protected Color backColor, foreColor;
	public static PrintStream DEBUG_STREAM = System.out;
	protected OutputStream outputStream = null;
	protected static ParseINI props = new ParseINI();
	protected ParseINI printerSettings;
	protected String form;
	protected Vector stringTotalPages;
	protected boolean isPageDirty;
	protected int outputType = -1;
	protected int printerOutputMode = -1;
	protected boolean modal = false;
	protected String docName = "PDFReport.pdf";
	protected static INativeFunctions nativeCode = NativeFunctions.getInstance();
	protected static Hashtable<String, String> fontSubstitutes = new Hashtable<>();
	protected static String configurationFile = null;
	protected static String configurationTemplateFile = null;
	protected static String defaultRelativePrepend = null;
	protected static String defaultRelativePrependINI = null;
	protected static String webAppDir = null;
	public static boolean DEBUG = false;
	protected int currLine;
	protected int lastLine = 0;
	private static String predefinedSearchPath = "";
	protected float leftMargin;
	protected float topMargin;
	protected float bottomMargin;
	protected int templateFontSize;
	protected boolean backFill = true;
	protected Color templateColorFill;
	protected int pages=0;
	protected boolean templateCreated = false;
	public static float DASHES_UNITS_ON = 10;
	public static float DASHES_UNITS_OFF = 10;
	public static float DOTS_UNITS_OFF = 3;
	public static float DOTS_UNITS_ON = 1;
	public boolean lineCapProjectingSquare = true;
	public boolean barcode128AsImage = true;
	public int justifiedType;
	protected HttpContext httpContext = null;
	protected static boolean firstTime = true;
	float[] STYLE_SOLID = new float[]{1,0};
	float[] STYLE_NONE = null;
	float[] STYLE_DOTTED,
		STYLE_DASHED,
		STYLE_LONG_DASHED,
		STYLE_LONG_DOT_DASHED;
	int STYLE_NONE_CONST=1;

	protected enum VerticalAlign{
		TOP(0),
		MIDDLE(1),
		BOTTOM(2);
		private int intValue;
		VerticalAlign(int val) {
			this.intValue=val;
		}
		public int value(){
			return intValue;
		}
	}
	protected static char alternateSeparator = File.separatorChar == '/' ? '\\' : '/';

	/** Setea el OutputStream a utilizar
	 *  @param outputStream Stream a utilizar
	 */
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	/** Busca la ubicación del Acrobat. Si no la encuentra tira una excepción
	 */
	protected static String getAcrobatLocation() throws Exception {
		ParseINI props;
		try {
			props = new ParseINI(Const.INI_FILE);
			if(new File(Const.INI_FILE).length() == 0)
				new File(Const.INI_FILE).delete();
		}
		catch(IOException e) {
			props = new ParseINI();
		}
		// Primero debo obtener la ubicación + ejecutable del Acrobat
		String acrobatLocation = props.getGeneralProperty(Const.ACROBAT_LOCATION); // Veo si esta fijada la ubicación del Acrobat en la property
		if(acrobatLocation == null)
		{
			if(NativeFunctions.isUnix()) { // Si estoy en Unix no puedo ir a buscar el registry ;)
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
	 * @exception Exception si no se puede realizar la operación
	 */
	public static void printReport(String pdfFilename, boolean silent) throws Exception {
		if(NativeFunctions.isWindows()) { // En Windows obtenemos el full path
			// En Linux esto no anda bien
			pdfFilename = "\"" + new File(pdfFilename).getAbsolutePath() + "\"";
		}

		String [] cmd = {};
		String acrobatLocation = null;
		try {
			// Primero debo obtener la ubicación + ejecutable del Acrobat
			acrobatLocation = getAcrobatLocation();
		}catch(Exception acrobatNotFound) {
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
	 * @exception Exception no se puede encontrar el Acrobat
	 */
	public static void showReport(String filename, boolean modal) throws Exception {
		if(NativeFunctions.isWindows()) { // En Windows obtenemos el full path
			// En Linux esto no anda bien
			filename = "\"" + new File(filename).getAbsolutePath() + "\"";
		}
		String acrobatLocation;
		try {
			// Primero debo obtener la ubicación + ejecutable del Acrobat
			acrobatLocation = getAcrobatLocation();
		}catch(Exception acrobatNotFound) {
			throw new Exception("Acrobat cannot be found in this machine: " + acrobatNotFound.getMessage());
		}

		if(modal) {
			nativeCode.executeModal(acrobatLocation + " " + filename, true);
		}
		else {
			Runtime.getRuntime().exec(new String[] { acrobatLocation, filename});
		}
	}
	public GXReportPDFCommons(ModelContext context) {
		stringTotalPages = new Vector();
		httpContext = (HttpContext) context.getHttpContext();

		if(defaultRelativePrepend == null) {
			defaultRelativePrepend = httpContext.getDefaultPath();
			if(defaultRelativePrepend == null || defaultRelativePrepend.trim().equals(""))
				defaultRelativePrepend = "";
			else
				defaultRelativePrepend = defaultRelativePrepend.replace(alternateSeparator, File.separatorChar) + File.separatorChar;
			defaultRelativePrependINI = defaultRelativePrepend;
			if(new File(defaultRelativePrepend + Const.WEB_INF).isDirectory()) {
				configurationFile = defaultRelativePrepend + Const.WEB_INF + File.separatorChar + Const.INI_FILE; // Esto es para que en aplicaciones web el PDFReport.INI no quede visible en el server
				configurationTemplateFile = defaultRelativePrepend + Const.WEB_INF + File.separatorChar + Const.INI_TEMPLATE_FILE;
			}
			else {
				configurationFile = defaultRelativePrepend + Const.INI_FILE;
				configurationTemplateFile = defaultRelativePrepend + Const.INI_TEMPLATE_FILE;
			}
			webAppDir = defaultRelativePrepend;

			if(httpContext instanceof HttpContextWeb || !httpContext.getDefaultPath().isEmpty()) {
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
				if(staticContentBase != null) {
					staticContentBase = staticContentBase.trim();
					if(staticContentBase.indexOf(':') == -1) { // Si la staticContentBase es una ruta relativa
						staticContentBase = staticContentBase.replace(alternateSeparator, File.separatorChar);
						if(staticContentBase.startsWith(File.separator)) {
							staticContentBase = staticContentBase.substring(1);
						}
						if(!staticContentBase.equals("")) {
							defaultRelativePrepend += staticContentBase;
							if(!defaultRelativePrepend.endsWith(File.separator)) {
								defaultRelativePrepend += File.separator;
							}
						}
					}
				}
			}
		}
		if (firstTime) {
			loadProps();
			firstTime = false;
		}
	}

	protected void loadPrinterSettingsProps(String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex)
	{
		if(new File(defaultRelativePrependINI + Const.WEB_INF).isDirectory()) {
			iniFile = defaultRelativePrependINI + Const.WEB_INF + File.separatorChar + iniFile;
		}
		else {
			iniFile = defaultRelativePrependINI + iniFile;
		}

		try {
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

	protected void loadProps() {
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

		if(props.getBooleanGeneralProperty("DEBUG", false)) {
			DEBUG = true;
			DEBUG_STREAM = System.out;
		}
		else {
			DEBUG = false;
			DEBUG_STREAM = new PrintStream(new com.genexus.util.NullOutputStream());
		}

		Utilities.addPredefinedSearchPaths(new String[]{System.getProperty("java.awt.fonts", "c:\\windows\\fonts"),
			System.getProperty("com.ms.windir", "c:\\windows") + "\\fonts"});
	}

	public static final void addPredefinedSearchPaths(String [] predefinedPaths) {
		String predefinedPath = "";
		for(int i = 0; i < predefinedPaths.length; i++)
			predefinedPath += predefinedPaths[i] + ";";
		predefinedSearchPath = predefinedPath + predefinedSearchPath; // SearchPath= los viejos más los nuevos
	}

	public static final String getPredefinedSearchPaths()
	{
		return predefinedSearchPath;
	}

	protected abstract void init();

	public void GxRVSetLanguage(String lang) {}

	public void GxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength) {}

	protected float[] parsePattern(String patternStr) {
		if (patternStr!=null) {
			StringTokenizer st = new StringTokenizer(patternStr.trim(), ";");
			int length = st.countTokens();
			if (length>0) {
				int i = 0;
				float[] pattern = new float[length];
				while(st.hasMoreTokens()) {
					pattern[i] = Float.parseFloat(st.nextToken());
					i++;
				}
				return pattern;
			}
		}
		return null;
	}

	protected float [] getDashedPattern(int style) {
		switch(style) {
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

	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue) {
		GxDrawRect(left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue, 0, 0);
	}

	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, int style, int cornerRadius) {
		GxDrawRect(left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue, style, style, style, style, cornerRadius, cornerRadius, cornerRadius, cornerRadius);
	}
	public abstract void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue,
						   int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR);

	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue) {
		GxDrawLine(left, top, right, bottom, width, foreRed, foreGreen, foreBlue, 0);
	}

	public abstract void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style);

	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom) {
		GxDrawBitMap(bitmap, left, top, right, bottom, 0);
	}

	public abstract void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio);

	public String getSubstitute(String fontName) {
		Vector<String> fontSubstitutesProcessed = new Vector<>();
		String newFontName = fontName;
		while( fontSubstitutes.containsKey(newFontName)) {
			if (!fontSubstitutesProcessed.contains(newFontName)) {
				fontSubstitutesProcessed.addElement(newFontName);
				newFontName = fontSubstitutes.get(newFontName);
			}
			else {
				return fontSubstitutes.get(newFontName);
			}
		}
		return newFontName;
	}

	public abstract void GxAttris(String fontName, int fontSize, boolean fontBold, boolean fontItalic, boolean fontUnderline, boolean fontStrikethru, int Pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue);

	protected String getFontLocation(String fontName) {
		String fontPath = props.getProperty(Const.MS_FONT_LOCATION, fontName, "");
		if (fontPath.equals("")) {
			fontPath = props.getProperty(Const.SUN_FONT_LOCATION, fontName, "");
		}
		return fontPath;
	}
	@SuppressWarnings("unchecked")
	protected Hashtable getFontLocations() {
		Hashtable msLocations = props.getSection(Const.MS_FONT_LOCATION);
		Hashtable sunLocations = props.getSection(Const.SUN_FONT_LOCATION);
		Hashtable locations = new Hashtable();
		if (msLocations != null) {
			for (Enumeration e = msLocations.keys(); e.hasMoreElements() ;) {
				Object key = e.nextElement();
				locations.put(key, msLocations.get(key));
			}
		}
		if (sunLocations != null) {
			for (Enumeration e = sunLocations.keys(); e.hasMoreElements() ;) {
				Object key = e.nextElement();
				locations.put(key, sunLocations.get(key));
			}
		}
		return locations;
	}

	protected boolean isEmbeddedFont(String realFontName) {
		boolean generalEmbeedFont = props.getBooleanGeneralProperty(Const.EMBEED_SECTION, false);
		boolean generalEmbeedNotSpecified = props.getBooleanGeneralProperty(Const.EMBEED_NOT_SPECIFIED_SECTION, false);
		return generalEmbeedFont && props.getBooleanProperty(Const.EMBEED_SECTION, realFontName, generalEmbeedNotSpecified);
	}

	public abstract void setAsianFont(String fontName, String style);

	/**
	 * @deprecated
	 */
	public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align) {
		GxDrawText(sTxt, left, top, right, bottom, align, 0);
	}
	public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat) {
		GxDrawText(sTxt, left, top, right, bottom, align, htmlformat, 0);
	}
	public void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border) {
		GxDrawText(sTxt, left, top, right, bottom, align, htmlformat, border, 0);
	}
	public abstract void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border, int valign);
	boolean pageHeightExceeded(float bottomAux, float drawingPageHeight){
		return bottomAux > drawingPageHeight;
	}

	public void GxClearAttris() {}

	public static final double PAGE_SCALE_Y = 20; // Indica la escala de la página
	public static final double PAGE_SCALE_X = 20; // Indica la escala de la página
	public static final double GX_PAGE_SCALE_Y_OLD = 15.45;
	public static final double GX_PAGE_SCALE_Y = 14.4; // Indica la escala de la página, GeneXus lleva otra escala para el tamaño de la hoja, (variando este parametro, se agranda o achica el tamaño imprimible por GeneXus)
	//Por ejemplo: si en genexus se tiene un reporte con Paper Height de 1169 (A4) centésimos de pulgada (1/100 inch),
	//en el parámetro pageLength llega 16834 que esta en Twips (16834 = 1169*14.4). 1 twip = 1/1440 inch.
	//Con el valor anterior 15.45 estaba quedando un margen bottom fijo que no se podia eliminar (incluso seteando mb 0).
	protected static double TO_CM_SCALE = 28.6;  // Escala CM -> metricas PDF (utilizado en el pageMargin)

	private boolean preGxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex){
		try {
			PPP = gxYPage[0];
			loadPrinterSettingsProps(iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);

			if(outputStream != null) {
				if (output.equalsIgnoreCase("PRN"))
					outputType = Const.OUTPUT_STREAM_PRINTER;
				else
					outputType = Const.OUTPUT_STREAM;
			}
			else {
				if(output.equalsIgnoreCase("SCR"))
					outputType = Const.OUTPUT_SCREEN;
				else if(output.equalsIgnoreCase("PRN"))
					outputType = Const.OUTPUT_PRINTER;
				else outputType = Const.OUTPUT_FILE;

				if(outputType == Const.OUTPUT_FILE)
					TemporaryFiles.getInstance().removeFileFromList(docName);
				else {
					String tempPrefix = docName;
					String tempExtension = "pdf";
					int tempIndex = docName.lastIndexOf('.');
					if(tempIndex != -1) {
						tempPrefix = docName.substring(0, tempIndex);
						tempExtension = ((docName + " ").substring(tempIndex + 1)).trim();
					}
					docName = TemporaryFiles.getInstance().getTemporaryFile(tempPrefix, tempExtension);
				}
				try {
					setOutputStream(new FileOutputStream(docName));
				}catch(IOException accessError) { // Si no se puede generar el archivo, muestro el stackTrace y seteo el stream como NullOutputStream
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

			return true;
		} catch (Exception e) {return false;}
	}

	public boolean GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex){
		return preGxPrintInit(output, gxXPage, gxYPage, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);
	}

	public int getPageLines() {
		if(DEBUG)DEBUG_STREAM.println("getPageLines: --> " + pageLines);
		return pageLines;
	}
	public int getLineHeight() {
		if(DEBUG)DEBUG_STREAM.println("getLineHeight: --> " + this.lineHeight);
		return this.lineHeight;
	}
	public void setPageLines(int P_lines) {
		if(DEBUG)DEBUG_STREAM.println("setPageLines: " + P_lines);
		pageLines = P_lines;
	}
	public void setLineHeight(int lineHeight) {

		if(DEBUG)DEBUG_STREAM.println("setLineHeight: " + lineHeight);
		this.lineHeight = lineHeight;
	}

	protected int M_top ;
	protected int M_bot ;

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

	public abstract void GxEndPage();

	public abstract void GxEndDocument();

	public void GxEndPrinter() {}
	public abstract void GxStartPage();

	public void GxStartDoc() {}
	public void GxSetDocFormat(String format) {}

	public void GxSetDocName(String docName) {
		this.docName = docName.trim();
		if(this.docName.indexOf('.') < 0)
			this.docName += ".pdf";
		if(!new File(docName).isAbsolute()) { // Si el nombre del documento es relativo, veo si hay que agregarle el outputDir
			String outputDir = props.getGeneralProperty(Const.OUTPUT_FILE_DIRECTORY, "").replace(alternateSeparator, File.separatorChar).trim();
			if(!outputDir.equalsIgnoreCase("") && !outputDir.equalsIgnoreCase(".")) {
				if(!outputDir.endsWith(File.separator)) {
					outputDir += File.separator;
				}
				new File(outputDir).mkdirs();
				this.docName = outputDir + this.docName;
			}
			else {
				if (ModelContext.getModelContext() != null) {
					HttpContext webContext = (HttpContext) ModelContext.getModelContext().getHttpContext();
					if ((webContext != null) && (webContext instanceof HttpContextWeb)) {
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

	public boolean GxPrTextInit(String ouput, int nxPage[], int nyPage[], String psIniFile, String psForm, String sPrinter, int nMode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines) {
		return true;
	}

	public boolean GxPrnCfg( String ini )
	{
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

	protected int page;
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

	public void setMetrics(String fontName, boolean bold, boolean italic, int ascent, int descent, int height, int maxAdvance, int[] sizes) {}


	/** Carga la tabla de substitutos
	 */
	protected void loadSubstituteTable() {
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
			for(Enumeration enumera = otherMappings.keys(); enumera.hasMoreElements();) {
				String fontName = (String)enumera.nextElement();
				fontSubstitutes.put(fontName, (String)otherMappings.get(fontName));
				if(tempInverseMappings.containsKey(fontName)) { // Con esto solucionamos el tema de la recursión de Fonts -> Fonts, x ej: Si tenú} Font1-> Font2, y ahora tengo Font2->Font3, pongo cambio el 1º por Font1->Font3
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

	public void showInformation() {}

	public static final double SCALE_FACTOR = 72;
	protected double PPP = 96;
	protected double convertScale(int value) {
		double result = value * SCALE_FACTOR / PPP;
		return result;
	}

	protected double convertScale(double value) {
		double result = value * SCALE_FACTOR / PPP;
		return result;
	}

	protected float reconvertScale(float value) {
		float result = value / (float)(SCALE_FACTOR / PPP);
		return result;
	}

	class FontProps {
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
	protected static String replace(String s,String f,String t) {
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
}
