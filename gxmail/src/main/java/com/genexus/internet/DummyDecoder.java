
package com.genexus.internet;

import java.io.IOException;
import java.io.OutputStream;

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
