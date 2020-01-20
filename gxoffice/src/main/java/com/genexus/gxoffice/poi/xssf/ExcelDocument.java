package com.genexus.gxoffice.poi.xssf;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.genexus.gxoffice.IExcelDocument;
import com.genexus.gxoffice.Constants;
import com.genexus.gxoffice.IExcelCells;
import com.genexus.gxoffice.IGxError;
import com.genexus.util.GXFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class ExcelDocument implements IGxError,IExcelDocument{	
	
	private boolean saved = false;
	private String dateFormat = "m/d/yy h:mm";
	
	public short Open(String fileName) 
	{
		resetError();		
		if(fileName.indexOf('.') == -1)
		{
			fileName += ".xlsx";
		}
		
		try	
		{			
            if(!template.equals(""))
            {
                GXFile templateFile = new GXFile(template);
				if (templateFile.exists())
				{			
					workBook = new XSSFWorkbook(templateFile.getStream());
				}
				else
				{
				    errCod = 4; //Invalid template
				    errDescription = "Invalid template.";
				    return errCod;
				}
            }
            else
            {
                GXFile file = new GXFile(fileName, Constants.EXTERNAL_PRIVATE_UPLOAD);
				if (file.exists()) {
					//System.out.println("Opening..");				
					workBook = new XSSFWorkbook(file.getStream());
				}
				else {
					//System.out.println("Creating..");
					workBook = new XSSFWorkbook();
				}
            }
			this.selectFirstSheet();
            xlsFileName = fileName.toString();
            stylesCache = new StylesCache(workBook);
		}
		
		catch(Exception e)
		{						
			errCod = 10; //error creando xlsx file
			errDescription = "Could not open file.";			
			System.err.println("GXOffice Error: "+ e.toString());
			return errCod;
		}		
		return 0;
	}

	public short Save()
	{
		resetError();
        if(isReadOnly() || saved)  //saved. WA for bug: POI: https://issues.apache.org/bugzilla/show_bug.cgi?id=51158
                {
                    return -1;
                }
		autoFitColumns();
		recalculateFormulas();
		
		try {
			ByteArrayOutputStream fs = new ByteArrayOutputStream();
			workBook.write(fs);
			ByteArrayInputStream in = new ByteArrayInputStream(fs.toByteArray());
			fs.close();
            GXFile file = new GXFile(xlsFileName, Constants.EXTERNAL_PRIVATE_UPLOAD);
            file.create(in, true);
			saved = true;
		}
		catch(Exception e)
		{						
			errCod = 12; //error grabando xls file
			errDescription = "Could not save file.";			
			System.err.println("GXOffice Error: "+ e.toString());
			return -1;
		}		
		return 0;
	}
	
	private void recalculateFormulas() 
	{
		try 
		{
			workBook.setForceFormulaRecalculation(true);
		} 
		catch (Exception e) 
		{
			System.err.println("GXOffice Error: "+ e.toString());
		}	
	}

	private void autoFitColumns()
	{
		if (autoFit == 1)
		{
			int sheetsCount = workBook.getNumberOfSheets();
			for (int i = 0; i < sheetsCount; i++)
			{
				org.apache.poi.ss.usermodel.Sheet sheet = workBook.getSheetAt(i);
				Row row = sheet.getRow(sheet.getFirstRowNum());
				int columnCount = row.getPhysicalNumberOfCells();			
				for (int j = 0; j < columnCount; j++)
				{
					sheet.autoSizeColumn(j);
				}
			}
		}
	}
	
	public short Close() 
	{
		resetError();
		Save();	
		return 0;
	}
	
	public short SelectSheet(String sheetName)
	{
		resetError();
		if(sheetName!=null && sheetName.compareTo("")!=0)
		{
			org.apache.poi.ss.usermodel.Sheet pSheet = workBook.getSheet(sheetName);
                        if ((pSheet == null) && isReadOnly())
                        {
                               this.errCod=5;
                               this.errDescription="Invalid worksheet name";
                               return -1;
                        }

			if (pSheet == null) { // not exists sheet
				pSheet = workBook.createSheet(sheetName);
			}
			currentSheet = sheetName.toString();
                        return 0;
		} else
		{
			this.errCod=5;
			this.errDescription="Invalid worksheet name";
                        return -1;
		}
	}
	
	protected void selectFirstSheet()
	{
		if ((workBook.getNumberOfSheets() == 0) && isReadOnly())
        {
              return;
        }
		//System.out.println("Selecting First Sheet..");		
		if (workBook.getNumberOfSheets() == 0)
		{
			workBook.createSheet();
		}
		currentSheet = workBook.getSheetName(0);
	}
	
	public short RenameSheet(String sheetName)
	{		
		resetError();
        if(isReadOnly())
        {
             errCod = 13;
             errDescription = "Can not modify a readonly document";
             return -1;
        }
		workBook.setSheetName(workBook.getSheetIndex(currentSheet), sheetName);
		currentSheet = sheetName;
		return 0;
	}
	
	public short Clear()
	{		
		resetError();
        if(isReadOnly())
        {
            errCod = 13;
            errDescription = "Can not modify a readonly document";
            return -1;
        }

        Row delRow;
		Sheet sheet = workBook.getSheet(currentSheet);
		
		int lastRowNum = sheet.getLastRowNum();
		
		for (int i = 0; i <= lastRowNum; i++)
		{
			delRow = sheet.getRow(i);
			if (delRow != null)
			{
				sheet.removeRow(delRow);
			}			
		}
		//System.out.println("Rows clear complete.");
		return 0;
	}
	
	public IExcelCells Cells(int Row, int Col) {		
		
		ExcelCells iCell = new ExcelCells((IGxError)this, this, workBook, workBook.getSheet(currentSheet), Row-1, Col-1, 1, 1, isReadOnly(), stylesCache);
		return iCell;
				
	}	
	public IExcelCells Cells(int Row, int Col, int Height, int Width) {		
		
		ExcelCells iCell = new ExcelCells((IGxError)this,this, workBook, workBook.getSheet(currentSheet), Row-1, Col-1, Height, Width, isReadOnly(), stylesCache);
		return iCell;		
	}
	public IExcelCells getCells(int Row, int Col, int Height, int Width)
    {
        ExcelCells iCell = new ExcelCells(this,this,  workBook, workBook.getSheet(currentSheet), Row - 1, Col - 1,Height ,Width, isReadOnly(), stylesCache);
        return iCell;
    }
	
	public short getErrCode()
	{
		return this.errCod;
	}
	
	public String getErrDescription()
	{
		return this.errDescription;
	}
	
	public void setReadOnly(short value)
	{
		readOnly = value;
	}
	
	public short getReadOnly()
	{
		return readOnly;
	}

        protected boolean isReadOnly()
        {
          return (readOnly != 0);
        }

	public void cleanup()
	{		
	}	
	
	public void setErrCod(short EerrCod)
	{
		this.errCod=EerrCod;
	}
	public void setErrDes(String EerrDes)
	{
		this.errDescription=EerrDes;
	}

	protected void resetError()
	{
		errCod = 0;
		errDescription="OK";	
	}

    public void setTemplate(String t)
    {
      template = t;
    }

	public String getTemplate()
    {
      return template;
    }

    public Workbook getWorkBook()
	{
		return workBook;
	}

    protected String template="";
    protected Workbook workBook;		
    protected String currentSheet;
    protected String xlsFileName;			
    protected short errCod = 0;
    protected String errDescription="OK"; 
    protected short readOnly = 0;
    protected StylesCache stylesCache;

    public short Show() { return -1;}
    public short Unbind() { return -1;}
    public short Hide() { return -1;}
    public short PrintOut(short preview) { return -1;}
    public void setErrDisplay(short s) { }
    public short getErrDisplay() { return -1; }
    public void setDefaultPath(String  s) {  }
    public String getDefaultPath() { return ""; }
    public void setDelimiter(String t) {  }
    public String getDelimiter() { return ""; }

    short autoFit = 0;
    public void setAutoFit(short s) { autoFit = s; }
    public short getAutoFit() { return autoFit; }


    public void setDateFormat(String dFormat) {
    	dateFormat = dFormat;		
    }
    public String getDateFormat()
    {
    	return dateFormat;
    }
}