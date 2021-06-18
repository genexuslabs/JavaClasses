package com.genexus.msoffice.excel;

import java.util.Date;

import com.genexus.msoffice.excel.style.ExcelStyle;

public interface IExcelCellRange
{
	int getRowStart();

	int getRowEnd();

	int getColumnStart();

	int getColumnEnd();

	String getCellAdress();

	String getValueType();

	/*
	 *
	 * D: For date or datetime types C: For character type N: For numeric type U: If
	 * the type is unknown
	 */
	String getText();

	java.math.BigDecimal getNumericValue();

	Date getDateValue();

	Boolean setText(String value);

	Boolean setNumericValue(java.math.BigDecimal value);

	Boolean setDateValue(Date value);

	Boolean empty();

	Boolean mergeCells();

	Boolean setCellStyle(ExcelStyle style);

	ExcelStyle getCellStyle();

	
}
