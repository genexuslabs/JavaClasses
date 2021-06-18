package com.genexus.msoffice.excel.style;

public class ExcelColor extends ExcelStyleDimension
{	
	private Integer _alpha = null;
	public Integer getAlpha() {
		return _alpha;
	}

	public Integer getRed() {
		return _red;
	}

	public Integer getGreen() {
		return _green;
	}

	public Integer getBlue() {
		return _blue;
	}

	private Integer _red = null;
	private Integer _green = null;
	private Integer _blue = null;
	
	public ExcelColor() {		
		
	}	
	
	public ExcelColor(int alpha, int r, int g, int b) {		
		setColorImpl(alpha, r, g, b);
	}	
	
	public boolean setColorRGB(int r, int g, int b) {		
		setColorImpl(0, r, g, b);			
		return true;
	}
	
	public boolean setColorARGB(int alpha, int r, int g, int b) {		
		setColorImpl(alpha, r, g, b);			
		return true;
	}

	private void setColorImpl(int alpha, int r, int g, int b)
	{
		this._alpha = alpha;
		this._red = r;
		this._green = g;
		this._blue = b;
		setChanged();
	}
	
	

}
