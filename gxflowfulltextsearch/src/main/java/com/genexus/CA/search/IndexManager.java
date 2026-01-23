package com.genexus.CA.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexManager {
	private static final Map<String, Indexer> INDEXERS = new ConcurrentHashMap<>();

	public static void addContent(String dir, String uri, String lang, String title, String summary, byte fromFile, String body, String filePath) {
		getIndexer(dir).addContent(uri, lang, title, summary, fromFile, body, filePath);
	}

	public static void deleteContent(String dir, String uri) {
		getIndexer(dir).deleteContent(uri);
	}

	private static Indexer getIndexer(String dir) {
		return INDEXERS.computeIfAbsent(dir, Indexer::new);
	}
}
