package com.genexus.search;


public class EmptySearchResult extends SearchResult {

    public EmptySearchResult() {
        m_items = new SearchResultCollection();
    }

    public int getCount() {
        return 0;
    }

}
