// $Log: PDFJob.java,v $
// Revision 1.1  2001/03/13 15:05:16  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/03/13 15:05:16  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * This class extends awt's PrintJob, to provide a simple method of writing
 * PDF documents.
 *
 * <p>You can use this with any code that uses Java's printing mechanism. It
 * does include a few extra methods to provide access to some of PDF's features
 * like annotations, or outlines.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public abstract class PDFJob extends PrintJob implements Serializable
{
    /**
     * This is the OutputStream the PDF file will be written to when complete
     * Note: This is transient, as it's not valid after being Serialized.
     */
    protected transient OutputStream os;

    /**
     * This is the PDF file being constructed
     */
    protected PDF pdf;

    /**
     * This is the current page being constructed by the last getGraphics()
     * call
     */
    protected PDFPage page;

    /**
     * This is the page number of the current page
     */
    protected int pagenum;

    /**
     * This constructs the job. This method must be used when creating a
     * template pdf file, ie one that is Serialised by one application, and
     * then restored by another.
     */
    public PDFJob() {
	init(null);
    }

    /**
     * This constructs the job.
     * @param os OutputStream to send the PDF file when completed.
     */
    public PDFJob(OutputStream os) {
	init(os);
    }

    /**
     * Internal method to actually construct the job.
     * @param os OutputStream to send the PDF file when completed.
     */
    protected void init(OutputStream os) {
	this.os = os;
	this.pdf = new PDF();
	pagenum = 0;
    }

    /**
     * This writes the PDF document to the OutputStream, finishing the
     * document.
     */
    public void end() {
	try {
	    pdf.write(os);
	} catch(IOException ioe) {
	    // Ideally we should throw this. However, PrintJob doesn't throw
	    // anything, so we will print the Stack Trace instead.
	    ioe.printStackTrace();
	}

	// This should mark us as dead
	os = null;
	pdf = null;
    }

    /**
     * This returns a graphics object that can be used to draw on a page.
     * In PDF, this will be a new page within the document.
     * @return Graphics object to draw.
     */
    public Graphics getGraphics() {
	return getGraphics(PDFPage.PORTRAIT);
    }

    /**
     * This returns a graphics object that can be used to draw on a page.
     * In PDF, this will be a new page within the document.
     *
     * @param orient The page orientation as defined by PDFPage
     * @return Graphics object to draw.
     * @see PDFPage#PORTRAIT
     * @see PDFPage#LANDSCAPE
     * @see PDFPage#INVERTEDPORTRAIT
     * @see PDFPage#SEASCAPE
     */
    public abstract Graphics getGraphics(int orient);

    public abstract Graphics getGraphics(Rectangle media, int orient);

    /**
     * Returns the pages dimensions in pixels.
     * @return Dimension of the page
     */
    public Dimension getPageDimension() {
	Rectangle r = page.getMedia();

	// if were landscape or seascape, then we swap the dimensions which
	// should fool existing code.
	int rot = page.getOrientation();
	if(rot==90 || rot==270)
	    return new Dimension(r.height-r.y,r.width-r.x);

	return new Dimension(r.width-r.x,r.height-r.y);
    }

    /**
     * This returns the page resolution.
     *
     * <p>This is the PDF (and Postscript) device resolution of 72 dpi
     * (equivalent to 1 point).
     *
     * @return Page resolution in pixels per inch
     */
    public int getPageResolution() {
	return 72;
    }

    /**
     * In AWT's PrintJob, this would return true if the user requested that the
     * file is printed in reverse order. For PDF's this is not applicable, so
     * it will always return false.
     * @return false
     */
    public boolean lastPageFirst() {
	return false;
    }

    //======== END OF PrintJob extension ==========

    /**
     * Returns the PDF object for this document. Useful for gaining access to
     * the internals of PDF.
     * @return the PDF object
     */
    public PDF getPDF() {
	return pdf;
    }

    /**
     * Returns the current PDFPage being worked on. Useful for working on
     * Annotations (like links), etc.
     * @return the current PDFPage being constructed
     */
    public PDFPage getCurrentPage() {
	return page;
    }

    /**
     * Returns the current page number.
     * Useful if you need to include one in the document
     * @return the current page number
     */
    public int getCurrentPageNumber() {
	return pagenum;
    }

    /**
     * This method attaches an outline to the current page being generated.
     * When selected, the outline displays the top of the page.
     * @param title Outline title to attach
     * @return PDFOutline object created, for adding sub-outline's if required.
     */
    public PDFOutline addOutline(String title) {
	return page.addOutline(title);
    }

    /**
     * This method attaches an outline to the current page being generated.
     * When selected, the outline displays the specified region.
     * @param title Outline title to attach
     * @param x Left coordinate of region
     * @param y Top coordinate of region
     * @param w width of region
     * @param h height of region
     * @return PDFOutline object created, for adding sub-outline's if required.
     */
    public PDFOutline addOutline(String title,int x,int y,int w,int h) {
	return page.addOutline(title,x,y,w,h);
    }

    /**
     * Convenience method: Adds a text note to the document.
     * @param note Text of the note
     * @param x Coordinate of note
     * @param y Coordinate of note
     * @param w Width of the note
     * @param h Height of the note
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addNote(String note,int x,int y,int w,int h) {
	return page.addNote(note,x,y,w,h);
    }

  /** Obtiene un Vector con los PDFFonts de este documento
   *  @return Vector con los PDFFonts de este documento
   */
    public Vector getFonts()
    {
        return pdf.getFonts();
    }

}
