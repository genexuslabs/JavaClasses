// $Log: PDF.java,v $
// Revision 1.3  2004/09/27 16:46:05  iroqueta
// Ciertos caracteres japoneses no se mostraban en forma correcta...
// A esos caracteres se le agrega un "\" luego del mismo para que se vean en forma correcta.
//
// Revision 1.2  2004/03/03 18:12:22  gusbro
// - Cambios para soportar fonts CJK
//
// Revision 1.1.1.1  2001/06/11 17:59:52  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2001/06/11 17:59:52  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import com.genexus.reports.ParseINI;
import com.genexus.reports.Const;

/**
 * This class is the base of the PDF generator. A PDF class is created for a
 * document, and each page, object, annotation, etc is added to the document.
 * Once complete, the document can be written to an OutputStream, and the PDF
 * document's internal structres are kept in sync.
 * @author Peter T Mount &lt;<a href="http://www.retep.org.uk/pdf/">http://www.retep.org.uk/pdf/&gt;
 * @version 1.0
 */
public class PDF implements Serializable
{
    public ParseINI props = null;

    /**
     * This is used to allocate objects a unique serial number in the document.
     */
    protected int objser;

    /**
     * This vector contains each indirect object within the document.
     */
    protected Vector objects;

    /**
     * This is the Catalog object, which is required by each PDF Document
     */
    private catalog catalog;

    /**
     * This is the info object. Although this is an optional object, we
     * include it.
     */
    private info info;

    /**
     * This is the Pages object, which is required by each PDF Document
     */
    private pages pages;

    /**
     * This is the Outline object, which is optional
     */
    private PDFOutline outline;

    /**
     * This holds a PDFObject describing the default border for annotations.
     * It's only used when the document is being written.
     */
    protected PDFObject defaultOutlineBorder;

    /**
     * This page mode indicates that the document should be opened just with the
     * page visible.
     */
    public static final int USENONE = 0;

    /**
     * This page mode indicates that the Outlines should also be displayed when
     * the document is opened.
     */
    public static final int USEOUTINES = 1;

    /**
     * This page mode indicates that the Thumbnails should be visible when the
     * document first opens.
     */
    public static final int USETHUMBS = 2;

    /**
     * This page mode indicates that when the document is opened, it is displayed
     * in full-screen-mode. There is no menu bar, window controls nor any other
     * window present.
     */
    public static final int FULLSCREEN = 3;

    /**
     * These map the page modes just defined to the pagemodes setting of PDF.
     */
    private static String pagemodepdf[] = {
	"/UseNone",
	"/UseOutlines",
	"/UseThumbs",
	"/FullScreen"
    };

    /**
     * This is used to provide a unique name for a font
     */
    private int fontid = 0;

    private int imageid = 0;

    /**
     * This holds the current fonts
     */
    private Vector fonts;

    private Vector images;

    /**
     * This holds the platform dependent package name
     */
    protected static String basename;

    /**
     * This inits the basename variable, used to handle running on both
     * JDK1.1.x and Java2 platforms
     */
    private static void init() {
	basename = "uk.org.retep.pdf.j2";
	if(System.getProperty("java.version").startsWith("1.1"))
	    basename = "uk.org.retep.pdf.j1";
    }

    /**
     * @return the sub package to use in this JVM
     */
    public static String getBasePackage() {
	return basename;
    }

    /**
     * This creates a PDF document
     */
    public PDF() {
	this(USENONE);
    }


    /** Setea las properties a utilizar (fonts, etc)
     * @param props ParseINI con la properties a utilizar
     */
    public void setProperties(ParseINI props)
    {
        this.props = props;
    }

    /**
     * This creates a PDF document
     * @param pagemode Determines how the document will present itself to
     * the viewer when it first opens.
     */
    public PDF(int pagemode) {
	objser = 1;
	objects = new Vector();
	fonts = new Vector();
        images = new Vector();

	// Now create some standard objects
	add(pages = new pages());
	add(catalog = new catalog(pages,pagemode));
	add(info = new info());

	// Acroread on linux seems to die if there is no root outline
	add(getOutline());
    }

    /**
     * This adds a top level object to the document.
     *
     * <p>Once added, it is allocated a unique serial number.
     *
     * <p><b>Note:</b> Not all object are added directly using this method.
     * Some objects which have Kids (in PDF sub-objects or children are
     * called Kids) will have their own add() method, which will call this
     * one internally.
     *
     * @param obj The PDFObject to add to the document
     * @return the unique serial number for this object.
     */
    public synchronized int add(PDFObject obj)
    {
	objects.addElement(obj);
	obj.objser=objser++; // create a new serial number
	obj.pdf = this;	 // so they can find the pdf they belong to

	// If its a page, then add it to the pages collection
	if(obj instanceof PDFPage)
	    pages.add((PDFPage)obj);

	return obj.objser;
    }

    /**
     * This returns a specific page. It's used mainly when using a
     * Serialized template file.
     * @param page page number to return
     * @return PDFPage at that position
     */
    public PDFPage getPage(int page) {
	return pages.getPage(page);
    }

    /**
     * @return the root outline
     */
    public PDFOutline getOutline()
    {
	if(outline==null) {
	    outline = new PDFOutline();
	    catalog.setOutline(outline);
	}
	return outline;
    }

    /**
     * This returns a font of the specified type and font. If the font has
     * not been defined, it creates a new font in the PDF document, and
     * returns it.
     *
     * @param type PDF Font Type - puede ser "/Type1" o "/TrueType"
     * @param font Java font name
     * @param style java.awt.Font style (NORMAL, BOLD etc)
     * @return PDFFont defining this font
     */
    public PDFFont getFont(String type, Font f)
    {
      String fontName = f.getName();
      int style = f.getStyle();
	for(Enumeration en = fonts.elements(); en.hasMoreElements(); ) {
	    PDFFont ft = (PDFFont) en.nextElement();
	    if(ft.equals(type,fontName,style))
		return ft;
	}

	// the font wasn't found, so create it
	fontid++;
	PDFFont ft = new PDFFont("/F"+fontid,type, f);
	add(ft);
	if(ft.getType().equalsIgnoreCase("/TrueType"))
	{ // Si el Font es un TrueTypeFont, debo agregar al diccionario un FontDescriptor
		PDFFontDescriptor fontDescriptor = PDFFontDescriptor.getPDFFontDescriptor();
		props.setupProperty(Const.EMBEED_SECTION, ft.getRealFontName(), props.getGeneralProperty(Const.EMBEED_SECTION, "false"));

		fontDescriptor.init(ft, props.getBooleanGeneralProperty(Const.EMBEED_SECTION, false) && props.getBooleanProperty(Const.EMBEED_SECTION, ft.getRealFontName(), false));
		add((PDFFontDescriptor)fontDescriptor); // Agrego el fontDescriptor y obtengo el SerialID
		ft.setFontDescriptor(fontDescriptor);
		if(fontDescriptor.getEmbeededFontStream() != null) // Si tengo un Stream del EmbeededFont, lo agrego al diccionario
			add(fontDescriptor.getEmbeededFontStream());
	}

	fonts.addElement(ft);
	return ft;
    }

    public PDFImage getImage(String filename, Image image, int x, int y, int width, int height, ImageObserver obs)
    {
        for(Enumeration en = images.elements(); en.hasMoreElements(); ) {
            PDFImage im = (PDFImage) en.nextElement();
            if(im.getFileName().equalsIgnoreCase(filename))
                return im;
        }

        PDFImage pdfImage = new PDFImage(image, x, y, width, height, obs);
        pdfImage.setFileName(filename);
        setImageName(pdfImage);
        images.addElement(pdfImage);
        add(pdfImage);
        return pdfImage;
    }

  /** Obtiene un Vector con los PDFFonts de este documento
   *  @return Vector con los PDFFonts de este documento
   */
    public Vector getFonts()
    {
        return (Vector)fonts.clone();
    }

    /**
     * Sets a unique name to a PDFImage
     * @param img PDFImage to set the name of
     * @return the name given to the image
     */
    public String setImageName(PDFImage img) {
	imageid++;
	img.setName("/Image"+imageid);
	return img.getName();
    }

    /**
     * Sets the document's author
     * @param author Name of the document's author
     */
    public void setAuthor(String author) {
	info.author = author;
    }

    /**
     * @return the name of the Document's author
     */
    public String getAuthor() {
	return info.author;
    }

    /**
     * Sets the document's creator, usually the name of the application that's
     * creating the document, and it's version number.
     * @param creator Name of the application creating the document.
     */
    public void setCreator(String creator) {
	info.creator = creator;
    }

    /**
     * @return The name and version of the application that created this
     * document.
     */
    public String getCreator() {
	return(info.creator);
    }

    /**
     * Sets the title of the document.
     * @param title Document title
     */
    public void setTitle(String title) {
	info.title = title;
    }

    /**
     * @return The document's title
     */
    public String getTitle() {
	return info.title;
    }

    /**
     * @param subject The document's subject
     */
    public void setSubject(String subject) {
	info.subject = subject;
    }

    /**
     * @return The document's subject
     */
    public String getSubject() {
	return(info.subject);
    }

    /**
     * Sets the keywords for this document. This is usually used by archive
     * applications and search engines.
     * @param keywords Keywords for the document
     */
    public void setKeywords(String keywords) {
	info.keywords = keywords;
    }

    /**
     * @return The keywords for this document.
     */
    public String getKeywords() {
	return info.keywords;
    }

    /**
     * This inner class is used to hold the xref information in the PDF
     * Trailer block.
     *
     * <p>Basically, each object has an id, and an offset in the end file.
     */
    class xref {
	/**
	 * The id of a PDF Object
	 */
	public int id;

	/**
	 * The offset within the PDF file
	 */
	public int offset;

	/**
	 * The generation of the object, usually 0
	 */
	public int generation;

	/**
	 * Creates a crossreference for a PDF Object
	 * @param id The object's ID
	 * @param offset The object's position in the file
	 */
	public xref(int id,int offset)
	{
	    this(id,offset,0);
	}

	/**
	 * Creates a crossreference for a PDF Object
	 * @param id The object's ID
	 * @param offset The object's position in the file
	 * @param generation The object's generation, usually 0
	 */
	public xref(int id,int offset,int generation)
	{
	    this.id = id;
	    this.offset = offset;
	    this.generation = generation;
	}

	/**
	 * @return The xref in the format of the xref section in the PDF file
	 */
	public String toString()
	{
	    String of = Integer.toString(offset);
	    String ge = Integer.toString(generation);
	    String rs = "0000000000".substring(0,10-of.length())+of+" "+"00000".substring(0,5-ge.length())+ge;
	    if(generation==65535)
		return rs+" f ";
	    return rs+" n ";
	}
    }

    /**
     * This inner class is used to write a PDF document. It acts as a wrapper
     * to a real OutputStream, but is necessary for certain internal PDF
     * structures to be built correctly.
     */
    class output
    {
	/**
	 * This is the actual OutputStream used to write to.
	 */
	protected OutputStream os;

	/**
	 * This is the OutputStream used to write each object to.
	 *
	 * <p>We use a separate stream, because we need to keep track of how
	 * many bytes have been written for each object for the xref table to
	 * work correctly.
	 */
	protected ByteArrayOutputStream baos;

	/**
	 * This is the current position within the stream
	 */
	protected int offset;

	/**
	 * This vector contains offsets of each object
	 */
	protected Vector offsets;

	/**
	 * This is used to track the /Root object (catalog)
	 */
	protected PDFObject rootID;

	/**
	 * This is used to track the /Info object (info)
	 */
	protected PDFObject infoID;

	/**
	 * This creates a PDF OutputStream
	 *
	 * @param os The output stream to write the PDF file to.
	 */
	public output(OutputStream os) throws IOException
	{
	    this.os = os;
	    offset = 0;
	    offsets = new Vector();
	    baos = new ByteArrayOutputStream();

	    // Now write the PDF header
	    //
	    // Note: As the encoding is fixed here, we use getBytes().
	    //
	    baos.write("%PDF-1.2\n".getBytes());

	    // This second comment is advised in the PDF Reference manual
	    // page 61
	    baos.write("%\342\343\317\323\n".getBytes());

	    offset = baos.size();
	    baos.writeTo(os);
	}

	/**
	 * This method writes a PDFObject to the stream.
	 *
	 * @param ob PDFObject Obeject to write
	 * @exception IOException on error
	 */
	protected void write(PDFObject ob) throws IOException
	{
	    // Check the object to see if it's one that is needed in the trailer
	    // object
	    if(ob instanceof catalog)	rootID=ob;
	    if(ob instanceof info)		infoID=ob;

	    offsets.addElement(new xref(ob.getSerialID(),offset));
	    baos.reset();
	    ob.write(baos);
	    offset+=baos.size();
	    baos.writeTo(os);
	}

	/**
	 * This closes the Stream, writing the xref table
	 */
	protected void close() throws IOException
	{
	    // Make sure everything is written
	    os.flush();

	    // we use baos to speed things up a little.
	    // Also, offset is preserved, and marks the begining of this block.
	    // This is required by PDF at the end of the PDF file.
	    baos.reset();
	    baos.write("xref\n".getBytes());

	    // Now a single subsection for object 0
	    //baos.write("0 1\n0000000000 65535 f \n".getBytes());

	    // Now scan through the offsets list. The should be in sequence,
	    // but just in case:
	    int firstid = 0;			// First id in block
	    int lastid = -1;			// The last id used
	    Vector block = new Vector();	// xrefs in this block

	    // We need block 0 to exist
	    block.addElement(new xref(0,0,65535));

	    for(Enumeration en = offsets.elements(); en.hasMoreElements(); ) {
		xref x = (xref)en.nextElement();

		if(firstid==-1) firstid=x.id;

		// check to see if block is in range (-1 means empty)
		if(lastid>-1 && x.id != (lastid+1)) {
		    // no, so write this block, and reset
		    writeblock(firstid,block);
		    block.removeAllElements();
		    firstid=-1;
		}

		// now add to block
		block.addElement(x);
		lastid = x.id;
	    }

	    // now write the last block
	    if(firstid>-1)
		writeblock(firstid,block);

	    // now the trailer object
	    baos.write("trailer\n<<\n".getBytes());

	    // the number of entries (REQUIRED)
	    baos.write("/Size ".getBytes());
	    baos.write(Integer.toString(offsets.size()+1).getBytes());
	    baos.write("\n".getBytes());

	    // the /Root catalog indirect reference (REQUIRED)
	    if(rootID != null) {
		baos.write("/Root ".getBytes());
		baos.write(rootID.toString().getBytes());
		baos.write("\n".getBytes());
	    } else
		throw new IOException("Root object is not present in document");

	    // the /Info reference (OPTIONAL)
	    if(infoID != null) {
		baos.write("/Info ".getBytes());
		baos.write(infoID.toString().getBytes());
		baos.write("\n".getBytes());
	    }

	    // end the trailer object
	    baos.write(">>\nstartxref\n".getBytes());
	    baos.write(Integer.toString(offset).getBytes());
	    baos.write("\n%%EOF\n".getBytes());

	    // now flush the stream
	    baos.writeTo(os);
	    os.flush();
	}

	/**
	 * Writes a block of references to the PDF file
	 * @param firstid ID of the first reference in this block
	 * @param block Vector containing the references in this block
	 * @exception IOException on write error
	 */
	protected void writeblock(int firstid,Vector block) throws IOException
	{
	    baos.write(Integer.toString(firstid).getBytes());
	    baos.write(" ".getBytes());
	    baos.write(Integer.toString(block.size()).getBytes());
	    baos.write("\n".getBytes());
	    //baos.write("\n0000000000 65535 f\n".getBytes());

	    for(Enumeration en=block.elements(); en.hasMoreElements(); ) {
		baos.write(en.nextElement().toString().getBytes());
		baos.write("\n".getBytes());
	    }
	}
    }

    /**
     * This writes the document to an OutputStream.
     *
     * <p><b>Note:</b> You can call this as many times as you wish, as long as
     * the calls are not running at the same time.
     *
     * <p>Also, objects can be added or amended between these calls.
     *
     * <p>Also, the OutputStream is not closed, but will be flushed on
     * completion. It is up to the caller to close the stream.
     *
     * @param os OutputStream to write the document to
     * @exception IOException on error
     */
    public void write(OutputStream os) throws IOException
    {
	output pos = new output(os);

	// Write each object to the OutputStream. We call via the output
	// as that builds the xref table
	for(Enumeration en = objects.elements(); en.hasMoreElements(); )
	    pos.write((PDFObject)en.nextElement());

	// Finally close the output, which writes the xref table.
	pos.close();

	// and flush the output stream to ensure everything is written.
	os.flush();
    }

    /**
     * This returns a PrintJob object, that can be used by existing code, so
     * instead of printing, a pdf file is produced.
     *
     * <p>This is a convenience method. It simply calls getPDFJob(), which
     * returns the same object.
     *
     * @param os OutputStream to write the PDF to once complete
     * @param title The job title. Here this will become the document's title
     * @return PrintJob to pass to existing code
     * @exception IOException on error writing
     */
    public static PrintJob getPrintJob(OutputStream os,String title) {
	return (PrintJob) getPDFJob(os,title);
    }

    /**
     * This returns a PDFJob object, which can be used in the same way as AWT's
     * PrintJob class (it extends it), except the PDFJob class contains extra
     * methods for adding annotations, and outlines, etc.
     * @param os OutputStream to write the PDF to once complete
     * @param title The job title. Here this will become the document's title
     * @return PDFJob to pass to existing code
     */
    public static PDFJob getPDFJob(OutputStream os) {
	return getPDFJob(os,null);
    }

    /**
     * This returns a PDFJob object, which can be used in the same way as AWT's
     * PrintJob class (it extends it), except the PDFJob class contains extra
     * methods for adding annotations, and outlines, etc.
     * @param os OutputStream to write the PDF to once complete
     * @param title The job title. Here this will become the document's title
     * @return PDFJob to pass to existing code
     */
    public static PDFJob getPDFJob(OutputStream os,String title) {
	init();
	try {
	    PDFJob job = (PDFJob) (Class.forName(basename+".PDFJob").newInstance());
	    job.init(os);
	    job.getPDF().setTitle(title);
	    return job;
	} catch(Exception ex) {
	    ex.printStackTrace();
	}

	return null;
    }

    /**
     * This presents a Dialog box asking for a details of a document (for the
     * info object), and a file name (a button exists to open a file requester)
     * to write the PDF file to.
     *
     * <p>This is identical in syntax to the method in java.awt.Toolkit
     *
     * @param frame Frame for the Dialog box
     * @param title The job title. Here this will become the document's title
     * @param props Properties (unused)
     * @return PrintJob to pass to existing code
     * @exception IOException on error writing
     */
    public static PrintJob getPrintJob(Frame frame,String title,Properties props)
    {
	// to be worked on - should have this code in a separate class to keep
	// this class size down
	return null;
    }

    /**
     * This object contains the document's pages.
     */
    class pages extends PDFObject
    {
	/**
	 * This holds the pages
	 */
	private Vector pages;

	/**
	 * This constructs a PDF Pages object.
	 */
	public pages() {
	    super("/Pages");
	    pages = new Vector();
	}

	/**
	 * This adds a page to the document.
	 *
	 * @param page PDFPage to add
	 */
	public void add(PDFPage page) {
	    pages.addElement(page);

	    // Tell the page of ourselves
	    page.pages = this;
	}

	/**
	 * This returns a specific page. Used by the PDF class.
	 * @param page page number to return
	 * @return PDFPage at that position
	 */
	public PDFPage getPage(int page) {
	    return (PDFPage)(pages.elementAt(page));
	}

	/**
	 * @param os OutputStream to send the object to
	 * @exception IOException on error
	 */
	public void write(OutputStream os) throws IOException {
	    // Write the object header
	    writeStart(os);

	    // now the objects body

	    // the Kids array
	    os.write("/Kids ".getBytes());
	    os.write(PDFObject.toArray(pages).getBytes());
	    os.write("\n".getBytes());

	    // the number of Kids in this document
	    os.write("/Count ".getBytes());
	    os.write(Integer.toString(pages.size()).getBytes());
	    os.write("\n".getBytes());

	    // finish off with its footer
	    writeEnd(os);
	}
    }

    /**
     * This inner class implements the PDF Catalog, also known as the root node
     */
    class catalog extends PDFObject
    {
	/**
	 * The pages of the document
	 */
	private pages pages;

	/**
	 * The outlines of the document
	 */
	private PDFOutline outlines;

	/**
	 * The initial page mode
	 */
	private int pagemode;

	/**
	 * This constructs a PDF Catalog object
	 *
	 * @param pages The pages object thats the root of the documents page
	 * tree
	 * @param pagemode How the document should appear when opened.
	 * Allowed values are USENONE, USEOUTLINES, USETHUMBS or FULLSCREEN.
	 */
	public catalog(pages pages,int pagemode) {
	    super("/Catalog");
	    this.pages = pages;
	    this.pagemode = pagemode;
	}

	/**
	 * This sets the root outline object
	 * @param outline The root outline
	 */
	protected void setOutline(PDFOutline outline) {
	    this.outlines = outline;
	}

	/**
	 * @param os OutputStream to send the object to
	 * @exception IOException on error
	 */
	public void write(OutputStream os) throws IOException {
	    // Write the object header
	    writeStart(os);

	    // now the objects body

	    // the /Pages object
	    os.write("/Pages ".getBytes());
	    os.write(pages.toString().getBytes());
	    os.write("\n".getBytes());

	    // the Outlines object
	    if(outlines!=null) {
		//if(outlines.getLast()>-1) {
		os.write("/Outlines ".getBytes());
		os.write(outlines.toString().getBytes());
		os.write("\n".getBytes());
		//}
	    }

	    // the /PageMode setting
	    os.write("/PageMode ".getBytes());
	    os.write(pagemodepdf[pagemode].getBytes());
	    os.write("\n".getBytes());

	    // finish off with its footer
	    writeEnd(os);
	}
    }

    /**
     * This class stores details of the author, the PDF generator etc.
     * The values are accessible via the PDF class
     */
    class info extends PDFObject
    {
	/**
	 * The author of the document
	 */
	public String author;

	/**
	 * PDF has two values, a Creator and a Producer. The creator field is
	 * available for calling code. The producer is fixed by this library.
	 */
	public String creator;

	/**
	 * This is the document's title
	 */
	public String title;

	/**
	 * This is the subject of the document
	 */
	public String subject;

	/**
	 * This contains any keywords for the document
	 */
	public String keywords;

	/**
	 * This constructs a minimal info object
	 */
	public info() {
	    super(null);
	    author = creator = title = subject = keywords = null;
	}

	/**
	 * @param title Title of this document
	 */
	public info(String title) {
	    this();
	    this.title = title;
	}

	/**
	 * @param os OutputStream to send the object to
	 * @exception IOException on error
	 */
	public void write(OutputStream os) throws IOException {
	    // Write the object header
	    writeStart(os);

	    // now the objects body

	    if(author!=null) {
		os.write("/Author (".getBytes());
		os.write(PDF.toString(author).getBytes());
		os.write(")\n".getBytes());
	    }

	    if(creator!=null) {
		os.write("/Creator (".getBytes());
		os.write(PDF.toString(creator).getBytes());
		os.write(")\n".getBytes());
	    }

	    os.write("/Producer ".getBytes());
	    os.write(PDF.toString("Retep JavaPDF Library 1.0 http://www.retep.org.uk - extension 1").getBytes());
	    os.write("\n".getBytes());

	    if(title!=null) {
		os.write("/Title ".getBytes());
		os.write(PDF.toString(title).getBytes());
		os.write("\n".getBytes());
	    }

	    if(subject!=null) {
		os.write("/Subject (".getBytes());
		os.write(PDF.toString(subject).getBytes());
		os.write(")\n".getBytes());
	    }

	    if(keywords!=null) {
		os.write("/Keywords (".getBytes());
		os.write(PDF.toString(keywords).getBytes());
		os.write(")\n".getBytes());
	    }

	    // finish off with its footer
	    writeEnd(os);
	}
    }

    /**
     * This converts a string into PDF. It prefixes ( or ) with \
     * and wraps the string in a ( ) pair.
     * @param s String to convert
     * @return String that can be placed in a PDF (or Postscript) stream
     */
    public static String toString(String s) {
	if(s.indexOf("(")>-1)
	    s = replace(s,"(","\\(");

	if(s.indexOf(")")>-1)
	    s = replace(s,")","\\)");

	return "("+s+")";
    }
	
    public static String toStringCJK(String s) {
	String s1 = "";
	for(int i=0; i<=s.length()-1; i++)
	{
		s1 = s1 + s.charAt(i);
		if (s.charAt(i) == '\u7533')
		{
		s1 = s1 + "\\";
		}
	}
	
	//System.out.println(s1.charAt(s1.length()-2));
	//if (s1.charAt(s1.length()-2) != '\u7533')
	//{
	//s1 = s1.substring(0, s1.length()-1);
	//System.out.println(s1);
	//}
		
	if(s.indexOf("(")>-1)
	    s = replace(s,"(","\\(");

	if(s.indexOf(")")>-1)
	    s = replace(s,")","\\)");

	return "("+s1+")";
    }	

    /**
     * Helper method for toString()
     * @param s source string
     * @param f string to remove
     * @param t string to replace f
     * @return string with f replaced by t
     */
    private static String replace(String s,String f,String t) {
	StringBuffer b = new StringBuffer();
	int p = 0, c=0;

	while(c>-1) {
	    if((c = s.indexOf(f,p)) > -1) {
		b.append(s.substring(p,c));
		b.append(t);
		p=c+1;
	    }
	}

	// include any remaining text
	if(p<s.length())
	    b.append(s.substring(p));

	return b.toString();
    }

}
