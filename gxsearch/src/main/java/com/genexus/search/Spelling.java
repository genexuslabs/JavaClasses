package com.genexus.search;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;

public class Spelling {
	private static Spelling m_instance = new Spelling();

	private Spelling() {
	}

	public static Spelling getInstance() {
		return m_instance;
	}

	public boolean buildDictionary() {
		try {
			IndexReader my_luceneReader = IndexReader.open(Settings.getInstance().getIndexFolder());
			SpellChecker spell = getSpelling();
			if (spell != null)
				spell.indexDictionary(new LuceneDictionary(my_luceneReader, IndexRecord.CONTENTFIELD));
			my_luceneReader.close();
			return true;
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

	public String suggest(String phrase) {
		try {
			StringBuffer res = new StringBuffer();
			StringTokenizer stok = new StringTokenizer(phrase, " ", false);
			SpellChecker spell = getSpelling();
			while (stok.hasMoreTokens()) {
				String word = stok.nextToken();
				if (spell != null) {
					String[] similar = spell.suggestSimilar(word, 1);
					if (similar != null && similar.length > 0)
						res.append(similar[0]);
				}
			}
			return res.toString();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
			return "";
		}
	}

	private SpellChecker getSpelling() {
		try {
			String dictionaryFolder = Settings.getInstance().getIndexFolder() + File.separator + "Dictionary";
			File dic = new File(dictionaryFolder);
			if (!dic.exists()) {
				dic.mkdirs();
			}
			return new SpellChecker(FSDirectory.getDirectory(dictionaryFolder));
		} catch (Exception ex) {
			return null;
		}
	}
}
