package com.genexus.webpanels;

public class HTMLCheckbox extends HTMLObject implements ICheckbox
{
	private String checkedValue;
	private String value;
	private String caption;

	public HTMLCheckbox(GXWebPanel webPanel)
	{
		super(webPanel);
	}

	public void setCheckedValue(String checkedValue)
	{
		this.checkedValue = checkedValue;
	}
	
	public String getCheckedValue()
	{
		return this.checkedValue;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public String getCaption()
	{
		return this.caption;
	}
}