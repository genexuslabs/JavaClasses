// $Log: SSLSocketException.java,v $
// Revision 1.1  2001/09/05 14:46:26  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/09/05 14:46:26  gusbro
// GeneXus Java Olimar
//
package HTTPClient;

/**
 * Title: Clases manejo SSL
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author gb
 * @version 1.0
 */

import java.io.IOException;
public class SSLSocketException extends IOException
{
  public SSLSocketException() { super(); }
  public SSLSocketException(String msg) { super(msg); }
}