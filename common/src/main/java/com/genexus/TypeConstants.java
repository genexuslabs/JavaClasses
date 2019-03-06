// $Log: TypeConstants.java,v $
// Revision 1.4  2004/05/19 19:17:06  gusbro
// - Agrego constantes para collections y arrays
//
// Revision 1.3  2003/03/12 15:35:13  gusbro
// - Cambios para J# (la interfaz pasa a ser una clase)
//
// Revision 1.2  2002/12/04 21:43:41  aaguiar
// - Se bancan BLOBs
//
// Revision 1.1.1.1  2002/03/26 17:18:28  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2002/03/26 17:18:28  gusbro
// GeneXus Java Olimar
//
package com.genexus;

public class TypeConstants
{
	public static final int BYTE 	= 1;
	public static final int SHORT 	= 2;
	public static final int INT  	= 3;
	public static final int LONG 	= 4;
	public static final int FLOAT  = 5;
	public static final int DOUBLE = 6;
	public static final int STRING = 7;
	public static final int DATE	= 8;
	public static final int DECIMAL= 9;
	public static final int BOOLEAN= 10;
	public static final int BLOB = 11;
	public static final int OBJECT_COLLECTION = 12;
	public static final int UUID = 13;
	public static final int ARRAY = 1 << 15;
}