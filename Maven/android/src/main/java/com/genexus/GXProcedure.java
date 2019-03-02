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

import java.util.*;
import java.sql.SQLException;

import com.genexus.common.classes.AbstractModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.*;
import com.genexus.util.*;

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
    private UserInformation ui=null;
	
	private Date beginExecute; 
	
	public GXProcedure(int remoteHandle, ModelContext context, String location)
	{
		this(false, remoteHandle, context, location);
	}

	public GXProcedure(boolean inNewUTL, int remoteHandle, ModelContext context, String location)
	{		
		this.remoteHandle = remoteHandle;
		this.context	  = context;
		this.location	  = location;
		
		// inNewUTL not supported by Android offline generator.
		
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
			default:
				ui = Application.getConnectionManager().getUserInformation(remoteHandle);
		}

		localUtil    	  = ui.getLocalUtil();
		httpContext = context.getHttpContext();
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
	}
	
	public void release()
	{
	}

	protected String formatLink(String jumpURL)
	{
		return jumpURL;
	}	
	
	public void callSubmit(final int id, Object [] submitParms)
	{
		// Call Submit
		SubmitThreadPool.submit(this, id, submitParms);
	}
	
	public void callSubmit(String blockName, String message, final int id, Object [] submitParms)
	{
		ReorgSubmitThreadPool.submitReorg(blockName, message, this, id);
	}	
	
	/** Este metodo es redefinido por la clase GX generada cuando hay submits
	 */
	public void submit(int id, Object [] submitParms){ ; }
	public void submit(int id, Object [] submitParms, AbstractModelContext context){ ; }
	public void submitReorg(int id, Object [] submitParms) throws SQLException{ ; }
	
	
	public void setejbMessageCall()
	{
		ejbMessageCall = true;
	}
	
	public void SetCreateDataBase()
	{

	}

        public int setLanguage(String language)
        {
            int res = com.genexus.GXutil.setLanguage(language, context, ui);
            this.localUtil = ui.getLocalUtil();
            return res;
        }
}