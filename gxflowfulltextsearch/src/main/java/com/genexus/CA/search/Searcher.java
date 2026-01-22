package com.genexus.CA.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

public class Searcher {
	private static final Logger logger = LogManager.getLogger(Searcher.class);

	private static String escapeXml(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	public static String search(String dir, String lang, String query, int maxResults, int from) {
		StringBuilder buff = new StringBuilder();
		long startTime = System.currentTimeMillis();
		
		if (from < 0) {
			logger.warn("Search 'from' cannot be negative. Using 0 instead. from={}", from);
			from = 0;
		}
		if (maxResults < 0) {
			logger.warn("Search 'maxResults' cannot be negative. Using 0 instead. maxResults={}", maxResults);
			maxResults = 0;
		}

		if (!indexExists(dir)) {
			buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Results hits=\"0\" time=\"0ms\"></Results>");
			return buff.toString();
		}
		
		IndexSearcher searcher = null;
		try {
			searcher = new IndexSearcher(dir);
			String[] fields = new String[]{"title", "content", "summary"};
			Occur[] clauses = new Occur[]{Occur.SHOULD, Occur.SHOULD, Occur.SHOULD};
			
			Query q;
			try {
				q = MultiFieldQueryParser.parse(query, fields, clauses, AnalyzerManager.getAnalyzer(lang));
			} catch (ParseException e) {
				try {
					String escapedQuery = QueryParser.escape(query);
					q = MultiFieldQueryParser.parse(escapedQuery, fields, clauses, AnalyzerManager.getAnalyzer(lang));
					logger.warn("Query had invalid syntax. Escaped version was used: {}", escapedQuery, e);
				} catch (ParseException escapedException) {
					logger.warn("Could not parse query, falling back to TermQuery: " + query, escapedException);
					q = new TermQuery(new Term("content", query));
				}
			}

			if (lang != null && !lang.trim().isEmpty() && !"IND".equalsIgnoreCase(lang)) {
				Query q2 = new TermQuery(new Term("language", lang));
				BooleanQuery bq = new BooleanQuery();
				bq.add(q, Occur.MUST);
				bq.add(q2, Occur.MUST);
				q = bq;
			}

			Hits hits = searcher.search(q);
			int totalHits = hits.length();
			
			long endTime = System.currentTimeMillis();
			String time = String.valueOf(endTime - startTime);

			buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			buff.append("<Results hits=\"").append(totalHits).append("\" time=\"").append(time).append("ms\">");

			int end = Math.min(totalHits, from + maxResults);
			for (int i = from; i < end; i++) {
				buff.append("<Result>");
				Document doc = hits.doc(i);
				String uri = doc.getField("uri").stringValue();
				buff.append("<URI>").append(escapeXml(uri)).append("</URI>");
				buff.append("</Result>");
			}
		} catch (Exception e) {
			logger.error("Error during search", e);
			// Return an empty but valid XML in case of error
			buff.setLength(0); // Clear buffer
			buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Results hits=\"0\" time=\"0ms\"></Results>");
			return buff.toString();
		} finally {
			if (searcher != null) {
				try {
					searcher.close();
				} catch (Exception e) {
					logger.error("Error closing IndexSearcher", e);
				}
			}
		}

		buff.append("</Results>");
		return buff.toString();
	}

	private static boolean indexExists(String dir) {
		IndexSearcher searcher = null;
		try {
			searcher = new IndexSearcher(dir);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (searcher != null) {
				try {
					searcher.close();
				} catch (Exception e) {
					logger.warn("Error closing IndexSearcher during indexExists check", e);
				}
			}
		}
	}
}
