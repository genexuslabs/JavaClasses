
package HTTPClient;

import com.genexus.common.interfaces.SpecificImplementation;

import java.net.*;
import java.io.IOException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.X509Certificate;

/** Esta clase provee un Binding SSL para la libreria JSSE de Sun
 *
 */
public class JSSESSLConnection implements ISSLConnection
{
    private static SSLSocketFactory defaultSSLFactory =	(SSLSocketFactory) SSLSocketFactory.getDefault();
    private SSLSocketFactory  sslFactory = defaultSSLFactory;

    public JSSESSLConnection() throws Exception
    {
    }

    public Socket processSSLSocket(Socket fromSocket, String host, int port) throws IOException
    {
		SSLSocket sock = (SSLSocket)sslFactory.createSocket(fromSocket, host, port, true);
		if (SpecificImplementation.HttpClient != null)
			SpecificImplementation.HttpClient.prepareSSLSocket( sock);
		return sock;
    }

    public Socket getSSLSocket(InetAddress addr, int port) throws IOException
    {
        return new Socket(addr, port);
    }

    public Socket getSSLSocket(InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException
    {
        return new Socket(addr, port, localAddr, localPort);
    }

    public Object clone()
    {
      return this;
    }

    public String toString()
    {
      return "JSSE SSL Connection, using: " + sslFactory;
    }

    /**
     * Set the SSL socket factory for this connection. If not set, uses the
     * default factory.
     *
     * @param sslFactory the SSL socket factory
     */
    public static void setDefaultSSLSocketFactory(SSLSocketFactory sslFactory)
    {
	defaultSSLFactory = sslFactory;
    }

    /**
     * Set the current SSL socket factory for this connection.
     *
     * @return the current SSL socket factory
     */
    public static SSLSocketFactory getDefaultSSLSocketFactory()
    {
	return defaultSSLFactory;
    }

    /**
     * Set the SSL socket factory for this connection. If not set, uses the
     * default factory.
     *
     * @param sslFactory the SSL socket factory
     */
    public void setSSLSocketFactory(SSLSocketFactory sslFactory)
    {
	this.sslFactory = sslFactory;
    }

    /**
     * Set the current SSL socket factory for this connection.
     *
     * @return the current SSL socket factory
     */
    public SSLSocketFactory getSSLSocketFactory()
    {
		return sslFactory;
    }
}	

