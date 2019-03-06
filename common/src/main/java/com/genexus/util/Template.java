package com.genexus.util;

import java.util.*;
import java.io.*;

public class Template
{
	static String start = "<#";
	static String end 	= "#>";

	private Hashtable values = new Hashtable();

	public static void main(String arg[])
	{
		Template t = new Template();
		t.addPattern("ENABLE_PLUGIN_NS"	, "true");
		t.addPattern("ENABLE_PLUGIN_IE"	, "true");
		t.addPattern("ENABLE_CAB_IE"	, "true");
		t.addPattern("IE_PLUGIN_URL"	, "http://algo");
		
		try
		{
			t.applyTemplate(new BufferedReader(new FileReader("deployment.htm")), new BufferedWriter(new FileWriter("out.htm")));
		}
		catch (IOException e)
		{
			System.err.println("E " + e);
		}
	}

	public void addPattern(String pattern, String value)
	{
		values.put(pattern.toUpperCase(), value);
	}

	private StringBuffer outLine = new StringBuffer();
	public String applyPatternLine(String line)
	{
		for (Enumeration en = values.keys(); en.hasMoreElements();)
		{
			int lastElement = 0;
			outLine.setLength(0);
			String key = (String) en.nextElement();
			String searchString =  start + key + end;
			
			int first = line.indexOf(searchString, lastElement);

			while (first >= 0)
			{
				outLine.append(line.substring(lastElement, first) + values.get(key.toUpperCase()));
				lastElement = first + searchString.length();
				first = line.indexOf(searchString, lastElement );
			}
			outLine.append(line.substring(lastElement, line.length()));

			line = outLine.toString();
		}

		return outLine.toString();
	}

	public void applyTemplate(BufferedReader reader, BufferedWriter writer) throws IOException
	{
		String line;

		while ( (line = reader.readLine()) != null)
		{
			writer.write(applyPatternLine(line));
			writer.newLine();
		}
		writer.close();
	}
}
