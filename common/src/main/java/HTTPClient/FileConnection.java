package HTTPClient;
import java.net.*;
import java.io.*;

/**
 * Esta clase hace un proxy sobre los pedidos de HTTP y lee directamente sobre
 * la estructura de archivos.
 * El constructor toma como parametros una URL con el protocolo FILE, y almacena el
 * disco source (que puede ser un Shared Network), o sea, los pedidos luego se deben
 * hacer con la ruta completa.
 * NOTA: Las URL son de este estilo:
 *        FILE://\\networked\dir1\dir2
 *        FILE://e:\dir1\dir2
 * NOTA2: Tambien se permite que se comienze con FILE:///, o sea, con 3 barras
 * en vez de 2, porque los browsers lo hacen as�.
 * Ejemplo: URL= FILE:///e:\dir1\dir2
 *
 */
public class FileConnection extends HTTPConnection
{
    private String completeFilesDir; // Ruta completa (drive + dir) del 'server' --> Si el fileServer NO es un network Drive, el filesDir solo contiene el nombre del drive (pero NO el path)
    private String filesDir;
    private static char alternateSeparator = File.separatorChar == '\\' ? '/' : '\\';
    /** Ver la descripci�n de la clase para ver el formato de la URL
    */
    public FileConnection(URL dirUrl) throws IOException
    {
        super("FileConnection");

        if(!dirUrl.getProtocol().equalsIgnoreCase("file"))throw new IOException("FileConnection needs 'FILE' protocol");
        File tempFile;
        if(dirUrl.getHost().startsWith("\\\\"))
        { // Si se trata de un Shared Network
            completeFilesDir = dirUrl.getHost() + dirUrl.getFile().replace(alternateSeparator, File.separatorChar);
            filesDir = dirUrl.getHost() + File.separatorChar;
            // En el Shared Network no se chequea que exista, porque en s� mismo NO existe, s�lo existe con path inclu�do
        }
        else if(dirUrl.getFile().replace('/', '\\').startsWith("\\\\"))
        { // En algunos casos el URL queda todo en el File (depende de la VM), asi que chequeo desde aca
            completeFilesDir = dirUrl.getFile().replace(alternateSeparator, File.separatorChar);
//            filesDir = "\\\\" + (completeFilesDir + "\\").substring(2, (completeFilesDir.substring(2) + "\\").indexOf('\\') + 3);
            filesDir = completeFilesDir + File.separatorChar;
        }
        else
        {  // En el caso en que no sea un Shared Network Dir
            if(!(tempFile = new File(dirUrl.getHost() + ":" + dirUrl.getFile())).isDirectory())throw new IOException( dirUrl.getFile() + " is NOT a directory");
            else completeFilesDir = tempFile.getAbsolutePath().replace(alternateSeparator, File.separatorChar);
            filesDir = dirUrl.getHost() + ":" + File.separator;  // Lo que guardo aca es el Drive del host
        }
    }

    /** En File viene la ruta completa al archivo (sin inclu�r el drive host)
     * @param file ruta completa al archivo (sin inclu�r el drive host)
     *
     */
    public HTTPResponse Get(String file, String query, NVPair[] headers) throws IOException, ModuleException
    {
        if(file.startsWith("/"))file = file.substring(1);
        return new FileResponse(filesDir + file);
    }
}

class FileResponse extends HTTPResponse
{
    private FileInputStream inp = null;
    private long size;
    private String file;
    private byte Data [] = null;

    public FileResponse(String file) throws IOException
    {
        //super(new HTTPClientModule[0], 0, null);
        this.file = file.replace('/', '\\');
        if(!new File(this.file).canRead())throw new IOException(file + " not found");
        size = new File(this.file).length();
    }

    public InputStream getInputStream()throws IOException
    {
        if(inp == null) inp = new FileInputStream(file);
        return inp;
    }

    public synchronized byte [] getData() throws IOException
    {
        if(Data != null)return Data;
        	Data = new byte[(int)size];
        if(inp == null)
			try {
				getInputStream().read(Data);
			} finally {
				getInputStream().close();
			}
        return Data;
    }

    synchronized boolean handleResponse()  throws IOException, ModuleException
    {
        return false;
    }
}
