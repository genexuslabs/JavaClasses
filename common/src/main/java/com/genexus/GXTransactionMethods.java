package com.genexus;

public interface GXTransactionMethods
{
	byte FIRST   = 1;
	byte BACK    = 2;
	byte NEXT    = 3;
	byte LAST    = 4;

	byte ENTER   = 5;
	byte DELETE  = 6;

	byte SA_STARTUP = 7;
	byte GETEQ = 8;
	byte GETEQ_NOMOD = 9;
	byte SA_NOMOD = 10;
	byte PREVIOUS = 11;
	byte GET = 12;
	byte SA_MOD = 13;

	byte AFTER_LEVEL = 20;

	byte CLEANUP = 30;

}