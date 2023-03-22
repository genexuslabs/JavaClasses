package com.genexus.reports;

import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.platform.INativeFunctions;
import com.genexus.platform.NativeFunctions;
import com.genexus.util.TemporaryFiles;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.reports.fonts.PDFFont;
import com.genexus.reports.fonts.PDFFontDescriptor;
import com.genexus.reports.fonts.Type1FontMetrics;
import com.genexus.reports.fonts.Utilities;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.util.Matrix;

public class PDFReportpdfbox implements IReportHandler{
	private int lineHeight, pageLines;
	private PDRectangle pageSize;
	private PDType1Font font;
	private PDType0Font baseFont;
	//private BarcodeUtil barcode = null; por ahora no soportamos barcode
	private boolean fontUnderline;
	private boolean fontStrikethru;
	private int fontSize;
	private boolean fontBold = false;
	private boolean fontItalic = false;
	private Color backColor, foreColor;
	public static PrintStream DEBUG_STREAM = System.out;
	private OutputStream outputStream = null;
	private static ParseINI props = new ParseINI();
	private ParseINI printerSettings;
	private String form;
	private Vector stringTotalPages;
	private int outputType = -1;
	private int printerOutputMode = -1;
	private boolean modal = false;
	private String docName = "PDFReport.pdf";
	private static INativeFunctions nativeCode = NativeFunctions.getInstance();
	private static Hashtable<String, String> fontSubstitutes = new Hashtable<>();
	private static String configurationFile = null;
	private static String configurationTemplateFile = null;
	private static String defaultRelativePrepend = null;
	private static String defaultRelativePrependINI = null;
	private static String webAppDir = null;
	public static boolean DEBUG = false;
	private PDDocument document;
	private PDDocumentCatalog writer;
	private static String predefinedSearchPath = "";
	private float leftMargin;
	private float topMargin;
	private float bottomMargin;
	private PDPageContentStream template;
	private PDFormXObject formXObjecttemplate;
	private PDType0Font templateFont;
	private int templateFontSize;
	private boolean backFill = true;
	private Color templateColorFill;
	private int pages=0;
	private boolean templateCreated = false;
	public boolean lineCapProjectingSquare = true;
	public boolean barcode128AsImage = true;
	ConcurrentHashMap<String, PDImageXObject> documentImages;
	public int runDirection = 0;
	private HttpContext httpContext = null;
	float[] STYLE_SOLID = new float[]{1,0};//0
	float[] STYLE_NONE = null;
	float[] STYLE_DOTTED,
		STYLE_DASHED,
		STYLE_LONG_DASHED,
		STYLE_LONG_DOT_DASHED;
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

	public void setOutputStream(OutputStream outputStream)
	{
		this.outputStream = outputStream;
	}

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
		String acrobatLocation = props.getGeneralProperty(Const.ACROBAT_LOCATION); // Veo si esta fijada la ubicación del Acrobat en la property
		if(acrobatLocation == null)
		{
			if(NativeFunctions.isUnix())
			{
				throw new Exception("Try setting Acrobat location & executable in property '" + Const.ACROBAT_LOCATION + "' of PDFReport.ini");
			}
		}
		return acrobatLocation;
	}

	public static void printReport(String pdfFilename, boolean silent) throws Exception
	{
		if(NativeFunctions.isWindows())
		{
			pdfFilename = "\"" + new File(pdfFilename).getAbsolutePath() + "\"";
		}

		String [] cmd = {};
		String acrobatLocation = null;
		try
		{
			acrobatLocation = getAcrobatLocation();
		}catch(Exception acrobatNotFound)
		{
			throw new Exception("Acrobat cannot be found in this machine: " + acrobatNotFound.getMessage());
		}
		nativeCode.executeModal(acrobatLocation + " -toPostScript " + pdfFilename, false);
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
	public static void showReport(String filename, boolean modal) throws Exception
	{
		if(NativeFunctions.isWindows())
		{
			filename = "\"" + new File(filename).getAbsolutePath() + "\"";
		}
		String acrobatLocation;
		try
		{
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

	public PDFReportpdfbox(ModelContext context)
	{
		document = null;
		pageSize = null;
		stringTotalPages = new Vector();
		documentImages = new ConcurrentHashMap<>();
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
				configurationFile = defaultRelativePrepend + Const.WEB_INF + File.separatorChar + Const.INI_FILE;
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
				String staticContentBase = httpContext.getStaticContentBase();
				if(staticContentBase != null)
				{
					staticContentBase = staticContentBase.trim();
					if(staticContentBase.indexOf(':') == -1)
					{
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

		loadSubstituteTable();

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

		Utilities.addPredefinedSearchPaths(new String[]{System.getProperty("java.awt.fonts", "c:\\windows\\fonts"),
			System.getProperty("com.ms.windir", "c:\\windows") + "\\fonts"});
	}

	public static final void addPredefinedSearchPaths(String [] predefinedPaths)
	{
		String predefinedPath = "";
		for(int i = 0; i < predefinedPaths.length; i++)
			predefinedPath += predefinedPaths[i] + ";";
		predefinedSearchPath = predefinedPath + predefinedSearchPath;
	}

	public static final String getPredefinedSearchPaths()
	{
		return predefinedSearchPath;
	}

	private void init()
	{
		try {
			document = new PDDocument();
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void GxRVSetLanguage(String lang) {}

	public void GxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength) {}

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
	private void drawRectangle(PDPageContentStream cb, float x, float y, float w, float h,
							   int styleTop, int styleBottom, int styleRight, int styleLeft,
							   float radioTL, float radioTR, float radioBL, float radioBR, float penAux, boolean hideCorners)
	{
		float[] dashPatternTop = getDashedPattern(styleTop);
		float[] dashPatternBottom = getDashedPattern(styleBottom);
		float[] dashPatternLeft = getDashedPattern(styleLeft);
		float[] dashPatternRight = getDashedPattern(styleRight);

		try {
			if (styleBottom!=STYLE_NONE_CONST)
			{
				cb.setLineDashPattern(dashPatternBottom, 0);
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
			if (styleBottom!=STYLE_NONE_CONST)
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
			if (styleRight!=STYLE_NONE_CONST && dashPatternRight!=dashPatternBottom)
			{
				cb.stroke();
				cb.setLineDashPattern(dashPatternRight, 0);
				if (hideCorners && styleBottom==STYLE_NONE_CONST && radioBR==0)
				{
					cb.moveTo(x + w, y + penAux);
				}
				else
				{
					cb.moveTo(x + w, y + radioBR);
				}
			}
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
			if (styleTop!=STYLE_NONE_CONST && dashPatternTop!=dashPatternRight)
			{
				cb.stroke();
				cb.setLineDashPattern(dashPatternTop, 0);
				if (hideCorners && styleRight==STYLE_NONE_CONST && radioTR==0)
				{
					cb.moveTo(x + w - penAux, y + h);
				}
				else
				{
					cb.moveTo(x + w - radioTR, y + h);
				}
			}
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
			if (styleLeft!=STYLE_NONE_CONST  && dashPatternLeft!=dashPatternTop)
			{
				cb.stroke();
				cb.setLineDashPattern(dashPatternLeft, 0);
				if (hideCorners && styleTop==STYLE_NONE_CONST && radioTL==0)
				{
					cb.moveTo(x, y + h - penAux);
				}
				else
				{
					cb.moveTo(x, y + h - radioTL);
				}
			}
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
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}
	}

	private void roundRectangle(PDPageContentStream cb, float x, float y, float w, float h,
								float radioTL, float radioTR, float radioBL, float radioBR)
	{
		try {
			float b = 0.4477f;
			if (radioBL>0)
			{
				cb.moveTo(x + radioBL, y);
			}
			else
			{
				cb.moveTo(x, y);
			}
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
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
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
		try (PDPageContentStream cb = new PDPageContentStream(document, document.getPage(page - 1))){

			float penAux = (float)convertScale(pen);
			float rightAux = (float)convertScale(right);
			float bottomAux = (float)convertScale(bottom);
			float leftAux = (float)convertScale(left);
			float topAux = (float)convertScale(top);

			cb.saveGraphicsState();

			float x1, y1, x2, y2;
			x1 = leftAux + leftMargin;
			y1 = pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin;
			x2 = rightAux + leftMargin;
			y2 = pageSize.getUpperRightY() - topAux - topMargin -bottomMargin;

			cb.setLineWidth(penAux);
			cb.setLineCapStyle(2);

			if (cornerRadioBL==0 && cornerRadioBR==0 && cornerRadioTL==0 && cornerRadioTR==0 && styleBottom==0 && styleLeft==0 && styleRight==0 && styleTop==0)
			{
				if (pen > 0)
					cb.setStrokingColor(foreRed, foreGreen, foreBlue);
				else
					cb.setStrokingColor (backRed, backGreen, backBlue);

				cb.addRect(x1, y1, x2 - x1, y2 - y1);

				if (backMode!=0)
				{
					cb.setNonStrokingColor(new Color(backRed, backGreen, backBlue));
					cb.fillAndStroke();
				}
				cb.closePath();
				cb.stroke();
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

				int max = (int)Math.min(w, h);
				cRadioTL = Math.max(0, Math.min(cRadioTL, max/2));
				cRadioTR = Math.max(0, Math.min(cRadioTR, max/2));
				cRadioBL = Math.max(0, Math.min(cRadioBL, max/2));
				cRadioBR = Math.max(0, Math.min(cRadioBR, max/2));

				if (backMode!=0)
				{
					cb.setStrokingColor(backRed, backGreen, backBlue);
					cb.setLineWidth(0);
					roundRectangle(cb, x1, y1, w, h,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR);
					cb.setNonStrokingColor(new Color(backRed, backGreen, backBlue));
					cb.fillAndStroke();
					cb.setLineWidth(penAux);
				}
				if (pen > 0)
				{
					cb.setStrokingColor(foreRed, foreGreen, foreBlue);
					drawRectangle(cb, x1, y1, w, h,
						styleTop, styleBottom, styleRight, styleLeft,
						cRadioTL, cRadioTR,
						cRadioBL, cRadioBR, penAux, false);
				}
			}
			cb.restoreGraphicsState();

			if(DEBUG)DEBUG_STREAM.println("GxDrawRect -> (" + left + "," + top + ") - (" + right + "," + bottom + ")  BackMode: " + backMode + " Pen:" + pen);
		} catch (Exception e) {

		}
	}

	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue)
	{
		GxDrawLine(left, top, right, bottom, width, foreRed, foreGreen, foreBlue, 0);
	}

	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style)
	{
		try (PDPageContentStream cb = new PDPageContentStream(document, document.getPage(page - 1))){

			float widthAux = (float)convertScale(width);
			float rightAux = (float)convertScale(right);
			float bottomAux = (float)convertScale(bottom);
			float leftAux = (float)convertScale(left);
			float topAux = (float)convertScale(top);

			if(DEBUG)DEBUG_STREAM.println("GxDrawLine -> (" + left + "," + top + ") - (" + right + "," + bottom + ") Width: " + width);

			float x1, y1, x2, y2;

			x1 = leftAux + leftMargin;
			y1 = pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin;
			x2 = rightAux + leftMargin;
			y2 = pageSize.getUpperRightY() - topAux - topMargin -bottomMargin;

			cb.saveGraphicsState();
			cb.setStrokingColor(foreRed, foreGreen, foreBlue);
			cb.setLineWidth(widthAux);

			if (lineCapProjectingSquare)
			{
				cb.setLineCapStyle(2);
			}
			if (style!=0)
			{
				float[] dashPattern = getDashedPattern(style);
				cb.setLineDashPattern(dashPattern, 0);
			}
			cb.moveTo(x1, y1);
			cb.lineTo(x2, y2);
			cb.stroke();

			cb.restoreGraphicsState();
		} catch (Exception e){

		}
	}

	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom)
	{
		GxDrawBitMap(bitmap, left, top, right, bottom, 0);
	}

	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio)
	{
		try (PDPageContentStream cb = new PDPageContentStream(document, document.getPage(page - 1))){
			PDImageXObject image;
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

					if (!new File(bitmap).isAbsolute() && !bitmap.toLowerCase().startsWith("http:") && !bitmap.toLowerCase().startsWith("https:"))
					{
						if (bitmap.startsWith(httpContext.getStaticContentBase()))
						{
							bitmap = bitmap.replace(httpContext.getStaticContentBase(), "");
						}
						image = PDImageXObject.createFromFile(defaultRelativePrepend + bitmap,document);
						if(image == null)
						{
							bitmap = webAppDir + bitmap;
							image = PDImageXObject.createFromFile(bitmap,document);
						}
						else
						{
							bitmap = defaultRelativePrepend + bitmap;
						}
					}
					else
					{
						image = PDImageXObject.createFromFile(bitmap,document);
					}
				}
			}
			catch(java.lang.IllegalArgumentException ex)
			{
				URL url= new java.net.URL(bitmap);
				image = PDImageXObject.createFromFile(url.toString(),document);
			}

			if (documentImages == null)
			{
				documentImages = new ConcurrentHashMap<>();
			}
			documentImages.putIfAbsent(bitmap, image);


			if(DEBUG)DEBUG_STREAM.println("GxDrawBitMap -> '" + bitmap + "' [" + left + "," + top + "] - Size: (" + (right - left) + "," + (bottom - top) + ")");

			if(image != null)
			{
				float rightAux = (float)convertScale(right);
				float bottomAux = (float)convertScale(bottom);
				float leftAux = (float)convertScale(left);
				float topAux = (float)convertScale(top);

				float x = leftAux + leftMargin;
				float y = this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin;

				if (aspectRatio == 0)
					cb.drawImage(image, x, y, rightAux - leftAux, bottomAux - topAux);
				else
					cb.drawImage(image, x, y, (rightAux - leftAux) * aspectRatio, (bottomAux - topAux) * aspectRatio);
				cb.close();
			}
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
			fontName = getSubstitute(fontName);
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
			// Por ahora no soportamos barcode
		}
		else
		{
			// Por ahora no soportamos barcode
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
						COSDictionary dict = new COSDictionary();
						dict.setItem(COSName.TYPE, COSName.FONT);
						dict.setItem(COSName.SUBTYPE, COSName.TYPE0);
						dict.setItem(COSName.BASE_FONT, COSName.getPDFName(fontName));
						dict.setItem(COSName.ENCODING, COSName.WIN_ANSI_ENCODING);
						dict.setItem(COSName.DESCENDANT_FONTS, new COSArray());
						dict.setBoolean(COSName.getPDFName("Embedded"), false);
						baseFont = new PDType0Font(dict);
					}

					switch (fontName.trim().toUpperCase()) {
						case "COURIER":
							font = PDType1Font.COURIER;
							break;
						case "COURIER_BOLD":
							font = PDType1Font.COURIER_BOLD;
							break;
						case "COURIER_BOLD_OBLIQUE":
							font = PDType1Font.COURIER_BOLD_OBLIQUE;
							break;
						case "COURIER_OBLIQUE":
							font = PDType1Font.COURIER_OBLIQUE;
							break;
						case "HELVETICA":
							font = PDType1Font.HELVETICA;
							break;
						case "HELVETICA_BOLD":
							font = PDType1Font.HELVETICA_BOLD;
							break;
						case "HELVETICA_BOLD_OBLIQUE":
							font = PDType1Font.HELVETICA_BOLD_OBLIQUE;
							break;
						case "HELVETICA_OBLIQUE":
							font = PDType1Font.HELVETICA_OBLIQUE;
							break;
						case "SYMBOL":
							font = PDType1Font.SYMBOL;
							break;
						case "TIMES_BOLD":
							font = PDType1Font.TIMES_BOLD;
							break;
						case "TIMES_BOLD_ITALIC":
							font = PDType1Font.TIMES_BOLD_ITALIC;
							break;
						case "TIMES_ITALIC":
							font = PDType1Font.TIMES_ITALIC;
							break;
						case "TIMES_ROMAN":
							font = PDType1Font.TIMES_ROMAN;
							break;
						case "ZAPF_DINGBATS":
							font = PDType1Font.ZAPF_DINGBATS;
							break;
						default:
							font = PDType1Font.HELVETICA;
							break;
					}
				}
			}
			else
			{
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
					fontPath = PDFFontDescriptor.getTrueTypeFontLocation(fontName, props);
					if (fontPath.equals(""))
					{
						COSDictionary dict = new COSDictionary();
						dict.setItem(COSName.TYPE, COSName.FONT);
						dict.setItem(COSName.SUBTYPE, COSName.TYPE0);
						dict.setItem(COSName.BASE_FONT, COSName.HELV);
						dict.setItem(COSName.ENCODING, COSName.WIN_ANSI_ENCODING);
						dict.setItem(COSName.DESCENDANT_FONTS, new COSArray());
						dict.setItem(COSName.FONT_FILE2, COSNull.NULL);
						baseFont = new PDType0Font(dict);
						foundFont = false;
					}
				}
				if (foundFont)
				{
					if (isEmbeddedFont(fontName))
					{
						baseFont = PDType0Font.load(document,new File(fontPath));
					}
					else
					{
						baseFont = PDType0Font.load(document,new File(fontPath + style));
						COSDictionary dict = baseFont.getCOSObject();
						dict.setItem(COSName.ENCODING, COSName.IDENTITY_H);
						dict.setItem(COSName.FONT_FILE2, COSNull.NULL);
					}
				}
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
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
			COSDictionary fontDict = new COSDictionary();
			fontDict.setName(COSName.TYPE, "Font");
			fontDict.setName(COSName.SUBTYPE, "Type0");
			fontDict.setName(COSName.STYLE, style);
			COSArray differencesArray = new COSArray();
			COSDictionary encodingDict = new COSDictionary();
			encodingDict.setName(COSName.TYPE, "Encoding");
			encodingDict.setName(COSName.BASE_ENCODING, "Identity-H");

			if (style.equals(""))
			{
				if (fontName.equals("Japanese")){
					fontDict.setName(COSName.BASE_FONT, "HeiseiMin-W3");
					differencesArray.add(COSName.getPDFName("UniJIS-UCS2-HW-H"));
				}
				if (fontName.equals("Japanese2")){
					fontDict.setName(COSName.BASE_FONT, "HeiseiKakuGo-W5");
					differencesArray.add(COSName.getPDFName("UniJIS-UCS2-HW-H"));
				}
				if (fontName.equals("SimplifiedChinese")){
					fontDict.setName(COSName.BASE_FONT, "STSong-Light");
					differencesArray.add(COSName.getPDFName("UniGB-UCS2-H"));
				}
				if (fontName.equals("TraditionalChinese")){
					fontDict.setName(COSName.BASE_FONT, "MHei-Medium");
					differencesArray.add(COSName.getPDFName("UniGB-UCS2-H"));
				}
				if (fontName.equals("Korean")) {
					fontDict.setName(COSName.BASE_FONT, "HYSMyeongJo-Medium");
					differencesArray.add(COSName.getPDFName("UniKS-UCS2-H"));
				}
				fontDict.setItem(COSName.ENCODING, encodingDict);
				fontDict.setItem(COSName.DESC, null);
				baseFont = new PDType0Font(fontDict);
			}
			else
			{
				if (fontName.equals("Japanese")){
					fontDict.setName(COSName.BASE_FONT, "HeiseiMin-W3");
					differencesArray.add(COSName.getPDFName("UniJIS-UCS2-HW-H"));
				}
				if (fontName.equals("Japanese2")){
					fontDict.setName(COSName.BASE_FONT, "HeiseiKakuGo-W5");
					differencesArray.add(COSName.getPDFName("UniJIS-UCS2-HW-H"));
				}
				if (fontName.equals("SimplifiedChinese")){
					fontDict.setName(COSName.BASE_FONT, "STSong-Light");
					differencesArray.add(COSName.getPDFName("UniGB-UCS2-H"));
				}
				if (fontName.equals("TraditionalChinese")){
					fontDict.setName(COSName.BASE_FONT, "MHei-Medium");
					differencesArray.add(COSName.getPDFName("UniGB-UCS2-H"));
				}
				if (fontName.equals("Korean")) {
					fontDict.setName(COSName.BASE_FONT, "HYSMyeongJo-Medium");
					differencesArray.add(COSName.getPDFName("UniKS-UCS2-H"));
				}
				fontDict.setItem(COSName.ENCODING, encodingDict);
				fontDict.setItem(COSName.DESC, null);
				baseFont = new PDType0Font(fontDict);
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
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
		try (PDPageContentStream cb =  new PDPageContentStream(document, document.getPage(page - 1))){
			boolean printRectangle = false;
			if (props.getBooleanGeneralProperty(Const.BACK_FILL_IN_CONTROLS, true))
				printRectangle = true;

			if (printRectangle && (border == 1 || backFill))
			{
				GxDrawRect(left, top, right, bottom, border, foreColor.getRed(), foreColor.getGreen(), foreColor.getBlue(), backFill ? 1 : 0, backColor.getRed(), backColor.getGreen(), backColor.getBlue(), 0, 0);
			}

			sTxt = CommonUtil.rtrim(sTxt);

			COSDictionary fontDict = baseFont.getCOSObject();
			COSDictionary newFontDict = new COSDictionary(fontDict);
			newFontDict.setFloat(COSName.SIZE, fontSize);
			PDType0Font font = new PDType0Font(newFontDict);

			cb.setFont(font,fontSize);
			cb.setNonStrokingColor(foreColor);
			int arabicOptions = 0;
			float captionHeight = font.getFontDescriptor().getCapHeight();
			float rectangleWidth = font.getStringWidth(sTxt);
			float lineHeight = font.getFontDescriptor().getFontBoundingBox().getUpperRightY() - font.getFontDescriptor().getFontBoundingBox().getLowerLeftX();
			float textBlockHeight = (float)convertScale(bottom-top);
			int linesCount =   (int)(textBlockHeight/lineHeight);
			int bottomOri = bottom;
			int topOri = top;

			if (linesCount >= 2 && !((align & 16) == 16) && htmlformat != 1)
			{
				if (valign == PDFReportpdfbox.VerticalAlign.TOP.value())
					bottom = top + (int)reconvertScale(lineHeight);
				else if (valign == PDFReportpdfbox.VerticalAlign.BOTTOM.value())
					top = bottom - (int)reconvertScale(lineHeight);
			}

			float bottomAux = (float)convertScale(bottom) - ((float)convertScale(bottom-top) - captionHeight)/2;
			float topAux = (float)convertScale(top) + ((float)convertScale(bottom-top) - captionHeight)/2;


			float startHeight = bottomAux - topAux - captionHeight;

			float leftAux = (float)convertScale(left);
			float rightAux = (float)convertScale(right);
			int alignment = align & 3;
			boolean autoResize = (align & 256) == 256;

			if (htmlformat == 1)
			{
				//Por ahora no soportamos la impresion de HTML
			}
			else
			if (1 == 2)//(barcode!=null)
			{
				//Por ahora no soportamos la impresion de barcodes
			}
			else
			{

				if(backFill)
				{
					PDRectangle rectangle = new PDRectangle(0,0);
					switch(alignment)
					{
						case 1: // Center Alignment
							rectangle = new PDRectangle((leftAux + rightAux)/2 + leftMargin - rectangleWidth/2, (float)this.pageSize.getUpperRightY() -  bottomAux - topMargin -bottomMargin , (leftAux + rightAux)/2 + leftMargin + rectangleWidth/2, (float)this.pageSize.getUpperRightY() - topAux - topMargin -bottomMargin);
							break;
						case 2: // Right Alignment
							rectangle = new PDRectangle(rightAux + leftMargin - rectangleWidth, (float)this.pageSize.getUpperRightY() -  bottomAux - topMargin -bottomMargin , rightAux + leftMargin, (float)this.pageSize.getUpperRightY() - topAux - topMargin -bottomMargin);
							break;
						case 0: // Left Alignment
							rectangle = new PDRectangle(leftAux + leftMargin, (float)this.pageSize.getUpperRightY() -  bottomAux - topMargin -bottomMargin , leftAux + leftMargin + rectangleWidth, (float)this.pageSize.getUpperRightY() - topAux - topMargin -bottomMargin);
							break;
					}
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1));
					contentStream.setNonStrokingColor(backColor);
					contentStream.addRect(rectangle.getLowerLeftX(), rectangle.getLowerLeftY(),rectangle.getWidth(), rectangle.getHeight());
					contentStream.fill();
					contentStream.close();
					try
					{
						document.getPage(page - 1).setMediaBox(rectangle);
						contentStream.close();
					}
					catch(Exception e)
					{
						System.err.println(e.getMessage());
					}
				}

				float underlineSeparation = lineHeight / 5;
				int underlineHeight = (int)underlineSeparation + (int)(underlineSeparation/4);
				PDRectangle underline;

				if (fontUnderline)
				{
					underline = new PDRectangle(0,0);

					switch(alignment)
					{
						case 1: // Center Alignment
							underline = new PDRectangle(
								(leftAux + rightAux)/2 + leftMargin - rectangleWidth/2,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation,
								(leftAux + rightAux)/2 + leftMargin + rectangleWidth/2,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight);
							break;
						case 2: // Right Alignment
							underline = new PDRectangle( rightAux + leftMargin - rectangleWidth ,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation,
								rightAux + leftMargin,
								this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);
							break;
						case 0: // Left Alignment
							underline = new PDRectangle( leftAux + leftMargin ,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation,
								leftAux + leftMargin + rectangleWidth,
								this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight);
							break;
					}
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1));
					contentStream.setNonStrokingColor(foreColor); // set background color to yellow
					contentStream.addRect(underline.getLowerLeftX(), underline.getLowerLeftY(),underline.getWidth(), underline.getHeight());
					contentStream.fill();
					contentStream.close();
					try
					{
						document.getPage(page - 1).setMediaBox(underline);
						contentStream.close();
					}
					catch(Exception e)
					{
						System.err.println(e.getMessage());
					}
				}

				if (fontStrikethru)
				{
					underline = new PDRectangle(0,0);
					float strikethruSeparation = lineHeight / 2;

					switch(alignment)
					{
						case 1: // Center Alignment
							underline = new PDRectangle(
								(leftAux + rightAux)/2 + leftMargin - rectangleWidth/2,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation,
								(leftAux + rightAux)/2 + leftMargin + rectangleWidth/2,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);
							break;
						case 2: // Right Alignment
							underline = new PDRectangle( rightAux + leftMargin - rectangleWidth ,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation,
								rightAux + leftMargin,
								this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);
							break;
						case 0: // Left Alignment
							underline = new PDRectangle( leftAux + leftMargin ,
								this.pageSize.getUpperRightY() - bottomAux - topMargin -bottomMargin + startHeight - underlineSeparation + strikethruSeparation,
								leftAux + leftMargin + rectangleWidth,
								this.pageSize.getUpperRightY() - bottomAux  - topMargin -bottomMargin + startHeight - underlineHeight + strikethruSeparation);
							break;
					}
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1));
					contentStream.setNonStrokingColor(foreColor);
					contentStream.addRect(underline.getLowerLeftX(), underline.getLowerLeftY(),underline.getWidth(), underline.getHeight());
					contentStream.fill();
					contentStream.close();
					try
					{
						document.getPage(page - 1).setMediaBox(underline);
						contentStream.close();
					}
					catch(Exception e)
					{
						System.err.println(e.getMessage());
					}
				}

				if(sTxt.trim().equalsIgnoreCase("{{Pages}}"))
				{
					if (!templateCreated)
					{
						formXObjecttemplate = new PDFormXObject(document);
						template = new PDPageContentStream(document, formXObjecttemplate, outputStream);
						formXObjecttemplate.setResources(new PDResources());
						formXObjecttemplate.setBBox(new PDRectangle(right - left, bottom - top));
						templateCreated = true;
					}
					PDFormXObject form = new PDFormXObject(document);
					PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1));
					contentStream.transform(Matrix.getTranslateInstance(leftAux + leftMargin, leftAux + leftMargin));
					contentStream.drawForm(form);
					contentStream.close();
					templateFont = baseFont;
					templateFontSize = fontSize;
					templateColorFill = foreColor;
					return;
				}

				float textBlockWidth = rightAux - leftAux;
				float TxtWidth = baseFont.getStringWidth(sTxt)/ 1000 * fontSize;
				boolean justified = (alignment == 3) && textBlockWidth < TxtWidth;
				boolean wrap = ((align & 16) == 16);

				if (wrap || justified)
				{
					bottomAux = (float)convertScale(bottomOri);
					topAux = (float)convertScale(topOri);

					float leading = (float)(Double.valueOf(props.getGeneralProperty(Const.LEADING)).doubleValue());
					PDAnnotationText annotation = new PDAnnotationText();
					String alignmentString;
					switch(alignment) {
						case 1:
							alignmentString = "center";
							break;
						case 2:
							alignmentString = "right";
							break;
						default:
							alignmentString = "left";
					}
					annotation.setDefaultAppearance("/" + font.getName() + " " + fontSize + " Tf " + leading + " TL 0 g " + alignmentString + " <</BMC [0 0]>>BDC q BT /F1 " + fontSize + " Tf " + leading + " TL Tj ET Q EMC");
					annotation.setContents(sTxt);
					annotation.setRectangle(new PDRectangle(leftAux + leftMargin, this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin,
						rightAux + leftMargin, this.pageSize.getUpperRightY() - topAux - topMargin - bottomMargin));
					try{
						document.getPage(page - 1).getAnnotations().add(annotation);
						PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1));
						contentStream.beginText();
						contentStream.showText(annotation.getContents());
						contentStream.endText();
						contentStream.close();
					}
					catch (Exception e)
					{
						e.printStackTrace(System.err);
					}
				}
				else
				{
					startHeight=0;
					if (!autoResize)
					{
						String newsTxt = sTxt;
						while(TxtWidth > textBlockWidth && (newsTxt.length()-1>=0))
						{
							sTxt = newsTxt;
							newsTxt = newsTxt.substring(0, newsTxt.length()-1);
							TxtWidth = baseFont.getStringWidth(newsTxt) / 1000 * fontSize;
						}
					}

					try (PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(page - 1))){

						switch(alignment)
						{
							case 1: // Center Alignment
								float x = ((leftAux + rightAux) / 2) + leftMargin;
								float y = this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin + startHeight;
								contentStream.setHorizontalScaling(1f);
								contentStream.setFont(baseFont, fontSize);
								contentStream.beginText();
								contentStream.newLineAtOffset(x, y);
								contentStream.showText(sTxt);
								contentStream.endText();
								contentStream.close();
								break;
							case 2: // Right Alignment
								x = rightAux + leftMargin;
								y = this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin + startHeight;
								contentStream.setHorizontalScaling(1f);
								contentStream.setFont(baseFont, fontSize);
								contentStream.beginText();
								contentStream.newLineAtOffset(x, y);
								contentStream.showText(sTxt);
								contentStream.endText();
								contentStream.close();
								break;
							case 0: // Left Alignment
							case 3: // Justified, only one text line
								x = (leftAux + rightAux) / 2 + leftMargin;
								y = this.pageSize.getUpperRightY() - bottomAux - topMargin - bottomMargin + startHeight;
								contentStream.setHorizontalScaling(1f);
								contentStream.setFont(baseFont, fontSize);
								contentStream.beginText();
								contentStream.newLineAtOffset(x, y);
								contentStream.showText(sTxt);
								contentStream.endText();
								break;
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}
		} catch (Exception e){
			System.err.println(e.getMessage());
		}
	}
	boolean pageHeightExceeded(float bottomAux, float drawingPageHeight){
		return bottomAux > drawingPageHeight;
	}

	public void GxClearAttris() {}

	public static final double PAGE_SCALE_Y = 20;
	public static final double PAGE_SCALE_X = 20;
	public static final double GX_PAGE_SCALE_Y_OLD = 15.45;
	public static final double GX_PAGE_SCALE_Y = 14.4;
	private static double TO_CM_SCALE =28.6;
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
			{
				accessError.printStackTrace(System.err);
				outputStream = new com.genexus.util.NullOutputStream();
				outputType = Const.OUTPUT_FILE;
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

		this.pageSize = computePageSize(leftMargin, topMargin, pageWidth, pageLength, props.getBooleanGeneralProperty(Const.MARGINS_INSIDE_BORDER, false));
		gxXPage[0] = (int)this.pageSize.getUpperRightX ();
		if (props.getBooleanGeneralProperty(Const.FIX_SAC24437, true))
			gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y);
		else
			gxYPage[0] = (int)(pageLength / GX_PAGE_SCALE_Y_OLD);

		init();

		return true;
	}

	private PDRectangle computePageSize(float leftMargin, float topMargin, int width, int length, boolean marginsInsideBorder)
	{
		if ((leftMargin == 0 && topMargin == 0)||marginsInsideBorder)
		{
			if (length == 23818 && width == 16834)
				return PDRectangle.A3;
			else if (length == 16834 && width == 11909)
				return PDRectangle.A4;
			else if (length == 11909 && width == 8395)
				return PDRectangle.A5;
			else if (length == 20016 && width == 5731)
				return new PDRectangle(250f, 353f);
			else if (length == 14170 && width == 9979)
				return new PDRectangle(176f, 250f);
			else if (length == 15120 && width == 10440)
				return new PDRectangle(184.15f, 266.7f);
			else if (length == 20160 && width == 12240)
				return PDRectangle.LEGAL;
			else if (length == 15840 && width == 12240)
				return PDRectangle.LETTER;
			else
				return new PDRectangle((int)(width / PAGE_SCALE_X) , (int)(length / PAGE_SCALE_Y) );
		}
		return new PDRectangle((int)(width / PAGE_SCALE_X) + leftMargin, (int)(length / PAGE_SCALE_Y) + topMargin);
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

	public void GxEndPage() {}

	public void GxEndDocument()
	{
		if(document.getNumberOfPages() == 0)
		{
			document.addPage(new PDPage(this.pageSize));
			pages++;
		}
		if (template != null)
		{
			try{
				template.beginText();
				template.setFont(baseFont, fontSize);
				template.setTextMatrix(new Matrix());
				template.setNonStrokingColor(templateColorFill);
				template.showText(String.valueOf(pages));
				template.endText();
				template.close();
				for (PDPage page : document.getPages()){
					try (PDPageContentStream templatePainter = new PDPageContentStream(document, page)) {
						templatePainter.drawForm(formXObjecttemplate);
					}

				}
				template.close();
			} catch (IOException e){ System.err.println(e.getMessage()); }
		}
		int copies = 1;
		try
		{
			copies = Integer.parseInt(printerSettings.getProperty(form, Const.COPIES));
			if(DEBUG)DEBUG_STREAM.println("Setting number of copies to " + copies);

			writer = document.getDocumentCatalog();

			COSDictionary dict = new COSDictionary();
			if (writer.getViewerPreferences() != null && writer.getViewerPreferences().getCOSObject() != null)
				dict = writer.getViewerPreferences().getCOSObject();
			PDViewerPreferences viewerPreferences = new PDViewerPreferences(dict);
			viewerPreferences.setPrintScaling(PDViewerPreferences.PRINT_SCALING.None);
			dict.setInt("NumCopies", copies);
			writer.setViewerPreferences(viewerPreferences);

			int duplex= Integer.parseInt(printerSettings.getProperty(form, Const.DUPLEX));
			COSName duplexValue;
			switch (duplex){
				case 1: duplexValue = COSName.HELV; break;
				case 2: duplexValue = COSName.DUPLEX; break;
				case 3: duplexValue = COSName.DUPLEX;break;
				case 4: duplexValue = COSName.DUPLEX;break;
				default: duplexValue = COSName.NONE;
			}
			if(DEBUG)DEBUG_STREAM.println("Setting duplex to " + duplexValue);
			writer = document.getDocumentCatalog();
			dict = writer.getViewerPreferences().getCOSObject();
			if (dict == null) {dict = new COSDictionary();}
			viewerPreferences = new PDViewerPreferences(dict);
			viewerPreferences.setPrintScaling(PDViewerPreferences.PRINT_SCALING.None);
			dict.setName(COSName.DUPLEX, duplexValue.toString());
			writer.setViewerPreferences(viewerPreferences);
		}
		catch(Exception ex)
		{
			ex.printStackTrace(System.err);
		}

		String serverPrinting = props.getGeneralProperty(Const.SERVER_PRINTING);
		boolean fit= props.getGeneralProperty(Const.ADJUST_TO_PAPER).equals("true");
		if ((outputType==Const.OUTPUT_PRINTER || outputType==Const.OUTPUT_STREAM_PRINTER) && (httpContext instanceof HttpContextWeb && serverPrinting.equals("false")))
		{
			PDDocumentCatalog catalog = document.getDocumentCatalog();
			StringBuffer jsActions = new StringBuffer();
			jsActions.append("var pp = this.getPrintParams();\n");
			String printerAux=printerSettings.getProperty(form, Const.PRINTER);
			String printer = replace(printerAux, "\\", "\\\\");

			if (printer!=null && !printer.equals(""))
			{
				jsActions.append("pp.printerName = \"" + printer + "\";\n");
			}

			if (fit)
			{
				jsActions.append("pp.pageHandling = pp.constants.handling.fit;\n");
			}
			else
			{
				jsActions.append("pp.pageHandling = pp.constants.handling.none;\n");
			}

			if (printerSettings.getProperty(form, Const.MODE, "3").startsWith("0"))
			{
				jsActions.append("pp.interactive = pp.constants.interactionLevel.automatic;\n");
				for(int i = 0; i < copies; i++)
				{
					jsActions.append("this.print(pp);\n");
				}
			}
			else
			{
				jsActions.append("pp.interactive = pp.constants.interactionLevel.full;\n");
				jsActions.append("this.print(pp);\n");
			}
			PDActionJavaScript openActions = new PDActionJavaScript(jsActions.toString());
			catalog.setOpenAction(openActions);
		}
		try {
			String pdfFilename = new File(docName).getAbsolutePath();
			document.save(pdfFilename);
			document.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		if(DEBUG)DEBUG_STREAM.println("GxEndDocument!");

		try{ props.save(); } catch(IOException e) { ; }

		switch(outputType)
		{
			case Const.OUTPUT_SCREEN:
				try{ outputStream.close(); } catch(IOException e) { }
				try{ showReport(docName, modal); }
				catch(Exception e) {
					e.printStackTrace();
				}
				break;
			case Const.OUTPUT_PRINTER:
				try{ outputStream.close(); } catch(IOException e) { }
				try{
					if (!(httpContext instanceof HttpContextWeb) || !serverPrinting.equals("false"))
					{
						printReport(docName, this.printerOutputMode == 1);
					}
				} catch(Exception e){
					e.printStackTrace();
				}
				break;
			case Const.OUTPUT_FILE:
				try{ outputStream.close(); } catch(IOException e) { ; }
				break;
			case Const.OUTPUT_STREAM:
			case Const.OUTPUT_STREAM_PRINTER:
			default: break;
		}
		outputStream = null;
	}

	public void GxEndPrinter() {}
	public void GxStartPage()
	{
		document.addPage(new PDPage());
		pages++;
	}

	public void GxStartDoc() {}
	public void GxSetDocFormat(String format) {}

	public void GxSetDocName(String docName)
	{
		this.docName = docName.trim();
		if(this.docName.indexOf('.') < 0)
			this.docName += ".pdf";
		if(!new File(docName).isAbsolute())
		{
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

	public void setMetrics(String fontName, boolean bold, boolean italic, int ascent, int descent, int height, int maxAdvance, int[] sizes) {}

	/** Carga la tabla de substitutos
	 */
	private void loadSubstituteTable()
	{
		Hashtable<String, Vector<String>> tempInverseMappings = new Hashtable<>();

		for(int i = 0; i < Const.FONT_SUBSTITUTES_TTF_TYPE1.length; i++)
			fontSubstitutes.put(Const.FONT_SUBSTITUTES_TTF_TYPE1[i][0], Const.FONT_SUBSTITUTES_TTF_TYPE1[i][1]);

		Hashtable otherMappings = props.getSection(Const.FONT_SUBSTITUTES_SECTION);
		if(otherMappings != null)
			for(Enumeration enumera = otherMappings.keys(); enumera.hasMoreElements();)
			{
				String fontName = (String)enumera.nextElement();
				fontSubstitutes.put(fontName, (String)otherMappings.get(fontName));
				if(tempInverseMappings.containsKey(fontName))
				{
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

		if(p<s.length())
			b.append(s.substring(p));

		return b.toString();
	}

}