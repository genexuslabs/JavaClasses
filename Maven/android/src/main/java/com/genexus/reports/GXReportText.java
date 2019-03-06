package com.genexus.reports;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import com.artech.base.services.AndroidContext;
import com.artech.base.services.IBluetoothPrinter;
import com.genexus.CommonUtil;
import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.util.GXFile;
import com.genexus.util.NullOutputStream;

public abstract class GXReportText extends GXProcedure
{
	protected int lineHeight;
   	protected int Gx_line;
   	protected int P_lines;
   	protected int gxXPage;
   	protected int gxYPage;
   	protected int Gx_page;
   	protected String Gx_out = ""; // Esto est? asi porque no me pude deshacer de una comparacion contra Gx_out antes del ask.
	protected String fileName = "";
	
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
            this.fileName = GXFile.convertToLocalFullPath(fileName);
			out = new AsciiPrintWriter(new BufferedWriter(new FileWriter(this.fileName)));
		}
		catch (IOException e)
		{
			System.err.println("Can't open file " + fileName);
			setOutput(System.out);
		}
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
			if (Gx_out.equals("PRN"))
			{
				try
				{
					IBluetoothPrinter btPrinter =  AndroidContext.ApplicationContext.getBluetoothPrinter();;
                    FileInputStream fInput = new FileInputStream(fileName);
                    btPrinter.print(fInput);
                    btPrinter.cleanUp();
				}
				catch (FileNotFoundException e1)
				{	
					System.out.println("File " + fileName + " not found: " + e1.getMessage());
				}
			}				
		}		
	}
}