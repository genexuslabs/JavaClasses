package com.genexus.webpanels;

import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;

public abstract class GXMasterPage extends GXWebPanel
{
  	GXDataArea DataAreaObject;
	
	private boolean _ShowMPWhenPopUp;

	public GXMasterPage(HttpContext httpContext)
	{
		super(httpContext);
	}

	/**
	 * Constructor para los est�ticos.
	 *
	 */

	public GXMasterPage(int remoteHandle, ModelContext context)
	{
		super(remoteHandle, context);
		ui.setAutoDisconnect(true);
		isStaticGeneration = true;
	}

	public void setDataArea( GXDataArea DataAreaObject)
	{
	  this.DataAreaObject = DataAreaObject;
	  this.httpContext = DataAreaObject.getHttpContext();
	  _ShowMPWhenPopUp = true;
	}

	public void setDataArea( GXDataArea DataAreaObject, boolean ShowMPWhenPopUp)
	{
	  this.DataAreaObject = DataAreaObject;
	  this.httpContext = DataAreaObject.getHttpContext();
	  _ShowMPWhenPopUp = ShowMPWhenPopUp;
	}
	
	public GXDataArea getDataAreaObject()
	{
		return DataAreaObject;
	}

	public void master_styles()
	{
	}
	
	public boolean ShowMPWhenPopUp()
	{
		return _ShowMPWhenPopUp;
	}

	public boolean isMasterPage()
	{
		return true;
	}

}
