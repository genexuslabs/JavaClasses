package com.genexus;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Hashtable;

import com.artech.base.services.AndroidContext;
import com.genexus.common.interfaces.IPreferences;
import com.genexus.platform.NativeFunctions;
import com.genexus.util.IniFile;

public class Preferences implements IPreferences {
	public static final byte ORB_NEVER = 0;
	public static final byte ORB_SUN_RMI = 1;
	public static final byte ORB_CORBA = 2;
	public static final byte ORB_DCOM = 3;
	public static final byte ORB_EJB = 4;
	public static final byte ORB_BEST = 5;
	public static final byte ORB_WL_RMI = 6;
	public static final byte ORB_WL_EJB = 7;
	public static final byte ORB_VBROKER = 8;
	public static final byte ORB_HTTP_STATELESS = 9;
	public static final byte ORB_HTTP_STATEFUL = 10;

	public Byte REMOTE_CALLS;
	protected IniFile iniFile;
	protected String defaultSection;

	public Preferences(IniFile iniFile, String defaultSection) {
		this.iniFile = iniFile;
		this.defaultSection = defaultSection;
	}

	public static Class defaultResourceClass;

	public Preferences(Class resourceClass, String fileName, String defaultSection) {
		this.defaultSection = defaultSection;
		if (NativeFunctions.isWindows())
			iniFile = ConfigFileFinder.getConfigFile(resourceClass, fileName, defaultResourceClass);
		else
			iniFile = ConfigFileFinder.getConfigFile(fileName);
	}

	public void setAutomaticReloading(boolean autoReloading) {
		iniFile.setAutomaticReloading(autoReloading);
	}

	public String getNAME_SPACE() {
		return getProperty("NAME_SPACE");
	}

	public IniFile getIniFile() {
		return iniFile;
	}

	public String getServerKey() {
		return iniFile.getServerKey();
	}

	public String getSiteKey() {
		return iniFile.getSiteKey();
	}

	public String getORQ_SERVER_DIR() {
		return PrivateUtilities.addLastPathSeparator(getProperty("ORQ_SERVER_DIR", ""));
	}

	public String getORQ_CLIENT_URL() {
		return PrivateUtilities.addLastChar(getProperty("ORQ_CLIENT_URL", ""), "/");
	}

	public String getSMTP_HOST() {
		return getProperty("SMTP_HOST", "");
	}

	public String getHTTP_BACKEND_URL() {
		return getProperty("HTTP_BACKEND_URL", "");
	}

	public String getNAME_HOST() {
		return getProperty("NAME_HOST", "");
	}

	public int getCONN_TIMEOUT() {
		return (int) CommonUtil.val(getProperty("CONN_TIMEOUT", "300"));
	}

	private String BLOB_PATH = null;

	public String getBLOB_PATH() {

		BLOB_PATH = AndroidContext.ApplicationContext.getFilesBlobsApplicationDirectory();

		// fix blobs path directory, add separator at the end
		if(!BLOB_PATH.equals("") && !BLOB_PATH.endsWith(File.separator))
		{
			BLOB_PATH += File.separator;
		}

		return BLOB_PATH;
	}

	public String getMultimediaPath() {
		File blobDir = new File(getBLOB_PATH());
		File multimediaDir = new File(blobDir, GXDbFile.getMultimediaDirectory());
		if (!multimediaDir.exists())
			multimediaDir.mkdirs();

		return multimediaDir.getPath();
	}

	public String getLUCENE_INDEX_DIRECTORY() {
		return getProperty("LUCENE_INDEX_DIRECTORY", "");
	}

	public String getINDEX_QUEUE_MAX_SIZE() {
		return getProperty("INDEX_QUEUE_MAX_SIZE", "");
	}

	public String getLUCENE_ANALYZER() {
		return getProperty("LUCENE_ANALYZER", "");
	}

	public int getSUBMIT_POOL_SIZE() {
		return (int) CommonUtil.val(getProperty("SUBMIT_POOL_SIZE", "3"));
	}

	public static Preferences getDefaultPreferences() {
		try {
			return Application.getClientPreferences();
		} catch (Exception e) {
			return null;
			// return ApplicationServer.getServerPreferences();
		}
	}

	public boolean propertyExists(String key) {
		return iniFile.propertyExists(defaultSection, key);
	}

	public String getProperty(String key, String defaultValue) {
		return iniFile.getProperty(defaultSection, key, defaultValue);
	}

	public String getProperty(String section, String key, String defaultValue) {
		return iniFile.getProperty(section, key, defaultValue);
	}

	public int getRemoteAdminPort() {
		return (int) CommonUtil.val(iniFile.getProperty(defaultSection, "RemoteAdminPort", "1999"));
	}

	protected String getProperty(String key) {
		String out = iniFile.getProperty(defaultSection, key);

		if (out == null) {
			System.err.println("Can't find key " + key);
			new Throwable().printStackTrace();
		}

		return out;
	}

	public String getCORBA_SERVER_NAME() {
		return iniFile.getProperty(defaultSection, "CORBA_SERVER_NAME", "");
	}

	public String getDCOM_GUID() {
		return iniFile.getProperty(defaultSection, "DCOM_GUID", "");
	}

	protected byte mapRemoteProtocol(String aux) {
		if (aux.equals("CORBA"))
			return ORB_CORBA;
		else if (aux.equals("SUN_RMI"))
			return ORB_SUN_RMI;
		else if (aux.equals("DCOM"))
			return ORB_DCOM;
		else if (aux.equals("VBROKER"))
			return ORB_VBROKER;
		else if (aux.equals("HTTP_STATELESS"))
			return ORB_HTTP_STATELESS;
		else if (aux.equals("HTTP_STATEFUL"))
			return ORB_HTTP_STATEFUL;
		else if (aux.equals("BEST"))
			return ORB_BEST;
		else
			return ORB_NEVER;
	}

	public byte getREMOTE_CALLS() {
		if (REMOTE_CALLS == null) {
			String aux;
			aux = getProperty("REMOTE_CALLS").toUpperCase();

			REMOTE_CALLS = new Byte(mapRemoteProtocol(aux));
		}

		return REMOTE_CALLS.byteValue();
	}

	private Boolean LDAP_USERID;

	public boolean getLDAP_USERID() {
		return getLDAP_USERID_TYPE() != LDAP_USERID_OS;
	}

	public static final int LDAP_USERID_OS = 0;
	public static final int LDAP_USERID_LDAP = 1;
	public static final int LDAP_USERID_LDAP_ONLY_SERVER = 2;
	private Integer LDAP_USERID_TYPE = null;

	public int getLDAP_USERID_TYPE() {
		if (LDAP_USERID_TYPE == null) {
			try {
				String userIdType = getProperty("LDAP_USERID_TYPE", "OS");
				if (userIdType.equalsIgnoreCase("LDAP")) {
					LDAP_USERID_TYPE = new Integer(LDAP_USERID_LDAP);
				} else if (userIdType.equalsIgnoreCase("LDAP_SERVER")) {
					LDAP_USERID_TYPE = new Integer(LDAP_USERID_LDAP_ONLY_SERVER);
				} else
					LDAP_USERID_TYPE = new Integer(LDAP_USERID_OS);
			} catch (Throwable e) {
				e.printStackTrace();
				LDAP_USERID_TYPE = new Integer(LDAP_USERID_OS);
			}
		}
		return LDAP_USERID_TYPE.intValue();
	}

	private Boolean LOGIN_AS_USERID;

	public boolean getLOGIN_AS_USERID() {
		try {
			return booleanPreference(LOGIN_AS_USERID, "LOGIN_AS_USERID");
		} catch (Throwable e) {
			return false;
		}
	}

	/**
	 * Devuelve el valor de una preference como un valor booleano. Los que tienen
	 * distinto de 0 en el archivo, toman valor true, los que tienen 0, valor false.
	 * <p>
	 * Al generar el archivo de propiedades, toma valor 0 los que <b>no</b> tienen
	 * el valor por defecto.
	 */
	protected boolean booleanPreference(Boolean value, String key, String defValue) {
		if (value == null) {
			value = new Boolean(!getProperty(key, defValue).equals("0"));
		}

		return value.booleanValue();
	}

	protected boolean booleanPreference(Boolean value, String key) {
		if (value == null) {
			value = new Boolean(!getProperty(key).equals("0"));
		}

		return value.booleanValue();
	}

	protected byte bytePreference(Byte value, String key, String defValue) {
		if (value == null) {
			try {
				value = new Byte(getProperty(key, defValue));
			} catch (NumberFormatException e) {
				throw new InternalError("Illegal format in model.properties - " + key);
			}
		}
		return value.byteValue();

	}

	/**
	 * Devuelve el valor de una preference como un valor byte.
	 */
	protected byte bytePreference(Byte value, String key) {
		if (value == null) {
			try {
				value = new Byte(getProperty(key));
			} catch (NumberFormatException e) {
				throw new InternalError("Illegal format in model.properties - " + key);
			}
		}
		return value.byteValue();
	}

	/**
	 * Devuelve el valor de una preference como un valor short.
	 */
	protected short shortPreference(Short value, String key) {
		if (value == null) {
			try {
				value = new Short(getProperty(key));
			} catch (NumberFormatException e) {
				throw new InternalError("Illegal format in model.properties - " + key);
			}
		}

		return value.shortValue();
	}

	/**
	 * Devuelve el valor de una preference como un valor int.
	 */
	protected int intPreference(Integer value, String key) {
		if (value == null) {
			try {
				value = new Integer(getProperty(key));
			} catch (NumberFormatException e) {
				throw new InternalError("Illegal format in model.properties - " + key);
			}
		}

		return value.intValue();
	}

	private static int modelNum = -1;

	public static int getGXModelNum() {
		if (modelNum == -1) {
			String list[] = new File(".").list(new Filter());

			if (list != null && list.length > 0) {
				modelNum = (int) CommonUtil.val(list[0].substring(list[0].lastIndexOf('.') + 1));
			}
		}
		return modelNum;
	}

	public long getCACHE_TTL(int category, long defaultTTL) {
		try {
			return Long.parseLong(getProperty("CACHE_TTL_" + category, "" + defaultTTL));
		} catch (Exception e) {
			return defaultTTL;
		}
	}

	public int getCACHE_HTL(int category, int defaultTTL) {
		try {
			return Integer.parseInt(getProperty("CACHE_HTL_" + category, "" + defaultTTL));
		} catch (Exception e) {
			return defaultTTL;
		}
	}

	public long getCACHE_STORAGE_SIZE() {
		try {
			return Long.parseLong(getProperty("CACHE_STORAGE_SIZE", "0"));
		} catch (Exception e) {
			return 0;
		}
	}

	private Boolean CACHING;

	public boolean getCACHING() {
		return booleanPreference(CACHING, "CACHING", "0");
	}

	private Hashtable eventTable = new Hashtable();

	public String getEvent(String eventName) {
		eventName = eventName.toUpperCase();
		String proc = (String) eventTable.get(eventName);
		if (proc == null) {
			proc = getProperty("EVENT_" + eventName, "").trim().toLowerCase();
			eventTable.put(eventName, proc);
		}
		return proc;
	}

	public String getDefaultTheme() {
		return getProperty("Theme", "").trim();
	}

}

class Filter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return name.toUpperCase().startsWith("GXPPRG1");
	}
}
