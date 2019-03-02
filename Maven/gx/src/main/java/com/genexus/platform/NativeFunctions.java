package com.genexus.platform;

import java.lang.reflect.InvocationTargetException;

import com.genexus.CommonUtil;

public class NativeFunctions
{
    private static INativeFunctions instance;

    private static Boolean isMicrosoft;

    private NativeFunctions()
    {
    }

    public static boolean isWindows()
    {
    	return CommonUtil.isWindows();
    }

    public static boolean isUnix()
    {
        return !isWindows();
    }

	private static Object handleLock = new Object();

    public static INativeFunctions getInstance() 
    {
        if	(instance == null)
        {
            synchronized (handleLock)
            {
                if	(instance == null)
                {
                    try
                    {
                    	instance = (INativeFunctions) Class.forName("com.genexus.platform.NativeFunctions11").newInstance();
                    }
                    catch (ClassNotFoundException e)
                    {
                        System.err.println("ClassNotFound " + e.getMessage());
                    }
                    catch (IllegalAccessException e)
                    {
                        System.err.println("ClassNotFound " + e.getMessage());
                    }
                    catch (InstantiationException e)
                    {
                        System.err.println("ClassNotFound " + e.getMessage());
                    }
                }
            }
        }

        return instance;
	}

	public static void endNativeFunctions()
	{
		instance = null;
	}
}
