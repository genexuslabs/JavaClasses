package com.genexus.webpanels;

import com.genexus.Application;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.security.GXSecurityProvider;
import json.org.json.JSONObject;

public class GXOAuthLogout extends GXWebObjectStub
{   
	protected void doExecute(HttpContext context) throws Exception
	{	
		new WebApplicationStartup().init(Application.gxCfg, context);
		
		context.setStream();
		try
		{			
			String genexus_agent = context.getHeader("Genexus-Agent");

			ModelContext modelContext =  new ModelContext(Application.gxCfg);
			modelContext.setHttpContext(context);
			ModelContext.getModelContext().setHttpContext(context);			
			String[] URL = new String[] {""};
			short[] statusCode = new short[]{0};
			GXSecurityProvider.getInstance().oauthlogout(-2, modelContext, URL, statusCode);

			if (statusCode[0] == 303)
				context.getResponse().setStatus(200);
			else
				context.getResponse().setStatus(statusCode[0]);

			context.getResponse().setContentType("application/json");
			JSONObject jObj = new JSONObject();
			if (genexus_agent.equals("WebFrontend Application") && URL[0].length() > 0)
			{				
				context.getResponse().addHeader("GXLocation", URL[0]);			
				jObj.put("GXLocation", URL[0]);
			}
			else
			{
				jObj.put("code", statusCode[0]);					
			}
			((HttpContextWeb) context).writeText(jObj.toString());
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