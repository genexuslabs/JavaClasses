package com.genexus.reports;

interface IGXReportViewerImpl
{
	boolean gxRptSilentMode();
	boolean gxOpenDoc(String fileName);
	boolean gxSetDocFormat( int handle, int format);
	boolean gxRVSetLanguage(String lang);
	boolean gxPrnCfg( String ini );
	boolean gxPrInit( int handle[], 
		int isStoring, 
		int xPage[], 
		int yPage[], 
		int dummy,	
		String iniFile,	
		String form, 
		String printer,
		int mode,
		int orientation,
		int paperSize,
		int paperLength,
		int paperWidth,
		int scale,
		int copies,
		int defaultSource,
		int printQuality,
		int color,
		int duplex );
	boolean gxIsAlive();
	boolean gxIsAliveDoc(int handle);
	boolean gxRptMng();
	boolean gxRptWndNormal();
	boolean gxRptWndMaximize();
	boolean gxRptWndOnTop();
	boolean gxStartDoc(int handle);
	boolean gxStartPg(int handle, int flag );
	boolean gxEndPg(int handle, int flag );
	boolean gxEndDoc(int handle, int flag );
	boolean gxEndPrn(int handle, int flag );
	boolean gxDwTextNB(int handle,
		String text,
		int left,
		int top,
		int right,
		int bottom,
		String fontName,
		int fontSize,
		int textFlags,
		boolean bold,
		boolean italic,
		boolean underline,
		boolean strikeOut,
		int pen,
		int frontColR,
		int frontColG,
		int frontColB );
	boolean gxDwText( int handle,
		String text,
		int left,
		int top,
		int right,
		int bottom,
		String fontName,
		int fontSize,
		int textFlags,
		boolean bold,
		boolean italic,
		boolean underline,
		boolean strikeOut,
		int pen,
		int frontColR,
		int frontColG,
		int frontColB,
		int backMode,
		int backColR,
		int backColG,
		int backColB );
	boolean gxDwRect( int handle,
		int left,
		int top,
		int right,
		int bottom,
		int pen,
		int frontColR,
		int frontColG,
		int frontColB,
		int backMode,
		int backColR,
		int backColG,
		int backColB );
	boolean gxDwRectNB( int handle,
		int left,
		int top,
		int right,
		int bottom,
		int pen,
		int frontColR,
		int frontColG,
		int frontColB );
	boolean gxDwBMap( int handle,
		String name,
		int left,
		int top,
		int right,
		int bottom );
	boolean gxDwLine( int handle,
		int left,
		int top,
		int right,
		int bottom,
		int pen,
		int frontColR,
		int frontColG,
		int frontColB );
	boolean gxPrnCmd(int handle, String cmd);
	boolean gxSetDocName(int handle, String cmd);
	boolean gxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength);
	boolean gxPrTextInit(int nHandle[], int nOutputMode, int nxPage[], int nyPage[], String psIniFile, String psForm, String sPrinter, int nMode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines);
	boolean gxShutdown();

} // end IGXReportViewer
