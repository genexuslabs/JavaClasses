package com.genexus.gxoffice.poi.hssf;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import com.genexus.gxoffice.IExcelDocument;
import com.genexus.gxoffice.Constants;
import com.genexus.gxoffice.IExcelCells;
import com.genexus.gxoffice.IGxError;
import com.genexus.util.GXFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Diego
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class ExcelDocument implements IGxError,IExcelDocument{	
	
	public short Open(String fileName) 
	{
		resetError();
		if(fileName.indexOf('.') == -1)
		{
			fileName += ".xls";
		}

		InputStream is = null;
		try	
		{			
            if(!template.equals(""))
            {
                GXFile templateFile = new GXFile(template);
				if (templateFile.exists())
				{
					is = templateFile.getStream();
					POIFSFileSystem poifs = new POIFSFileSystem(is);
					workBook = new HSSFWorkbook(poifs);
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
				boolean isAbsolute = new java.io.File(fileName).isAbsolute();
				GXFile file = new GXFile(fileName, Constants.EXTERNAL_UPLOAD_ACL, isAbsolute);
				if (file.exists()) {
					is = file.getStream();
					POIFSFileSystem poifs = new POIFSFileSystem(is);
					workBook = new HSSFWorkbook(poifs);
				}
				else {
					//System.out.println("Creating..");
					workBook = new HSSFWorkbook();
				}
            }
			this.selectFirstSheet();
            xlsFileName = fileName.toString();
            stylesCache = new StylesCache(workBook);
		}
		
		catch(Exception e)
		{						
			errCod = 10; //error creando xls file
			errDescription = "Could not open file.";			
			System.err.println("GXOffice Error: "+ e.toString());
			return errCod;
		}

		finally
		{
			try {if (is != null) is.close();} catch (IOException e) {System.err.println("GXOffice Error: "+ e);}
		}
		return 0;
	}

	public short Save()
	{
		resetError();
                if(isReadOnly())
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
			boolean isAbsolute = new java.io.File(xlsFileName).isAbsolute();
			GXFile file = new GXFile(xlsFileName, Constants.EXTERNAL_UPLOAD_ACL, isAbsolute);
            file.create(in, true);
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
				HSSFSheet sheet = workBook.getSheetAt(i);
				int maxCellNum = 0;
				int lastRowNum = sheet.getLastRowNum(); 
				for (int j=0; j<=lastRowNum; j++) 
				{
					HSSFRow row = sheet.getRow(j);
					if (row != null)
						maxCellNum = Math.max(maxCellNum, row.getLastCellNum());
				}
				for (int k = 0; k < maxCellNum; k++)
				{
					sheet.autoSizeColumn(k);
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
			HSSFSheet pSheet = workBook.getSheet(sheetName);
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
	
	private void selectFirstSheet()
	{
                if ((workBook.getNumberOfSheets() == 0) && isReadOnly())
                {
                        return;
                }
		//System.out.println("Selecting First Sheet..");		
		if (workBook.getNumberOfSheets() == 0)
		{
			HSSFSheet pSheet = workBook.createSheet();
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

		HSSFRow delRow;
		HSSFSheet sheet = workBook.getSheet(currentSheet);
		
		int firstRowNum = sheet.getFirstRowNum();
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
		
		ExcelCells iCell = new ExcelCells((IGxError)this,workBook, workBook.getSheet(currentSheet), Row-1, Col-1, 1, 1, isReadOnly(), stylesCache);
		return iCell;
				
	}	
	public IExcelCells Cells(int Row, int Col, int Height, int Width) {		
		
		ExcelCells iCell = new ExcelCells((IGxError)this,workBook, workBook.getSheet(currentSheet), Row-1, Col-1, Height, Width, isReadOnly(), stylesCache);
		return iCell;		
	}
	public IExcelCells getCells(int Row, int Col, int Height, int Width)
    {
    		resetError();
        ExcelCells iCell = new ExcelCells(this, workBook, workBook.getSheet(currentSheet), Row - 1, Col - 1,Height ,Width, isReadOnly(), stylesCache);
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

        private boolean isReadOnly()
        {
          return (readOnly != 0);
        }

	public void cleanup()
	{
		ColorManager.cleanup(this.workBook);
	}	
	//IGxError implementation :Willy 1/06/05
	public void setErrCod(short EerrCod)
	{
		this.errCod=EerrCod;
	}
	public void setErrDes(String EerrDes)
	{
		this.errDescription=EerrDes;
	}
	
	private void resetError()
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

    private String template="";
	private HSSFWorkbook workBook;		
	private String currentSheet;
	private String xlsFileName;			
	private short errCod = 0;
	private String errDescription="OK"; 
	private short readOnly = 0;
        private StylesCache stylesCache;
	
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
		// TODO Auto-generated method stub
		
	}
}