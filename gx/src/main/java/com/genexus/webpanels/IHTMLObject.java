package com.genexus.webpanels;

public interface IHTMLObject 
{
	String getThemeClass();
	void setThemeClass(String themeClass);

	String getColumnClass();
	void setColumnClass(String themeClass);

	String getColumnHeaderClass();
	void setColumnHeaderClass(String themeClass);

	String getJsevent();
	void setJsevent(String onclick);

	String getJsonclick();
	void setJsonclick(String onclick);

	String getInternalname();
	void setInternalname(String onclick);

	void setFocus();
	void setWebtags(String tags);
	String getWebtags();

	String getLinkTarget();
	void setLinkTarget(String link);

	String getLink();
	void setLink(String link);

	byte getBackstyle();
	void setBackstyle(int backGround);

	int getIBackground();
	void setIBackground(int backGround);
	int getIForeground();
	void setIForeground(int backGround);

	byte getFontBold();
	void setFontBold(int fontBold);
	byte getFontItalic();
	void setFontItalic(int fontItalic);
	byte getFontUnderline();
	void setFontUnderline(int fontUnderline);
	byte getFontStrikethru();
	void setFontStrikethru(int fontStrikethru);
	String getFontName();
	void setFontName(String fontName);
	byte getFontSize();
	void setFontSize(int fontSize);
	byte getIsPassword();
	void setIsPassword(int isPassword);
	String getName();
	void setName(String controlName);
	void setVisible(int visible);
	byte getVisible();
	void setLeft(int left);
	void setTop(int top);
	void setTag(String tag);
	void setEnabled(int enabled);
	int getEnabled();
	long getHeight();
	int getLeft();
	long getWidth();
	int getTop();
	void setWidth(long value);
	void setHeight(long value);
	String getTag();

	int getTitleForeColor();
	void setTitleForeColor(int value);

	int getTitleBackColor();
	void setTitleBackColor(int value);

	int getTitleFontBold();
	void setTitleFontBold(int value);

	int getTitleFontItalic();
	void setTitleFontItalic(int value);

	int getTitleFontUnderline();
	void setTitleFontUnderline(int value);

	int getTitleFontStrikethru();
	void setTitleFontStrikethru(int value);

	int getTitleFontSize();
	void setTitleFontSize(int value);

	String getTitleFontName();
	void setTitleFontName(String font);

	int getTitleBackStyle();

	void setTitleBackStyle(int value);

	String getTitle();
	void setTitle(String value);

	int getTitleFormat();
	void setTitleFormat(int titleformat);

        String getTooltip();
        void setTooltip(String tooltip);
}
