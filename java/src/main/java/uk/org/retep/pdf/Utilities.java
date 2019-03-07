// $Log: Utilities.java,v $
// Revision 1.1  2001/08/09 18:37:20  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/08/09 18:37:20  gusbro
// GeneXus Java Olimar
//
package uk.org.retep.pdf;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/** Esta clase provee de metodos �tiles para varias clases del GXWS
 */

public class Utilities
{
		private static String predefinedSearchPath = ""; // Contiene los predefinedSearchPaths
        private static final String [] searchEnvironmentVars = {
            "java.library.path",
            "sun.boot.library.path",
            "java.class.path",
            "com.ms.sysdir"
        };
		/** Agrega una lista de paths de b�squeda predefinidos
		 *  Por ejemplo en MS se podr�a pasarle algunos paths con SystemInformation
		 * @param predefinedPaths Array de Strings con los paths predefinidos
		 */
		public static final void addPredefinedSearchPaths(String [] predefinedPaths)
		{
			String predefinedPath = "";
			for(int i = 0; i < predefinedPaths.length; i++)
				predefinedPath += predefinedPaths[i] + ";";
			predefinedSearchPath = predefinedPath + predefinedSearchPath; // SearchPath= los viejos m�s los nuevos
		}

                public static final String getPredefinedSearchPaths()
                {
                    return predefinedSearchPath;
                }

	/** Trata de obtener el Full Path de un archivo
        *   @param nombre del archivo a obtener full Path
        *   @return Full path del archivo o una excepcion IOException si no lo encuentra
        */
        public static String findFileInPath(String filename) throws IOException
        {
            File tempFile;
            Enumeration enumera;
			String tempStr = "";
			// Primero veo si el archivo que me pasaron como par�metro existe 'como me lo pasaron'
			if((tempFile = new File(filename)).exists())return tempFile.getCanonicalPath();
			else filename = tempFile.getName();

			// Ahora lo busco entre las predefinedSearchPath
				enumera = parseLine(predefinedSearchPath, ";").elements();
				while(enumera.hasMoreElements())
					for(tempStr = (tempStr = (String)enumera.nextElement()).endsWith(File.separator) ? tempStr.substring(0, tempStr.length() - 1) : tempStr;
						tempStr != null;
						tempStr = new File(tempStr).getParent())
							if((tempFile = new File(tempStr + File.separator + filename)).isFile())return tempFile.getCanonicalPath();

			// Ahora busco en los searchEnvironmentVars
			for(int i = 0; i < searchEnvironmentVars.length; i++)
			{
			// Busco en la ruta y en todo el path hacia abajo (en todos los parents)
				enumera = parseLine(System.getProperty(searchEnvironmentVars[i], ""), ";").elements();
				while(enumera.hasMoreElements())
					for(tempStr = (tempStr = (String)enumera.nextElement()).endsWith(File.separator) ? tempStr.substring(0, tempStr.length() - 1) : tempStr;
						tempStr != null;
						tempStr = new File(tempStr).getParent())
							if((tempFile = new File(tempStr + File.separator + filename)).isFile())return tempFile.getCanonicalPath();
			}
            throw new FileNotFoundException(filename);
        }


    private static final int CHUNK_SIZE = 16384;

    /** Copia un archivo
     * @param nameIn Nombre del archivo a copiar
     * @param nameOut Nombre del archivo copiado
     * @return boolean True si la copia se realizo con exito
     */
    public static boolean copyFile(String nameIn, String nameOut)
    {
        FileOutputStream out;
        BufferedInputStream in;
        try
        {
            in = new BufferedInputStream(new FileInputStream(nameIn));
            out = new FileOutputStream(nameOut);
        }catch(IOException openError)
        {
            return false;
        }
        int size;
        byte [] b= new byte[CHUNK_SIZE];
        try
        {
            while((size = in.read(b))!= -1)
                out.write(b, 0, size);
            in.close();
            out.close();
         }catch(IOException e)
         {
            return false;
         }
         try
         {
              in.close();
              out.close();
         }catch(Exception e){}
         return true;
    }

	/** Obtiene el archivo del zipFile
	 * @param zipName nombre del archivo ZIP
	 * @param fileName Nombre del archivo a obtener
	 * @param outFileName Nombre del archivo a crear
	 */
	public static void copyFromZipFile(String zipName, String fileName, String outFileName) throws Exception
	{
         File outFile;
         if(! (outFile = new File(outFileName)).isFile())
         {
			 ZipFile zipFile = new ZipFile(zipName);
			 try
			 {
				 ZipEntry entry = zipFile.getEntry(fileName);
				 if(entry == null) {
					 zipFile.close();
					 throw new Exception("Copy Error: " + fileName + " not found");
				 }
				 DataInputStream in = new DataInputStream(zipFile.getInputStream(entry));
				 FileOutputStream out = new FileOutputStream(outFile);
				 byte b[] = new byte[CHUNK_SIZE];
				 int size;
				 while((size = in.read(b))!= -1)out.write(b, 0, size);
				 in.close();
				 out.close();
				 zipFile.close();
			 }catch(Exception e)
			 {
				 zipFile.close();
				 throw e;
			 }
        }
		return;
	}

	public static void copyFromStream(InputStream in, File outFilename)throws Exception
	{
		try
		{
			FileOutputStream out = new FileOutputStream(outFilename);
			int size;
			byte b[] = new byte[CHUNK_SIZE];
			while((size = in.read(b) )!= -1)out.write(b, 0, size);
			in.close();
			out.close();
		}catch(Exception e)
		{
			in.close();
			throw e;
		}
	}

	public static void beep()
	{
        try
        {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }catch(Throwable e) { ; }
	}

        private static final char ALTERNATE = '-';
        /** Retorna el nombre v�lido de archivo convirtiendo las \\ y / a - y eliminando los espacios */
        public static String setValidFilename(String filename)
        {
          return filename.replace('\\', ALTERNATE).replace('/', ALTERNATE).trim();
        }

	/** Separa partes de una lines y las retorna en un Vector. Las partes pueden estar encerradas por comillas
	 * @param line linea a separar
	 * @param separator String conteniendo el separador de partes
	 * @return Vector de Strings conteniendo las partes separadas
	 */
	public static Vector parseLine(String line, String separator)
	{
		Vector partes=new Vector();
		int index = 0,offset = 0;
		int indexComillas;
		boolean startingComillas = true;
		if(line==null)return partes;
		if(!line.endsWith(separator))line+=separator;
		if((indexComillas = line.indexOf('\"')) == -1)indexComillas = Integer.MAX_VALUE;
		while((index=line.indexOf(separator,startingComillas ? offset : indexComillas))!=-1)
		{
			if(index > indexComillas)
			{
				if((indexComillas = line.indexOf('\"', index)) == -1)indexComillas = Integer.MAX_VALUE;
				if(startingComillas)
				{
					startingComillas = false;
					offset++;
					if(indexComillas == Integer.MAX_VALUE)break;
					else continue;
				}
				else startingComillas = true;
				index--;
			}
			partes.addElement(line.substring(offset,index));
			offset=index;
			while(line.startsWith(separator,++offset)&&offset<line.length()); // Elimino separadores seguidos
		}
		if(!startingComillas)  // Si faltan las comillas de cierre, igual pongo esa parte
			partes.addElement(line.substring(offset, line.length() - separator.length()));
		return partes;
	}

}
