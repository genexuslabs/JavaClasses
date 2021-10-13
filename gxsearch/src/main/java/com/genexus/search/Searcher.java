package com.genexus.search;

import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

import com.genexus.ModelContext;

public class Searcher {
	private Analyzer m_analyzer = Indexer.CreateAnalyzer();
	private static Searcher m_instance = new Searcher();
	private IndexSearcher m_searcher;

	private Searcher() {
	}

	public static Searcher getInstance() {
		return m_instance;
	}

	private String TranslateQuery(String query, ModelContext context) {
		/*
		 * try{ Pattern p; try { String rex = "\\([^\\(\\)]*\\)"; p =
		 * Pattern.compile(rex, Pattern.MULTILINE); } catch (PatternSyntaxException e) {
		 * return query; } Matcher m = p.matcher(query); while (m.find()) { String value
		 * = m.group(0); for (int i = 1; i <= m.groupCount(); i++) { if (m.group(i) !=
		 * null) { String match = m.group(i); String translatedStr =
		 * context.getHttpContext().getMessage(match.substring(1,match.length() - 2));
		 * query = GXutil.strReplace(query, match,translatedStr); } }
		 * 
		 * } return query; }catch (Exception e) { return query; }
		 */
		return query;
	}

	public SearchResult search(String query, int maxresults) {
		return search(query, maxresults, 0, null);
	}

	public SearchResult search(String query, int itemsPerPage, int pageNumber, ModelContext context) {
		if (query == null || query.length() == 0)
			return SearchResult.getEmpty();

		if (context != null) {
			query = TranslateQuery(query, context);
		}
		IndexSearcher searcher = getSearcher();
		if (searcher == null)
			return SearchResult.getEmpty();

		QueryParser qp = new QueryParser(IndexRecord.CONTENTFIELD, m_analyzer);
		qp.setAllowLeadingWildcard(true);
		qp.setDefaultOperator(QueryParser.Operator.AND);
		try {
			Query q = qp.parse(IndexRecord.processContent(query));
			Date t1 = new Date();
			Hits hits = searcher.search(q, Sort.RELEVANCE);
			Date t2 = new Date();
			// searcher.close();
			// Closing the IndexSearcher is best only after a deleteDocuments with a reader
			// or changes with a writer.
			// For performance reasons, it is better to not close the IndexSearcher if not
			// needed
			return new SearchResult(hits, itemsPerPage, pageNumber, t2.getTime() - t1.getTime());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void close() {
		if (m_searcher != null) {
			try {
				m_searcher.close();
				m_searcher = null;
			} catch (Exception ex) {
			}
		}
	}

	private IndexSearcher getSearcher() {
		try {
			if (m_searcher == null)
				m_searcher = new IndexSearcher(Settings.getInstance().getIndexFolder());
			return m_searcher;
		} catch (Exception ex) {
			return null;
		}
	}
}
