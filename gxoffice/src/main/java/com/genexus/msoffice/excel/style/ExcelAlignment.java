package com.genexus.msoffice.excel.style;

public class ExcelAlignment extends ExcelStyleDimension{
	public static final int VERTICAL_ALIGN_MIDDLE = 1;
	public static final int VERTICAL_ALIGN_TOP = 2;
	public static final int VERTICAL_ALIGN_BOTTOM = 3;
	public static final int HORIZONTAL_ALIGN_LEFT = 1;
	public static final int HORIZONTAL_ALIGN_CENTER = 2;
	public static final int HORIZONTAL_ALIGN_RIGHT = 3;
	
	private Integer horizontalAlignment;
	private Integer verticalAlignment;
	
	
	public ExcelAlignment() {
		
	}
		
	public Integer getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(Integer horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
		setChanged();
	}

	public Integer getVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(Integer verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
		setChanged();
	}

	
	
	
}
