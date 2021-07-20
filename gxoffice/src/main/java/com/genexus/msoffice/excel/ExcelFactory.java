package com.genexus.msoffice.excel;

import com.genexus.Application;
import com.genexus.msoffice.excel.poi.xssf.ExcelSpreadsheet;
import com.genexus.msoffice.excel.exception.ExcelDocumentNotSupported;
import com.genexus.msoffice.excel.exception.ExcelTemplateNotFoundException;
import com.genexus.util.GXServices;

import java.io.File;
import java.io.IOException;

public class ExcelFactory
{


	public static IExcelSpreadsheet create(IGXError handler, String filePath, String template)
			throws ExcelTemplateNotFoundException, IOException, ExcelDocumentNotSupported
	{
		filePath = resolvePath(filePath);
		template = resolvePath(template);

		if (filePath.endsWith(".xlsx") || !filePath.contains("."))
		{
			return new ExcelSpreadsheet(handler, filePath, template);
		}
		throw new ExcelDocumentNotSupported();
	}

	private static String resolvePath(String filePath) {
		if (filePath.isEmpty()) {
			return "";
		}
		if (new File(filePath).isAbsolute())
		{
			return filePath;
		}
		if (Application.getExternalProvider() != null) {
			return filePath;
		}

		if (com.genexus.ModelContext.getModelContext() != null) {
			com.genexus.internet.HttpContext webContext = (com.genexus.internet.HttpContext) com.genexus.ModelContext
				.getModelContext().getHttpContext();
			if ((webContext != null) && (webContext instanceof com.genexus.webpanels.HttpContextWeb)) {
				filePath = ((com.genexus.webpanels.HttpContextWeb) webContext).getRealPath(filePath);
			}
		}

		return filePath;
	}

}
