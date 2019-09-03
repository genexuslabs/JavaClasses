package com.genexus;

import java.util.Hashtable;

import com.genexus.util.IniFile;

public final class ServerPreferences extends Preferences
{
	public static String fileName = "server.cfg";

	private static ServerPreferences instance;
	private static Hashtable<String, ServerPreferences> preferences = new Hashtable<>();

    public static void setFileName(String iFileName)
    {
        fileName = iFileName;
    }
    
    public ServerPreferences(IniFile iniFile)
	{
		super(iniFile, "Server");
	}

	private ServerPreferences(Class resourceClass)
	{
		super(resourceClass, fileName, "Server");
	}
/*
	private ServerPreferences()
	{
		super(fileName, "Server");
	}
*/
	public static ServerPreferences getInstance(Class resourceClass)
	{
		String packageName = CommonUtil.getPackageName(resourceClass);

		ServerPreferences ret = preferences.get(packageName);

		if	(ret == null)
		{
			ret = new ServerPreferences(resourceClass);
			preferences.put(packageName, ret);	
		}

		return ret;
	}
/*
	public static ServerPreferences getInstance(Class resourceClass)
	{
		if	(instance == null)
			instance = new ServerPreferences();

		return instance;
	}

	public boolean getPOOL_STARTUP()
	{
		return iniFile.getProperty(defaultSection, "POOL_STARTUP").equals("1");
	}
*/

	/**
	*  Maximum number of users per Read/Only connection.
	*/

/*	public int getROPoolSize()
	{
		return (int) GXutil.val(iniFile.getProperty(defaultSection, "POOLSIZE_RO"));
	}

	public boolean getROPoolEnabled()
	{
		return iniFile.getProperty(defaultSection, "PoolROEnabled").equals("1");
	}
	
	public boolean getROPoolSizeUnlimited()
	{
		return iniFile.getProperty(defaultSection, "UnlimitedROPool").equals("1");
	}

	public int getRWPoolSize()
	{
		return (int) GXutil.val(iniFile.getProperty(defaultSection, "POOLSIZE_RW"));
	}

	public boolean getRWPoolSizeUnlimited()
	{
		return iniFile.getProperty(defaultSection, "UnlimitedRWPool").equals("1");
	}
*/
	public String getLocation()
	{
		return iniFile.getProperty(defaultSection, "Location", "");
	}

	public boolean getLogToConsole()
	{
		return !iniFile.getProperty(defaultSection, "LogConsole").equals("0");
	}

	public boolean getLogEnabled()
	{
		return !iniFile.getProperty(defaultSection, "LogEnabled").equals("0");
	}

	public boolean getRemoteAdminEnabled()
	{
		return !iniFile.getProperty(defaultSection, "RemoteAdmin", "1").equals("0");
	}

	public boolean getIsNameServer()
	{
		return iniFile.getProperty(defaultSection, "IsNameServer", "1").equals("1");
	}

	public int getRMI_SERVER_PORT()
	{
		return (int) CommonUtil.val(iniFile.getProperty(defaultSection, "RMI_SERVER_PORT", "0"));
	}

	public String getORB_LOG()
	{
		return iniFile.getProperty(defaultSection, "ORB_LOG");
	}

	public int getServerProtocol()
	{
		return mapRemoteProtocol(iniFile.getProperty(defaultSection, "ServerProtocol", "NEVER"));
	}

	/** Obtiene el nombre del proc a llamar al desconectarse un usuario */
	public String getOnDisconnectProcName()
	{
		return iniFile.getProperty(defaultSection, "OnDisconnect", null);
	}
}

