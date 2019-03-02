package com.genexus.internet;

import com.genexus.util.Codecs;

import json.org.json.*;

public class GXJSONObject extends JSONObject
{
	private boolean base64Encoded;

	public GXJSONObject(boolean base64Encoded)
	{
		this.base64Encoded = base64Encoded;
	}
	public boolean getBase64Encoded()
	{
		return base64Encoded;
	}
	public void setBase64Encoded(boolean value)
	{
		base64Encoded = value;
	}
	public String toString()
	{
		if (base64Encoded)
			return Codecs.base64Encode(super.toString(), "UTF8");
		else
			return super.toString();
	}

}
