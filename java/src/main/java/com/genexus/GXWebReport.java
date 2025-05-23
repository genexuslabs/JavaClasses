package com.genexus;

import com.genexus.db.UserInformation;
import com.genexus.internet.HttpContext;
import com.genexus.reports.*;
import com.genexus.webpanels.GXWebProcedure;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class GXWebReport extends GXWebProcedure {
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GXWebReport.class);

	public static final int OUTPUT_RVIEWER = 1;
	public static final int OUTPUT_PDF = 2;

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
	protected String filename;
	protected String filetype;

	public GXWebReport(HttpContext httpContext) {
		super(httpContext);
	}

	protected void initState(ModelContext context, UserInformation ui) {
		super.initState(context, ui);

		httpContext.setResponseBufferMode(HttpContext.ResponseBufferMode.ENABLED);
		httpContext.setBinary(true);
		String implementation = com.genexus.Application.getClientContext().getClientPreferences().getPDF_RPT_LIBRARY();
		if (implementation.equals("ITEXT"))
			reportHandler = new PDFReportItext2(context);
		else if (implementation.equals("ITEXT8"))
			reportHandler = new PDFReportItext8(context);
		else
			reportHandler = new PDFReportPDFBox(context);
		initValues();
	}

	protected void preExecute() {
		httpContext.setContentType("application/pdf");
		httpContext.setStream();
		((GXReportPDFCommons) reportHandler).setOutputStream(httpContext.getOutputStream());
	}

	protected void setOutputFileName(String outputFileName) {
		filename = outputFileName;
	}
	protected void setOutputType(String outputType) {
		filetype = outputType.toLowerCase();
	}
	private void initValues() {
		Gx_line = 0;
		P_lines = 0;
		gxXPage = 0;
		gxYPage = 0;
		Gx_page = 0;
		Gx_out = ""; // Esto est� asi porque no me pude deshacer de una comparacion contra Gx_out antes del ask.
		lineHeight = 0;
	}

	public void setPrinter(IReportHandler reportHandler) {
		this.reportHandler = reportHandler;
	}

	public IReportHandler getPrinter() {
		return reportHandler;
	}

	protected void GxEndPage() throws ProcessInterruptedException {
		if (reportHandler != null)
			reportHandler.GxEndPage();
	}

	protected boolean initTextPrinter(String output, int gxXPage, int gxYPage, String iniFile, String form, String printer, int mode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines) {
		int x[] = {
			gxXPage
		};
		int y[] = {
			gxYPage
		};

		getPrinter().GxRVSetLanguage(localUtil._language);
		boolean ret = getPrinter().GxPrTextInit(output, x, y, iniFile, form, printer, mode, nPaperLength, nPaperWidth, nGridX, nGridY, nPageLines);

		this.gxXPage = x[0];
		this.gxYPage = y[0];

		return ret;
	}

	protected boolean initPrinter(String output, int gxXPage, int gxYPage, String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int scale, int copies, int defSrc, int quality, int color, int duplex) {
		int x[] = {
			gxXPage
		};
		int y[] = {
			gxYPage
		};
		setResponseOutputFileName();

		getPrinter().GxRVSetLanguage(localUtil._language);
		boolean ret = getPrinter().GxPrintInit(output, x, y, iniFile, form, printer, mode, orientation, pageSize, pageLength, pageWidth, scale, copies, defSrc, quality, color, duplex);

		this.gxXPage = x[0];
		this.gxYPage = y[0];

		return ret;
	}

	private void setResponseOutputFileName() {
		String outputFileName = (filename != null ? filename : getClass().getSimpleName());
		String outputFileType = (filetype != null ? "." + filetype.toLowerCase() : ".pdf");

		try {
			String encodedFileName = URLEncoder.encode(outputFileName, "UTF-8").replace("+", "%20");
			boolean isAscii = StandardCharsets.US_ASCII.newEncoder().canEncode(outputFileName);
			String fallbackFileName = outputFileName;
			if (!isAscii) {
				fallbackFileName = outputFileName.replaceAll("[^\\p{ASCII}]", "");
				if (fallbackFileName.isEmpty()) {
					fallbackFileName = "download";
				}
			}
			String contentDispositionValue =
				"inline; filename=\"" + fallbackFileName + outputFileType + "\"; " +
					"filename*=UTF-8''" + encodedFileName + outputFileType;

			httpContext.getResponse().addHeader("Content-Disposition", contentDispositionValue);
		} catch (UnsupportedEncodingException e) {
			httpContext.getResponse().addHeader("Content-Disposition", "inline; filename=\"download" + outputFileType + "\"");
		}
	}

	/**
	 * Returns an ASCII version of the filename.
	 * Non-ASCII characters are replaced with underscore to ensure ASCII compliance.
	 */
	private String asciiFileName(String fileName) {
		StringBuilder asciiFileName = new StringBuilder();
		for (char character: fileName.toCharArray()) {
			if ((character <= 127)) {
				asciiFileName.append(character);
			} else {
				asciiFileName.append("_");
			}
		}
		return asciiFileName.toString();
	}

	protected void endPrinter() {
		getPrinter().GxEndPrinter();
	}

	protected int getOutputType() {
		return OUTPUT_RVIEWER;
	}

	protected java.io.OutputStream getOutputStream() {
		throw new RuntimeException("Output stream not set");
	}

	//M�todos para la implementaci�n de reportes din�micos
	protected void loadReportMetadata(String name) {
		reportMetadata = new GXReportMetadata(name, reportHandler);
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

	protected void cleanup() {
		super.cleanup();
	}
}