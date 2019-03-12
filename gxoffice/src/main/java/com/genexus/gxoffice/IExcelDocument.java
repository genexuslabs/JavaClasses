package com.genexus.gxoffice;

public interface IExcelDocument
{
	
  public short Open(String xlName);

  public short Show();

  public short Close();

  public short Unbind();

  public short Save();

  public short Hide();

  public short Clear();

  public IExcelCells getCells(int Row, int Col, int Height, int Width);

  public short PrintOut(short Preview);

  public short SelectSheet(String Sheet);

  public short RenameSheet(String SheetName);

  public short getErrCode();

  public void setDateFormat(String dFormat);
  
  public String getErrDescription();

  public void setErrDisplay(short _jcomparam_0);
  public short getErrDisplay();

  public void setDefaultPath(String _jcomparam_0);
  public String getDefaultPath();

  public void setTemplate(String _jcomparam_0);
  public String getTemplate();

  public void setDelimiter(String _jcomparam_0);
  public String getDelimiter();

  public void setReadOnly(short _jcomparam_0);
  public short getReadOnly();

  public void setAutoFit(short _jcomparam_0);
  public short getAutoFit();

//  public short RunMacro(short xlHandle, String Macro, Variant Arg1, Variant Arg2, Variant Arg3, Variant Arg4, Variant Arg5, Variant Arg6, Variant Arg7, Variant Arg8, Variant Arg9, Variant Arg10, Variant Arg11, Variant Arg12, Variant Arg13, Variant Arg14, Variant Arg15, Variant Arg16, Variant Arg17, Variant Arg18, Variant Arg19, Variant Arg20, Variant Arg21, Variant Arg22, Variant Arg23, Variant Arg24, Variant Arg25, Variant Arg26, Variant Arg27, Variant Arg28, Variant Arg29, Variant Arg30);

//  public Variant getMacroReturnValue();

//  public Object getGetWorkbook(short xlHandle);

  public void cleanup();	
}
