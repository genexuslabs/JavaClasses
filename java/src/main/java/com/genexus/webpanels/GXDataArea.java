package com.genexus.webpanels;

import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;

public abstract class GXDataArea extends GXWebPanel
{
	public GXDataArea(HttpContext httpContext)
	{
		super(httpContext);
	}

	/*
	 *
	 * Constructor para los est�ticos.
	 *
	 */

	public GXDataArea(int remoteHandle, ModelContext context)
	{
		super(remoteHandle, context);
		ui.setAutoDisconnect(true);
		isStaticGeneration = true;
	}

	abstract public byte executeStartEvent();
	abstract public void renderHtmlHeaders();
	abstract public void renderHtmlOpenForm();
	abstract public void renderHtmlCloseForm();
	abstract public void renderHtmlContent();
	abstract public void dispatchEvents();
	abstract public String getSelfLink();
	abstract public boolean hasEnterEvent();
	abstract public GXWebForm getForm( );
	abstract public void initialize_properties( );

	protected GXMasterPage MasterPageObj;

	protected void sendSpaHeaders()
	{
		super.sendSpaHeaders();
		if (MasterPageObj != null) 
		{
			httpContext.getResponse().setHeader(GX_SPA_MASTERPAGE_HEADER, MasterPageObj.getPgmname());
		}
	}

	protected void validateSpaRequest()
	{
		// SPA is disabled if the master page of the source and target objects is different or if the source object doesn't have a master page, 
		// so the client side replaces the full content using AJAX.
		String sourceMasterPage = httpContext.getHeader(GX_SPA_MASTERPAGE_HEADER);
		if (isSpaRequest())
		{
			if (sourceMasterPage.trim().length() == 0 || sourceMasterPage.toLowerCase().compareTo(MasterPageObj.getPgmname().toLowerCase()) != 0)
			{
				httpContext.disableSpaRequest();
			}
			sendSpaHeaders();
		}
	}
}
