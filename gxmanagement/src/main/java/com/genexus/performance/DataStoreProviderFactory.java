package com.genexus.performance;

import com.genexus.IDataStoreProviderFactory;
import com.genexus.IDataStoreProviderInfo;

public class DataStoreProviderFactory implements IDataStoreProviderFactory {
	public IDataStoreProviderInfo createDataStoreProviderInfo(String key) {
		return new DataStoreProviderInfo(key);
	}
}

