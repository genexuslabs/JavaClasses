package com.genexus.dummy;

import java.sql.SQLException;

import com.genexus.*;
import com.genexus.common.classes.AbstractModelContext;
import com.genexus.db.*;
import com.genexus.internet.*;

public class GXSDPanel implements ISubmitteable
{
	protected ModelContext context;
	protected HttpContext  httpContext;
	protected LocalUtil    localUtil;
	protected int remoteHandle = -1;

	protected UserInformation ui;
	
	public GXSDPanel(int remoteHandle, ModelContext context)
	{
		this.context      = context;

		ui = (UserInformation) GXObjectHelper.getUserInformation(context, remoteHandle);
		this.remoteHandle = ui.getHandle();

		initState(context, ui);
		ui.setAutoDisconnect(true);
	}
	
	protected void initState(ModelContext context, UserInformation ui)
	{
		localUtil   = ui.getLocalUtil();
		httpContext = context.getHttpContext();
		httpContext.setContext( context);
	}
	
	protected void cleanup()
	{
	}
	
	public Object getParm( Object[] parms, int index)
	{
	  return parms[index];
	}	
	
	/**
	 *  @Hack: Tenemos que ejecutar el submit esto en otro thread, pues el submit es asincrono,
	 *        pero si creamos en el fuente generado el codigo del nuevo thread, se va a crear un
	 *        archivo nuevo xxx$N.class al compilar el xxx, que deberia ser tratado especialmente
	 *        en el makefile (copiado al directorio de servlets, etc); asi que delegamos la
	 *        creacion del thread a la gxclassr donde se llama al metodo a ser ejecutado
	 *		  Adem�s ahora manejamos un pool de threads en el submit
	 */
	public void callSubmit(final int id, Object [] submitParms)
	{
		com.genexus.util.SubmitThreadPool.submit(this, id, submitParms);
	}

	/** Este metodo es redefinido por la clase GX generada cuando hay submits
	 */
	public void submit(int id, Object [] submitParms){ ; }
	public void submitReorg(int id, Object [] submitParms) throws SQLException{ ; }

	@Override
	public void submit(int submitId, Object[] submitParms, AbstractModelContext ctx) {
	}
	
	
	
}