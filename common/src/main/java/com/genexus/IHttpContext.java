package com.genexus;

public interface IHttpContext {

	String getStaticContentBase();
	void setStaticContentBase(String staticContentBase);

	String convertURL(String string);
	String getLanguage();
	String getDefaultPath();
	String getHeader(String string);
	String getClientId();

	String getUserId(String key, ModelContext modelContext, int handle, String dataSource);
	String getUserId(String key, ModelContext modelContext, int handle, com.genexus.db.IDataStoreProvider dataStore);

	short setWrkSt(int handle, String wrkSt);

	String getWorkstationId(int handle);

	String getApplicationId(int handle);

    short setUserId(int handle, String user, String dataSource);

    // HTTP cgi interface
    String cgiGet(String varName);
	String cgiGetFileName(String varName);
	String cgiGetFileType(String varName);

	String getRemoteAddr();
	String getImagePath(String file, String KBId, String theme);
	String getImageSrcSet(String baseImage);
	String getTheme();

	String getLanguageProperty(String property);

    boolean isLocalStorageSupported();

	void pushCurrentUrl();

}
