package com.genexus.msoffice.excel.exception;

import com.genexus.msoffice.excel.ErrorCodes;

public class ExcelDocumentNotSupported extends ExcelException
{
	public ExcelDocumentNotSupported()
	{
		super(ErrorCodes.EXTENSION_NOT_SUPPORTED, "File extension not supported");
	}
}
