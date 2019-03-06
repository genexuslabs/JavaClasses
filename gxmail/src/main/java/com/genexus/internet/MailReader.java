// $Log: MailReader.java,v $
// Revision 1.1  1999/06/30 15:22:48  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/06/30 15:22:48  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.IOException;

interface MailReader
{
	int read() throws IOException;
	String readLine() throws IOException;
	String getSeparator();
	void setSeparator(String separator);
}