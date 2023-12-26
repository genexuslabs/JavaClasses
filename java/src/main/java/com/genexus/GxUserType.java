package com.genexus;

import com.genexus.xml.GXXMLSerializable;
import java.util.HashMap;
import json.org.json.JSONObject;
import org.apache.logging.log4j.Logger;

public abstract class GxUserType extends GXXMLSerializable implements Cloneable, java.io.Serializable, IGXAssigned
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(GxUserType.class);

	public GxUserType(ModelContext context, String type)
	{
		super(-1, context, type);
	}

	public GxUserType(int remoteHandle, ModelContext context, String type)
	{
		super( remoteHandle, context, type);
	}

	boolean bIsAssigned = true;

	public abstract String getJsonMap( String value );

	@Override
	public boolean getIsAssigned() {
		return bIsAssigned;
	}

	@Override
	public void setIsAssigned(boolean bAssigned) {
		bIsAssigned = bAssigned;
	}

	protected Object getJsonObjectFromHashMap( Object userType) {
		JSONObject jsonObj = new JSONObject();
		try {
			if (userType instanceof HashMap)
				jsonObj = new json.org.json.JSONObject((HashMap)userType);
		}
		catch(Exception e) {
			log.error("Could not create Json Object", e);
		}
		return jsonObj;
	}

	protected void setHashMapFromJson(String json) {
		fromjson_(json);
	}

	protected void fromjson_(String json){}
}
