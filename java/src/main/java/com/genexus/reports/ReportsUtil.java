
package com.genexus.reports;

public class ReportsUtil
{
	public static void gxsetfrm()
	{
		new GXReportViewerThreaded().GxPrnCfg("GXPRN.INI");
	}
}