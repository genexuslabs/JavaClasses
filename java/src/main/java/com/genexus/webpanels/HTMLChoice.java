package com.genexus.webpanels;

import java.util.HashMap;
import java.util.Vector;

import com.genexus.CommonUtil;
import com.genexus.GXutil;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;
import json.org.json.JSONException;
import json.org.json.JSONObject;

public class HTMLChoice extends HTMLObject implements IChoice
{
	private String value = "";
	protected Vector<HTMLChoiceElement> listval;
	protected boolean sortDescriptions = false;
    private boolean _IsSet = false;
    private String caption;

	public HTMLChoice()
	{
		listval = new Vector<>();
	}

	public HTMLChoice(GXWebPanel webPanel)
	{
		this();
	}

	public boolean isSelected(int idx)
	{
		if	(idx == 1)
		{
			if	((listval.elementAt(idx - 1)).key.equals(value.trim()))
				return true;

			boolean any_selected = false;
			for(int i = 0; i < listval.size(); i++)
			{
				if ((listval.elementAt(i)).key.equals(value.trim()))
				{
					any_selected = true;
					break;
				}
			}
			if	(!any_selected)
				return true;

		}

		return (listval.elementAt(idx - 1)).key.trim().equals(value.trim());
	}

	public void addItem(long value, String description)
	{
		_IsSet = true;
		int index = itemIndex("" + value);
		if (index == 0)
		{
			listval.addElement(new HTMLChoiceElement("" + value, description));
		}
		else
		{
			listval.setElementAt(new HTMLChoiceElement("" + value, description), index - 1);
		}
	}

	public void addItem(long value, String description, int index)
	{
		_IsSet = true;
		if	(index > 0 && index > listval.size())
			listval.insertElementAt(new HTMLChoiceElement("" + value, description), index - 1);
		else
			addItem(value, description);
	}

	public void addItem(String value, String description)
	{
		_IsSet = true;
		int index = itemIndex(value);
		if (index == 0)
		{
			listval.addElement(new HTMLChoiceElement(value, description));
		}
		else
		{
			listval.setElementAt(new HTMLChoiceElement(value, description), index - 1);
		}		
	}

	public void addItem(String value, String description, int index)
	{
		_IsSet = true;
		if	(index > 0 && index > listval.size())
			listval.insertElementAt(new HTMLChoiceElement(value, description), index - 1);
		else
			addItem(value, description);
	}

	public void addItem(double value, String description)
	{
		_IsSet = true;
          if (!existItem(GXutil.str(value, 15, 2).trim()))
		listval.addElement(new HTMLChoiceElement(GXutil.str(value, 15, 2).trim(), description));
	}

	public void addItem(double value, String description, int index)
	{
		_IsSet = true;
		if	(index > 0)
			listval.insertElementAt(new HTMLChoiceElement(GXutil.str(value, 15, 2).trim(), description), index - 1);
		else
			addItem(value, description);
	}

	public void setWidth(long value)
	{
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return this.value;
	}
	
	public String getCaption()
	{
		return caption;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}	

	public void removeAllItems()
	{
		_IsSet = true;
		listval.removeAllElements();
		jsonObj = new JSONObject();
	}

	public void removeItem(String key)
	{
		_IsSet = true;
		key = CommonUtil.upper(CommonUtil.trim(key));
		int index = -1;

		for (int i = 0; i < listval.size(); i++)
		{
			if	(CommonUtil.upper(CommonUtil.trim((  listval.elementAt(i)).key)).equals(key))
			{
				index = i;
			}
		}

		if	(index != -1)
		{
			listval.removeElementAt(index);
		}
	}

	public String getItemText(int ndx)
	{
		if	(ndx > 0 && ndx <= listval.size())
			return (listval.elementAt(ndx - 1)).description;

		return "";
	}

	public String getItemValue(int ndx)
	{
		if	(ndx > 0 && ndx <= listval.size())
			return (listval.elementAt(ndx - 1)).key;

		return "";
	}

        public String getValidValue(String value)
        {
            return getValidValueImp(value);
        }

        private String getValidValueImp(Object valid)
        {
            for (int i = 0; i < listval.size(); i++)
			 {
					  HTMLChoiceElement el = listval.elementAt(i);
					  if (el.key.trim().equals(valid.toString().trim()))
							  return valid.toString();
			 }
            return getItemValue(1);
        }
	
        public boolean existItem(String key)
        {
          for (int i = 0; i < listval.size(); i++)
          {
                  HTMLChoiceElement el = listval.elementAt(i);
                  if (el.key.trim().equals(key.trim()))
                          return true;
          }
          return false;
        }

        public int itemIndex(String key)
        {
	        for (int i = 0; i < listval.size(); i++)
	        {
	                HTMLChoiceElement el = listval.elementAt(i);
	                if (el.key.trim().equals(key.trim()))
	                        return i +1;
	        }
	        return 0;
        }

	public void setDescription(String s)
	{
	}

	public String getDescription()
	{
		for (int i = 0; i < listval.size(); i++)
		{
			HTMLChoiceElement el = listval.elementAt(i);
			if (el.key.trim().equals(value.trim()))
				return el.description;
		}

		return "";
	}

	public int getCount()
	{
		return getItemCount();
	}

	public int getItemCount()
	{
		return listval.size();
	}

	public void setSort(int value)
	{
		sortDescriptions = (value == 1);
	}

	//-- Metodos que no hacen nada en el web
	public void repaint() { ; }


	public void tojson()
	{
		try
		{
			jsonObj.put("isset", _IsSet);
			jsonObj.put("s", CommonUtil.trim(getValue()));
			JSONArray jsonArrValues = new JSONArray();
                        HashMap<String, JSONArray> itemsHash = new HashMap<String, JSONArray>();
			for (int i = 0; i < listval.size(); i++)
			{
                                String itemValue = CommonUtil.rtrim((listval.elementAt(i)).key);
                                if (!itemsHash.containsKey(itemValue))
                                {
                                    JSONArray jsonItem = new JSONArray();
                                    jsonItem.put(itemValue);
                                    jsonItem.put((listval.elementAt(i)).description);
                                    itemsHash.put(itemValue, jsonItem);
                                    jsonArrValues.put(jsonItem);
                                }
                                else
                                {
                                    itemsHash.get(itemValue).put(1, (listval.elementAt(i)).description);
                                }
			}
			jsonObj.put("v", jsonArrValues);
		}
		catch (JSONException e)
                {
                    e.printStackTrace();
                }
	}

        public void FromJSONObject(IJsonFormattable obj)
	{
            this.removeAllItems();
            jsonObj = (JSONObject)obj;
            try
            {
                JSONArray jsonArrValues = (JSONArray)jsonObj.get("v");
                if (jsonArrValues != null)
                {
                        for (int i=0; i<jsonArrValues.length(); i++)
                        {
                            JSONArray jsonItem = jsonArrValues.getJSONArray(i);
                            Object code = jsonItem.get(0);
                            Object desc = jsonItem.get(1);
                            if (code != null && desc != null)
                            {
                                this.addItem(code.toString(), desc.toString());
                            }
                        }
                        Object selected = jsonObj.get("s");
                        if (selected != null)
                        {
                                this.setValue(selected.toString());
                        }
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
	}
}

class HTMLChoiceElement
{
	String key;
	String description;

	HTMLChoiceElement(String key, String description)
	{
		this.key = key;
		this.description = CommonUtil.rtrim(description);
	}
}
