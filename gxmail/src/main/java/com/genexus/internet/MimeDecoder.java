package com.genexus.internet;

import java.io.IOException;
import java.io.OutputStream;

interface MimeDecoder
{
	public void decode(MailReader in, OutputStream out) throws IOException;
}
