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