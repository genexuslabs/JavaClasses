package com.genexus.webpanels;

import java.util.Hashtable;

import com.genexus.ModelContext;
import com.genexus.Application;
import com.genexus.internet.HttpContext;
import com.genexus.GXRuntimeException;
import com.genexus.CommonUtil;

/**
* Tiene que extender webpanel porque se puede correr directamente como un webpanel..
*/

public abstract class GXWebComponent extends GXWebPanel
{
	public abstract void componentstart();
	public abstract void componentdraw();
	public abstract void componentprocess(String prefix, String sPSFPrefix, String sCompEvt );
   public abstract String componentgetstring( String sGXControl );

	public boolean IsUrlCreated()
	{
		return mFixedParms != null;
	}

	private boolean isServlet = false;
	private Object[] mFixedParms;
	private boolean justCreated = false;

	public boolean isMasterPage()
	{
		return false;
	}

	
	public boolean GetJustCreated()
	{
		return justCreated;
	}
	
	public void setjustcreated()
	{
		justCreated = true;
	}

  	public GXWebComponent(HttpContext httpContext)
	{
		super(httpContext);
		isServlet = true;
	}

	public GXWebComponent(int remoteHandle, ModelContext context)
	{
		super(remoteHandle, context);
	}

	private java.lang.reflect.Method getMethod(String methodName, Class gxClass)
	{
		java.lang.reflect.Method[] methods = gxClass.getMethods();
		for (int i = 0; i < methods.length; i++)
		{
			if (methods[i].getName().equals(methodName))
				return methods[i];
		}
		return null;
	}
        
        public void setparmsfromurl(String url)
        {
            int questIdx = url.indexOf("?");
            int endClass = url.indexOf("_impl");
            if (questIdx != -1)
            {
                String parmsStr = "";
                if (endClass > questIdx && endClass < url.length())
                {
                    parmsStr = url.substring(questIdx+1, endClass);
                }
                else
                {
                    parmsStr = url.substring(questIdx+1);
                }
                if (!parmsStr.equals(""))
                {
                    setParms(WebUtils.parmsToObjectArray(context, parmsStr, url));
                }
            }
        }

        public void setParms( Object[] parms)
        {
            mFixedParms = parms;
        }

        public Object getParm( Object[] parms, int index, int type)
	{
            if (mFixedParms == null)
                 return parms[index];
             if (index == 0)
             {
                 Object[] newFixedParms = new Object[mFixedParms.length + parms.length];
                 System.arraycopy(parms, 0, newFixedParms, 0, parms.length);
                 System.arraycopy(mFixedParms, 0, newFixedParms, parms.length, mFixedParms.length);
                 mFixedParms = newFixedParms;
             }
             if (index>=parms.length)//Fixed parms must be converted (when Encrypting URL Parms)
             {
                 Class[] classArr = new Class[1];
                 classArr[0] = com.genexus.CommonUtil.mapTypeToClass(type);
                 return convertparm(classArr,0, mFixedParms[index]);
             }

          return com.genexus.GXutil.convertObjectTo(mFixedParms[index], type);
        }

	public Object getParm( Object[] parms, int index)
	{
         	 return parms[index];
	}

        public void componentbind(Object[] parms)
	{
		executeMethod("componentbind", parms);
	}
	
	public void componentrestorestate(String prefix, String sPSFPrefix )
	{
	
	}

	public void componentprepare(Object[] parms)
	{
		executeMethod("componentprepare", parms);
	}

	public void componentjscripts()
	{
	}

	public void componentthemes()
	{
	}

	protected void cleanup()
	{
		if	(isServlet)
		{
			super.cleanup();
		}
	}
}

