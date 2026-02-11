package com.genexus.CA.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public final class Indexer {
   private String indexDirectory = ".";
   private static final int OPERATION_INDEX = 1;
   private static final int OPERATION_DELETE = 2;

   private static final Logger logger = LogManager.getLogger(Indexer.class);

   Indexer(String directory) {
      this.indexDirectory = normalizeIndexDirectory(directory);
      if (!this.indexExists(this.indexDirectory)) {
         IndexWriter writer = null;
         try {
            writer = new IndexWriter(this.indexDirectory, new StandardAnalyzer(), true);
         } catch (Exception e) {
            logger.error("Error creating index directory: {}", this.indexDirectory, e);
         }
         finally {
            closeWriter(writer);
         }
      }

   }

   void addContent(String uri, String lang, String title, String summary, byte fromFile, String body, String filePath) {
		Document doc = new Document();
		StringBuilder contentBuilder = new StringBuilder();
      boolean fileContentRead = false;
      String normalizedUri = normalizeUri(uri);
      String normalizedLang = normalizeLang(lang);

		if (fromFile == 1 && filePath != null && !filePath.trim().isEmpty()) {
			String lowerFilePath = filePath.toLowerCase();
			try {
            if (this.isDocxExtension(lowerFilePath)) {
					try (FileInputStream file = new FileInputStream(filePath); XWPFDocument reader = new XWPFDocument(file)) {
						for (XWPFParagraph p : reader.getParagraphs()) {
							contentBuilder.append(p.getText()).append(" ");
						}
                  fileContentRead = true;
					}
				} else if (this.isPdfExtension(lowerFilePath)) {
					try (PDDocument document = Loader.loadPDF(new File(filePath))) {
						PDFTextStripper tStripper = new PDFTextStripper();
						contentBuilder.append(tStripper.getText(document));
                  fileContentRead = true;
					}
				} else if (this.isTxtExtension(lowerFilePath)) {
               contentBuilder.append(readTextFile(filePath));
               fileContentRead = true;
				}
			} catch (IOException e) {
            logger.error("Error reading file content from: {}", filePath, e);
			}
		}

      if (body != null && !body.isEmpty() && !fileContentRead) {
			contentBuilder.append(body);
		}
	
		String content = contentBuilder.toString();

      this.indexOperation(OPERATION_DELETE, normalizedLang, null, normalizedUri);

      doc.add(new Field("uri", normalizedUri, Store.YES, Index.UN_TOKENIZED));
      doc.add(new Field("language", normalizedLang, Store.YES, Index.UN_TOKENIZED));
		doc.add(new Field("title", title == null ? "" : title, Store.YES, Index.TOKENIZED));
		doc.add(new Field("summary", summary == null ? "" : summary, Store.YES, Index.TOKENIZED));
		doc.add(new Field("content", content, Store.YES, Index.TOKENIZED));

		try {
         this.indexOperation(OPERATION_INDEX, normalizedLang, doc, null);
		} catch (Exception e) {
         logger.error("Error indexing content. uri={}, lang={}", normalizedUri, normalizedLang, e);
		}
   }

   void deleteContent(String uri) {
      try {
		 this.indexOperation(OPERATION_DELETE, null, null, normalizeUri(uri));
      } catch (Exception e) {
         logger.error("Error deleting content. uri={}", uri, e);
      }

   }

   private synchronized void indexOperation(int op, String lang, Document doc, String uri) {
      switch(op) {
      case OPERATION_INDEX:
         IndexWriter writer = null;
         try {
            writer = new IndexWriter(this.getIndexDirectory(), AnalyzerManager.getAnalyzer(lang), false);
            writer.addDocument(doc);
            // writer.optimize(); // This is a costly operation and should not be done for every document.
         } catch (Exception e) {
            logger.error("Error indexing document. uri={}, lang={}", uri, lang, e);
         } finally {
            closeWriter(writer);
         }
         break;
      case OPERATION_DELETE:
         IndexReader reader = null;
         try {
            Term term = null;
            int docId = 0;
            if (lang == null) {
               term = new Term("uri", uri);
            } else {
               docId = this.getDocumentId(uri, lang);
            }

            reader = IndexReader.open(this.getIndexDirectory());
            if (lang == null) {
               reader.deleteDocuments(term);
            } else if (docId != -1) {
               reader.deleteDocument(docId);
            }

         } catch (Exception e) {
            logger.error("Error deleting document. uri={}, lang={}", uri, lang, e);
         } finally {
            if (reader != null) {
               try {
                  reader.close();
               } catch (IOException e) {
                  logger.error("Error closing IndexReader", e);
               }
            }
         }
		 break;
      }

   }

   public String getIndexDirectory() {
      return this.indexDirectory;
   }

   private String normalizeIndexDirectory(String dir) {
      if (dir == null || dir.trim().isEmpty()) {
         return ".";
      }
      return new File(dir).getAbsolutePath();
   }

   private boolean indexExists(String dir) {
      IndexSearcher searcher = null;
      try {
         searcher = new IndexSearcher(dir);
         return true;
      } catch (IOException e) {
         return false;
      }
      finally {
         if (searcher != null) {
            try {
               searcher.close();
            } catch (IOException e) {
               logger.error("Error closing IndexSearcher", e);
            }
         }
      }
   }

   private int getDocumentId(String uri, String lang) {
      int documentId = -1;

      try {
         Hits hits = this.getHits(uri, lang);
         if (hits.length() > 0) {
            documentId = hits.id(0);
         }
      } catch (IOException e) {
         logger.error("Error getting document id. uri={}, lang={}", uri, lang, e);
      }

      return documentId;
   }

   private boolean isDocxExtension(String filePath) {
      return filePath.toLowerCase().endsWith(".docx");
   }

   private Hits getHits(String uri, String lang) {
      IndexSearcher searcher = null;
      Hits hits = null;
      try {
         searcher = new IndexSearcher(this.indexDirectory);
         BooleanQuery query = new BooleanQuery();
         query.add(new TermQuery(new Term("uri", uri)), Occur.MUST);
         if (lang != null && !lang.trim().isEmpty()) {
            query.add(new TermQuery(new Term("language", lang)), Occur.MUST);
         }
         hits = searcher.search(query);
      } catch (IOException e) {
         logger.error("Error searching hits. uri={}, lang={}", uri, lang, e);
      } finally {
         if (searcher != null) {
            try {
               searcher.close();
            } catch (IOException e) {
               logger.error("Error closing IndexSearcher", e);
            }
         }
      }

      return hits;
   }

   private String normalizeUri(String uri) {
      if (uri == null) {
         return "";
      }
      return uri.trim().toLowerCase();
   }

   private String normalizeLang(String lang) {
      if (lang == null) {
         return "";
      }
      return lang.trim().toLowerCase();
   }

   private String readTextFile(String filePath) throws IOException {
      StringBuilder builder = new StringBuilder();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filePath)), StandardCharsets.UTF_8))) {
         String line;
         while ((line = reader.readLine()) != null) {
            builder.append(line).append(' ');
         }
      }
      return builder.toString();
   }

   private boolean isPdfExtension(String filePath) {
      return filePath.toLowerCase().endsWith(".pdf");
   }

   private boolean isTxtExtension(String filePath) {
      String lowerFilePath = filePath.toLowerCase();
      return lowerFilePath.endsWith(".txt") || lowerFilePath.endsWith(".html");
   }

   private void closeWriter(IndexWriter writer) {
      if (writer != null) {
         try {
            writer.close();
         } catch (IOException e) {
            logger.error("Error closing IndexWriter", e);
         }
      }
   }
}
