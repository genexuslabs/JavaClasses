package com.genexus.db;

import java.util.Enumeration;
import java.util.Hashtable;

import com.artech.base.services.AndroidContext;
import com.genexus.ApplicationContext;
import com.genexus.CommonUtil;
import com.genexus.GXJarClassLoader;
import com.genexus.ModelContext;
import com.genexus.Preferences;
import com.genexus.common.classes.AbstractNamespace;
import com.genexus.db.driver.DataSource;
import com.genexus.db.driver.GXDBDebug;
import com.genexus.db.driver.JDBCLogConfig;
import com.genexus.util.IniFile;

public class Namespace extends AbstractNamespace
{
	public static final int GXDB_CLIENT = 0;
	public static final int GXDB_SERVER = 1;
	private static Hashtable namespaceList = new Hashtable();

	private GXJarClassLoader classLoader;

	private Hashtable dataSources = new Hashtable();
	private String 	name;

	public int		remoteCalls;
	public int		timeout;
	public String  	nameHost;
	public String  	classesArchive;
	public String 	autoRemote;
	public String 	gxdbLocation;
	public boolean	autoReload;
	public int 		gxdbType;
	public IniFile iniFile;
	private JDBCLogConfig logConfig;
	private GXDBDebug debugLog;
	private static boolean connectStartup = false;

	public Namespace(String name, IniFile iniFile)
	{
		this.iniFile = iniFile;
		String mainSection  = iniFile.sectionExists("Client")?"Client":"Server";

		Preferences preferences = new Preferences(iniFile, mainSection);
		timeout 	  = preferences.getCONN_TIMEOUT();
		remoteCalls   = preferences.getREMOTE_CALLS();
		nameHost      = preferences.getNAME_HOST();

		logConfig = new JDBCLogConfig(iniFile.getProperty(name, "JDBC_LOG", ""),
														false, /* in android dont use jdbc log */
														/*iniFile.getProperty(name, "JDBCLogEnabled", "0").equals("1"),*/
														iniFile.getProperty(name, "JDBCUniqueName", "0").equals("1"),
														iniFile.getIntegerProperty(name, "JDBCLogDetail", "0"),
														iniFile.getProperty(name, "JDBCLogBuffer", "0").equals("1"),
														iniFile.getProperty(name, "JDBCLogPath", ""),
														iniFile.getIntegerProperty(name, "JDBCLogLevel", "0"));
		logConfig.setNamespaceName(name);

		if	(logConfig.getLevel() == logConfig.LEVEL_NAMESPACE)
			debugLog = new GXDBDebug(logConfig);

		boolean inReorg = ApplicationContext.getInstance().getReorganization();

		for (int i = 1; ; i++)
		{
			String dataSourceName;
			try
			{
				dataSourceName = iniFile.getProperty(name, "DataSource" + com.genexus.GXutil.str(i, 5, 0).trim());
			}
			catch (InternalError e)
			{
				break;
			}

			if	(dataSourceName == null)
				break;

			String section = name + "|" + dataSourceName;


			logConfig.setDataSourceName(dataSourceName);
			//connectStartup = iniFile.getProperty(section, "CS_CONNECT", "First").equals("Startup");
			//Always false for android offline app, not apply to Android SqLite app
			connectStartup = false;
			DataSource ds =  new DataSource(
									dataSourceName.toUpperCase(),
									/*com.genexus.PrivateUtilities.getJDBC_DRIVER(iniFile, section),
									iniFile.getProperty(section, "DB_URL",""),*/
									"org.sqldroid.SQLDroidDriver",
									"jdbc:sqldroid:" + AndroidContext.ApplicationContext.getDataBaseFilePath(),
									iniFile.getPropertyEncrypted(section, "USER_ID",""),
									iniFile.getPropertyEncrypted(section, "USER_PASSWORD",""),
									logConfig,
									iniFile.getProperty(section, "USE_JDBC_DATASOURCE", "0").equals("1") && !inReorg &&
									(ApplicationContext.getInstance().isServletEngine() || iniFile.getProperty(section, "REMOTE_CALLS", "").toUpperCase().startsWith("HTTP")),
									iniFile.getProperty(section, "USE_MULTI_JDBC_DATASOURCE", "0").equals("1"),
									iniFile.getProperty(section, "MULTI_JDBC_DATASOURCE_PROC", ""),
									iniFile.getProperty(section, "JDBC_DATASOURCE", ""),
									iniFile.getIntegerProperty(section, "MAX_CURSOR", "100"),
									iniFile.getProperty(section, "ISOLATION_LEVEL", "CR").equals("CR")?1:0,
									iniFile.getProperty(section, "XBASE_TINT", "1").equals("1"),
									iniFile.getProperty(section, "DBMS", "sqlserver"),
									iniFile.getProperty(section, "CS_SCHEMA", ""),
									iniFile.getProperty(section, "INITIALIZE_NEW", "1").equals("1"),
									iniFile.getProperty(section, "CS_LIBL400", "1"),
									iniFile.getProperty(section, "CS_DBNAME", "1"), //AndroidContext.ApplicationContext.getAppName()
									iniFile.getIntegerProperty(section, "WAIT_RECORD", "1"),
                                                                        iniFile.getIntegerProperty(section, "LOCK_RETRY", "10"),
                                                                        true , //Always true for android offline app /* iniFile.getProperty(section, "LoginInServer", "0").equals("1"),*/
									iniFile.getIntegerProperty(section, "POOLSIZE_RW", "-1"),
									iniFile.getProperty(section, "UnlimitedRWPool", "1").equals("1"),
									/* iniFile.getProperty(section, "PoolROEnabled", "1").equals("1"), */
									false,
									iniFile.getIntegerProperty(section, "POOLSIZE_RO", "1"),
									iniFile.getProperty(section, "UnlimitedROPool", "1").equals("1"),
									iniFile.getIntegerProperty(section, "PoolROUsers", "-1"),
									iniFile.getProperty(section, "POOL_STARTUP", "0").equals("1") && !inReorg,
									inReorg,
									connectStartup,
									iniFile.getProperty(section, "PoolRWEnabled", "1").equals("1"),
									iniFile.getProperty(section, "RecycleRO", "0").equals("1"),
									iniFile.getIntegerProperty(section, "RecycleROMin", "30"),
									iniFile.getProperty(section, "RecycleRW", "0").equals("1"),
									iniFile.getIntegerProperty(section, "RecycleRWMin", "30"),
									iniFile.getIntegerProperty(section, "RecycleRWType", "1")
								);

			section = name + "|" + dataSourceName;

			if	(logConfig.getLevel() == logConfig.LEVEL_NAMESPACE)
			{
				ds.setLog(getLog());
			}
			else
			{
				ds.setLog(new GXDBDebug(logConfig));
			}

			ds.setNamespace(name);
			ds.initialize();

			dataSources.put(dataSourceName.toUpperCase(), ds );
		}

		classesArchive  = iniFile.getProperty(name, "Archive", "");
		gxdbLocation	= iniFile.getProperty(name, "GXDB_LOCATION");
		autoRemote		= iniFile.getProperty(name, "AUTO_REMOTE", "");
		autoReload		= iniFile.getProperty(name, "AutoReload", "0").equals("1");
		gxdbType		= (int) CommonUtil.val(iniFile.getProperty(name, "GXDBType", "0"));
		this.name 		= name;
				
	}

	public static Namespace createNamespace(ModelContext context)
	{
		IniFile iniFile = context.getPreferences().getIniFile();

		String name = context.getNAME_SPACE();
		Namespace ret = null;
		if	(name != null)
		{
			ret = (Namespace) namespaceList.get(name);
			if	(ret == null)
			{

				ret = new Namespace(name, iniFile);
				namespaceList.put(name, ret);
			}
		}

		return ret;
	}
	
	public static void endNamespace()
	{
		namespaceList = null;
	}

	static boolean initialized = false;
	public static void createNamespaces(IniFile iniFile)
	{
		if (initialized) return;
		initialized = true;
		String baseName = "Namespace";
		String section  = iniFile.sectionExists("Client")?"Client":"Server";
		String name     = "";
		int i = 0;
		while (name != null)
		{
			name = iniFile.getProperty(section, baseName + (++i));
			if	(name != null)
				namespaceList.put(name, new Namespace(name, iniFile));
		}
	}

	public static void createNamespaces2(IniFile iniFile)
	{
		String baseName = "Namespace";
		String section  = iniFile.sectionExists("Client")?"Client":"Server";
		String name     = "";
		int i = 0;
		while (name != null)
		{
			name = iniFile.getProperty(section, baseName + (++i));
			if	(name != null)
				namespaceList.put(name, new Namespace(name, iniFile));
		}
	}

	public boolean isRemoteGXDB()
	{
		return !gxdbLocation.equals(ApplicationContext.getInstance().getCurrentLocation());
	}

	public boolean isClientGXDB()
	{
		return (isRemoteGXDB() && gxdbLocation.length() != 0);
	}

	public static Namespace getNamespace(String name)
	{
		Namespace ns = (Namespace) namespaceList.get(name);

		if	(ns == null)
		{
			createNamespaces2(com.genexus.ConfigFileFinder.getConfigFile(null, "server.cfg", null));
			ns = (Namespace) namespaceList.get(name);
		}

		return ns;
	}


	public void reset()
	{
		if	(classLoader != null)
		{
            classLoader = null;
/*			try
			{
				classLoader.resetClassLoader();
			}
			catch (java.io.IOException e)
			{
				System.err.println("Error resetting namespace classloader " + e.getMessage());
			}
*/
		}
	}

	public synchronized GXJarClassLoader getClassLoader()
	{
        if(classLoader == null)
            classLoader = new GXJarClassLoader(classesArchive, autoReload);
        else classLoader = classLoader.getClassLoaderInstance();
        return classLoader;
	}

	public int getDataSourceCount()
	{
		return dataSources.size();
	}

	public Enumeration getDataSourcesNames()
	{
		return dataSources.keys();
	}

	public Enumeration getDataSources()
	{
		return dataSources.elements();
	}

	public DataSource getDataSource(String name)
	{
		return (DataSource) dataSources.get(name.toUpperCase());
	}

	public static Enumeration getNamespaceList()
	{
		return namespaceList.keys();
	}

	public String getName()
	{
		return name;
	}

	public GXDBDebug getLog()
	{
		return debugLog;
	}

	public void cleanup() throws java.sql.SQLException
	{
		for (Enumeration enDs = getDataSources(); enDs.hasMoreElements();)
		{
			((DataSource) enDs.nextElement()).cleanup();
		}

		if	(getLog() != null)
			getLog().close(JDBCLogConfig.LEVEL_NAMESPACE);
	}

	public static boolean getConnectAtStartup()
	{
		return connectStartup && Preferences.getDefaultPreferences().getREMOTE_CALLS() == Preferences.ORB_NEVER;
	}

	public static void connectAll(int remoteHandle)
	{
		for(Enumeration enum1 = namespaceList.elements(); enum1.hasMoreElements();)
		{
			Namespace ns = (Namespace)enum1.nextElement();
			for(Enumeration enum2 = ns.dataSources.elements();enum2.hasMoreElements();)
			{
				DataSource ds = (DataSource)enum2.nextElement();
				DBConnection dbconn = new com.genexus.db.DBConnection(ds, remoteHandle);
				dbconn.connect();
				if(dbconn.getErrCode() == 3)
				{
					System.err.println(dbconn.getErrDescription());
				}
			}
		}
	}	

}

