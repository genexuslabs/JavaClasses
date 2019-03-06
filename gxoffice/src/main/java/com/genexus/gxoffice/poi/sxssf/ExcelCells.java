
package com.genexus.gxoffice.poi.sxssf;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.*;

import com.genexus.gxoffice.IGxError;
import com.genexus.gxoffice.poi.xssf.StylesCache;


public class ExcelCells extends com.genexus.gxoffice.poi.xssf.ExcelCells{
	
    public ExcelCells(IGxError errAccess,ExcelDocument document, Workbook workBook, org.apache.poi.ss.usermodel.Sheet selectedSheet, int rowPos, int colPos, int height, int width, StylesCache stylesCache)
	{
        this(errAccess, document, workBook, selectedSheet, rowPos, colPos, height, width, false, stylesCache);
    }

	public ExcelCells(IGxError errAccess, ExcelDocument document, Workbook workBook, org.apache.poi.ss.usermodel.Sheet selectedSheet, int rowPos, int colPos, int height, int width, boolean readonly, StylesCache stylesCache)
	{
		doc = document;
		m_errAccess=errAccess;
		pWidth   = width;
		pHeight  = height;
		cntCells = 0;
                pColPos = colPos;
		pWorkbook = workBook;
                pSelectedSheet = selectedSheet;
                fitColumnWidth = true;
                this.readonly = readonly;
                this.stylesCache = stylesCache;
		pCells = new SXSSFCell[(width * height) + 1];
		try
		{
			for (int y = rowPos; y < (rowPos + pHeight); y++) {
				Row pRow = getExcelRow(selectedSheet, y);				
                                if (pRow != null)
                                {
                                       for (short x=(short)colPos; x<(colPos+pWidth); x++)
                                       {
                                              Cell pCell = getExcelCell(pRow, x);
                                              if (pCell != null)
                                              {
                                                     cntCells++;
                                                     pCells[cntCells] = pCell;
                                              }
                                       }
				}
			}
		} catch(Exception e)
		{
			m_errAccess.setErrDes("Invalid cell coordinates");
			m_errAccess.setErrCod((short)8);
		}
	}
	
}