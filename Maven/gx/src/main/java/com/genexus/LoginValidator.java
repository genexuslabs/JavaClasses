// $Log: LoginValidator.java,v $
// Revision 1.1  1999/10/12 18:39:14  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/10/12 18:39:14  gusbro
// GeneXus Java Olimar
//
package com.genexus;

public interface LoginValidator
{
	boolean login(java.util.Properties props);
	String getMessage();
}