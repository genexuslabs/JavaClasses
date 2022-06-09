package com.genexus;

import com.genexus.xml.GXXMLSerializable;

public abstract class GxUserType extends GXXMLSerializable implements Cloneable, java.io.Serializable, IGXAssigned
{

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
}
