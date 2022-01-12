package com.genexus.util;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Hashtable;
import com.genexus.internet.StringCollection;


public class GXMap extends Hashtable<String, Object>
{
		public boolean hasKey(String key)
		{
			return containsKey(key);
		}
		public StringCollection getKeys()
		{
			StringCollection result = new StringCollection();
			for(Enumeration keys = keys(); keys.hasMoreElements();)
			{
				result.add((String)keys.nextElement());
			}
			return result;
		}
		public String value(String key)
		{
			if (containsKey(key))
				return (String)get(key);
			else 
				return "";
		}
		public void set(String key, String value)
		{
			put(key, value);
		}
		public GXMap difference(GXMap value)
		{
			GXMap diffDictionary = new GXMap();
			for(Enumeration keys = keys(); keys.hasMoreElements();)
			{
				String key = (String)keys.nextElement();
				if (!value.containsKey(key))
					diffDictionary.put(key, get(key));
			}
			return diffDictionary;
		}

}

