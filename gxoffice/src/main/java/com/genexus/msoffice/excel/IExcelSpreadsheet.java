package com.genexus.msoffice.excel;

import java.util.List;

import com.genexus.msoffice.excel.exception.ExcelException;
import com.genexus.msoffice.excel.poi.xssf.ExcelWorksheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface IExcelSpreadsheet
{
	// General Methods
	Boolean save() throws ExcelException;

	Boolean saveAs(String newFileName) throws ExcelException;

	Boolean close() throws ExcelException;

	// CellMethods
	IExcelCellRange getCells(IExcelWorksheet worksheet, int startRow, int startCol, int rowCount, int colCount) throws ExcelException;

	IExcelCellRange getCell(IExcelWorksheet worksheet, int startRow, int startCol) throws ExcelException;

	Boolean insertRow(IExcelWorksheet worksheet, int rowIdx, int rowCount);

	Boolean deleteRow(IExcelWorksheet worksheet, int rowIdx);

	// Columns not supported
	// Boolean insertColumn(IExcelWorksheet worksheet, int rowIdx, int
	// colIdx);
	Boolean deleteColumn(IExcelWorksheet worksheet, int colIdx);

	// Worksheets
	List<ExcelWorksheet> getWorksheets();
	ExcelWorksheet getWorkSheet(String name);

	Boolean insertWorksheet(String newSheetName, int idx) throws ExcelException;
	Boolean getAutofit();
	void setAutofit(boolean autofit);

	void setColumnWidth(IExcelWorksheet worksheet, int colIdx, int width);
	void setRowHeight(IExcelWorksheet worksheet, int rowIdx, int height);

	boolean setActiveWorkSheet(String name);

	boolean deleteSheet(int sheetIdx);

	boolean deleteSheet(String sheetName);

	boolean toggleColumn(IExcelWorksheet worksheet, int colIdx, Boolean visible);

	boolean toggleRow(IExcelWorksheet _currentWorksheet, int i, Boolean visible);
	boolean cloneSheet(String sheetName, String newSheetName) throws ExcelException;

	XSSFWorkbook getUnderlyingObject();
}
