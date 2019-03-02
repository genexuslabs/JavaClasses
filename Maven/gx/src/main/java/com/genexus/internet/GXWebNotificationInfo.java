package com.genexus.internet;

import com.genexus.ModelContext;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.xml.XMLReader;
import com.genexus.xml.XMLWriter;

public final class GXWebNotificationInfo extends GXXMLSerializable
{
	public String id;
	public String object;
	public String groupName;
	public GXXMLSerializable message;

	public GXWebNotificationInfo(int arg0, ModelContext arg1,
				String arg2) {
			super(arg0, arg1, arg2);
			// TODO Auto-generated constructor stub
	}
		
	public void tojson( boolean includeState )
    {
		AddObjectProperty("Id", id);
		AddObjectProperty("Object", object);
		AddObjectProperty("GroupName", groupName);
		AddObjectProperty("Message", message.toJSonString());            
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void setObject(String obj)
	{
		this.object = obj;
	}
	
	public void setGroupName(String gName)
	{
		this.groupName = gName;
	}
	
	public void setMessage(GXXMLSerializable msg)
	{
		this.message = msg;
	}
	
	public void tojson( )
	{
	   tojson( true) ;
	}

	public String getJsonMap(String value) {return null;}
	
	public void initialize() {
		// TODO Auto-generated method stub	
	}

	
	public short readxml(XMLReader arg0, String arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void writexml(XMLWriter arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void writexml(XMLWriter arg0, String arg1, String arg2,
			boolean arg3) {
		// TODO Auto-generated method stub
		
	}
}