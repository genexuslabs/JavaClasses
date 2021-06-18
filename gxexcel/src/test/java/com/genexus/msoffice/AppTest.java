package com.genexus.msoffice;

import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.genexus.msoffice.excel.ExcelSpreadsheetGXWrapper;
import com.genexus.msoffice.excel.IExcelCellRange;
import com.genexus.msoffice.excel.style.ExcelStyle;
import org.junit.Ignore;
import org.junit.Test;

import com.genexus.msoffice.excel.poi.xssf.ExcelCells;
import com.genexus.msoffice.excel.poi.xssf.ExcelWorksheet;
import com.genexus.msoffice.excel.style.ExcelAlignment;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */

public class AppTest {
    private static String basePath = System.getProperty("user.dir") + File.separatorChar + "excel" + File.separatorChar;

    static {
        com.genexus.specific.java.Connect.init();
    }

    @Test
    public void testNumberFormat1() {
        ExcelSpreadsheetGXWrapper excel = create("testNumberFormat1");
        excel.getCells(1, 1, 1, 1).setNumericValue(BigDecimal.valueOf(123.456));
        excel.getCells(2, 1, 1, 1).setNumericValue(BigDecimal.valueOf(1));
        excel.getCells(3, 1, 1, 1).setNumericValue(BigDecimal.valueOf(100));

        excel.getCells(4, 1, 1, 1).setNumericValue(BigDecimal.valueOf(0.123));
        excel.save();

    }

    @Test
    public void testCellStyle1() {
		ExcelSpreadsheetGXWrapper excel = create("testCellStyle1");
        excel.setColumnWidth(1, 100);
        excel.getCells(2, 1, 1, 5).setNumericValue(BigDecimal.valueOf(123.456));
        ExcelStyle newCellStyle = new ExcelStyle();
        newCellStyle.getCellFont().setBold(true);
        excel.getCells(2, 1, 1, 5).setCellStyle(newCellStyle);

        boolean ok = excel.save();
        assertTrue(ok);
    }

    @Test
    public void testCellStyle2() {
		ExcelSpreadsheetGXWrapper excel = create("testCellStyle2");
        excel.setColumnWidth(1, 100);
        excel.getCells(2, 1, 5, 5).setNumericValue(BigDecimal.valueOf(123.456));
        ExcelStyle newCellStyle = new ExcelStyle();
        newCellStyle.getCellFont().setBold(true);
        excel.getCells(2, 1, 3, 3).setCellStyle(newCellStyle);

        excel.save();

    }

    @Test
    public void testInsertSheets() {
		ExcelSpreadsheetGXWrapper excel = create("testInsertSheets");
        boolean ok = excel.insertSheet("test1");
        assertTrue(ok);
        ok = excel.insertSheet("test2");
        assertTrue(ok);
        ok = excel.insertSheet("test1");
        assertFalse(ok);
        excel.save();
    }

    @Test
    public void testInsertDuplicateSheets() {
		ExcelSpreadsheetGXWrapper excel = create("testInsertDuplicateSheets");
        boolean ok = excel.insertSheet("test1");
        assertTrue(ok);
        ok = excel.insertSheet("test1");
        assertFalse(ok);
        logErrorCodes(excel);
        ok = excel.insertSheet("test1");
        logErrorCodes(excel);
        assertFalse(ok);
        excel.save();
    }

    @Test
    public void testActiveWorksheet() {
		ExcelSpreadsheetGXWrapper excel = create("ActiveWorksheet");
        excel.getCells(2, 1, 5, 5).setNumericValue(BigDecimal.valueOf(123.456));
        excel.insertSheet("test1");

        excel.insertSheet("test2");
        excel.insertSheet("test3");
        excel.setCurrentWorksheetByName("test2");
        excel.getCells(2, 1, 5, 5).setNumericValue(new java.math.BigDecimal(3));
        excel.save();

    }

    @Test
    public void testOpenAndSave() {
		ExcelSpreadsheetGXWrapper excel = create("testActive");
        try {
            excel.getCells(2, 1, 5, 5).setDate(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        excel.save();
    }

    @Test
    public void testFolderNotExists() {
        String excel1 = basePath + File.separatorChar + "notexistsFolder" + File.separatorChar + "test-active";
        ExcelSpreadsheetGXWrapper excel = new ExcelSpreadsheetGXWrapper();
        excel.open(excel1);

        try {
            excel.getCells(2, 1, 5, 5).setDate(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean saved = excel.save();

        assertFalse(saved);
        assertNotSame(0, excel.getErrCode());
        assertNotSame("", excel.getErrDescription());
    }

    @Test
    public void testWithoutExtensions() {
        String excel1 = basePath + "test_withoutextension";
        ensureFileDoesNotExists(excel1 + ".xlsx");
        ExcelSpreadsheetGXWrapper excel = new ExcelSpreadsheetGXWrapper();
        excel.open(excel1);
        excel.insertSheet("genexus0");
        excel.insertSheet("genexus1");
        excel.insertSheet("genexus2");

        List<ExcelWorksheet> wSheets = excel.getWorksheets();
        assertTrue(wSheets.size() == 3);
        assertTrue(wSheets.get(0).getName() == "genexus0");
        assertTrue(wSheets.get(1).getName() == "genexus1");
        assertTrue(wSheets.get(2).getName() == "genexus2");

        excel.save();

    }

    @Test
    public void testInsertSheet() {
		ExcelSpreadsheetGXWrapper excel = create("testInsertSheet");
        excel.insertSheet("genexus0");
        excel.insertSheet("genexus1");
        excel.insertSheet("genexus2");

        List<ExcelWorksheet> wSheets = excel.getWorksheets();
        assertTrue(wSheets.size() == 3);
        assertTrue(wSheets.get(0).getName() == "genexus0");
        assertTrue(wSheets.get(1).getName() == "genexus1");
        assertTrue(wSheets.get(2).getName() == "genexus2");

        excel.save();

    }


    @Test
    public void testDeleteSheet() {
		ExcelSpreadsheetGXWrapper excel = create("testDeleteSheet");
        excel.insertSheet("gx1");
        excel.insertSheet("gx2");
        excel.insertSheet("gx3");
        excel.insertSheet("gx4");

        List<ExcelWorksheet> wSheets = excel.getWorksheets();
        assertTrue(wSheets.size() == 4);
        assertTrue(wSheets.get(0).getName() == "gx1");
        assertTrue(wSheets.get(1).getName() == "gx2");
        assertTrue(wSheets.get(2).getName() == "gx3");
        excel.deleteSheet(2);
        wSheets = excel.getWorksheets();
        assertTrue(wSheets.get(0).getName() == "gx1");
        assertTrue(wSheets.get(1).getName() == "gx3");
        excel.save();

    }

    @Test
    public void testSetCellValues() {
		ExcelSpreadsheetGXWrapper excel = create("testSetCellValues");
        excel.setAutofit(true);
        excel.getCells(1, 1, 1, 1).setNumericValue(new java.math.BigDecimal(100));
        excel.getCells(2, 1, 1, 1).setText("hola!");
        excel.getCells(3, 1, 1, 1).setDateValue(new Date());
        excel.getCells(4, 1, 1, 1).setNumericValue(BigDecimal.valueOf(66.78));

        excel.save();
        excel.close();
        // Verify previous Excel Document
        excel = open("testSetCellValues");

        assertEquals(100, excel.getCells(1, 1, 1, 1).getNumericValue().intValue());

        assertEquals("No Coindicen", excel.getCells(2, 1, 1, 1).getText(), "hola!");
        excel.save();
    }

    @Test
    public void testFormulas() {
		ExcelSpreadsheetGXWrapper excel = create("testFormulas");
        excel.setAutofit(true);
        excel.getCell(1, 1).setNumericValue(new java.math.BigDecimal(5));
        excel.getCell(2, 1).setNumericValue(new java.math.BigDecimal(6));
        excel.getCell(3, 1).setText("=A1+A2");
        excel.save();
        excel.close();
        // Verify previous Excel Document
        excel = open("testFormulas");

        assertEquals(11, excel.getCell(3, 1).getNumericValue().intValue());

        excel.save();
    }


    @Test
    public void testExcelCellRange() {
		ExcelSpreadsheetGXWrapper excel = create("testExcelCellRange");
        IExcelCellRange cellRange = excel.getCells(2, 2, 5, 10);

        assertEquals(2, cellRange.getColumnStart(), 0);
        assertEquals(11, cellRange.getColumnEnd(), 0);
        assertEquals(2, cellRange.getRowStart(), 0);
        assertEquals(6, cellRange.getRowEnd(), 0);
        excel.close();
    }


    @Test
    @Ignore
    public void testCellRangeCellAddress() {
        //Pending Implementation..
    }


    @Test
    public void testSetCurrentWorksheetByName() {
		ExcelSpreadsheetGXWrapper excel = create("testSetCurrentWorksheetByName");
        excel.insertSheet("hoja1");
        excel.insertSheet("hoja2");
        excel.insertSheet("hoja3");
        excel.save();
        excel.close();
        excel = open("testSetCurrentWorksheetByName");
		excel.setCurrentWorksheetByName("hoja2");
        assertEquals("hoja2", excel.getCurrentWorksheet().getName());
        excel.getCell(5, 5).setText("hola");
        excel.save();
        excel.close();


		excel = open("testSetCurrentWorksheetByName");
        excel.setCurrentWorksheetByName("hoja2");
        assertEquals("hola", excel.getCell(5, 5).getText());

        excel.setCurrentWorksheetByName("hoja1");
        assertEquals("", excel.getCell(5, 5).getText());
        excel.close();
    }

    @Test
    public void testSetCurrentWorksheetByIdx() {
		ExcelSpreadsheetGXWrapper excel = create("testSetCurrentWorksheetByIdx");
        excel.insertSheet("hoja1");
        excel.insertSheet("hoja2");
        excel.insertSheet("hoja3");
        excel.save();
        excel.close();
        excel = open("testSetCurrentWorksheetByIdx");
        excel.setCurrentWorksheet(2);
        assertEquals("hoja2", excel.getCurrentWorksheet().getName());
        excel.getCell(5, 5).setText("hola");
        excel.save();
        excel.close();


		excel = open("testSetCurrentWorksheetByIdx");

        boolean ok = excel.setCurrentWorksheet(2);
        assertEquals("hola", excel.getCell(5, 5).getText());
        assertEquals(true, ok);

        ok = excel.setCurrentWorksheet(1);
        assertEquals(true, ok);
        ok = excel.setCurrentWorksheet(3);
        assertEquals(true, ok);
        ok = excel.setCurrentWorksheet(4);
        assertEquals(false, ok);
        ok = excel.setCurrentWorksheet(5);
        assertEquals(false, ok);
        ok = excel.setCurrentWorksheet(0);
        assertEquals(false, ok);
        excel.close();
    }


    @Test
    public void testCopySheet() {
		ExcelSpreadsheetGXWrapper excel = create("testCopySheet");

        excel.insertSheet("hoja1");
        excel.setCurrentWorksheetByName("hoja1");
        excel.getCells(1, 1, 3, 3).setText("test");
        excel.insertSheet("hoja2");
        excel.insertSheet("hoja3");
        excel.save();
        excel.close();
        excel = open("testCopySheet");
        excel.setCurrentWorksheetByName("hoja1");
        excel.getCurrentWorksheet().copy("hoja1Copia");
        excel.save();
        excel.close();
		excel = open("testCopySheet");
        excel.setCurrentWorksheetByName("hoja1Copia");
        assertEquals("No Coindicen", excel.getCells(1, 1, 3, 3).getText(), "test");
        excel.close();
    }

    @Test
    public void testGetWorksheets() {
		ExcelSpreadsheetGXWrapper excel = create("testGetWorksheets");
        excel.insertSheet("hoja1");
        excel.insertSheet("hoja2");
        excel.insertSheet("hoja3");
        excel.insertSheet("hoja4");
        excel.save();
        excel.close();
        excel = open("testGetWorksheets");
        List<ExcelWorksheet> sheets = excel.getWorksheets();
        assertEquals("hoja1", sheets.get(0).getName());
        assertEquals("hoja2", sheets.get(1).getName());
        assertEquals("hoja3", sheets.get(2).getName());
        assertEquals("hoja4", sheets.get(3).getName());
        excel.close();
    }

    @Test
    public void testHiddenCells() {
        ExcelSpreadsheetGXWrapper excel = create("testHiddenCells");

        excel.setAutofit(true);
        excel.insertSheet("hoja1");
        excel.setCurrentWorksheetByName("hoja1");
        excel.getCurrentWorksheet().setProtected("password");
        excel.getCells(1, 1, 3, 3).setText("texto no se puede editar");
        ExcelStyle style = new ExcelStyle();
        style.setHidden(true);
        excel.getCells(1, 1, 3, 3).setCellStyle(style);


        ExcelCells cells = excel.getCells(5, 1, 3, 3);
        cells.setText("texto SI se puede editar");
        style = new ExcelStyle();
        style.setLocked(false);
        cells.setCellStyle(style);
        excel.save();
        excel.close();
    }

    @Test
    public void testProtectSheet() {
        ExcelSpreadsheetGXWrapper excel = create("testProtectSheet");
        excel.setAutofit(true);
        excel.insertSheet("hoja1");
        excel.setCurrentWorksheetByName("hoja1");
        excel.getCurrentWorksheet().setProtected("password");
        excel.getCells(1, 1, 3, 3).setText("texto no se puede editar");
        ExcelStyle style = new ExcelStyle();
        style.setLocked(true);
        excel.getCells(1, 1, 3, 3).setCellStyle(style);


        ExcelCells cells = excel.getCells(5, 1, 3, 3);
        cells.setText("texto SI se puede editar");
        style = new ExcelStyle();
        style.setLocked(false);
        cells.setCellStyle(style);
        excel.save();
        excel.close();
    }

	private ExcelSpreadsheetGXWrapper create(String fileName){
		String excelPath = basePath + fileName + ".xlsx";
		ensureFileDoesNotExists(excelPath);
		File theDir = new File(basePath);
		if (!theDir.exists()){
			theDir.mkdirs();
		}
		assertTrue("File must not exist: " + fileName, !new File(excelPath).exists());
    	ExcelSpreadsheetGXWrapper excel = new ExcelSpreadsheetGXWrapper();
		excel.open(excelPath);
		return excel;
	}

    private ExcelSpreadsheetGXWrapper open(String fileName){
		String excelPath = basePath + fileName + ".xlsx";
		assertTrue("Cannot open file. File does not exists: " + fileName, new File(excelPath).exists());
        ExcelSpreadsheetGXWrapper excel = new ExcelSpreadsheetGXWrapper();
        excel.open(excelPath);
        return excel;
    }

    @Test
    public void testHideSheet() {
        ExcelSpreadsheetGXWrapper excel = create("testHideSheet");
        excel.setAutofit(true);
        excel.insertSheet("hoja1");
        excel.insertSheet("hoja2");
        excel.insertSheet("hoja3");
        excel.insertSheet("hoja4");
        excel.setCurrentWorksheetByName("hoja2");

        assertFalse(excel.getCurrentWorksheet().isHidden());
        assertTrue(excel.getCurrentWorksheet().setHidden(true));
        assertTrue(excel.getCurrentWorksheet().isHidden());

        excel.setCurrentWorksheetByName("hoja1");
        excel.save();
        excel.close();
    }


    @Test
    public void testCloneSheet() {
		ExcelSpreadsheetGXWrapper excel = create("testCloneSheet");
        excel.insertSheet("hoja1");
        excel.getCell(1, 1).setText("1");
        excel.insertSheet("hoja2");
        excel.getCell(1, 1).setText("2");
        excel.insertSheet("hoja3");
        excel.cloneSheet("hoja2", "cloned_hoja2");
        excel.save();
        excel.close();
        excel = open("testCloneSheet");
        List<ExcelWorksheet> sheets = excel.getWorksheets();
        assertEquals(4, sheets.size());
        excel.close();
    }

    @Test
    public void testCloneSheet2() {
		ExcelSpreadsheetGXWrapper excel = create("testCloneSheet2");
        excel.getCell(2, 2).setText("hello");
        boolean ok = excel.cloneSheet(excel.getCurrentWorksheet().getName(), "clonedSheet");
        assertTrue(ok);
        excel.save();
        excel.close();
        excel = open("testCloneSheet2");
        List<ExcelWorksheet> sheets = excel.getWorksheets();
        assertEquals(2, sheets.size());
        excel.close();
    }

    @Test
    public void testCloneSheetError() {
		ExcelSpreadsheetGXWrapper excel = create("testCloneSheetError");
        excel.insertSheet("hoja1");
        excel.getCell(1, 1).setText("1");
        excel.insertSheet("hoja2");
        excel.getCell(1, 1).setText("2");
        excel.insertSheet("hoja3");
        excel.cloneSheet("hoja2", "cloned_hoja2");
        excel.cloneSheet("hoja2", "hoja2");
        excel.save();
        excel.close();
        excel = open("testCloneSheetError");
        List<ExcelWorksheet> sheets = excel.getWorksheets();
        assertEquals(4, sheets.size());
        excel.close();
    }

    @Test
    public void testWorksheetRename() {
		ExcelSpreadsheetGXWrapper excel = create("testWorksheetRename");
        excel.getCurrentWorksheet().rename("defaultsheetrenamed");
        excel.insertSheet("hoja1");
        excel.insertSheet("hoja2");
        excel.insertSheet("hoja3");
        excel.insertSheet("hoja4");

        excel.save();
        excel.close();
        excel = open("testWorksheetRename");
        excel.getWorksheets().get(3).rename("modificada");
        excel.save();
        excel.close();
		excel = open("testWorksheetRename");
        List<ExcelWorksheet> sheets = excel.getWorksheets();
        assertEquals("hoja1", sheets.get(1).getName());
        assertEquals("hoja2", sheets.get(2).getName());
        assertEquals("modificada", sheets.get(3).getName());
        assertEquals("hoja4", sheets.get(4).getName());
        excel.close();
    }

    @Test
    public void testMergeCells() {
		ExcelSpreadsheetGXWrapper excel = create("testMergeCells");
        excel.getCells(2, 10, 10, 5).mergeCells();
        excel.getCells(2, 10, 10, 5).setText("merged cells");
        excel.save();
        excel.close();
    }

    @Test
    public void testMergeCellsError() {
        ExcelSpreadsheetGXWrapper excel = create("testMergeCellsError");
        excel.getCells(2, 10, 10, 5).mergeCells();
        excel.getCells(2, 10, 10, 5).mergeCells();
        excel.getCells(2, 10, 10, 5).mergeCells();
        excel.getCells(3, 11, 2, 2).mergeCells();
        excel.getCells(2, 10, 10, 5).mergeCells();

        excel.getCells(2, 10, 10, 5).setText("merged cells");
        excel.save();
        excel.close();
    }

    @Test
    public void testColumnAndRowHeight() {
		ExcelSpreadsheetGXWrapper excel = create("testColumnAndRowHeight");
        excel.getCells(1, 1, 5, 5).setText("texto de las celdas largo");
        excel.setRowHeight(2, 50);
        excel.setColumnWidth(1, 100);
        excel.save();
        excel.close();
    }

    @Test
    public void testAlignment() {
		ExcelSpreadsheetGXWrapper excel = create("testAlignment");
        excel.getCells(2, 2, 3, 3).setText("a");
        ExcelStyle style = new ExcelStyle();
        style.getCellAlignment().setHorizontalAlignment(ExcelAlignment.HORIZONTAL_ALIGN_RIGHT); //center
        style.getCellAlignment().setVerticalAlignment(ExcelAlignment.VERTICAL_ALIGN_MIDDLE); //middle
        excel.getCells(2, 2, 3, 3).setCellStyle(style);
        excel.save();
        excel.close();

    }

    @Test
    public void testExcelCellStyle() {
		ExcelSpreadsheetGXWrapper excel = create("testExcelCellStyle");

        IExcelCellRange cells = excel.getCells(1, 1, 2, 2);

        ExcelStyle style = new ExcelStyle();

        cells.setText("texto muy largo");
        style.getCellAlignment().setHorizontalAlignment(3);
        style.getCellFont().setBold(true);
        style.getCellFont().setItalic(true);
        style.getCellFont().setSize(18);
        style.getCellFont().getColor().setColorRGB(1, 1, 1);
        style.getCellFill().getCellBackColor().setColorRGB(210, 180, 140);
        style.setTextRotation(5);

        style.setWrapText(true);
        cells.setCellStyle(style);
        excel.setColumnWidth(1, 70);
        excel.setRowHeight(1, 45);
        excel.setRowHeight(2, 45);

        cells = excel.getCells(5, 2, 4, 4);

        cells.setText("texto2");
        style = new ExcelStyle();
        style.setIndentation(5);
        style.getCellFont().setSize(10);
        style.getCellFont().getColor().setColorRGB(255, 255, 255);
        style.getCellFill().getCellBackColor().setColorRGB(90, 90, 90);

        cells.setCellStyle(style);


        cells = excel.getCells(10, 2, 2, 2);
        cells.setText("texto3");
        style = new ExcelStyle();
        style.getCellFont().setBold(false);
        style.getCellFont().setSize(10);
        style.getCellFont().getColor().setColorRGB(180, 180, 180);
        style.getCellFill().getCellBackColor().setColorRGB(45, 45, 45);
        style.setTextRotation(-20);
        cells.setCellStyle(style);


        excel.save();
        excel.close();

    }


    @Test
    public void testExcelBorderStyle() {
		ExcelSpreadsheetGXWrapper excel = create("testExcelBorderStyle");
        IExcelCellRange cells = excel.getCells(5, 2, 4, 4);
        cells.setText("texto2");

		ExcelStyle style = new ExcelStyle();
        style.getCellFont().setSize(10);

        style.getBorder().getBorderTop().setBorder("THICK");
        style.getBorder().getBorderTop().getBorderColor().setColorRGB(220, 20, 60);

        style.getBorder().getBorderDiagonalUp().setBorder("THIN");
        style.getBorder().getBorderDiagonalUp().getBorderColor().setColorRGB(220, 20, 60);

        style.getBorder().getBorderDiagonalDown().setBorder("THIN");
        style.getBorder().getBorderDiagonalDown().getBorderColor().setColorRGB(220, 20, 60);

        cells.setCellStyle(style);

        cells = excel.getCells(10, 2, 2, 2);
        cells.setText("texto3");
        style = new ExcelStyle();

        style.getCellFont().setBold(false);
        style.getCellFont().setSize(10);
        style.getCellFont().getColor().setColorRGB(180, 180, 180);

        cells.setCellStyle(style);


        excel.save();
        excel.close();

    }

    @Test
    public void testNumberFormat() {
		ExcelSpreadsheetGXWrapper excel = create("testNumberFormat");
        ExcelStyle style = new ExcelStyle();
        style.setDataFormat("#.##");
        style.getCellFont().setBold(true);
        excel.getCell(1, 1).setNumericValue(new java.math.BigDecimal(1.123456789));
        excel.getCell(1, 1).setCellStyle(style);
        excel.getCell(2, 1).setNumericValue(new java.math.BigDecimal(20000.123456789));

        excel.save();
        excel.close();
    }

    @Test
    public void testInsertRow() {
		ExcelSpreadsheetGXWrapper excel = create("testInsertRow");

        excel.getCell(1, 1).setNumericValue(new java.math.BigDecimal(1));
        excel.getCell(2, 1).setNumericValue(new java.math.BigDecimal(2));
        excel.getCell(3, 1).setNumericValue(new java.math.BigDecimal(3));
        excel.getCell(4, 1).setNumericValue(new java.math.BigDecimal(4));
        excel.getCell(5, 1).setNumericValue(new java.math.BigDecimal(5));
        excel.save();
        excel.close();
        // Verify previous Excel Document
        excel = open("testInsertRow");

        assertEquals(2, excel.getCell(2, 1).getNumericValue().intValue());
        excel.insertRow(2, 2);
        assertEquals(2, excel.getCell(4, 1).getNumericValue().intValue());
        excel.save();
    }

    @Test
    public void testDeleteRow() {
		ExcelSpreadsheetGXWrapper excel = create("testDeleteRow");

        excel.getCells(1, 1, 1, 5).setNumericValue(new java.math.BigDecimal(1));
        excel.getCells(2, 1, 1, 5).setNumericValue(new java.math.BigDecimal(2));
        excel.getCells(3, 1, 1, 5).setNumericValue(new java.math.BigDecimal(3));
        excel.getCells(4, 1, 1, 5).setNumericValue(new java.math.BigDecimal(4));
        excel.save();
        excel.close();
        // Verify previous Excel Document
        excel = open("testDeleteRow");

        assertEquals(1, excel.getCell(1, 1).getNumericValue().intValue());
        assertEquals(2, excel.getCell(2, 1).getNumericValue().intValue());
        excel.deleteRow(2);
        excel.save();
		excel = open("testDeleteRow");
        assertEquals(3, excel.getCell(2, 1).getNumericValue().intValue());
        excel.save();
    }

    @Test
    public void testDeleteRow2() {
		ExcelSpreadsheetGXWrapper excel = create("testDeleteRow2");

        excel.getCell(2, 2).setText("hola");
        excel.save();
        excel.close();
        // Verify previous Excel Document
		excel = open("testDeleteRow2");
        assertEquals("hola", excel.getCell(2, 2).getText());
        boolean result = excel.deleteRow(1);
        assertTrue(result);
        excel.save();
        excel.close();
		excel = open("testDeleteRow2");
        assertEquals("hola", excel.getCell(1, 2).getText());
        excel.save();
    }


    @Test
    public void testHideRow() {
		ExcelSpreadsheetGXWrapper excel = create("testHideRow");

        excel.getCell(1, 1).setNumericValue(new java.math.BigDecimal(1));

        excel.getCell(2, 1).setNumericValue(new java.math.BigDecimal(2));

        excel.getCell(3, 1).setNumericValue(new java.math.BigDecimal(3));

        excel.save();
        excel.close();
        // Verify previous Excel Document
        excel = open("testHideRow");

        assertEquals(1, excel.getCell(1, 1).getNumericValue().intValue());
        excel.toggleRow(2, false);
        //assertEquals(7, excel.getCell(1, 1).getNumericValue().intValue());
        excel.save();
    }

    @Test
    public void testHideColumn() {
		ExcelSpreadsheetGXWrapper excel = create("testHideColumn");

        excel.getCell(1, 1).setNumericValue(new java.math.BigDecimal(1));
        excel.getCell(2, 1).setNumericValue(new java.math.BigDecimal(1));
        excel.getCell(3, 1).setNumericValue(new java.math.BigDecimal(1));

        excel.getCell(1, 2).setNumericValue(new java.math.BigDecimal(2));
        excel.getCell(2, 2).setNumericValue(new java.math.BigDecimal(2));
        excel.getCell(3, 2).setNumericValue(new java.math.BigDecimal(2));

        excel.getCell(1, 3).setNumericValue(new java.math.BigDecimal(3));
        excel.getCell(2, 3).setNumericValue(new java.math.BigDecimal(3));
        excel.getCell(3, 3).setNumericValue(new java.math.BigDecimal(3));

        excel.save();
        excel.close();
        // Verify previous Excel Document
        excel = open("testHideColumn");

        assertEquals(1, excel.getCell(2, 1).getNumericValue().intValue());
        excel.toggleColumn(2, false);
        //assertEquals(7, excel.getCell(1, 1).getNumericValue().intValue());
        excel.save();
    }

    @Test
    public void testDeleteColumn() {
		ExcelSpreadsheetGXWrapper excel = create("testDeleteColumn");

        excel.getCell(1, 1).setNumericValue(new java.math.BigDecimal(1));
        excel.getCell(2, 1).setNumericValue(new java.math.BigDecimal(1));
        excel.getCell(3, 1).setNumericValue(new java.math.BigDecimal(1));

        excel.getCell(1, 2).setNumericValue(new java.math.BigDecimal(2));
        excel.getCell(2, 2).setNumericValue(new java.math.BigDecimal(2));
        excel.getCell(3, 2).setNumericValue(new java.math.BigDecimal(2));

        excel.getCell(1, 3).setNumericValue(new java.math.BigDecimal(3));
        excel.getCell(2, 3).setNumericValue(new java.math.BigDecimal(3));
        excel.getCell(3, 3).setNumericValue(new java.math.BigDecimal(3));

        excel.save();
        excel.close();
        // Verify previous Excel Document
        excel = open("testDeleteColumn");

        assertEquals(2, excel.getCell(2, 2).getNumericValue().intValue());
        assertTrue(excel.deleteColumn(2));
        assertEquals(3, excel.getCell(2, 2).getNumericValue().intValue());
        excel.save();
    }

    @Test
    public void testDeleteColumn2() {
        ExcelSpreadsheetGXWrapper excel = create("exceldeletecolumn");
        excel.deleteColumn(2);
        excel.save();
    }

    @Test
    public void testSaveAs() {
        ExcelSpreadsheetGXWrapper excel = create("testSaveAs");
        excel.getCells(1, 1, 15, 15).setNumericValue(new BigDecimal(100));
        String excelNew = basePath + "testSaveAsCopy.xlsx";
        excel.saveAs(excelNew);
        excel.close();
        assertEquals(new File(excelNew).exists(), true);

    }

    private void logErrorCodes(ExcelSpreadsheetGXWrapper excel) {
       // System.out.println(String.format("%s - %s", excel.getErrCode(), excel.getErrDescription()));
    }

    private void ensureFileDoesNotExists(String path){
    	try {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		}
    	catch (Exception e)
		{

		}
	}

}
