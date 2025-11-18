package com.genexus.CA.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
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

   protected Indexer(String directory) {
      this.indexDirectory = directory;
      if (!this.indexExists(directory)) {
         try {
            this.indexDirectory = directory;
            IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer(), true);
            writer.close();
         } catch (Exception var3) {
            Logger.print(var3.toString());
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
               for(Iterator var14 = data.iterator(); var14.hasNext(); content = content + p.getText()) {
                  p = (XWPFParagraph)var14.next();
               }
            } else if (this.isPdfExtension(filePath)) {
               PDDocument document = Loader.loadPDF(new File(filePath));
               new PDFTextStripperByArea();
               PDFTextStripper tStripper = new PDFTextStripper();
               content = content + tStripper.getText(document);
            } else if (this.isTxtExtension(filePath)) {
               File txt = new File(filePath);

               String st;
               for(BufferedReader br = new BufferedReader(new FileReader(txt)); (st = br.readLine()) != null; content = content + st) {
               }
            }
         } catch (IOException var16) {
            var16.printStackTrace();
         }
      }

      if (doc != null) {
         if (this.documentExists(uri, lang)) {
            this.indexOperation(2, lang, (Document)null, uri.toLowerCase());
         }

         doc.add(new Field("uri", uri, Store.YES, Index.UN_TOKENIZED));
         doc.add(new Field("content", content, Store.YES, Index.TOKENIZED));

         try {
            this.indexOperation(1, lang, doc, (String)null);
         } catch (Exception var15) {
            Logger.print(var15.toString());
         }
      }

   }

   protected void deleteContent(String uri) {
      try {
         this.indexOperation(2, (String)null, (Document)null, uri.toLowerCase());
      } catch (Exception var3) {
         Logger.print(var3.toString());
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
            Logger.print(var9.toString());
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
            Logger.print(var8.toString());
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

      try {
         IndexSearcher searcher = new IndexSearcher(this.indexDirectory);
         BooleanQuery query = new BooleanQuery();
         query.add(new TermQuery(new Term("uri", uri)), Occur.MUST);
         query.add(new TermQuery(new Term("language", lang)), Occur.MUST);
         Hits hits = searcher.search(query);
         searcher.close();
         if (hits.length() > 0) {
            value = true;
         }
      } catch (IOException var7) {
         Logger.print(var7.toString());
      }

      return value;
   }

   private int getDocumentId(String uri, String lang) {
      int value = -1;

      try {
         IndexSearcher searcher = new IndexSearcher(this.indexDirectory);
         BooleanQuery query = new BooleanQuery();
         query.add(new TermQuery(new Term("uri", uri)), Occur.MUST);
         query.add(new TermQuery(new Term("language", lang)), Occur.MUST);
         Hits hits = searcher.search(query);
         if (hits.length() > 0) {
            value = hits.id(0);
         }

         searcher.close();
      } catch (IOException var7) {
         Logger.print(var7.toString());
      }

      return value;
   }

   private boolean isMicrosoftExtension(String filePath) {
      return filePath.endsWith(".doc") || filePath.endsWith(".docx") || filePath.endsWith(".xls") || filePath.endsWith(".xlsx") || filePath.endsWith(".ppt") || filePath.endsWith(".pptx");
   }

   private boolean isPdfExtension(String filePath) {
      return filePath.endsWith(".pdf");
   }

   private boolean isTxtExtension(String filePath) {
      return filePath.endsWith(".txt") || filePath.endsWith(".html");
   }
}
