package com.genexus.internet;

import java.io.*;

/**
 * Common superclass for a variety of encoders.
 * @author      Anders Kristensen
 */
public abstract class CharacterEncoder {
  /**
   * Encode the InputStream and write the encoded form to the OutputStream.
   * @throws IOException        if an I/O error has occurred.
   */
  public abstract void encodeBuffer(InputStream in,
                                    OutputStream out) throws IOException;

  /**
   * Encode the contents of the buffer and write the encoded form
   * to the OutputStream.
   * @throws IOException        if an I/O error has occurred.
   */
  public void encodeBuffer(byte[] buf, OutputStream out)
    throws IOException
  {
    ByteArrayInputStream bin = new ByteArrayInputStream(buf);
    encodeBuffer(bin, out);
  }

  /**
   * Encode the contents of the buffer and return it as a String.
   */
  public final String encodeBuffer(byte[] buf) {
    // The encoded form is approximately 33 % bigger than the unencoded form
    ByteArrayInputStream bin = new ByteArrayInputStream(buf);
    ByteArrayOutputStream bout = new ByteArrayOutputStream((int) (buf.length*1.4));
    try { encodeBuffer(bin, bout); } catch (IOException e) {}
    return new String(bout.toByteArray(), 0);
  }
}
