package com.genexus.search;

public interface ISearchResultCollection {
	int getItemCount();

	SearchResultItem item(int index); // throws SearchException, IOException;
}
