package com.genexus.CA.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SearcherTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void returnsEmptyXmlWhenIndexIsMissing() {
        File missing = new File(temporaryFolder.getRoot(), "missing-index");

        String result = Searcher.search(missing.getAbsolutePath(), "en", "anything", 10, 0);

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Results hits=\"0\" time=\"0ms\"></Results>", result);
    }

    @Test
    public void searchesAndEscapesUrisUsingLanguageFilter() throws IOException {
        String indexDir = temporaryFolder.newFolder("index-escapes").getAbsolutePath();
        Indexer indexer = new Indexer(indexDir);
        indexer.addContent("HTTP://Example.com/?a=1&b=<tag>", "en", "Title", "Summary", (byte) 0, "alpha", null);
        indexer.addContent("other-uri", "es", "Title", "Summary", (byte) 0, "alpha", null);

        String result = Searcher.search(indexDir, "en", "alpha", 10, 0);

        assertTrue("Expected one hit with English language", result.contains("hits=\"1\""));
        assertTrue("URI should be lower-cased and XML-escaped", result.contains("<URI>http://example.com/?a=1&amp;b=&lt;tag&gt;</URI>"));
        assertFalse("Non-matching language should be filtered out", result.contains("other-uri"));
    }

    @Test
    public void doesNotReturnResultsWhenMaxResultsOrFromAreNegative() throws IOException {
        String indexDir = temporaryFolder.newFolder("index-negative").getAbsolutePath();
        Indexer indexer = new Indexer(indexDir);
        indexer.addContent("uri-negative", "en", "Title", "Summary", (byte) 0, "beta", null);

        String result = Searcher.search(indexDir, "en", "beta", -1, -5);

        assertTrue("Hits should reflect actual matches even when not returned", result.contains("hits=\"1\""));
        assertFalse("No results should be returned when maxResults becomes zero", result.contains("<Result>"));
    }
}
