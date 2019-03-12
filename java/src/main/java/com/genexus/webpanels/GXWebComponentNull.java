package com.genexus.webpanels;

import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;

public final class GXWebComponentNull extends GXWebComponent
{
	public GXWebComponentNull(HttpContext httpContext)
	{
		super(httpContext);
	}

	public GXWebComponentNull(int remoteHandle, ModelContext context)
	{
		super(remoteHandle, context);
	}

	public boolean isMasterPage()
	{
		return false;
	}

	
	public void webExecute() {}
	public void setPrefix(String prefix) {}
	protected void createObjects() {}
	public void initialize() {}
	public void componentstart() {}
	public void componentdraw() {}
	public void componentprocess(String prefix, String sPSFPrefix, String sCompEvt ) {}
	public String componentgetstring( String sGXControl ) { return ""; }
	public void componentbind(Object[] parms) {}	
	public void componentprepare(Object[] parms) {}

	public void initialize_properties() {
	}

}
