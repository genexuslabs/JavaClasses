package HTTPClient;

/**
 * Title:        Clases manejo SSL
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author gb
 * @version 1.0
 */

public class SSLManager
{
  private static ISSLConnection sslConnection = null;

  /** Obtiene una instancia de ISSLConnection para ser pasada al HTTPConnection
   *  @see HTTPConnection.setSSLConnection
   *  @return ISSLConnection
   */
  public static ISSLConnection getSSLConnection()
  {
		return getSSLConnection(true);
  }

  /** Obtiene una instancia de ISSLConnection
   *  Este metodo se llama directamente desde SMTPSession y POP3Session
   *  @param uniqueInstance Indica si se quiere obtener la instancia como un singleton
   *  @see SMTPSession y POP3Session
   *  @return ISSLConnection
   */
  public static ISSLConnection getSSLConnection(boolean uniqueInstance)
  {
    if(uniqueInstance)
    {
      if (sslConnection != null)return (ISSLConnection) sslConnection.clone();
    }
    else
    {
      sslConnection = null;
    }

    //Si se quiere usar TLS retorno enseguida la conexion
    if(Boolean.getBoolean("HTTPClient.sslUseTLS"))
    {
            try
            {
                    sslConnection = new TLSConnection();
                    return sslConnection;
            }catch(Throwable TLSNotAvailable){ TLSNotAvailable.printStackTrace(); }
    }

    // Primero probamos con JSSE
	if(!Boolean.getBoolean("HTTPClient.sslDontUseJSSE"))
	{
		try
		{
			sslConnection = new JSSESSLConnection();
		}catch(Throwable JSSENotAvailable){ ; }
	}

    // Sino, retornamos un DefaultSSLConnection (es decir, conexiones sin SSL)
    if(sslConnection == null) sslConnection = new DefaultSSLConnection();

    return (ISSLConnection)sslConnection.clone();
  }

  /** Indica si las conexiones SSL est�n disponibles
   *  @return true si se pueden realizar conexiones SSL
   */
  public static boolean isSSLAvailable()
  {
    if(sslConnection == null)getSSLConnection();
    return !(sslConnection instanceof DefaultSSLConnection);
  }
}
