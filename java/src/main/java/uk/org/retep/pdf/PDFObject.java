// $Log: PDFObject.java,v $
// Revision 1.1  2000/02/08 11:55:08  gusbro
// Initial revision
//
// Revision 1.1.1.1  2000/02/08 11:55:08  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This is the base class for all Objects that form the PDF document.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public abstract class PDFObject implements Serializable
{
    /**
     * This is the object's PDF Type
     */
    private String type;
    
    /**
     * This is the unique serial number for this object.
     */
    protected int objser;
    
    /**
     * This allows any PDF object to refer to the document being constructed.
     */
    protected PDF pdf;
    
    /**
     * This is the null constructor.
     */
    //public PDFObject()
    //{
    //type = null;
    //}
    
    /**
     * This is usually called by extensors to this class, and sets the
     * PDF Object Type
     */
    public PDFObject(String type)
    {
	this.type = type;
    }
    
    /**
     * Returns the PDF Type of this object
     * @return The PDF Type of this object
     */
    public String getType()
    {
	return type;
    }
    
    /**
     * Returns the unique serial number of this object.
     * @return Unique serial number of this object.
     */
    public final int getSerialID()
    {
	return objser;
    }
    
    /**
     * Returns the PDF document this object belongs to.
     * @return PDF containing this object
     */
    public final PDF getPDF()
    {
	return pdf;
    }
    
    /**
     * Writes the object to the output stream.
     * This method must be overidden.
     *
     * <p><b>Note:</b> It should not write any other objects, even if they are
     * it's Kids, as they will be written by the calling routine.
     *
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public abstract void write(OutputStream os) throws IOException;
    
    /**
     * The write method should call this before writing anything to the
     * OutputStream. This will send the standard header for each object.
     *
     * <p>Note: There are a few rare cases where this method is not called.
     *
     * @param os OutputStream to write to
     * @exception IOException on error
     */
    public final void writeStart(OutputStream os) throws IOException
    {
	os.write(Integer.toString(objser).getBytes());
	os.write(" 0 obj\n<<\n".getBytes());
	if(type!=null) {
	    os.write("/Type ".getBytes());
	    os.write(type.getBytes());
	    os.write("\n".getBytes());
	}
    }
    
    /**
     * The write method should call this after writing anything to the
     * OutputStream. This will send the standard footer for each object.
     *
     * <p>Note: There are a few rare cases where this method is not called.
     *
     * @param os OutputStream to write to
     * @exception IOException on error
     */
    public final void writeEnd(OutputStream os) throws IOException
    {
	os.write(">>\nendobj\n".getBytes());
    }
    
    /**
     * Returns the unique serial number in PDF format
     * @return the serial number in PDF format
     */
    public String toString()
    {
	return ""+objser+" 0 R";
    }
    
    /**
     * This utility method returns a String containing an array definition
     * based on a Vector containing PDFObjects
     * @param v Vector containing PDFObjects
     * @return String containing a PDF array
     */
    public static String toArray(Vector v)
    {
	if(v.size()==0)
	    return "";
	
	StringBuffer b = new StringBuffer();
	String bs = "[";
	for(Enumeration en = v.elements(); en.hasMoreElements(); ) {
	    b.append(bs);
	    b.append(en.nextElement().toString());
	    bs = " ";
	}
	b.append("]");
	return b.toString();
    }
}
