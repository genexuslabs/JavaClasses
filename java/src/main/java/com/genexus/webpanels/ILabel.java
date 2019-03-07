package com.genexus.webpanels;

public interface ILabel extends IHTMLObject
{
	String getCaption();
	int getAutoresize();
	void setCaption(String caption);
	void setAutoresize(int autoresize);
}
