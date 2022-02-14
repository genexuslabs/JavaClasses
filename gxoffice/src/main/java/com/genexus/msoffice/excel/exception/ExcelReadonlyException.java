package com.genexus.msoffice.excel.exception;

public class ExcelReadonlyException extends ExcelException
{
	public ExcelReadonlyException()
	{
		super(13, "Can not modify a readonly document");
	}
}
