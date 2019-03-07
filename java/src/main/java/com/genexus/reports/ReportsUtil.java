// $Log: ReportsUtil.java,v $
// Revision 1.1  2000/03/23 17:58:24  gusbro
// Initial revision
//
// Revision 1.1.1.1  2000/03/23 17:58:24  gusbro
// GeneXus Java Olimar
//
package com.genexus.reports;

public class ReportsUtil
{
	public static void gxsetfrm()
	{
		new GXReportViewerThreaded().GxPrnCfg("GXPRN.INI");
	}
}