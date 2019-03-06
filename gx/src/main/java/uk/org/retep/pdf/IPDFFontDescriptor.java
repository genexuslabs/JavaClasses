// $Log: IPDFFontDescriptor.java,v $
// Revision 1.1  2001/04/11 18:36:58  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/04/11 18:36:58  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;
import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: ARTECH
 * @author gb
 * @version 1.0
 */

/**
 *  Esta clase maneja los FontDescriptors de un Font (para los TrueType Fonts)
 */
 public interface IPDFFontDescriptor
 {

       /** Inicializa un PDFObject FontDescriptor con Font embebido
   *  @param font PDFFont a describir
   */
   public void init(PDFFont font, boolean embeed);
  /** Obtiene el PDFFontStream del Font embebido
   *
   */
  public PDFFontStream getEmbeededFontStream();
  
  /** Indica si se ha podido embeber el font
   *  @return boolean True si el font ser� embebido
   */
  public boolean isEmbeeded();

  /** Retorna la ubicacion del FontFile
   *  @return String con la ubicaci�n del FontFile o null si no se encontr�
   */
  public String getFontFileLocation();

  /** Obtiene la lista de par�metros de las M�tricas PDF
   *  @return String con la representaci�n de las metricas PDF
   */
  public String getWidths();

}