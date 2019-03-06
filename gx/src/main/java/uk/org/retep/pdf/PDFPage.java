// $Log: PDFPage.java,v $
// Revision 1.1  2002/01/04 20:28:48  gusbro
// Initial revision
//
// Revision 1.1.1.1  2002/01/04 20:28:48  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * This class defines a single page within a document.
 *
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public class PDFPage extends PDFObject implements Serializable
{
    /**
     * Specifies that the page is in PORTRAIT orientation.
     */
    public static final int PORTRAIT = 0;

    /**
     * Specifies that the page is in LANDSCAPE orientation.
     */
    public static final int LANDSCAPE = 90;

    /**
     * Specifies that the page is in INVERTEDPORTRAIT orientation.
     */
    public static final int INVERTEDPORTRAIT = 180;

    /**
     * Specifies that the page is in SEASCAPE orientation.
     */
    public static final int SEASCAPE = 270;

    /**
     * Rectangle defining a page in letter format.
     */
    public static final Rectangle MEDIA_letter = new Rectangle(0,0,612,792);

    /**
     * Rectangle defining a page in note format.
     */
    public static final Rectangle MEDIA_note = new Rectangle(0,0,540,720);

    /**
     * Rectangle defining a page in legal format.
     */
    public static final Rectangle MEDIA_legal = new Rectangle(0,0,612,1008);

    /**
     * Rectangle defining a page in a0 format.
     */
    public static final Rectangle MEDIA_a0 = new Rectangle(0,0,2380,3368);

    /**
     * Rectangle defining a page in a1 format.
     */
    public static final Rectangle MEDIA_a1 = new Rectangle(0,0,1684,2380);

    /**
     * Rectangle defining a page in a2 format.
     */
    public static final Rectangle MEDIA_a2 = new Rectangle(0,0,1190,1684);

    /**
     * Rectangle defining a page in a3 format.
     */
    public static final Rectangle MEDIA_a3 = new Rectangle(0,0,842,1190);

    /**
     * Rectangle defining a page in a4 format.
     */
    public static final Rectangle MEDIA_a4 = new Rectangle(0,0,595,842);

    /**
     * Rectangle defining a page in a5 format.
     */
    public static final Rectangle MEDIA_a5 = new Rectangle(0,0,421,595);

    /**
     * Rectangle defining a page in a6 format.
     */
    public static final Rectangle MEDIA_a6 = new Rectangle(0,0,297,421);

    /**
     * Rectangle defining a page in a7 format.
     */
    public static final Rectangle MEDIA_a7 = new Rectangle(0,0,210,297);

    /**
     * Rectangle defining a page in a8 format.
     */
    public static final Rectangle MEDIA_a8 = new Rectangle(0,0,148,210);

    /**
     * Rectangle defining a page in a9 format.
     */
    public static final Rectangle MEDIA_a9 = new Rectangle(0,0,105,148);

    /**
     * Rectangle defining a page in a10 format.
     */
    public static final Rectangle MEDIA_a10 = new Rectangle(0,0,74,105);

    /**
     * Rectangle defining a page in b0 format.
     */
    public static final Rectangle MEDIA_b0 = new Rectangle(0,0,2836,4008);

    /**
     * Rectangle defining a page in b1 format.
     */
    public static final Rectangle MEDIA_b1 = new Rectangle(0,0,2004,2836);

    /**
     * Rectangle defining a page in b2 format.
     */
    public static final Rectangle MEDIA_b2 = new Rectangle(0,0,1418,2004);

    /**
     * Rectangle defining a page in b3 format.
     */
    public static final Rectangle MEDIA_b3 = new Rectangle(0,0,1002,1418);

    /**
     * Rectangle defining a page in b4 format.
     */
    public static final Rectangle MEDIA_b4 = new Rectangle(0,0,709,1002);

    /**
     * Rectangle defining a page in b5 format.
     */
    public static final Rectangle MEDIA_b5 = new Rectangle(0,0,501,709);

    /**
     * Rectangle defining a page in archE format.
     */
    public static final Rectangle MEDIA_archE = new Rectangle(0,0,2592,3456);

    /**
     * Rectangle defining a page in archD format.
     */
    public static final Rectangle MEDIA_archD = new Rectangle(0,0,1728,2592);

    /**
     * Rectangle defining a page in archC format.
     */
    public static final Rectangle MEDIA_archC = new Rectangle(0,0,1296,1728);

    /**
     * Rectangle defining a page in archB format.
     */
    public static final Rectangle MEDIA_archB = new Rectangle(0,0,864,1296);

    /**
     * Rectangle defining a page in archA format.
     */
    public static final Rectangle MEDIA_archA = new Rectangle(0,0,648,864);

    /**
     * Rectangle defining a page in flsa format.
     */
    public static final Rectangle MEDIA_flsa = new Rectangle(0,0,612,936);

    /**
     * Rectangle defining a page in flse format.
     */
    public static final Rectangle MEDIA_flse = new Rectangle(0,0,612,936);

    /**
     * Rectangle defining a page in halfletter format.
     */
    public static final Rectangle MEDIA_halfletter = new Rectangle(0,0,396,612);

    /**
     * Rectangle defining a page in 11x17 format.
     */
    public static final Rectangle MEDIA_11x17 = new Rectangle(0,0,792,1224);

    /**
     * Rectangle defining a page in ledger format.
     */
    public static final Rectangle MEDIA_ledger = new Rectangle(0,0,1224,792);

    /**
     * This is this pages media box, ie the size of the page
     */
    protected Rectangle mediabox;

    /**
     * This is the pages object id that this page belongs to.
     * It is set by the pages object when it is added to it.
     */
    protected PDFObject pages;

    /**
     * This holds the contents of the page.
     */
    protected Vector contents;

    /**
     * Specifies the number of degrees the page should be rotated clockwise
     * when it is displayed. This value must be zero (the default), or a
     * multiple of 90.
     * @see #PORTRAIT
     * @see #LANDSCAPE
     * @see #INVERTEDPORTRAIT
     * @see #SEASCAPE
     */
    protected int rotate;

    /**
     * Object ID that contains a thumbnail sketch of the page.
     * -1 indicates no thumbnail.
     */
    protected PDFObject thumbnail;

    /**
     * This holds any Annotations contained within this page.
     */
    protected Vector annotations;

    /**
     * This holds any resources for this page
     */
    protected Vector resources;

    /**
     * The fonts associated with this page
     */
    protected Vector fonts;

    /**
     * Los bitmaps de esta p�gina
     */
    protected Vector images;

    /**
     * These handle the procset for this page.
     * Refer to page 140 of the PDF Reference manual
     * NB: Text is handled when the fonts Vector is null, and a font is created
     * refer to getFont() to see where it's defined
     */
    protected boolean hasImageB,hasImageC,hasImageI;
    protected procset procset;
    protected Point margin;
    
    public void setMargin(Point margin)
    {
        this.margin = margin;
    }

    /**
     * This constructs a Page object, which will hold any contents for this
     * page.
     *
     * <p>Once created, it is added to the document via the PDF.add() method.
     * (For Advanced use, via the PDFPages.add() method).
     *
     * <p>This defaults to a4 media.
     */
    public PDFPage()
    {
	super("/Page");
	mediabox	= MEDIA_a4;
	contents	= new Vector();
	rotate		= 0;
	thumbnail	= null;
	annotations	= new Vector();
	resources	= new Vector();
	fonts		= new Vector();
        images          = new Vector();
	procset		= null;
    }

    /**
     * Constructs a page, using the supplied media size.
     *
     * @param mediabox Rectangle describing the page size
     */
    public PDFPage(Rectangle mediabox)
    {
	this();
	setMedia(mediabox);
    }

    /**
     * Constructs a page using A4 media, but using the supplied orientation.
     * @param rotate Rotation: 0, 90, 180 or 270
     * @see #PORTRAIT
     * @see #LANDSCAPE
     * @see #INVERTEDPORTRAIT
     * @see #SEASCAPE
     */
    public PDFPage(int rotate)
    {
	this();
	setOrientation(rotate);
    }

    /**
     * Constructs a page using the supplied media size and orientation.
     * @param mediabox Rectangle describing the page size
     * @param rotate Rotation: 0, 90, 180 or 270
     * @see #PORTRAIT
     * @see #LANDSCAPE
     * @see #INVERTEDPORTRAIT
     * @see #SEASCAPE
     */
    public PDFPage(Rectangle mediabox,int rotate)
    {
	this();
	setMedia(mediabox);
	setOrientation(rotate);
    }

    /**
     * This returns a PDFGraphics object, which can then be used to render
     * on to this page. If a previous PDFGraphics object was used, this object
     * is appended to the page, and will be drawn over the top of any previous
     * objects.
     */
    public PDFGraphics getGraphics() {
	try {
	    PDFGraphics g = (PDFGraphics) (Class.forName(PDF.getBasePackage()+".PDFGraphics").newInstance());
	    g.init(this);
	    return g;
	} catch(Exception ex) {
	    ex.printStackTrace();
	}

	return null;
    }

    /**
     * Returns a PDFFont, creating it if not yet used.
     * @para type Font type, usually /Type1
     * @param font Font name
     * @param style java.awt.Font style, ie Font.NORMAL
     */
    public PDFFont getFont(String type, Font f) //String font,int style) {
    {
	// Search the fonts on this page, and return one that matches this
	// font.
	// This keeps the number of font definitions down to one per font/style
        String fontName = f.getName();
        int style = f.getStyle();
        for(Enumeration en = fonts.elements(); en.hasMoreElements(); ) {
            PDFFont ft = (PDFFont) en.nextElement();
            if(ft.equals(type,fontName,style))
                return ft;
        }

	// Ok, the font isn't in the page, so create one.

	// We need a procset if we are using fonts, so create it (if not
	// already created, and add to our resources
	if(fonts.size()==0) {
	    addProcset();
	    procset.add("/Text");
	}

	// finally create and return the font
	PDFFont font = pdf.getFont(type,f);
	fonts.addElement(font);
	return font;
    }


    /**
     * Returns a PDFImage, creating it if not yet used.
     * @param filename Nombre del archivo bitmap
     */
    public PDFImage getImage(String filename, Image img, ImageObserver obs) {
	for(Enumeration en = images.elements(); en.hasMoreElements(); ) {
	    PDFImage im = (PDFImage) en.nextElement();
	    if(im.getFileName().equalsIgnoreCase(filename))
		return im;
	}

	// Ok, the font isn't in the page, so create one.

/*	// We need a procset if we are using fonts, so create it (if not
	// already created, and add to our resources
	if(fonts.size()==0) {
	    addProcset();
	    procset.add("/Text");
	}
*/

	// finally create and return the font
	PDFImage im = pdf.getImage(filename, img,0,0, img.getWidth(obs), img.getHeight(obs), obs);
        im.setFileName(filename);
	images.addElement(im);
	return im;
    }

    /**
     * Sets the media size for this page.
     *
     * <p>Normally, this should be done when the page is created, to avoid
     * problems.
     *
     * @param mediabox Rectangle describing the page size
     */
    public void setMedia(Rectangle mediabox)
    {
	this.mediabox = mediabox;
    }

    /**
     * Returns the page's media.
     * @return Rectangle describing the page size in device units (72dpi)
     */
    public Rectangle getMedia() {
	return mediabox;
    }

    /**
     * Sets the page's orientation.
     *
     * <p>Normally, this should be done when the page is created, to avoid
     * problems.
     *
     * @param rotate Rotation: 0, 90, 180 or 270
     */
    public void setOrientation(int rotate)
    {
	this.rotate = rotate - (rotate%90);	// must be modulus of 90
    }

    /**
     * Returns the pages orientation
     * @see #PORTRAIT
     * @see #LANDSCAPE
     * @see #INVERTEDPORTRAIT
     * @see #SEASCAPE
     * @return current rotation of the page
     */
    public int getOrientation() {
	return rotate;
    }

    /**
     * This adds an object that describes some content to this page.
     *
     * <p><b>Note:</b> Objects that describe contents must be added using this
     * method _AFTER_ the PDF.add() method has been called.
     *
     * @param ob PDFObject describing some contents
     */
    public void add(PDFObject ob) {
	contents.addElement(ob);
    }

    /**
     * This adds an Annotation to the page.
     *
     * <p>As with other objects, the annotation must be added to the pdf
     * document using PDF.add() before adding to the page.
     *
     * @param ob Annotation to add.
     */
    public void addAnnotation(PDFObject ob) {
	annotations.addElement(ob);
    }

    /**
     * This method adds a text note to the document.
     * @param note Text of the note
     * @param x Coordinate of note
     * @param y Coordinate of note
     * @param w Width of the note
     * @param h Height of the note
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addNote(String note,int x,int y,int w,int h) {
	int xy1[] = cxy(x,y+h);
	int xy2[] = cxy(x+w,y);
	PDFAnnot ob = new PDFAnnot(xy1[0],xy1[1],
				   xy2[0],xy2[1],
				   note);
	pdf.add(ob);
	annotations.addElement(ob);
	return ob;
    }

    /**
     * Adds a hyperlink to the document.
     * @param x Coordinate of active area
     * @param y Coordinate of active area
     * @param w Width of the active area
     * @param h Height of the active area
     * @param dest Page that will be displayed when the link is activated. When
     * displayed, the zoom factor will be changed to fit the display.
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addLink(int x,int y,int w,int h,PDFObject dest) {
	int xy1[] = cxy(x,y+h);
	int xy2[] = cxy(x+w,y);
	PDFAnnot ob = new PDFAnnot(xy1[0],xy1[1],
				   xy2[0],xy2[1],
				   dest
				   );
	pdf.add(ob);
	annotations.addElement(ob);
	return ob;
    }

    /**
     * Adds a hyperlink to the document.
     * @param x Coordinate of active area
     * @param y Coordinate of active area
     * @param w Width of the active area
     * @param h Height of the active area
     * @param dest Page that will be displayed when the link is activated
     * @param view Rectangle defining what part of the page should be displayed
     * (defined in Java coordinates). If this is null, then the page is fitted to
     * the display.
     * @return Returns the annotation, so other settings can be changed.
     */
    public PDFAnnot addLink(int x,int y,int w,int h,
			    PDFObject dest,
			    int vx,int vy,int vw,int vh) {
	int xy1[] = cxy(x,y+h);
	int xy2[] = cxy(x+w,y);
	int xy3[] = cxy(vx,vy+vh);
	int xy4[] = cxy(vx+vw,vy);
	PDFAnnot ob = new PDFAnnot(xy1[0],xy1[1],
				   xy2[0],xy2[1],
				   dest,
				   xy3[0],xy3[1],
				   xy4[0],xy4[1]
				   );
	pdf.add(ob);
	annotations.addElement(ob);
	return ob;
    }

    /**
     * This adds a resource to the page.
     * @param resource String defining the resource
     */
    public void addResource(String resource) {
	resources.addElement(resource);
    }

    /**
     * This adds an object that describes a thumbnail for this page.
     * <p><b>Note:</b> The object must already exist in the PDF, as only the
     * object ID is stored.
     * @param thumbnail PDFObject containing the thumbnail
     */
    public void setThumbnail(PDFObject thumbnail)
    {
	this.thumbnail = thumbnail;
    }

    /**
     * This method attaches an outline to the current page being generated. When
     * selected, the outline displays the top of the page.
     * @param title Outline title to attach
     * @return PDFOutline object created, for addSubOutline if required.
     */
    public PDFOutline addOutline(String title) {
	PDFOutline outline = new PDFOutline(title,this);
	pdf.add(outline);
	pdf.getOutline().add(outline);
	return outline;
    }

    /**
     * This method attaches an outline to the current page being generated.
     * When selected, the outline displays the top of the page.
     *
     * <p>Note: If the outline is not in the top level (ie below another
     * outline) then it must <b>not</b> be passed to this method.
     *
     * @param title Outline title to attach
     * @param l Left coordinate of region
     * @param b Bottom coordinate of region
     * @param r Right coordinate of region
     * @param t Top coordinate of region
     * @return PDFOutline object created, for addSubOutline if required.
     */
    public PDFOutline addOutline(String title,int x,int y,int w,int h) {
	int xy1[] = cxy(x,y+h);
	int xy2[] = cxy(x+w,y);
	PDFOutline outline = new PDFOutline(title,this,
					    xy1[0],xy1[1],
					    xy2[0],xy2[1]);
	pdf.add(outline);
	pdf.getOutline().add(outline);
	return outline;
    }

    /**
     * @param os OutputStream to send the object to
     * @exception IOException on error
     */
    public void write(OutputStream os) throws IOException
    {
        // El mediabox que voy a usar es el que ya hab�a con el m�rgen que yo le especifiqu�
        // con el setMargin()
        // (no actualizamos el propio mediabox, porque cada p�gina hereda su valor)
        // Cuando existe un margen, el tama�o de la hoja va a quedar m�s grande!
        // Esto es as� porque lo que hacemos al meter el margen es meter espacio a la izquierda y 
        // arriba de la p�gina. Notar que el MediaBox se mide [left, bottom, width, height]
//        Rectangle thisMediaBox = new Rectangle(this.mediabox.x - margin.x, this.mediabox.y + margin.y, this.mediabox.width, this.mediabox.height);
        Rectangle thisMediaBox = new Rectangle(this.mediabox.x - margin.x, this.mediabox.y, this.mediabox.width + margin.x, this.mediabox.height + margin.y);
        
	// Write the object header
	writeStart(os);

	// now the objects body

	// the /Parent pages object
	os.write("/Parent ".getBytes());
	os.write(pages.toString().getBytes());
	os.write("\r\n".getBytes());

    // the /MediaBox for the page size
	os.write("/MediaBox [".getBytes());
	os.write(Integer.toString(thisMediaBox.x).getBytes());
	os.write(" ".getBytes());
	os.write(Integer.toString(thisMediaBox.y).getBytes());
	os.write(" ".getBytes());
	os.write(Integer.toString(thisMediaBox.x+thisMediaBox.width).getBytes());
	os.write(" ".getBytes());
	os.write(Integer.toString(thisMediaBox.y+thisMediaBox.height).getBytes());
	os.write("]\r\n".getBytes());

	// Rotation (if not zero)
	if(rotate!=0) {
	    os.write("/Rotate ".getBytes());
	    os.write(Integer.toString(rotate).getBytes());
	    os.write("\r\n".getBytes());
	}

	// Now the resources
	os.write("/Resources << ".getBytes());
	// fonts
	if(fonts.size()>0) {
	    os.write("/Font << ".getBytes());
	    for(Enumeration en=fonts.elements();en.hasMoreElements();) {
		PDFFont font = (PDFFont)en.nextElement();
		os.write(font.getName().getBytes());
		os.write(" ".getBytes());
		os.write(font.toString().getBytes());
		os.write(" ".getBytes());
	    }
	    os.write(">> ".getBytes());
	}

	if(images.size()>0) {
	    os.write("/XObject << ".getBytes());
	    for(Enumeration en=images.elements();en.hasMoreElements();) {
		PDFImage image = (PDFImage)en.nextElement();
		os.write(image.getName().getBytes());
		os.write(" ".getBytes());
		os.write(image.toString().getBytes());
		os.write(" ".getBytes());
	    }
	    os.write(">> ".getBytes());
	}
	// Any other resources
	for(Enumeration en=resources.elements();en.hasMoreElements();) {
	    os.write(en.nextElement().toString().getBytes());
	    os.write(" ".getBytes());
	}
	os.write(">>\r\n".getBytes());

	// The thumbnail
	if(thumbnail!=null) {
	    os.write("/Thumb ".getBytes());
	    os.write(thumbnail.toString().getBytes());
	    os.write("\r\n".getBytes());
	}

	// the /Contents pages object
	if(contents.size()>0) {
	    if(contents.size()==1) {
		PDFObject ob = (PDFObject)contents.elementAt(0);
		os.write("/Contents ".getBytes());
		os.write(ob.toString().getBytes());
		os.write("\r\n".getBytes());
	    } else {
		os.write("/Contents [".getBytes());
		os.write(PDFObject.toArray(contents).getBytes());
		os.write("\r\n".getBytes());
	    }
	}

	// The /Annots object
	if(annotations.size()>0) {
	    os.write("/Annots ".getBytes());
	    os.write(PDFObject.toArray(annotations).getBytes());
	    os.write("\r\n".getBytes());
	}

	// finish off with its footer
	writeEnd(os);
    }

    /**
     * This creates a procset and sets up the page to reference it
     */
    private void addProcset() {
	if(procset==null) {
	    pdf.add(procset = new procset());
	    resources.addElement("/ProcSet "+procset);
	}
    }

    /**
     * This defines a procset
     */
    public class procset extends PDFObject {
	private Vector set;

	public procset() {
	    super(null);
	    set = new Vector();

	    // Our default procset (use addElement not add, as we dont want a
	    // leading space)
	    set.addElement("/PDF");
	}

	/**
	 * @param proc Entry to add to the procset
	 */
	public void add(String proc) {
	    set.addElement(" "+proc);
	}

	/**
	 * @param os OutputStream to send the object to
	 * @exception IOException on error
	 */
	public void write(OutputStream os) throws IOException {
	    // Write the object header
	    //writeStart(os);

	    os.write(Integer.toString(objser).getBytes());
	    os.write(" 0 obj\r\n".getBytes());

	    // now the objects body
	    os.write("[".getBytes());
	    for(Enumeration en = set.elements(); en.hasMoreElements(); )
		os.write(en.nextElement().toString().getBytes());
	    os.write("]\r\n".getBytes());

	    // finish off with its footer
	    //writeEnd(os);
	    os.write("endobj\r\n".getBytes());
	}
    }

    /**
     * This utility method converts the y coordinate from Java to User space
     * within the page.
     * @param y Coordinate in Java space
     * @return y Coordinate in User space
     */
    public int cy(int x,int y) {
	return cxy(x,y)[1];
    }

    /**
     * This utility method converts the y coordinate from Java to User space
     * within the page.
     * @param y Coordinate in Java space
     * @return y Coordinate in User space
     */
    public int cx(int x,int y) {
	return cxy(x,y)[0];
    }

    /**
     * This utility method converts the Java coordinates to User space
     * within the page.
     * @param x Coordinate in Java space
     * @param y Coordinate in Java space
     * @return array containing the x & y Coordinate in User space
     */
    public int[] cxy(int x,int y) {
	int nx = x, ny = y;
	int mw = mediabox.width, mh = mediabox.height;

	switch(rotate)
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

	int r[] = new int[2];
	r[0] = nx;
	r[1] = ny;
	return r;
    }

}
