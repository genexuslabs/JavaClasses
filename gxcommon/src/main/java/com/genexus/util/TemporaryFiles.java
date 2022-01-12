package com.genexus.util;

import com.genexus.ICleanedup;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.platform.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.File;

public class TemporaryFiles implements ICleanedup 
{
    private static final Vector<String> files = new Vector<>();
    private static final INativeFunctions nativeCode = SpecificImplementation.NativeFunctions.getInstance();
    private static final TemporaryFiles temporaryFiles = new TemporaryFiles();
    
    /** Obtiene la instancia de TemporaryFiles
     */
    public static TemporaryFiles getInstance()
    {
        return temporaryFiles;
    }
    
    private TemporaryFiles() 
    { 
        SpecificImplementation.Application.addCleanup(this);
    }
    
    /** Realiza el cleanup de los archivos temporales
     */
    public void cleanup()
    {
        for(Enumeration enum1 = files.elements(); enum1.hasMoreElements();)
			nativeCode.removeFile((String)enum1.nextElement());
        files.removeAllElements();
    }
    
    /** Obtiene un nombre temporal y lo inserta en la lista
     * @param Extension del archivo temporal (sin el .)
     * @return Nombre del archivo temporal 
     */
    public String getTemporaryFile(String extension)
    {
        String tempFile;
        extension = "." + extension;
        do tempFile = "" + ((int)(Math.random() * 1e8)) + extension;
        while(new File(tempFile).exists());
        addFile(tempFile);
        return tempFile;
    }
    
    /** Obtiene un nombre temporal a partir del nombre y extensi�n pasados como par�metros
     *  y lo inserta en la lista
     * @param filename Nombre temporal tentativo
     * @param extension Extension del archivo temporal (sin el .)
     * @return Nombre del archivo temporal
     */
    public String getTemporaryFile(String filename, String extension)
    {
        extension = "." + extension;
        String tempFile = filename + extension; // Primero intentamos con el nombre del archivo
        int expCounter = 1;
        File file = new File(tempFile);
        while(file.exists()) // Luego intentamos con un PRNG incremental en el tama�o
        {
            tempFile = filename + ((int)(Math.random() * expCounter)) + extension;
            expCounter *= 2;
            expCounter %= 1e8;
            file = new File(tempFile);
            file.delete(); // Intento eliminarlo (si no est� lockeado)
        }
        addFile(tempFile);
        return tempFile;
    }
    
    /** Agrega un archivo a la lista de TemporaryFiles
     * @param Nombre del archivo
     */
    public void addFile(String file)
    {
        files.addElement(file);
    }
    
    /** Elimina un archivo de la lista
     * @param Nombre del archivo a eliminar
     * @return true si se elimino el archivo de la lista
     */
    public boolean removeFileFromList(String file)
    {
        return files.removeElement(file);
    }

    /** Obtiene una copia del vector de archivos temporales
     * @return una copia del Vector de archivos temporales
     */
	@SuppressWarnings("unchecked")
    public Vector<String> getFiles()
    {
        return (Vector<String>)files.clone();
    }
    
}
