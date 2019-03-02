// $Log: GXReportViewerThreaded.java,v $
// Revision 1.11  2006/11/29 15:02:19  alevin
// - (CMurialdo) Cambios para soportar nuevas propiedades en rectangulos de los reportes  (cornerradio para cada esquina y nuevos valores del style de lineas y lados de rectangulo).
//
// Revision 1.10  2006/06/20 14:26:51  iroqueta
// Implementacion de las propiedades CornersRadio y Style para los rect�ngulos y Style para las l�neas.
// Solo es valido para los reportes PDFs con la implementacion de iText.
//
// Revision 1.9  2006/04/24 18:39:03  iroqueta
// No se estaba heredando el MT y MB entre reportes... SAC 20051.
// Para que se pueda hacer se implmento el setM_top, setM_bot y getM_top, getM_bot en la interface IReportHandler.
//
// Revision 1.8  2006/01/30 14:29:47  iroqueta
// Duermo el thread 1 segundo al comienzo del cleanup para arreglar el SAC 19441.
// Sino habia veces que no se cerraba la VM porque quedaba trancado al intentar hacer el cleanup.
//
// Revision 1.7  2005/07/05 16:55:54  iroqueta
// Implementacion de aspectRatio en las imagenes de reportes.
// La idea por ahora es que se puede configurar en el config.gx si se quiere mantener al aspect ratio o no en las imagenes de reportes.
// Por el momento solo esta implementado en los reportes pdfs que usen itext.
//
// Revision 1.6  2005/01/19 12:47:38  iroqueta
// Comento el codigo del metodo waitEndRViewer() para poder solucionar el sac 17454.
// El doevents necesario se pasa a hacer en el metodo run de la clase ThreadedCommandQueue.
//
// Revision 1.5  2003/06/06 16:44:04  gusbro
// - fix: No estaba funcioanado el openGxReport de GX
//
// Revision 1.4  2002/07/19 21:38:21  aaguiar
// - Se habia perdido el arreglo de que quedaban colgados reportes a disco sin el &Line como parametro.
//
// Revision 1.3  2002/07/16 19:50:45  aaguiar
// - Cambio para que no consuma 100% de CPU
//
// Revision 1.1.1.1  2002/04/17 21:16:50  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/04/17 21:16:50  gusbro
// GeneXus Java Olimar
//
package com.genexus.reports;

import com.genexus.ICleanedup;
import com.genexus.ProcessInterruptedException;
import com.genexus.ResourceReader;
import com.genexus.CommonUtil;
import com.genexus.ApplicationContext;
import com.genexus.Application;
import com.genexus.PrivateUtilities;
import com.genexus.RunnableThrows;
import com.genexus.util.ThreadedCommandQueue;
import com.genexus.platform.NativeFunctions;
import com.genexus.platform.INativeFunctions;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class GXReportViewerThreaded implements IReportHandler, ICleanedup
{
	protected static String fileSeparator = System.getProperty("file.separator");
	protected IGXReportViewerImpl reportViewer;

	protected int handle;

	private String fontName;
	private int fontSize;
	private boolean fontBold;
	private boolean fontItalic;
	private boolean fontUnderline;
	private boolean fontStrikethru;
	private int Pen;
	private int foreRed;
	private int foreGreen;
	private int foreBlue;
	private int backMode; 
	private int backRed;
	private int backGreen;
	private int backBlue;

	protected final String msReportViewer = "com.genexus.reports.MSGXReportViewer";
	protected final String sunReportViewer = "com.genexus.reports.SunGXReportViewer";

	protected Hashtable tmpFiles;
 	static ThreadedCommandQueue q;
 	static boolean firstRV = true;

	static
	{
		//q = new QueuedExecutor(); //ThreadedCommandQueue();
		q = new ThreadedCommandQueue();
		q.startDispatching();
	}

	public GXReportViewerThreaded()
	{
		getReportViewer();
	}

	public static void GxOpenDoc(String fileName)
	{
		GXReportViewerThreaded rr = new GXReportViewerThreaded();
		rr.gxOpenDoc(fileName);
		rr.waitEndRViewer();
	}						 

	private int gxOpenDoc(final String fileName)
	{
		addCommand(new Runnable() { 
						public void run() 
						{
							getReportViewer().gxOpenDoc(fileName);
						} 
					});

		return 0;
	}

	protected IGXReportViewerImpl getReportViewer()
	{
		if	(reportViewer == null)
		{
			addCommand(new CreateRViewer());

			if	(firstRV)
			{
				Application.addCleanup(this);
				firstRV = false;
			}
		}		

		return reportViewer;
	}

	class CreateRViewer implements Runnable
	{
		public void run()
		{
		//	Thread.currentThread().setName("Report Viewer Queue");
			String rvClass = sunReportViewer;

			try
			{
				GXReportViewerThreaded.this.reportViewer = (IGXReportViewerImpl) Class.forName(rvClass).getDeclaredConstructor().newInstance();
			}
			catch (Exception e)
			{
			}

			if (!ApplicationContext.getInstance().isMsgsToUI())
			{
				reportViewer.gxRptSilentMode();
			}
		}
	}

	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue) 
	{
		GxDrawRect(left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue, 0, 0); 
	}
	
	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, int style, int cornerRadius) throws ProcessInterruptedException
	{
		GxDrawRect(left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue, style, style, style, style, cornerRadius, cornerRadius, cornerRadius, cornerRadius); 
	}	
	
	public void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, 
		int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR) throws ProcessInterruptedException
	{
		GxDrawRect cmd = new GxDrawRect(left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue);

		addCommand(cmd);

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}

	class GxDrawRect implements Runnable
	{
		boolean ret;
		int left;
		int top;
		int right;
		int bottom;
		int pen;
		int foreRed;
		int foreGreen;
		int foreBlue;
		int backMode;
		int backRed;
		int backGreen;
		int backBlue;

		GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue) 
		{
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.pen = pen;
			this.foreRed = foreRed;
			this.foreGreen = foreGreen;
			this.foreBlue = foreBlue;
			this.backMode = backMode;
			this.backRed = backRed;
			this.backGreen = backGreen;
			this.backBlue = backBlue ;
		}

		public void run()
		{
			ret = getReportViewer().gxDwRect(handle, left, top, right, bottom, pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue);
		}

		public boolean getReturn()
		{	
			return ret;
		}
	}

	protected void addCommand(Runnable cmd)
	{
	/*
		try

		{
		q.execute(cmd);
		} catch (InterruptedException e) { System.err.println("A " + e);}
		*/
		q.addCommand(cmd);
	}

	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue) 
	{
		GxDrawLine(left, top, right, bottom, width, foreRed, foreGreen, foreBlue, 0); 
	}
	
	public void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style) throws ProcessInterruptedException
	{
		GxDrawLine cmd;		
		addCommand(cmd = new GxDrawLine(left, top, right, bottom, width, foreRed, foreGreen, foreBlue));

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}

	class GxDrawLine implements Runnable
	{
		boolean ret;
		int left;
		int top;
		int right;
		int bottom;
		int width;
		int foreRed;
		int foreGreen;
		int foreBlue;

		GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue) 
		{
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.width  = width ;
			this.foreRed = foreRed;
			this.foreGreen = foreGreen;
			this.foreBlue = foreBlue;
		}

		public void run()
		{
			ret = getReportViewer().gxDwLine(handle, left, top, right, bottom, width, foreRed, foreGreen, foreBlue);
		}

		public boolean getReturn()
		{	
			return ret;
		}
	}

	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio) throws ProcessInterruptedException
	{
		GxDrawBitMap(bitmap, left, top, right, bottom);
	}																						 
																						 
	public void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom) throws ProcessInterruptedException
	{
		bitmap = CommonUtil.rtrim(CommonUtil.lower(bitmap));
		try
		{
			NativeFunctions.getInstance().executeWithPermissions(new CopyAndDraw(bitmap, left, top, right, bottom), INativeFunctions.FILE_ALL);
		}
		catch (Exception e)
		{
			throw (ProcessInterruptedException) e;
		}

	}
	

	class CopyAndDraw implements RunnableThrows
	{
		boolean ret;
		int left;
		int top;
		int right;
		int bottom;
		String bitmap;

		public CopyAndDraw(String bitmap, int left, int top, int right, int bottom) 
		{
			this.bitmap = bitmap;
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}

		public Object run() throws ProcessInterruptedException
		{
			if	(!(new File(bitmap).exists()))
			{
				String tempPath = fileSeparator + "temp" + fileSeparator;
				
				if	(tmpFiles == null)
				{
					new File(tempPath).mkdir();
					tmpFiles = new Hashtable();
				}

				if	(bitmap.lastIndexOf('/') >= 0)
					bitmap = bitmap.substring(bitmap.lastIndexOf('/') + 1, bitmap.length());
				else if (bitmap.lastIndexOf('\\') >= 0)
					bitmap = bitmap.substring(bitmap.lastIndexOf('\\') + 1, bitmap.length());

				bitmap = tempPath + bitmap;

				if	(tmpFiles.get(bitmap) == null)
				{
					// La idea aqui es que aunque ya exista el archivo, si todavia en esta sesion
					// no lo le�, lo paso por arriba, de modo que si cambi�, se actualice
					tmpFiles.put(bitmap, bitmap);

					InputStream is = ResourceReader.getFileAsStream(bitmap);
					if	(is == null)
						return null;

				 	PrivateUtilities.InputStreamToFile(is, bitmap);
				}
			}
			
			GxDrawBitMap cmd;		
			addCommand(cmd = new GxDrawBitMap(bitmap, left, top, right, bottom));

			if	(!cmd.getReturn())
				throw new ProcessInterruptedException();

			return null;
		}
	}

	class GxDrawBitMap implements Runnable
	{
		boolean ret;
		int left;
		int top;
		int right;
		int bottom;
		String bitmap;

		public GxDrawBitMap(String bitmap, int left, int top, int right, int bottom) 
		{
			this.bitmap = bitmap;
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}

		public void run()
		{
			ret = getReportViewer().gxDwBMap(handle, bitmap, left, top, right, bottom);
		}

		public boolean getReturn()
		{	
			return ret;
		}
	}

	public void GxAttris(String fontName, int fontSize, boolean fontBold, boolean fontItalic, boolean fontUnderline, boolean fontStrikethru, int Pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue)
	{
		this.fontName 		  = fontName;
		this.fontSize 		  = fontSize;
		this.fontBold		  = fontBold;
		this.fontItalic		  = fontItalic;
		this.fontUnderline	  = fontUnderline;
		this.fontStrikethru	  = fontStrikethru;
		this.Pen			  = Pen;
		this.foreRed		  = foreRed;
		this.foreGreen		  = foreGreen;
		this.foreBlue 		  = foreBlue;
		this.backMode 		  = backMode;
		this.backRed 		  = backRed;
		this.backGreen 		  = backGreen;
		this.backBlue		  = backBlue;
	}
	public void GxDrawText(String text, int left, int top, int right, int bottom, int align, int htmlformat, int border, int valign) throws ProcessInterruptedException
	{
		GxDrawText cmd;		
		addCommand(cmd = new GxDrawText(CommonUtil.rtrim(text), left, top, right, bottom, align));

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}
	public void GxDrawText(String text, int left, int top, int right, int bottom, int align, int htmlformat, int border) throws ProcessInterruptedException
	{
		GxDrawText(text, left, top, right, bottom, align, htmlformat, border, 0);
	}
	public void GxDrawText(String text, int left, int top, int right, int bottom, int align) throws ProcessInterruptedException
	{
		GxDrawText(text, left, top, right, bottom, align, 0);
	}
	public void GxDrawText(String text, int left, int top, int right, int bottom, int align, int htmlformat) throws ProcessInterruptedException
	{
		GxDrawText(text, left, top, right, bottom, align, htmlformat, 0);
	}
	
	class GxDrawText implements Runnable
	{
		boolean ret;
		int left;
		int top;
		int right;
		int bottom;
		int width;
		int align;
		String text;

		public GxDrawText(String text, int left, int top, int right, int bottom, int align) 
		{
			this.text = text;
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
			this.align = align;
		}

		public void run()
		{
			ret = getReportViewer().gxDwText(handle, text, left, top, right, bottom, fontName, fontSize, align, fontBold, fontItalic, fontUnderline, fontStrikethru, Pen, foreRed, foreGreen, foreBlue, backMode, backRed, backGreen, backBlue);
		}

		public boolean getReturn()
		{	
			return ret;
		}
	}

   	public void GxClearAttris()
	{
		this.fontName 		  = "Courier New";
		this.fontSize 		  = 9;
		this.fontBold		  = false;
		this.fontItalic		  = false;
		this.fontUnderline	  = false;
		this.fontStrikethru	  = false;
		this.Pen			  = 0;
		this.foreRed		  = 0;
		this.foreGreen		  = 0;
		this.foreBlue 		  = 0;
		this.backMode 		  = 0;
		this.backRed 		  = 255;
		this.backGreen 		  = 255;
		this.backBlue		  = 255;
	}

    public boolean GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) 
	{
		GxPrintInit cmd;		
		addCommand(cmd = new GxPrintInit(output, gxXPage, gxYPage, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex));

		return cmd.getReturn();
	}

	class GxPrintInit implements Runnable
	{
		String output; 
		int gxXPage[]; 
		int gxYPage[];
		String iniFile;
		String form;
		String printer;
		int mode;
		int orientation;
		int pageSize;
		int pageLength;
		int pageWidth;
		int scale;
		int copies; 
		int defSrc;
		int quality;
		int color;
		int duplex;
		boolean ret;

		public GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) 
		{
			this.gxXPage     = gxXPage; 
			this.gxYPage	 = gxYPage;
			this.iniFile	 = iniFile;
			this.form		 = form;
			this.printer	 = printer;
			this.mode		 = mode;
			this.orientation = orientation;
			this.pageSize	 = pageSize;
			this.pageLength  = pageLength;
			this.pageWidth	 = pageWidth;
			this.scale		 = scale;
			this.copies 	 = copies; 
			this.defSrc	  	 = defSrc;
			this.quality	 = quality;
			this.color		 = color;
			this.duplex	  	 = duplex;
		}
		
		public void run()
		{
			int newHandle[] =  {0};

			ret = getReportViewer().gxPrInit( newHandle, getOutputCode(output), gxXPage, gxYPage, 0, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);
			GXReportViewerThreaded.this.handle = newHandle[0];
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public boolean GxPrTextInit(String output, int nxPage[], int nyPage[], String psIniFile, String psForm, String sPrinter, int nMode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines)
	{
		GxPrTextInit cmd;		
		addCommand(cmd = new GxPrTextInit(output, nxPage, nyPage, psIniFile, psForm, sPrinter, nMode, nPaperLength, nPaperWidth, nGridX, nGridY, nPageLines));

		return cmd.getReturn();
	}

	class GxPrTextInit implements Runnable
	{
		boolean ret;

		String output;
		int nxPage[];
		int nyPage[]; 
		String psIniFile;
		String psForm;
		String sPrinter;
		int nMode;
		int nPaperLength;
		int nPaperWidth;
		int nGridX;
		int nGridY;
		int nPageLines;

		public GxPrTextInit(String output, int nxPage[], int nyPage[], String psIniFile, String psForm, String sPrinter, int nMode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines)
		{
			this.nxPage			= nxPage;
			this.nyPage			= nyPage;
			this.psIniFile		= psIniFile;
			this.psForm			= psForm;
			this.sPrinter		= sPrinter;
			this.nMode			= nMode;
			this.nPaperLength	= nPaperLength;
			this.nPaperWidth	= nPaperWidth;
			this.nGridX			= nGridX;
			this.nGridY		    = nGridY;
			this.nPageLines		= nPageLines;
		}
	
		public void run()
		{
			int newHandle[] =  {0};

			ret = getReportViewer().gxPrTextInit(newHandle, getOutputCode(output), nxPage, nyPage, psIniFile, psForm, sPrinter, nMode, nPaperLength, nPaperWidth, nGridX, nGridY, nPageLines);

			GXReportViewerThreaded.this.handle = newHandle[0];
		}

		public boolean getReturn()	
		{
			return ret;
		}
	}



	public void GxPrintMax() 
	{
		addCommand(new GxPrintMax());
	}

	public void GxPrintNormal () 
	{
		addCommand(new GxPrintNormal());
	}

	class GxPrintNormal implements Runnable
	{
		public void run()
		{
	   		getReportViewer().gxRptWndNormal();
		}
	}

	class GxPrintMax implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxRptWndMaximize();
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public void GxPrintOnTop() 
	{
		addCommand(new GxPrintOnTop());
	}


	class GxPrintOnTop implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxRptWndOnTop();
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

    public void setPageLines(int pageLines) 
	{
		this.pageLines = pageLines;
	}
    
    public void setLineHeight(int lineHeight) 
	{
		this.lineHeight = lineHeight;
	}
	
	int pageLines;
	int lineHeight;
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

    public int getPageLines() 
	{
		return pageLines;
	}

	int page;
    public int getPage() 
	{
		return page;
	}

    public void setPage(int page) 
	{
		this.page = page;
	}


    public int getLineHeight() 
	{
		return lineHeight;
	}

    public void GxEndPage()  throws ProcessInterruptedException
    {
		GxEndPage cmd;		
		addCommand(cmd = new GxEndPage());

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}
    

	class GxEndPage implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxEndPg(handle, 0);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

    public void GxEndDocument()  throws ProcessInterruptedException
    {
		GxEndDocument cmd;		
		addCommand(cmd = new GxEndDocument());

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}

	class GxEndDocument implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxEndDoc(handle, 0);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

    public void GxEndPrinter() throws ProcessInterruptedException
    {
		GxEndPrinter cmd;		
		addCommand(cmd = new GxEndPrinter());

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();

		waitEndRViewer();
    }

	class GxEndPrinter implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxEndPrn(handle, 0);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}
	

	public void waitEndRViewer() 
	{
	}

	public void GxStartPage() throws ProcessInterruptedException
	{
		GxStartPage cmd;		
		addCommand(cmd = new GxStartPage());

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	} 

	class GxStartPage implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxStartPg(handle, 0);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public void GxStartDoc() throws ProcessInterruptedException
	{
		GxStartDoc cmd;		
		addCommand(cmd = new GxStartDoc());

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}

	class GxStartDoc implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxStartDoc(handle);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public void GxRVSetLanguage(String lang)
	{
		addCommand(new GxRVSetLanguage(lang));
	}

	class GxRVSetLanguage implements Runnable
	{
		private String lang;

		GxRVSetLanguage(String lang)
		{
			this.lang = lang;
		}

		public void run()
		{
	   		getReportViewer().gxRVSetLanguage(lang);
		}
	
	}

	public void GxPrnCmd(String scmd) throws ProcessInterruptedException
	{
		GxPrnCmd cmd;
		addCommand(cmd = new GxPrnCmd(scmd));

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}
	
	class GxPrnCmd implements Runnable
	{
		boolean ret;
		String cmd;

		public GxPrnCmd(String cmd)
		{
			this.cmd = cmd;
		}
		
		public void run()
		{
	   		ret = getReportViewer().gxPrnCmd(handle, cmd);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public void GxSetDocName(String docName) throws ProcessInterruptedException
	{
		GxSetDocName cmd;		
		addCommand(cmd = new GxSetDocName(docName));

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}

	class GxSetDocName implements Runnable
	{
		boolean ret;
		String docName;

		public GxSetDocName (String docName)
		{
			this.docName = docName;
		}

		public void run()
		{
	   		ret = getReportViewer().gxSetDocName(handle, docName);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public void GxSetDocFormat(String docFormat) throws ProcessInterruptedException
	{
		GxSetDocFormat cmd;		
		addCommand(cmd = new GxSetDocFormat(docFormat));

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}

	class GxSetDocFormat implements Runnable
	{
		boolean ret;
		String docFormat;

		public GxSetDocFormat (String docFormat)
		{
			this.docFormat = docFormat;
		}

		public void run()
		{
			int format = 0;

			if		(docFormat.equalsIgnoreCase("GXR"))
				format = 0;
			else if	(docFormat.equalsIgnoreCase("RTF"))
				format = 1;
			else if	(docFormat.equalsIgnoreCase("HTML"))
				format = 2;
			else if	(docFormat.equalsIgnoreCase("TXT"))
				format = 3;
	
	   		ret = getReportViewer().gxSetDocFormat(handle, format);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}


	public void GxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength) throws ProcessInterruptedException
	{
		GxSetTextMode cmd;		
		addCommand(cmd = new GxSetTextMode(nHandle, nGridX, nGridY, nPageLength));

		if	(!cmd.getReturn())
			throw new ProcessInterruptedException();
	}

	class GxSetTextMode implements Runnable
	{
		boolean ret;
		int nHandle, nGridX, nGridY, nPageLength;

		public GxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength) 
		{
			this.nHandle   = nHandle;
			this.nGridX = nGridX;
			this.nGridY = nGridY;
			this.nPageLength = nPageLength;
		}
			
		public void run()
		{
			ret = getReportViewer().gxSetTextMode(nHandle, nGridX, nGridY, nPageLength);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public boolean GxIsAlive() 
	{
		GxIsAlive cmd;		
		addCommand(cmd = new GxIsAlive());

		return cmd.getReturn();
	}

	class GxIsAlive implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxIsAlive();
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public boolean GxIsAliveDoc()
	{
		GxIsAliveDoc cmd;		
		addCommand(cmd = new GxIsAliveDoc());

		return cmd.getReturn();
	}

	class GxIsAliveDoc implements Runnable
	{
		boolean ret;

		public void run()
		{
	   		ret = getReportViewer().gxIsAliveDoc(handle);
		}

		public boolean getReturn()
		{
			return ret;
		}
	}

	public void cleanup()
	{
		try
		{
			Thread.sleep(1000);
		}
		catch(InterruptedException e) { ; }
		
		addCommand(new GxShutDown());
		//q.shutdownAfterProcessingCurrentlyQueuedTasks(); //cleanup();
		q.cleanup();

		if	(tmpFiles != null)
		{
			NativeFunctions.getInstance().executeWithPermissions(new DeleteTmpFiles(), INativeFunctions.FILE_ALL);
		}
	}

	class DeleteTmpFiles implements Runnable
	{
		public void run()
		{
			for (Enumeration en = tmpFiles.keys(); en.hasMoreElements(); )
			{
				new File((String) en.nextElement()).delete();
			}
		}
	}

	class GxShutDown implements Runnable
	{
		public void run()
		{
	   		getReportViewer().gxShutdown();
		}
	}

	public boolean GxPrnCfg( final String ini )
	{
		addCommand(new Runnable() { 
						public void run() 
						{
							getReportViewer().gxPrnCfg(ini);
						} 
					});
		return true;
	}

	protected static int getOutputCode(String output)
	{
		if	(output.equals("PRN"))
			return 0;
		if  (output.equals("FIL"))
			return 2;
		
		return 1;
	}

	private boolean modal;
	public boolean getModal()
	{
		return modal;
	}
	public void setModal(boolean modal)
	{
		this.modal = modal;
	}

    public void setMetrics(String fontName, boolean bold, boolean italic, int ascent, int descent, int height, int maxAdvance, int[] sizes)
	{
	}
}

