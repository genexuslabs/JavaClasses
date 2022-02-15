package com.genexus.msoffice.excel.style;

public class ExcelStyle extends ExcelStyleDimension
{	
	private ExcelFill _cellFill;
	private ExcelFont _cellFont;
	private Boolean _locked;
	private Boolean _hidden;
	private Boolean _wrapText;
	private Boolean _shrinkToFit;
	private ExcelCellBorder _borders;
	private int _indentation = -1;
	private int _textRotation;
	private String _dataFormat;
	private ExcelAlignment _cellAlignment;
	
	public ExcelStyle() {		
		_cellFill = new ExcelFill();
		_cellFont = new ExcelFont();
		_cellAlignment = new ExcelAlignment();
		_borders = new ExcelCellBorder();
	}
	
	public Boolean isLocked() {
		return _locked;
	}

	public void setLocked(boolean value) {
		_locked = value;
	}

	public Boolean isHidden() {
		return _hidden;
	}

	public void setHidden(boolean value) {
		_hidden = value;
	}

	public ExcelAlignment getCellAlignment() {
		return _cellAlignment;
	}


	public ExcelFill getCellFill() {		
		return _cellFill;
	}
	
	public ExcelFont getCellFont() {		
		return _cellFont;
	}
	
	
	@Override
	public boolean isDirty() {
		return super.isDirty() || _cellFill.isDirty() || _cellFont.isDirty() || _cellAlignment.isDirty();
	}

	public Boolean getWrapText() {
		return _wrapText;
	}

	public void setWrapText(Boolean _wrapText) {
		this._wrapText = _wrapText;
	}

	public Boolean getShrinkToFit() {
		return _shrinkToFit;
	}

	public void setShrinkToFit(Boolean _shrinkToFit) {
		this._shrinkToFit = _shrinkToFit;
	}
	
	public int getTextRotation() {
		return _textRotation;
	}

	public void setTextRotation(int _textRotation) {
		this._textRotation = _textRotation;
	}

	public ExcelCellBorder getBorder() {
		return _borders;
	}

	public void setBorder(ExcelCellBorder _borders) {
		this._borders = _borders;
	}

	public int getIndentation() {
		return _indentation;
	}

	public void setIndentation(int _indentation) {
		this._indentation = _indentation;
	}

	public String getDataFormat() {
		return _dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this._dataFormat = dataFormat;
	}
}

