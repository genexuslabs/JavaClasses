package com.genexus.gxoffice;
import java.util.*;

public interface IExcelCells
{
public double  getNumber();
  public Date    getDate();
  public String  getText();
  public String  getValue();

  public void    setNumber(double Value);
  public void    setDate(Date Value);
  public void    setText(String Value);

  public String	getFont();
  public void 	setFont(String sNewVal);

  public long	getColor();
  public void 	setColor(long nNewVal);

  public double	getSize();
  public void 	setSize(double nNewVal);

  public String	getType();

  public short	getBold();
  public void 	setBold(short nNewVal);

  public short	getItalic();
  public void 	setItalic(short nNewVal);

  public short	getUnderline();
  public void 	setUnderline(short nNewVal);
}
