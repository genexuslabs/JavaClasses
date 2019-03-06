// $Log: ExcelCells.java,v $
// Revision 1.12  2008/06/05 16:57:18  iroqueta
// En el getExcelRow y getExcelCell si era readonly se retornaba null.
// Eso esta mal, porque puede que quiera solo leer el tipo de la cela por ejemplo.
// SAC 23577
//
// Revision 1.11  2008-05-14 16:06:35  alevin
// - Paso las cosas que se habian perdido en el cambio del CVS del brain al svnsrv.
//
// Revision 1.11  2008/01/31 18:43:26  alevin
// - Arreglo para leer bien el tipo de una celda formula.
//
// Revision 1.10  2007-05-23 19:21:33  alevin
// - Se estaban creando styles y fonts cada vez que se modificabauna celda; se implementa un cache para los
//   styles y se agrega el metodo getInternalFont que busca un font y si no esta lo crea. SAC21801.
//
// Revision 1.9  2007/05/09 17:38:18  alevin
// - Cambios para poder abrir un archivo de Excel en forma readonly. SAC 22260
//
// Revision 1.8  2006/07/19 20:39:01  alevin
// - En el copyPropertiesStyle faltaban copiar las demas properties del style.
//
// Revision 1.7  2006/06/20 14:05:47  alevin
// - En el setText se hace un setEncoding(1) para que soporte guardar caracteres
//   multibyte.
//
// Revision 1.6  2006/06/16 14:31:33  gusbro
// Para el caso del getColor hay que hacer lo opuesto del put anterior, o sea restar 7 al ColorIndex.
//
// Revision 1.5  2006/06/15 18:19:38  gusbro
// El setColor no estaba quedando igual al seteado en la implementacion vieja de excelCell.
// 7 es el ofset que hay que sumar para que el colorIndex quede igual
// SAC 19497
//
// Revision 1.4  2006/06/05 20:40:09  alevin
// - Arreglo en el setDate, quedaba mal formateado.
//
//
/*
 * Created on 24/06/2005
 * 
 * 13/7 Willy:
 *			Cambio de package
 *			se implementa IExcelCells
 *
 * 12/7 Willy: entran los fuentes al svn
 * 
 * 9/7/5  Diego: 
 * 		GxError support added
 * 		Big Changes in color manipulation. 
 */
package com.genexus.gxoffice.poi.hssf;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import java.util.Map;
import java.util.Date;
import com.genexus.gxoffice.IExcelCells;
import com.genexus.CommonUtil;
import com.genexus.gxoffice.IGxError;

/**
 * @author Diego
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExcelCells implements IExcelCells{
	private IGxError m_errAccess; // Willy: 01/06/05
	private int pWidth, pHeight;
        private boolean readonly;
        private StylesCache stylesCache;

        public ExcelCells(IGxError errAccess,HSSFWorkbook workBook, HSSFSheet selectedSheet, int rowPos, int colPos, int height, int width, StylesCache stylesCache)
	{
           this(errAccess,workBook, selectedSheet, rowPos, colPos, height, width, false, stylesCache);
        }

	public ExcelCells(IGxError errAccess,HSSFWorkbook workBook, HSSFSheet selectedSheet, int rowPos, int colPos, int height, int width, boolean readonly, StylesCache stylesCache)
	{
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
		pCells = new HSSFCell[(width * height) + 1];
		try
		{
			for (int y = rowPos; y < (rowPos + pHeight); y++) {
				HSSFRow pRow = getExcelRow(selectedSheet, y);				
                                if (pRow != null)
                                {
                                       for (short x=(short)colPos; x<(colPos+pWidth); x++)
                                       {
                                              HSSFCell pCell = getExcelCell(pRow, x);
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
	
	private HSSFRow getExcelRow(HSSFSheet sheet, int rowPos)
	{
		HSSFRow row = sheet.getRow(rowPos);
		
                /*if ((row == null) && readonly)
                {
                        return null;
                }*/

		if (row == null)
		{						
			row = sheet.createRow(rowPos);			
		}
		return row;
	}

	private HSSFCell getExcelCell(HSSFRow row, short colPos)
	{
		HSSFCell cell = row.getCell(colPos);
		
                /*if ((cell == null) && readonly)
                {
                        return null;
                }*/

		if (cell == null)
		{						
			cell = row.createCell(colPos);			
		}
		return cell;
	}
	
	public void setNumber(double value)
	{
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try
		{
			for (int i=1;i <= cntCells; i++)
			{
				pCells[i].setCellValue(value);
			}
			
		}catch(Exception e)
		{
			m_errAccess.setErrDes("Invalid cell value");
			m_errAccess.setErrCod((short)7);
		}		
	}
	
	public double getNumber()
	{
		try
		{
			return pCells[1].getNumericCellValue();
			
		}catch(Exception e)
		{
			m_errAccess.setErrDes("Invalid cell value");
			m_errAccess.setErrCod((short)7);
			return -1;
		}		
		
	}
	
	public void setDate(Date value)
	{
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try
		{
			if (!CommonUtil.nullDate().equals(value)){
                  String dformat = null;
                  if(value.getMinutes() !=0 || value.getHours() !=0 || value.getSeconds() !=0)
                  {
                    dformat = "m/d/yy h:mm";
                  }
                  else
                  {
                    dformat = "m/d/yy";
                  }
                  short format = org.apache.poi.hssf.usermodel.HSSFDataFormat.getBuiltinFormat(dformat);

				  HSSFCellStyle newStyle = stylesCache.getCellStyle(format);

					for (int i=1;i <= cntCells; i++)
					{
						HSSFCellStyle cellStyle = pCells[i].getCellStyle();
						copyPropertiesStyle(newStyle, cellStyle);
						newStyle.setDataFormat(format);
						pCells[i].setCellValue(value);
						pCells[i].setCellStyle(newStyle);
						fitColumnWidth(i, dformat.length() + 4);
					}
				}
		}
		catch(Exception e)
		{
			m_errAccess.setErrDes("Invalid cell value");
			m_errAccess.setErrCod((short)7);
		}
	}
	
	public Date getDate()
	{
		Date returnValue = null;
		try {
			returnValue = pCells[1].getDateCellValue();
		}catch(Exception e)
		{ 	//Doc: For strings we throw an exception. For blank cells we return a null
			m_errAccess.setErrDes("Invalid cell value");
			m_errAccess.setErrCod((short)7);						
		}
		if (returnValue == null)
			returnValue = CommonUtil.nullDate();
		return returnValue;
	}	
	
	public void setText(String value)
	{
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try
		{
			for (int i=1;i <= cntCells; i++)
			{
				//pCells[i].setEncoding((short)1);
				if (value.length()>0 && value.charAt(0)=='=')
				{
					try
					{
						pCells[i].setCellFormula(value.substring(1));
					}
					catch(Exception e)
					{
						pCells[i].setCellType(HSSFCell.CELL_TYPE_STRING);
						pCells[i].setCellValue(value);
					}
				}
				else
					pCells[i].setCellValue(value);
			}			
		}catch(Exception e)
		{
			m_errAccess.setErrDes("Invalid cell value");
			m_errAccess.setErrCod((short)7);
		}
	}
	
	public String getText()
	{
		try
		{
			if (pCells[1].getCellType()==HSSFCell.CELL_TYPE_FORMULA)
				return "=" + pCells[1].getCellFormula();
			else if (pCells[1].getCellType()==HSSFCell.CELL_TYPE_NUMERIC)
			{
				if (HSSFDateUtil.isCellDateFormatted(pCells[1]))
				{
					return pCells[1].getDateCellValue().toString();
				}
				else
				{
					return Double.toString(pCells[1].getNumericCellValue());
				}			
			}
			else
				return pCells[1].getStringCellValue();
		} catch(Exception e)
		{
			m_errAccess.setErrDes("Invalid cell value");
			m_errAccess.setErrCod((short)7);			
			return null;
		}				
	}
	
	public String getValue()
	{
		try
		{
			if (pCells[1].getCellType()==HSSFCell.CELL_TYPE_FORMULA)
			{
				String type = getFormulaType();
				if (type == "N")
					return Double.toString(getNumber());
				else if (type == "D")
					return getDate().toString();
				else
					return pCells[1].getStringCellValue();
			}
			else if (pCells[1].getCellType()==HSSFCell.CELL_TYPE_BOOLEAN)
			{
				Boolean b = pCells[1].getBooleanCellValue();
				return String.valueOf(b);		
			}			
			else
			{
				return getText();
			}
		} catch(Exception e)
		{
			m_errAccess.setErrDes("Invalid cell value");
			m_errAccess.setErrCod((short)7);			
			return null;
		}				
	}

	public String getType()
	{		
		String type = "";
		switch (pCells[1].getCellType())
		{
			case HSSFCell.CELL_TYPE_BLANK:
				type = "U";
				break;
			case HSSFCell.CELL_TYPE_BOOLEAN:
				type = "N";
				break;
			case HSSFCell.CELL_TYPE_ERROR:
				type = "U";
				break;
			case HSSFCell.CELL_TYPE_FORMULA:				
				type = getFormulaType();
				break;
			case HSSFCell.CELL_TYPE_NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(pCells[1])) {				
					type = "D";				
				} else {
					type = "N"; }			
				break;
			case HSSFCell.CELL_TYPE_STRING:
				type = "C";
				break;
		}
		return type;
	}
	
        private String getFormulaType()
        {
          try
          {
            pCells[1].getNumericCellValue();
            return "N";
          }
          catch(Exception e)
          {
            try
            {
              Date dVal = pCells[1].getDateCellValue();
              if (dVal != null)
              {
                return "D";
              }
            }
            catch(Exception e1){}
          }
          String sVal = "";
          try
          {
            sVal = pCells[1].getStringCellValue();
          }
          catch(Exception e){}
          if (!sVal.equals(""))
          {
            return "C";
          }
          else
          {
            return "U";
          }
        }

	public double getSize()
	{
		return pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getFontHeightInPoints();
	}
	
	public void setSize(double value)
	{
              if(readonly)
              {
                   m_errAccess.setErrDes("Can not modify a readonly document");
                   m_errAccess.setErrCod((short)13);
                   return;
              }

		try {			
			for (int i=1;i <= cntCells; i++)
			{				
				HSSFCellStyle cellStyle = pCells[i].getCellStyle();
				HSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
				HSSFCellStyle newStyle = null;
				HSSFFont newFont = null;
				
				if (fontCell.getFontHeightInPoints() != value)
				{					
                    newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), (short) value,
                                               fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
					copyPropertiesFont(newFont, fontCell);

					newFont.setFontHeightInPoints((short) value);

                                        newStyle = stylesCache.getCellStyle(newFont);
					copyPropertiesStyle(newStyle, cellStyle);

					newStyle.setFont(newFont);
					pCells[i].setCellStyle(newStyle);					
				}
			}
			
		} catch (Exception e)
		{
				
		}
	}	
	
	public String getFont()
	{
		return pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getFontName();
	}
	
        private HSSFFont getInternalFont(boolean b, short color, short fontHeight, String name,
                                         boolean italic, boolean strikeout, short typeOffset, byte underline)
        {
                HSSFFont font = pWorkbook.findFont(b, color, fontHeight, name, italic, strikeout, typeOffset, underline);
                if(font == null)
                {
                       font = pWorkbook.createFont();
                }
                return font;
        }

	public void setFont(String value)
	{
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try {			
			for (int i=1;i <= cntCells; i++)
			{
				HSSFCellStyle cellStyle = pCells[i].getCellStyle();
				HSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
				HSSFCellStyle newStyle = null;
				HSSFFont newFont = null;
				
				if (!fontCell.getFontName().equals(value))
				{
				    newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                        value, fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
				    copyPropertiesFont(newFont, fontCell);

				    newFont.setFontName(value);

                                    newStyle = stylesCache.getCellStyle(newFont);
                                    copyPropertiesStyle(newStyle, cellStyle);

				    newStyle.setFont(newFont);
					pCells[i].setCellStyle(newStyle);					
				}				
			}
			
		} catch (Exception e)
		{
				m_errAccess.setErrDes("Invalid font properties");
				m_errAccess.setErrCod((short)6);
		}
	}	
	
	public short getBold()
	{
		if (pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getBold())
		{
			return 1;
		}		
		return 0;
	}
	
	public void setBold(short value)	
	{		
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try {			
		
		for (int i=1;i <= cntCells; i++)
		{
			HSSFCellStyle cellStyle = pCells[i].getCellStyle();
			HSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			HSSFCellStyle newStyle = null;
			HSSFFont newFont = null;
			
			switch (value)
			{
				case 0:				
					if (fontCell.getBold())
					{
                                          newFont = getInternalFont(true, fontCell.getColor(), fontCell.getFontHeight(),
                                               fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setBold(true);

                                        newStyle = stylesCache.getCellStyle(newFont);
			        	copyPropertiesStyle(newStyle, cellStyle);

			        	newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);
					}
					break;
				case 1:
					if (fontCell.getBold())
					{
                                          newFont = getInternalFont(true, fontCell.getColor(), fontCell.getFontHeight(),
                                               fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setBold(true);

                                        newStyle = stylesCache.getCellStyle(newFont);
			        	copyPropertiesStyle(newStyle, cellStyle);

			        	newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);					
					}
					break;
			}
			
		}
		} catch (Exception e)
		{
			m_errAccess.setErrDes("Invalid font properties");
			m_errAccess.setErrCod((short)6);
		}
	}
	
	public short getItalic()
	{
		if (pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getItalic())
		{
			return 1;
		}		
		return 0;
	}
	
	public void setItalic(short value)	
	{		
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try {
			
		for (int i=1;i <= cntCells; i++)
		{
			HSSFCellStyle cellStyle = pCells[i].getCellStyle();
			HSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			HSSFCellStyle newStyle = null;
			HSSFFont newFont = null;
			
			switch (value)
			{
				case 0:				
					if (fontCell.getItalic())
					{
                                          newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                              fontCell.getFontName(), false, fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setItalic(false);

                                        newStyle = stylesCache.getCellStyle(newFont);
			        	copyPropertiesStyle(newStyle, cellStyle);

			        	newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);
					}
					break;
				case 1:
					if (!fontCell.getItalic())
					{
                                          newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                              fontCell.getFontName(), true, fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setItalic(true);

                                        newStyle = stylesCache.getCellStyle(newFont);
			        	copyPropertiesStyle(newStyle, cellStyle);

			        	newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);					
					}
					break;
			}			
		}
		} catch (Exception e)
		{
			m_errAccess.setErrDes("Invalid font properties");
			m_errAccess.setErrCod((short)6);
		}
	}
	public short getUnderline()
	{
		if (pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getUnderline() != HSSFFont.U_NONE)
		{
			return 1;
		}		
		return 0;
	}
	
	public void setUnderline(short value)	
	{		
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try {			
		
		for (int i=1;i <= cntCells; i++)
		{
			HSSFCellStyle cellStyle = pCells[i].getCellStyle();
			HSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			HSSFCellStyle newStyle = null;
			HSSFFont newFont = null;
			
			switch (value)
			{
				case 0:				
					if (fontCell.getUnderline() != HSSFFont.U_NONE)
					{
                                          newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                              fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), HSSFFont.U_NONE);
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setUnderline(HSSFFont.U_NONE);

                                        newStyle = stylesCache.getCellStyle(newFont);
			        	copyPropertiesStyle(newStyle, cellStyle);

			        	newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);
					}
					break;
				case 1:
					if (fontCell.getUnderline() != HSSFFont.U_SINGLE)
					{
                                          newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                              fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), HSSFFont.U_SINGLE);
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setUnderline(HSSFFont.U_SINGLE);

                                        newStyle = stylesCache.getCellStyle(newFont);
			        	copyPropertiesStyle(newStyle, cellStyle);

			        	newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);					
					}
					break;
			}			
		}
		} catch (Exception e)
		{
			m_errAccess.setErrDes("Invalid font properties");
			m_errAccess.setErrCod((short)6);
		}
	}
	
	public long getColor()
	{
		return pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getColor() - 7;		
	}
	
	public void setColor(short value)	
	{
		setColor((long) value);
	}
	
	public void setColor(int value)	
	{
		setColor((long) value);
	}
	// Ver setColor()
/*	public void setColor(long value) //Willy version	
	{		
		int val=(int)value;
		int r=val >> 16 & 0xff;
		int g=val >> 8 & 0xff;
		int b=val & 0xff;
		HSSFPalette palette = pWorkbook.getCustomPalette();
		try {
		for (int i=1;i <= cntCells; i++)
		{
			HSSFCellStyle cellStyle = pCells[i].getCellStyle();
			HSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			HSSFCellStyle newStyle = null;
			HSSFFont newFont = null;

			newStyle = pWorkbook.createCellStyle();
			PropertyUtils.copyProperties(newStyle, cellStyle);
			newFont = pWorkbook.createFont();	
			int colorIdx=ColorManager.getColor(pWorkbook);
			PropertyUtils.copyProperties(newFont, pWorkbook.getFontAt(cellStyle.getFontIndex()));
			palette.setColorAtIndex((short)colorIdx,(byte)r,(byte)g,(byte)b);
			newFont.setColor((short)colorIdx);
			//newFont.setColor(HSSFFont.COLOR_RED);
			newStyle.setFont(newFont);
			pCells[i].setCellStyle(newStyle);				
			
		}
				
		} catch (Exception e)
		{
			
		}
	}
	*/
	// Esta version optimiza la paleta de colores existente en la planilla
	// Busca colores parecidos y si los encuentra, los toma para no recargar 
	// la paleta de colores que tiene un maximo de 40h-10h posiciones.
	public void setColor(long value) // 05/07/05 B@tero
	{
                if(readonly)
                {
                     m_errAccess.setErrDes("Can not modify a readonly document");
                     m_errAccess.setErrCod((short)13);
                     return;
                }

		try {		
			
			for (int i=1;i <= cntCells; i++)
			{				
				HSSFCellStyle cellStyle = pCells[i].getCellStyle();
				HSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
				HSSFCellStyle newStyle = null;
				HSSFFont newFont = null;
				HSSFColor newColor = null;
				
				int val=(int)value;
				int red=val >> 16 & 0xff;
				int green=val >> 8 & 0xff;
				int blue=val & 0xff;			
					
				HSSFPalette palette = pWorkbook.getCustomPalette();								
				HSSFColor fontColor = palette.getColor(fontCell.getColor());
				
				if (value < 0)
				{
					if (fontColor == null)
					{
						//System.out.println("Automatic color.");
						
						if ((red + green + blue) != 0)
						{							
							HSSFColor foundColor = palette.findColor((byte)red, (byte)green, (byte)blue);
							if (foundColor == null)
							{								
								for (short j=9; j <= 63; j++)
								{
									foundColor = palette.getColor(j);
									if (foundColor == null)
									{
										newColor = palette.addColor((byte)red, (byte)green, (byte)blue);									
										j = 64;
									}
								}
								if (newColor == null)
								{									
									newColor = palette.findSimilarColor((byte)red, (byte)green, (byte)blue);								
									palette.setColorAtIndex(newColor.getIndex(), (byte)red, (byte)green, (byte)blue);
								}
							}
							else
							{
								newColor = foundColor;
							}
							
                                                        newFont = getInternalFont(fontCell.getBold(), newColor.getIndex(), fontCell.getFontHeight(),
                                                            fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
							copyPropertiesFont(newFont, fontCell);

							newFont.setColor(newColor.getIndex());

                                                        newStyle = stylesCache.getCellStyle(newFont);
                                                        copyPropertiesStyle(newStyle, cellStyle);

							newStyle.setFont(newFont);
							pCells[i].setCellStyle(newStyle);					
						}					
						
					}
					else
					{
						short triplet[] = fontColor.getTriplet();
					
						if (triplet[0] != red || triplet[1] != green || triplet[2] != blue)
						{
							HSSFColor foundColor = palette.findColor((byte)red, (byte)green, (byte)blue);
							if (foundColor == null)
							{
								for (short j=9; j <= 63; j++)
								{
									foundColor = palette.getColor(j);
									if (foundColor == null)
									{
										newColor = palette.addColor((byte)red, (byte)green, (byte)blue);									
										j = 64;
									}
								}
								if (newColor == null)
								{
									newColor = palette.findSimilarColor((byte)red, (byte)green, (byte)blue);
									palette.setColorAtIndex(newColor.getIndex(), (byte)red, (byte)green, (byte)blue);
								}							
							}
							else
							{
								newColor = foundColor;						
							}
							
                                                        newFont = getInternalFont(fontCell.getBold(), newColor.getIndex(), fontCell.getFontHeight(),
                                                            fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
							copyPropertiesFont(newFont, fontCell);

							newFont.setColor(newColor.getIndex());

                                                        newStyle = stylesCache.getCellStyle(newFont);
                                                        copyPropertiesStyle(newStyle, cellStyle);

							newStyle.setFont(newFont);
							pCells[i].setCellStyle(newStyle);					
						}					
					}			
				}
				else
				{
					//Es el ofset que hay que sumar para que el colorIndex quede igual 
					//al de la implementacion anterior de excel
					value = value +7;  
					if (fontColor != null)
					{
						if (fontColor.getIndex() != value)
						{
                                                        newFont = getInternalFont(fontCell.getBold(), (short)value, fontCell.getFontHeight(),
                                                            fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
							copyPropertiesFont(newFont, fontCell);

							newFont.setColor((short)value);

                                                        newStyle = stylesCache.getCellStyle(newFont);
                                                        copyPropertiesStyle(newStyle, cellStyle);

							newStyle.setFont(newFont);
							pCells[i].setCellStyle(newStyle);
						}
					}
					else
					{
                                                newFont = getInternalFont(fontCell.getBold(), (short)value, fontCell.getFontHeight(),
                                                            fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
						copyPropertiesFont(newFont, fontCell);

						newFont.setColor((short)value);

                                                newStyle = stylesCache.getCellStyle(newFont);
                                                copyPropertiesStyle(newStyle, cellStyle);

						newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);						
					}
				}
			}
		} catch (Exception e)
		{
			m_errAccess.setErrDes("Invalid font properties");
			m_errAccess.setErrCod((short)6);
		}		
	}
	
	private void copyPropertiesStyle(HSSFCellStyle dest, HSSFCellStyle source)
	{
          dest.setAlignment(source.getAlignmentEnum());
          dest.setBorderBottom(source.getBorderBottomEnum());
          dest.setBorderLeft(source.getBorderLeftEnum());
          dest.setBorderTop(source.getBorderTopEnum());
          dest.setBottomBorderColor(source.getBottomBorderColor());
          dest.setDataFormat(source.getDataFormat());
          dest.setFillBackgroundColor(source.getFillBackgroundColor());
          dest.setFillForegroundColor(source.getFillForegroundColor());
          dest.setFillPattern(source.getFillPatternEnum());
          dest.setFont(pWorkbook.getFontAt(source.getFontIndex()));
          dest.setHidden(source.getHidden());
          dest.setIndention(source.getIndention());
          dest.setLeftBorderColor(source.getLeftBorderColor());
          dest.setLocked(source.getLocked());
          dest.setRightBorderColor(source.getRightBorderColor());
          dest.setRotation(source.getRotation());
          dest.setTopBorderColor(source.getTopBorderColor());
          dest.setVerticalAlignment(source.getVerticalAlignmentEnum());
          dest.setWrapText(source.getWrapText());
	}
	
	private void copyPropertiesFont(HSSFFont dest, HSSFFont source)
	{
		dest.setFontHeightInPoints(source.getFontHeightInPoints());
		dest.setFontName(source.getFontName());
		dest.setBold(source.getBold());
		dest.setItalic(source.getItalic());
		dest.setUnderline(source.getUnderline());
		dest.setColor(source.getColor());
	}	

    private void fitColumnWidth(int i, int data)
    {
          if(fitColumnWidth)
          {
				short colW = (short) pSelectedSheet.getColumnWidth((short)(i+pColPos-1));
				if((256*(data)) > colW)
				{
					colW = (short)(256 * (data));
				}
				pSelectedSheet.setColumnWidth((short)(i+pColPos-1), colW);
          }
    }

    public void setFitColumnWidth(boolean fitCol)
    {
		fitColumnWidth = fitCol;
    }

    public boolean getFitColumnWidth()
    {
		return fitColumnWidth;
    }

	/*public void setAlignment(short value)
	{
		setCellStyleProperty("alignment", new Short(value));
	}
	
	public void setBorder(short value)
	{
		setCellStyleProperty("borderLeft", new Short(value));
	}
	
	public void setCellStyleProperty(String propertyName, Object propertyValue)
	{
		try
	    {	    
			
			HSSFCellStyle originalStyle =	 pCells[1].getCellStyle();
			HSSFCellStyle newStyle = null;
			
	        Map values = PropertyUtils.describe(originalStyle);
	        values.put(propertyName, propertyValue);
	        values.remove("index");  // not good to compare on
	        
	        short numberCellStyles = pWorkbook.getNumCellStyles();
	        
	        for (short i = 0; i < numberCellStyles; i++)
	        {
	        	HSSFCellStyle wbStyle = pWorkbook.getCellStyleAt(i);
	            Map wbStyleMap = PropertyUtils.describe(wbStyle);
	            wbStyleMap.remove( "index" );
	  
	            if (wbStyleMap.equals(values))
	            {
	            	newStyle = wbStyle;
	            	//System.out.println("founded!!!");
	                break;
	            }
	        }
	  
	        if ( newStyle == null )
	        {
	        	newStyle = pWorkbook.createCellStyle();
	        	newStyle.setFont(pWorkbook.getFontAt(originalStyle.getFontIndex()));
	        	
	        	PropertyUtils.copyProperties(newStyle, originalStyle);
	        	PropertyUtils.setProperty(newStyle, propertyName, propertyValue);
	        	//System.out.println("not founded!!!");
	        }
	        
	        pCells[1].setCellStyle(newStyle);
	        
	    }
		
	    catch (Exception e)
	    {
	    	e.printStackTrace();
//	    	throw new NestableException( "Couldn't setCellStyleProperty.", e );
	    }
	}
	*/
	
    private boolean fitColumnWidth;
	private int cntCells;
    private int pColPos;
    private HSSFSheet pSelectedSheet;
	private HSSFCell[] pCells;
	private HSSFWorkbook pWorkbook;
}