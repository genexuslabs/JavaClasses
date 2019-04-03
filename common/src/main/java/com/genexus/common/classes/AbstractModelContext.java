package com.genexus.common.classes;

import java.util.TimeZone;

import com.genexus.Globals;
import com.genexus.IHttpContext;
import com.genexus.common.interfaces.IClientPreferences;


public abstract class AbstractModelContext {

	public Globals globals = new Globals();

	public abstract TimeZone getClientTimeZone();

	public abstract IHttpContext getHttpContext();

	public abstract String cgiGet(String varName) ;

	public abstract String cgiGetFileName(String varName);

	public abstract String cgiGetFileType(String varName);

	public abstract String getSOAPErrMsg();
	
	public abstract void setSOAPErrMsg(String msg);

	public abstract IClientPreferences getClientPreferences();

	public abstract String getLanguage() ;

	public abstract String getLanguageProperty(String string);

	public abstract Object getThreadModelContext();

	public abstract void setThreadModelContext(Object ctx);

	public abstract String getServerKey();
}
