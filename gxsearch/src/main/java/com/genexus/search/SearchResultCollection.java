package com.genexus.search;

import org.apache.lucene.search.Hits;

import com.genexus.GXSimpleCollection;
import com.genexus.xml.XMLReader;

public class SearchResultCollection extends GXSimpleCollection {
	private Hits m_hits;
	private int m_maxresults;
	private int m_itemsPerPage;
	private int m_offset;

	protected SearchResultCollection(Hits hits, int itemsPerPage, int pageNumber) {
		m_hits = hits;
		m_itemsPerPage = itemsPerPage;
		if (pageNumber == 0 || pageNumber == 1) {
			m_offset = 0;
		} else {
			m_offset = itemsPerPage * (pageNumber - 1);
		}
		m_maxresults = (m_hits != null) ? hits.length() : 0;
	}

	public SearchResultCollection() {
	}

	public int getItemCount() {
		return size();
	}

	public int size() {
		int count = 0;
		if (m_itemsPerPage == -1) {
			count = (m_hits != null) ? m_hits.length() : 0;
		} else {
			if (m_hits != null) {
				count = m_hits.length() - m_offset;
				if (count < 0) {
					count = 0;
				} else if (count > m_itemsPerPage) {
					count = m_itemsPerPage;
				}
			} else {
				count = 0;
			}
		}
		return count;
	}

	public Object elementAt(int index) {
		try {
			index = m_offset + index;
			if (index < m_maxresults && index >= 0) {
				return new SearchResultItem(m_hits.doc(index), m_hits.score(index));
			} else {
				throw new SearchException(SearchException.INDEXERROR);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Object item(int i) // throws SearchException, IOException
	{
		return elementAt(i - 1);
	}

	public short readxmlcollection(com.genexus.xml.XMLReader oReader, String sName, String itemName) {
		return 0;
	}

	public short readxml(XMLReader reader, String sName) {
		return 0;
	}
}
