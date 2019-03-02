package uk.org.retep.pdf.j2;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/**
 * This class extends the main PDFGraphics class, and is compiled on the
 * Java 2 platform only as it is specific to that platform.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public class PDFGraphics extends uk.org.retep.pdf.PDFGraphics implements Serializable
{
    /**
     * This constructor must exist for the library to create new instances.
     */
    public PDFGraphics() {
	super();
    }
    
    /**
     * This method creates a new instance of the class. It's used internally by
     * the super class, so that different JDK versions can be used.
     *
     * @param page the page to attach to
     * @param pw the printwriter to attach to.
     */
    protected uk.org.retep.pdf.PDFGraphics createGraphic(uk.org.retep.pdf.PDFPage page,
							 PrintWriter pw)
    {
	PDFGraphics g = new PDFGraphics();
	g.init(page,pw);
	return g;
    }
    
    /**
     * Returns the Shape of the clipping region
     * As my JDK docs say, this may break with Java 2D.
     * @return Shape of the clipping region
     */
    public Shape getClip() {
	return null;
    }
    
    /**
     * Draws a string using a AttributedCharacterIterator.
     * <p>This is not supported yet, as I have no idea what an
     * AttributedCharacterIterator is.
     * <p>This method is new to the Java2 API.
     */
    public void drawString(java.text.AttributedCharacterIterator aci,int x,int y)
    {
    }
    
}

