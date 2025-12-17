package com.genexus.msoffice.excel;

import com.genexus.msoffice.excel.exception.ExcelException;

public class ExcelWorksheetGXWrapper implements IExcelWorksheet {
	private IExcelWorksheet _workSheet;

	public ExcelWorksheetGXWrapper() {
	}

	public ExcelWorksheetGXWrapper(IExcelWorksheet sheet) {
		_workSheet = sheet;
	}

	public String getName() {
		return _workSheet.getName();
	}

	public Boolean setHidden(boolean hidden) {
		return _workSheet.setHidden(hidden);
	}

	public Boolean isHidden() {
		return _workSheet.isHidden();
	}

	public Boolean rename(String newName) {
		return _workSheet.rename(newName);
	}

	public Boolean copy(String newName) {
		try {
			return _workSheet.copy(newName);
		}
		catch (ExcelException e) {
			return false;
		}
	}

	public void setProtected(String password) {
		_workSheet.setProtected(password);
	}
}
