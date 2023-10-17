package com.genexus.reports;

import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.ProcessInterruptedException;

import org.apache.logging.log4j.Logger;

public abstract class GXReport extends GXProcedure {
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
   	protected String Gx_out = ""; // Esto esto asi porque no me pude deshacer de una comparacion contra Gx_out antes del ask.

	protected static Logger log = org.apache.logging.log4j.LogManager.getLogger(GXReport.class);

	public GXReport(int remoteHandle, ModelContext context, String location)
	{
		super(remoteHandle, context, location);
	}
	
	public GXReport(boolean inNewUTL, int remoteHandle, ModelContext context, String location) {
		super(inNewUTL, remoteHandle, context, location);
	}

	public static byte openGXReport(String document) {
		if(document.toLowerCase().endsWith(".pdf")) { // Si es un .pdf
			try {
				GXReportPDFCommons.showReport(document, false);
			} catch(Exception e) {
				log.error("GXReport failed to open report " + document + " : ", e);
				return -1;
			}							  
		} else {
			GXReportViewerThreaded.GxOpenDoc(document);
		}
		return 0;
	}
		
        public String setPrintAtClient()
        {
            String blobPath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
            String fileName = com.genexus.PrivateUtilities.getTempFileName(blobPath, "clientReport", getOutputType() == OUTPUT_PDF ? "pdf":"gxr");			
			
            getPrinter().GxSetDocName(fileName);
            getPrinter().GxSetDocFormat("GXR");
            com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(fileName);
			return fileName;
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
				try {
					String implementation = com.genexus.Application.getClientContext().getClientPreferences().getPDF_RPT_LIBRARY();
					if (implementation.equals("ITEXT"))
						reportHandler = new PDFReportItext2(context);
					else if (implementation.equals("ITEXT8"))
						reportHandler = new PDFReportItext8(context);
					else
						reportHandler = new PDFReportPDFBox(context);
					((GXReportPDFCommons) reportHandler).setOutputStream(getOutputStream());
				} catch (RuntimeException e) {
					log.debug("getPrinter may have failed to set the output stream for the report: ", e);
				} catch (Exception e) {
					log.error("Failed to set output stream: ", e);
				}
			}
			else {
				throw new RuntimeException("Unrecognized report type: " + getOutputType());
			}
		}
		return reportHandler;
	}
	protected void GxEndPage() throws ProcessInterruptedException {
		if	(reportHandler != null)
			reportHandler.GxEndPage();
	}

	protected boolean initTextPrinter(String output, int gxXPage, int gxYPage, String iniFile, String form, String printer, int mode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines) {
		int x[] = {gxXPage};
		int y[] = {gxYPage};

		getPrinter().GxRVSetLanguage(localUtil._language);
		boolean ret = getPrinter().GxPrTextInit(output, x, y,  iniFile, form, printer, mode, nPaperLength, nPaperWidth, nGridX, nGridY, nPageLines);

		this.gxXPage = x[0];
		this.gxYPage = y[0];

		return ret;
	}

	protected boolean initPrinter(String output, int gxXPage, int gxYPage, String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) {
		int x[] = {gxXPage};
		int y[] = {gxYPage};

		getPrinter().GxRVSetLanguage(localUtil._language);
      	boolean ret = getPrinter().GxPrintInit(output, x, y, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);

		this.gxXPage = x[0];
		this.gxYPage = y[0];

		return ret;
	}

	protected void endPrinter() {
		try {
			getPrinter().GxEndPrinter();
			waitPrinterEnd();
		} catch (Exception e) {}
	}

	protected void waitPrinterEnd() {
		if	(reportHandler != null && Gx_out.equals("SCR") && reportHandler.getModal()) {
			while (reportHandler.GxIsAlive());
		}
	}

   	protected int getOutputType() {
		return OUTPUT_RVIEWER;
	}

	protected java.io.OutputStream getOutputStream() {
		throw new RuntimeException("Output stream not set");
	}
	
	//M�todos para la implementaci�n de reportes din�micos

	protected void loadReportMetadata(String name) {
		reportMetadata = new GXReportMetadata(name, getPrinter());
		reportMetadata.load();
	}
	
	protected int GxDrawDynamicGetPrintBlockHeight(int printBlock) {
		return reportMetadata.GxDrawGetPrintBlockHeight(printBlock);
	}
	
	protected void GxDrawDynamicText(int printBlock, int controlId, int Gx_line) {
		reportMetadata.GxDrawText(printBlock, controlId, Gx_line);
	}
	
	protected void GxDrawDynamicText(int printBlock, int controlId, String value, int Gx_line) {
		reportMetadata.GxDrawText(printBlock, controlId, Gx_line, value);
	}
	
	protected void GxDrawDynamicLine(int printBlock, int controlId, int Gx_line) {
		reportMetadata.GxDrawLine(printBlock, controlId, Gx_line);
	}
	
	protected void GxDrawDynamicRect(int printBlock, int controlId, int Gx_line) {
		reportMetadata.GxDrawRect(printBlock, controlId, Gx_line);
	}	
	
	protected void GxDrawDynamicBitMap(int printBlock, int controlId, String value, int Gx_line) {
		reportMetadata.GxDrawBitMap(printBlock, controlId, Gx_line, value, 0);
	}
	
	protected void GxDrawDynamicBitMap(int printBlock, int controlId, String value, int aspectRatio, int Gx_line) {
		reportMetadata.GxDrawBitMap(printBlock, controlId, Gx_line, value, aspectRatio);
	}	
}
