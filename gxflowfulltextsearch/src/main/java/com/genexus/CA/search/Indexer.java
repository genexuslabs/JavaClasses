package com.genexus.CA.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public final class Indexer {
   private String indexDirectory = ".";
   private static final int IDX = 1;
   private static final int DLT = 2;

   private static final Logger logger = LogManager.getLogger("Indexer.class");

   protected Indexer(String directory) {
      this.indexDirectory = directory;
      if (!this.indexExists(directory)) {
         try {
            this.indexDirectory = directory;
            IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer(), true);
            writer.close();
         } catch (Exception var3) {
            logger.error(var3.getMessage(), var3);
         }
      }

   }

   protected void addContent(String uri, String lang, String title, String summary, byte fromFile, String body, String filePath) {
      Document doc = null;
      doc = new Document();
      String content = "";
      if (fromFile == 1) {
         try {
            if (this.isMicrosoftExtension(filePath)) {
               FileInputStream file = new FileInputStream(filePath);
               XWPFDocument reader = new XWPFDocument(file);
               List<XWPFParagraph> data = reader.getParagraphs();

               XWPFParagraph p;
               for(Iterator<XWPFParagraph> var14 = data.iterator(); var14.hasNext(); content = content + p.getText()) {
                  p = var14.next();
               }
            } else if (this.isPdfExtension(filePath)) {
               PDDocument document = Loader.loadPDF(new File(filePath));
               new PDFTextStripperByArea();
               PDFTextStripper tStripper = new PDFTextStripper();
               content = content + tStripper.getText(document);
            }
         } catch (IOException var16) {
            var16.printStackTrace();
         }
      }

	   if (this.documentExists(uri, lang)) {
		   this.indexOperation(2, lang, (Document) null, uri.toLowerCase());
	   }

	   doc.add(new Field("uri", uri, Store.YES, Index.UN_TOKENIZED));
	   doc.add(new Field("content", content, Store.YES, Index.TOKENIZED));

	   try {
		  this.indexOperation(1, lang, doc, (String)null);
	   } catch (Exception var15) {
		  logger.error(var15.getMessage(), var15);
	   }

   }

   protected void deleteContent(String uri) {
      try {
         this.indexOperation(2, (String)null, (Document)null, uri.toLowerCase());
      } catch (Exception var3) {
         logger.error(var3.getMessage(), var3);
      }

   }

   protected synchronized void indexOperation(int op, String lang, Document doc, String uri) {
      switch(op) {
      case 1:
         try {
            IndexWriter writer = new IndexWriter(this.getIndexDirectory(), AnalyzerManager.getAnalyzer(lang), false);
            writer.addDocument(doc);
            writer.optimize();
            writer.close();
         } catch (Exception var9) {
            logger.error(var9.getMessage(), var9);
         }
         break;
      case 2:
         try {
            Term term = null;
            int docId = 0;
            if (lang == null) {
               term = new Term("uri", uri);
            } else {
               docId = this.getDocumentId(uri, lang);
            }

            IndexReader reader = IndexReader.open(this.getIndexDirectory());
            if (lang == null) {
               reader.deleteDocuments(term);
            } else if (docId != -1) {
               reader.deleteDocument(docId);
            }

            reader.close();
         } catch (Exception var8) {
            logger.error(var8.getMessage(), var8);
         }
      }

   }

   public String getIndexDirectory() {
      return this.indexDirectory;
   }

   private boolean indexExists(String dir) {
      try {
         new IndexSearcher(dir);
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   private boolean documentExists(String uri, String lang) {
      boolean value = false;

	  Hits hits = getHits(uri, lang);
	  if (hits.length() > 0) {
		  value = true;
	  }

      return value;
   }

   private int getDocumentId(String uri, String lang) {
      int value = -1;

      try {
         Hits  hits = this.getHits(uri, lang);
         if (hits.length() > 0) {
            value = hits.id(0);
         }
      } catch (IOException var7) {
         logger.error(var7.getMessage(), var7);
      }

      return value;
   }

   private boolean isMicrosoftExtension(String filePath) {
      return filePath.endsWith(".doc") || filePath.endsWith(".docx") || filePath.endsWith(".xls") || filePath.endsWith(".xlsx") || filePath.endsWith(".ppt") || filePath.endsWith(".pptx");
   }

   private Hits getHits(String uri, String lang) {
	   IndexSearcher searcher = null;
	   Hits hits = null;
	   try {
		   searcher = new IndexSearcher(this.indexDirectory);
		   BooleanQuery query = new BooleanQuery();
		   query.add(new TermQuery(new Term("uri", uri)), Occur.MUST);
		   query.add(new TermQuery(new Term("language", lang)), Occur.MUST);
		   hits = searcher.search(query);
		   searcher.close();
	   } catch (IOException e) {
		   logger.error(e.getMessage(), e);
	   }

	   return hits;
   }

   private boolean isPdfExtension(String filePath) {
      return filePath.endsWith(".pdf");
   }

   private boolean isTxtExtension(String filePath) {
      return filePath.endsWith(".txt") || filePath.endsWith(".html");
   }
}
