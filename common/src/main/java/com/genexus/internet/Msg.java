package com.genexus.internet;

import com.genexus.json.JSONObjectWrapper;

public class Msg implements java.io.Serializable, IGxJSONAble
{
  protected String description;
  protected  String id;
  protected int type;
  protected String att;
  protected boolean gxMessage;
  protected JSONObjectWrapper jsonObj = new JSONObjectWrapper();

  public Msg( String description, String id, int type, String att)
  {
    this.description = description;
    this.id = id;
    this.type = type;
    this.att = att;
	this.gxMessage = false;
  }
  
  public Msg( String description, String id, int type, String att, boolean gxMessage)
  {
    this.description = description;
    this.id = id;
    this.type = type;
    this.att = att;
	this.gxMessage = gxMessage;
  }  

  public String getDescription()
  {
      return description;
  }
  public String getId()
  {
      return id;
  }
  public int getType()
  {
      return type;
  }
  public String getAtt()
  {
      return att;
  }
  public boolean getGXMessage()
  {
      return gxMessage;
  }  
  public String ToJavascriptSource()
  {
      return GetJSONObject().toString();
  }

  public void tojson()
  {
      AddObjectProperty("id", getId());
      AddObjectProperty("text", getDescription());
      AddObjectProperty("type", new Integer(getType()));
      AddObjectProperty("att", getAtt());
  }

  public void AddObjectProperty(String name, Object prop)
  {
      try
      {
          jsonObj.put(name, prop);
      }
      catch(Exception e) {}
  }

  public Object GetJSONObject(boolean includeState)
  {
	return GetJSONObject();
  }
  public Object GetJSONObject()
  {
      tojson();
      return jsonObj;
  }

  public void FromJSONObject(Object obj)
  {
  }
}
