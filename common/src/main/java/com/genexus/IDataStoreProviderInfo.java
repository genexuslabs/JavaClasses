package com.genexus;

import java.io.PrintStream;
import com.genexus.xml.XMLWriter;
import java.util.Hashtable;

public interface IDataStoreProviderInfo {

	String getName();

	long getSentenceCount();
	void incSentenceCount();

	long getSentenceSelectCount();
	void incSentenceSelectCount();

	long getSentenceUpdateCount();
	void incSentenceUpdateCount();

	long getSentenceDeleteCount();
	void incSentenceDeleteCount();

	long getSentenceInsertCount();
	void incSentenceInsertCount();

	long getSentenceCallCount();
	void incSentenceCallCount();

	long getSentenceDirectSQLCount();
	void incSentenceDirectSQLCount();

	void dump(PrintStream out);
	void dump(XMLWriter writer);

	ISentenceInfo addSentenceInfo(String key, String sqlSentence);
	ISentenceInfo getSentenceInfo(String key);
}

