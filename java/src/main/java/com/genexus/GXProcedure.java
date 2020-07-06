
package com.genexus;

import java.sql.SQLException;
import java.util.Date;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.GXDebugInfo;
import com.genexus.diagnostics.GXDebugManager;
import com.genexus.internet.HttpContext;
import com.genexus.performance.ProcedureInfo;
import com.genexus.performance.ProceduresInfo;
import com.genexus.util.ReorgSubmitThreadPool;
import com.genexus.util.SubmitThreadPool;

public abstract class GXProcedure implements IErrorHandler, ISubmitteable
{
	public abstract void initialize();

	protected transient int remoteHandle;
	public transient ModelContext context;
	protected transient LocalUtil localUtil;
	protected transient String location = "";
	protected com.genexus.internet.HttpContext httpContext;
	protected boolean isRemote;
	protected boolean disconnectUserAtCleanup = false;
	protected boolean ejbMessageCall = false;
    UserInformation ui=null;
	
	private Date beginExecute; 
	
	public static final int IN_NEW_UTL = -2;

	public GXProcedure(int remoteHandle, ModelContext context, String location)
	{
		this(false, remoteHandle, context, location);
	}

	public GXProcedure(boolean inNewUTL, int remoteHandle, ModelContext context, String location)
	{

		//JMX Counter
		beginExecute = new Date();
		ProcedureInfo pInfo = ProceduresInfo.addProcedureInfo(this.getClass().getName());
		pInfo.incCount();

		this.remoteHandle = remoteHandle;
		this.context	  = context;
		this.location	  = location;
		int parentHandle = remoteHandle;
		
		if(inNewUTL)
		{
			remoteHandle = IN_NEW_UTL;
		}
		if (context != null && context.getSessionContext() != null)
		{
			ApplicationContext.getInstance().setEJB(true);
			ApplicationContext.getInstance().setPoolConnections(true);
		}

		switch(remoteHandle)
		{
			case -1:
				ui = Application.getConnectionManager().createUserInformation(Namespace.getNamespace(context.getNAME_SPACE()));
				this.remoteHandle = ui.getHandle();
				ApplicationContext.getInstance().setMsgsToUI(false);
				
				// I have to set the 'autoDisconnect' to true, because it indicates
				// that I have to 'keep alive' the connection in case that no requests
				// are made, because the only requests that are accounted for this are
				// the 'create object', but not the methods in the objects, so if the
				// procedure spends a lot of time in processing without creating objects
				// it could be disconnected.
				ui.setAutoDisconnect(true);
				break;
			case IN_NEW_UTL:
				if(context.prefs == null && ApplicationContext.getInstance().isApplicationServer())
				{
					if(parentHandle == IN_NEW_UTL)
					{
						context.prefs = ClientPreferences.getInstance(context.packageClass);
					}
					else
					{						
						context.prefs = ServerPreferences.getInstance(context.packageClass);
						Preferences specificPrefs = (Preferences) context.getPreferences();
						specificPrefs.iniFile.setProperty(specificPrefs.defaultSection, "NAME_SPACE", Application.getConnectionManager().getUserInformation(parentHandle).getNamespace().getName());
					}
				}
				if (ApplicationContext.getInstance().isApplicationServer())
				{
					if(context.prefs.getProperty("NAME_SPACE", "").equals(""))
					{
						Preferences specificPrefs = (Preferences) context.getPreferences();
						specificPrefs.iniFile.setProperty(specificPrefs.defaultSection, "NAME_SPACE", Application.getConnectionManager().getUserInformation(parentHandle).getNamespace().getName());
					}
				}
				ui = Application.getConnectionManager().createUserInformation(Namespace.getNamespace(context.getNAME_SPACE()));
				this.remoteHandle = ui.getHandle();
				disconnectUserAtCleanup = true;
				break;
			default:
				ui = Application.getConnectionManager().getUserInformation(remoteHandle);
		}

		localUtil    	  = ui.getLocalUtil();
		if (context != null)
		{
			httpContext = (HttpContext) context.getHttpContext();
			httpContext.initClientId();
		}
	}

	public Object me()
	{
		return this;
	}
	public void handleError()
	{
		new DefaultErrorHandler().handleError(context, remoteHandle);
	}

	public int getHandle()
	{
		return remoteHandle;
	}

	public ModelContext getContext()
	{
		return context;
	}

	protected boolean isRemoteProcedure()
	{
		return Application.isRemoteProcedure(context, remoteHandle, location);
	}

	protected void exitApplication()
	{
		if(dbgInfo != null && Application.realMainProgram == this)
			dbgInfo.onExit();
		if(disconnectUserAtCleanup)
		{
			try
			{
				Application.getConnectionManager().disconnect(remoteHandle);
			}catch(Exception disconnectException){ ; }
		}
		Application.cleanup(context, this, remoteHandle);
	}

	public void endExecute(String name)
	{
		ProcedureInfo pInfo = ProceduresInfo.getProcedureInfo(name);
		pInfo.setTimeExecute(System.currentTimeMillis() - beginExecute.getTime());
		
		if (context != null && context.getSessionContext() != null)
		{
			ApplicationContext.getInstance().setEJB(false);
			ApplicationContext.getInstance().setPoolConnections(false);
		}		
	}
	
	public void release()
	{
	}

	protected String formatLink(String jumpURL)
	{
		return formatLink(jumpURL, new String[]{}, new String[]{});
	}

	protected String formatLink(String jumpURL, String[] parms, String[] parmsName)
	{
		String lowURL = CommonUtil.lower(jumpURL);
		String packageName = context.getPackageName();

		// Convert 'call', adding package when needed
		if	(com.genexus.webpanels.GXWebPanel.getStaticGeneration() && (lowURL.startsWith("http:" + packageName + "h") || lowURL.startsWith("https:" + packageName + "h")))
		{
			return  com.genexus.webpanels.WebUtils.getDynURL() + jumpURL.substring(lowURL.indexOf(':') + 1);
		}

		String contextPath = (httpContext.getRequest() == null)? "" : httpContext.getRequest().getContextPath();
		return URLRouter.getURLRoute(jumpURL, parms, parmsName, contextPath);
	}
	
	public void callSubmit(final int id, Object [] submitParms)
	{
		SubmitThreadPool.submit(this, id, submitParms, context.submitCopy());
	}
	
	public void callSubmit(String blockName, String message, final int id, Object [] submitParms)
	{
		ReorgSubmitThreadPool.submitReorg(blockName, message, this, id);
	}	
	
	/** This method is overridden by subclass in the generated code
	 */
	public void submit(int id, Object [] submitParms, ModelContext ctx){  }
	public void submit(int id, Object [] submitParms){  }
	public void submitReorg(int id, Object [] submitParms) throws SQLException{  }
	
	
	public void setejbMessageCall()
	{
		ejbMessageCall = true;
	}
	
	public void SetCreateDataBase()
	{
		GXReorganization.setCreateDataBase();
	}

    public int setLanguage(String language)
    {
		int res = GXutil.setLanguage(language, context, ui);
		this.localUtil = ui.getLocalUtil();
		return res;
	}

	protected void callWebObject(String url)
	{
		httpContext.wjLoc = url;
	}

	protected void cleanup()
	{
	}

	public void handleException(String gxExceptionType, String gxExceptionDetails, String gxExceptionStack)
	{
	}

	private GXDebugInfo dbgInfo = null;
	protected void trkCleanup()
	{
		if(dbgInfo != null)
			dbgInfo.onCleanup();
	}

	protected void initialize(int objClass, int objId, int dbgLines, long hash)
	{
		dbgInfo = GXDebugManager.getInstance().getDbgInfo(context, objClass, objId, dbgLines, hash);
	}

	protected void trk(int lineNro)
	{
		if(dbgInfo != null)
			dbgInfo.trk(lineNro);
	}

	protected void trk(int lineNro, int lineNro2)
	{
		if(dbgInfo != null)
			dbgInfo.trk(lineNro, lineNro2);
	}

	protected void trkrng(int lineNro, int lineNro2)
	{
		trkrng(lineNro, 0, lineNro2, 0);
	}

	protected void trkrng(int lineNro, int colNro, int lineNro2, int colNro2)
	{
		if(dbgInfo != null)
			dbgInfo.trkRng(lineNro, colNro, lineNro2, colNro2);
	}
}
