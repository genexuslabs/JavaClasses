package com.genexus.msoffice.excel;

import com.genexus.msoffice.excel.exception.ExcelDocumentNotSupported;
import com.genexus.msoffice.excel.exception.ExcelTemplateNotFoundException;
import com.genexus.msoffice.excel.poi.xssf.ExcelSpreadsheet;

import java.io.IOException;

public class ExcelFactory
{

	public static IExcelSpreadsheet create(IGXError handler, String filePath, String template)
			throws ExcelTemplateNotFoundException, IOException, ExcelDocumentNotSupported
	{
		if (filePath.endsWith(".xlsx") || !filePath.contains("."))
		{
			return new ExcelSpreadsheet(handler, filePath, template);
		}
		throw new ExcelDocumentNotSupported();
	}

}
