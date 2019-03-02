package com.genexus.platform;

public class NativeFunctions
{
    private static INativeFunctions instance;

    private static Boolean is12;
    private static Boolean is13;
	private static Boolean is14orGreater;
	private static Boolean is15;
    private static Boolean isMicrosoft;
    private static Boolean isNetscape;
    private static Boolean isSun;
    private static Boolean isWindows;

    private NativeFunctions()
    {
    }

    public static boolean isWindows()
    {
        // Chequeo que la property 'os.name' tenga 'indows'... NOTA: Es 'indows' y NO 'windows'
        // porque sino el index puede ser 0, por ejemplo para 'os.name=Windows 2000'
        if(isWindows == null)
        {
            isWindows = new Boolean((System.getProperty("os.name", "NONE").toLowerCase().indexOf("windows") >= 0));
        }
        return isWindows.booleanValue();
    }

    public static boolean isUnix()
    {
        return !isWindows();
    }

    public static boolean isMicrosoft()
    {
        if	(isMicrosoft == null)
        {
            isMicrosoft = new Boolean(System.getProperty("java.vendor").toUpperCase().indexOf("MICROSOFT") >= 0);
        }

        return isMicrosoft.booleanValue();
    }

    public static boolean isNetscape()
    {
        if	(isNetscape == null)
        {
            isNetscape = new Boolean (System.getProperty("java.vendor").toUpperCase().indexOf("NETSCAPE") >= 0);
        }

        return isNetscape.booleanValue();
    }

    // http://developer.android.com/reference/java/lang/System.html#getProperty(java.lang.String)
	// java.version	(Not useful on Android) return	0
    // java 1.6 needed to compile android apps
	
    public static boolean is11()
    {
    	//return new Boolean(System.getProperty("java.version").substring(0, 3).startsWith("1.1")).booleanValue();
    	return false;
    }

    public static boolean is12()
    {
        //if	(is12 == null)
        //    is12 = new Boolean(System.getProperty("java.version").substring(0, 3).startsWith("1.2"));
    	//return is12.booleanValue();
    	return false;
    }

    public static boolean is13()
    {
        //if	(is13 == null)
        //    is13 = new Boolean(System.getProperty("java.version").substring(0, 3).startsWith("1.3"));
    	//return is13.booleanValue();
    	return false;
    }
	
    public static boolean is15()
    {
		//Por ahora hago que me responda true tambien para el JDK 1.6
		//Despues cualquier cosa vemos de separar los casos, pero por ahora parece no ser necesario.
        //if	(is15 == null)
		//{
        //    is15 = new Boolean(System.getProperty("java.version").substring(0, 3).startsWith("1.5")
		//		|| System.getProperty("java.version").substring(0, 3).startsWith("1.6"));
		//}
    	//return is15.booleanValue();
    	return true;
    }	
	
	public static boolean is14orGreater()
	{
		if(is14orGreater == null)
			is14orGreater = new Boolean((!isMicrosoft() && !is11() && !is12() && !is13()));
		return is14orGreater.booleanValue();
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
			        instance = (INativeFunctions) new com.genexus.platform.NativeFunctions11();
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