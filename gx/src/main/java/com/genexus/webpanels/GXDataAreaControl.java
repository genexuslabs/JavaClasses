/*$Log: GXDataAreaControl.java,v $
/*Revision 1.1.2.1  2004/12/13 19:40:17  dmendez
/*Soporte de MasterPages
/**/
package com.genexus.webpanels;

import com.genexus.*;
import com.genexus.internet.*;

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
