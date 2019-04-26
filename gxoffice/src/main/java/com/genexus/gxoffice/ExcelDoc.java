package com.genexus.gxoffice;
import java.util.*;
import java.io.*;

import com.genexus.LocalUtil;
import com.genexus.util.GXFile;
import com.genexus.Application;
import com.genexus.util.GXServices;

public class ExcelDoc
{
	public short Index = -1;
	private String excelFileName = "";
		
	private boolean makeExternalUpload = Application.getGXServices().get(GXServices.STORAGE_SERVICE) != null;
	
	public void setDefaultUseAutomation(short useAutomation)
	{
	}

	public static short getDefaultUseAutomation()
	{
		return 0;
	}
	
	public void setUseAutomation(short useAutomation)
	{
	}
	
	public short getUseAutomation()
	{
		return 0;
	}
	
	boolean readOnly = false;
	public void setReadOnly(short readOnly)
	{
		this.readOnly = readOnly != 0 ? true : false;
	}

	boolean bufferedStreaming = false;
	public void setBufferedStreaming(boolean bufferedStreaming)
	{
		this.bufferedStreaming = bufferedStreaming;
	}

	public short getReadOnly()
	{
		return (short)(readOnly ? 1 : 0);
	}

	
	IExcelDocument document;
	public void checkExcelDocument()
	{
		if(document == null)
		{
			try
			{
				if (excelFileName.endsWith(".xlsx") || excelFileName.endsWith(".xlsm") || excelFileName.endsWith(".xlsb") || excelFileName.endsWith(".xlam") || isXlsx(excelFileName))
				{
					if (bufferedStreaming){
						Class.forName("org.apache.poi.xssf.streaming.SXSSFWorkbook");
						document = new com.genexus.gxoffice.poi.sxssf.ExcelDocument();
					}else{
						Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
						document = new com.genexus.gxoffice.poi.xssf.ExcelDocument();
					}
				}else
				{
					Class.forName("org.apache.poi.hssf.usermodel.HSSFWorkbook");
					document = new com.genexus.gxoffice.poi.hssf.ExcelDocument();
				}				
			}catch(Throwable e)
			{
				document = new com.genexus.gxoffice.ExcelDocument();
			}
			document.setReadOnly(getReadOnly());
			if(!defPath.equals(""))setDefaultPath(defPath);
			if(!template.equals(""))setTemplate(template);
			setErrDisplay(errDisplay);
			if(!delimiter.equals(""))setDelimiter(delimiter);
			setAutoFit(autoFit);
		}		
	}

	private boolean isXlsx(String fileName) throws Throwable
	{
		try
		{
			GXFile file = new GXFile(fileName);
			java.io.InputStream is = new BufferedInputStream(file.getStream());			
			boolean isXlsx = org.apache.poi.poifs.filesystem.DocumentFactoryHelper.hasOOXMLHeader(is);
			is.close();
			return isXlsx;
		}
		catch(java.io.IOException e)
		{
			return false;
		}
	}
		
	// Otras properties

	String defPath = "";
	public void setDefaultPath(String p1)
	{
		defPath = p1;
		if(document != null)
		{
			document.setDefaultPath(p1);
		}
	}
	public String getDefaultPath()
	{		
		return document != null ? document.getDefaultPath() : defPath;
	}
	
	String template = "";
	public void setTemplate(String p1)
	{
    	File path = new File(p1);
		if(com.genexus.ModelContext.getModelContext() != null)
		{
			com.genexus.internet.HttpContext webContext = (com.genexus.internet.HttpContext) com.genexus.ModelContext.getModelContext().getHttpContext();
			if( (webContext != null) && (webContext instanceof com.genexus.webpanels.HttpContextWeb))
			{
            	if (!path.isAbsolute())
				{
					p1 = ( (com.genexus.webpanels.HttpContextWeb) webContext).getRealPath(p1);
				}
            }
		}
                
        if(makeExternalUpload){
            String localTemplate = p1;
            if(path.isAbsolute())
                p1 =path.getName().toString();
            GXFile template = new GXFile(p1);
            if(!template.exists()){
                Application.getExternalProvider().upload(localTemplate, p1, false);
            }
        }		
		template = p1;
		if(document != null)
		{
			document.setTemplate(p1);
		}
	}
	
	public String getTemplate()
	{
		return document != null ? document.getTemplate() : template;
	}
	
	public void setDateFormat(LocalUtil localUtil, int lenDate, int lenTime, int ampmFmt, int dateFmt,
			String dSep, String tSep, String dtSep)
	{		
		String dFormat = localUtil.getDateFormatPattern(lenDate, lenTime, ampmFmt, dateFmt, dSep, tSep, dtSep);
		document.setDateFormat(dFormat);
	}
	
	short errDisplay = 0;
	public void setErrDisplay(short p1)
	{
		errDisplay = p1;
		if(document != null)
		{					
			document.setErrDisplay(p1);
		}
	}
	
	public short getErrDisplay()
	{
		return document != null ? document.getErrDisplay() : errDisplay;
	}

	String delimiter = "";
	public void setDelimiter(String p1)
	{
		delimiter = p1;
		if(document != null)
		{
			document.setDelimiter(p1);
		}
	}
	
	public String getDelimiter()
	{
		return document != null ? document.getDelimiter() : delimiter;
	}

	
	short autoFit = 0;
	public void setAutoFit(short p1)
	{
		autoFit = p1;
		if(document != null)
		{
			document.setAutoFit(p1);
		}
	}
	public short getAutoFit()
	{
		return document != null ? document.getAutoFit() : autoFit;
	}

	// Metodos wrapper
	//
	
	public short Open(String xlName)
	{
		if(!makeExternalUpload && com.genexus.ModelContext.getModelContext() != null)
		{
            com.genexus.internet.HttpContext webContext = (com.genexus.internet.HttpContext) com.genexus.ModelContext.getModelContext().getHttpContext();
            if( (webContext != null) && (webContext instanceof com.genexus.webpanels.HttpContextWeb))
			{
				if (!new File(xlName).isAbsolute())
				{
					xlName = ( (com.genexus.webpanels.HttpContextWeb) webContext).getRealPath(xlName);
				}
            }
        }
		
		if(document != null && closed)
		{
			document.cleanup();
			document = null;
		}		
		excelFileName = xlName;
		checkExcelDocument();
		closed = false;
		return document.Open(xlName);
	}

	public short Show()
	{
		return document != null ? document.Show() : -1;
	}		

	public boolean closed = false;
	public short Close()
	{
		if(document != null)
		{
			short ret = document.Close();
			if(ret == 0)
			{
				closed = true;
//				document.cleanup();				
//				document = null;
			}
			return ret;
		}
		return 0;
	}

	public short Unbind()
	{
		return document != null ? document.Unbind() : -1;
	}

	public short Save()
	{
		return document != null ? document.Save() : -1;
	}
	public short Hide()
	{
		return document != null ? document.Hide() : -1;
	}
	public short Clear()
	{
		return document != null ? document.Clear() : -1;
	}
	public IExcelCells Cells(int Row, int Col, int Height, int Width)
	{
		checkExcelDocument();
		return document.getCells(Row, Col, Height, Width);
	}
	
	public IExcelCells Cells(int Row, int Col)
	{
		return Cells(Row, Col, 1, 1);
	}

	public short PrintOut()
	{
		return PrintOut((short) 0);
	}

	public short PrintOut(short Preview)
	{
		return document != null ? document.PrintOut(Preview) : -1;
	}
	public short SelectSheet(String Sheet)
	{
		return document != null ? document.SelectSheet(Sheet) : -1;
	}
	public short RenameSheet(String SheetName)
	{
		return document != null ? document.RenameSheet(SheetName) : -1;
	}
	
	public short getErrCode()
	{
		return document != null ? document.getErrCode() : -2;
	}
	public String getErrDescription()
	{
		return document != null ? document.getErrDescription() : "";
	}

	//  public short RunMacro(short xlHandle, String Macro, Variant Arg1, Variant Arg2, Variant Arg3, Variant Arg4, Variant Arg5, Variant Arg6, Variant Arg7, Variant Arg8, Variant Arg9, Variant Arg10, Variant Arg11, Variant Arg12, Variant Arg13, Variant Arg14, Variant Arg15, Variant Arg16, Variant Arg17, Variant Arg18, Variant Arg19, Variant Arg20, Variant Arg21, Variant Arg22, Variant Arg23, Variant Arg24, Variant Arg25, Variant Arg26, Variant Arg27, Variant Arg28, Variant Arg29, Variant Arg30);

	//  public Variant getMacroReturnValue();

	//  public Object getGetWorkbook(short xlHandle);

	public void cleanup()
	{
		if(document != null)
		{
			document.cleanup();
		}
	}
}
