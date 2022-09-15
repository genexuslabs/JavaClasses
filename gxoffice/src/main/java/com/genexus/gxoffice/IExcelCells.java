package com.genexus.gxoffice;

import java.util.Date;

public interface IExcelCells {
	public String getValue();

	public double getNumber();
	public void setNumber(double Value);

	public Date getDate();
	public void setDate(Date Value);

	public String getText();
	public void setText(String Value);

	public String getFont();
	public void setFont(String sNewVal);

	public long getColor();
	public void setColor(long nNewVal);

	public long getBackColor();
	public void setBackColor(long nNewVal);

	public double getSize();
	public void setSize(double nNewVal);

	public String getType();

	public short getBold();
	public void setBold(short nNewVal);

	public short getItalic();
	public void setItalic(short nNewVal);

	public short getUnderline();
	public void setUnderline(short nNewVal);
}
