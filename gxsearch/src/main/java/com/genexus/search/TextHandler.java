package com.genexus.search;

import java.io.*;

public class TextHandler implements IDocumentHandler
{
        public String getText(String filename)
        {
			try (FileReader rd = new FileReader(filename))
			{
				File f = new File(filename);
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
