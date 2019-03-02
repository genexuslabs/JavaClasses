// $Log: SunGXReportViewer.java,v $
// Revision 1.6  2005/01/21 21:02:10  iroqueta
// Se puso publica la clase para poder usarla desde el ThreadedCommandQueue.java
//
// Revision 1.5  2004/04/13 18:43:24  gusbro
// - Arreglos para fonts asiaticos
//
// Revision 1.4  2004/03/22 17:17:46  gusbro
// - Por un cambio en la rbuildj.dll estaban quedando deshabilitadas las opciones Open y Save As
//
// Revision 1.3  2004/03/15 19:04:01  gusbro
// - Arreglo en llamada a la dll cuando se tenian encodings asiaticos
//
// Revision 1.2  2002/10/30 19:00:33  gusbro
// - Se agrega la GxGetMainFrameHandle que obtiene el handle del frame del reportViewer
//
// Revision 1.1.1.1  2001/07/30 21:32:34  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/07/30 21:32:34  gusbro
// GeneXus Java Olimar
//
package com.genexus.reports;

import java.io.*;
import com.genexus.*;

public class SunGXReportViewer implements IGXReportViewerImpl
{
	static
	{
		String file = "";
		if(ApplicationContext.getInstance().isServletEngine())
		{
			try
			{
				file = ApplicationContext.getInstance().getServletEngineDefaultPath();
				if(!file.equals("") && !file.endsWith(File.separator))
				{
					file += File.separator;
				}
			}catch(Exception ee) { ; }						
		}
		else
		{
			file = new java.io.File("").getAbsolutePath() + File.separator;
		}
		
		LoadLibrary.load(file + "gxdib32.dll");
		LoadLibrary.load(file + "rbuildj.dll");
	}
		
	public SunGXReportViewer()
	{
		initialize();
	}
	
	public void initialize()
	{
		// Por defecto activo el SaveAs y el Open		
		GxRVSetEnableSaveAs((short)(Boolean.getBoolean("rviewer.disableSaveAs") ? 0 : 1));
		GxRVSetEnableOpen((short)(Boolean.getBoolean("rviewer.disableOpen") ? 0 : 1));
	}

	public boolean gxRptSilentMode()
	{
		return GxRptSilentMode();
	}

	public boolean gxOpenDoc(String fileName)
	{
		return GxOpenDoc(fileName);
	}

	public boolean gxSetDocFormat(int handle, int format)
	{
		return GxSetDocFormat(handle, format);
	}

	public boolean gxRVSetLanguage(String lang)
	{
		return GxRVSetLanguage(lang);
	}

	public boolean gxShutdown()
	{
		return GxShutdown(); 
	}

	public boolean gxIsAlive()
	{
		return GxIsAlive();
	} // end gxIsAlive

	public boolean gxRptMng()
	{
		return GxRptMng();
	} // end gxRptMng

	public boolean gxRptWndNormal()
	{
		return GxRptWndNormal();
	} // end gxRptWndNormal

	public boolean gxRptWndMaximize()
	{
		return GxRptWndMaximize();
	} // end gxRptWndMaximize

	public boolean gxRptWndOnTop()
	{
		return GxRptWndOnTop();
	} // end gxRptWndOnTop

	public boolean gxPrnCfg( String ini )
	{
		return GxPrnCfg( ini );
	} // end gxRptWndOnTop

	public boolean gxPrTextInit(int nHandle[], int nOutputMode, int nxPage[], int nyPage[], String psIniFile, String psForm, String sPrinter, int nMode, int nPaperLength, int nPaperWidth, int nGridX, int nGridY, int nPageLines)
	{
		return GxPrTextInit(nHandle, nOutputMode, nxPage, nyPage, psIniFile, psForm, sPrinter, nMode, nPaperLength, nPaperWidth, nGridX, nGridY, nPageLines);
	}
	
	public boolean gxPrnCmd(int handle, String cmd)
	{
		return GxPrnCmd(cmd, handle);
		//handle, cmd);
	}

	public boolean gxSetDocName(int handle, String cmd)
	{
		try
		{
			cmd = new String(cmd.getBytes(), "8859_1");			
		}catch(java.io.UnsupportedEncodingException e) { ; }
		boolean ret = GxSetDocName(handle, cmd);
		return ret;
	}

	public boolean gxStartDoc(int handle)
	{
		return StartDocument(handle);
	} // end gxStartDocument

	public boolean gxIsAliveDoc(int nHandle)
	{
		return GxIsAliveDoc(nHandle);
	} // end gxIsAlive

	public boolean gxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength)
	{
		return GxSetTextMode(nHandle, nGridX, nGridY, nPageLength);
	}

	public boolean gxPrInit( int handle[],
		int nOutputMode,
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
		int duplex )
	{
		return GxPrInit( handle, nOutputMode, xPage, yPage, dummy, iniFile, form, printer, mode, orientation, paperSize, paperLength, paperWidth, scale, copies, defaultSource, printQuality, color, duplex );		
	} // end gxPrnIni

	public boolean gxStartPg( int handle, int flag )
	{
		return GxStartPg( handle, flag );
	} // end gxStartPg

	public boolean gxEndPg( int handle, int flag )
	{
		return GxEndPg( handle, flag );
	} // end gxEndPg

	public boolean gxEndDoc( int handle, int flag )
	{
		return GxEndDoc( handle, flag );
	} // end gxEndPg

	public boolean gxEndPrn( int handle, int flag )
	{
		return GxEndPrn( handle, flag );
	} // end gxEndPg

	public boolean gxDwText( int handle,
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
		int backColB )
	{
		try
		{
			text = new String(text.getBytes(), "8859_1");
			fontName = new String(fontName.getBytes(), "8859_1");
		}catch(java.io.UnsupportedEncodingException e) { ; }
		return GxDwText( handle, text, left, top, right, bottom, fontName, fontSize, textFlags, bold, italic, underline, strikeOut, pen, frontColR, frontColG, frontColB, backMode, backColR, backColG, backColB );
	} // end gxDwText

	public boolean gxDwTextNB( int handle,
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
		int frontColB )
	{
		try
		{
			text = new String(text.getBytes(), "8859_1");
			fontName = new String(fontName.getBytes(), "8859_1");
		}catch(java.io.UnsupportedEncodingException e) { ; }
		return GxDwTextNB( handle, text, left, top, right, bottom, fontName, fontSize, textFlags, bold, italic, underline, strikeOut, pen, frontColR, frontColG, frontColB );
	} // end gxDwText

	public boolean gxDwRect( int handle,
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
		int backColB )
	{
		return GxDwRect( handle, left, top, right, bottom, pen, frontColR, frontColG, frontColB, backMode, backColR, backColG, backColB );
	} // end gxDwText

	public boolean gxDwRectNB( int handle,
		int left,
		int top,
		int right,
		int bottom,
		int pen,
		int frontColR,
		int frontColG,
		int frontColB )
	{
		return GxDwRectNB( handle, left, top, right, bottom, pen, frontColR, frontColG, frontColB );
	} // end gxDwTextNB

	public boolean gxDwBMap( int handle,
		String name,
		int left,
		int top,
		int right,
		int bottom )
	{
		try
		{
			name = new String(name.getBytes(), "8859_1");
		}catch(java.io.UnsupportedEncodingException e) { ; }
		return GxDwBMap( handle, name, left, top, right, bottom );
	} // end gxDwBMap

	public boolean gxDwLine( int handle,
		int left,
		int top,
		int right,
		int bottom,
		int pen,
		int frontColR,
		int frontColG,
		int frontColB )
	{
		return GxDwLine( handle, left, top, right, bottom, pen, frontColR, frontColG, frontColB );
	} // end gxDwLine

// ------------------------------------------------------------------
// Native code
// ------------------------------------------------------------------
	private static native boolean GxRVSetLanguage(String lang);
	private static native boolean GxShutdown();
	private static native boolean GxIsAlive();
	private static native boolean GxIsAliveDoc(int nHandle);
	private static native boolean GxRptMng();
	private static native boolean GxRptWndNormal();
	private static native boolean GxRptWndMaximize();
	private static native boolean GxRptWndOnTop();
	private static native boolean GxPrnCfg( String psIni );
	private static native boolean StartDocument(int handle);
	private static native boolean GxPrInit( int nHandle[], int nOutputMode, int nxPage[], int nyPage[], int dummy, String psIniFile, 
						String psForm, String psPrinter, 
						int nMode, int nOrientation, int nPaperSize, int nPaperLength, 
						int nPaperWidth, int nScale, int nCopies, int nDefaultSource , 
						int nPrintQuality , int nColor, int nDuplex);
	
	private static native boolean GxStartPg( int handle, int flag );
	private static native boolean GxEndPg( int handle, int flag );
	private static native boolean GxEndDoc( int handle, int flag );
	private static native boolean GxEndPrn( int handle, int flag );
	private static native boolean GxDwText( 
								int nHandle, String psText, int nleft, int ntop, int nright, int nbottom, 
								String psFace, int nFontSize, int nTextFlags, boolean bBold, 
								boolean bItalic, boolean bUnderline, boolean bStrikeOut, int nPen,	
								int nFrontColR, int nFrontColG, int nFrontColB, 
								int nBackMode, int nBackColR, int nBackColG, int nBackColB );

		
	private static native boolean GxDwTextNB( int handle, String psText, int nleft, int ntop, int nright, int nbottom, String psFace, int nFontSize, int nTextFlags, boolean bBold, boolean bItalic, boolean bUnderline, boolean bStrikeOut, int nPen, int nFrontColR, int nFrontColG, int nFrontColB );

	private static native boolean GxDwRect( int handle, int nleft, int ntop, int nright, int nbottom, int nPen, int nFrontColR, int nFrontColG, int nFrontColB, int nBackMode, int nBackColR, int nBackColG, int nBackColB );

	private static native boolean GxDwRectNB( int handle, int nleft, int ntop, int nright, int nbottom, int nPen, int nFrontColR, int nFrontColG, int nFrontColB );

	private static native boolean GxDwBMap( int handle, String psText, int nleft, int ntop, int nright, int nbottom );

	private static native boolean GxDwLine( int handle, int nleft, int ntop, int nright, int nbottom, int nPen, int nFrontColR, int nFrontColG, int nFrontColB );

	private static native boolean GxPrnCmd( String cmd, int handle);

	private static native boolean GxSetDocFormat( int handle, int format);
	private static native boolean GxSetDocName( int handle, String docName);

	private static native boolean GxSetTextMode(int nHandle, int nGridX, int nGridY, int nPageLength);

	private static native boolean GxPrTextInit(int nHandle[], int nOutputMode, int nxPage[], int nyPage[], String psIniFile,
						String psForm, String sPrinter, int nMode, int nPaperLength,
						int nPaperWidth, int nGridX, int nGridY, int nPageLines);
	private static native boolean GxOpenDoc(String fileName);
	private static native boolean GxRptSilentMode();
	private static native int GxGetMainFrameHandle();

	private static native boolean GxRVSetEnableSaveAs(short enableSaveAs);
	private static native boolean GxRVSetEnableOpen(short enableOpen);
} 
