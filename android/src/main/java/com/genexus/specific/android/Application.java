package com.genexus.specific.android;

import java.sql.SQLException;

import com.genexus.GXDBException;
import com.genexus.GXReorganization;
import com.genexus.ICleanedup;
import com.genexus.ModelContext;
import com.genexus.PrivateUtilities;
import com.genexus.SdtMessages_Message;
import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.classes.AbstractModelContext;
import com.genexus.common.classes.AbstractNamespace;
import com.genexus.common.classes.AbstractUserInformation;
import com.genexus.common.interfaces.IClientPreferences;
import com.genexus.common.interfaces.IExtensionApplication;
import com.genexus.common.interfaces.IPreferences;
import com.genexus.db.DBConnectionManager;
import com.genexus.db.Namespace;
import com.genexus.internet.HttpResponse;
import com.genexus.util.IniFile;
import com.genexus.wrapper.GXCollectionWrapper;
import com.genexus.xml.IXMLReader;
import com.genexus.xml.XMLReader;

public class Application implements IExtensionApplication {

	@Override
	public void exit() {
	}

	@Override
	public Object getProperty(String key, String defaultValue) {
		return com.genexus.Application.getClientContext().getClientPreferences().getProperty(key, defaultValue);
	}

	@Override
	public <T> Object createCollectionWrapper(Object struct) {
		return new GXCollectionWrapper<T>(struct);
	}

	@Override
	public void printWarning(String text, Exception e) {
		com.genexus.Application.printWarning(text, e);
	}

	@Override
	public void init(Class<? extends Object> configClass) {
		com.genexus.Application.init(configClass);
	}

	@Override
	public void GXLocalException(AbstractModelContext context, int handle, String text, GXDBException ex) {
		com.genexus.Application.GXLocalException((ModelContext) context, handle, text, ex);

	}

	@Override
	public IPreferences getDefaultPreferences() {

		return com.genexus.Preferences.getDefaultPreferences();
	}

	@Override
	public void addCleanup(ICleanedup temporaryFiles) {
		com.genexus.Application.addCleanup(temporaryFiles);
	}

	@Override
	public AbstractModelContext getModelContext() {
		return ModelContext.getModelContext();
	}

	@Override
	public void executeStatement(AbstractModelContext context, int handle, String dataSource, String sqlSentence)
			throws SQLException {
		com.genexus.Application.getConnectionManager().executeStatement((ModelContext) context, handle, dataSource,
				sqlSentence);
	}

	@Override
	public void GXLocalException(int handle, String text, SQLException ex) {
		com.genexus.Application.GXLocalException(handle, text, ex);
	}

	@Override
	public IniFile getConfigFile(Class resourceClass, String fileName, Class defaultResourceClass) {
		return com.genexus.ConfigFileFinder.getConfigFile(resourceClass, fileName, defaultResourceClass);
	}

	@Override
	public boolean executedBefore(String statement) {
		return GXReorganization.executedBefore(statement);
	}

	@Override
	public void addExecutedStatement(String statement) {
		GXReorganization.addExecutedStatement(statement);
	}

	@Override
	public boolean handlSQLException(int handle, String dataSource, SQLException ex) {
		return com.genexus.Application.getConnectionManager().getDataSource(handle, dataSource).dbms.ObjectNotFound(ex);
	}

	@Override
	public Class getModelContextClass() {
		return ModelContext.class;
	}

	@Override
	public AbstractModelContext createModelContext(Class<SdtMessages_Message> class1) {
		return new ModelContext(class1);
	}

	@Override
	public String getPACKAGE() {
		return com.genexus.Application.getClientContext().getClientPreferences().getPACKAGE();
	}

	@Override
	public Class getModelContextPackageClass() {
		return ModelContext.getModelContextPackageClass();
	}

	@Override
	public Class getConfigurationClass() {
		return com.genexus.Application.gxCfg.getClass();
	}

	@Override
	public String getContentType(String fileName) {
		return HttpResponse.getContentType(fileName);
	}

	@Override
	public AbstractNamespace createNamespace(AbstractModelContext context) {
		return Namespace.createNamespace((ModelContext)context);
	}

	@Override
	public AbstractUserInformation createUserInformation(AbstractNamespace ns) {
		return com.genexus.Application.getConnectionManager().createUserInformation((Namespace) ns);
	}

	@Override
	public AbstractUserInformation getUserInformation(int remoteHandle) {
		return com.genexus.Application.getConnectionManager().getUserInformation(remoteHandle);
	}

	@Override
	public void replaceMsg(int submitId, String message) {
		GXReorganization.replaceMsg(submitId, message);
	}

	@Override
	public IClientPreferences getClientPreferences() {
		return com.genexus.Application.getClientPreferences();
	}

	@Override
	public AbstractGXConnection getConnection(AbstractModelContext context, int remoteHandle, String dataStore,
			boolean readOnly, boolean sticky) throws SQLException {
		return DBConnectionManager.getInstance((ModelContext) context).getConnection((ModelContext) context, remoteHandle, dataStore, readOnly, sticky);
	}

	@Override
	public IXMLReader createXMLReader() {
		return new XMLReader();
	}

	@Override
	public AbstractModelContext getModelContext(Class packageClass) {
		return ModelContext.getModelContext(packageClass);
	}

	@Override
	public Class getApplicationClass() {
		return Application.class;
	}

	@Override
	public Class getGXWebObjectStubClass() {
		return null;
	}

	@Override
	public void displayURL(String fileName) {
		PrivateUtilities.displayURL(fileName);
	}

}
