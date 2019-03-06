package com.genexus.search;

import java.util.Date;
import com.genexus.internet.StringCollection;

public interface ISearchResultItem
{
	String getId();
	String getType();
	Date getTimestamp();
	float getScore();
	StringCollection getKey();
}
