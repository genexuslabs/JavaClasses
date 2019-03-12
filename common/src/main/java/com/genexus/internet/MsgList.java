package com.genexus.internet;

import java.util.Vector;

import com.genexus.SdtMessages_Message;

import json.org.json.*;

import com.genexus.GXBaseCollection;

public class MsgList extends java.util.Vector implements IGxJSONAble
{
    JSONArray jsonArr = new JSONArray();
	final byte MESSAGE_TYPE_ERROR = 1;
	final byte MESSAGE_TYPE_WARNING = 0;
	final String MESSAGE_SUCADDED = "SuccessfullyAdded";
	final String MESSAGE_UPDATED = "SuccessfullyUpdated";
	final String MESSAGE_DELETED = "SuccessfullyDeleted";

	public void append(GXBaseCollection<SdtMessages_Message> list)
	{
		for (SdtMessages_Message item : list)
		{
			if (!(item.getgxTv_SdtMessages_Message_Type()==MESSAGE_TYPE_WARNING && item.getgxTv_SdtMessages_Message_Id().equals(MESSAGE_SUCADDED)))
				addItem(item.getgxTv_SdtMessages_Message_Description(), item.getgxTv_SdtMessages_Message_Type(), item.getgxTv_SdtMessages_Message_Id());
		}
	}
	public byte anyError()
	{
		for (Object item : this) {
			if (((Msg)item).getType()==MESSAGE_TYPE_ERROR)
					return MESSAGE_TYPE_ERROR;
		}
		return MESSAGE_TYPE_WARNING;
	}
	public void addItem(String description, String id, int type, String att)
	{
	  addElement( new Msg( description, id, type, att));
	}
	
	public void addItem(String description, String id, int type, String att, boolean gxMessage)
	{
	  addElement( new Msg( description, id, type, att, gxMessage));
	}	

	public void addItem(String description, int type, String att)
	{
	  addElement( new Msg( description, "", type, att));
	}
	
	public void addItem(String description, int type, String att, boolean gxMessage)
	{
	  addElement( new Msg( description, "", type, att, gxMessage));
	}	

	public void addItem(String description)
	{
	  addElement( new Msg( description, "", 0, ""));
	}
	
	public void addItem(String description, boolean gxMessage)
	{
	  addElement( new Msg( description, "", 0, "", gxMessage));
	}	

	public int getItemCount()
	{
		return size();
	}

	public short getItemType(int i)
	{
		return (short) ((Msg)elementAt(i - 1)).getType();
	}

	public String getItemText(int i)
	{
	  return ((Msg) elementAt(i - 1)).getDescription();
	}

	public String getItemValue(int i)
	{
	  return ((Msg) elementAt(i - 1)).getId();
	}

	public String getItemAtt(int i)
	{
		return ((Msg)elementAt(i - 1)).getAtt();
	}
	
	public boolean getItemGXMessage(int i)
	{
		return ((Msg)elementAt(i - 1)).getGXMessage();
	}	

	public void removeAllItems()
	{
		removeAllElements();
	}

	private short displayMode = 1;

	public void setDisplaymode(short displayMode)
	{
		this.displayMode = displayMode;
	}

	public short getDisplaymode()
	{
		return displayMode;
	}
        public String ToJavascriptSource()
        {
            return GetJSONObject().toString();
        }

        public void tojson()
        {
            jsonArr = new JSONArray();
            for (int i = 0; i < getItemCount(); i++)
            {
                AddObjectProperty(elementAt(i));
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
}
