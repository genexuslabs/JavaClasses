package com.genexus.gxoffice;

public interface IExcelDocument
{
	
  short Open(String xlName);

  short Show();

  short Close();

  short Unbind();

  short Save();

  short Hide();

  short Clear();

  IExcelCells getCells(int Row, int Col, int Height, int Width);

  short PrintOut(short Preview);

  short SelectSheet(String Sheet);

  short RenameSheet(String SheetName);

  short getErrCode();

  void setDateFormat(String dFormat);
  
  String getErrDescription();

  void setErrDisplay(short _jcomparam_0);
  short getErrDisplay();

  void setDefaultPath(String _jcomparam_0);
  String getDefaultPath();

  void setTemplate(String _jcomparam_0);
  String getTemplate();

  void setDelimiter(String _jcomparam_0);
  String getDelimiter();

  void setReadOnly(short _jcomparam_0);
  short getReadOnly();

  void setAutoFit(short _jcomparam_0);
  short getAutoFit();

//  public short RunMacro(short xlHandle, String Macro, Variant Arg1, Variant Arg2, Variant Arg3, Variant Arg4, Variant Arg5, Variant Arg6, Variant Arg7, Variant Arg8, Variant Arg9, Variant Arg10, Variant Arg11, Variant Arg12, Variant Arg13, Variant Arg14, Variant Arg15, Variant Arg16, Variant Arg17, Variant Arg18, Variant Arg19, Variant Arg20, Variant Arg21, Variant Arg22, Variant Arg23, Variant Arg24, Variant Arg25, Variant Arg26, Variant Arg27, Variant Arg28, Variant Arg29, Variant Arg30);

//  public Variant getMacroReturnValue();

//  public Object getGetWorkbook(short xlHandle);

  void cleanup();
}
