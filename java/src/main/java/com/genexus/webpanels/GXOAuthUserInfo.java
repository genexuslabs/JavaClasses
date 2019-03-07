package com.genexus.webpanels;

import com.genexus.Application;
import com.genexus.ModelContext;
import com.genexus.internet.HttpContext;
import com.genexus.security.GXSecurityProvider;

public class GXOAuthUserInfo extends GXWebObjectStub
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
			
			String[] user = new String[]{""};
			boolean[] flag = new boolean[]{false};
			GXSecurityProvider.getInstance().oauthgetuser(-2, modelContext, user, flag);
			
			if(!flag[0])
			{
			}
			else
			{
				context.getResponse().setContentType("application/json");
				context.getResponse().setStatus(200);
				context.writeText(user[0]);
				context.getResponse().flushBuffer();
				return;				
			}			
		}
		catch (Throwable e)
		{
			context.sendResponseStatus(500, e.getMessage());
		}
	}      

	protected boolean IntegratedSecurityEnabled( )
	{
		return false;
	}
	
	protected int IntegratedSecurityLevel( )
	{
		return 0;
	}
	
	protected String IntegratedSecurityPermissionPrefix( )
	{
		return "";
	}
	
   protected void init(HttpContext context )
   {
   }	   
}