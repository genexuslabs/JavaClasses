package com.genexus.CA.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

public class Searcher {
	private static final Logger logger = LogManager.getLogger("Searcher.class");

	public static String search(String dir, String lang, String query, int maxResults, int from) {
		StringBuilder buff = new StringBuilder();

		try {
			IndexSearcher searcher = new IndexSearcher(dir);
			String[] fields = new String[]{"title", "content"};
			Occur[] clauses = new Occur[]{Occur.SHOULD, Occur.SHOULD};
			Query q = MultiFieldQueryParser.parse(query, fields, clauses, AnalyzerManager.getAnalyzer(lang));
			if (!lang.equals("IND")) {
				Query q2 = new TermQuery(new Term("language", lang));
				BooleanQuery bq = new BooleanQuery();
				bq.add((Query) q, Occur.MUST);
				bq.add(q2, Occur.MUST);
				q = bq;
			}

			Hits hits = searcher.search((Query) q);
			String time = "";
			int max = hits.length();
			buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			buff.append("<Results hits = '").append(max).append("' time = '").append(time).append("'>");

			for (int i = 0; i < max; ++i) {
				buff.append("<Result>");
				Document doc = hits.doc(i);
				buff.append("<URI>").append(doc.getField("uri").stringValue()).append("</URI>");
				buff.append("</Result>");
			}
		} catch (Exception var15) {
			logger.error(var15.getMessage(), var15);
		}

		buff.append("</Results>");
		return buff.toString();
	}
}
