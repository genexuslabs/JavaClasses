// $Log: DummyDecoder.java,v $
// Revision 1.1  1999/06/30 15:20:20  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/06/30 15:20:20  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.*;

class DummyDecoder implements MimeDecoder
{
	public void decode(MailReader input, OutputStream out) throws IOException
	{
		String line;

		while ( (line = input.readLine()) != null)
		{
			out.write(line.getBytes(), 0, line.length());
			out.write(GXInternetConstants.CRLFByteArray);
		}
	}
}
