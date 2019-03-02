package com.genexus.gxoffice;

public class ExcelDocument implements IExcelDocument
{
	static
	{
		System.loadLibrary("gxoffice2");
	}
	public short Index = -1;

	public native short Open(String xlName);

	public native short Show();

	public native short Close();

	public native short Unbind();

	public native short Save();

	public native short Hide();

	public native short Clear();

	public native ExcelCells Cells(int Row, int Col, int Height, int Width);

	public IExcelCells getCells(int Row, int Col, int Height, int Width)
	{
		return (IExcelCells)Cells(Row, Col, Height, Width);
	}
	
	public IExcelCells Cells(int Row, int Col)
	{
		return (IExcelCells)Cells(Row, Col, 1, 1);
	}

	public short PrintOut()
	{
		return PrintOut((short) 0);
	}

	public native short PrintOut(short Preview);

	public native short SelectSheet(String Sheet);

	public native short RenameSheet(String SheetName);

	public native short getErrCode();

	public native String getErrDescription();

	public native void setErrDisplay(short _jcomparam_0);
	public native short getErrDisplay();

	public native void setDefaultPath(String _jcomparam_0);
	public native String getDefaultPath();

	public native void setTemplate(String _jcomparam_0);
	public native String getTemplate();

	public native void setDelimiter(String _jcomparam_0);
	public native String getDelimiter();

	public native void setReadOnly(short _jcomparam_0);
	public native short getReadOnly();

	public native void setAutoFit(short _jcomparam_0);
	public native short getAutoFit();

	//  public short RunMacro(short xlHandle, String Macro, Variant Arg1, Variant Arg2, Variant Arg3, Variant Arg4, Variant Arg5, Variant Arg6, Variant Arg7, Variant Arg8, Variant Arg9, Variant Arg10, Variant Arg11, Variant Arg12, Variant Arg13, Variant Arg14, Variant Arg15, Variant Arg16, Variant Arg17, Variant Arg18, Variant Arg19, Variant Arg20, Variant Arg21, Variant Arg22, Variant Arg23, Variant Arg24, Variant Arg25, Variant Arg26, Variant Arg27, Variant Arg28, Variant Arg29, Variant Arg30);

	//  public Variant getMacroReturnValue();

	//  public Object getGetWorkbook(short xlHandle);
	public native void cleanup();

	public void setDateFormat(String dFormat) {		
	}
}
