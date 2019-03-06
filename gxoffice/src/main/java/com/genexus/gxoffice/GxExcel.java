package com.genexus.gxoffice;

public class GxExcel
{
  static
  {
        System.loadLibrary("gxoffice2");
  }

  public native short Open(String xlName, short[] xlHandle);

  public native short Show(short xlHandle);

  public native short Close(short xlHandle);

  public native short Save(short xlHandle);

  public native short Hide(short xlHandle);

  public native short Clear(short xlHandle);

  public native short GetLong(short xlHandle, int xlRow, int xlCol, long[] xlValue);
  public native short GetDouble(short xlHandle, int xlRow, int xlCol, double[] xlValue);
  public native short GetDate(short xlHandle, int xlRow, int xlCol, java.util.Date[] xlValue);
  public native short GetString(short xlHandle, int xlRow, int xlCol, String[] xlValue);

  public native short PutLong(short xlHandle, int xlRow, int xlCol, long xlValue);
  public native short PutDouble(short xlHandle, int xlRow, int xlCol, double xlValue);
  public native short PutDate(short xlHandle, int xlRow, int xlCol, java.util.Date xlValue);
  public native short PutString(short xlHandle, int xlRow, int xlCol, String xlValue);

  public native short Type(short xlHandle, int xlRow, int xlCol, String[] xlType);

  public native short getError();

  public native void setDisplayMessages(short _jcomparam_0);

  public native void setDefaultPath(String _jcomparam_0);

  public native short PrintOut(short xlHandle, short xlPreview);

  public native void setTemplate(String _jcomparam_0);

  public native void setDelimiter(String _jcomparam_0);

  public native void setReadOnly(short _jcomparam_0);

  public native short SelectSheet(short xlHandle, String xlSheet);

  public native short RenameSheet(short xlHandle, String xlSheetName);

  public native short GetFormat(short xlHandle, int xlRow, int xlCol, int[] xlColor, String[] xlFont, int[] xlSize);

  public native short PutFormat(short xlHandle, int xlRow, int xlCol, int xlHeight, int xlWidth, int xlColor, String xlFont, double xlSize);

  public native void setAutoFit(short _jcomparam_0);

//  public short RunMacro(short xlHandle, String Macro, Variant Arg1, Variant Arg2, Variant Arg3, Variant Arg4, Variant Arg5, Variant Arg6, Variant Arg7, Variant Arg8, Variant Arg9, Variant Arg10, Variant Arg11, Variant Arg12, Variant Arg13, Variant Arg14, Variant Arg15, Variant Arg16, Variant Arg17, Variant Arg18, Variant Arg19, Variant Arg20, Variant Arg21, Variant Arg22, Variant Arg23, Variant Arg24, Variant Arg25, Variant Arg26, Variant Arg27, Variant Arg28, Variant Arg29, Variant Arg30);

//  public Variant getMacroReturnValue();

//  public Object getGetWorkbook(short xlHandle);


  public native void cleanup();
}
