package com.genexus.search;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import com.genexus.GxSilentTrnSdt;
import com.genexus.util.GXFile;

public class DocumentHandler {
	static IndexReader reader;
	static HashMap<String, Query> queries = new HashMap<String, Query>();

	public static String getText(String filename, String extension) {
		try {
			IDocumentHandler docHandler = null;
			extension = extension.toLowerCase();
			if (extension.startsWith("htm") || extension.startsWith(".htm")) {
				docHandler = new JTidyHTMLHandler();
			} else if (extension.startsWith("doc") || extension.startsWith(".doc")) {
				docHandler = new TextWordDocHandler();
			} else if (extension.startsWith("txt") || extension.startsWith(".txt")) {
				docHandler = new TextHandler();
			} else if (extension.startsWith("pdf") || extension.startsWith(".pdf")) {
				docHandler = new PdfHandler();
			}
			if (docHandler == null)
				return "";
			else
				return docHandler.getText(filename);
		} catch (Exception ex) {
			System.out.println("GetText Error " + ex.getMessage());
			return "";
		}
	}

	public static String htmlCleanFile(String fileName) {
		try {
			return new JTidyHTMLHandler().htmlClean(new FileInputStream(fileName));
		} catch (FileNotFoundException ex) {
			System.out.println(ex.getMessage());
			return "";
		}
	}

	public static String htmlClean(String text) {
		return new JTidyHTMLHandler().htmlClean(new StringBufferInputStream(text));
	}

	public static String htmlPreview(Object obj, String query, String textType, String preTag, String postTag,
			int fragmentSize, int maxNumFragments) {
		String text;
		try {
			if (obj instanceof GxSilentTrnSdt) {
				text = ((GxSilentTrnSdt) obj).getTransaction().toString();
			} else if (obj instanceof GXFile) {
				GXFile file = (GXFile) obj;
				text = DocumentHandler.getText(file.getAbsoluteName(), file.getExt());
			} else if (textType.toLowerCase().startsWith("htm")) {
				text = new JTidyHTMLHandler().getTextFromString(obj.toString());
			} else {
				text = obj.toString();
			}
			if (!query.equals("") && !text.equals("")) {
				QueryParser qp = new QueryParser(IndexRecord.CONTENTFIELD, Indexer.CreateAnalyzer());
				qp.setDefaultOperator(QueryParser.Operator.AND);

				Query unReWrittenQuery = qp.parse(query);
				Query q = unReWrittenQuery;
				try {
					if (reader == null) {
						reader = Indexer.getReader();
					}
					if (queries.get(query) != null) {
						q = (Query) queries.get(query);
					} else {
						q = unReWrittenQuery.rewrite(reader);// required to expand search terms (for the usage of
																// highlighting with wildcards)
						if (queries.size() == Integer.MAX_VALUE) {
							queries.clear();
						}
						queries.put(query, q);
					}
				} catch (Exception ex) {
				}

				QueryScorer scorer = new QueryScorer(q);

				SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(preTag, postTag);
				Highlighter highlighter = new Highlighter(formatter, scorer);
				Fragmenter fragmenter = new SimpleFragmenter(fragmentSize);
				highlighter.setTextFragmenter(fragmenter);

				TokenStream tokenStream = Indexer.CreateAnalyzer().tokenStream(IndexRecord.CONTENTFIELD,
						new StringReader(text));

				String result = highlighter.getBestFragments(tokenStream, text, maxNumFragments, "...");
				return result;
			} else {
				return text;
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return "";
		}
	}

}
