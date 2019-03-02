package com.genexus.webpanels;
import com.genexus.*;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.internet.HttpContext;

import json.org.json.JSONArray;

import javax.servlet.ServletInputStream;import java.lang.reflect.*;

public class GXMultiCall extends GXWebObjectStub
{		private final static String METHOD_EXECUTE = "execute";	
    protected void doExecute(HttpContext context) throws Exception
    {
		new WebApplicationStartup().init(Application.gxCfg, context);				context.doNotCompress(true);
		String procName = context.GetNextPar().toLowerCase();
		ModelContext modelContext = ModelContext.getModelContext(Application.gxCfg);
		String appPackage = modelContext.getPackageName();
		if (!appPackage.equals(""))
		{
			appPackage = appPackage + ".";
		}
		modelContext.setHttpContext(context);		
		String restProcName = procName + "_services_rest";
		try
		{
			Class myClassRest = Class.forName(appPackage + restProcName);
		}
		catch(ClassNotFoundException cnf)
		{
			context.sendResponseStatus(404, "");
			return;
		}
		
		String jsonStr = parsePostData(context.getRequest().getContentLength(), context.getRequest().getInputStream());
		
		Class myClass = Class.forName(appPackage + procName);
		Method[] methods = myClass.getMethods();
		Class[] parameters = null;
		for(int i = 0; i < methods.length; i++)
		{
			Method method = methods[i];
			if(method.getName().equalsIgnoreCase(METHOD_EXECUTE))
			{
				parameters = method.getParameterTypes();
			}
		}

		int parmCount;
		JSONArray jsonArr = new JSONArray(jsonStr);
		UserInformation ui = Application.getConnectionManager().createUserInformation(Namespace.getNamespace(modelContext.getNAME_SPACE()));
		int remoteHandle = ui.getHandle();
		
		for(int i = 0; i < jsonArr.length(); i++)
		{
			JSONArray procParms = new JSONArray(jsonArr.getJSONArray(i).toString());
			parmCount = procParms.length();
			Object [] params = new Object[parmCount];
			for(int j = 0; j < parmCount; j++)
			{
				params[j] = GXutil.convertObjectTo(procParms.getString(j), parameters[j]);
			}
			com.genexus.db.DynamicExecute.dynamicExecute(modelContext, remoteHandle, modelContext.packageClass, appPackage + procName, params);			
		}
		
		Application.cleanupConnection(remoteHandle);
		
		context.setStream();		
				try
		{
			context.getResponse().setContentType("application/json");			context.getResponse().setStatus(200);
			context.writeText("");
			context.getResponse().flushBuffer();
			return;			
		}
		catch (Throwable e)
		{
			context.sendResponseStatus(404, e.getMessage());
		}
    }	   protected boolean IntegratedSecurityEnabled( )
   {
      return false;
   }	      protected int IntegratedSecurityLevel( )
   {
      return 0;
   }         protected String IntegratedSecurityPermissionPrefix( )
   {
      return "";
   }      private String parsePostData(int len, ServletInputStream in)
   {
	   if(len <= 0)
		   return new String();
	   if(in == null)
		   throw new IllegalArgumentException();	
	   byte postedBytes[] = new byte[len];
	   try
	   {
		   int offset = 0;
		   do
		   {
			   int inputLen = in.read(postedBytes, offset, len - offset);
			   if(inputLen <= 0)
			   {
				   throw new IllegalArgumentException ("err.io.short_read : length " + len + " read : " + offset + " Content: \n" + new String(postedBytes));
			   }
			   offset += inputLen;
		   } while(len - offset > 0);
	   }
	   catch(java.io.IOException e)
	   {
		   throw new IllegalArgumentException(e.getMessage());
	   }
	   try
	   {
		   String postedBody = new String(postedBytes, 0, len, "8859_1");
		   return postedBody;
	   }
	   catch(java.io.UnsupportedEncodingException e)
	   {
		   throw new IllegalArgumentException(e.getMessage());
	   }   }
	   
   protected void init(HttpContext context )
   {
   }	   
}
