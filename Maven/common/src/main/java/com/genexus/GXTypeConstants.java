// $Log: GXTypeConstants.java,v $
// Revision 1.1  2001/04/27 19:34:42  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/04/27 19:34:42  gusbro
// GeneXus Java Olimar
//

package com.genexus;

public final class GXTypeConstants
{
	public static int NUMERIC		= 1;
	public static int DATE 			= 2;
	public static int DATETIME		= 3;
	public static int CHAR 			= 4;
	public static int VARCHAR 		= 5;
	public static int LONGVARCHAR 	= 6;

	public static boolean isCharacter(int type)
	{
		return (type == CHAR || type == VARCHAR || type == LONGVARCHAR);
	}

	public static boolean isNumeric (int type)
	{
		return (type == NUMERIC);
	}

	public static boolean isJavaDate(int type)
	{
		return (type == DATE || type == DATETIME);
	}

}
