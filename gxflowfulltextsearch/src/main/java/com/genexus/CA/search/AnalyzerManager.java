package com.genexus.CA.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerManager {
	private static final Map<String, Analyzer> ANALYZERS = new ConcurrentHashMap<>();

	static {
		ANALYZERS.put("default", new StandardAnalyzer());
		// In the future, when the Lucene version is updated, specific analyzers for different languages can be added here.
		// For example, for Spanish:
		// ANALYZERS.put("es", new org.apache.lucene.analysis.es.SpanishAnalyzer());
	}

	public static Analyzer getAnalyzer(String lang) {
		if (lang == null || lang.trim().isEmpty()) {
			return ANALYZERS.get("default");
		}
		return ANALYZERS.getOrDefault(lang, ANALYZERS.get("default"));
	}
}
