package com.genexus.msoffice.excel.style;

public class ExcelCellBorder {
	private ExcelBorder borderTop = new ExcelBorder();
	private ExcelBorder borderBottom  = new ExcelBorder();
	private ExcelBorder borderLeft = new ExcelBorder();
	private ExcelBorder borderRight = new ExcelBorder();
	private ExcelBorder borderDiagonalUp = new ExcelBorder();
	private ExcelBorder borderDiagonalDown = new ExcelBorder();


	public void setAll(ExcelBorder borderStyle) {
		borderTop = borderStyle;
		borderBottom = borderStyle;
		borderLeft = borderStyle;
		borderRight = borderStyle;
	}
	
	public ExcelBorder getBorderBottom() {
		return borderBottom;
	}
	
	public void setBorderBottom(ExcelBorder borderBottom) {
		this.borderBottom = borderBottom;
	}
	
	public ExcelBorder getBorderTop() {
		return borderTop;
	}
	
	public void setBorderTop(ExcelBorder borderTop) {
		this.borderTop = borderTop;
	}
	
	public ExcelBorder getBorderLeft() {
		return borderLeft;
	}
	
	public void setBorderLeft(ExcelBorder borderLeft) {
		this.borderLeft = borderLeft;
	}
	
	public ExcelBorder getBorderRight() {
		return borderRight;
	}
	public void setBorderRight(ExcelBorder borderRight) {
		this.borderRight = borderRight;
	}

	public ExcelBorder getBorderDiagonalUp() {
		return borderDiagonalUp;
	}
	public void setBorderDiagonalUp(ExcelBorder borderDiagonal) {
		this.borderDiagonalUp = borderDiagonal;
	}

	public ExcelBorder getBorderDiagonalDown() {
		return borderDiagonalDown;
	}
	public void setBorderDiagonalDown(ExcelBorder borderDiagonal) {
		this.borderDiagonalDown = borderDiagonal;
	}
}
