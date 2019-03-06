// $Log: MimeDecoder.java,v $
// Revision 1.1  1999/06/30 15:20:08  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/06/30 15:20:08  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.*;

interface MimeDecoder
{
	public void decode(MailReader in, OutputStream out) throws IOException;
}
