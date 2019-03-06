package com.genexus.search;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;

public class TextWordDocHandler implements IDocumentHandler
{
	public String getText(String filename)
	{
		try
		{
			InputStream is = new FileInputStream(filename);
			String bodyText = "";
			try
			{
				if (filename.endsWith(".doc"))
				{
					HWPFDocument doc = new HWPFDocument(is);
					WordExtractor ex = new WordExtractor(doc);
					bodyText = ex.getText();
					ex.close();
				}
				else 
				{						
					XWPFDocument docx = new XWPFDocument(is);
					XWPFWordExtractor ex = new XWPFWordExtractor(docx);
					bodyText = ex.getText();
					ex.close();
				}							
			}
			catch (Exception e)
			{
				System.out.println("Cannot extract text from a Word document" + e.getMessage());
			}
			
			is.close();
			return bodyText;
		}
		catch (IOException ex)
		{

		}
		return "";
	}

}
