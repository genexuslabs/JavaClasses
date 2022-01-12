package com.genexus.util;

import com.genexus.platform.NativeFunctions;

public class ThreadedCommandQueue implements ICommandQueue, Runnable //, ICleanedup
{
	private LinkedQueue queue;
	private volatile boolean endFlag = false;
	private Thread thread ;
	//private Object lock 		= new Object();
	private Object consumerLock = new Object();
	private boolean isDispatching;

	public void startDispatching()
	{
		isDispatching = true;
		queue = new LinkedQueue();
		thread = new Thread(this, "Report viewer queue");
		thread.start();
	}
	
	private void restartDispatching()
	{
		isDispatching = true;
		thread = new Thread(this, "Report viewer queue");
		thread.start();
	}

	public void addCommandNowait(Runnable cmd)
	{
		try
		{
			if(!isDispatching)
				restartDispatching();
			
			queue.put(new RunnableFlag(cmd, true));
		}
		catch (InterruptedException e) {}
	}

	public void addCommand(Runnable cmd)
	{
		RunnableFlag cmdr = new RunnableFlag(cmd);

		try
		{
			if(!isDispatching)
				restartDispatching();
			
			queue.put(cmdr);
		}
		catch (InterruptedException e) {}
		
		synchronized (cmdr)
		{
			cmdr.setFlag(true);
			try
			{
				cmdr.wait();
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	public boolean consume(RunnableFlag o)
	{	
		boolean ret = false;
		RunnableFlag cmdr ;
				
		cmdr = (RunnableFlag) o;

		if	(cmdr != null)		
		{
			ret = true;

			// Esto es para no hacer el run si el no entro en el wait.
			while (!cmdr.getFlag()) 
				Thread.yield();

		 	cmdr.runnable.run();
			synchronized (cmdr)
			{
				cmdr.notifyAll();
			}
		}
		return ret;
	}

	public void run()
	{
		/*try
		{*/
			boolean ret;
			while (true)
			{
					RunnableFlag rf = (RunnableFlag) queue.peek();
					if (rf != null)
					{
						try
						{
							rf = (RunnableFlag) queue.take();
							if(!rf.getRunCleanup())
							{
								consume(rf);
							}
							else
							{
								isDispatching = false;
								thread.join();
								return;
							}
						}
						catch (InterruptedException e) {}			
					}
					else
					{
						try
						{
							Thread.sleep(1);
						}catch(InterruptedException e) { ; }				
					}
					if (NativeFunctions.isMicrosoft())
					{ 
						try
						{//Agrego este sleep porque ejecutando con MS se estaba comiendo 100% de CPU
							Thread.sleep(5);
						}catch(InterruptedException e) { ; }	
					}
			}
		/*}
		catch (ClassCastException e)
		{
			// Truquito.. si hace un classcast es porque metio un Object, y tiene
			// que salir.
		}

		// Esto es para que termine el cleanup
		synchronized(lock)
		{
			lock.notifyAll();
		}*/
	}

	public void cleanup()
	{
		try
		{	
			RunnableFlag cleanupCmd = new RunnableFlag(null);
			cleanupCmd.setRunCleanup(true);
			queue.put(cleanupCmd);
		}
		catch (InterruptedException e) {}

		/*while (thread.isAlive()) 
			Thread.yield();*/
	}
}


	class RunnableFlag
	{
		Runnable runnable;

		private boolean runCleanup;
		
		private volatile boolean  flag;
		RunnableFlag(Runnable runnable)
		{
			this.runnable = runnable;
			this.runCleanup = false;
		}

		RunnableFlag(Runnable runnable, boolean flag)
		{
			this.runnable = runnable;
			this.flag = flag;
			this.runCleanup = false;
		}

		void setFlag(boolean flag)
		{
			this.flag = flag;
		}

		boolean getFlag()
		{
			return flag;
		}
		
		void setRunCleanup(boolean cleanup)
		{
			this.runCleanup = cleanup;
		}

		boolean getRunCleanup()
		{
			return runCleanup;
		}
	}
