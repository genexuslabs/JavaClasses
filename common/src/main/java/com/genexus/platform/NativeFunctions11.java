package com.genexus.platform;

import com.genexus.*;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.*;
import java.io.InputStream;
import java.io.IOException;
import java.util.*;

public class NativeFunctions11 implements INativeFunctions
{
  public NativeFunctions11()
  {
  }

	public String getThreadId()
	{
		return Thread.currentThread().toString();
	}

	public String getWorkstationName()
	{
          try 
          {
            java.net.InetAddress i = java.net.InetAddress.getLocalHost();
            return i.getHostName().toUpperCase();
          }
          catch(Exception e)
          {
            e.printStackTrace();
            return "";
          }
	}

	public int openDocument(String fileName, int windowMode)
	{
		SpecificImplementation.Application.displayURL(fileName);
		return 1;
	}

	public int shellExecute(String fileName, String cmd)
	{
		return 0;
	}

	public void executeWithPermissions(Runnable code, int permissions)
	{
		code.run();
	}

	public Object executeWithPermissions(RunnableThrows code, int permissions) throws Exception
	{
		return code.run();
	}

  /** Elimina un archivo.
   * @param filename Nombre del archivo a eliminar
   * @return true si el archivo NO estaba en uso
   **/
  public boolean removeFile(String filename)
  {
      java.io.File file = new java.io.File(filename);
      if(!file.exists() || file.delete())return true;
      return false;
  }
	
	/** Ejecuta el comando especificado.
	 * @param command Comando a ejecutar
	 * @param newConsole Indica si se desea crear una 'consola' para la ejecuci�n
	 * @return true si se pudo ejecutar el comando
	 */
	public boolean executeModal(String command, boolean newConsole)
	{
    Process proc;
    try
    {
        proc = Runtime.getRuntime().exec(command);
    }catch(IOException e)
    {
        return false;
    }
    final InputStream stdin = proc.getInputStream();
    final InputStream stderr = proc.getErrorStream();
    Thread inReader = new Thread(new Runnable(){
            public void run(){
                while(true) try{ stdin.read(); } catch(IOException e) { ; }
            }});
    inReader.setPriority(Thread.MIN_PRIORITY);
    inReader.start();
    Thread errReader = new Thread(new Runnable(){
            public void run(){
                while(true) try{ stderr.read(); } catch(IOException e) { ; }
            }});
    errReader.setPriority(Thread.MIN_PRIORITY);
    errReader.start();

    while(true)
    {
        try
        {
            proc.waitFor();
            inReader.stop();
            errReader.stop();
            return true;
        }catch(InterruptedException wait){ ; }
    }
	}
  
	/** Retorna una instancia de ThreadLocal
	 * @return IThreadLocal
	 */
	public com.genexus.util.IThreadLocal newThreadLocal()
	{
		return  SpecificImplementation.NativeFunctions.newThreadLocal(null);
	}
	
	/** Retorna una instancia de ThreadLocal
	 * @param initializer instancia que crea el objeto inicial asociado sl ThreadLocal
	 * @return IThreadLocal
	 */
	public IThreadLocal newThreadLocal(IThreadLocalInitializer initializer)
	{
		return  SpecificImplementation.NativeFunctions.newThreadLocal(initializer); 
	}
}