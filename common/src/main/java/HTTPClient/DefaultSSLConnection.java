package HTTPClient;

import java.net.*;
import java.io.*;

public class DefaultSSLConnection implements ISSLConnection
{
    public DefaultSSLConnection() {  }

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