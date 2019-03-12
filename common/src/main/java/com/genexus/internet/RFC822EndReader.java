package com.genexus.internet;

import java.io.*;

class RFC822EndReader extends BufferedReader implements GXInternetConstants
{
	private String lastLine;
	private boolean isEndOfMessage = false;
	private PrintStream logOutput;

	public RFC822EndReader(Reader reader, PrintStream logOutput)
	{
		super(reader);
		this.logOutput = logOutput;
	}

	private int log(int line)
	{
		if	(DEBUG)
		{
			if (logOutput != null) 
				logOutput.println("byte: " + line);
		}

		return line;
	}

	private String log(String line)
	{
		if	(DEBUG)
		{
			if (logOutput != null) 
				logOutput.println("Line: " + line);
		}

		return line;
	}

	public int read() throws IOException
	{
		if	(isEndOfMessage)
			return -1;

		return log(super.read());
	}

	public String readLine() throws IOException
	{
		if	(isEndOfMessage)
			return null;

		String out;

		if	(lastLine == null)
		{
			out = log(super.readLine());
		}
		else
		{
			out = lastLine;
			lastLine = null;
		}
		
		if	(out != null)
		{
			if	(out.equals(""))
			{
				lastLine = log(super.readLine());
				if	(lastLine.equals("."))
				{
					isEndOfMessage = true;
					out = null;
					lastLine = null;
				}
			}
			else if (out.startsWith("."))
			{
				// El exchange no manda un LF antes del ".", asi que puede estar
				// enseguida..
				if	(out.equals("."))
				{
					isEndOfMessage = true;
					out = null;
					lastLine = null;
				}
				else
				{
					out = out.substring(1, out.length());					
				}
			}
		}

		return out;
	}	
}
