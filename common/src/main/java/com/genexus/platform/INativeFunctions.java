package com.genexus.platform;

import com.genexus.util.*;

public interface INativeFunctions
{
	int FILE_READ 	 	= 1;
	int FILE_ALL  	 	= 2;
	int LOAD_LIBRARY 	= 3;
	int EXIT		 	= 4;
	int READ_PROPERTIES = 5;
	int CONNECT 		= 6;
	int ALL				= 7;

	String 	getWorkstationName();
	String 	getThreadId();
	int 	shellExecute(String fileName, String cmd);
	int 	openDocument(String fileName, int windowMode);

	void    executeWithPermissions(Runnable code, int permissions);
	Object  executeWithPermissions(com.genexus.RunnableThrows code, int permissions) throws Exception;
    
  /** Elimina un archivo */
  public boolean removeFile(String filename);
  
  /** Ejecuta el comando especificado. 
   * @param command Comando a ejecutar
   * @param newConsole Indica si se desea crear una 'consola' para la ejecuci�n
   * @return true si se pudo ejecutar el comando
   */
  public boolean executeModal(String command, boolean newConsole);
	
	/** Retorna una instancia de ThreadLocal
	 * @return IThreadLocal
	 */
	public IThreadLocal newThreadLocal();

	/** Retorna una instancia de ThreadLocal
	 * @param initializer instancia que crea el objeto inicial asociado sl ThreadLocal
	 * @return IThreadLocal
	 */
	public IThreadLocal newThreadLocal(IThreadLocalInitializer initializer);
}