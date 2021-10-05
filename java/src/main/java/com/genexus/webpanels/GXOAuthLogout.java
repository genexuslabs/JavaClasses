package com.genexus.webpanels;

import com.genexus.Application;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.security.GXSecurityProvider;

public class GXOAuthLogout extends GXWebObjectStub
{   
	protected void doExecute(HttpContext context) throws Exception
	{	
		new WebApplicationStartup().init(Application.gxCfg, context);
		
		context.setStream();
		try
		{			
			ModelContext modelContext =  new ModelContext(Application.gxCfg);
			modelContext.setHttpContext(context);
			ModelContext.getModelContext().setHttpContext(context);
			
			GXSecurityProvider.getInstance().oauthlogout(-2, modelContext);
			
			context.getResponse().setContentType("application/json");
			context.getResponse().setStatus(200);
			context.writeText("{}");
			context.getResponse().flushBuffer();
			return;			
		}
		catch (Throwable e)
		{
			context.sendResponseStatus(500, e.getMessage());
		}
	}      

	protected boolean IntegratedSecurityEnabled( )
	{
		return com.genexus.Application.getClientPreferences().getProperty("EnableIntegratedSecurity", "0").equals("1");
	}
	
	protected int IntegratedSecurityLevel( )
	{
		return SECURITY_GXOBJECT;
	}
	
	protected String IntegratedSecurityPermissionPrefix( )
	{
		return "";
	}

	protected String EncryptURLParameters() {return "NO";};
	
   protected void init(HttpContext context )
   {
   }	   
}