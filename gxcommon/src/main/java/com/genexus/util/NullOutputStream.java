package com.genexus.util;

import java.io.*;

/**
 * Acts as a sink for bytes that should disappear.
 */
public class NullOutputStream extends OutputStream
{
   	public void write (int b) {}
   	public void write(byte [] p) { ; }
    public void write(byte [] p, int i, int s) { ; }
}

