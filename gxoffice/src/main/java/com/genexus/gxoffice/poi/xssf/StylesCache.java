package com.genexus.gxoffice.poi.xssf;

import java.util.Hashtable;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

public class StylesCache
{
       private Workbook pWorkbook;
       private Hashtable<String, CellStyle> stylesByFont;
       private Hashtable<String, CellStyle> stylesByFormat;

       public StylesCache(Workbook pWorkbook)
       {
             this.pWorkbook = pWorkbook;
             this.stylesByFont = new Hashtable<String, CellStyle>();
             this.stylesByFormat = new Hashtable<String, CellStyle>();
       }

       public CellStyle getCellStyle(Font newFont)
       {
              String fontKey = newFont.getFontHeightInPoints() + newFont.getFontName()
                  + newFont.getBold() + newFont.getItalic() + newFont.getUnderline()
                  + newFont.getColor();

              Object styleObj = stylesByFont.get(fontKey);
              if(styleObj != null)
              {
                  return (XSSFCellStyle)styleObj;
              }
              CellStyle newStyle = pWorkbook.createCellStyle();
              stylesByFont.put(fontKey, newStyle);
              return newStyle;
       }

       public CellStyle getCellStyle(short format)
       {
              String formatKey = String.valueOf(format);

              Object styleObj = stylesByFormat.get(formatKey);
              if(styleObj != null)
              {
                  return (CellStyle)styleObj;
              }
              CellStyle newStyle = pWorkbook.createCellStyle();
              stylesByFormat.put(formatKey, newStyle);
              return newStyle;
       }
}
