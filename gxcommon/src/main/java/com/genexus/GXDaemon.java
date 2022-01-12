package com.genexus;

import com.genexus.common.interfaces.SpecificImplementation;

public class GXDaemon
{
	public static void main(String args[])
	{
		if	(args.length == 0)
		{
			System.out.println("Usage: java com.genexus.GXDaemon [-t:<timeout>] <pgmname> [ pgm parms ]");
			System.out.println("timeout is in seconds, default is 5 minutes");
			System.exit(1);
		}

		new GXDaemon().execute(args);
	}
	
	public static long timeout = 300000L;

	private void execute(String args[])
	{
		String nextArgs[] = processParameters(args);

		Class<?> myClass;
		try
		{	
			myClass = Class.forName(pgmName);
		}
		catch (ClassNotFoundException e)	
		{ 
			SpecificImplementation.Application.printWarning("ClassNotFoundException Can't execute dynamic call " + pgmName + " - " + e.getMessage(), e);
			return;
		}

		Class[] intClass = new Class[] {int.class};
		try
		{	
			Object gxCfg = myClass.getMethod("refClasses", new Class[0]).invoke( myClass,  new Object[0]);
			SpecificImplementation.Application.init(gxCfg.getClass());
			Object constructed = myClass.getConstructor(intClass).newInstance(new Object[] {new Integer(-1)});

			while (true)
			{
				System.out.println(new java.util.Date() + " - Executing " + pgmName);
				 constructed.getClass().getMethod("execute", new Class[0]).invoke( constructed,  new Object[0]);

				try
				{
					Thread.sleep(timeout);
				}
				catch (InterruptedException e)
				{
				}
			}
		}
		catch (NoSuchMethodException e)
		{
			System.out.println("NoSuchMethodException " + e);
		}
		catch (IllegalAccessException e)
		{
			System.out.println("IllegalAcceptException " + e);
		}
		catch (java.lang.reflect.InvocationTargetException e)
		{
			System.out.println("InvocationTargetException " + e.getTargetException());
		}
		catch (InstantiationException e)
		{
			System.out.println("InstantiationException " + e);
		}
	}

	String pgmName = "";
	private String[] processParameters(String args[])
	{
		int i = 0;
		while (i < args.length)
		{
			if	(args[i].startsWith("-"))
			{
				if	(args[i].toLowerCase().startsWith("-t:"))
				{
					timeout  = ((long) CommonUtil.val(args[i].substring(args[i].indexOf(':') + 1))) * 1000;
				}
			}
			else
			{
				break;
			}

			i++;
		}

		pgmName = args[i++];

		String[] out = new String[args.length - i];

		System.arraycopy(args, i, out, 0, out.length);

		return out;
	}
}