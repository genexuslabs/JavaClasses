// $Log: GXReportText.java,v $
// Revision 1.5  2006/01/31 13:36:31  iroqueta
// Los form feed solo se mandan si el output es PRN (SAC 18548)
//
// Revision 1.4  2005/10/25 21:18:06  alevin
// - Escribo los Form Feeds en ASCII.
//
// Revision 1.3  2005/10/04 17:29:48  gusbro
// - Agrego un constructor nuevo que recibe un boolean para saber si ejecutar en una nueva UTL
//
// Revision 1.2  2005/06/24 18:03:48  gusbro
// - Convierto los \\n en newlines de la plataforma
//
// Revision 1.1  2001/10/29 12:44:14  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/10/29 12:44:14  gusbro
// GeneXus Java Olimar
//
package com.genexus.reports;

import java.io.*;
import com.genexus.*;
import com.genexus.util.*;
import com.genexus.webpanels.HttpContextWeb;
import com.genexus.internet.HttpContext;
import com.genexus.platform.*;
import javax.print.*;
import java.io.FileNotFoundException;

public abstract class GXReportText extends GXProcedure
{
	protected int lineHeight;
   	protected int Gx_line;
   	protected int P_lines;
   	protected int gxXPage;
   	protected int gxYPage;
   	protected int Gx_page;
   	protected String Gx_out = ""; // Esto est� asi porque no me pude deshacer de una comparacion contra Gx_out antes del ask.
	protected String fileName = "";
	protected boolean printAtClient = false;
	
	protected java.io.PrintWriter out;
	public GXReportText(int remoteHandle, ModelContext context, String location)
	{
		this(false, remoteHandle, context, location);		

		out = new PrintWriter(new OutputStreamWriter(new NullOutputStream()));
	}

	public GXReportText(boolean inNewUTL, int remoteHandle, ModelContext context, String location)
	{
		super(inNewUTL, remoteHandle, context, location);
	}

	protected void setOutput(PrintStream outStream)
	{
		out = new AsciiPrintWriter(outStream);
	}

	protected void setOutput(String fileName)
	{
		try
		{
			this.fileName = fileName;
			if (!printAtClient)
			{
				if(!new File(fileName).isAbsolute())
				{
					if (ModelContext.getModelContext() != null) 
					{
						HttpContext webContext = ModelContext.getModelContext().getHttpContext();
						if ((webContext != null) && (webContext instanceof HttpContextWeb))
						{
							this.fileName = com.genexus.ModelContext.getModelContext().getHttpContext().getDefaultPath() + File.separator + fileName;
						}
					}					
				}
			}
			out = new AsciiPrintWriter(new BufferedWriter(new FileWriter(this.fileName)));
		}
		catch (IOException e)
		{
			System.err.println("Can't open file " + this.fileName);
			setOutput(System.out);
		}
	}
	
		protected void setPrintAtClient()
		{
			setPrintAtClient("");
		}
		
        protected void setPrintAtClient(String printerRule)
        {
			printAtClient = true;
            String blobPath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
            String fileName = com.genexus.PrivateUtilities.getTempFileName(blobPath, "clientReport", "txt");			
			
			setOutput(fileName);
            if (httpContext != null)
            {
                httpContext.printReportAtClient(fileName, printerRule);
            }
            com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(fileName);
        }	
	
	class AsciiPrintWriter extends PrintWriter
	{
		public String eol = System.getProperty("line.separator", "\r\n");
        public String formFeed = "\u000C";

		public AsciiPrintWriter(PrintStream out)
		{
			super(out);
		}
		
		public AsciiPrintWriter(Writer writer) throws IOException 
		{
			super(writer);
		}
		
		public void print(String str)
		{
            str = CommonUtil.strReplace(str, "\n", eol);
			if (Gx_out.equals("PRN"))
			{
				str = CommonUtil.strReplace(str, "\f", formFeed);
			}
			else
			{
				str = CommonUtil.strReplace(str, "\f", "");
			}
            super.print(str);
		}
		
		public void println(String str)
		{
            str = CommonUtil.strReplace(str, "\n", eol);
            if (Gx_out.equals("PRN"))
			{
				str = CommonUtil.strReplace(str, "\f", formFeed);
			}
			else
			{
				str = CommonUtil.strReplace(str, "\f", "");
			}
            super.println(str);
		}
		
		public void close()
		{
			super.close();
			if (Gx_out.equals("PRN") && !printAtClient)
			{
				if(!NativeFunctions.isWindows())
				{
					try
					{
						String [] cmd = new String[] { "lp", fileName};
						Runtime.getRuntime().exec(cmd);
					} 
					catch(Exception e)
					{
						e.printStackTrace(); 
					}
				}
				else
				{
					try
					{
						PrintService ps = getDefaultPrinter();
						FileInputStream fInput = new FileInputStream(fileName);
						DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
						DocPrintJob pj = ps.createPrintJob();
						Doc doc = new SimpleDoc(fInput, flavor, null);			
						pj.print(doc, null);			
					}
					catch (FileNotFoundException e1)
					{	
						System.out.println("File " + fileName + " not found: " + e1.getMessage());
					}
					catch (PrintException e) 
					{
						System.out.println("Error printing report " + fileName + " " + e.getMessage());
					}					
				}
			}				
		}
		
		private PrintService getDefaultPrinter()
		{
			PrintService ps = PrintServiceLookup.lookupDefaultPrintService();
			if (ps == null)
			{
				PrintService[] ps1 = PrintServiceLookup.lookupPrintServices(null, null);
				ps = ps1[0];
			}
			return ps;
		}
	}
}