package com.genexus.reports;

public class Const
{

    public static final String ACROBAT_LOCATION = "Acrobat Location"; // Indica la ubicaci�n del Acrobat
    public static final String DEFAULT_ACROBAT_LOCATION = "Applications\\Acrobat.exe\\shell\\open\\command"; // Ubicaci�n del Acrobat por defecto en el registry 
    public static final String DEFAULT_ACROREAD_LOCATION = "Applications\\AcroRd32.exe\\shell\\open\\command"; // Ubicaci�n del Acrobat Reader por defecto en el registry 
    public static final String DEFAULT_ACROBAT_EX_LOCATION = "SOFTWARE\\Classes\\AcroExch.Document\\shell\\open\\command"; // Este es otro lugar donde puede estar el acrobat (sobre HKEY_LOCAL_MACHINE)
    public static final String DEFAULT_ACROBAT_EX_LOCATION2 = "AcroExch.Document\\shell\\open\\command"; // Este es otro lugar donde puede estar el acrobat (sobre HKEY_LOCAL_MACHINE)
    	
    public static final String INI_FILE = "PDFReport.ini";
    public static final String INI_TEMPLATE_FILE = "PDFReport.template";
    public static final String WEB_INF = "WEB-INF";
    public static final String EMBEED_DEFAULT = "false"; // Valor por defecto sobre si embeber o no los fonts
    public static final String SEARCH_FONTS_ALWAYS = "SearchNewFonts";   // Indica si siempre se va a buscar por nuevos fonts
    public static final String SEARCH_FONTS_ONCE = "SearchNewFontsOnce"; // Indica si se va abuscar por nuevos fonts por unica vez
    public static final String LEFT_MARGIN = "LeftMargin"; 
    public static final String TOP_MARGIN = "TopMargin";
	public static final String BOTTOM_MARGIN = "BottomMargin";
    public static final String DEFAULT_LEFT_MARGIN = "0.75"; // 0.75 Cent�metros de DefaultMargin
    public static final String DEFAULT_TOP_MARGIN = "0.75";  // 0.75 Cent�metros de DefaultMargin
	public static final String DEFAULT_BOTTOM_MARGIN = "6";
	public static final String DEFAULT_MARGINS_INSIDE_BORDER = "false"; //Por defecto los margenes no se suman al pageSize especificado en el reporte.
	public static final String DEFAULT_LINE_CAP_PROJECTING_SQUARE = "true";
	public static final String DEFAULT_BARCODE128_AS_IMAGE = "true";
    public static final String PDF_REPORT_INI_VERSION_ENTRY = "Version";
    public static final String PDF_REPORT_INI_VERSION = "1.0.0.0";
	public static final String MARGINS_INSIDE_BORDER = "MarginsInsideBorder"; // Indica si el valor de TopMargin y LeftMargin se suman al PageSize o no.
    public static final String FONTS_LOCATION = "FontsLocation"; // Indica la ubicacion de los fonts (separados por ;)
	public static final String OUTPUT_FILE_DIRECTORY = "OutputFileDirectory"; // Indica el directorio de destino de los PDFs cuando el docName es relativo
	public static final String SERVER_PRINTING = "ServerPrinting"; // Indica el directorio de destino de los PDFs cuando el docName es relativo
	public static final String ADJUST_TO_PAPER = "AdjustToPaper"; // Indica si se hace fit to page
	public static final String LINE_CAP_PROJECTING_SQUARE = "LineCapProjectingSquare"; 
	public static final String BARCODE128_AS_IMAGE = "Barcode128AsImage";
	public static final String LEADING = "Leading";
	
	//Printer settings
	public static final String PRINTER = "Printer"; 
    public static final String MODE = "Mode"; 
    public static final String ORIENTATION = "Orientation"; 
    public static final String PAPERSIZE = "PaperSize"; 
    public static final String PAPERLENGTH = "PaperLength"; 
    public static final String PAPERWIDTH = "PaperWidth"; 
    public static final String SCALE = "Scale"; 
    public static final String COPIES = "Copies"; 
    public static final String DEFAULTSOURCE = "DefaultSource"; 
    public static final String PRINTQUALITY = "PrintQuality"; 
    public static final String COLOR = "Color"; 
    public static final String DUPLEX = "Duplex";

    // Secciones
    public static final String EMBEED_SECTION = "Embeed Fonts";
	public static final String EMBEED_NOT_SPECIFIED_SECTION= "EmbeedNotSpecifiedFonts";
    public static final String MS_FONT_LOCATION = "Fonts Location (MS)"; // Seccion donde estan los mappings font <--> FileName de la VM Microsoft
    public static final String SUN_FONT_LOCATION = "Fonts Location (Sun)"; // Seccion donde estan los mappings font <--> FileName de la VM de Sun
    public static final String FONT_SUBSTITUTES_SECTION = "Fonts Substitutions"; // Seccion donde estan los mappings extra de Font --> Font.
	public static final String FONT_METRICS_SECTION = "Font Metrics"; // Seccion donde se pueden colocar metricas para un font
    
    public static final String DEBUG_SECTION = "Debug";
    public static final String DRAW_IMAGE = "DrawImage"; 
    public static final String DRAW_LINE = "DrawLine"; 
    public static final String DRAW_TEXT = "DrawText"; 
    public static final String DRAW_BOX = "DrawBox"; 

	public static final String STYLE_DOTTED = "DottedStyle"; 
	public static final String STYLE_DASHED = "DashedStyle"; 
	public static final String STYLE_LONG_DASHED = "LongDashedStyle"; 
	public static final String STYLE_LONG_DOT_DASHED = "LongDotDashedStyle"; 

	public static final String DEFAULT_STYLE_DOTTED = "1;2";
	public static final String DEFAULT_STYLE_DASHED = "4;2";
	public static final String DEFAULT_STYLE_LONG_DASHED = "6;2";
	public static final String DEFAULT_STYLE_LONG_DOT_DASHED = "6;2;1;2";

	public static final String RUN_DIRECTION = "RunDirection";
	public static final String RUN_DIRECTION_LTR = "2";//PdfWriter.RUN_DIRECTION_LTR;

	public static final String JUSTIFIED_TYPE_ALL = "JustifiedTypeAll";

    // Constantes PDFReport.java 
    public static final int OUTPUT_SCREEN = 0; // Indica que la salida sera en la pantalla
    public static final int OUTPUT_PRINTER = 1; // Indica que la salida sera a la impresora
    public static final int OUTPUT_FILE = 2; // Indica que la salida sera a un archivo
    public static final int OUTPUT_STREAM = 3; // Indica que la salida sera a un outputStream indicado anteriormente
	public static final int OUTPUT_STREAM_PRINTER = 4; // Indica que la salida sera a un outputStream indicado anteriormente (pero ademas tiene output=only to printer)

    public static float OPTIMAL_MINIMU_BAR_WIDTH_SMALL_FONT = 0.6f;
	public static float OPTIMAL_MINIMU_BAR_WIDTH_LARGE_FONT = 0.68f;
	public static int LARGE_FONT_SIZE = 10;

    
    // DefaultMappings de fonts TTF -> Type1
    // Estos mappings se realizan autom�ticamente por el Acrobat, pero para poder manejar el tema de las
    // m�tricas, aca detectamos lo que el Acrobat va a hacer y hacemos el mapping en este momento (sino 
    // el Acrobat usa m�tricas err�neas y no se ve bien el PDF)
    //
    // El usuario puede definir nuevos fontsSubstitutes en el PDFReport.INI
    // Ademas si estamos en Windows, tambien tomamos los fontsSubstitutes propios de Windows
    public static final String FONT_SUBSTITUTES_TTF_TYPE1 [][] = {
                                                                  {"Arial", "Helvetica"},
                                                                  {"Courier New", "Courier"}, 
                                                                  {"Fixedsys", "Helvetica"}, 
                                                                  {"Modern", "Helvetica"},
                                                                  {"MS Sans Serif", "Helvetica"},
                                                                  {"MS Serif", "Helvetica"},
                                                                  {"Roman", "Helvetica"},
                                                                  {"Script", "Helvetica"},
                                                                  {"System", "Helvetica"}, 
                                                                  {"Times New Roman", "Times"},
																  {"\uff2d\uff33 \u660e\u671d", "Japanese"},
																  {"\uff2d\uff33 \u30b4\u30b7\u30c3\u30af", "Japanese2"},
																  };
                                                                  
    public static final String REGISTRY_FONT_SUBSTITUTES_ENTRY = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\FontSubstitutes"; // Contiene la entrada del Registry de los substitutos a los Fonts en NT/2000
    public static final String REGISTRY_FONT_SUBSTITUTES_ENTRY_NT = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\FontSubstitutes"; // Contiene la entrada del Registry de los substitutos a los Fonts en 95/98/Me
	
	public static final String FIX_SAC24437 = "FixSac24437";	
	public static final String BACK_FILL_IN_CONTROLS = "BackFillInControls";
}
