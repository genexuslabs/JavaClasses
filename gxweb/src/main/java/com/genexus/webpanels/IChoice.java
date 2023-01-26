package com.genexus.webpanels;

public interface IChoice extends IHTMLObject
{
	String getValue();
	void setValue(String value);
	void addItem(long value, String description);
	void addItem(String value, String description);
	void addItem(double value, String description);
//	void addItem(java.util.Date value, String description);
	void addItem(long value, String description, int index);
	void addItem(String value, String description, int index);
	void addItem(double value, String description, int index);
//	void addItem(java.util.Date value, String description, int index);
	String getItemValue(int ndx);
	String getItemText(int ndx);
	String getDescription();
	void setDescription(String value);
	void removeAllItems();
	void removeItem(String ndx);
	int getItemCount();
	int getCount();
	void setSort(int sort);
	boolean isSelected(int ndx);
}
