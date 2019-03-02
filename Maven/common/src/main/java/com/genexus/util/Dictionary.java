package com.genexus.util;
import java.util.StringTokenizer;
import java.util.Enumeration;

public class Dictionary
{
	FastVector vector = new FastVector();

	public void addItem(String value, String text)
	{
		for (int i = 0; i < vector.size(); i++)
		{
			if (getItemValue(i + 1).equals(value))
			{
				((String[]) vector.elementAt(i)) [1] = text;
				return;
			}
		}

		vector.addElement(new String[] { value, text });
	}

	public String getItemValue(int idx)
	{
		return ((String[]) vector.elementAt(idx-1)) [0];
	}

	public String getItemText(int idx)
	{
		return ((String[]) vector.elementAt(idx-1))[1];
	}

	public int getCount()
	{
		return vector.size();
	}
}

