
package uk.org.retep.pdf;


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