package com.genexus.common.interfaces;

public interface IPreferences {

	String getBLOB_PATH();

	int getSUBMIT_POOL_SIZE();

	byte getREMOTE_CALLS();

	com.genexus.util.IniFile getIniFile();

	String getNAME_SPACE();

	String getNAME_HOST();

	String getServerKey();

	String getSiteKey();

    String getEvent(String after_connect);

    String getProperty(String server_name, String s);

    boolean propertyExists(String drawGridsAtServer);

	String getORQ_SERVER_DIR();

	String getORQ_CLIENT_URL();

}
