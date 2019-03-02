package com.genexus.gxoffice;
import java.util.*;

public class ExcelCells implements IExcelCells
{
  static
  {
        System.loadLibrary("gxoffice2");
  }

  public short Index = -1;

  public native double  getNumber();
  public native Date    getDate();
  public native String  getText();
  public native String  getValue();

  public native void    setNumber(double Value);
  public native void    setDate(Date Value);
  public native void    setText(String Value);

  public native String	getFont();
  public native void 	setFont(String sNewVal);

  public native long	getColor();
  public native void 	setColor(long nNewVal);

  public native double	getSize();
  public native void 	setSize(double nNewVal);

  public native String	getType();

  public native short	getBold();
  public native void 	setBold(short nNewVal);

  public native short	getItalic();
  public native void 	setItalic(short nNewVal);

  public native short	getUnderline();
  public native void 	setUnderline(short nNewVal);
}

