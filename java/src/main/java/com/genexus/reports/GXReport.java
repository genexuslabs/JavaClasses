// $Log: GXReport.java,v $
// Revision 1.5  2005/10/04 17:29:48  gusbro
// - Agrego un constructor nuevo que recibe un boolean para saber si ejecutar en una nueva UTL
//
// Revision 1.4  2005/07/08 23:09:16  gusbro
// - Cambio en la openGXReport para que si el nombre del archivo termina en .pdf
//   te lo abra con el Acrobat
//
// Revision 1.3  2004/11/03 16:56:45  iroqueta
// La implementacion de reportes PDF usando itext solo se usa si se ejecuta con la VM de Sun.
//
// Revision 1.2  2004/10/25 15:29:24  iroqueta
// Implementacion de los reportes PDFs usando la lib iText
//
// Revision 1.1  2002/06/10 17:14:42  gusbro
// Initial revision
//
// Revision 1.2  2002/06/10 17:14:41  aaguiar
// - Se le pasa el context al PDFReport
//
// Revision 1.1.1.1  2001/12/11 13:21:46  gusbro
// GeneXus Java Olimar
//
package com.genexus.reports;

import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.ProcessInterruptedException;

public abstract class GXReport extends GXProcedure
{
	protected static final int OUTPUT_RVIEWER = 1;
	protected static final int OUTPUT_PDF     = 2;

	// Tiene que ser protected porque se pasa como par�metro en los reports
	protected GXReportMetadata reportMetadata;	
	protected IReportHandler reportHandler;

	protected int lineHeight;
   	protected int Gx_line;
   	protected int P_lines;
   	protected int gxXPage;
   	protected int gxYPage;
   	protected int Gx_page;
   	protected String Gx_out = ""; // Esto est� asi porque no me pude deshacer de una comparacion contra Gx_out antes del ask.

	public GXReport(int remoteHandle, ModelContext context, String location)
	{
		super(remoteHandle, context, location);
	}
	
	public GXReport(boolean inNewUTL, int remoteHandle, ModelContext context, String location)
	{
		super(inNewUTL, remoteHandle, context, location);
	}

	public static byte openGXReport(String document)
	{
		if(document.toLowerCase().endsWith(".pdf"))
		{ // Si es un .pdf
			try
			{
				PDFReportItext.showReport(document, false);
			}catch(Exception e)
			{
				System.err.println(e.toString());
				return -1;
			}							  
		}else
		{
			GXReportViewerThreaded.GxOpenDoc(document);
		}
		return 0;
	}
     
		protected void setPrintAtClient()
		{
			setPrintAtClient("");
		}
		
        protected void setPrintAtClient(String printerRule)
        {
            String blobPath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
            String fileName = com.genexus.PrivateUtilities.getTempFileName(blobPath, "clientReport", getOutputType() == OUTPUT_PDF ? "pdf":"gxr");			
			
            getPrinter().GxSetDocName(fileName);
            getPrinter().GxSetDocFormat("GXR");
            if (httpContext != null)
            {
                httpContext.printReportAtClient(fileName, printerRule);
            }
            com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(fileName);
        }

	public void setPrinter(IReportHandler reportHandler)
	{
		this.reportHandler = reportHandler;
	}

	public IReportHandler getPrinter()
	{
		if	(reportHandler == null)
		{
			if	(getOutputType() == OUTPUT_RVIEWER)
			{
  				reportHandler = new GXReportViewerThreaded();
			}
			else if  (getOutputType() == OUTPUT_PDF)
			{
                            reportHandler = new PDFReportItext(context);

				try
				{
	                ((PDFReportItext) reportHandler).setOutputStream(getOutputStream());
				}
				catch (Exception e)
				{
				}
			}
			else
			{
				throw new RuntimeException("Unrecognized report type: " + getOutputType());
			}
		}

		return reportHandler;
	}
/*
	public String format(String value, String picture)
	{
		return PictureFormatter.format(value, picture);
	}

	public String format(long value, String picture)
	{
		return localUtil.format(value, picture);
	}

	public String format(java.util.Date value, String picture)
	{
		return localUtil.format(value, picture);
	}

	public String format(double value, String picture)
	{
		return localUtil.format(value, picture);
	}
*/
	protected void GxEndPage() throws ProcessInterruptedException
	{
		if	(reportHandler != null)
			reportHandler.GxEndPage();
	}

	protected boolean initTextPrinter(String output, int gxXPage, int gxYPage, String iniFile, String form, String printer, int mode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines)
	{
		int x[] = {gxXPage};
		int y[] = {gxYPage};

		getPrinter().GxRVSetLanguage(localUtil._language);
		boolean ret = getPrinter().GxPrTextInit(output, x, y,  iniFile, form, printer, mode, nPaperLength, nPaperWidth, nGridX, nGridY, nPageLines);

		this.gxXPage = x[0];
		this.gxYPage = y[0];

		return ret;
	}

	protected boolean initPrinter(String output, int gxXPage, int gxYPage, String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex)
	{
		int x[] = {gxXPage};
		int y[] = {gxYPage};

		getPrinter().GxRVSetLanguage(localUtil._language);
      	boolean ret = getPrinter().GxPrintInit(output, x, y, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);

		this.gxXPage = x[0];
		this.gxYPage = y[0];

		return ret;
	}

	protected void endPrinter()
	{
		try
		{
			getPrinter().GxEndPrinter();
			waitPrinterEnd();
		}
		catch (Exception e)
		{
		}
	}

	protected void waitPrinterEnd()
	{
		if	(reportHandler != null && Gx_out.equals("SCR") && reportHandler.getModal())
		{
			while	(reportHandler.GxIsAlive());
		}

/*		IGUIContext ctx = context.getGUIContext();
		if	(ctx instanceof com.genexus.ui.GUIContext)
		{
			((com.genexus.ui.GUIContext) ctx).getWorkpanel().setFocus();
		}
*/
	}

   	protected int getOutputType()
	{
		return OUTPUT_RVIEWER;
	}

	protected java.io.OutputStream getOutputStream()
	{
		throw new RuntimeException("Output stream not set");
	}
	
	//M�todos para la implementaci�n de reportes din�micos
	
	protected void loadReportMetadata(String name)
	{
		reportMetadata = new GXReportMetadata(name, getPrinter());
		reportMetadata.load();
	}
	
	protected int GxDrawDynamicGetPrintBlockHeight(int printBlock)
	{
		return reportMetadata.GxDrawGetPrintBlockHeight(printBlock);
	}
	
	protected void GxDrawDynamicText(int printBlock, int controlId, int Gx_line)
	{
		reportMetadata.GxDrawText(printBlock, controlId, Gx_line);
	}
	
	protected void GxDrawDynamicText(int printBlock, int controlId, String value, int Gx_line)
	{
		reportMetadata.GxDrawText(printBlock, controlId, Gx_line, value);
	}
	
	protected void GxDrawDynamicLine(int printBlock, int controlId, int Gx_line)
	{
		reportMetadata.GxDrawLine(printBlock, controlId, Gx_line);
	}
	
	protected void GxDrawDynamicRect(int printBlock, int controlId, int Gx_line)
	{
		reportMetadata.GxDrawRect(printBlock, controlId, Gx_line);
	}	
	
	protected void GxDrawDynamicBitMap(int printBlock, int controlId, String value, int Gx_line)
	{
		reportMetadata.GxDrawBitMap(printBlock, controlId, Gx_line, value, 0);
	}
	
	protected void GxDrawDynamicBitMap(int printBlock, int controlId, String value, int aspectRatio, int Gx_line)
	{
		reportMetadata.GxDrawBitMap(printBlock, controlId, Gx_line, value, aspectRatio);
	}	
}
