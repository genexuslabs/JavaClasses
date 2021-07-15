package com.genexus.msoffice.excel.poi.xssf;

import java.util.Hashtable;

import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class StylesCache
{
	private XSSFWorkbook pWorkbook;
	private Hashtable<String, XSSFCellStyle> stylesByFont;
	private Hashtable<String, XSSFCellStyle> stylesByFormat;

	public StylesCache(XSSFWorkbook pWorkbook)
	{
		this.pWorkbook = pWorkbook;
		this.stylesByFont = new Hashtable<String, XSSFCellStyle>();
		this.stylesByFormat = new Hashtable<String, XSSFCellStyle>();
	}

	public XSSFCellStyle getCellStyle(XSSFFont newFont)
	{
		
		String fontKey = newFont.getFontHeightInPoints() + newFont.getFontName() + newFont.getBold()
				+ newFont.getItalic() + newFont.getUnderline() + newFont.getColor();

		Object styleObj = stylesByFont.get(fontKey);
		if (styleObj != null)
		{
			return (XSSFCellStyle) styleObj;
		}
		XSSFCellStyle newStyle = pWorkbook.createCellStyle();
		stylesByFont.put(fontKey, newStyle);
		return newStyle;
	}

	public XSSFCellStyle getCellStyle(short format)
	{
		String formatKey = String.valueOf(format);

		Object styleObj = stylesByFormat.get(formatKey);
		if (styleObj != null)
		{
			return (XSSFCellStyle) styleObj;
		}
		XSSFCellStyle newStyle = pWorkbook.createCellStyle();
		stylesByFormat.put(formatKey, newStyle);
		return newStyle;
	}
}
