
package com.genexus;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class GXJarClassLoader extends ClassLoader
{
	private static final boolean DEBUG = DebugFlag.DEBUG;
    private String source;
	private ZipFile zipFile = null;
    private boolean sourceIsJAR;
    private Hashtable<String, Long> classTimeStamps = new Hashtable<>(); // Contiene el los timeStamps
    private Hashtable<String, Class> classes = new Hashtable<>();
    private long jarTimeStamp = 0;
    private boolean autoReload;
    private int loadDepth; // Esta variable mantiene un depth de intentos de lectura del Zip
    
    /** El Nombre esta medio mal, porque el GXJarClassLoader obtiene las clases de un JAR o 
     * de una directorio base
     * @param location Archivo ZIP/JAR o ruta a directorio base
     * @param autoReload Indica si se llevara info sobre el autoReload
     * @see getClassLoader
     */
    public GXJarClassLoader(String location, boolean autoReload)
	{
        this.source = location.trim().toLowerCase();
        this.autoReload = autoReload;
        
        sourceIsJAR = new File(source).isFile();
        loadDepth = 0;
        if(!autoReload)openJar();
        if(DEBUG)
            System.out.println("## GXJarClassLoader: Initialized (autoReloading: " + autoReload + ")");
	}
    
    /** Obtiene el ClassLoader asociado. En efecto lo que hace es retornarse a s� mismo en el
     * caso en que el autoReload sea false o que el ClassLoader siga siendo v�lido y sino retorna
     * un nuevo classLoader
     * @return GXJarClassLoader asociado
     */
    public GXJarClassLoader getClassLoaderInstance()
    {
        if  (!autoReload || !wasModified())
            return this;
        else 
		{
			if	(DEBUG)
                System.out.println("## GXJarClassLoader: Changed classes detected ..." );
        	return new GXJarClassLoader(source, autoReload);
		}
    }
    
    /** Indica si se lleva info de autoReload
     * @return boolean indicando si se lleva info de AutoReload
     */
    public boolean getAutoReload()
    {
        return autoReload;
    }

	public Class loadClass(String className) throws ClassNotFoundException 
	{
    	Class cls = (loadClass(className, true));
        
        return cls;
	}
    
    private void openJar()
    {
        if(!sourceIsJAR)return;
        if(zipFile == null)
        {
            try
            {
                zipFile = new ZipFile(source);
                jarTimeStamp = new File(source).lastModified();
            }
            catch (IOException e)
            {
                if	(source.length() != 0)
                    System.err.println("Can't open JAR File: " + source + " : " + e.getMessage());
            }
        }
        loadDepth ++;
    }
    
    private void closeJar()
    {
        if(!sourceIsJAR || zipFile == null)return;
        loadDepth --;
        if(loadDepth > 0)return;
        try
        {
            zipFile.close();
        }catch(IOException e) { ; }
        zipFile = null;

    }
    
	public synchronized Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException 
	{
    	Class   result;
		byte[]  classBytes;

        // Primero vemos si es una SystemClass
        try 
    	{
        	result = super.findSystemClass(className);
        	return result;
        }
    	catch (Throwable e) { ; }
        		
        // Ahora la busco en el cache
        
        if ((result = classes.get(className)) != null)
    		return result;

		// HACK:
        // Intentamos mantener abierto el Jar en el inter�n de carga de clases
        // Como el resolveIt es True, una llamada al loadClass puede implicar la lectura
        // de varias clases de una vez. Todas estas se van a cargar desde el mismo zipFile
        if (autoReload) 
        	openJar(); 
                
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
                if(DEBUG)
                    System.out.println("## GXJarClassLoader: Loading ParentClass: " + className);
                return result;
            }catch(Throwable e) { ; }            
         	throw new ClassNotFoundException(className);
        }
        result = defineClass(className, classBytes, 0, classBytes.length);
    	
    	if (result == null) 
        {
        	closeJar();
            throw new ClassFormatError();
        }

    	if (resolveIt) 
    		resolveClass(result);

    	classes.put(className, result);

        // Ahora 'cerramos' el Jar, porque sino no permitimos que se pueda sobreescribir
        // Esto s�lo lo hacemos si este ClassLoader tiene el flag de autoReload marcado
        // Aparte se 'cierra' unicamente en el caso en que loadDepth sea 1
        if(autoReload)closeJar();

    	return result;
	}
    
    /** Obtiene los bytes de la clase desde el JAR o del directorio
     */
    private byte[] loadBytes(String className) 
	{
		byte[] result = null;
		className = className.replace('.', '/') + ".class";

        if(DEBUG)
            System.out.println("## GXJarClassLoader: Loading class: " + className + " [" + source + "]");
        try
        {
            if(sourceIsJAR)
            { // Si el source se trata de un .JAR
                ZipEntry theEntry;
                if((theEntry = zipFile.getEntry(className)) != null)
                {
                    result = new byte[(int)theEntry.getSize()];
                    InputStream theStream = zipFile.getInputStream(theEntry);
                    new DataInputStream(new BufferedInputStream(theStream)).readFully(result);
                    theStream.close();
                }
            }
            else
            { // OK, si es un directorio
                File theFile = new File(source + File.separator + className);
                if(theFile.exists())
                {
                    result = new byte[(int)theFile.length()];
                    FileInputStream theStream = new FileInputStream(theFile);
                    new DataInputStream(new BufferedInputStream(theStream)).readFully(result);
                    theStream.close();
                    classTimeStamps.put(className, new Long(theFile.lastModified()));
                }
            }
        }catch(IOException e) { ; }
		return result;
	}
	
	/** Indica si este classLoader tiene clases 'viejas'
     * @return true si el classLoader tiene clases viejas
     *  
     */

    public boolean wasModified()
    {
        if  (sourceIsJAR)
		{
			return (new File(source).lastModified()) != jarTimeStamp;
		}
		else
		{ 
			// Si el source es un directorio, tengo que chequear todos las clases cargadas
            for(Enumeration enum1 = classTimeStamps.keys(); enum1.hasMoreElements();)
            {
            	String className = (String) enum1.nextElement();
                if (new File(source + File.separator + className).lastModified() != classTimeStamps.get(className).longValue())
                	return true;
            }
            return false;
		}
    }
} 