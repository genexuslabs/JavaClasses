package com.genexus.search;

import org.apache.lucene.search.Hits;

public class SearchResult {
	protected static SearchResultCollection m_items;
	private static SearchResult m_empty = null;
	protected int m_maxresults;
	protected double m_elapsedTime;
	protected int m_itemsPerPage;
	protected int m_offset;

	public SearchResult() {
	}

	public SearchResult(Hits hits, int itemsPerPage, int pageNumber, double elapsedTime) {

		m_items = new SearchResultCollection(hits, itemsPerPage, pageNumber);
		m_itemsPerPage = itemsPerPage;
		m_elapsedTime = elapsedTime;
		if (pageNumber == 0 || pageNumber == 1) {
			m_offset = 0;
		} else {
			m_offset = itemsPerPage * (pageNumber - 1);
		}
		m_maxresults = (hits != null) ? hits.length() : 0;
	}

	public int getMaxitems() {
		return m_maxresults;
	}

	public double getElapsedtime() {
		return m_elapsedTime;
	}

	public SearchResultCollection items() {
		return m_items;
	}

	public static SearchResult getEmpty() {
		if (m_empty == null)
			m_empty = new EmptySearchResult();
		return m_empty;
	}

}
