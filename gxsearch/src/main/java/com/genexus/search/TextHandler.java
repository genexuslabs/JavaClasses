package com.genexus.search;

import java.io.*;

public class TextHandler implements IDocumentHandler
{
        public String getText(String filename)
        {
			try
			{
				File f = new File(filename);
				FileReader rd = new FileReader(f);
				char[] buf = new char[(int)f.length()];
				rd.read(buf);
				rd.close();
				return new String(buf);
			}
			catch (IOException ex)
			{
				System.out.println(ex.getMessage());
				return "";
			}
        }
}
