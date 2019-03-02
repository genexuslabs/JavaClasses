package com.genexus.util;

import java.io.*;

public class FastByteArrayOutputStream extends ByteArrayOutputStream
{
	public void writeToOutputStream(OutputStream o) throws IOException
	{
		o.write(buf, 0, count);
	}
}