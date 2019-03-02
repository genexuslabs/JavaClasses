package com.genexus.util;

import java.io.*;

public class FixedBufferedInputStream extends BufferedInputStream 
{
    public FixedBufferedInputStream(InputStream in) 
    {
		super(in);
    }

    public FixedBufferedInputStream(InputStream in, int size) 
    {
		super(in, size);
    }

	public synchronized int read(byte[] buffer, int bufPos, int length) throws IOException 
	{
		int i = super.read(buffer, bufPos, length);

		if  ((i == length) || (i == -1)) 
			return i;

		int j = super.read(buffer, bufPos + i, length - i);

		if (j == -1) 
			return i;

		return j + i;
	}
}
