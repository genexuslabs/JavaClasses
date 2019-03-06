// $Log: GXProcedure.java,v $
// Revision 1.11  2006/05/26 16:38:29  iroqueta
// El arreglo del put anterior solo tiene sentido para 3 capas.
// Le puse que solo se corra en el caso de 3 capas, porque sino da un error de nullpointer al ejecutar un EJB porque el context.prefs es nulo en ese caso.
//
// Revision 1.10  2006/04/05 20:21:30  alevin
// - Arreglo en el constructor para que si las preferences no tienen NAME_SPACE
//   se lo agrego. Pasaba por ejemplo en el beforeConnect en tres capas que las
//   server preferences no lo tienen.
//
// Revision 1.9  2005/10/04 17:27:04  aaguiar
// - Arreglo al put anterior
//
// Revision 1.8  2005/09/28 20:27:24  aaguiar
// - Arreglo para execute in new UTL en appserver
//
// Revision 1.7  2005/07/22 23:18:18  iroqueta
// Agrego ifdefs para que no de problemas JMX en .NET
//
// Revision 1.6  2005/07/21 15:06:48  iroqueta
// Implementacion de soporte de JMX
//
// Revision 1.5  2005/03/28 20:53:39  iroqueta
// Implementacion de Message Driven Bean.
// Agrego la propiedad ejbMessageCall para determinar cuando se esta llamando a un Message Driven Bean o a un Session Bean.
//
// Revision 1.4  2005/03/15 21:09:16  gusbro
// - Cambios para soportar pool de threads de submit
//
// Revision 1.3  2005/02/24 22:00:50  iroqueta
// Soporte para poder llamar webpanels desde procs.
//
// Revision 1.2  2004/02/19 20:12:42  gusbro
// - Cambios para soportar ejecutar el proc en una nueva UTL
//
// Revision 1.1.1.1  2002/04/19 17:38:06  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/04/19 17:38:06  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import java.sql.SQLException;
import java.util.Date;

import com.genexus.common.classes.AbstractModelContext;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
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
						context.prefs.iniFile.setProperty(context.prefs.defaultSection, "NAME_SPACE", Application.getConnectionManager().getUserInformation(parentHandle).getNamespace().getName());
					}
				}
				if (ApplicationContext.getInstance().isApplicationServer())
				{
					if(context.prefs.getProperty("NAME_SPACE", "").equals(""))
					{
						context.prefs.iniFile.setProperty(context.prefs.defaultSection, "NAME_SPACE", Application.getConnectionManager().getUserInformation(parentHandle).getNamespace().getName());
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
			httpContext = context.getHttpContext();
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
		String lowURL = CommonUtil.lower(jumpURL);
		String packageName = context.getPackageName();

		// Convert 'call', adding package when needed
		if	(com.genexus.webpanels.GXWebPanel.getStaticGeneration() && (lowURL.startsWith("http:" + packageName + "h") || lowURL.startsWith("https:" + packageName + "h")))
		{
			return  com.genexus.webpanels.WebUtils.getDynURL() + jumpURL.substring(lowURL.indexOf(':') + 1);
		}

		return jumpURL;
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
	public void submit(int id, Object [] submitParms, AbstractModelContext ctx){ ; }
	public void submit(int id, Object [] submitParms){ ; }
	public void submitReorg(int id, Object [] submitParms) throws SQLException{ ; }
	
	
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
}
