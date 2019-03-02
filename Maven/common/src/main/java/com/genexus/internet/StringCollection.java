// $Log: StringCollection.java,v $
// Revision 1.2  2005/07/26 21:07:05  iroqueta
// El add estaba filtrando que no se agregara si el valor era vacio.
// Eso lo saque porque sino no funcionan los combos dinamicos con desc vacias.
// Si da algun problema habria que hacer este mismo filtro antes de llamar al add en lugar de dentro del add.
//
// Revision 1.1  2001/07/16 18:57:42  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/07/16 18:57:42  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;
import com.genexus.util.*;

import json.org.json.*;

import com.genexus.CommonUtil;
import com.genexus.SdtMessages_Message;
import com.genexus.GXBaseCollection;
import java.util.StringTokenizer;
import java.util.Enumeration;

public class StringCollection implements IGxJSONAble
{
	JSONArray jsonArr = new JSONArray();
	FastVector vector = new FastVector();

	public void add(String value)
	{
		//if	(value.trim().length() != 0)
			vector.addElement(value);
	}
	
	public void insertAt(int position, String value)
	{
		vector.insertElementAt(value, position);
	}

	public void removeAllItems()
	{
		vector.removeAllElements();
	}

	public void clear()
	{
		vector.removeAllElements();
	}


	public String item(int idx)
	{
		return (String) vector.elementAt(idx - 1);
	}

	public int getCount()
	{
		return vector.size();
	}
	
	public String toString()
	{
		String result = "";
		for (int i = 1; i <= getCount(); i++)
		{
			result = result + item(i);
		}
		return result;
	}

	String getString()
	{
		StringBuffer b = new StringBuffer();

		for (Enumeration en = vector.elements(); en.hasMoreElements();)
		{
			b.append( (String) en.nextElement());
			if	(en.hasMoreElements())
				b.append(";");
		}

		return b.toString();
	}

	static StringCollection getFromString(String list)
	{
		StringCollection ret = new StringCollection();
		StringTokenizer tokenizer = new StringTokenizer(list, ";");

   		while (tokenizer.hasMoreTokens()) 
			ret.add(tokenizer.nextToken());

		return ret;
	}
	public boolean fromJSonString(String list)
	{
		return fromJSonString(list, null);
	}	
	public boolean fromJSonString(String list, GXBaseCollection<SdtMessages_Message> messages)
	{
		try{
			if	(list.startsWith("[") && list.endsWith("]"))
			{
				StringTokenizer tokenizer = new StringTokenizer(list.substring(1, list.length() -1), ",");
	   			while (tokenizer.hasMoreTokens())
					add(removeStringDelimiters(tokenizer.nextToken()));
			}
			return true;
		}
		catch(Exception ex)
		{
			CommonUtil.ErrorToMessages("fromjson error", ex.getMessage(), messages);
			return false;
		}
	}
	
	public String removeStringDelimiters(String token)
	{
		if	(token.startsWith("\"") && token.endsWith("\""))
		{
			token = token.substring(1, token.length() -1);
		}
		return token;
	}	
	
    public void tojson()
    {
        jsonArr = new JSONArray();
        for (int i = 0; i < getCount(); i++)
        {
            AddObjectProperty(vector.elementAt(i));
        }
    }
    public void AddObjectProperty(String name, Object prop)
    {
        AddObjectProperty(prop);
    }
    public void AddObjectProperty(Object prop)
    {
        if (prop instanceof IGxJSONAble)
        {
            jsonArr.put(((IGxJSONAble)prop).GetJSONObject());
        }
        else
        {
            jsonArr.put(prop.toString());
        }
    }

    public Object GetJSONObject(boolean includeState)
    {
		return GetJSONObject();
    }
    public Object GetJSONObject()
    {
        tojson();
        return jsonArr;
    }

    public void FromJSONObject(IJsonFormattable obj)
    {
    }

    public String ToJavascriptSource()
    {
        return GetJSONObject().toString();
    }
}

