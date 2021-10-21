package com.genexus.webpanels;

import com.genexus.internet.HttpContext;
import com.genexus.internet.StringCollection;

public class GXValidService extends GXWebObjectStub
{   
    protected void doExecute(HttpContext context) throws Exception
    {
		context.doNotCompress(true);
		String[] elements = context.GetNextPar().split("&");
		String jsonResponse ="";
		String appPackage = context.getPackage();
		String object = elements[0].substring(7).toLowerCase() + "_impl";
		String att = elements[1].substring(4);
		String methodName = "rest_" + att.toLowerCase();
		StringCollection parms = new StringCollection();
		
		int index = 2 ;
		while ( index < elements.length)
		{
			parms.add(elements[index].substring(elements[index].indexOf("=")+1));
			index++;
		}
        
		Class<?> trn = Class.forName(appPackage + object);
		Object constructed = trn.getConstructor(new Class<?>[] {HttpContext.class}).newInstance(new Object[] {context});
		Object objectResponse = constructed.getClass().getMethod(methodName, new Class[] {StringCollection.class}).invoke(constructed, new Object[]{parms});
		jsonResponse = (String)objectResponse;
		context.setStream();
		
		try
		{
			context.getResponse().setContentType("application/json");
			context.getResponse().setStatus(200);
			context.writeText(jsonResponse);
			context.getResponse().flushBuffer();
			return;			
		}
		catch (Throwable e)
		{
			context.sendResponseStatus(404, e.getMessage());
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

   protected String EncryptURLParameters() {return "NO";};
   
   protected void init(HttpContext context )
   {
   }      
}
