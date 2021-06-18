package com.genexus.msoffice.excel.poi.xssf;

import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.genexus.msoffice.excel.IExcelWorksheet;

public class ExcelWorksheet implements IExcelWorksheet
{
	private XSSFSheet _sheet;

	public ExcelWorksheet()
	{
	
	}
	
	
	public ExcelWorksheet(XSSFSheet sheet)
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
			XSSFWorkbook wb = _sheet.getWorkbook();
			wb.setSheetVisibility(sheetIndex(wb), (hidden)? SheetVisibility.HIDDEN: SheetVisibility.VISIBLE);
			return true;
		}
		return false;
	}

	public Boolean isHidden()
	{
		if (_sheet != null) {
			XSSFWorkbook wb = _sheet.getWorkbook();
			SheetVisibility sheetVisibility = wb.getSheetVisibility(sheetIndex(wb));
			return sheetVisibility.compareTo(SheetVisibility.HIDDEN) == 0;
		}
		return false;
	}

	public Boolean rename(String newName)
	{
		if (_sheet != null) {
			XSSFWorkbook wb = _sheet.getWorkbook();
			wb.setSheetName(wb.getSheetIndex(getName()), newName);
			return getName().equals(newName);
		}
		return false;
	}


	@Override
	public Boolean copy(String newName) {
		if (_sheet != null) {
			XSSFWorkbook wb = _sheet.getWorkbook();
			wb.cloneSheet(wb.getSheetIndex(getName()), newName);
			return true;
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

	private int sheetIndex(XSSFWorkbook wb) {
		return wb.getSheetIndex(getName());
	}

}
