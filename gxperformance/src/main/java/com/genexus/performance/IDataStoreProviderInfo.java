package com.genexus.performance;

public interface IDataStoreProviderInfo {
	DataStoreProviderInfo getDataStoreProviderInfo(String key);

	static long getSentenceCount() {
		return 0;
	}

	static long getSentenceSelectCount() {
		return 0;
	}

	static long getSentenceUpdateCount() {
		return 0;
	}

	static long getSentenceDeleteCount() {
		return 0;
	}

	static long getSentenceInsertCount() {
		return 0;
	}

	static long getSentenceCallCount() {
		return 0;
	}

	static long getSentenceDirectSQLCount() {
		return 0;
	}

	static void dump() {
	}
}
