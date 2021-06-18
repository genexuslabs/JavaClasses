package com.genexus.msoffice.excel.exception;

import com.genexus.msoffice.excel.ErrorCodes;

public class ExcelTemplateNotFoundException extends ExcelException
{
	public ExcelTemplateNotFoundException()
	{
		super(ErrorCodes.TEMPLATE_NOT_FOUND, "Template not found");
	}
}
