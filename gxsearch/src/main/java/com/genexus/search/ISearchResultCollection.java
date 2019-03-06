package com.genexus.search;

import java.util.Collection;
import java.io.IOException;

public interface ISearchResultCollection
{
	int getItemCount();
	SearchResultItem item(int index); //throws SearchException, IOException;
}
