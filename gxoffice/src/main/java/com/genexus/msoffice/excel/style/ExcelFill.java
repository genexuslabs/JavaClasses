package com.genexus.msoffice.excel.style;

public class ExcelFill  extends ExcelStyleDimension
{
	private ExcelColor cellBackColor;

	public ExcelFill() {
		
		cellBackColor = new ExcelColor();
	}
	
	public ExcelColor getCellBackColor() {
		return cellBackColor;
	}

	@Override
	public boolean isDirty() {
		return super.isDirty() || cellBackColor.isDirty();
	}
	
}
