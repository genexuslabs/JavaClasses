package com.genexus.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadedCommandQueue implements ICommandQueue, Runnable //, ICleanedup
{
	private ConcurrentLinkedQueue queue;
	private Thread thread ;
	private boolean isDispatching;

	public void startDispatching()
	{
		isDispatching = true;
		queue = new ConcurrentLinkedQueue();
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
			
			queue.add(new RunnableFlag(cmd, true));
		}
		catch (IllegalStateException e) {}
	}

	public void addCommand(Runnable cmd)
	{
		RunnableFlag cmdr = new RunnableFlag(cmd);

		try
		{
			if(!isDispatching)
				restartDispatching();
			
			queue.add(cmdr);
		}
		catch (IllegalStateException e) {}
		
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
			while (true)
			{
					RunnableFlag rf = (RunnableFlag) queue.peek();
					if (rf != null)
					{
						try
						{
							rf = (RunnableFlag) queue.poll();
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
					new com.genexus.reports.SunGXReportViewer().gxIsAlive();
			}
	}

	public void cleanup()
	{
		try
		{	
			RunnableFlag cleanupCmd = new RunnableFlag(null);
			cleanupCmd.setRunCleanup(true);
			queue.add(cleanupCmd);
		}
		catch (IllegalStateException e) {}
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
