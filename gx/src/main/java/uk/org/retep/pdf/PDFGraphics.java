// $Log: PDFGraphics.java,v $
// Revision 1.2  2004/09/27 16:46:32  iroqueta
// Ciertos caracteres japoneses no se mostraban en forma correcta...
// A esos caracteres se le agrega un "\" luego del mismo para que se vean en forma correcta.
//
// Revision 1.1  2002/06/24 20:39:36  gusbro
// Initial revision
//
// Revision 1.2  2002/06/24 20:39:36  gusbro
// *** empty log message ***
//
// Revision 1.1.1.1  2001/11/06 20:09:48  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/**
 * This class is our implementation of AWT's Graphics class. It provides a
 * Java standard way of rendering into a PDF Document's Page.
 *
 * Note: This class is abstract, because if it wasn't, this would fail when
 * compiled under Java2. To allow this to compile under JDK1.1.x or Java2,
 * we have two subclasses. The JDK1.1.x class is empty, the other has the
 * additional methods to bring it in line.
 *
 * It consists of four parts:<ol>
 * <li>Constructor handling initialisation, and parenting of child instances
 * <li>Graphics implementation, which translates between Java and PDF
 * <li>Extensions containing useful PDF operators
 * <li>Optimizer which handles reducing the generated output size and the
 *     final rendering speed.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 * @see uk.org.retep.pdf.j1.PDFGraphics
 * @see uk.org.retep.pdf.j2.PDFGraphics
 */
public abstract class PDFGraphics extends Graphics implements Serializable
{
    public static double SCALE_FACTOR = 0.7555; // Scaling de los bitmaps (Java  <--> PDF)
    public static double LINE_WIDTH_SCALE = 0.69; // Scaling de los lineWidths

    // Implementation notes:
    //
    // Pages 333-335 of the PDF Reference Manual
    //
    // Unless absolutely required, use the moveto, lineto and rectangle
    // operators to perform those actions. They contain some extra optimisations
    // which will reduce the output size by up to half in some cases.
    //
    // About fill operators: For correct operation, any fill operation should
    // start with c(), which will ensure any previous path is completed,
    // otherwise you may find the fill will include previous items

    /**
     * This is the media we are working with
     */
    protected Rectangle media;

    /**
     * The media's rotation, either 0,90,180 or 270.
     */
     private int mediaRot;

     /**
      * This is used to translate coordinates
      */
    protected int trax;

     /**
      * This is used to translate coordinates
      */
    protected int tray;

    /**
     * Part of the optimiser:
     * This is written to the stream when the np() is called. np then clears
     * this value.
     */
    private String pre_np;

    /**
     * Part of the optimiser:
     * When true, we are drawing a path.
     * @see #np
     */
    private boolean inStroke;

    /**
     * Part of the optimiser:
     * The last known moveto/lineto x coordinate
     * @see #moveto
     * @see #lineto
     */
    private int lx;		// last known moveto/lineto coords

    /**
     * Part of the optimiser:
     * The last known moveto/lineto y coordinate
     * @see #moveto
     * @see #lineto
     */
    private int ly;		// last known moveto/lineto coords

    /**
     * Part of the optimiser:
     * When true, we are within a Text Block.
     * @see #nt
     */
    private boolean inText;	// true if within a Text Block - see nt()

    /**
     * Part of the optimiser:
     * When true, the font has changed.
     * @see #nt
     */
    private boolean newFont;	// true if the font changes - see nt()

    /**
     * Part of the optimiser:
     * The last x coordinate when rendering text
     */
    private int tx;		// the last coordinate for text rendering

    /**
     * Part of the optimiser:
     * The last y coordinate when rendering text
     */
    private int ty;		// the last coordinate for text rendering

    /**
     * This is the current pen/fill color
     */
    private Color color;

    /**
     * This is the current font (in PDF format)
     */
    private PDFFont pdffont = null;

    /**
     * This is the current font (in Java format)
     */
    private Font    font;

    /**
     * This is the PrintWriter used to write PDF drawing commands to the Stream
     */
    private PrintWriter pw;

    /**
     * This is a reference to the PDFPage we are rendering to.
     */
    private PDFPage page;

    /**
     * This is true for any Graphics instance that didn't create the stream.
     * @see #create
     */
    private boolean child;

    /**
     * Do not use, must try to convert this to protected.
     * It is currently required by PDFPage to create new instances.
     */
    public PDFGraphics() {
    }

    /**
     * This is called by PDFPage when creating a Graphcis instance.
     * @param page The PDFPage to draw onto.
     */
    protected void init(PDFPage page) {
	this.page = page;

	// We are the parent instance
	child = false;

	// Now create a stream to store the graphics in
	PDFStream stream = new PDFStream();
	page.getPDF().add(stream);
	page.add(stream);
	pw = stream.getWriter();

	// initially, we are limited to the page size
	clipRectangle = new Rectangle(page.getMedia());

	// finally initialise the stream
	init();
    }

    /**
     * This method is used internally by create() and by the PDFJob class
     * @param page PDFPage to draw into
     * @param pw PrintWriter to use
     */
    protected void init(PDFPage page,PrintWriter pw) {
	this.page = page;
	this.pw   = pw;

	// In this case, we didn't create the stream (our parent did)
	// so child is true (see dispose)
	child = true;

	// finally initialise the stream
	init();
    }

    /**
     * This initialises the stream by saving the current graphics state, and
     * setting up the default line width (for us).
     *
     * It also sets up the instance ready for graphic operations and any
     * optimisations.
     *
     * <p>For child instances, the stream is already open, so this should keep
     * things happy.
     */
    private void init() {
	// save graphics state (restored by dispose)
	if(child)
	    pw.print("q ");

	// Set the line width
	setDefaultLineWidth();

	// now initialise the instance
	//setColor(Color.black);
	color = Color.black;
	// possible: if parent.color is not black, then force black?
	// must check to see what AWT does?

	// get the page dimensions (needed to get the orientation correct)
	media = page.getMedia();
	mediaRot = page.getOrientation();

	// Finally set the page Orientation
	if(!child)
	    setOrientation();
    }

    /**
     * Returns the PrintWriter handling the underlying stream
     * @return the PrintWriter handling the underlying stream
     */
    public PrintWriter getWriter() {
	return pw;
    }

    /**
     * Returns the associated PDFPage for this graphic
     * @return the associated PDFPage for this graphic
     */
    public PDFPage getPage() {
	return page;
    }

    /**
     * This returns a child instance of this Graphics object. As with AWT, the
     * affects of using the parent instance while the child exists, is not
     * determined.
     *
     * <p>Once complete, the child should be released with it's dispose()
     * method which will restore the graphics state to it's parent.
     *
     * @return Graphics object to render onto the page
     */
    public Graphics create() {
	c();

	PDFGraphics g = createGraphic(page,pw);

	// The new instance inherits a few items
	g.media = new Rectangle(media);
	g.trax = trax;
	g.tray = tray;
	g.clipRectangle = new Rectangle(clipRectangle);

	return (Graphics) g;
    }

    /**
     * This method creates a new instance of the class. It's used internally by
     * the super class, so that different JDK versions can be used.
     *
     * This method must be overidden.
     * @param page the page to attach to
     * @param pw the printwriter to attach to.
     */
    protected abstract PDFGraphics createGraphic(PDFPage page,PrintWriter pw);

    /**
     * This releases any resources used by this Graphics object. You must use
     * this method once finished with it. Leaving it open will leave the PDF
     * stream in an inconsistent state, and will produce errors.
     *
     * <p>If this was created with Graphics.create() then the parent instance
     * can be used again. If not, then this closes the graphics operations for
     * this page when used with PDFJob.
     *
     * <p>When using PDFPage, you can create another fresh Graohcs instance,
     * which will draw over this one.
     *
     */
    public void dispose() {
	c();
	//System.err.println("PDFGraphics.dispose() child="+child+" graphics="+this);
	if(child)
	    pw.println("Q");	// restore graphics context
	else
	    pw.close();	// close the stream if were the parent
    }

    // *********************************************
    // **** Impelmentation of java.awt.Graphics ****
    // *********************************************

    //============ Rectangle operations =======================

    /**
     * This simply draws a White Rectangle to clear the area
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     */
    public void clearRect(int x,int y,int w,int h) {
	c();
	pw.print("q 1 1 1 RG ");// save state, set colour to White
	drawRect(x,y,w,h);
	c("B Q");		// close fill & stroke, then restore state
    }

    /**
     * We overide Graphics.drawRect as it doesn't join the 4 lines.
     * Also, PDF provides us with a Rectangle operator, so we will use that.
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     */
    public void drawRect(int x,int y,int w,int h) {
	np();
	pw.print(cxy(x,y)+cwh(w,h)+"re "); // rectangle

	lx=x; // I don't know if this is correct, but lets see if PDF ends
	ly=y; // the rectangle at it's start.
	// stroke (optimised)
    }

    /**
     * Fills a rectangle with the current colour
     *
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     */
    public void fillRect(int x,int y,int w,int h) {
	// end any path & stroke. This ensures the fill is on this
	// rectangle, and not on any previous graphics
	c();
	drawRect(x,y,w,h);
	c("B");	// rectangle, fill stroke
    }

    //============ Round Rectangle operations =======================

    /**
     * This is not yet implemented
     *
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     * @param aw a-width
     * @param ah a-height
     */
    public void fillRoundRect(int x,int y,int w,int h,int aw,int ah) {
    }

    /**
     * This is not yet implemented
     *
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     * @param aw a-width
     * @param ah a-height
     */
    public void drawRoundRect(int x,int y,int w,int h,int aw,int ah) {
    }

    //============ Oval operations =======================

    /**
     * This is not yet implemented
     *
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     */
    public void drawOval(int x,int y,int w,int h) {
    }

    /**
     * This is not yet implemented
     *
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     */
    public void fillOval(int x,int y,int w,int h) {
    }

    //============ Polygon operations =======================

    /**
     * Draws a polygon, linking the first and last coordinates.
     * @param xp Array of x coordinates
     * @param yp Array of y coordinates
     * @param np number of points in polygon
     */
    public void drawPolygon(int[] xp,int[] yp,int np) {
	polygon(xp,yp,np);
	c("s"); // closepath and stroke
    }

    /**
     * Draws a polyline. The first and last coordinates are not linked.
     * @param xp Array of x coordinates
     * @param yp Array of y coordinates
     * @param np number of points in polyline
     */
    public void drawPolyline(int[] xp,int[] yp,int np) {
	polygon(xp,yp,np);
	// no stroke, as we keep the optimiser in stroke state
    }

    /**
     * Fills a polygon.
     * @param xp Array of x coordinates
     * @param yp Array of y coordinates
     * @param np number of points in polygon
     */
    public void fillPolygon(int[] xp,int[] yp,int np) {
	c();	// finish off any previous paths
	polygon(xp,yp,np);
	c("b");	// closepath, fill and stroke
    }

    //============ Image operations =======================
    /**
     * Draw's an image onto the page.
     * <p>Not yet implemented.
     *
     * @param imageName Nombre de la imagen (para optimizar el PDF)
     * @param img The java.awt.Image
     * @param x coordinate on page
     * @param y coordinate on page
     * @param w Width on page
     * @param h height on page
     * @param obs ImageObserver
     * @return true if drawn
     */
    public boolean drawImage(String imageName, Image img,int x,int y,int w,int h,ImageObserver obs)
    {
        pw.println();
        if(Const.DEBUG_PDF)pw.println("% DrawImage: " + imageName + " [" + x + "," + y + " -> " + x + w + "," + y + h + "]");
        c(); // Cierro cualquier path abierto

        PDFImage stream = page.getImage(imageName, img, obs);
        pw.println("q");  // Guardo el estado grafico
        pw.println("1 0 0 1 " + cxy(x,y + h) + " cm"); // Hago un Translate a la posici�n
        pw.println((SCALE_FACTOR * (float)w) + " 0 0 " + (SCALE_FACTOR * (float)h) + " 0 0 cm");  // Hago un Scale al tama�o
        pw.println(stream.getName() + " Do");  // Inserto la imagen
        pw.println("Q");  // Restauro el estado grafico
	return true;
    }


    /**
     * Draw's an image onto the page
     * @param img The java.awt.Image
     * @param x coordinate on page
     * @param y coordinate on page
     * @param obs ImageObserver
     * @return true if drawn
     */
    public boolean drawImage(Image img,int x,int y,ImageObserver obs) {
	return drawImage(img,x,y,img.getWidth(obs),img.getHeight(obs),obs);
    }

    /**
     * Draw's an image onto the page.
     * <p>Not yet implemented.
     *
     * @param img The java.awt.Image
     * @param x coordinate on page
     * @param y coordinate on page
     * @param w Width on page
     * @param h height on page
     * @param obs ImageObserver
     * @return true if drawn
     */
    public boolean drawImage(Image img,int x,int y,int w,int h,ImageObserver obs) {
      // Si no tengo el nombre del bitmap, asumo que es img.toString()
      return drawImage(img.toString(), img, x, y, w, h, obs);
    }

    /**
     * Draw's an image onto the page, with a backing colour.
     * <p>Currently this just draws the backing colour, as images are not yet
     * supported.
     *
     * @param img The java.awt.Image
     * @param x coordinate on page
     * @param y coordinate on page
     * @param bgcolor Background colour
     * @param obs ImageObserver
     * @return true if drawn
     */
    public boolean drawImage(Image img,int x,int y,Color bgcolor,ImageObserver obs) {
	return drawImage(img,x,y,img.getWidth(obs),img.getHeight(obs),bgcolor,obs);
    }

    /**
     * Draw's an image onto the page, with a backing colour.
     * <p>Currently this just draws the backing colour, as images are not yet
     * supported.
     *
     * @param img The java.awt.Image
     * @param x coordinate on page
     * @param y coordinate on page
     * @param w Width on page
     * @param h height on page
     * @param bgcolor Background colour
     * @param obs ImageObserver
     * @return true if drawn
     */
    public boolean drawImage(Image img,int x,int y,int w,int h,Color bgcolor,ImageObserver obs) {
	c();
	pw.print("q ");	// save state
	Color c = color;	// save current colour
	setColor(bgcolor);	// change the colour
	drawRect(x,y,w,h);
	c("B Q");		// fill stroke, restore state
	color = c;		// restore original colour
	return drawImage(img,x,y,img.getWidth(obs),img.getHeight(obs),obs);
    }

    /**
     * Draw's an image onto the page, with scaling
     * <p>This is not yet supported.
     *
     * @param img The java.awt.Image
     * @param dx1 coordinate on page
     * @param dy1 coordinate on page
     * @param dx2 coordinate on page
     * @param dy2 coordinate on page
     * @param sx1 coordinate on image
     * @param sy1 coordinate on image
     * @param sx2 coordinate on image
     * @param sy2 coordinate on image
     * @param obs ImageObserver
     * @return true if drawn
     */
    public boolean drawImage(Image img,int dx1,int dy1,int dx2,int dy2,int sx1,int sy1,int sx2,int sy2,ImageObserver obs) {
	return false;
    }

    /**
     * Draw's an image onto the page, with scaling
     * <p>This is not yet supported.
     *
     * @param img The java.awt.Image
     * @param dx1 coordinate on page
     * @param dy1 coordinate on page
     * @param dx2 coordinate on page
     * @param dy2 coordinate on page
     * @param sx1 coordinate on image
     * @param sy1 coordinate on image
     * @param sx2 coordinate on image
     * @param sy2 coordinate on image
     * @param bgcolor Background colour
     * @param obs ImageObserver
     * @return true if drawn
     */
    public boolean drawImage(Image img,int dx1,int dy1,int dx2,int dy2,int sx1,int sy1,int sx2,int sy2,Color bgcolor,ImageObserver obs) {
	return false;
    }

    //============ Clipping operations =======================

    /**
     * This holds the current clipRectangle
     */
    protected Rectangle clipRectangle;

    /**
     * Clips to a set of coordinates
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     */
    public void clipRect(int x,int y,int w,int h) {
	setClip(x,y,w,h);
    }

    /**
     * Clips to a set of coordinates
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     */
    public void setClip(int x,int y,int w,int h) {
	clipRectangle = new Rectangle(x,y,w,h);
	c();		// finish off any existing paths
	drawRect(x,y,w,h);
	c("W n");		// clip to current path
    }

    /**
     * As my JDK docs say, this may break with Java 2D.
     * <p>Sets the clipping region to that of a Shape.
     * @param s Shape to clip to.
     */
    public void setClip(Shape s) {
	Rectangle r = s.getBounds();
	setClip(r.x,r.y,r.width,r.height);
    }

    /**
     * This extra method allows PDF users to clip to a Polygon.
     *
     * <p>In theory you could use setClip(), except that java.awt.Graphics
     * only supports Rectangle with that method, so we will have an extra
     * method.
     * @param p Polygon to clip to
     */
    public void clipPolygon(Polygon p) {
	c();		// finish off any existing path
	polygon(p.xpoints,p.ypoints,p.npoints);
	c("W");		// clip to current path
	clipRectangle = p.getBounds();
    }

    /**
     * Returns the Shape of the clipping region
     * As my JDK docs say, this may break with Java 2D
     * @return Shape of the clipping region
     */
    public abstract Shape getClip();

    /**
     * Returns the Rectangle that fits the current clipping region
     * @return the Rectangle that fits the current clipping region
     */
    public Rectangle getClipBounds() {
	return clipRectangle;
    }

    //============ Colour operations =======================

    /**
     * Returns the current pen Colour
     * @return the current pen Colour
     */
    public Color getColor() {
	return color;
    }

    /**
     * Sets the colour for drawing
     * @param c Color to use
     */
    public void setColor(Color c) {
	color = c;
	double r = ((double)c.getRed())/255.0;
	double g = ((double)c.getGreen())/255.0;
	double b = ((double)c.getBlue())/255.0;
	c(); // This ensures any paths are drawn in the previous colours
	pw.println(""+r+" "+g+" "+b+" rg "+r+" "+g+" "+b+" RG");
    }

    /**
     * Not implemented, as this is not supported in the PDF specification.
     */
    public void setPaintMode() {
    }

    /**
     * Not implemented, as this is not supported in the PDF specification.
     * @param c1 Color to xor with
     */
    public void setXORMode(Color c1) {
    }

    //============ Text operations =======================

    /**
     * Returns the FontMetrics for a font.
     * <p>This doesn't work correctly. Perhaps having some way of mapping
     * the base 14 fonts to our own FontMetrics implementation?
     * @param font The java.awt.Font to return the metrics for
     * @return FontMetrics for a font
     */
    public FontMetrics getFontMetrics(Font font) {
	return Toolkit.getDefaultToolkit().getFontMetrics(getFont());
    }

    /**
     * Return's the current font.
     * @return the current font.
     */
    public Font getFont() {
	if(font==null)
	    setFont(new Font("SansSerif",Font.PLAIN,12));
	return font;
    }

    /**
     * This sets the font.
     * @param f java.awt.Font to set to.
     */
    public void setFont(Font f) 
    {
        // Optimise: Save some space if the font is already the current one.
        if(f == null)getFont();
        if(font!=f) {
            font = f;
            // Chequeo a ver si el font es un Type1
            if(PDFFont.isType1(f.getName())) pdffont = page.getFont("/Type1",f);
            else pdffont = page.getFont("/TrueType",f);

            // mark the font as changed
            newFont = true;
        }
    }

    /**
     * This draws a string.
     *
     * @oaran s String to draw
     * @param x coord
     * @param y coord
     */
    public void drawString(String s,int x,int y) {
	nt(x,y);
	if (pdffont.isCJK())		
		pw.println(PDF.toStringCJK(s)+" Tj");
	else
		pw.println(PDF.toString(s)+" Tj");
    }
    
    /** Imprime un texto con el alineamiento especificado 
     * @param s String a imprimir
     * @param x coordenada x
     * @param y coordenada y (bottom del string)
     * @param x2 2� coordenada x
     * @param y2 2� coordenada y
     * @param flags 0 -> Left, 1 -> Center, 2 -> Right | 16 -> wordWrap | 256 -> autoResize
     * @param fontUnderline indica si hay que subrayar el String 
     * @param fontStrikeThru indica si hay que tachar el String
     */
    public void drawString(String text, int x, int y, int x2, int y2, int flags, boolean fontUnderline, boolean fontStrikeThru)
    {
        if(Const.DEBUG_PDF)
        	pw.println("% DrawText: " + text + " [" + x + "," + y + "] Flags: " + flags);
        y += pdffont.getHeight(font.getSize());

        // Aqui se podr�} chequear para ver si el texto entra en la altura y2 - y, para ello lo �nico que hay que hacer es esto:
        // if(y > y2)return;
        
        int width = -1;
        
        int alignment = flags & 3;
        boolean wordWrap = (flags & 16) == 16;
        boolean autoResize = (flags & 256) == 256;
        
        String wordWrapResto = ""; // wordWrapResto tiene la porci�n de texto que no entra en esta l�nea y que ser� impresa una linea m�s abajo del reporte
        // Antes que nada veo si tiene wordWrap, pues en ese caso vamos a ejecutar varios 'drawStrings'
        if(wordWrap)
        {
            if(pdffont == null) // Si no tengo Font asociado, tomo el por defecto
                pdffont = page.getFont("/Type1", getFont());
			// Obtengo la porci�n del texto que se imprimir� en esta l�nea
            String [] retString = new String[1];
            width = pdffont.stringWidth(text, font.getSize(), retString, x2 - x);

            // S�lo debo terminar la l��ea con una palabra completa excepto cuando s�lo entra una palabra cortada
            String tempString = retString[0];
            int strlen = tempString.length();
            if(text.length() != strlen)
                for(;strlen > 0; strlen--)
                    if(text.charAt(strlen) == ' ')break;
            int resto; // Indica en que parte del texto original va a comenzar la pr�xima l��ea (es decir la l��ea despues de esta que estoy procesando ahora)            
            if(strlen == 0)
            {
                strlen = tempString.length(); //En el caso en que no entre ni una palabra entera, metemos lo que podamos
                // Ok, ahora vamos para el otro lado, hasta encontrar el fin de la palabra.
                for(resto = strlen; resto < text.length(); resto++)
                    if(text.charAt(resto) == ' ')break;
                
            }
            else resto = strlen;
            resto++;
            try{ wordWrapResto = text.substring(resto); } catch(StringIndexOutOfBoundsException noMoreText) { wordWrapResto = ""; }
            text = retString[0].substring(0, strlen);            
        }
        else
        { // En caso de que NO sea wordWrap
            if(!autoResize)
            { // Si el AutoResize esta en false, calculo la parte del String que entra dentro del rectangulo
                if(pdffont == null) // Si no tengo Font asociado, tomo el por defecto
                    pdffont = page.getFont("/Type1", getFont());
                String [] retString = new String[1];
                width = pdffont.stringWidth(text, font.getSize(), retString, x2 - x);
                text = retString[0];
            }
            else
                if(alignment != 0 || fontUnderline || fontStrikeThru)
                { // Si el AutoResize esta en true, NO calculo el width para strings 'left' aligned (los m�s comunes)
                    if(pdffont == null) // Si no tengo Font asociado, tomo el por defecto
                        pdffont = page.getFont("/Type1", getFont());
                    width = pdffont.stringWidth(text, font.getSize()); // Obtengo el largo de este String
                }
        }
        
        switch(alignment)
        {
        case 1: // Center Alignment
                drawString(text, (x + x2 - width) / 2, y);

                if(fontUnderline)
                    drawLine((x + x2 - width) / 2, y + 2, (x + x2 + width) / 2, y + 2);
                if(fontStrikeThru)
//                    drawLine((x + x2 - width) / 2, y - font.getSize() / 2 + 2, (x + x2 - width) / 2 + (int)((java.awt.Toolkit.getDefaultToolkit().getFontMetrics(font).stringWidth(s)) / this.SCALE_FACTOR), y - font.getSize() / 2 + 2);
                    drawLine((x + x2 - width) / 2, y - font.getSize() / 2 + 2, (x + x2 - width) / 2 + (int)((PDFFontMetrics.getFontMetricsSize(font).stringWidth(text)) / this.SCALE_FACTOR), y - font.getSize() / 2 + 2);
                break;
        case 2: // Right Alignment
                drawString(text, x2 - width, y);

                if(fontUnderline)
                    drawLine(x2 - width, y + 2, x2, y + 2);
                if(fontStrikeThru)
//                    drawLine(x2 - width, y - font.getSize() / 2 + 2, x2 - width + (int)((java.awt.Toolkit.getDefaultToolkit().getFontMetrics(font).stringWidth(s)) / this.SCALE_FACTOR), y - font.getSize() / 2 + 2);
                    drawLine(x2 - width, y - font.getSize() / 2 + 2, x2 - width + (int)((PDFFontMetrics.getFontMetricsSize(font).stringWidth(text)) / this.SCALE_FACTOR), y - font.getSize() / 2 + 2);
                break;
        case 0: // Left Alignment
        default:
                drawString(text, x, y);

                if(fontUnderline)
                    drawLine(x, y + 2, x + width, y + 2);
                if(fontStrikeThru)
//                    drawLine(x, y - font.getSize() / 2 + 2, x + (int)((java.awt.Toolkit.getDefaultToolkit().getFontMetrics(font).stringWidth(s)) / this.SCALE_FACTOR), y - font.getSize() / 2 + 2);
                    drawLine(x, y - font.getSize() / 2 + 2, x + (int)((PDFFontMetrics.getFontMetricsSize(font).stringWidth(text)) / this.SCALE_FACTOR), y - font.getSize() / 2 + 2);
                break;
        }
        

        // Si el texto ten�} wordWrap, posiblemente falte continuar con m�s texto en la pr�xima l��ea
        if(wordWrap && 
           (y < y2) && 
           wordWrapResto.length() > 0 &&
           (autoResize || (y + pdffont.getHeight(font.getSize()) < y2))) 
        {            
            drawString(wordWrapResto, x, y, x2, y2, flags, fontUnderline, fontStrikeThru);
        }
                
    }

    //============ Optimizers =======================

    /**
     * All functions should call this to close any existing optimised blocks.
     * @see #np
     * @see #nt
     */
    public void c() {
	c("S");
    }

    /**
     * This is used by code that use the path in any way other than Stroke
     * (like Fill, close path & Stroke etc). Usually this is used internally.
     *
     * @param code PDF operators that will close the path
     * @see #np
     * @see #nt
     */
    public void c(String code) {
	if(inText) {
	    pw.println("ET Q");
	    setOrientation(); // fixes Orientation matrix
	}

	if(inStroke)
	    pw.println(code);

	inStroke=inText=false;
    }

    /**
     * Functions that draw lines should start by calling this. It starts a
     * new path unless inStroke is set, in that case it uses the existing path
     * @see #c
     * @see #nt
     */
    public void np() {
	if(inText)
	    c();

	if(!inStroke) {
	    if(pre_np!=null) {
		pw.print(pre_np);	// this is the prefix set by setOrientation()
		pre_np = null;
	    }
	    pw.print("n ");
	}

	inText=false;
	inStroke=true;

	// an unlikely coordinate to fool the moveto() optimizer
	lx = ly = -9999;
    }

    /**
     * Functions that draw text should start by calling this. It starts a text
     * block (accounting for media orientation) unless we are already in a Text
     * block.
     *
     * <p>It also handles if the font has been changed since the current text
     * block was started, so your function will be current.
     *
     * @param x x coord in java space
     * @param y y coord in java space
     */
    public void nt(int x,int y) {
	// close the current path if there is one
	if(inStroke)
	    c();

	// create the text block if one is not current. If we are, the newFont
	// condition at the end catches font changes
	if(!inText) {
	    // This ensures that there is a font available
	    getFont();

	    pw.print("q BT ");
	    tx=ty=0;

	    // produce the text matrix for the media
	    switch(mediaRot)
		{
		case 0:	// Portrait
		    //pw.println("1 0 0 1 0 0 Tm");
		    break;

		case 90:	// Landscape
		    pw.println("0 1 -1 0 0 0 Tm");	// rotate
		    break;

		case 180:	// Inverted Portrait
		    pw.println("1 0 0 -1 0 0 Tm");
		    break;

		case 270:	// Seascape
		    pw.println("0 -1 1 0 0 0 Tm");	// rotate
		    break;
		}

	    // move the text cursor by an absolute amount
	    pw.print(txy(x,y)+"Td ");

	} else {
	    // move the text cursor by a relative amount
	    //int ox=x-tx, oy=ty-y;
	    //pw.print(""+ox+" "+oy+" Td ");
	    //pw.print(cwh(x-tx,y-ty)+"Td ");
	    pw.print(twh(x,y,tx,ty)+"Td ");
	}

	// preserve the coordinates for the next time
	tx = x;
	ty = y;

	if(newFont || !inText)
	    pw.print(pdffont.getName() + " " + font.getSize() + " Tf ");

	// later add colour changes here (if required)

	inStroke = newFont = false;
	inText = true;
    }

    //============ Unsupported operations =======================

    /**
     * This is unsupported - how do you do this with Vector graphics?
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     * @param dx coord
     * @param dy coord
     */
    public void copyArea(int x,int y,int w,int h,int dx,int dy) {
    }

    //============ Line operations =======================

    /**
     * Draws a line between two coordinates.
     *
     * If the first coordinate is the same as the last one drawn
     * (ie a previous drawLine, moveto, etc) it is ignored.
     * @param x1 coord
     * @param y1 coord
     * @param x2 coord
     * @param y2 coord
     */
    public void drawLine(int x1,int y1,int x2,int y2) {
if(Const.DEBUG_PDF)pw.println("% DrawLine: " + x1 + "," + y1 + " -> " + x2 + "," + y2);
	moveto(x1,y1);
	lineto(x2,y2);
    }

    /**
     * Translate the origin.
     * @param x coord offset
     * @param y coord offset
     */
    public void translate(int x,int y) {
	trax+=x;
	tray+=y;
	//c();
	//// we use cw & ch here as the coordinates are relative not absolute
	//pw.println("1 0 0 1 "+cwh(x,y)+" cm");
    }

    //============ Arcs operations ==============================
    // These are the standard Graphics operators. They use the
    // arc extension operators to achieve the affect.

    /**
     * Draws an arc
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     * @param sa Start angle
     * @param aa End angle
     */
    public void drawArc(int x,int y,int w,int h,int sa,int aa) {
	w=w>>1;
	h=h>>1;
	x+=w;
	y+=h;

	arc((double)x,(double)y,
	    (double)w,(double)h,
	    (double)-sa,(double)(-sa-aa),
	    false);
    }

    /**
     * Fills an arc, joining the start and end coordinates
     * @param x coord
     * @param y coord
     * @param w width
     * @param h height
     * @param sa Start angle
     * @param aa End angle
     */
    public void fillArc(int x,int y,int w,int h,int sa,int aa) {
	// here we fool the optimizer. We force any open path to be closed,
	// then draw the arc. Finally, as the optimizer hasn't stroke'd the
	// path, we close and fill it, and mark the Stroke as closed.
	//
	// Note: The lineto to the centre of the object is required, because
	//       the fill only fills the arc. Skipping this includes an extra
	//       chord, which isn't correct. Peter May 31 2000
	c();
	drawArc(x,y,w,h,sa,aa);
	lineto(x+(w>>1),y+(h>>1));
	c("b"); // closepath and fill
    }

    //============ Extension operations ==============================
    // These are extensions, and provide access to PDF Specific
    // operators.

    /**
     * This moves the current drawing point.
     * @param x coord
     * @param y coord
     */
    public void moveto(int x,int y) {
	np();
	if(lx!=x && ly!=y)
	    pw.print(cxy(x,y)+"m ");
	lx=x;
	ly=y;
    }

    /**
     * This moves the current drawing point.
     * @param x coord
     * @param y coord
     */
    public void moveto(double x,double y) {
	np();
	// no optimisation here as it may introduce errors on decimal coords.
	pw.print(cxy(x,y)+"m ");
	lx=(int)x;
	ly=(int)y;
    }

    /**
     * This adds a line segment to the current path
     * @param x coord
     * @param y coord
     */
    public void lineto(int x,int y) {
	np();
	if(lx!=x && ly!=y)
	    pw.print(cxy(x,y)+"l ");
	lx=x;
	ly=y;
    }

    /**
     * This adds a line segment to the current path
     * @param x coord
     * @param y coord
     */
    public void lineto(double x,double y) {
	np();
	// no optimisation here as it may introduce errors on decimal coords.
	pw.print(cxy(x,y)+"l ");
	lx=(int)x;
	ly=(int)y;
    }

    /**
     * This extension allows the width of the drawn line to be set
     * @param w Line width in mm
     * @return double con el LineWidth PDF utilizado
     */
    public double setLineWidth(double w) 
    {
        if(Const.DEBUG_PDF)pw.println("% SetLineWidth: " + w);
        w *= LINE_WIDTH_SCALE;
        c(); // draw any path before we change the line width
        pw.println("" + w + " w");
        return w;
    }

    /**
     * This extension sets the line width to the default of 1mm which is what
     * Java uses when drawing to a PrintJob.
     */
    public void setDefaultLineWidth() {
	c(); // draw any path before we change the line width
	pw.println("1 w");
    }

    /**
     * This is used to add a polygon to the current path.
     * Used by drawPolygon(), drawPolyline() and fillPolygon() etal
     * @param xp Array of x coordinates
     * @param yp Array of y coordinates
     * @param np number of points in polygon
     * @see #drawPolygon
     * @see #drawPolyline
     * @see #fillPolygon
     */
    public void polygon(int[] xp,int[] yp,int np) {
	// np() not needed here as moveto does it ;-)
	moveto(xp[0],yp[0]);
	for(int i=1;i<np;i++)
	    lineto(xp[i],yp[i]);
    }

    /**
     * This extension appends a Bezier curve to the path. The curve
     * extends from the current point to (x3,y3) using (x1,y1) and
     * (x2,y2) as the Bezier control points.
     * <p>The new current point is (x3,y3)
     *
     * @param x1 First control point
     * @param y1 First control point
     * @param x2 Second control point
     * @param y2 Second control point
     * @param x3 Destination point
     * @param y3 Destination point
     */
    public void curveto(int x1,int y1,int x2,int y2,int x3,int y3) {
	np();
	pw.println(cxy(x1,y1)+cxy(x2,y2)+cxy(x3,y3)+"c");
	lx=x3;
	ly=y3;
    }

    /**
     * This extension appends a Bezier curve to the path. The curve
     * extends from the current point to (x3,y3) using (x1,y1) and
     * (x2,y2) as the Bezier control points.
     * <p>The new current point is (x3,y3)
     *
     * @param x1 First control point
     * @param y1 First control point
     * @param x2 Second control point
     * @param y2 Second control point
     * @param x3 Destination point
     * @param y3 Destination point
     */
    public void curveto(double x1,double y1,double x2,double y2,double x3,double y3) {
	np();
	pw.println(cxy(x1,y1)+cxy(x2,y2)+cxy(x3,y3)+"c");
	lx=(int)x3;
	ly=(int)y3;
    }

    /**
     * This extension appends a Bezier curve to the path. The curve
     * extends from the current point to (x2,y2) using the current
     * point and (x1,y1) as the Bezier control points.
     * <p>The new current point is (x2,y2)
     *
     * @param x1 Second control point
     * @param y1 Second control point
     * @param x2 Destination point
     * @param y2 Destination point
     */
    public void curveto(int x1,int y1,int x2,int y2) {
	np();
	pw.println(cxy(x1,y1)+cxy(x2,y2)+"v");
	lx=x2;
	ly=y2;
    }

    /**
     * This extension appends a Bezier curve to the path. The curve
     * extends from the current point to (x2,y2) using the current
     * point and (x1,y1) as the Bezier control points.
     * <p>The new current point is (x2,y2)
     *
     * @param x1 Second control point
     * @param y1 Second control point
     * @param x2 Destination point
     * @param y2 Destination point
     */
    public void curveto(double x1,double y1,double x2,double y2) {
	np();
	pw.println(cxy(x1,y1)+cxy(x2,y2)+"v");
	lx=(int)x2;
	ly=(int)y2;
    }

    /**
     * This extension appends a Bezier curve to the path. The curve
     * extends from the current point to (x2,y2) using (x1,y1) and
     * the end point as the Bezier control points.
     * <p>The new current point is (x2,y2)
     *
     * @param x1 Second control point
     * @param y1 Second control point
     * @param x2 Destination point
     * @param y2 Destination point
     */
    public void curveto2(int x1,int y1,int x2,int y2) {
	np();
	pw.println(cxy(x1,y1)+cxy(x2,y2)+"y");
	lx=x2;
	ly=y2;
    }

    /**
     * This extension appends a Bezier curve to the path. The curve
     * extends from the current point to (x2,y2) using (x1,y1) and
     * the end point as the Bezier control points.
     * <p>The new current point is (x2,y2)
     *
     * @param x1 Second control point
     * @param y1 Second control point
     * @param x2 Destination point
     * @param y2 Destination point
     */
    public void curveto2(double x1,double y1,double x2,double y2) {
	np();
	pw.println(cxy(x1,y1)+cxy(x2,y2)+"y");
	lx=(int)x2;
	ly=(int)y2;
    }


  // Arcs are horrible and complex. They are at the end of the
  // file, because they are the largest. This is because, unlike
  // Postscript, PDF doesn't have any arc operators, so we must
  // implement them by converting into one or more Bezier curves
  // (which is how Postscript does them internally).

    /**
     * One degree in radians
     */
    private static final double degrees_to_radians = Math.PI/180.0;

    /**
     * This produces an arc by breaking it down into one or more Bezier curves.
     * It is used internally to implement the drawArc and fillArc methods.
     *
     * @param axc X coordinate of arc centre
     * @param ayc Y coordinate of arc centre
     * @param width of bounding rectangle
     * @param height of bounding rectangle
     * @param ang1 Start angle
     * @param ang2 End angle
     * @param clockwise true to draw clockwise, false anti-clockwise
     */
    public void arc(double axc,double ayc,
		    double width,double height,
		    double ang1,double ang2,
		    boolean clockwise) {

	double adiff;
	double x0, y0;
	double x3r, y3r;
	boolean first = true;

	// may not need this
	//if( ar < 0 ) {
	//ang1 += fixed_180;
	//ang2 += fixed_180;
	//ar = - ar;
	//}

	double ang1r = (ang1%360.0)*degrees_to_radians;

	double sin0 = Math.sin(ang1r);
	double cos0 = Math.cos(ang1r);

	x0 = axc + width*cos0;
	y0 = ayc + height*sin0;

	// NB: !clockwise here as Java Space is inverted to User Space
	if( !clockwise ) {
	    // Quadrant reduction
	    while ( ang1 < ang2 ) ang2 -= 360.0;
	    while ( (adiff = ang2 - ang1) < -90.0 ) {
		double w = sin0; sin0 = -cos0; cos0 = w;
		x3r = axc + width*cos0;
		y3r = ayc + height*sin0;
		arc_add(first,
			width, height,
			x0, y0,
			x3r, y3r,
			(x0 + width*cos0),
			(y0 + height*sin0)
			);

		x0 = x3r;
		y0 = y3r;
		ang1 -= 90.0;
		first = false;
	    }
	} else {
	    // Quadrant reduction
	    while ( ang2 < ang1 ) ang2 += 360.0;
	    while ( (adiff = ang2 - ang1) > 90.0 ) {
		double w = cos0; cos0 = -sin0; sin0 = w;
		x3r = axc + width*cos0;
		y3r = ayc + height*sin0;
		arc_add(first,
			width, height,
			x0, y0,
			x3r, y3r,
			(x0 + width*cos0),
			(y0 + height*sin0)
			);

		x0 = x3r;
		y0 = y3r;
		ang1 += 90.0;
		first = false;
	    }
	}

	// Compute the intersection of the tangents.
	// We know that -fixed_90 <= adiff <= fixed_90.
	double trad = Math.tan(adiff * (degrees_to_radians / 2));
	double ang2r = ang2 * degrees_to_radians;
	double xt = x0 - trad * width*sin0;
	double yt = y0 + trad * height*cos0;
	arc_add(first, width, height, x0, y0,
		(axc + width * Math.cos(ang2r)),
		(ayc + height * Math.sin(ang2r)),
		xt, yt);
    }

    /**
     * Used by the arc method to actually add an arc to the path
     * Important: We write directly to the stream here, because this method
     * operates in User space, rather than Java space.
     * @param first true if the first arc
     * @param w width
     * @param h height
     * @param x0 coord
     * @param y0 coord
     * @param x3 coord
     * @param y3 coord
     * @param xt coord
     * @param yt coord
     */
    private void arc_add(boolean first,
			 double w,double h,
			 double x0,double y0,
			 double x3,double y3,
			 double xt,double yt) {
	double dx = xt - x0, dy = yt - y0;
	double dist = dx*dx + dy*dy;
	double w2 = w*w, h2=h*h;
	double r2 = w2+h2;

	double fw = 0.0, fh = 0.0;
	if(dist < (r2*1.0e8)) {
	    fw = (4.0/3.0)/(1+Math.sqrt(1+dist/w2));
	    fh = (4.0/3.0)/(1+Math.sqrt(1+dist/h2));
	}

	// The path must have a starting point
	if(first)
	    moveto(x0,y0);

	double x = x0+((xt-x0)*fw);
	double y = y0+((yt-y0)*fh);
	x0 = x3+((xt-x3)*fw);
	y0 = y3+((yt-y3)*fh);

	// Finally the actual curve.
	curveto(x,y,x0,y0,x3,y3);
    }

    /**
     * This sets the media Orientation (0=Portrait, 90=Landscape,
     * 180=Inverse, 270=Seascape).
     *
     * <p>Normally, this is called when the Graphics instance is created, but
     * if the media is changed, then this must be called, especially when using
     * the PDFJob class to create the file.
     *
     */
    public void setOrientation() {
	mediaRot = page.getOrientation();
	switch(mediaRot)
	    {
	    case 0:	// Portrait
		//pre_np = "1 0 0 1 0 "+media.height+" cm 1 0 0 -1 0 0 cm ";
		break;

	    case 90:	// Landscape
		//pw.println("0.7071067 0.7071067 -0.7071067 0.7071067 0 0 Tm");
		//pw.println("1 0 0 1 0 -"+page.getMedia().height+" Tm");
		//pre_np = "1 0 0 1 "+page.getMedia().width+" 0 cm 0 1 -1 0 0 0 cm ";
		break;

	    case 180:	// Inverted Portrait
		//pre_np = "1 0 0 1 "+media.width+" 0 cm -1 0 0 1 0 0 cm ";
		break;

	    case 270:	// Seascape
		// check this
		//pre_np = "1 0 0 1 -"+page.getMedia().width+" 0 cm 0 -1 1 0 0 0 cm ";
		break;
	    }
    }

    /**
     * Converts the Java space coordinates into pdf.
     * @param x coord
     * @param y coord
     * @return String containing the coordinates in PDF space
     */
    private String cxy(int x,int y) {
	return cxy((double)x,(double)y);
    }

    /**
     * Converts the Java space coordinates into pdf.
     * @param x coord
     * @param y coord
     * @return String containing the coordinates in PDF space
     */
    private String cxy(double x,double y) {
        // Scaling...
          x*= SCALE_FACTOR;
          y*= SCALE_FACTOR;
        //
	double nx=x,ny=y; // scratch
	double mw = (double)(media.width);
	double mh = (double)(media.height);

	// handle any translations
	x-=trax;
	y-=tray;

	switch(mediaRot)
	    {
	    case 0:
		// Portrait
		//nx = x;
		ny = mh - y;
		break;

	    case 90:
		// Landscape
		nx = y;
		ny = x;
		break;

	    case 180:
		// Inverse Portrait
		nx = mw - x;
		//ny = y;
		break;

	    case 270:
		// Seascape
		nx = mw - y;
		ny = mh - x;
		break;
	    }
	return ""+nx+" "+ny+" ";
    }

    /**
     * Converts the Java space dimension into pdf.
     * @param w width
     * @param h height
     * @return String containing the coordinates in PDF space
     */
    private String cwh(int w,int h) {
	return cwh((double)w,(double)h);
    }

    /**
     * Converts the Java space dimension into pdf.
     * @param w width
     * @param h height
     * @return String containing the coordinates in PDF space
     */
    private String cwh(double w,double h) {
        // Scaling...
          w*= SCALE_FACTOR;
          h*= SCALE_FACTOR;
        //

	double nw=w,nh=h; // scratch
	switch(mediaRot)
	    {
	    case 0:
		// Portrait
		//nw = w;
		nh = -h;
		break;

	    case 90:
		// Landscape
		nw = h;
		nh = w;
		break;

	    case 180:
		// Inverse Portrait
		nw = -w;
		//nh = h;
		break;

	    case 270:
		// Seascape
		nw = -h;
		nh = -w;
		break;
	    }

	return ""+nw+" "+nh+" ";
    }

    /**
     * Converts the Java space coordinates into pdf text space.
     * @param x coord
     * @param y coord
     * @return String containing the coordinates in PDF text space
     */
    private String txy(int x,int y) {
        // Scaling...
          x*= SCALE_FACTOR;
          y*= SCALE_FACTOR;
        //
	int nx=x, ny=y;
	int mw = media.width;
	int mh = media.height;

	// handle any translations
	x+=trax;
	y+=tray;

	switch(mediaRot)
	    {
	    case 0:
		// Portrait
		//nx = x;
		ny = mh - y;
		break;

	    case 90:
		// Landscape
		//nx = y;
		//ny = x;
		nx = x;
		ny = -y;
		break;

	    case 180:
		// Inverse Portrait
		// to be completed
		nx = mw - x;
		//ny = y;
		break;

	    case 270:
		// Seascape
		// to be completed
		nx = mw - y;
		ny = mh - x;
		break;
	    }

	return ""+nx+" "+ny+" ";
    }

    /**
     * Converts the Java space coordinates into pdf text space.
     * @param x coord
     * @param y coord
     * @param tx coord
     * @param ty coord
     * @return String containing the coordinates in PDF text space
     */
    private String twh(int x,int y,int tx,int ty) {
        // Scaling...
          x*= SCALE_FACTOR;
          y*= SCALE_FACTOR;
          tx*= SCALE_FACTOR;
          ty*= SCALE_FACTOR;
        //
	int nx=x, ny=y;
	int ntx=tx, nty=ty;
	int mw = media.width;
	int mh = media.height;
	int sx=1,sy=1;
	switch(mediaRot)
	    {
	    case 0:
		// Portrait
		//nx = x;
		ny  = mh - y;
		nty = mh - ty;
		break;

	    case 90:
		// Landscape
		//nx = y;
		//ny = x;
		//ntx = ty;
		//nty = tx;
		//sy=-1;
		nx = x;
		ny = -y;
		ntx = tx;
		nty = -ty;
		//sy=-1;
		break;

	    case 180:
		// Inverse Portrait
		// to be completed
		nx = mw - x;
		//ny = y;
		break;

	    case 270:
		// Seascape
		// to be completed
		nx = mw - y;
		ny = mh - x;
		break;
	    }

	nx = sx*(nx-ntx);
	ny = sy*(ny-nty);
	return ""+nx+" "+ny+" ";
    }    
}
