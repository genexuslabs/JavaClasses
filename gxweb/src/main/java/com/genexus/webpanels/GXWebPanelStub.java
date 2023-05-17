package com.genexus.webpanels;

import com.genexus.ModelContext;
import com.genexus.internet.HttpAjaxContext;
import com.genexus.internet.HttpContext;
import com.genexus.servlet.ServletException;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;

public abstract class GXWebPanelStub extends GXWebObjectStub{

	public GXWebPanelStub()
	{
	}

	public GXWebPanelStub(int remoteHandle , ModelContext context)
	{
		super(remoteHandle, context);
	}

	protected void callExecute(String method, IHttpServletRequest req, IHttpServletResponse res) throws ServletException {
		HttpContext httpContext = null;
		try
		{
			httpContext = new HttpAjaxContext(method, req, res, getWrappedServletContext());
			super.callExecute(method, req, res, httpContext);
		}
		catch (Exception e)
		{
			handleException(e, httpContext);
		}
	}
}
