package com.genexus.webpanels;

public final class UIFactory
{
	public static int getColor(int r, int g, int b)
	{
		return (r * 256 * 256 ) + (g * 256) + b;
	}

	public static ILabel getLabel(GXWebPanel webPanel)
	{
		return new HTMLLabel(webPanel);	
	}

	public static IChoice getChoice(GXWebPanel webPanel)
	{
		return new HTMLChoice(webPanel);
	}

	public static IListbox getListbox(GXWebPanel webPanel)
	{
		return new HTMLListBox(webPanel);	
	}

	public static ICheckbox getCheckbox(GXWebPanel webPanel)
	{
		return new HTMLCheckbox(webPanel);
	}
}
