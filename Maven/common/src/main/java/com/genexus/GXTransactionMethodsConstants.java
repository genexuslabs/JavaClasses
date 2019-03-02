// $Log: GXTransactionMethodsConstants.java,v $
// Revision 1.1  2002/07/17 19:11:53  gusbro
// Pasaje de Interfaz a Clase para que marche con C#
//
// Revision 1.1.1.1  2001/10/30 14:05:28  gusbro
// GeneXus Java Olimar
//
package com.genexus;

public class GXTransactionMethodsConstants
{
	public static final byte FIRST   = GXTransactionMethods.FIRST;
	public static final byte BACK    = GXTransactionMethods.BACK;
	public static final byte NEXT    = GXTransactionMethods.NEXT;
	public static final byte LAST    = GXTransactionMethods.LAST;

	public static final byte ENTER   = GXTransactionMethods.ENTER;
	public static final byte DELETE  = GXTransactionMethods.DELETE;

	public static final byte SA_STARTUP = GXTransactionMethods.SA_STARTUP;
	public static final byte GETEQ = GXTransactionMethods.GETEQ;
	public static final byte GETEQ_NOMOD = GXTransactionMethods.GETEQ_NOMOD;
	public static final byte SA_NOMOD = GXTransactionMethods.SA_NOMOD;
	public static final byte PREVIOUS = GXTransactionMethods.PREVIOUS;
	public static final byte GET = GXTransactionMethods.GET;
	public static final byte SA_MOD = GXTransactionMethods.SA_MOD;

	public static final byte AFTER_LEVEL = GXTransactionMethods.AFTER_LEVEL;

	public static final byte CLEANUP = GXTransactionMethods.CLEANUP;
}