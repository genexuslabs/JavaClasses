package com.genexus;

import java.sql.SQLException;
import java.util.Date;

import com.genexus.ModelContext;
import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.internet.HttpContext;
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
		httpContext = (HttpContext) context.getHttpContext();
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
	public void submit(int id, Object [] submitParms, ModelContext context){ ; }
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