package com.genexus.webpanels;

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
    public Object gxMultiCall(String jsonStr) throws Exception
    {
		super.init( "POST" );

		try
		{
			GXMultiCall.callProcRest(context, jsonStr);
		}
		catch(ClassNotFoundException cnf)
		{
			builder = Response.notFound();
			cleanup();
			return builder.build() ;
		}

		builder = Response.okWrapped();
		builder.type("application/json");
		builder.entity("");
		cleanup();
		return builder.build() ;
    }

	public static void callProcRest(ModelContext context, String jsonStr) throws Exception
	{
		String procName = ((HttpContextWeb) context.getHttpContext()).GetNextPar().toLowerCase();

		ModelContext modelContext = ModelContext.getModelContext(Application.gxCfg);
		String appPackage = modelContext.getNAME_SPACE();
		if (!appPackage.equals(""))
		{
			appPackage = appPackage + ".";
		}

		String restProcName = procName + "_services_rest";

		Class myClassRest = Class.forName(appPackage + restProcName);

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
	}
	
   protected boolean IntegratedSecurityEnabled( )
   {
      return false;
   }	
   
   protected int IntegratedSecurityLevel( )
   {
      return 0;
   }
}
