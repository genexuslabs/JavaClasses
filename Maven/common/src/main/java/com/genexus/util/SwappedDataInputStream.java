/*
 * SwappedDataInputStream.java
 */
package com.genexus.util;

import java.io.*;

/**
 * Provide byte-order-reversed input of bytes, int's, and short's. Allows for
 * proper reading of those numeric values from a "little-endian" source.
 */

public class SwappedDataInputStream extends InputStream
{
    InputStream is;
    long pos = 0;

    public SwappedDataInputStream(InputStream is)
    {
        this.is = is;
    }

    public long curPos()
    {
        return pos;
    }

    public int read()
    throws
        IOException
    {
        int n = is.read();
        if (n < 0) return -1;
        pos++;
        return n;
    }

    public int read(byte[] b)
    throws
        IOException
    {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int offset, int len)
    throws
        IOException
    {
        int n = is.read(b, offset, len);
        if (n < 0) return -1;
        pos += n;
        return n;
    }

    public short readShort()
    throws
        IOException
    {
        byte[] b = {0, 0};
        short mask = 0xff;
        short s = 0;

        int n = read(b, 0, 2);
        if (n < 2) throw new IOException();

        // Swap the bytes
        s |= ((short)(b[1])) << 8;
        s |= (((short)(b[0])) & mask);

        return s;
    }

    public int readInt()
    throws
        IOException
    {
        byte[] b = {0, 0, 0, 0};
        short mask = 0xff;
        int i = 0;

        int n = read(b, 0, 4);
        if (n < 4) throw new IOException();

        // Swap the bytes
        i |= ((((short)(b[3])) & mask) << 24);
        i |= ((((short)(b[2])) & mask) << 16);
        i |= ((((short)(b[1])) & mask) << 8);
        i |= (((short)(b[0])) & mask);

        return i;
    }

    // We don't use these, but they must be overridden to complete the class.
    // The reason is that this class is merely a wrapper for the InputStream
    // object that it uses.

    public int available()
    throws
        IOException
    {
        return is.available();
    }

    public void close()
    throws
        IOException
    {
        is.close();
    }

    public void mark(int readlimit)
    {
        is.mark(readlimit);
    }

    public boolean markSupported()
    {
        return is.markSupported();
    }

    public void reset()
    throws
        IOException
    {
        is.reset();
        pos = 0;
    }

    public long skip(long n)
    throws
        IOException
    {
        long m = is.skip(n);
        pos += m;
        return m;
    }
}

