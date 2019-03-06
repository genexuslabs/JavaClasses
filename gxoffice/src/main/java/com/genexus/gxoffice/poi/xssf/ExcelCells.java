
package com.genexus.gxoffice.poi.xssf;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.util.Date;
import com.genexus.gxoffice.IExcelCells;
import com.genexus.CommonUtil;
import com.genexus.gxoffice.IGxError;


public class ExcelCells implements IExcelCells{
	protected ExcelDocument doc;

	protected IGxError m_errAccess; 
	protected int pWidth, pHeight;
	protected boolean readonly;
	protected StylesCache stylesCache;
	public ExcelCells(IGxError errAccess,ExcelDocument document, Workbook workBook, org.apache.poi.ss.usermodel.Sheet selectedSheet, int rowPos, int colPos, int height, int width, StylesCache stylesCache)
	{
		this(errAccess, document, workBook, selectedSheet, rowPos, colPos, height, width, false, stylesCache);
	}

	public ExcelCells(){}
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
		pCells = new XSSFCell[(width * height) + 1];
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
	
	protected Row getExcelRow(org.apache.poi.ss.usermodel.Sheet sheet, int rowPos)
	{
		Row row = sheet.getRow(rowPos);
		
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

	protected Cell getExcelCell(Row row, short colPos)
	{
		Cell cell = row.getCell(colPos);
		
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
                  String dformat = this.doc.getDateFormat().toLowerCase();
                  
                  if(value.getMinutes() == 0 && value.getHours() ==0 && value.getSeconds() ==0 && dformat.indexOf(' ') > 0)
                  {
                	  dformat = dformat.substring(0, dformat.indexOf(' '));
                  }

				  CellStyle newStyle = pWorkbook.createCellStyle();

					for (int i=1;i <= cntCells; i++)
					{
						DataFormat df = pWorkbook.createDataFormat();
						CellStyle cellStyle = pCells[i].getCellStyle();
						copyPropertiesStyle(newStyle, cellStyle);
						newStyle.setDataFormat(df.getFormat(dformat));						
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
						pCells[i].setCellType(XSSFCell.CELL_TYPE_STRING);
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
			if (pCells[1].getCellType()==XSSFCell.CELL_TYPE_FORMULA)
				return "=" + pCells[1].getCellFormula();
			else if (pCells[1].getCellType()==XSSFCell.CELL_TYPE_NUMERIC)
			{
				if(org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(pCells[1]))
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
			if (pCells[1].getCellType()==XSSFCell.CELL_TYPE_FORMULA)
			{
				String type = getFormulaType();
				if (type == "N")
					return Double.toString(getNumber());
				else if (type == "D")
					return getDate().toString();
				else
					return pCells[1].getStringCellValue();
			}
			else if (pCells[1].getCellType()==XSSFCell.CELL_TYPE_BOOLEAN)
			{
				Boolean b = pCells[1].getBooleanCellValue();
				return String.valueOf(b);		
			}			
			else
			{
				return pCells[1].getStringCellValue();
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
			case XSSFCell.CELL_TYPE_BLANK:
				type = "U";
				break;
			case XSSFCell.CELL_TYPE_BOOLEAN:
				type = "N";
				break;
			case XSSFCell.CELL_TYPE_ERROR:
				type = "U";
				break;
			case XSSFCell.CELL_TYPE_FORMULA:				
				type = getFormulaType();
				break;
			case XSSFCell.CELL_TYPE_NUMERIC:
				if(org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(pCells[1]))	
				{
					type = "D";				
				} else {
					type = "N"; }			
				break;
			case XSSFCell.CELL_TYPE_STRING:
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
            
			DataFormatter formatter = new DataFormatter();
			
			java.text.Format format = formatter.getDefaultFormat(pCells[1]);
			if (format.getClass() == java.text.DateFormat.class )
			{
				pCells[1].getDateCellValue();
				return "D";
			}
			else
			{
				return "N";
			}			
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
				CellStyle cellStyle = pCells[i].getCellStyle();
				Font fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
				CellStyle newStyle = null;
				Font newFont = null;
				
				if (fontCell.getFontHeightInPoints() != value)
				{
				//	System.out.println("Changing Size...");
                                        newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), (short) (value * 20),
                                               fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), fontCell.getUnderline());
					copyPropertiesFont(newFont, fontCell);

					newFont.setFontHeightInPoints((short) value);
					newFont.setFontHeight((short)(value * 20));

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
	
        protected Font getInternalFont(boolean b, short color, short fontHeight, String name,
                                         boolean italic, boolean strikeout, short typeOffset, byte underline)
        {
        		Font font = pWorkbook.findFont(b, color, fontHeight, name, italic, strikeout, typeOffset, underline);
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
				CellStyle cellStyle = pCells[i].getCellStyle();
				Font fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
				CellStyle newStyle = null;
				Font newFont = null;
				
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
			CellStyle cellStyle = pCells[i].getCellStyle();
			Font fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			CellStyle newStyle = null;
			Font newFont = null;
			
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
					if (!fontCell.getBold())
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
			CellStyle cellStyle = pCells[i].getCellStyle();
			Font fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			CellStyle newStyle = null;
			Font newFont = null;
			
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
		if (pWorkbook.getFontAt(pCells[1].getCellStyle().getFontIndex()).getUnderline() != XSSFFont.U_NONE)
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
			CellStyle cellStyle = pCells[i].getCellStyle();
			Font fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			CellStyle newStyle = null;
			Font newFont = null;
			
			switch (value)
			{
				case 0:				
					if (fontCell.getUnderline() != XSSFFont.U_NONE)
					{
                                          newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                              fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), XSSFFont.U_NONE);
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setUnderline(XSSFFont.U_NONE);

                                        newStyle = stylesCache.getCellStyle(newFont);
			        	copyPropertiesStyle(newStyle, cellStyle);

			        	newStyle.setFont(newFont);
						pCells[i].setCellStyle(newStyle);
					}
					break;
				case 1:
					if (fontCell.getUnderline() != XSSFFont.U_SINGLE)
					{
                                          newFont = getInternalFont(fontCell.getBold(), fontCell.getColor(), fontCell.getFontHeight(),
                                              fontCell.getFontName(), fontCell.getItalic(), fontCell.getStrikeout(), fontCell.getTypeOffset(), XSSFFont.U_SINGLE);
			        	copyPropertiesFont(newFont, fontCell);

			        	newFont.setUnderline(XSSFFont.U_SINGLE);

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
			XSSFCellStyle cellStyle = pCells[i].getCellStyle();
			XSSFFont fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
			CellStyle newStyle = null;
			XSSFFont newFont = null;

			newStyle = pWorkbook.createCellStyle();
			PropertyUtils.copyProperties(newStyle, cellStyle);
			newFont = pWorkbook.createFont();	
			int colorIdx=ColorManager.getColor(pWorkbook);
			PropertyUtils.copyProperties(newFont, pWorkbook.getFontAt(cellStyle.getFontIndex()));
			palette.setColorAtIndex((short)colorIdx,(byte)r,(byte)g,(byte)b);
			newFont.setColor((short)colorIdx);
			//newFont.setColor(XSSFFont.COLOR_RED);
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
				CellStyle cellStyle = pCells[i].getCellStyle();
				Font fontCell = pWorkbook.getFontAt(cellStyle.getFontIndex());
				CellStyle newStyle = null;
				XSSFFont newFont = null;
				XSSFColor newColor = null;			
					
				XSSFColor fontColor = ((XSSFFont) fontCell).getXSSFColor();
					
				int val=(int)value;
				int red=val >> 16 & 0xff;
				int green=val >> 8 & 0xff;
				int blue=val & 0xff;
				
				if (red != 0 || green != 0 || blue > 56) //Si es value esta entre 1 y 56 entonces supongo que es un color Index de Excel y voy por el else 
				{					
					if (fontColor == null || (fontColor != null && (fontColor.getRGB() == null ||  (fontColor.getRGB()[0] == 0 && fontColor.getRGB()[1] == 0 && fontColor.getRGB()[2] == 0))))
					{
						//System.out.println("Automatic color.");
						
						if ((red + green + blue) != 0)
						{										 
							newColor = new XSSFColor(new java.awt.Color(red,green,blue));												
							
							newFont = (XSSFFont)pWorkbook.createFont();
							copyPropertiesFont(newFont, fontCell);

							newFont.setColor(newColor);
							
							newStyle = pWorkbook.createCellStyle();
							copyPropertiesStyle(newStyle, cellStyle);

							newStyle.setFont(newFont);
							pCells[i].setCellStyle(newStyle);					
						}											
					}
					else
					{
						byte[] triplet = fontColor.getRGB();
					
						if (triplet[0] != red || triplet[1] != green || triplet[2] != blue)
						{
							newColor = new XSSFColor(new java.awt.Color(red,green,blue));						
							
							newFont = (XSSFFont)pWorkbook.createFont();
							copyPropertiesFont(newFont, fontCell);

							newFont.setColor(newColor);
							
							newStyle = pWorkbook.createCellStyle();
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
						if (fontColor.getIndexed() != value)
						{
                            newFont = (XSSFFont) getInternalFont(fontCell.getBold(), (short)value, fontCell.getFontHeight(),
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
                                                newFont = (XSSFFont) getInternalFont(fontCell.getBold(), (short)value, fontCell.getFontHeight(),
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
	
	protected void copyPropertiesStyle(CellStyle dest,CellStyle source)
	{
					dest.cloneStyleFrom(source);       
	}
	
	protected void copyPropertiesFont(Font dest, Font source)
	{
		dest.setFontHeightInPoints(source.getFontHeightInPoints());
		dest.setFontName(source.getFontName());
		dest.setBold(source.getBold());
		dest.setItalic(source.getItalic());
		dest.setUnderline(source.getUnderline());
		dest.setColor(source.getColor());
		dest.setStrikeout(source.getStrikeout());
		dest.setTypeOffset(source.getTypeOffset());		
	}	

    private void fitColumnWidth(int i, int data)
    {
          if(fitColumnWidth)
          {
				int colW = pSelectedSheet.getColumnWidth((int)(i+pColPos-1));
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
			
			XSSFCellStyle originalStyle =	 pCells[1].getCellStyle();
			CellStyle newStyle = null;
			
	        Map values = PropertyUtils.describe(originalStyle);
	        values.put(propertyName, propertyValue);
	        values.remove("index");  // not good to compare on
	        
	        short numberCellStyles = pWorkbook.getNumCellStyles();
	        
	        for (short i = 0; i < numberCellStyles; i++)
	        {
	        	XSSFCellStyle wbStyle = pWorkbook.getCellStyleAt(i);
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
	
    protected boolean fitColumnWidth;
    protected int cntCells;
    protected int pColPos;
    protected org.apache.poi.ss.usermodel.Sheet pSelectedSheet;
    protected Cell[] pCells;
    protected Workbook pWorkbook;
}