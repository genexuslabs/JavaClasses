package com.genexus.CA.search;

import java.util.HashMap;

public class IndexManager {
   private static HashMap hash = new HashMap();

   public static void addContent(String dir, String uri, String lang, String title, String summary, byte fromFile, String body, String filePath) {
      getIndexer(dir).addContent(uri, lang, title, summary, fromFile, body, filePath);
   }

   public static void deleteContent(String dir, String uri) {
      getIndexer(dir).deleteContent(uri);
   }

   private static synchronized Indexer getIndexer(String dir) {
      Indexer indexer = null;
      if (hash.containsKey(dir)) {
         indexer = (Indexer)hash.get(dir);
      } else {
         indexer = new Indexer(dir);
         hash.put(dir, indexer);
      }

      return indexer;
   }
}
