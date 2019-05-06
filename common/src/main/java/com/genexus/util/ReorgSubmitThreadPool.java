package com.genexus.util;
import com.genexus.*;
import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;

import java.util.*;

public class ReorgSubmitThreadPool
{
	public static final String SUBMIT_THREAD = "SubmitThread-";
	private static ReorgSubmitThread [] threadPool = null;
	private static Vector submitQueue = new Vector();
	private static Vector submitPrecedenceQueue = new Vector();
	private static int remainingSubmits = 0;
	private static Object lockObject = new Object();
	private static boolean onlyOneThread = false;
	
	public static void startProcess()
	{
		if(threadPool == null)
		{ // Si el pool todav�a no fue creado
			if (onlyOneThread)
			{
				threadPool = new ReorgSubmitThread[1];
			}
			else
			{
				threadPool = new ReorgSubmitThread[SpecificImplementation.Application.getDefaultPreferences().getSUBMIT_POOL_SIZE()];
			}
		}
		
		for(int i = 0; i < threadPool.length; i++)
		{
			if(submitQueue.size() > 0)
			{
				Object [] nextSubmit = (Object[])submitQueue.firstElement();
				submitQueue.removeElement(nextSubmit);

				threadPool[i] = new ReorgSubmitThread(i);
				threadPool[i].start();
				
				// @HACK: al levantar al thread, debo esperar hasta que llegue al wait
				// porque sino se puede perder el primer notify() (y en efecto era lo que pasaba)				
				while(!threadPool[i].isInitialized())
				{
					try
					{
						Thread.sleep(100);
					}catch(InterruptedException e){ ; }
				}

				synchronized(threadPool[i])
				{
					threadPool[i].setProc((String)nextSubmit[2], (ISubmitteable)nextSubmit[0], ((Integer)nextSubmit[1]).intValue(), SpecificImplementation.Application.getModelContext());
					threadPool[i].notify();
				}
			}
		}
	}
	
	/*public static void submit(final String blockName, final ISubmitteable proc, final int id, final Object []submitParms)
	{	
		ReorgSubmitThreadPool.incRemainingSubmits();

		GXParameterPacker parameterPacker = new GXParameterPacker();
		parameterPacker.reset();
		parameterPacker.writeObject(submitParms); 
		
		// Ahora busco un thread libre
		for(int i = 0; i < threadPool.length; i++)
		{
			if(threadPool[i] == null)
			{ // Si nunca se ha utilizado este thread, debo crearlo
				threadPool[i] = new ReorgSubmitThread(i);
				threadPool[i].start();
				
				// @HACK: al levantar al thread, debo esperar hasta que llegue al wait
				// porque sino se puede perder el primer notify() (y en efecto era lo que pasaba)				
				while(!threadPool[i].isInitialized())
				{
					try
					{
						Thread.sleep(100);
					}catch(InterruptedException e){ ; }
				}
			}
			
			synchronized(threadPool[i])
			{			
				if(threadPool[i].inUse())
				{ // Si el thread esta en uso, sigo buscando
					continue;
				}
				threadPool[i].setProc(blockName, proc, id);				
				threadPool[i].notify();
				return;
			}
		}
		
		// Si llego aqui es porque tengo utilizados todos los thread, asi que encolo el submit
		submitQueue.addElement(new Object[]{proc, new Integer(id), parameterPacker.toByteArray(), blockName});
	}*/
	
	public static void setOnlyOneThread()
	{
		onlyOneThread = true;
	}
	
	protected static void incRemainingSubmits()
	{
		synchronized(lockObject)
		{
			remainingSubmits++;
		}
	}
	
	protected synchronized static void decRemainingSubmits()
	{
		//synchronized(lockObject)
		//{
			remainingSubmits--;
			if (remainingSubmits == 0 || errorCount == SpecificImplementation.Application.getDefaultPreferences().getSUBMIT_POOL_SIZE() || (errorCount >0 && onlyOneThread))
			{
				ReorgSubmitThreadPool.class.notify();
				//lockObject.notify();
			}
		//}
	}
	
	public synchronized static void waitForEnd()
	{
		//synchronized(lockObject)
		//{		
			if(remainingSubmits > 0)
			{
				System.err.println("Waiting for " + remainingSubmits + " submitted procs to end...");
			}
		while(!(onlyOneThread && errorCount > 0) && remainingSubmits > 0 && !(errorCount == SpecificImplementation.Application.getDefaultPreferences().getSUBMIT_POOL_SIZE()))
		{
			try
			{
				//lockObject.wait();
				ReorgSubmitThreadPool.class.wait();
			}catch(InterruptedException e)
			{ 
				e.printStackTrace(); 
			}
		}
		//}
	}
	
	private static boolean hasAnError = false;
	private static int errorCount = 0;
	
	public static void setAnError()
	{
		hasAnError = true;
		errorCount++;
	}
		
	public static boolean hasAnyError()
	{
		return hasAnError;
	}
	
	protected synchronized static Object [] getNextSubmit()
	{
		if (!(submitQueue.size() > 0))
		{
			for(Enumeration enumPrecedence = submitPrecedenceQueue.elements(); enumPrecedence.hasMoreElements();)
			{
				Object [] nextPrecedenceSubmit = (Object [])enumPrecedence.nextElement();
				int id = ((Integer)nextPrecedenceSubmit[1]).intValue();
				String blockName = (String)nextPrecedenceSubmit[2];
				if (allPrecedencesEnded(id , blockName))
				{
					ISubmitteable proc = (ISubmitteable)nextPrecedenceSubmit[0];
					submitPrecedenceQueue.removeElement(nextPrecedenceSubmit);
					//submit(blockName, proc, id);
					ReorgSubmitThreadPool.incRemainingSubmits();
					submitQueue.addElement(new Object[]{proc, new Integer(id), blockName});					
				}
			}
		}
		if(submitQueue.size() > 0)
		{
			Object [] nextSubmit = (Object[])submitQueue.firstElement();
			submitQueue.removeElement(nextSubmit);
			return nextSubmit;
		}
		return null;
	}
	
	static Hashtable blocks = new Hashtable();
	
	
	public static void addBlock(String blockName)
	{
		blocks.put(blockName, new ReorgBlock());
	}
	
	public static void addPrecedence(String blockName, String precedence)
	{
		((ReorgBlock)blocks.get(blockName)).addPrecedence(precedence);
	}
	
	public static void submitReorg(String blockName, String message, final ISubmitteable proc, final int id)
	{
		Vector precedence = ((ReorgBlock)blocks.get(blockName)).getPrecedences();
		((ReorgBlock)blocks.get(blockName)).setMessage(message);

		if (precedence == null)
		{
			ReorgSubmitThreadPool.incRemainingSubmits();
			submitQueue.addElement(new Object[]{proc, new Integer(id), blockName});
		}
		else
		{
			submitPrecedenceQueue.addElement(new Object[]{proc, new Integer(id), blockName});
		}
	}
	
	private static boolean allPrecedencesEnded(int id, String blockName)
	{
		Vector precedence = ((ReorgBlock)blocks.get(blockName)).getPrecedences();
		if (precedence == null)
		{
			return true;
		}
		else
		{
			String precede;
			for(Enumeration enum1 = precedence.elements(); enum1.hasMoreElements();)
			{
				precede = (String)enum1.nextElement();
				if (!((ReorgBlock)blocks.get(precede)).getEnded() )
				{
					String message = ((ReorgBlock)blocks.get(blockName)).getMessage() + " WAITING FOR " + ((ReorgBlock)blocks.get(precede)).getMessage();
					SpecificImplementation.Application.replaceMsg( id , message);
					return false;
				}
			}						
			return true;
		}
	}
	
}

class ReorgSubmitThread extends Thread
{
	private boolean exit = false;
	private Object threadLock;
	private boolean inUse = false;
	private ISubmitteable proc;
	private int submitId;
	private boolean initialized = false;
	private String blockName;
	private ModelContext ctx;
	
	public ReorgSubmitThread(int index)
	{
		super(ReorgSubmitThreadPool.SUBMIT_THREAD + index);
	}
	
	public void run()
	{		
		synchronized(this)
		{
			initialized = true;
		}
		while(!exit)
		{
			synchronized(this)
			{
				if(!inUse)
				{
					try
					{
						this.wait();
					}catch(InterruptedException e) { ; }
					if(exit)
					{
						break;
					}
				}
			}
				
			// Ejecuto el submit
			try
			{
				if (ctx.getThreadModelContext() == null)
					ctx.setThreadModelContext(ctx);				
				proc.submitReorg(submitId, new Object[]{ });
			}catch(Throwable e)
			{
				e.printStackTrace();
				ReorgSubmitThreadPool.setAnError();
				ReorgSubmitThreadPool.decRemainingSubmits();
				String message = ((ReorgBlock)ReorgSubmitThreadPool.blocks.get(blockName)).getMessage() + " FAILED";
				SpecificImplementation.Application.replaceMsg( submitId , message);
				//this.destroy();
				ReorgSubmitThreadPool.decRemainingSubmits();			
				return;
			}
				
			proc = null;
			((ReorgBlock)ReorgSubmitThreadPool.blocks.get(blockName)).setEnded();
			
			// Veo si hay un nuevo submit para ejecutar
			Object [] nextSubmit = ReorgSubmitThreadPool.getNextSubmit();
			//Paso el decRemainingSubmits para luego del getNextSubmit porque parece que puede pasar que se termine la ejecucion
			//porque si justo antes del getNextSubmit se llama al waitForEnd y da cero se piensa que no tiene mas nada para hacer.
			ReorgSubmitThreadPool.decRemainingSubmits();			
			
			synchronized(this)
			{ // Aqui debo sincronizar pues se setea la variable inUse
				if(nextSubmit != null)
				{
					setProc((String)nextSubmit[2], (ISubmitteable)nextSubmit[0], ((Integer)nextSubmit[1]).intValue(), ctx);
				}
				else
				{
					inUse = false;
				}			
			}
		}
	}
	
	public synchronized boolean inUse()
	{
		return inUse;
	}
	
	public void setProc(String blockName, ISubmitteable proc, int submitId, ModelContext ctx)
	{
		this.blockName = blockName;
		this.proc = proc;
		this.submitId = submitId;
		this.ctx = ctx;
		inUse = true;
	}
	
	public void setExit()
	{
		this.exit = true;
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
}	

class ReorgBlock
{
	private boolean ended = false;
	private Vector precedences = null;
	private String message;
	
	public ReorgBlock()
	{
	}
		
	public void setEnded()
	{
		ended = true;
	}
	
	public boolean getEnded()
	{
		return ended;
	}		
	
	public void addPrecedence(String precedence)
	{
		if (precedences == null)
		{
			precedences = new Vector() ;
		}
		precedences.addElement(precedence);
	}
		
	public Vector getPrecedences()
	{
		return precedences;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}
}