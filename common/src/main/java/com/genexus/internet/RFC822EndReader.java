package com.genexus.internet;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

import java.io.*;

public class RFC822EndReader extends BufferedReader implements GXInternetConstants
{
	private String lastLine;
	private boolean isEndOfMessage = false;
	private static final ILogger logger = LogManager.getLogger(RFC822EndReader.class);

	public RFC822EndReader(Reader reader)
	{
		super(reader);
	}

	private int log(int line)
	{
		logger.debug("byte: " + line);
		return line;
	}

	private String log(String line)
	{
		logger.debug("Line: " + line);

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
