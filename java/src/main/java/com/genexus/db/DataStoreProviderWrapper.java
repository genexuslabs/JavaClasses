package com.genexus.db;

import com.genexus.performance.DataStoreProviderInfo;
import com.genexus.performance.IDataStoreProviderInfo;

public class DataStoreProviderWrapper implements IDataStoreProviderInfo {
	public DataStoreProviderInfo getDataStoreProviderInfo(String key) { return DataStoreProvider.getDataStoreProviderInfo(key); }

	long getSentenceCount() {
		return DataStoreProvider.getSentenceCount();
	}

	long getSentenceSelectCount() {
		return DataStoreProvider.getSentenceSelectCount();
	}

	long getSentenceUpdateCount() {
		return DataStoreProvider.getSentenceUpdateCount();
	}

	long getSentenceDeleteCount() { return DataStoreProvider.getSentenceDeleteCount(); }

	long getSentenceInsertCount() {
		return DataStoreProvider.getSentenceInsertCount();
	}

	long getSentenceCallCount() {
		return DataStoreProvider.getSentenceCallCount();
	}

	long getSentenceDirectSQLCount() {
		return DataStoreProvider.getSentenceDirectSQLCount();
	}

	void dump() { DataStoreProvider.dump();}
}
