package com.genexus.common.interfaces;

import java.sql.SQLException;
import com.genexus.GXDBException;
import com.genexus.ICleanedup;
import com.genexus.SdtMessages_Message;
import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.classes.AbstractModelContext;
import com.genexus.common.classes.AbstractNamespace;
import com.genexus.common.classes.AbstractUserInformation;

import com.genexus.util.IniFile;
import com.genexus.xml.IXMLReader;

public interface IExtensionApplication {
	void exit();

	Object getProperty(String string, String string2);

	<T> Object createCollectionWrapper(Object struct);

	void printWarning(String string, Exception e);

	void init(Class<? extends Object> configClass);

	void GXLocalException(AbstractModelContext context, int handle, String string, GXDBException gxdbException);

	IPreferences getDefaultPreferences();

	void addCleanup(ICleanedup temporaryFiles);

	AbstractModelContext getModelContext();

	void executeStatement(AbstractModelContext context, int handle, String dataSource, String statement) throws SQLException;

	void GXLocalException(int handle, String string, SQLException ex);

	IniFile getConfigFile(Class<?> object, String string, Class<?> object2);

	boolean executedBefore(String statement);

	void addExecutedStatement(String statement);

	boolean handlSQLException(int handle, String dataSource, SQLException ex);

	Class<?> getModelContextClass();

	AbstractModelContext createModelContext(Class<SdtMessages_Message> class1);

	String getPACKAGE(); //getClientContext().getClientPreferences()

	Class<?> getModelContextPackageClass();

	Class<?> getConfigurationClass(); //.gxCfg

	String getContentType(String fileName);

	AbstractNamespace createNamespace(AbstractModelContext context);

	AbstractUserInformation createUserInformation(AbstractNamespace ns); //getConnectionManager().createUserInformation(ns);

	AbstractUserInformation getUserInformation(int remoteHandle);

	void replaceMsg(int submitId, String message); 

	IClientPreferences getClientPreferences();

	AbstractGXConnection getConnection(AbstractModelContext context, int remoteHandle, String dataStore,
			boolean readOnly, boolean sticky) throws SQLException;

	IXMLReader createXMLReader();

	AbstractModelContext getModelContext(Class<?> modelContextClass);

	Class<?> getApplicationClass();

	Class<?> getGXWebObjectStubClass();

	void displayURL(String fileName);//PrivateUtilities
}
