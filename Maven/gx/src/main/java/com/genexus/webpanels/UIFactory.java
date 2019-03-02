// $Log: UIFactory.java,v $
// Revision 1.1  1999/07/30 20:50:42  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/07/30 20:50:42  gusbro
// GeneXus Java Olimar
//

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
