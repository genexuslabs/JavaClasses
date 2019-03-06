// $Log: DefaultSSLConnection.java,v $
// Revision 1.1  2001/09/05 16:56:48  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/09/05 16:56:48  gusbro
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

import java.net.*;
import java.io.*;

/** Esta clase provee un default Binding SSL
 *  Si se intenta conectar mediante HTTPS, se tirar� una SSLSocketException
 */
public class DefaultSSLConnection implements ISSLConnection
{
    public DefaultSSLConnection() { ; }

    public Socket processSSLSocket(Socket fromSocket, String host, int port) throws IOException
    {
      throw new SSLSocketException("Cannot use HTTPS with DefaultSSLConnection!");
    }

    public Socket getSSLSocket(InetAddress addr, int port) throws IOException
    {
      throw new SSLSocketException("Cannot use HTTPS with DefaultSSLConnection!");
    }

    public Socket getSSLSocket(InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException
    {
      throw new SSLSocketException("Cannot use HTTPS with DefaultSSLConnection!");
    }

    public Object clone()
    {
      return this;
    }

    public String toString()
    {
      return "Default SSL Connection (NO SSL Connection)";
    }
}