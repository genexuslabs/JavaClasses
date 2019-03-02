package com.genexus;

import com.genexus.common.interfaces.IClientPreferences;
import com.genexus.util.*;		    
import java.util.*;		    
import java.io.*;
import java.net.URL;


public final class ClientPreferences extends Preferences implements IClientPreferences
{
	public static final byte 	CHANGE_NEVER  = 0;
	public static final byte 	CHANGE_ALWAYS = 1;

	public static final byte 	FIELD_EXIT_TAB 	  = 0;
	public static final byte 	FIELD_EXIT_ENTER  = 1;
	public static final byte 	FIELD_EXIT_ADD	  = 2;
	public static final byte 	FIELD_EXIT_LAST_CHAR  = 3;
	public static final byte 	FIELD_EXIT_TAB_ENTER  = 4;

	public static final byte 	LF_NATIVE 		  = 0;
	public static final byte 	LF_CROSSPLATFORM  = 1;

	public static final byte 	PREFERRED_UI_BEST = 0;
	public static final byte 	PREFERRED_UI_AWT  = 1;
	public static final byte 	PREFERRED_UI_JFC  = 2;
	public static final byte 	PREFERRED_UI_WFC  = 3;
	public static final byte 	PREFERRED_UI_SWT  = 4;

	public static final byte 	ESCAPE_EXIT_NONE = 0;
	public static final byte 	ESCAPE_EXIT_FORM = 1;

	public  Boolean		CS_REORGJAVA;
	private Byte		YEAR_LIMIT;
	private String 	 	GXDB_LOCATION;
	public  Boolean		CS_CONNECT;
	public  String		LANGUAGE;
	private Character	DECIMAL_POINT;				  
	public  String		DATE_FMT;
	public  String		TIME_FMT;
	public  Byte		REMOTE_CALLS;

	private Byte		IMAGE_SUBST;
	private Byte		FIELD_EXIT;
	private Boolean		FIELD_EXIT_GRID;
	private Boolean SDI_CLOSING_FIX;
	private Integer		KEY_REFRESH;
	private Boolean 	MDI_FORMS;
	private Integer		KEY_PROMPT;
	private Byte 		JFC_LF;
	private Byte 		MODEL_NUM;
	private String		PREFERRED_UI;
	private Boolean		SUBFILE_ORDER;
	private Byte 		ESCAPE_FUNCTION;
	private Boolean		FC_READONLY;
	private HTMLDocType docType = HTMLDocType.UNDEFINED;
	
	private static ClientPreferences instance;
	private static Hashtable preferences = new Hashtable();

	public ClientPreferences(IniFile iniFile)
	{
		super(iniFile, "Client");
	}

	private ClientPreferences(Class resourceClass, String name)
	{
		super(resourceClass, name, "Client");
	}																				 
/*
	public ClientPreferences()
	{
		super("client.cfg", "Client");
	}
*/
	public static void resetPreferences()
	{
		preferences = new Hashtable();
	}

	public static ClientPreferences getInstance(Class resourceClass)
	{
		String packageName = CommonUtil.getPackageName(resourceClass);

		ClientPreferences ret = (ClientPreferences) preferences.get(packageName);

		if	(ret == null)
		{
			ret = new ClientPreferences(resourceClass, ApplicationContext.getInstance().getReorganization()?"reorg.cfg":"client.cfg");
			preferences.put(packageName, ret);	
		}

		return ret;
	}
	
	public static void endClientPreferences()
	{
		preferences = null;
	}
/*
	public static ClientPreferences getInstance()
	{
		if	(instance == null)
			instance = new ClientPreferences();

		return instance;
	}
*/
	public int getMODEL_NUM()
	{
		return (int) CommonUtil.val(getProperty("MODEL_NUM"));
	}

	public int getGENERATOR_NUM()
	{
		return (int) CommonUtil.val(getProperty("GENERATOR_NUM", "11"));
	}

	public boolean getODBC_CALLS()
	{
		return getProperty("ODBC_CALLS").equals("1");
	}

	public boolean getUSE_JNDI()
	{
		return getProperty("USE_JNDI", "0").equals("1");
	}

	public boolean getSHOW_STATUS()
	{
			return getProperty("SHOW_STATUS", "1").equals("1");
	}

	public byte getJFC_LF()
	{
		if	(JFC_LF == null)
		{
			String prop = getProperty("JFC_LF", "N").toUpperCase();

			if		(prop.substring(0, 1).equals("N"))
					JFC_LF = new Byte(LF_NATIVE);
			else 
				 	JFC_LF = new Byte(LF_CROSSPLATFORM);
		}

		return JFC_LF.byteValue();
	}

	public String getPACKAGE()
	{
		return getProperty("PACKAGE");
	}

	/**
	* Devuelve true si hay que pintar los readonly tal como se especifican
	* en genexus, y false si hay que pintarlos en gris siempre (como dice windows)
	*/

	public boolean getFC_READONLY()
	{
		if	(FC_READONLY == null)
		{
			FC_READONLY = new Boolean(  getProperty("FC_READONLY", "ORIGINAL").equalsIgnoreCase("ORIGINAL") );
		}
				
		return FC_READONLY.booleanValue();
	}

	public boolean getMDI_FORMS()
	{
		if (Application.executingGeneratorTool)
		{
			return false;
		}
		return booleanPreference(MDI_FORMS, "MDI_FORMS", "0");
	}


	/**
	* Devuelve true si hay que ordenar el subfile cuando se da click en una columna, y falso
	* si no hay que hacerlo.
	*/
	public boolean getSUBFILE_ORDER()
	{
		if	(SUBFILE_ORDER == null)
		{
			SUBFILE_ORDER = new Boolean(getProperty("SUBFILE_ORDER", "Y").equalsIgnoreCase("Y") );
		}
				
		return SUBFILE_ORDER.booleanValue();
	}

	public byte getPREFERRED_UI()
	{
		if	(PREFERRED_UI == null)
			PREFERRED_UI = getProperty("PREFERRED_UI", "BEST").toUpperCase();

		if		(PREFERRED_UI.substring(0, 1).equals("A"))
				return PREFERRED_UI_AWT;
		else if	(PREFERRED_UI.substring(0, 1).equals("J"))
			 	return PREFERRED_UI_JFC;
		else if	(PREFERRED_UI.substring(0, 1).equals("W"))
				return PREFERRED_UI_WFC;
		else if	(PREFERRED_UI.substring(0, 1).equals("S"))
				return PREFERRED_UI_SWT;

		return PREFERRED_UI_BEST;
	}

	public byte getIMAGE_SUBST()
	{
		if	(IMAGE_SUBST == null)
		{
			IMAGE_SUBST = new Byte((byte) CommonUtil.val(getProperty("IMAGE_SUBST", "1")));
		}

		return IMAGE_SUBST.byteValue();
	}

	public byte getFIELD_EXIT()
	{
		if	(FIELD_EXIT == null)
		{
			String aux = getProperty("FIELD_EXIT", "TAB").toUpperCase();

			if 		(aux.equals("TAB"))
				FIELD_EXIT = new Byte(FIELD_EXIT_TAB);
			else if (aux.equals("ENTER"))
				FIELD_EXIT = new Byte(FIELD_EXIT_ENTER);
			else if (aux.equals("LAST_CHAR"))
				FIELD_EXIT = new Byte(FIELD_EXIT_LAST_CHAR);
			else if (aux.equals("TABENTER"))
				FIELD_EXIT = new Byte(FIELD_EXIT_TAB_ENTER);
			else //if (aux.equals("+"))
				FIELD_EXIT = new Byte(FIELD_EXIT_ADD);
		}

		return FIELD_EXIT.byteValue();
	}
	public boolean getFIELD_EXIT_GRID()
	{
		if	(FIELD_EXIT_GRID == null)
		{
			FIELD_EXIT_GRID = new Boolean(getProperty("FIELD_EXIT_GRID", "").toUpperCase().equals("FOXPRO"));
		}
		return FIELD_EXIT_GRID.booleanValue();
	}
	public boolean getSDI_CLOSING_FIX()
	{
		if (SDI_CLOSING_FIX == null)
		{
			SDI_CLOSING_FIX = new Boolean(getProperty("SDI_CLOSING_FIX", "N").equalsIgnoreCase("Y"));
		}
		return SDI_CLOSING_FIX.booleanValue();
	}
	public byte getESCAPE_FUNCTION()
	{
		if	(ESCAPE_FUNCTION == null)
		{
			String aux = getProperty("ESCAPE_FUNCTION", "Exit_form");

			if (aux.equals("None"))
				ESCAPE_FUNCTION = new Byte(ESCAPE_EXIT_NONE);
			else if (aux.equals("Exit_form"))
				ESCAPE_FUNCTION = new Byte(ESCAPE_EXIT_FORM);
		}

		return ESCAPE_FUNCTION.byteValue();
	}


	public int getKEY_REFRESH()
	{
		if	(KEY_REFRESH == null)
		{
			KEY_REFRESH = new Integer(mapFunctionKey(getProperty("KEY_REFRESH", "5")));
		}

		return KEY_REFRESH.intValue();
	}



	public int getKEY_PROMPT()
	{
		if	(KEY_PROMPT == null)
		{
			KEY_PROMPT = new Integer(mapFunctionKey(getProperty("KEY_PROMPT", "4")));
		}

		return KEY_PROMPT.intValue();
	}

	private static int mapFunctionKey(String key)
	{
		return -1;
	}


	public char getDECIMAL_POINT()
	{
		if (DECIMAL_POINT == null) 
		{
				DECIMAL_POINT = new Character(getProperty("DECIMAL_POINT").charAt(0));
		}

		return DECIMAL_POINT.charValue();
	}

	public byte getYEAR_LIMIT()
	{
			return bytePreference(YEAR_LIMIT, "YEAR_LIMIT");
	}
	
	public boolean getConnectFirstRequest()
	{
		if	(CS_CONNECT == null)
		{
			String value = getProperty("CS_CONNECT");

			CS_CONNECT = new Boolean (value.equalsIgnoreCase("FIRST"));
		}

		return CS_CONNECT.booleanValue();
	}
			
	public boolean getCS_REORGJAVA()
	{
		return booleanPreference(CS_REORGJAVA, "CS_REORG");
	}

	public String getModelLANGUAGE()
	{
	 	return getProperty("LANGUAGE");
	}

	public String getHELP_BASEURL()
	{
		return PrivateUtilities.addLastChar(getProperty("HELP_BASEURL", ""), "/");
	}

	public String getHELP_MODE()
	{
		return getProperty("HELP_MODE", "WINHTML");
	}


	public String getLANGUAGE()
	{
		return getProperty("LANGUAGE");
	}


	/**
	 * Devuelve el time format selecionado, en un string con el formato que
	 * usa Java para formater los times.
	 */

	public String getTIME_FMT()
	{
			String prop = getProperty("TIME_FMT");

			if	(prop.equals("default"))
				return "";
			return prop;
	}

	/**
	 * Devuelve el date format selecionado, en un string con el formato que
	 * usa Java para formater los dates.
	 */
	public String getDATE_FMT()
	{
			return getProperty("DATE_FMT").toLowerCase();
	}

	public boolean getBLANK_EMPTY_DATE()
	{
		return iniFile.getProperty(defaultSection, "BLANK_EMPTY_DATE", "0").equals("1");
	}
	
	public String getBUILD_NUMBER(int buildN)
	{	
		String buildNumber = iniFile.getProperty(defaultSection, "GX_BUILD_NUMBER", Integer.toString(buildN));
		buildNumber = Integer.toString(Math.max( buildN,Integer.parseInt(buildNumber)));			
		return buildNumber;
	}


	public String getGXDB_LOCATION()
	{
		return getIniFile().getProperty(getNAME_SPACE(), "GXDB_LOCATION", "");		
	}

	public boolean getCOMPRESS_HTML()
	{
		return iniFile.getProperty(defaultSection, "COMPRESS_HTML", "1").equals("1");
	}

	public String getWEB_IMAGE_DIR()
	{
		String dir = iniFile.getProperty(defaultSection, "WEB_IMAGE_DIR", "");

		if	(!(dir.endsWith("/") || dir.endsWith("\\")) && !dir.equals(""))
			return dir + "/";

		return dir;
	}
	
	int GX_NULL_TIMEZONEOFFSET = 9999;
	public boolean useTimezoneFix()
	{
		return getOffsetStorageTimezone() != GX_NULL_TIMEZONEOFFSET;	
	}
	
	static Integer GXOffsetStorageTimezone;
	private static final Object lock = new Object();
	
    public int getOffsetStorageTimezone()
    {
		if (GXOffsetStorageTimezone == null) {
			synchronized (lock) {
				if (GXOffsetStorageTimezone == null) {
			   		String sOffset = iniFile.getProperty(defaultSection, "StorageTimeZone", "0");
					int offset = Integer.parseInt( sOffset);
					if (offset == GX_NULL_TIMEZONEOFFSET)
					{
						GXOffsetStorageTimezone = new Integer( offset);
						return GXOffsetStorageTimezone.intValue();
					}
					int hours = (offset / 100) * 60;
					int minutes = offset % 100;
					GXOffsetStorageTimezone = new Integer( hours + minutes);
					return GXOffsetStorageTimezone.intValue();
				}
			}
		}
		return GXOffsetStorageTimezone.intValue();
    }

	public String getWEB_STATIC_DIR()
	{
		String dir = iniFile.getProperty(defaultSection, "WEB_STATIC_DIR", "");

		if	(!(dir.endsWith("/") || dir.endsWith("\\")) && !dir.equals(""))
			return dir + "/";

		return dir;
	}

	public String getUSE_ENCRYPTION()
	{
		String key = iniFile.getProperty(defaultSection, "USE_ENCRYPTION", "");
		return key;
	}

	public String getTMPMEDIA_DIR()
	{
		String dir = iniFile.getProperty(defaultSection, "TMPMEDIA_DIR", "");
		String separator  = java.io.File.separator;

		if	(!(dir.endsWith(separator)) && !dir.equals(""))
			return dir + separator;
			
		return dir;
	}
	
	
	
	public boolean getUSE_CALENDAR()
	{
		return iniFile.getProperty(defaultSection, "CALENDAR", "1").equals("1");
	}
	
	public boolean getUSE_CALCULATOR()
	{
		return iniFile.getProperty(defaultSection, "CALC", "1").equals("1");
	}
	
	public String getREORG_TIME_STAMP()
	{
		return getProperty("VER_STAMP");
	}
	public boolean getDOCTYPE_DTD()
	{
		return iniFile.getProperty(defaultSection, "DocumentTypeDTD", "1").equals("1");
	}
	  public HTMLDocType getDOCTYPE()
	  {
		  if (docType == HTMLDocType.UNDEFINED)
		  {
			  String value;
			  docType = HTMLDocType.NONE;
			  value = iniFile.getProperty(defaultSection, "DocumentType", "");
			  if (value.startsWith("XHTML"))
				  docType = HTMLDocType.XHTML1;
			  else if (value.startsWith("HTML4S"))
				  docType = HTMLDocType.HTML4S;
			  else if (value.startsWith("HTML4"))
				  docType = HTMLDocType.HTML4;
			  else if (value.startsWith("HTML5"))
				  docType = HTMLDocType.HTML5;
		  }
		  return docType;
	  }
}
