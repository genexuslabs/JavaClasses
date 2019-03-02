// $Log: GxUtilsLoader.java,v $
// Revision 1.4  2005/09/20 13:16:39  alevin
// - Agrego llamado al Client Config.
//
// Revision 1.3  2005/05/25 16:05:00  gusbro
// - Agrego un catch para imprimir errores de invocacion
//
// Revision 1.2  2005/05/13 15:33:28  gusbro
// - Release Inicial
//
//
package com.genexus.util; 

import java.util.zip.*;
import java.util.*;
import java.io.*;

public class GxUtilsLoader extends ClassLoader
{
	public static String GXUTILS_FILE = "GxUtils.jar";
	private static GxUtilsLoader loader = null;
	
	public static void runDeveloperMenu(String [] arg)
	{
		try
		{				
			Class c = getClass("com.genexus.gx.deployment.developermenu");
			c.getMethod("start", new Class[]{String[].class}).invoke(c.newInstance(), new Object[]{arg});
		}catch(java.lang.reflect.InvocationTargetException e)
		{
			e.getTargetException().printStackTrace();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void runServerConfig(String []fileName)
	{
		try
		{
			Class c = getClass("com.genexus.gx.deployment.usrvcfg");
			c.getMethod("execute", new Class[]{String[].class}).invoke(c.getConstructor(new Class[]{int.class}).newInstance(new Object[]{new Integer(-1)}), new Object[]{fileName});
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void runClientConfig(String []fileName)
	{
		try
		{
			Class c = getClass("com.genexus.gx.deployment.uclicfg");
			c.getMethod("execute", new Class[]{String[].class}).invoke(c.getConstructor(new Class[]{int.class}).newInstance(new Object[]{new Integer(-1)}), new Object[]{fileName});
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	///--------------------------------------------------------------------
	
	private static Class getClass(String className)throws Exception
	{
		try
		{
			return Class.forName(className);
		}catch(Exception e)
		{
			setLoader();
		}
		return loader.loadClass(className);
	}
	
	private static void setLoader() throws Exception 
	{
		if(loader == null)
		{
			System.err.println("Utilities jar file (" + GXUTILS_FILE + ") is not included in classpath");
			System.err.println("Automatically loading " + GXUTILS_FILE + "...");
			loader = new GxUtilsLoader("GxUtils.jar");
		}
	}
	
	private String source;
	private ZipFile zipFile = null;
	private Hashtable classes = new Hashtable();
	private int loadDepth; // Esta variable mantiene un depth de intentos de lectura del Zip
	
	/** El Nombre esta medio mal, porque el GXJarClassLoader obtiene las clases de un JAR o 
	 * de una directorio base
	 * @param location Archivo ZIP/JAR o ruta a directorio base
	 * @see getClassLoader
	 */
	public GxUtilsLoader(String location)
	{
		this.source = location.trim().toLowerCase();
		
		loadDepth = 0;
		openJar();
	}
	
	
	public Class loadClass(String className) throws ClassNotFoundException 
	{		Class cls = (loadClass(className, true));
		
		return cls;
	}
	
	private void openJar()
	{
		if(zipFile == null)
		{
			try
			{
				zipFile = new ZipFile(source);
			}
			catch (IOException e)
			{
				if	(source.length() != 0)
				{
					System.err.println("Could not open JAR File: " + source);
					System.err.println(e.toString());
				}
			}
		}
		loadDepth ++;
	}
	
	private void closeJar()
	{
		loadDepth --;
		if(loadDepth > 0)return;
		try
		{
			if(zipFile != null)
			{
				zipFile.close();
			}
		}catch(IOException e) { ; }
		zipFile = null;

	}
	
	public synchronized Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException 
	{
		Class   result;
		byte[]  classBytes;

		// Primero vemos si es una SystemClass
		try 
		{			result = super.findSystemClass(className);
			return result;
		}
		catch (Throwable e) { ; }
		
		// Ahora la busco en el cache
		
		if ((result = (Class) classes.get(className)) != null)
			return result;
		
		classBytes = loadBytes(className);

		if (classBytes == null) 
		{
			closeJar();
			// OK, si nuestro classLoader NO pudo encontrar la clase, le damos una �ltima oportunidad
			// buscando en el ParentClassLoader (no tiene por qu� haber uno)
			// Esto es importante cuando el esquema de classloaders es el de Java 2 (x ej, el usado en el Tomcat 4)
			try
			{
				result = this.getClass().getClassLoader().loadClass(className);
				return result;
			}catch(Throwable e) { ; }            
			throw new ClassNotFoundException(className);
		}
		result = defineClass(classBytes, 0, classBytes.length);
		
		if (result == null) 
		{
			closeJar();
			throw new ClassFormatError();
		}

		if (resolveIt) 
			resolveClass(result);

		classes.put(className, result);

		return result;
	}
	
	/** Obtiene los bytes de la clase desde el JAR o del directorio
	 */
	private byte[] loadBytes(String className) 
	{
		if(zipFile == null)
		{
			throw new NoClassDefFoundError(className);
		}
		
		byte[] result = null;
		className = className.replace('.', '/') + ".class";
		
		try
		{
			ZipEntry theEntry;
			if((theEntry = zipFile.getEntry(className)) != null)
			{
				result = new byte[(int)theEntry.getSize()];
				InputStream theStream = zipFile.getInputStream(theEntry);
				new DataInputStream(new BufferedInputStream(theStream)).readFully(result);
				theStream.close();
			}
		}catch(IOException e) { ; }
		return result;
	}		
} 	

