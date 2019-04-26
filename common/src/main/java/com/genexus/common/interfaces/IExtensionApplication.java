package com.genexus.common.interfaces;

import java.sql.SQLException;
import com.genexus.GXDBException;
import com.genexus.ICleanedup;
import com.genexus.ModelContext;
import com.genexus.SdtMessages_Message;
import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.ModelContext;
import com.genexus.common.classes.AbstractNamespace;
import com.genexus.common.classes.AbstractUserInformation;

import com.genexus.util.IniFile;

public interface IExtensionApplication {
	void exit();

	Object getProperty(String string, String string2);

	<T> Object createCollectionWrapper(Object struct);

	void printWarning(String string, Exception e);

	void init(Class<? extends Object> configClass);

	void GXLocalException(ModelContext context, int handle, String string, GXDBException gxdbException);

	IPreferences getDefaultPreferences();

	void addCleanup(ICleanedup temporaryFiles);

	ModelContext getModelContext();

	void executeStatement(ModelContext context, int handle, String dataSource, String statement) throws SQLException;

	void GXLocalException(int handle, String string, SQLException ex);

	IniFile getConfigFile(Class<?> object, String string, Class<?> object2);

	boolean executedBefore(String statement);

	void addExecutedStatement(String statement);

	boolean handlSQLException(int handle, String dataSource, SQLException ex);

	Class<?> getModelContextClass();

	ModelContext createModelContext(Class<SdtMessages_Message> class1);

	String getPACKAGE(); //getClientContext().getClientPreferences()

	Class<?> getModelContextPackageClass();

	Class<?> getConfigurationClass(); //.gxCfg

	String getContentType(String fileName);

	AbstractNamespace createNamespace(ModelContext context);

	AbstractUserInformation createUserInformation(AbstractNamespace ns); //getConnectionManager().createUserInformation(ns);

	AbstractUserInformation getUserInformation(int remoteHandle);

	void replaceMsg(int submitId, String message); 

	IClientPreferences getClientPreferences();

	AbstractGXConnection getConnection(ModelContext context, int remoteHandle, String dataStore,
			boolean readOnly, boolean sticky) throws SQLException;

	ModelContext getModelContext(Class<?> modelContextClass);

	Class<?> getApplicationClass();

	Class<?> getGXWebObjectStubClass();

	void displayURL(String fileName);//PrivateUtilities

	Class getContextClassName();

	void setContextClassName(Class packageClass);
}
