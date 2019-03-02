// $Log: Const.java,v $
// Revision 1.1  2001/05/22 17:09:50  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/05/22 17:09:50  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;

public interface Const
{
    // Aqui definimos algunas constantes globales a ser usadas
    public static boolean DEFLATE = true; // Indica si se va a comprimir el PDF
    public static boolean DEBUG_PDF = false; // Indica si se va a insertar debug dentro del PDF (�til si deflate esta en false)
    public static boolean IMAGE_ASCII_DEFLATE = false; // Indica si las imagenes se codifican usando AsciiDeflate (compatible con cualquier editor de textos)
}
