package com.genexus.webpanels;

import java.io.*;

public class HTMLLabel extends HTMLObject implements ILabel 
{
	private String caption;
	private int autoresize;

	public HTMLLabel(GXWebPanel webPanel)
	{
		super(webPanel);
	}

	public String getCaption()
	{
		return caption;
	}

	public int getAutoresize()
	{
		return this.autoresize;
	}

	public void setAutoresize(int autoresize)
	{
		this.autoresize = autoresize;
	}	

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

}