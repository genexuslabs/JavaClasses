package com.genexus.msoffice.excel.style;

public class ExcelBorder extends ExcelStyleDimension {
	private ExcelColor borderColor;
	private String borderStyle = "";
		
	public String getBorder() {
		return borderStyle;
	}

	public void setBorder(String border) {
		this.borderStyle = border;	
		setChanged();
	}

	public ExcelBorder() {		
		borderColor = new ExcelColor();
	}

	public ExcelColor getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(ExcelColor borderColor) {
		this.borderColor = borderColor;
	}
	
	
}
