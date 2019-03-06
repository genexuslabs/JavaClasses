// $Log: IReportHandler.java,v $
// Revision 1.6  2006/11/29 15:02:19  alevin
// - (CMurialdo) Cambios para soportar nuevas propiedades en rectangulos de los reportes  (cornerradio para cada esquina y nuevos valores del style de lineas y lados de rectangulo).
//
// Revision 1.5  2006/06/20 14:26:51  iroqueta
// Implementacion de las propiedades CornersRadio y Style para los rect�ngulos y Style para las l�neas.
// Solo es valido para los reportes PDFs con la implementacion de iText.
//
// Revision 1.4  2006/04/24 18:37:11  iroqueta
// No se estaba heredando el MT y MB entre reportes... SAC 20051.
// Para que se pueda hacer se implmento el setM_top, setM_bot y getM_top, getM_bot en la interface IReportHandler.
//
// Revision 1.3  2005/08/16 22:04:26  alevin
// - Agrego el metodo cleanup.
//
// Revision 1.2  2005/07/05 16:55:54  iroqueta
// Implementacion de aspectRatio en las imagenes de reportes.
// La idea por ahora es que se puede configurar en el config.gx si se quiere mantener al aspect ratio o no en las imagenes de reportes.
// Por el momento solo esta implementado en los reportes pdfs que usen itext.
//
// Revision 1.1  2001/10/29 12:42:16  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/10/29 12:42:16  gusbro
// GeneXus Java Olimar
//
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
