package com.genexus.common.interfaces;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;


public interface IGXSmartCacheProvider {

	Object getUpdatedTables();

	boolean isEnabled();

	void invalidateAll();

	void invalidate(String item);

	void recordUpdates(int handle);

	void setUpdated(String table, int handle);

	void discardUpdates(int handle);

	Object CheckDataStatus(String queryId, Date dateLastModified, Date[] dateUpdated_arr);

	ConcurrentHashMap<String, Vector<String>> queryTables();

}
