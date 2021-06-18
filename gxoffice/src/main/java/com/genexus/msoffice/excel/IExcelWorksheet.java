package com.genexus.msoffice.excel;

import com.genexus.msoffice.excel.exception.ExcelException;

public interface IExcelWorksheet
{
	public String getName();

	public Boolean isHidden();

	public Boolean rename(String newName);
	
	public Boolean copy(String newName) throws ExcelException;

	public void setProtected(String password);
}
