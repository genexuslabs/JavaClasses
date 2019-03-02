package com.genexus;

import com.genexus.internet.*;

public class  GxRegexMatch
{
	private String value;
	private StringCollection gropus = new StringCollection();
	
	public GxRegexMatch()
	{
		this("", new StringCollection());
	}
	
	public GxRegexMatch(String value, StringCollection gropus)
	{
		this.value = value;
		this.gropus = gropus;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public StringCollection getGroups()
	{
		return gropus;
	}
}