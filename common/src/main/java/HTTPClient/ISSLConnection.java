
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
import java.io.IOException;

/**
 * Esta interfaz es la que debe implementar cada una de las librer�as SSL
 *
 */
public interface ISSLConnection {

	/**
	 * Procesa el socket y retorna un SSL socket (o ejecuta operaciones sobre ese
	 * socket)
	 * 
	 * @see HTTPConnection.sendRequest line: 2921
	 * @param fromSocket
	 *            Socket a procesar
	 * @param host
	 *            Host a conectar
	 * @param port
	 *            Puerto a conectar
	 * @return Socket a ser utilizado en la conexi�n
	 */
	Socket processSSLSocket(Socket fromSocket, String host, int port) throws IOException;

	/**
	 * Obtiene un SSLSocket (o puede obtener un Socket com�n y luego ser
	 * 'transformado' por el m�todo processSSLSocket()
	 * 
	 * @param addr
	 *            InetAddress a conectarse
	 * @param port
	 *            puerto a utilizar
	 */
	Socket getSSLSocket(InetAddress addr, int port) throws IOException;

	/**
	 * Obtiene un SSLSocket (o puede obtener un Socket com�n y luego ser
	 * 'transformado' por el m�todo processSSLSocket()
	 * 
	 * @param addr
	 *            InetAddress a conectarse
	 * @param port
	 *            puerto a utilizar
	 * @param localAddr
	 *            InetAddress Local
	 * @param localPort
	 *            puerto local
	 */
	Socket getSSLSocket(InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException;

	/**
	 * Este m�todo es llamado en el RedirectionModule para obtener una nueva
	 * instancia de ISSLConnection Aqui se puede inicializar esta nueva instancia
	 * con los valores deseados
	 */
	Object clone();
}
