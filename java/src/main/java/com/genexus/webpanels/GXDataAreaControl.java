package com.genexus.webpanels;

public class GXDataAreaControl
{
    GXDataArea DataAreaObject;
	
	public GXDataAreaControl()
	{
	}

	public void setDataArea(GXDataArea DataAreaObject)
	{
		this.DataAreaObject = DataAreaObject;
	}
	
	public String getPgmname()
	{
		return DataAreaObject.getPgmname();
	}

	public String getPgmdesc()
	{
		return DataAreaObject.getPgmdesc();
	}
}
