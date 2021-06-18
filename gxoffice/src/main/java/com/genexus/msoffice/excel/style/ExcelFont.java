package com.genexus.msoffice.excel.style;

public class ExcelFont extends ExcelStyleDimension
{	
	private String fontFamily = null;
	private Boolean italic = null;
	private Integer size = null;
	private Boolean strike = null;
	private Boolean underline = null;
	private Boolean bold = null;
	private ExcelColor color = null;

	public ExcelFont() {
		color = new ExcelColor();
	}
	
	public String getFontFamily()
	{
		return fontFamily;
	}
	public void setFontFamily(String fontFamily)
	{
		this.fontFamily = fontFamily;
		setChanged();
	}
	public Boolean getItalic()
	{
		return italic;
	}
	public void setItalic(Boolean italic)
	{
		this.italic = italic;
		setChanged();
	}
	public Integer getSize()
	{
		return size;
	}
	public void setSize(Integer size)
	{
		this.size = size;
		setChanged();
	}
	public Boolean getStrike()
	{
		return strike;
	}
	public void setStrike(Boolean strike)
	{
		this.strike = strike;
		setChanged();
	}
	public Boolean getUnderline()
	{
		return underline;
	}
	public void setUnderline(Boolean underline)
	{
		this.underline = underline;
		setChanged();
	}
	public Boolean getBold()
	{
		return bold;
	}
	public void setBold(Boolean bold)
	{
		this.bold = bold;
		setChanged();
	}
	public ExcelColor getColor()
	{
		return color;
		
	}

	
}
