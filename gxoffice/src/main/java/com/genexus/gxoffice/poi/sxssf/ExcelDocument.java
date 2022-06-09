package com.genexus.gxoffice.poi.sxssf;

import com.genexus.util.GxFileInfoSourceType;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.genexus.gxoffice.Constants;
import com.genexus.gxoffice.IExcelCells;
import com.genexus.gxoffice.IGxError;
import com.genexus.gxoffice.poi.xssf.StylesCache;
import com.genexus.util.GXFile;

public class ExcelDocument extends com.genexus.gxoffice.poi.xssf.ExcelDocument {

	public short Open(String fileName) {
		resetError();
		if (fileName.indexOf('.') == -1) {
			fileName += ".xlsx";
		}

		try {
			if (!template.equals("")) {
				GXFile templateFile = new GXFile(template);
				if (templateFile.exists()) {
					workBook = new SXSSFWorkbook(new XSSFWorkbook(templateFile.getStream()));
				} else {
					errCod = 4; // Invalid template
					errDescription = "Invalid template.";
					return errCod;
				}
			} else {
				GXFile file = new GXFile("", fileName, Constants.EXTERNAL_UPLOAD_ACL, GxFileInfoSourceType.Unknown);
				if (file.exists()) {
					// System.out.println("Opening..");
					workBook = new SXSSFWorkbook(new XSSFWorkbook(file.getStream()));
				} else {
					// System.out.println("Creating..");
					workBook = new SXSSFWorkbook();
				}
			}
			this.selectFirstSheet();
			xlsFileName = fileName.toString();
			stylesCache = new StylesCache(workBook);
		}

		catch (Exception e) {
			errCod = 10; // error creando xlsx file
			errDescription = "Could not open file.";
			System.err.println("GXOffice Error: " + e.toString());
			return errCod;
		}
		return 0;
	}

	public IExcelCells Cells(int Row, int Col) {

		ExcelCells iCell = new ExcelCells((IGxError) this, this, workBook, workBook.getSheet(currentSheet), Row - 1,
				Col - 1, 1, 1, isReadOnly(), stylesCache);
		return iCell;

	}

	public IExcelCells Cells(int Row, int Col, int Height, int Width) {

		ExcelCells iCell = new ExcelCells((IGxError) this, this, workBook, workBook.getSheet(currentSheet), Row - 1,
				Col - 1, Height, Width, isReadOnly(), stylesCache);
		return iCell;
	}

	public IExcelCells getCells(int Row, int Col, int Height, int Width) {
		ExcelCells iCell = new ExcelCells(this, this, workBook, workBook.getSheet(currentSheet), Row - 1, Col - 1,
				Height, Width, isReadOnly(), stylesCache);
		return iCell;
	}

}