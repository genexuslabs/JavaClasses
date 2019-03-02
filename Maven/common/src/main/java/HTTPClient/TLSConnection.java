// $Log: TLSConnection.java,v $
// Revision 1.1.2.1  2006/04/26 22:02:43  alevin
// - Initial Revision.
//
//

package HTTPClient;

import java.net.Socket;
import java.io.IOException;
import java.net.InetAddress;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class TLSConnection implements ISSLConnection
{
	private static SSLSocketFactory socketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();

	public TLSConnection() {}

	public Socket processSSLSocket(Socket fromSocket, String host, int port) throws IOException
	{
		((SSLSocket)fromSocket).startHandshake();
		return fromSocket;
	}

	public Socket getSSLSocket(InetAddress addr, int port) throws IOException
	{
		SSLSocket socket = (SSLSocket)socketFactory.createSocket(addr, port);
		return socket;
	}

	public Socket getSSLSocket(InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException
	{
		SSLSocket socket = (SSLSocket)socketFactory.createSocket(addr, port, localAddr, localPort);
		return socket;
	}

	public Object clone()
	{
		return this;
	}
}
