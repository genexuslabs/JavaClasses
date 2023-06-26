package com.genexus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.internet.GXInternetConstants;
import com.genexus.platform.NativeFunctions;
import com.genexus.util.Codecs;
import com.genexus.util.IniFile;

public final class PrivateUtilities
{

	public static String getCurrentThreadId()
	{
		return NativeFunctions.getInstance().getThreadId();		
	}

	public static String addExtension(String name, String extension)
	{
		if	(name.indexOf('.') < 0)
			return name + "." + extension;
		return name;
	}

	public static String quoteString(String in, boolean entities8bit)
	{
		return CommonUtil.quoteString(in, entities8bit, true);
	}

	public static String quoteString(String in, boolean entities8bit, boolean encodeQuotes)
	{
		return CommonUtil.quoteString(in, entities8bit, encodeQuotes);
	}


	public static String quoteString(String in)
	{
		return quoteString(in, false);
	}

	public static String encodeURL(String url)
	{
		try
		{
			return java.net.URLEncoder.encode(url, "UTF8").replaceAll("\\+", "%20"); 
		}
		catch (java.io.UnsupportedEncodingException e)
		{
			System.out.println("URLEncoder unsupported encoding in encodeURL function");			
		}
		return "";
	}
	
	public static boolean containsNoAsciiCharacter(String input)
	{
		if (!input.equals(""))
		{
			for (int i = 0; i < input.length(); i++)
			{
				char c = input.charAt(i);
				if (((int)c) > 127 || c == '"')
					return true;
			}
		}
		return false;		
	}
	
	public static String encodeJSConstant(String in)
	{
		return com.genexus.CommonUtil.encodeJSON(in);
	}

	public static String toBase64(String str)
	{
		try
		{
			return new String(Codecs.base64Encode(str.getBytes("UTF8")));
		}catch(Exception e)
		{
			System.err.println("[toBase64 String]: " + e.toString());
			return "";
		}		
	}
	
	public static String fromBase64(String str)
	{
		try
		{
			return new String(Codecs.base64Decode(str.getBytes("UTF8")), "UTF8");
		}catch(Exception e)
		{
			System.err.println("[fromBase64 String]: " + e.toString());
			return "";
		}		
	}

	public static String encodeString(String str)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DeflaterOutputStream zip = new DeflaterOutputStream(out);
			zip.write(str.getBytes("UTF8"));
			zip.close();
			return new String(Codecs.base64Encode(out.toByteArray()));
		}catch(Exception e)
		{
			System.err.println("[Encode String]: " + e.toString());
			return "";
		}		
	}
	
	public static String decodeString(String str)
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(Codecs.base64Decode(str.getBytes()));
			InflaterInputStream zip = new InflaterInputStream(in);
			String out = new String(readToByteArray(zip), "UTF8");
			zip.close();
			return out;
		}
		catch(Exception e)
		{
			System.err.println("[Decode String]: " + e.toString());
			return "";
		}		
	}
	
	public static String stripAccents(String s) 
	{
		s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return s;
	}

	public static String encodeFileName(String str)
	{	
		//We should NOT encodeURL the fileName.
		return PrivateUtilities.encodeURL(stripAccents(str.replaceAll("\\\\|/|\\||:|\\?|\\*|\"|<|>|\\p{Cntrl}", "_")));				
	}
	
	 /**    
     * @deprecated The resulting string may vary depending on the platform's
     *             default encoding. Instead, use the encode(String,String)
     *             method to specify the encoding.
     * @return  the translated <code>String</code>.
     */
	public static String URLEncode(String parm)
	{
		return URLEncode(parm, "");
	}
	
	public final static String URLEncode(String parm, String encoding)
    {
		return CommonUtil.URLEncode(parm, encoding);
    }
	
	public static String encodeStaticParm(String parm)
	{
		return URLEncode(parm, "UTF8");
	}

	public static String getJDBC_DRIVER(IniFile iniFile, String section)
	{
		String driver = iniFile.getProperty(section, "JDBC_DRIVER", "");
		if	(driver.equals("CUSTOM"))
		{
			driver = iniFile.getProperty(section, "JDBC_CUSTOM_DRIVER", "");
		}

		return driver;
	}

	public static String getSystemProperty(String property, String defa)
	{
		String out = System.getProperty(property);
		if	(out == null)
			return defa;

		return out;
	}

	public static byte setSystemProperty(String property, String value)
	{
		System.getProperties().put(property, value);
		return 0;
	}

	public static String getStackTraceAsString(Throwable e) 
	{
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    PrintWriter writer = new PrintWriter(bytes, true);
	    e.printStackTrace(writer);
	    return bytes.toString();
	}



	public static String getPackageName(Class packageClass)
	{
		return getPackageName(packageClass.getName());
	}

	static Random random;
	static
	{
		random = new Random(System.currentTimeMillis());
	}

	public static String getTempFileName(String extension)
	{
		if(extension.trim().length() != 0)
		{
			extension = "." + extension;
		}
		String fileName;
		do
		{
			fileName  = java.util.UUID.randomUUID().toString() + extension;
		}
		while (new File(fileName).exists());
	
		return fileName;
	}	
	
	public static String getTempFileName(String baseDir, String name, String extension)
	{
		return getTempFileName(baseDir, name, extension, false);
	}	
	
	public static String getTempFileName(String baseDir, String name, String extension, boolean encodeName)
	{
		extension = getTempFileName(extension.trim());
		name = name.trim().toLowerCase();
		String separator = name.length() > 0 ? "_": "";
		if (encodeName)
		{
			name = encodeFileName(name);
		}
		name = checkFileNameLength(baseDir, name, extension);
		return GXutil.getNonTraversalPath(baseDir, String.format("%s%s%s", name, separator, extension));
	}
	
	public static String checkFileNameLength(String baseDir, String fileName, String extension ) 
	{
		int pathLength;
		int fileNameLength = fileName.length();
		int extensionLength = extension.length();
		if (org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS)
		{
			pathLength = baseDir.length() + fileNameLength + extensionLength;
			if (pathLength > 260)
			{
				fileName = fileName.substring(fileNameLength + baseDir.length() - 260 + extensionLength, fileNameLength);
			}
		}
		else
		{
			pathLength = fileNameLength + extensionLength;
			if (pathLength > 255) {
				fileName = fileName.substring(fileNameLength - 255 + extensionLength, fileNameLength);
			}
		}
		return fileName;
	}

	public static String getPackageName(String className)
	{
		if	(className.indexOf('.') < 0)
			return "";

		return className.substring(0, className.lastIndexOf('.'));
	}



	
	public static String removeQuotes(String fileName)
	{
		if	(fileName.length() > 0)
		{
			if	(fileName.charAt(0) == '"')
				return fileName.substring(1, fileName.length() -1 );
		}

		return fileName;
	}

	public static char getMnemonic(String text)
	{
		int keyPos;

		if	((keyPos = text.lastIndexOf("&")) >= 0 && keyPos < text.length() - 1)
		{
			return text.charAt(keyPos + 1);
		}

		return ' ';
	}

	public static String trimSpaces (String text)
	{
		String trimText  = text.trim();
		String stringRet = "";

		int i = trimText.indexOf(' ',1);

		while ( i != -1 )
		{
			stringRet += trimText.substring(0, i);
			trimText = trimText.substring(i + 1, trimText.length()).trim();
			i = trimText.indexOf(' ', 1);		
		}

		return (stringRet + trimText);
	}


	public static String removeMnemonicKey(String text)
	{
		int keyPos;

		if	((keyPos = text.lastIndexOf('&')) >= 0 && keyPos < text.length() - 1)
		{
			if	(keyPos == 0)
				return text.substring(1);

			return text.substring(0, keyPos ) + text.substring(keyPos + 1, text.length());
		}
		
		return text;
	}

	public static void printTrace()
	{
		try
		{
			throw new Exception();
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
	}

	/** Muestra las primeras lineas del stacktrace actual
	 * @param msg Mensaje a poner como header
	 * @param lines Cantidad de lineas a mostrar
	 */
	public static void printStackTrace(String msg, int lines)
    {
      try
      {
        throw new Error(msg);
      }catch(Throwable e)
      {
        try
        {
          ByteArrayOutputStream temp = new ByteArrayOutputStream();
          e.printStackTrace(new PrintStream(temp, true));
          temp.close();
          BufferedReader temp2 = new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(temp.toByteArray())));
          temp2.readLine();
          temp2.readLine();
          System.err.println("*** Showing " + lines + " lines of trace [" + msg + "]");
          for(int i = 0; i < lines; i++)
          {
			String line = temp2.readLine();
			if(line == null)
			{
				break;
			}
            System.err.println(line);
          }
          temp2.close();
        }catch(Throwable e2){ ; }
      }
    }

	public static boolean setFieldValue(Object instance, String fieldName, Object newValue)
	{
		try
		{
			Field f = getField(instance, fieldName);
			if(f == null)return false;
			try
			{
				f.getClass().getMethod("setAccessible", new Class[]{boolean.class}).invoke(f, new Object[]{new Boolean(true)});
			}catch(Exception ignore){;}
			f.set(instance, newValue);
			return true;
		} catch(Throwable e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public static Object getFieldValue(Object instance, String fieldName)
	{
		try
		{
			Field f = getField(instance, fieldName);
			if (f == null) return null;
			try
			{
				f.getClass().getMethod("setAccessible", new Class[]{boolean.class}).invoke(f, new Object[]{new Boolean(true)});
			}catch(Exception ignore){;}
			return f.get(instance);
		} catch(Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static Object getFieldTarget_parts(Object instance, String fieldName, String[] split, int s) throws Exception
	{
		Class c = instance.getClass();

		// Busco el field		
		Field[] fieldList = c.getDeclaredFields();
		if ( s < split.length)
		{
			for(int i = 0; i < fieldList.length; i++)
			{
				if(fieldList[i].getName().equals(split[s]))
				{
					instance = PrivateUtilities.getFieldValue(instance, split[s]);
					if (split.length < 3)
					{
						return instance;
					}
					else
					{
						return getFieldTarget_parts( instance, split[s + 1], split, s + 1);
					}							
				}
			}
		}
		return null;
	}

	public static Field getField_parts(Object instance, String fieldName, String[] split, int s) throws Exception
	{
		Class c = instance.getClass();

		// Busco el field		
		Field[] fieldList = c.getDeclaredFields();
		if ( s < split.length)
		{
			for(int i = 0; i < fieldList.length; i++)
			{
				if(fieldList[i].getName().equals(split[s]))
				{
					if (split.length < 2)
					{
						// Si el field no fuese publico que le cambiamos el flag de accesss
						return fieldList[i];
					}
					else
					{
						instance = PrivateUtilities.getFieldValue(instance, split[s]);
						return getField_parts( instance, split[s + 1], split, s + 1);
					}							
				}
			}
		}
		return null;
	}

	public static Object getFieldTarget(Object instance, String fieldName) throws Exception
	{
		if (fieldName != null)
		{
			String[] split = fieldName.split("\\.");
			Class c = instance.getClass();

			// Busco el field		
			while(c != null)
			{
				Field[] fieldList = c.getDeclaredFields();
				for(int i = 0; i < fieldList.length; i++)
				{
					if(fieldList[i].getName().equals(split[0]))
					{
						instance = PrivateUtilities.getFieldValue(instance, split[0]);
						if (split.length < 3)
						{
							// Si el field no fuese publico que le cambiamos el flag de accesss
							return instance;
						}
						else
						{
							return getFieldTarget_parts( instance, fieldName, split, 0);
						}							
					}
				}
				c = c.getSuperclass();

				if (c.getSimpleName().equals("GXRestServiceWrapper")) {
					return null;
				}
			}
		}
		return null;
	}

	public static Field getField(Object instance, String fieldName) throws Exception
	{
		String[] split = fieldName.split("\\.");
		Class c = instance.getClass();

		// Busco el field		
		while(c != null)
		{
			Field[] fieldList = c.getDeclaredFields();
			for(int i = 0; i < fieldList.length; i++)
			{
				if(fieldList[i].getName().equals(split[0]))
				{
					if (split.length < 2)
					{
						// Si el field no fuese publico que le cambiamos el flag de accesss
						return fieldList[i];
					}
					else
					{
						return getField_parts( instance, fieldName, split, 0);
					}							
				}
			}
			c = c.getSuperclass();

			if (c.getSimpleName().equals("GXRestServiceWrapper")) {
				return null;
			}
		}
		return null;
	}
	
	public static void waitKey()
	{
		try
		{
			System.in.read();
			while (System.in.available() != 0)
			{
				System.in.read();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
	}

	public static void msg(String sText)
	{
		System.err.println(sText);
	}

	public static void errorHandler(String text, Exception ex) 
	{
		System.err.println(Thread.currentThread() + ": " + getStackTraceAsString(ex));

		msg(Thread.currentThread() + " - " + text + " : " + ex.getMessage());
		SpecificImplementation.Application.exit();
	}


   /**
	* File.getParent() can return null when the file is specified without
	* a directory or is in the root directory. This method handles those cases.
	*
	* @param f the target File to analyze
	* @return the parent directory as a File
	*/
	private static File parent(File f) {
		String dirname = f.getParent();
		if (dirname == null) {
			if (f.isAbsolute()) return new File(File.separator);
			else return new File(System.getProperty("user.dir"));
		}
		return new File(dirname);
	}

	/**
	 * <pre>
	 * Checks if a string is an absolute path to local file system.
	 * Null safe.
	 * </pre>
	 */
	public static boolean isAbsoluteFilePath(String path) {
		try {
			return Paths.get(path).isAbsolute();
		} catch (InvalidPathException | NullPointerException ex) {
			return false;
		}
	}

	public static String addLastPathSeparator(String dir)
	{
		return addLastChar(dir, File.separator);
	}

	public static String addLastChar(String dir, String lastChar)
	{
		if	(!dir.equals("") && !dir.endsWith(lastChar))
			return dir + lastChar;
		
		return dir;
	}

	public static void copyFile(File src, File dest) throws IOException 
	{

		FileInputStream source = null;
		FileOutputStream destination = null;
		byte[] buffer;
		int bytes_read;

		// Make sure the specified source exists and is readable.
		if (!src.exists())
			throw new IOException("source not found: " + src);
		if (!src.canRead())
			throw new IOException("source is unreadable: " + src);

		if (src.isFile()) {
			if (!dest.exists()) {
                File parentdir = parent(dest);
                if (!parentdir.exists())
					parentdir.mkdir();
			}
			else if (dest.isDirectory()) {
				dest = new File(dest + File.separator + src);
			}
		}
		else if (src.isDirectory()) {
			if (dest.isFile())
				throw new IOException("cannot copy directory " + src + " to file " + dest);

			if (!dest.exists())
				dest.mkdir();
		}
		
		// The following line requires that the file already
		// exists!!  Thanks to Scott Downey (downey@telestream.com)
		// for pointing this out.  Someday, maybe I'll find out
		// why java.io.File.canWrite() behaves like this.  Is it
		// intentional for some odd reason?
		//if (!dest.canWrite())
			//throw new IOException("destination is unwriteable: " + dest);

		// If we've gotten this far everything is OK and we can copy.
		if (src.isFile()) {
			try {
	            source = new FileInputStream(src);
		        destination = new FileOutputStream(dest);
			    buffer = new byte[1024];
				while(true) {
	                bytes_read = source.read(buffer);
		            if (bytes_read == -1) break;
			        destination.write(buffer, 0, bytes_read);
				}
			}
	        finally {
		        if (source != null) 
			        try { source.close(); } catch (IOException e) { ; }
				if (destination != null) 
	                try { destination.close(); } catch (IOException e) { ; }
		    }
		}
		else if (src.isDirectory()) {
			String targetfile, target, targetdest;
			String[] files = src.list();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
		
				targetfile = files[i];
				target = src + File.separator + targetfile;
				targetdest = dest + File.separator + targetfile;


				if ((new File(target)).isDirectory()) {
		 			copyFile(new File(target), new File(targetdest));
				}
				else {

					try (FileInputStream fis = new FileInputStream(target)) {
						destination = new FileOutputStream(targetdest);
						buffer = new byte[1024];
					 
						while(true) {
							bytes_read = fis.read(buffer);
							if (bytes_read == -1) break;
							destination.write(buffer, 0, bytes_read);
						}
					}
					finally {
						if (destination != null) 
							try { destination.close(); } catch (IOException e) { ; }
					}
				}
			}
			}
		}
	}

	public static void copyFileRetry(String src, String dest)
	{
		FileInputStream source = null;
		FileOutputStream destination = null;
		byte[] buffer;
		int bytes_read;

		while (true)
		{
			try 
			{
		        destination = new FileOutputStream(dest);
				break;
			}
			catch (IOException e)
			{
			}
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException ie)
			{
			}

			System.out.println("Retrying copy of " + dest);
		}
		try
		{
            source = new FileInputStream(src);
		    buffer = new byte[4096];
			while(true) {
                bytes_read = source.read(buffer);
	            if (bytes_read == -1) break;
		        destination.write(buffer, 0, bytes_read);
			}
		}
		catch (IOException e)
		{
			System.out.println("Error " + e.getMessage());
		}
        finally {
	        if (source != null) 
		        try { source.close(); } catch (IOException e) { ; }
			if (destination != null) 
                try { destination.close(); } catch (IOException e) { ; }
	    }
	}
	public static String BOMInputStreamToStringUTF8(InputStream istream) {
		if (istream == null) {
			throw new IllegalArgumentException("BOMInputStreamToStringUTF8 -> Input stream can't be null");
		}
		boolean firstLine = true;
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(istream, "UTF8"))) {
			for (String s = ""; (s = r.readLine()) != null; ) {
				if (firstLine) {
					s = removeUTF8BOM(s);
					firstLine = false;
				}
				stringBuilder.append(s + GXInternetConstants.CRLFString);
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			System.err.println("Error reading stream:" + e.getMessage());
			return "";
		}
	}
	static final String UTF8_BOM = "\uFEFF";
	private static String removeUTF8BOM(String s) {
		if (s.startsWith(UTF8_BOM)) {
			s = s.substring(1);
		}
		return s;
	}

	public static void InputStreamToFile(InputStream source, String fileName)
					{
						if	(source == null)
						{
							throw new IllegalArgumentException("InputStreamToFile -> Input stream can't be null");
						}

						byte[] buffer;
						int bytes_read;
						OutputStream destination = null;
						try
						{
							destination = new BufferedOutputStream(new FileOutputStream(fileName));
							buffer = new byte[1024];

							while (true)
							{
								bytes_read = source.read(buffer);
								if (bytes_read == -1) break;
								destination.write(buffer, 0, bytes_read);
							}
						}
						catch (IOException e)
						{
							System.err.println("Error writing file " + fileName + ":" + e.getMessage());
						}
						finally
						{
							if (source != null)
								try { source.close(); } catch (IOException e) { ; }
			if (destination != null) 
				try {destination.close(); } catch (IOException e) { ; }
		}
	}
	
	public static byte remoteFileExists(String URLName){
	    try {	      
	      HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
	      con.setRequestMethod("HEAD");
	      return (con.getResponseCode() == HttpURLConnection.HTTP_OK)?(byte)1:0;
	    }
	    catch (Exception e) {	       
	       return 0;
	    }
	  }


	public static boolean hasExtension(String fileName, String extension)
	{
		return fileName.toLowerCase().endsWith(extension.toLowerCase());
	}

    /**
     * Return the extension portion of the file's name .
     *
     * @see #getExtension
     * @see FileFilter#accept
     */
     public static String getExtension(String fileName) 
     {
		if (fileName != null) 
		{
		    int i = fileName.lastIndexOf('.');

		    if (i >= 0)
				return fileName.substring(i + 1);
		}
		return null;
    }

	static final long day = 1000*60*60*24;

	public static boolean isNullDate(java.util.Date date, java.util.Date nullDate)
	{
		long dif = date.getTime() - nullDate.getTime();
		dif = dif < 0?-dif:dif;

		return (dif < day) ;
	}


	public static String setExtension(String fileName, String extension)
	{
		int indexOf = fileName.lastIndexOf('.');
		String name;

		if	(indexOf < 0)
			name = fileName;
		else
			name = fileName.substring(0, indexOf);

		// HACK: Workaround de un bug en la VM 3167 de MS.
		if	(name.charAt(name.length() - 1) != '.') name += ".";

		return name + extension; //fileName.substring( 0, fileName.length() - fileName.lastIndexOf('.')) + extension;
	}

	public static String removeExtension(String fileName)
	{
		if	(fileName.indexOf('.') == -1)
			return fileName;

		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

    public static final String readLine(InputStream in) throws IOException 
    {
		char lineBuffer[] = null;
		char buf[] = lineBuffer;

		if (buf == null) 
		{
	    	buf = lineBuffer = new char[128];
		}

		int room = buf.length;
		int offset = 0;
		int c;

loop:	while (true) 
		{
    		switch (c = in.read()) 
    		{
		   		case -1: 
	      		case '\n':
				break loop;

	      		case '\r':
				int c2 = in.read();

				if (c2 != '\n') 
				{
		    		if (!(in instanceof PushbackInputStream)) 
		    		{
						in = new PushbackInputStream(in);
		    		}
		    		((PushbackInputStream)in).unread(c2);
				}
				break loop;

	      		default:
					if (--room < 0) 
					{
		    			buf = new char[offset + 128];
		    			room = buf.length - offset - 1;
		    			System.arraycopy(lineBuffer, 0, buf, 0, offset);
		    			lineBuffer = buf;
					}

					buf[offset++] = (char) c;
					break;
	    	}
		}
		if ((c == -1) && (offset == 0)) 
		{
	    	return null;
		}
		return String.copyValueOf(buf, 0, offset);
    }


	/**
     	A fixed version of java.io.InputStream.read(byte[], int, int).  The
     	standard version catches and ignores IOExceptions from below.
     	This version sends them on to the caller.
	*/

    public static int read( InputStream in, byte[] b, int off, int len ) throws IOException
        {
        if ( len <= 0 )
            return 0;
        int c = in.read();
        if ( c == -1 )
            return -1;
        if ( b != null )
            b[off] = (byte) c;
        int i;
        for ( i = 1; i < len ; ++i )
            {
            c = in.read();
            if ( c == -1 )
                break;
            if ( b != null )
                b[off + i] = (byte) c;
            }
        return i;
        }

	public static byte[] readToByteArray(InputStream in) throws IOException
	{
     	byte[] output    = new byte[0];
     	byte[] newBuffer = new byte[2048];
		int size;
		
		while ((size = readFully(in, newBuffer, 0, 2048)) > 0)
		{
	    	byte tmpBuffer[] = new byte[output.length + size];

			// Paso del realbuffer al temp
		    System.arraycopy(output, 0, tmpBuffer, 0, output.length);

			// Paso del nuevo al temp
		    System.arraycopy(newBuffer, 0,  tmpBuffer, output.length, size);

			// Dejo el temp en realBuffer
			output = tmpBuffer;
		}

		return output;
	}

    /** A version of read that reads the entire requested block, instead
     	of sometimes terminating early.
     	@return -1 on EOF, otherwise len
	*/

    public static int readFully( InputStream in, byte[] b, int off, int len ) throws IOException
    {
		int l, r;

		for ( l = 0; l < len; )
	    {
	    	r = read( in, b, l, len - l );
	    	if ( r == -1 )
				return l;

	    	l += r;
	    }

		return len;
	}

    /// Dump out the current call stack onto System.err.
    public static void dumpStack()
	{
		(new Throwable()).printStackTrace();
	}
	
	public static boolean isClassPresent(String className) 
	{
		try
		{
		 	Class.forName(className);
			return true;
		}
		catch (Throwable ex)
		{
			return false;
		}
	}

	static final double YIELD_PROBABILITY = 0.001;
	static void myYield() 
	{
		if (Math.random() < YIELD_PROBABILITY) Thread.yield();
	}


    /**
     * Display a file in the system browser.  If you want to display a
     * file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with either "http://" or
     * "file://").
     */
    public static void displayURL(String url)
    {
        boolean windows = isWindowsPlatform();
        String cmd = null;

        try
        {
            if (windows)
            {
            	cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
            	Runtime.getRuntime().exec(cmd);
            }
            else
            {
                // Under Unix, Netscape has to be running for the "-remote"
                // command to work.  So, we try sending the command and
                // check for an exit value.  If the exit command is 0,
                // it worked, otherwise we need to start the browser.

                // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
                cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
                Process p = Runtime.getRuntime().exec(cmd);

                try
                {
                    // wait for exit code -- if it's 0, command worked,
                    // otherwise we need to start the browser up.
                    int exitCode = p.waitFor();

                    if (exitCode != 0)
                    {
                        // Command failed, start up the browser

                        // cmd = 'netscape http://www.javaworld.com'
                        cmd = UNIX_PATH + " "  + url;
                        p = Runtime.getRuntime().exec(cmd);
                    }
                }
                catch(InterruptedException x)
                {
                    System.err.println("Error bringing up browser, cmd='" +
                                       cmd + "'");
                    System.err.println("Caught: " + x);
                }
            }
        }
        catch(IOException x)
        {
            // couldn't exec browser
            System.err.println("Could not invoke browser, command=" + cmd);
            System.err.println("Caught: " + x);
        }
    }

    /**
     * Try to determine whether this application is running under Windows
     * or some other platform by examing the "os.name" property.
     *
     * @return true if this application is running under a Windows OS
     */
    public static boolean isWindowsPlatform()
    {
        String os = System.getProperty("os.name");

        if ( os != null && os.startsWith(WIN_ID))
            return true;
        else
            return false;
    }

    // Used to identify the windows platform.
    private static final String WIN_ID = "Windows";

    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";

    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

    // The default browser under unix.
    private static final String UNIX_PATH = "netscape";

    // The flag to display a url.
    private static final String UNIX_FLAG = "-remote openURL";


	public static void delTree(String directorio) throws IOException
	{
      	File d = new File(directorio);
  		String ent[] = d.list();

  		for (int i = 0; i < ent.length; i++)
      	{ 
      		File k = new File(directorio + "\\" + ent[i]);

        	if (k.isDirectory())
          		delTree(directorio + "\\" + ent[i]);
        	else
			{
        		if	(!k.delete())
					throw new IOException("Can't delete " + k.getName());
			}
  		}

      	if	(!d.delete())
			throw new IOException("Can't delete " + d.getName());
	}	

	public static String replaceString(String in, String patternIn, String patternOut)
	{
		String newFile = "";
		int pos = 0;
		int newPos;

		while ( (newPos = in.indexOf(patternIn, pos)) != -1)
		{
			newFile = newFile + in.substring(pos, newPos) + patternOut;
			pos = newPos + patternIn.length();
		}
		
		newFile = newFile + in.substring(pos);

		return newFile;
	}
}

	class ReadProperties implements Runnable
	{
		String fileName;
		Properties props;

		ReadProperties(String fileName, Properties props)
		{
			this.fileName = fileName;
			this.props    = props;
		}
	
		public void run()
		{
			try (FileInputStream in = new FileInputStream(fileName);)
			{
    			props.load(new BufferedInputStream(in));
			}
			catch (IOException e)
			{
				System.err.println("Can't read properties " + fileName );
			}
		}
	}
