package com.genexus.msoffice.excel;

import com.genexus.msoffice.excel.style.ExcelStyle;

import java.util.Date;

public class ExcelCellsGXWrapper implements IExcelCellRange {
	private IExcelCellRange _cellRange;

	public ExcelCellsGXWrapper() {
	}

	public ExcelCellsGXWrapper(IExcelCellRange cellRange) {
		_cellRange = cellRange;
	}

	@Override
	public int getRowStart() {
		return _cellRange.getRowStart();
	}

	@Override
	public int getColumnStart() {
		return _cellRange.getColumnStart();
	}

	@Override
	public int getRowEnd() {
		return _cellRange.getRowEnd();
	}

	@Override
	public int getColumnEnd() {
		return _cellRange.getColumnEnd();
	}

	@Override
	public String getValueType() {
		return _cellRange.getValueType();
	}

	@Override
	public String getText() {
		return _cellRange.getText();
	}

	@Override
	public Boolean setText(String value) {
		return _cellRange.setText(value);
	}

	@Override
	public java.math.BigDecimal getNumericValue() {
		return _cellRange.getNumericValue();
	}

	@Override
	public Boolean setNumericValue(java.math.BigDecimal d) {
		return _cellRange.setNumericValue(d);
	}

	@Override
	public Date getDateValue() {
		return _cellRange.getDateValue();
	}

	@Override
	public Boolean setDateValue(Date value) {
		return _cellRange.setDateValue(value);
	}

	@Override
	public String getHyperlinkValue() {
		return _cellRange.getHyperlinkValue();
	}

	@Override
	public Boolean setHyperlinkValue(String value) {
		return _cellRange.setHyperlinkValue(value);
	}

	@Override
	public Boolean empty() {
		return _cellRange.empty();
	}

	@Override
	public Boolean mergeCells() {
		return _cellRange.mergeCells();
	}

	@Override
	public Boolean setCellStyle(ExcelStyle newCellStyle) {
		return _cellRange.setCellStyle(newCellStyle);
	}
}
