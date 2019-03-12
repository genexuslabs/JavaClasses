
package uk.org.retep.pdf;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This implements the Image XObject
 */
public class PDFImage extends PDFStream implements ImageObserver, Serializable
{
  // the height of the object
  private int width,height;
  private Image img;
  private PixelGrabber grab;
  private int[] pixels;

  private String name;
  private String fileName = "";

  /** Obtiene el nombre del archivo bitmap o "" en caso de que no se le haya asignado uno
   *  @return Nombre del archivo bitmap
   */
  public String getFileName()
  {
      return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public PDFImage()
  {
    super("/XObject");
  }

  public PDFImage(Image img) {
    this();
    setImage(img,0,0,img.getWidth(this),img.getHeight(this),this);
  }

  public PDFImage(Image img,int x,int y,int w,int h,ImageObserver obs) {
    this();
    setImage(img,x,y,w,h,obs);
  }

  public void setName(String n) {
    name = n;
  }

  public String getName() {
    return name;
  }

  public void setImage(Image img,int x,int y,int w,int h,ImageObserver obs) {
    if(obs == null) obs = this;
    pixels = new int[w*h];
    this.img = img;
    width  = w;
    height = h;
    grab = new PixelGrabber(img,x,y,w,h,pixels,0,w);
    grab.startGrabbing();
  }

  /**
   * @param os OutputStream to send the object to
   * @exception IOException on error
   */
  public void write(OutputStream os) throws IOException
  {
    writeStart(os);

    // write the extra details
    os.write("/Subtype /Image\r\n/Name ".getBytes());
    os.write(name.getBytes());
    os.write("\r\n/Width ".getBytes());
    os.write(Integer.toString(width).getBytes());
    os.write("\r\n/Height ".getBytes());
    os.write(Integer.toString(height).getBytes());
    os.write("\r\n/BitsPerComponent 8\r\n/ColorSpace /DeviceRGB\r\n".getBytes());

    while(true) 
        try { grab.grabPixels(); // Ahora espero hasta obtener todos los pixels
              break;
        } catch (InterruptedException e) { ; }
//    while(grab.status()!=ImageObserver.ALLBITS) {
//    }

    ByteArrayOutputStream bos = getStream();
    byte pix[] = new byte[3*width];	// 1 scan line in bytes
    int p;				// scratch pixel
    int i=0;				// position in pixels array
    for(int y=0;y<height;y++) {

       int o=0;				// position in pix array
      for(int x=0;x<width;x++) {
	p = pixels[i++];
	pix[o++] = (byte)((p >> 16) &  0xff);	// Red
	pix[o++] = (byte)((p >> 8)  &  0xff);	// Green
	pix[o++] = (byte)( p        &  0xff);	// Blue
//        if(com.genexus.reports.PDFReport.DEBUG)com.genexus.reports.PDFReport.DEBUG_STREAM.print(Character.forDigit(pix[o-2]%36, 36)); // Con esto imprimimos el bitmap en modo texto
      }
      bos.write(pix);
//      if(com.genexus.reports.PDFReport.DEBUG)com.genexus.reports.PDFReport.DEBUG_STREAM.println();

    }

    // this will write the actual stream
    setDeflate(true); // Hay que hacer deflate
    setAsciiDeflate(Const.IMAGE_ASCII_DEFLATE); // No quiero hacer un AsciiDeflate
    writeStream(os);

    // Note: we do not call writeEnd() on streams!
  }

  public boolean imageUpdate(Image img,int infoflags,int x,int y,int w,int h) {
    //if(img == this.img) {
    if(infoflags==ImageObserver.WIDTH)
      width = w;
    if(infoflags==ImageObserver.HEIGHT)
      height = h;

    //return true;
    //}
    return false;
  }
}
