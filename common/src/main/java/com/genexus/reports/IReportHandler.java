package com.genexus.reports;

import com.genexus.ProcessInterruptedException;

/*
  NOTA---> Tuve que cambiar los ProcessInterrputedException por Exception porque el ProcessInterruptedException NO es public y
  ensa en otro package

*/
public interface IReportHandler
{
	void GxRVSetLanguage(String lang);
    void GxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength) throws ProcessInterruptedException;
    void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue) throws ProcessInterruptedException;
	void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, int style, int cornerRadius) throws ProcessInterruptedException;
	void GxDrawRect(int left, int top, int right, int bottom, int pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue, 
		int styleTop, int styleBottom, int styleRight, int styleLeft, int cornerRadioTL, int cornerRadioTR, int cornerRadioBL, int cornerRadioBR) throws ProcessInterruptedException;
	
    void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue) throws ProcessInterruptedException;
	void GxDrawLine(int left, int top, int right, int bottom, int width, int foreRed, int foreGreen, int foreBlue, int style) throws ProcessInterruptedException;
    void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom) throws ProcessInterruptedException;
	void GxDrawBitMap(String bitmap, int left, int top, int right, int bottom, int aspectRatio) throws ProcessInterruptedException;
    void GxAttris(String fontName, int fontSize, boolean fontBold, boolean fontItalic, boolean fontUnderline, boolean fontStrikethru, int Pen, int foreRed, int foreGreen, int foreBlue, int backMode, int backRed, int backGreen, int backBlue) throws ProcessInterruptedException;
    void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align) throws ProcessInterruptedException;
    void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat) throws ProcessInterruptedException;
    void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border) throws ProcessInterruptedException;
    void GxDrawText(String sTxt, int left, int top, int right, int bottom, int align, int htmlformat, int border, int valign) throws ProcessInterruptedException;
    void GxClearAttris();
    boolean GxPrintInit(String output, int gxXPage[], int gxYPage[], String iniFile, String form, String printer, int mode, int orientation, int pageSize, int pageLength, int pageWidth, int sacle, int copies, int defSrc, int quality, int color, int duplex) ;
    void GxPrintMax();
    void GxPrintNormal();
    void GxPrintOnTop();
    public void setPage(int page);
    public int getPage();

    int getPageLines() ;
    int getLineHeight() ;
	int getM_top() ;
    int getM_bot() ;
    void setPageLines(int P_lines) ;
    void setLineHeight(int lineHeight) ;
	void setM_top(int M_top) ;
    void setM_bot(int M_bot) ;
	
	void cleanup();

    void GxEndPage() throws ProcessInterruptedException;
    void GxEndDocument() throws ProcessInterruptedException;
    void GxEndPrinter() throws ProcessInterruptedException;
    void GxStartPage() throws ProcessInterruptedException;
    void GxStartDoc() throws ProcessInterruptedException;
    void GxSetDocFormat(String format) throws ProcessInterruptedException;

    void GxPrnCmd(String cmd) throws ProcessInterruptedException;
    void GxSetDocName(String docName) throws ProcessInterruptedException;
    boolean GxPrTextInit(String ouput, int nxPage[], int nyPage[], String psIniFile, String psForm, String sPrinter, int nMode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines) ;
	boolean GxPrnCfg( String ini );

	boolean GxIsAlive();
	boolean GxIsAliveDoc();

    void setModal(boolean value);
    boolean getModal();
    void setMetrics(String fontName, boolean bold, boolean italic, int ascent, int descent, int height, int maxAdvance, int[] sizes);

}
