package com.genexus.internet;

import java.io.IOException;

interface MailReader
{
	int read() throws IOException;
	String readLine() throws IOException;
	String getSeparator();
	void setSeparator(String separator);
}