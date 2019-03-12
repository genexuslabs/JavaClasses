
package uk.org.retep.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.zip.DeflaterOutputStream;

/**
 * This class implements a PDF stream object. In PDF, streams contain data
 * like the graphic operators that render a page, or the pixels of an image.
 *
 * <p>In PDF, a stream can be compressed using several different methods, or
 * left uncompressed. Here we support both uncompressed, and FlateDecode as
 * it's supported by the java core.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public class PDFStream extends PDFObject implements Serializable
{
    /**
     * This holds the stream's content.
     */
    transient ByteArrayOutputStream buf;

    /**
     * True if we will compress the stream in the pdf file
     */
    boolean deflate, asciiDeflate;

    /**
     * Constructs a plain stream.
     * <p>By default, the stream will be compressed.
     */
    public PDFStream() {
	this(null);
    }

    /**
     * Constructs a stream. The supplied type is stored in the stream's header
     * and is used by other objects that extend the PDFStream class (like
     * PDFImage).
     * <p>By default, the stream will be compressed.
     * @param type type for the stream
     * @see PDFImage
     */
    public PDFStream(String type) {
	super(type);
	buf = new ByteArrayOutputStream();

	// default deflate mode
	deflate = true;
    deflate = Const.DEFLATE;
    }

    /**
     * @param mode true will FlatDecode the stream
     */
    public void setDeflate(boolean mode) {
	deflate = mode;
    }

    public void setAsciiDeflate(boolean asciiDeflate)
    {
      this.asciiDeflate = asciiDeflate;
    }

    /**
     * Returs true if the stream will be compressed.
     * @return true if compression is enabled
     */
    public boolean getDeflate() {
	return deflate;
    }

    /**
     * Returns the OutputStream that will append to this stream.
     * @return The stream for this object
     */
    public OutputStream getOutputStream() {
	return (OutputStream)buf;
    }

    /**
     * Creates a PrintWriter that will append to this stream.
     * @return a PrintWriter to write to the stream
     */
    public PrintWriter getWriter() {
	return new PrintWriter(buf,true);
    }

    /**
     * This is for extenders, and provides access to the stream.
     * @return ByteArrayOutputStream containing the contents.
     */
    public ByteArrayOutputStream getStream() {
	return buf;
    }

    /**
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public void write(OutputStream os) throws IOException {
	writeStart(os);
	writeStream(os);
	// Unlike most PDF objects, we dont call writeEnd(os) because we
	// contain a stream
    }

    /**
     * This inserts the Streams length, then the actual stream, finally
     * the end of stream/object markers.
     *
     * <p>This is intended for anyone extending PDFStream, as objects
     * containing streams do no use writeEnd(), and they must be able
     * to write the actual stream.
     *
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public void writeStream(OutputStream os) throws IOException {
	if(deflate) {
          if(asciiDeflate)
          {
	    // AsciiDecode
            byte b[] = buf.toByteArray();
	    os.write("\r\n/Filter /ASCIIHexDecode\r\n".getBytes());
	    os.write("/Length ".getBytes());
	    os.write(Integer.toString(b.length * 2).getBytes());
	    os.write("\r\n>>\r\nstream\r\n".getBytes());
            PrintStream out = new PrintStream(os);
            for(int i = 0; i < b.length; i ++)
            {
              String hexValue = "00" + Integer.toHexString(b[i]);
              hexValue = hexValue.substring(hexValue.length() - 2);
              out.print(hexValue);
            }
	    os.write("\r\n".getBytes());
          }
          else
          {
	    ByteArrayOutputStream b = new ByteArrayOutputStream();
	    DeflaterOutputStream dos = new DeflaterOutputStream(b);
	    //,new Deflater(Deflater.BEST_COMPRESSION,true));
	    buf.writeTo(dos);
	    dos.finish();
	    dos.close();

	    // FlatDecode is compatible with the java.util.zip.Deflater class
	    os.write("/Filter /FlateDecode\r\n".getBytes());
	    os.write("/Length ".getBytes());
	    os.write(Integer.toString(b.size()+1).getBytes());
	    os.write("\r\n>>\r\nstream\r\n".getBytes());
	    b.writeTo(os);
	    os.write("\r\n".getBytes());
	}} else {
	    // This is a non-deflated stream
	    os.write("/Length ".getBytes());
	    os.write(Integer.toString(buf.size()).getBytes());
	    os.write("\r\n>>\r\nstream\r\n".getBytes());
	    buf.writeTo(os);
	}

	os.write("endstream\r\nendobj\r\n".getBytes());

	// Unlike most PDF objects, we dont call writeEnd(os) because we
	// contain a stream
    }

    /**
     * This implements our own special Serialization for this object.
     *
     * <p>Here we write the length of the stream's contents, then a byte
     * array of the contents. We have to do this, as ByteArrayOutputStream
     * is not serializable (hence the transient tag).
     *
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
	out.writeInt(buf.size());
	out.write(buf.toByteArray());
    }

    /**
     * This implements our own special Serialization for this object
     *
     * <p>Here we read the length of the stream's contents, then a byte
     * array of the contents. Then we recreate a new ByteArrayOutputStream.
     * We have to do this, as ByteArrayOutputStream is not serializable
     * (hence the transient tag).
     *
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException
    {
	int l = in.readInt();
	byte b[] = new byte[l];
	in.read(b,0,l);
	buf=new ByteArrayOutputStream(l);
	buf.write(b);
    }

}
