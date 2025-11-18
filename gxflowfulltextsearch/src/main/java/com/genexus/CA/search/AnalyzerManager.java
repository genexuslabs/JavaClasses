package com.genexus.CA.search;

import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerManager {
   private static HashMap hash = new HashMap();

   public static Analyzer getAnalyzer(String lang) {
      Analyzer analyzer = null;
      if (hash.containsKey(lang)) {
         analyzer = (Analyzer)hash.get(lang);
      } else {
         if (lang.equals("spa")) {
            analyzer = new StandardAnalyzer();
         } else {
            analyzer = new StandardAnalyzer();
         }

         hash.put(lang, analyzer);
      }

      return (Analyzer)analyzer;
   }
}
