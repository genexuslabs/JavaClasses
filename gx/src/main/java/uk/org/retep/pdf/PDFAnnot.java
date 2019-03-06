// $Log: PDFAnnot.java,v $
// Revision 1.1  2000/02/08 11:55:06  gusbro
// Initial revision
//
// Revision 1.1.1.1  2000/02/08 11:55:06  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * This class defines an annotation (commonly known as Bookmarks).
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public class PDFAnnot extends PDFObject implements Serializable
{
    /**
     * Solid border. The border is drawn as a solid line.
     */
    public static final short SOLID = 0;
    
    /**
     * The border is drawn with a dashed line.
     */
    public static final short DASHED = 1;
    
    /**
     * The border is drawn in a beveled style (faux three-dimensional) such
     * that it looks as if it is pushed out of the page (opposite of INSET)
     */
    public static final short BEVELED = 2;
    
    /**
     * The border is drawn in an inset style (faux three-dimensional) such
     * that it looks as if it is inset into the page (opposite of BEVELED)
     */
    public static final short INSET = 3;
    
    /**
     * The border is drawn as a line on the bottom of the annotation rectangle
     */
    public static final short UNDERLINED = 4;
    
    /**
     * The subtype of the outline, ie text, note, etc
     */
    private String subtype;
    
    /**
     * The size of the annotation
     */
    private int l,b,r,t;
    
    /**
     * The text of a text annotation
     */
    private String s;
    
    /**
     * flag used to indicate that the destination should fit the screen
     */
    private static final int FULL_PAGE = -9999;
    
    /**
     * Link to the Destination page 
     */
    private PDFObject dest;
    
    /**
     * If fl!=FULL_PAGE then this is the region of the destination page shown.
     * Otherwise they are ignored.
     */
    private int fl,fb,fr,ft;
    
    /**
     * the border for this annotation
     */
    private border border;
    
    /**
     * This is used to create an annotation.
     * @param s Subtype for this annotation
     * @param l Left coordinate
     * @param b Bottom coordinate
     * @param r Right coordinate
     * @param t Top coordinate
     */
    protected PDFAnnot(String s,int l,int b,int r,int t) {
	super("/Annot");
	subtype = s;
	this.l = l;
	this.b = b;
	this.r = r;
	this.t = t;
    }
    
    /**
     * Creates a text annotation
     * @param l Left coordinate
     * @param b Bottom coordinate
     * @param r Right coordinate
     * @param t Top coordinate
     * @param s Text for this annotation
     */
    public PDFAnnot(int l,int b,int r,int t,String s) {
	this("/Text",l,b,r,t);
	this.s = s;
    }
    
    /**
     * Creates a link annotation
     * @param l Left coordinate
     * @param b Bottom coordinate
     * @param r Right coordinate
     * @param t Top coordinate
     * @param dest Destination for this link. The page will fit the display.
     */
    public PDFAnnot(int l,int b,int r,int t,PDFObject dest) {
	this("/Link",l,b,r,t);
	this.dest = dest;
	this.fl = FULL_PAGE; // this is used to indicate a full page
    }
    
    /**
     * Creates a link annotation
     * @param l Left coordinate
     * @param b Bottom coordinate
     * @param r Right coordinate
     * @param t Top coordinate
     * @param dest Destination for this link
     * @param rect Rectangle describing what part of the page to be displayed
     * (must be in User Coordinates)
     */
    public PDFAnnot(int l,int b,int r,int t,
		    PDFObject dest,
		    int fl,int fb,int fr,int ft
		    ) {
	this("/Link",l,b,r,t);
	this.dest = dest;
	this.fl = fl;
	this.fb = fb;
	this.fr = fr;
	this.ft = ft;
    }
    
    /**
     * Sets the border for the annotation. By default, no border is defined.
     *
     * <p>If the style is DASHED, then this method uses PDF's default dash
     * scheme {3}
     *
     * <p>Important: the annotation must have been added to the document before
     * this is used. If the annotation was created using the methods in
     * PDFPage, then the annotation is already in the document.
     *
     * @param style Border style SOLID, DASHED, BEVELED, INSET or UNDERLINED.
     * @param width Width of the border
     */
    public void setBorder(short style,double width) {
	border = new border(style,width);
	pdf.add(border);
    }
    
    /**
     * Sets the border for the annotation. Unlike the other method, this
     * produces a dashed border.
     *
     * <p>Important: the annotation must have been added to the document before
     * this is used. If the annotation was created using the methods in
     * PDFPage, then the annotation is already in the document.
     *
     * @param width Width of the border
     * @param dash Array of lengths, used for drawing the dashes. If this
     * is null, then the default of {3} is used.
     */
    public void setBorder(double width,double dash[]) {
	border = new border(width,dash);
	pdf.add(border);
    }
    
    /**
     * This class is used to handle borders
     */
    class border extends PDFObject {
	/**
	 * The style of the border
	 */
	private short style;
	
	/**
	 * The width of the border
	 */
	private double width;
	
	/**
	 * This array allows the definition of a dotted line for the border
	 */
	private double dash[];
	
	/**
	 * Creates a border using the predefined styles in PDFAnnot.
	 * <p>Note: Do not use PDFAnnot.DASHED with this method.
	 * Use the other constructor.
	 *
	 * @param style The style of the border
	 * @param width The width of the border
	 * @see PDFAnnot
	 */
	public border(short style,double width) {
	    super("/Border");
	    this.style = style;
	    this.width = width;
	}
	
	/**
	 * Creates a border of style PDFAnnot.DASHED
	 *
	 * @param width The width of the border
	 * @param dash The line pattern definition
	 */
	public border(double width,double dash[]) {
	    super("/Border");
	    this.style = PDFAnnot.DASHED;
	    this.width = width;
	    this.dash = dash;
	}
	
	/**
	 * @param os OutputStream to send the object to
	 * @exception IOException on error
	 */
	public void write(OutputStream os) throws IOException {
	    //writeStart(os);
	    os.write(Integer.toString(objser).getBytes());
	    os.write(" 0 obj\n".getBytes());
	    
	    os.write("[/S /".getBytes());
	    os.write("SDBIU".substring(style,style+1).getBytes());
	    os.write(" /W ".getBytes());
	    os.write(Double.toString(width).getBytes());
	    if(dash!=null) {
		os.write(" /D [".getBytes());
		os.write(Double.toString(dash[0]).getBytes());
		for(int i=1;i<dash.length;i++) {
		    os.write(" ".getBytes());
		    os.write(Double.toString(dash[i]).getBytes());
		}
		os.write("] ".getBytes());
	    }
	    os.write("]\n".getBytes());
	    
	    //writeEnd(os);
	    os.write("endobj\n".getBytes());
	}
    }
    
    /**
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public void write(OutputStream os) throws IOException {
	// Write the object header
	writeStart(os);
	
	// now the objects body
	os.write("/Subtype ".getBytes());
	os.write(subtype.getBytes());
	os.write("\n/Rect [".getBytes());
	os.write(Integer.toString(l).getBytes());
	os.write(" ".getBytes());
	os.write(Integer.toString(b).getBytes());
	os.write(" ".getBytes());
	os.write(Integer.toString(r).getBytes());
	os.write(" ".getBytes());
	os.write(Integer.toString(t).getBytes());
	os.write("]\n".getBytes());
	
	// handle the border
	if(border==null) {
	    os.write("/Border [0 0 0]\n".getBytes());
	    //if(pdf.defaultOutlineBorder==null)
	    //pdf.add(pdf.defaultOutlineBorder = new border(SOLID,0.0));
	    //os.write(pdf.defaultOutlineBorder.toString().getBytes());
	} else {
	    os.write("/BS ".getBytes());
	    os.write(border.toString().getBytes());
	    os.write("\n".getBytes());
	}
	
	// Now the annotation subtypes
	if(subtype.equals("/Text")) {
	    os.write("/Contents ".getBytes());
	    os.write(PDF.toString(s).getBytes());
	    os.write("\n".getBytes());
	} else if(subtype.equals("/Link")) {
	    os.write("/Dest [".getBytes());
	    os.write(dest.toString().getBytes());
	    if(fl==FULL_PAGE)
		os.write(" /Fit]".getBytes());
	    else {
		os.write(" /FitR ".getBytes());
		os.write(Integer.toString(fl).getBytes());
		os.write(" ".getBytes());
		os.write(Integer.toString(fb).getBytes());
		os.write(" ".getBytes());
		os.write(Integer.toString(fr).getBytes());
		os.write(" ".getBytes());
		os.write(Integer.toString(ft).getBytes());
		os.write("]".getBytes());
	    }
	    os.write("\n".getBytes());
	}
	
	// finish off with its footer
	writeEnd(os);
    }
}
