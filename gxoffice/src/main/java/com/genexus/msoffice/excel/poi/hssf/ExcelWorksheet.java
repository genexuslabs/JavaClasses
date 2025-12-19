package com.genexus.msoffice.excel.poi.hssf;

import com.genexus.msoffice.excel.exception.ExcelException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.genexus.msoffice.excel.IExcelWorksheet;

public class ExcelWorksheet implements IExcelWorksheet
{
	private HSSFSheet _sheet;

	public ExcelWorksheet()
	{
	
	}
	
	
	public ExcelWorksheet(HSSFSheet sheet)
	{
		_sheet = sheet;
	}

	public String getName()
	{
		return _sheet.getSheetName();
	}

	public Boolean setHidden(boolean hidden)
	{
		if (_sheet != null) {
			HSSFWorkbook wb = _sheet.getWorkbook();
			wb.setSheetHidden(sheetIndex(wb), hidden);
			return true;
		}
		return false;
	}

	public Boolean isHidden()
	{
		if (_sheet != null) {
			HSSFWorkbook wb = _sheet.getWorkbook();
			return wb.isSheetHidden(sheetIndex(wb));
		}
		return false;
	}

	public Boolean rename(String newName)
	{
		if (_sheet != null) {
			HSSFWorkbook wb = _sheet.getWorkbook();
			wb.setSheetName(wb.getSheetIndex(getName()), newName);
			return getName().equals(newName);
		}
		return false;
	}


	@Override
	public Boolean copy(String newName) {
		if (_sheet != null) {
			HSSFWorkbook wb = _sheet.getWorkbook();
			if(wb.getSheet(newName) == null) {
				wb.cloneSheet(wb.getSheetIndex(getName()));
				int newIdx = wb.getSheetIndex(wb.getSheetName(wb.getNumberOfSheets() - 1));
				wb.setSheetName(newIdx, newName);
				return true;
			}
		}
		return false;
	}

	@Override
	public void setProtected(String password) {
		if (_sheet != null) {
			if (password.length() == 0)
				_sheet.protectSheet(null);
			else
				_sheet.protectSheet(password);		
		}		
	}

	private int sheetIndex(HSSFWorkbook wb) {
		return wb.getSheetIndex(getName());
	}

}