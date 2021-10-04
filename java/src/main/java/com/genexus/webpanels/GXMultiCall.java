package com.genexus.webpanels;

import java.io.InputStream;
import java.lang.reflect.Method;

import com.genexus.Application;
import com.genexus.GXutil;
import com.genexus.GxRestService;
import com.genexus.ModelContext;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.ws.rs.core.Response;

import json.org.json.JSONArray;

@javax.ws.rs.Path("/gxmulticall")
@jakarta.ws.rs.Path("/gxmulticall")
public class GXMultiCall extends GxRestService
{
	private final static String METHOD_EXECUTE = "execute";

	@javax.ws.rs.POST
	@jakarta.ws.rs.POST
	@javax.ws.rs.Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON})
	@jakarta.ws.rs.Consumes({jakarta.ws.rs.core.MediaType.APPLICATION_JSON})
	@javax.ws.rs.Produces({javax.ws.rs.core.MediaType.APPLICATION_JSON + ";charset=UTF-8"})
	@jakarta.ws.rs.Produces({jakarta.ws.rs.core.MediaType.APPLICATION_JSON + ";charset=UTF-8"})
    public Object gxMultiCall() throws Exception
    {
		super.init( "POST" );
		String procName = ((HttpContextWeb) context.getHttpContext()).GetNextPar().toLowerCase();

		ModelContext modelContext = ModelContext.getModelContext(Application.gxCfg);
		String appPackage = modelContext.getPackageName();
		if (!appPackage.equals(""))
		{
			appPackage = appPackage + ".";
		}
		
		String restProcName = procName + "_services_rest";
		try
		{
			Class myClassRest = Class.forName(appPackage + restProcName);
		}
		catch(ClassNotFoundException cnf)
		{
			builder = Response.notFound();
			cleanup();
			return builder.build() ;
		}
		
		String jsonStr = parsePostData( ((HttpContextWeb) context.getHttpContext()).getHttpRequest().getContentLength(), ((HttpContextWeb) context.getHttpContext()).getHttpRequest().getInputStream());
		
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

		builder = Response.okWrapped();
		builder.type("application/json");
		builder.entity("");
		cleanup();
		return builder.build() ;
    }
	
   protected boolean IntegratedSecurityEnabled( )
   {
      return false;
   }	
   
   protected int IntegratedSecurityLevel( )
   {
      return 0;
   }
	
   protected String EncryptURLParameters() {return "NO";};
   
   private String parsePostData(int len, InputStream in)
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
	   }
   }
}
