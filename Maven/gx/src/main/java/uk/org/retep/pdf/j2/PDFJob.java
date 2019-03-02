package uk.org.retep.pdf.j2;

import java.awt.*;
import java.io.*;
import java.util.*;

import uk.org.retep.pdf.PDFPage;

/**
 * This class extends awt's PrintJob, to provide a simple method of writing
 * PDF documents.
 *
 * <p>You can use this with any code that uses Java's printing mechanism. It
 * does include a few extra methods to provide access to some of PDF's features
 * like annotations, or outlines.
 *
 * <p>This class is specific to the JDK1.1.x platform.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public class PDFJob extends uk.org.retep.pdf.PDFJob implements Serializable
{
    public Graphics getGraphics(Rectangle media, int orient) {
	// create a new page
	page = new PDFPage(media, orient);
	pdf.add(page);
	pagenum++;

	// Now create a Graphics object to draw onto the page
	return new graphic(page,this);
    }

    /**
     * This returns a graphics object that can be used to draw on a page.
     * In PDF, this will be a new page within the document.
     *
     * @param Orientation of the new page, as defined in PDFPage
     * @return Graphics object to draw.
     */
    public Graphics getGraphics(int orient) {
	// create a new page
	page = new PDFPage(orient);
	pdf.add(page);
	pagenum++;

	// Now create a Graphics object to draw onto the page
	return new graphic(page,this);
    }

    /**
     * This inner class extends PDFGraphics for the PrintJob.
     *
     * Like with java.awt, Graphics instances created with PrintJob implement
     * the PrintGraphics interface. Here we implement that method, and overide
     * PDFGraphics.create() method, so all instances have this interface.
     */
    class graphic extends PDFGraphics implements PrintGraphics {
	/**
	 * The PDFJob we are linked with
	 */
	private PDFJob job;

	/**
	 * @param page to attach to
	 * @param job PDFJob containing this graphic
	 */
	public graphic(PDFPage page,PDFJob job) {
	    super();
	    init(page);
	    this.job = job;
	}

	/**
	 * This is used by our version of create()
	 */
	public graphic(PDFPage page,PDFJob job,PrintWriter pw) {
	    super();
	    init(page,pw);
	    this.job = job;
	}

	/**
	 * This returns a child instance of this Graphics object. As with AWT,
	 * the affects of using the parent instance while the child exists,
	 * is not determined.
	 *
	 * <p>Once complete, the child should be released with it's dispose()
	 * method which will restore the graphics state to it's parent.
	 *
	 * @return Graphics object
	 */
	public Graphics create() {
	    c();
	    graphic g = new graphic(getPage(),job,getWriter());

	    // The new instance inherits a few items
	    g.clipRectangle = new Rectangle(clipRectangle);

	    return (Graphics) g;
	}

	/**
	 * This is the PrintGraphics interface
	 * @return PrintJob for this object
	 */
	public PrintJob getPrintJob() {
	    return (PrintJob)job;
	}

    }

}

