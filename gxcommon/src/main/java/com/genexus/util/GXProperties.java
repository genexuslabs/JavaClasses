package com.genexus.util;

import java.util.Vector;

import com.genexus.internet.IGxJSONSerializable;

import json.org.json.*;

import com.genexus.CommonUtil;
import com.genexus.SdtMessages_Message;
import com.genexus.GXBaseCollection;
import java.util.Iterator;

public class GXProperties implements IGxJSONSerializable{
  private Vector<GXProperty> vector = new Vector<GXProperty>();
  private boolean eof;
  private int lastElement;

  public GXProperties() {
  }

  public void set(String name, String value)
  {
	  put(name, value);
  }
  
  public void add(String name, String value)
  {
	  addToTheEnd(name, value);
  }
  
  public void put(String name, String value)
  {
	  int index = findElement(name);
	  if ( index >= 0)
	  {
		  vector.elementAt(index).setValue(value);
	  }
	  else
	  {
		  addToTheEnd(name, value);
	  }
  }
  public String toString() {
	  StringBuilder builder = new StringBuilder();
	  for (GXProperty property : vector) {
		  builder.append(property.getValue());
	  }
	  return builder.toString();
  }
  private void addToTheEnd(String name, String value){

	  GXProperty prop = new GXProperty();
	  prop.setKey(name);
	  prop.setValue(value);
	  vector.addElement(prop);
  }
  public String get(String name)
  {
	  int index = findElement(name);
	  if (index >= 0)
		return vector.elementAt(index).getValue();
	  else
		  return "";
  }
  
  public void remove(String name)
  {
	  int index = findElement(name);
	  if (index >= 0)
		  vector.removeElementAt(index);
  }

  public boolean containsKey(String name)
  {
	  if (findElement(name) == -1)
		  return false;
	  return true;
  }

  private int findElement(String name)
  {			  
	  int i = 0;
	  while (count() > i)
	  {
		  if (item(i).getKey().equalsIgnoreCase(name))
			  return i;
		  i++;
	  }
	  return -1;
  }

  public GXProperty item(int i)
  {
    return vector.elementAt(i);
  }

  public int getCount()
	{
		return count();
	}

  public int count()
  {
    return vector.size();
  }

  public void clear()
  {
    vector.removeAllElements();
  }
  
  public GXProperty first()
  {
	  eof = false;
	  if (count() > 0)
	  {
		  lastElement = 0;
		  return vector.elementAt(0);
	  }
	  else
	  {
		  eof = true;
		  return null;
	  }
  }

  public boolean eof()
  {
	  return eof;
  }
  
  public GXProperty next()
  {
	  lastElement ++;
	  if (count() > lastElement)
	  {
		  return vector.elementAt(lastElement);
	  }
	  else
	  {
		  eof = true;
		  return null;
	  }	  
  }
  
	public Object GetJSONObject()  
	{
		JSONObject jObj = new JSONObject();
		int i = 0;
		while (count() > i)
		{
			GXProperty prop = item(i);
			try {
				jObj.put(prop.getKey(), prop.getValue());			
			} catch (JSONException e) {
			}
			i++;
		}
		return jObj; 		
	}
  
	public String toJSonString()
	{
		JSONObject jObj = (JSONObject)GetJSONObject();
		return jObj.toString();
	}
	public boolean fromJSonString(String s)
	{
		return fromJSonString(s, null);
	}	
	public boolean fromJSonString(String s, GXBaseCollection<SdtMessages_Message> messages)
	{
		this.clear();		
		if (!s.equals("")) {
			try {
				JSONObject jObj = new JSONObject(s);
				Iterator<String> keys = jObj.keys();
				while( keys.hasNext() ) {
					String key = keys.next();
					this.put(key, jObj.get(key).toString());
				}
				return true;
			} 
			catch (JSONException ex) 
			{
				CommonUtil.ErrorToMessages("fromjson error", ex.getMessage(), messages);
				return false;
			}
		}
		else
		{
			CommonUtil.ErrorToMessages("fromjson error", "empty string", messages);
			return false;
		}
	}
}
