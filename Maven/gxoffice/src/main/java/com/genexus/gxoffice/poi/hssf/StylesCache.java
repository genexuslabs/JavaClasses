// $Log: StylesCache.java,v $
// Revision 1.1.2.1  2007/05/23 19:21:33  alevin
// - Se estaban creando styles y fonts cada vez que se modificabauna celda; se implementa un cache para los
//   styles y se agrega el metodo getInternalFont que busca un font y si no esta lo crea. SAC21801.
//
//

package com.genexus.gxoffice.poi.hssf;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.usermodel.HSSFFont;
import java.util.Hashtable;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class StylesCache
{
       private HSSFWorkbook pWorkbook;
       private Hashtable stylesByFont;
       private Hashtable stylesByFormat;

       public StylesCache(HSSFWorkbook pWorkbook)
       {
             this.pWorkbook = pWorkbook;
             this.stylesByFont = new Hashtable();
             this.stylesByFormat = new Hashtable();
       }

       public HSSFCellStyle getCellStyle(HSSFFont font)
       {
              String fontKey = font.getFontHeightInPoints() + font.getFontName()
                  + font.getBold() + font.getItalic() + font.getUnderline()
                  + font.getColor();

              Object styleObj = stylesByFont.get(fontKey);
              if(styleObj != null)
              {
                  return (HSSFCellStyle)styleObj;
              }
              HSSFCellStyle newStyle = pWorkbook.createCellStyle();
              stylesByFont.put(fontKey, newStyle);
              return newStyle;
       }

       public HSSFCellStyle getCellStyle(short format)
       {
              String formatKey = String.valueOf(format);

              Object styleObj = stylesByFormat.get(formatKey);
              if(styleObj != null)
              {
                  return (HSSFCellStyle)styleObj;
              }
              HSSFCellStyle newStyle = pWorkbook.createCellStyle();
              stylesByFormat.put(formatKey, newStyle);
              return newStyle;
       }
}
