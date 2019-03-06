package com.genexus.gxoffice;

public class GxWord
{
  static
  {
        System.loadLibrary("gxoffice2");
  }
  
  public native short Open(String wName, short[] wHandle);

  public native short Hide(short wHandle);

  public native short Show(short wHandle);

  public native short Close(short wHandle);

  public native short getError();

  public native void setDisplayMessages(short _jcomparam_0);

  public native short PrintOut(short wHandle, short wPreview, short wBackground);

  public native void setTemplate(String _jcomparam_0);

  public native void setReadOnly(short _jcomparam_0);

  public native short Put(short wHandle, String wText, short wAppend);

  public native short Get(short wHandle, String[] wText);

  public native short SpellCheck(short wHandle);

  public native short Save(short wHandle);

  public native short Replace(short wHandle, String oldVal, String newVal, short MatchCase, short MatchWholeWord);

  public native short SaveAs(short wHandle, String Name, String Type, short DOSText, short LineBreaks);

  public native void cleanup();
}
