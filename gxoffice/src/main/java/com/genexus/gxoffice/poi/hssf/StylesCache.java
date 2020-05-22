package com.genexus.gxoffice.poi.hssf;

import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class StylesCache {
	private HSSFWorkbook pWorkbook;
	private Hashtable<String, HSSFCellStyle> stylesByFont;
	private Hashtable<String, HSSFCellStyle> stylesByFormat;

	public StylesCache(HSSFWorkbook pWorkbook) {
		this.pWorkbook = pWorkbook;
		this.stylesByFont = new Hashtable<String, HSSFCellStyle>();
		this.stylesByFormat = new Hashtable<String, HSSFCellStyle>();
	}

	public HSSFCellStyle getCellStyle(HSSFFont font) {
		String fontKey = font.getFontHeightInPoints() + font.getFontName() + font.getBold() + font.getItalic()
				+ font.getUnderline() + font.getColor();

		Object styleObj = stylesByFont.get(fontKey);
		if (styleObj != null) {
			return (HSSFCellStyle) styleObj;
		}
		HSSFCellStyle newStyle = pWorkbook.createCellStyle();
		stylesByFont.put(fontKey, newStyle);
		return newStyle;
	}

	public HSSFCellStyle getCellStyle(short format) {
		String formatKey = String.valueOf(format);

		Object styleObj = stylesByFormat.get(formatKey);
		if (styleObj != null) {
			return (HSSFCellStyle) styleObj;
		}
		HSSFCellStyle newStyle = pWorkbook.createCellStyle();
		stylesByFormat.put(formatKey, newStyle);
		return newStyle;
	}
}
