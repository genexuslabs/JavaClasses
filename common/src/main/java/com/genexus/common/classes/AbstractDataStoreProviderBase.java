package com.genexus.common.classes;

import java.sql.SQLException;

import com.genexus.db.IDataStoreHelper;


public abstract class AbstractDataStoreProviderBase {

	public abstract AbstractGXConnection getConnection() throws SQLException;

	public abstract IDataStoreHelper getHelper() ;

}
