package com.genexus.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;

import com.genexus.CommonUtil;
import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.platform.NativeFunctions;
import com.genexus.util.NullOutputStream;
import com.genexus.webpanels.HttpContextWeb;

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
						HttpContext webContext = (HttpContext) ModelContext.getModelContext().getHttpContext();
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
		
	public String setPrintAtClient()
	{
		printAtClient = true;
		String blobPath = com.genexus.Preferences.getDefaultPreferences().getBLOB_PATH();
		String fileName = com.genexus.PrivateUtilities.getTempFileName(blobPath, "clientReport", "txt");

		setOutput(fileName);
		com.genexus.webpanels.BlobsCleaner.getInstance().addBlobFile(fileName);
		return fileName;
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
					try (FileInputStream fInput = new FileInputStream(fileName))
					{
						PrintService ps = getDefaultPrinter();
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
					catch (IOException ioe)
					{
						System.out.println("Error opening file input stream of file " + fileName + " " + ioe.getMessage());
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