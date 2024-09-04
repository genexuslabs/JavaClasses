
package com.genexus.msoffice.excel.poi.xssf;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.genexus.CommonUtil;
import com.genexus.msoffice.excel.exception.ExcelException;
import com.genexus.msoffice.excel.exception.ExcelReadonlyException;
import com.genexus.msoffice.excel.style.*;
import com.genexus.msoffice.excel.IGXError;
import com.genexus.msoffice.excel.IExcelCellRange;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;

import java.math.BigDecimal;


public class ExcelCells implements IExcelCellRange {
    private IGXError _errorHandler;
    protected boolean fitColumnWidth;
    protected int cellCount;

    protected int colStartIdx;
    protected int rowStartIdx;
    protected int colEndIdx;
    protected int rowEndIdx;

    protected org.apache.poi.ss.usermodel.Sheet pSelectedSheet;
    protected XSSFCell[] pCells;
    protected XSSFWorkbook pWorkbook;

    protected ExcelSpreadsheet doc;

    protected int pWidth, pHeight;
    protected boolean readonly;

    protected StylesCache stylesCache;

    protected ExcelStyle cellStyle;

    public ExcelCells(IGXError errAccess, ExcelSpreadsheet document, XSSFWorkbook workBook, XSSFSheet selectedSheet,
                      int rowPos, int colPos, int height, int width, StylesCache stylesCache) throws ExcelException {
        this(errAccess, document, workBook, selectedSheet, rowPos, colPos, height, width, false, stylesCache);
    }

    public ExcelCells() {
    }

    public ExcelCells(IGXError errAccess, ExcelSpreadsheet document, XSSFWorkbook workBook, XSSFSheet selectedSheet,
                      int rowPos, int colPos, int height, int width, boolean readonly, StylesCache stylesCache)
            throws ExcelException {
        _errorHandler = errAccess;
        doc = document;

        cellCount = 0;
        pWidth = width;
        pHeight = height;

        colStartIdx = colPos;
        colEndIdx = colPos + (width - 1);
        rowStartIdx = rowPos;
        rowEndIdx = rowPos + (height - 1);

        pWorkbook = workBook;
        pSelectedSheet = selectedSheet;
        fitColumnWidth = true;
        this.readonly = readonly;
        this.stylesCache = stylesCache;
        pCells = new XSSFCell[(width * height) + 1];
        try {
            for (int y = rowPos; y < (rowPos + pHeight); y++) {
                XSSFRow pRow = getExcelRow(selectedSheet, y);
                if (pRow != null) {
                    for (short x = (short) colPos; x < (colPos + pWidth); x++) {
                        XSSFCell pCell = getExcelCell(pRow, x);
                        if (pCell != null) {
                            cellCount++;
                            pCells[cellCount] = pCell;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ExcelException(8, "Invalid cell coordinates");
        }
    }

    protected XSSFRow getExcelRow(XSSFSheet sheet, int rowPos) {
        XSSFRow row = sheet.getRow(rowPos);

        /*
         * if ((row == null) && readonly) { return null; }
         */

        if (row == null) {
            row = sheet.createRow(rowPos);
        }
        return row;
    }

    protected XSSFCell getExcelCell(XSSFRow row, short colPos) {
        XSSFCell cell = row.getCell(colPos);

        /*
         * if ((cell == null) && readonly) { return null; }
         */

        if (cell == null) {
            cell = row.createCell(colPos);
        }
        return cell;
    }

    public boolean setNumber(double value) throws ExcelException {

        try {
            for (int i = 1; i <= cellCount; i++) {

                pCells[i].setCellValue(value);
            }
            return true;

        } catch (Exception e) {
            throw new ExcelException(7, "Invalid cell value");
        }
    }

    public BigDecimal getNumber() throws ExcelException {
        try {
            return this.getValue();

        } catch (Exception e) {
            throw new ExcelException(7, "Invalid cell value");
        }

    }

	public boolean setDate(Date value) throws ExcelException {
		CheckReadonlyDocument();
		try {
			if (!CommonUtil.nullDate().equals(value)) {
				String dformat = "m/d/yy h:mm";//this.doc.getDateFormat().toLowerCase();
				Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
				calendar.setTime(value);
				if (calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.HOUR) == 0
					&& calendar.get(Calendar.SECOND) == 0 && dformat.indexOf(' ') > 0) {
					dformat = dformat.substring(0, dformat.indexOf(' '));
				}
				DataFormat df = pWorkbook.createDataFormat();

				for (int i = 1; i <= cellCount; i++) {
					XSSFCellStyle cellStyle = pCells[i].getCellStyle();
					if (! DateUtil.isCellDateFormatted(pCells[i])) {
						XSSFCellStyle newStyle = pWorkbook.createCellStyle();
						copyPropertiesStyle(newStyle, cellStyle);
						newStyle.setDataFormat(df.getFormat(dformat));
						pCells[i].setCellStyle(newStyle);
						fitColumnWidth(i, dformat.length() + 4);
					}else {
						fitColumnWidth(i, cellStyle.getDataFormatString().length() + 4);
					}
					pCells[i].setCellValue(value);
				}
				return true;
			}
		} catch (Exception e) {
			throw new ExcelException(7, "Invalid cell value");
		}
		return false;
	}

    public Date getDate() throws ExcelException {
        Date returnValue = null;
        try {
            returnValue = pCells[1].getDateCellValue();
        } catch (Exception e) { // Doc: For strings we throw an exception. For blank cells we return a null
            throw new ExcelException(7, "Invalid cell value");
        }
        if (returnValue == null)
            returnValue = CommonUtil.nullDate();
        return returnValue;
    }

    public boolean setTextImpl(String value) throws ExcelException {
        CheckReadonlyDocument();

        try {
            for (int i = 1; i <= cellCount; i++) {
                // pCells[i].setEncoding((short)1);
                if (value.length() > 0 && value.charAt(0) == '=') {
                    try {
                        pCells[i].setCellFormula(value.substring(1));
                    } catch (Exception e) {
                        pCells[i].setCellType(CellType.STRING);
                        pCells[i].setCellValue(value);
                    }
                } else
                    pCells[i].setCellValue(value);
            }
            return true;
        } catch (Exception e) {
            throw new ExcelException(7, "Invalid cell value", e);
        }
    }

    private void CheckReadonlyDocument() throws ExcelReadonlyException {
        if (readonly) {
            throw new ExcelReadonlyException();
        }
    }

    public String getText() {
        try {
            if (pCells[1].getCellType() == CellType.FORMULA)
                return "=" + pCells[1].getCellFormula();
            else if (pCells[1].getCellType() == CellType.NUMERIC) {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(pCells[1])) {
                    return pCells[1].getDateCellValue().toString();
                } else {
                    return Double.toString(pCells[1].getNumericCellValue());
                }
            } else
                return pCells[1].getStringCellValue();
        } catch (Exception e) {
            _errorHandler.setErrCod((short) 7);
            _errorHandler.setErrDes("Invalid cell value");
        }
        return null;
    }

    public BigDecimal getValue() throws ExcelException {
        BigDecimal value = new BigDecimal(0);
        try {
            CellType cType = pCells[1].getCellType();
            switch (cType) {
                case FORMULA:
                    String type = getFormulaType();
                    if (type == "N")
                        value = new BigDecimal(pCells[1].getNumericCellValue());
                    else if (type == "D")
                        value = new BigDecimal(getDate().getTime());
                    break;
                case BOOLEAN:
                    Boolean b = pCells[1].getBooleanCellValue();
                    value = new BigDecimal((b) ? 1 : 0);
                    break;
                default:
                    value = new BigDecimal(pCells[1].getNumericCellValue());
            }
        } catch (Exception e) {
            throw new ExcelException(7, "Invalid cell value");
        }
        return value;
    }

    public String getType() {
        String type = null;
        switch (pCells[1].getCellType()) {
            case BLANK:
                type = "U";
                break;
            case BOOLEAN:
                type = "N";
                break;
            case ERROR:
                type = "U";
                break;
            case FORMULA:
                type = getFormulaType();
                break;
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(pCells[1])) {
                    type = "D";
                } else {
                    type = "N";
                }
                break;
            case STRING:
                type = "C";
                break;
            default:
                type = "";
        }
        return type;
    }

    private String getFormulaType() {
        try {
            pCells[1].getNumericCellValue();

            DataFormatter formatter = new DataFormatter();

            java.text.Format format = formatter.getDefaultFormat(pCells[1]);
            if (format.getClass() == java.text.DateFormat.class) {
                pCells[1].getDateCellValue();
                return "D";
            } else {
                return "N";
            }
        } catch (Exception e) {
            try {
                Date dVal = pCells[1].getDateCellValue();
                if (dVal != null) {
                    return "D";
                }
            } catch (Exception e1) {
            }
        }
        String sVal = "";
        try {
            sVal = pCells[1].getStringCellValue();
        } catch (Exception e) {
        }
        if (!sVal.equals("")) {
            return "C";
        } else {
            return "U";
        }
    }

    public double getSize() {
        return pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getFontHeightInPoints();
    }

    public void setSize(double value) throws ExcelReadonlyException {
        CheckReadonlyDocument();

        try {
            for (int i = 1; i <= cellCount; i++) {
                XSSFCellStyle cellStyle = pCells[1].getCellStyle();
                XSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
                XSSFCellStyle newStyle = null;
                XSSFFont newFont = null;

                if (fontCell.getFontHeightInPoints() != value) {
                    // System.out.println("Changing Size...");
                    newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), (short) value,
                            fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(),
                            fontCell.getTypeOffset(), fontCell.getUnderline());
                    copyPropertiesFont(newFont, fontCell);

                    newFont.setFontHeightInPoints((short) value);

                    newStyle = stylesCache.getCellStyle(newFont);
                    copyPropertiesStyle(newStyle, cellStyle);

                    newStyle.setFont(newFont);
                    pCells[1].setCellStyle(newStyle);
                }
            }

        } catch (Exception e) {

        }
    }

    public String getFont() {
        return pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getFontName();
    }

    protected XSSFFont getInternalFont(boolean bold, short color, short fontHeight, String name, boolean italic,
                                       boolean strikeout, short typeOffset, byte underline) {
        XSSFFont font = pWorkbook.findFont(bold, color, fontHeight, name, italic, strikeout, typeOffset, underline);
        if (font == null) {
            font = pWorkbook.createFont();
        }
        return font;
    }

    public void setFont(String value) throws ExcelException {
        CheckReadonlyDocument();

        try {
            for (int i = 1; i <= cellCount; i++) {
                XSSFCellStyle cellStyle = pCells[i].getCellStyle();
                XSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
                XSSFCellStyle newStyle = null;
                XSSFFont newFont = null;

                if (!fontCell.getFontName().equals(value)) {
                    newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(), value,
                            fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(),
                            fontCell.getUnderline());
                    copyPropertiesFont(newFont, fontCell);

                    newFont.setFontName(value);

                    newStyle = stylesCache.getCellStyle(newFont);
                    copyPropertiesStyle(newStyle, cellStyle);

                    newStyle.setFont(newFont);
                    pCells[i].setCellStyle(newStyle);
                }
            }

        } catch (Exception e) {
            throw new ExcelException(7, "Invalid cell value");
        }
    }

    public short getBold() {
        if (pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getBold()) {
            return 1;
        }
        return 0;
    }

    public void setBold(short value) throws ExcelException {
        CheckReadonlyDocument();

        try {

            for (int i = 1; i <= cellCount; i++) {
                XSSFCellStyle cellStyle = pCells[i].getCellStyle();
                XSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
                XSSFCellStyle newStyle = null;
                XSSFFont newFont = null;

                switch (value) {
                    case 0:
                        if (fontCell.getBold()) {
                            newFont = getInternalFont(true, fontCell.getColor(), fontCell.getFontHeight(),
                                    fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(),
                                    fontCell.getTypeOffset(), fontCell.getUnderline());
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setBold(true);

                            newStyle = stylesCache.getCellStyle(newFont);
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                        break;
                    case 1:
                        if (!fontCell.getBold()) {
                            newFont = getInternalFont(true, fontCell.getColor(), fontCell.getFontHeight(),
                                    fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(),
                                    fontCell.getTypeOffset(), fontCell.getUnderline());
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setBold(true);

                            newStyle = stylesCache.getCellStyle(newFont);
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                        break;
                    default:
                        throw new ExcelException(6, "Invalid font properties");
                }


            }
        } catch (Exception e) {
            throw new ExcelException(6, "Invalid bold value");
        }
    }

    public short getItalic() {
        if (pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getItalic()) {
            return 1;
        }
        return 0;
    }

    public void setItalic(short value) throws ExcelException {
        CheckReadonlyDocument();

        try {

            for (int i = 1; i <= cellCount; i++) {
                XSSFCellStyle cellStyle = pCells[i].getCellStyle();
                XSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
                XSSFCellStyle newStyle = null;
                XSSFFont newFont = null;

                switch (value) {
                    case 0:
                        if (fontCell.getItalic()) {
                            newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                    fontCell.getFontName(), false, fontCell.getStrikeout(), fontCell.getTypeOffset(),
                                    fontCell.getUnderline());
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setItalic(false);

                            newStyle = stylesCache.getCellStyle(newFont);
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                        break;
                    case 1:
                        if (!fontCell.getItalic()) {
                            newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                    fontCell.getFontName(), true, fontCell.getStrikeout(), fontCell.getTypeOffset(),
                                    fontCell.getUnderline());
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setItalic(true);

                            newStyle = stylesCache.getCellStyle(newFont);
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                        break;
                    default:
                        throw new ExcelException(6, "Invalid font properties");
                }
            }
        } catch (Exception e) {
            throw new ExcelException(6, "Invalid font properties");
        }
    }

    public short getUnderline() {
        if (pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getUnderline() != XSSFFont.U_NONE) {
            return 1;
        }
        return 0;
    }

    public void setUnderline(short value) throws ExcelException {
        CheckReadonlyDocument();

        try {

            for (int i = 1; i <= cellCount; i++) {
                XSSFCellStyle cellStyle = pCells[i].getCellStyle();
                XSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
                XSSFCellStyle newStyle = null;
                XSSFFont newFont = null;

                switch (value) {
                    case 0:
                        if (fontCell.getUnderline() != XSSFFont.U_NONE) {
                            newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                    fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(),
                                    fontCell.getTypeOffset(), XSSFFont.U_NONE);
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setUnderline(XSSFFont.U_NONE);

                            newStyle = stylesCache.getCellStyle(newFont);
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                        break;
                    case 1:
                        if (fontCell.getUnderline() != XSSFFont.U_SINGLE) {
                            newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                    fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(),
                                    fontCell.getTypeOffset(), XSSFFont.U_SINGLE);
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setUnderline(XSSFFont.U_SINGLE);

                            newStyle = stylesCache.getCellStyle(newFont);
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                        break;
                    default:
                        throw new ExcelException(6, "Invalid font property");
                }
            }
        } catch (Exception e) {
            throw new ExcelException(6, "Invalid font properties");
        }
    }

    public long getColor() {
        return pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getColor() - 7;
    }

    public void setColor(short value) throws ExcelException {
        setColor((long) value);
    }

    public void setColor(int value) throws ExcelException {
        setColor((long) value);
    }

    // Ver setColor()
    /*
     * public void setColor(long value) //Willy version { int val=(int)value; int
     * r=val >> 16 & 0xff; int g=val >> 8 & 0xff; int b=val & 0xff; HSSFPalette
     * palette = pWorkbook.getCustomPalette(); try { for (int i=1;i <= cntCells;
     * i++) { XSSFCellStyle cellStyle = pCells[i].getCellStyle(); XSSFFont fontCell
     * = pWorkbook.getFontAt(cellStyle.getFontIndex()); CellStyle newStyle = null;
     * XSSFFont newFont = null;
     *
     * newStyle = pWorkbook.createCellStyle();
     * PropertyUtils.copyProperties(newStyle, cellStyle); newFont =
     * pWorkbook.createFont(); int colorIdx=ColorManager.getColor(pWorkbook);
     * PropertyUtils.copyProperties(newFont,
     * pWorkbook.getFontAt(cellStyle.getFontIndex()));
     * palette.setColorAtIndex((short)colorIdx,(byte)r,(byte)g,(byte)b);
     * newFont.setColor((short)colorIdx); //newFont.setColor(XSSFFont.COLOR_RED);
     * newStyle.setFont(newFont); pCells[i].setCellStyle(newStyle);
     *
     * }
     *
     * } catch (Exception e) {
     *
     * } }
     */
    // Esta version optimiza la paleta de colores existente en la planilla
    // Busca colores parecidos y si los encuentra, los toma para no recargar
    // la paleta de colores que tiene un maximo de 40h-10h posiciones.
    public void setColor(long value) throws ExcelException // 05/07/05 B@tero
    {
        CheckReadonlyDocument();

        try {

            for (int i = 1; i <= cellCount; i++) {
                XSSFCellStyle cellStyle = pCells[i].getCellStyle();
                XSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
                XSSFCellStyle newStyle = null;
                XSSFFont newFont = null;
                XSSFColor newColor = null;

                XSSFColor fontColor = ((XSSFFont) fontCell).getXSSFColor();

                int val = (int) value;
                int red = val >> 16 & 0xff;
                int green = val >> 8 & 0xff;
                int blue = val & 0xff;

                if (red != 0 || green != 0 || blue > 56) // Si es value esta entre 1 y 56 entonces supongo que es un
                // color Index de Excel y voy por el else
                {
                    if ((fontColor != null && (fontColor.getRGB() == null || (fontColor.getRGB()[0] == 0
                            && fontColor.getRGB()[1] == 0 && fontColor.getRGB()[2] == 0)))) {
                        // System.out.println("Automatic color.");

                        if ((red + green + blue) != 0) {
                            newColor = new XSSFColor(new java.awt.Color(red, green, blue), new DefaultIndexedColorMap());

                            newFont = (XSSFFont) pWorkbook.createFont();
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setColor(newColor);

                            newStyle = pWorkbook.createCellStyle();
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                    } else {
                        if (fontColor != null) {
                            byte[] triplet = fontColor.getRGB();

                            if (triplet[0] != red || triplet[1] != green || triplet[2] != blue) {
                                newColor = new XSSFColor( new java.awt.Color(red, green, blue), new DefaultIndexedColorMap());

                                newFont = (XSSFFont) pWorkbook.createFont();
                                copyPropertiesFont(newFont, fontCell);

                                newFont.setColor(newColor);

                                newStyle = pWorkbook.createCellStyle();
                                copyPropertiesStyle(newStyle, cellStyle);

                                newStyle.setFont(newFont);
                                pCells[i].setCellStyle(newStyle);
                            }
                        }
                    }
                } else {
                    // Es el ofset que hay que sumar para que el colorIndex quede igual
                    // al de la implementacion anterior de excel
                    value = value + 7;
                    if (fontColor != null) {
                        if (fontColor.getIndexed() != value) {
                            newFont = (XSSFFont) getInternalFont(fontCell.getBold(), (short) value,
                                    fontCell.getFontHeight(), fontCell.getFontName(), fontCell.getItalic(),
                                    fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
                            copyPropertiesFont(newFont, fontCell);

                            newFont.setColor((short) value);

                            newStyle = stylesCache.getCellStyle(newFont);
                            copyPropertiesStyle(newStyle, cellStyle);

                            newStyle.setFont(newFont);
                            pCells[i].setCellStyle(newStyle);
                        }
                    } else {
                        newFont = (XSSFFont) getInternalFont(fontCell.getBold(), (short) value,
                                fontCell.getFontHeight(), fontCell.getFontName(), fontCell.getItalic(),
                                fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
                        copyPropertiesFont(newFont, fontCell);

                        newFont.setColor((short) value);

                        newStyle = stylesCache.getCellStyle(newFont);
                        copyPropertiesStyle(newStyle, cellStyle);

                        newStyle.setFont(newFont);
                        pCells[i].setCellStyle(newStyle);
                    }
                }
            }
        } catch (Exception e) {
            throw new ExcelException(6, "Invalid font properties", e);
        }
    }

    protected void copyPropertiesStyle(XSSFCellStyle dest, XSSFCellStyle source) {
        dest.cloneStyleFrom(source);
    }

    protected void copyPropertiesFont(XSSFFont dest, XSSFFont source) {
        dest.setFontHeightInPoints(source.getFontHeightInPoints());
        dest.setFontName(source.getFontName());
        dest.setBold(source.getBold());
        dest.setItalic(source.getItalic());
        dest.setUnderline(source.getUnderline());
        dest.setColor(source.getColor());
    }

    private void fitColumnWidth(int i, int data) {
        if (fitColumnWidth) {
            int colW = pSelectedSheet.getColumnWidth((int) (i + colStartIdx - 1));
            if ((256 * (data)) > colW) {
                colW = (short) (256 * (data));
            }
            pSelectedSheet.setColumnWidth((short) (i + colStartIdx - 1), colW);
        }
    }

    public void setFitColumnWidth(boolean fitCol) {
        fitColumnWidth = fitCol;
    }

    public boolean getFitColumnWidth() {
        return fitColumnWidth;
    }

    /*
     * public void setAlignment(short value) { setCellStyleProperty("alignment", new
     * Short(value)); }
     *
     * public void setBorder(short value) { setCellStyleProperty("borderLeft", new
     * Short(value)); }
     *
     * public void setCellStyleProperty(String propertyName, Object propertyValue) {
     * try {
     *
     * XSSFCellStyle originalStyle = pCells[1].getCellStyle(); CellStyle newStyle =
     * null;
     *
     * Map values = PropertyUtils.describe(originalStyle); values.put(propertyName,
     * propertyValue); values.remove("index"); // not good to compare on
     *
     * short numberCellStyles = pWorkbook.getNumCellStyles();
     *
     * for (short i = 0; i < numberCellStyles; i++) { XSSFCellStyle wbStyle =
     * pWorkbook.getCellStyleAt(i); Map wbStyleMap =
     * PropertyUtils.describe(wbStyle); wbStyleMap.remove( "index" );
     *
     * if (wbStyleMap.equals(values)) { newStyle = wbStyle;
     * //System.out.println("founded!!!"); break; } }
     *
     * if ( newStyle == null ) { newStyle = pWorkbook.createCellStyle();
     * newStyle.setFont(pWorkbook.getFontAt(originalStyle.getFontIndex()));
     *
     * PropertyUtils.copyProperties(newStyle, originalStyle);
     * PropertyUtils.setProperty(newStyle, propertyName, propertyValue);
     * //System.out.println("not founded!!!"); }
     *
     * pCells[1].setCellStyle(newStyle);
     *
     * }
     *
     * catch (Exception e) { e.printStackTrace(); // throw new NestableException(
     * "Couldn't setCellStyleProperty.", e ); } }
     */

    @Override
    public int getRowStart() {
        return rowStartIdx + 1;
    }

    @Override
    public int getRowEnd() {
        return rowEndIdx + 1;
    }

    @Override
    public int getColumnStart() {
        return colStartIdx + 1;
    }

    @Override
    public int getColumnEnd() {
        return colEndIdx + 1;
    }

    @Override
    public String getCellAdress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getValueType() {
        return this.getType();
    }

    @Override
    public java.math.BigDecimal getNumericValue() {
        try {
            return this.getNumber();
        } catch (ExcelException e) {
            _errorHandler.setErrCod((short) e.get_errorCode());
            _errorHandler.setErrDes(e.get_errDsc());
        }
        return new java.math.BigDecimal(0);
    }

    @Override
    public Date getDateValue() {
        try {
            return this.getDate();
        } catch (ExcelException e) {
            _errorHandler.setErrCod((short) e.get_errorCode());
            _errorHandler.setErrDes(e.get_errDsc());
        }
        return null;
    }

    @Override
    public Boolean setText(String value) {
        try {
            return this.setTextImpl(value);
        } catch (ExcelException e) {
            _errorHandler.setErrCod((short) e.get_errorCode());
            _errorHandler.setErrDes(e.get_errDsc());
        }
        return false;
    }

    @Override
    public Boolean setNumericValue(java.math.BigDecimal d) {
        try {
            return this.setNumber(d.doubleValue());
        } catch (ExcelException e) {
            _errorHandler.setErrCod((short) e.get_errorCode());
            _errorHandler.setErrDes(e.get_errDsc());
        }
        return false;
    }

    @Override
    public Boolean setDateValue(Date value) {
        try {
            return this.setDate(value);
        } catch (ExcelException e) {
            _errorHandler.setErrCod((short) e.get_errorCode());
            _errorHandler.setErrDes(e.get_errDsc());
        }
        return false;
    }

    @Override
    public Boolean empty() {
        for (int i = 1; i <= cellCount; i++) {
            pCells[i].setCellValue("");
        }
        return this.cellCount > 0;
    }

    @Override
    public Boolean mergeCells() {
        CellRangeAddress cellRange = new CellRangeAddress(rowStartIdx, rowEndIdx, colStartIdx, colEndIdx);
        for (int i = 0; i < pSelectedSheet.getNumMergedRegions(); i++){
            CellRangeAddress mergedRegion = pSelectedSheet.getMergedRegion(i);
            if (cellRange.intersects(mergedRegion)){
                pSelectedSheet.removeMergedRegion(i);
            }
        }
        pSelectedSheet.addMergedRegion(cellRange);
        return true;
    }

    @Override
    public ExcelStyle getCellStyle() {

        return cellStyle;
    }

    @Override
    public Boolean setCellStyle(ExcelStyle newCellStyle) {
        if (cellCount > 0) {
            XSSFCellStyle style = pWorkbook.createCellStyle();

            style.cloneStyleFrom(style);
            applyNewCellStyle(style, newCellStyle);
            for (int i = 1; i <= cellCount; i++) {
                pCells[i].setCellStyle(style);
            }
        }
        return cellCount > 0;

    }

    private XSSFColor toColor(ExcelColor color) {
       return new XSSFColor( new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue()), new DefaultIndexedColorMap());
    }

    private XSSFCellStyle applyNewCellStyle(XSSFCellStyle cellStyle, ExcelStyle newCellStyle) {
        ExcelFont cellFont = newCellStyle.getCellFont();
        if (cellFont != null && cellFont.isDirty()) {
            // XSSFFont cellStyleFont = cellStyle.getFont();
            XSSFFont cellStyleFont = pWorkbook.createFont();
            cellStyle.setFont(cellStyleFont);
            ExcelFont font = newCellStyle.getCellFont();
            if (font != null) {
                if (font.getBold() != null) {
                    cellStyleFont.setBold(font.getBold());
                }
                if (font.getFontFamily() != null && font.getFontFamily().length() > 0) {
                    cellStyleFont.setFontName(font.getFontFamily());
                }
                if (font.getItalic() != null) {
                    cellStyleFont.setItalic(font.getItalic());
                }
                if (font.getStrike() != null) {
                    cellStyleFont.setStrikeout(font.getStrike());
                }
                if (font.getSize() != null) {
                    cellStyleFont.setFontHeight(font.getSize());
                }
                if (font.getUnderline() != null) {
                    cellStyleFont.setUnderline((byte) (font.getUnderline() ? 1 : 0));
                }
                if (font.getColor() != null && font.getColor().isDirty()) {
                    cellStyleFont.setColor(toColor(font.getColor()));
                }
            }
        }
        ExcelFill cellfill = newCellStyle.getCellFill();
        if (cellfill != null && cellfill.getCellBackColor() != null && cellfill.getCellBackColor().isDirty()) {
            cellStyle.setFillForegroundColor(toColor(cellfill.getCellBackColor()));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        ExcelAlignment alignment = newCellStyle.getCellAlignment();
        if (alignment != null && alignment.isDirty()) {
            if (alignment.getHorizontalAlignment() != null) {
                HorizontalAlignment align;
                switch (alignment.getHorizontalAlignment()) {
                    case ExcelAlignment.HORIZONTAL_ALIGN_CENTER:
                        align = HorizontalAlignment.CENTER;
                        break;
                    case ExcelAlignment.HORIZONTAL_ALIGN_LEFT:
                        align = HorizontalAlignment.LEFT;
                        break;
                    case ExcelAlignment.HORIZONTAL_ALIGN_RIGHT:
                        align = HorizontalAlignment.RIGHT;
                        break;
                    default:
                        align = HorizontalAlignment.forInt(alignment.getHorizontalAlignment());
                }
                cellStyle.setAlignment(align);
            }
            if (alignment.getVerticalAlignment() != null) {
                VerticalAlignment align;
                switch (alignment.getVerticalAlignment()) {
                    case ExcelAlignment.VERTICAL_ALIGN_BOTTOM:
                        align = VerticalAlignment.BOTTOM;
                        break;
                    case ExcelAlignment.VERTICAL_ALIGN_MIDDLE:
                        align = VerticalAlignment.CENTER;
                        break;
                    case ExcelAlignment.VERTICAL_ALIGN_TOP:
                        align = VerticalAlignment.TOP;
                        break;
                    default:
                        align = VerticalAlignment.forInt(alignment.getVerticalAlignment());
                }

                cellStyle.setVerticalAlignment(align);
            }
        }

        if (newCellStyle.isLocked() != null) {
            cellStyle.setLocked(newCellStyle.isLocked());
        }

        if (newCellStyle.isHidden() != null) {
            cellStyle.setHidden(newCellStyle.isHidden());
        }

        if (newCellStyle.getShrinkToFit() != null) {
            cellStyle.setShrinkToFit(newCellStyle.getShrinkToFit());
        }

        if (newCellStyle.getWrapText() != null) {
            cellStyle.setWrapText(newCellStyle.getWrapText());
        }

        if (newCellStyle.getTextRotation() != 0) {

            cellStyle.setRotation((short) (newCellStyle.getTextRotation()));
        }

        if (newCellStyle.getIndentation() >= 0) {
            cellStyle.setIndention((short) newCellStyle.getIndentation());
        }

        if (newCellStyle.getDataFormat() != null && newCellStyle.getDataFormat().length() > 0) {
            cellStyle.setDataFormat(pWorkbook.createDataFormat().getFormat(newCellStyle.getDataFormat()));
        }

        if (newCellStyle.getBorder() != null) {
            ExcelCellBorder cellBorder = newCellStyle.getBorder();
            applyBorderSide(cellStyle, BorderCellSide.TOP, cellBorder.getBorderTop());
            applyBorderSide(cellStyle, BorderCellSide.BOTTOM, cellBorder.getBorderBottom());
            applyBorderSide(cellStyle, BorderCellSide.LEFT, cellBorder.getBorderLeft());
            applyBorderSide(cellStyle, BorderCellSide.RIGHT, cellBorder.getBorderRight());

            Boolean hasDiagonalUp = cellBorder.getBorderDiagonalUp() != null && cellBorder.getBorderDiagonalUp().isDirty();
            Boolean hasDiagonalDown = cellBorder.getBorderDiagonalDown() != null && cellBorder.getBorderDiagonalDown().isDirty();
            if (hasDiagonalUp || hasDiagonalDown) {
                CTXf _cellXf = cellStyle.getCoreXf();
                ExcelBorder border = (hasDiagonalUp) ? cellBorder.getBorderDiagonalUp() : cellBorder.getBorderDiagonalDown();
                XSSFColor diagonalColor = toColor(border.getBorderColor());
                setBorderDiagonal(BorderStyle.valueOf(border.getBorder()), diagonalColor, this.pWorkbook.getStylesSource(), _cellXf, hasDiagonalUp, hasDiagonalDown);
            }
        }
        return cellStyle;
    }

    private static CTBorder getCTBorder(StylesTable _stylesSource, CTXf _cellXf) {
        CTBorder ct;
        if (_cellXf.getApplyBorder()) {
            int idx = (int) _cellXf.getBorderId();
            XSSFCellBorder cf = _stylesSource.getBorderAt(idx);
            ct = (CTBorder) cf.getCTBorder().copy();
        } else {
            ct = CTBorder.Factory.newInstance();
        }
        return ct;
    }

    public static void setBorderDiagonal(BorderStyle border, XSSFColor color, StylesTable _stylesSource, CTXf _cellXf, boolean up, boolean down) {
        CTBorder ct = getCTBorder(_stylesSource, _cellXf);
        CTBorderPr pr = ct.isSetDiagonal() ? ct.getDiagonal() : ct.addNewDiagonal();

        ct.setDiagonalDown(down);
        ct.setDiagonalUp(up);
        pr.setStyle(STBorderStyle.Enum.forInt(border.getCode() + 1));
        pr.setColor(color.getCTColor());

        int idx = _stylesSource.putBorder(new XSSFCellBorder(ct));
        _cellXf.setBorderId(idx);
        _cellXf.setApplyBorder(true);
    }

    private void applyBorderSide(XSSFCellStyle cellStyle, BorderCellSide bSide, ExcelBorder border) {
        if (border != null && border.isDirty()) {
            if (border.getBorderColor().isDirty()) {
                try {
                    XSSFCellBorder.BorderSide borderSide = XSSFCellBorder.BorderSide.valueOf(bSide.name());
                    cellStyle.setBorderColor(borderSide, toColor(border.getBorderColor()));
                }
                catch (IllegalArgumentException e) {}
			}
            if (border.getBorder() != null && border.getBorder().length() > 0) {
                BorderStyle bs = BorderStyle.valueOf(border.getBorder());
                if (bSide == BorderCellSide.BOTTOM) {
                    cellStyle.setBorderBottom(bs);
                } else if (bSide == BorderCellSide.TOP) {
                    cellStyle.setBorderTop(bs);
                } else if (bSide == BorderCellSide.LEFT) {
                    cellStyle.setBorderLeft(bs);
                } else if (bSide == BorderCellSide.RIGHT) {
                    cellStyle.setBorderRight(bs);
                }
            }
        }
    }

    public enum BorderCellSide {
        RIGHT, LEFT, TOP, BOTTOM, DIAGONALUP, DIAGONALDOWN
    }

	public XSSFCell[] getUnderlyingObject(){
		return pCells;

	}
}
