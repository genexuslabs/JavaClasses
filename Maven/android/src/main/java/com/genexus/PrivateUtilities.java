package com.genexus;

import java.io.BufferedInputStream;
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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

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
//		if	(url.equals("''.html"))
//			return "";
		return com.genexus.util.Encoder.encodeURL(url);
	}
	
	public static String encodeJSConstant(String in)
	{
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < in.length(); i++)
		{
			char currentChar = in.charAt(i);

			switch (currentChar)
			{
				case (char) 8:
					// Backspace  
					out.append("\\010"); break;
				case (char) 9:
					// Tab  
					out.append("\\011"); break;
				case (char) 10:
					// New line  
					out.append("\\012"); break;
				case (char) 11:
					// Vertical tab  
					out.append("\\013"); break;
				case (char) 13:
					// Carriage return
					out.append("\\015"); break;
				case (char) 34:
					// Double quote  
					out.append("\\042"); break;
				case (char) 39:
					// Apostrophe or single quote  
					out.append("\\047"); break;
				case (char) 92:
					// Backslash character
					out.append("\\134"); break;
				default:
					out.append( currentChar);
			}
		}
		return out.toString();
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
			String out = new String(CommonUtil.readToByteArray(zip), "UTF8");
			zip.close();
			return out;
		}catch(Exception e)
		{
			System.err.println("[Decode String]: " + e.toString());
			return "";
		}		
	}

	public static String encodeParm(String parm)
	{
		return java.net.URLEncoder.encode(parm);
	}
	
	public static String encodeParm(String parm, String encoding)
	{
		try
		{
			Class encoder = Class.forName("java.net.URLEncoder");
			String encoded = (String) encoder.getMethod("encode",
															  new Class[] { String.class, String.class }).invoke(null, 
																												 new Object[] { parm, encoding });
			return encoded;
		}
		catch(Exception exc)
		{
			System.out.println("ERROR");
			return encodeParm(parm);
		}
	}

	public static String encodeStaticParm(String parm)
	{
		return encodeParm(parm);
		//com.genexus.util.Encoder.encodeParm(parm);
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
			fileName  = ((int) (random.nextDouble() * 10000000)) + extension;
		}
		while (new File(fileName).exists());
	
		return fileName;
	}
	
	public static String getTempFileName(String baseDir, String name, String extension)
	{
		name = name.trim();
		extension = extension.trim();
			
		return baseDir + '/' + name + getTempFileName(extension);
	}

	public static String getPackageName(String className)
	{
		if	(className.indexOf('.') < 0)
			return "";

		return className.substring(0, className.lastIndexOf('.'));
	}


	private String removeQuotes(String fileName)
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
		return CommonUtil.trimSpaces(text);
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

	public static boolean loadModelProperties(String fileName, Properties props)
	{
		try
		{
			InputStream in = ResourceReader.getResourceAsStream(fileName);
			if	(in == null)
			{
				return loadProperties(fileName, props);
			}
			else
			{
	   			props.load(in);
   				in.close();
				return true;
			}

		}
		catch (IOException e)
		{
			System.err.println("Can't read " + fileName);
			return false;
		}
	}

	public static boolean loadProperties(String fileName, Properties props)
	{
		String toOpen = "";

		try 
		{
	 		//if	(Application.getApplet() == null)
			//{
				toOpen = fileName;
	    		FileInputStream in = new FileInputStream(fileName);
	    		props.load(new BufferedInputStream(in));
	    		in.close();
			//}									   
			//else	  
			//{
			//	String url = Application.getApplet().getCodeBase().toString();
			//	if	(url.charAt(url.length() - 1) == '.')
			//		url = url.substring(0, url.length()-1);
	    	//	
			//	URL u = new URL(url + fileName);
			//	toOpen = url + fileName;
	    	//	
			//	if 	(url.substring(0, 4).equals("file"))
			//	{
			//		String fileNew = url.substring(6, url.length());

//url     -file:/H:/M/Jsample/DATA003/-
//urlsub -:/M/Jsample/DATA003/-

			//		fileNew = fileNew.substring(0, 1) + ":/" + fileNew.substring(2) + fileName;
			//		NativeFunctions.getInstance().executeWithPermissions(new ReadProperties(fileNew, props), INativeFunctions.FILE_READ);
			//	}
			//	else
			//	{
			//		BufferedInputStream in = new BufferedInputStream(u.openStream());
    		// 		props.load(new BufferedInputStream(in));
    		// 		in.close();
			//	}
			//}			
					
			return true;
		}
		catch (IOException e) 
		{
			// Chanchadita para que no aparezca el mensaje la primera vez que se ejecuta el 
			// developer menu
			if	(!toOpen.equals("developer.properties"))
				System.err.println("error opening " + toOpen);
			return false;
		}
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
			return f == null ? null : f.get(instance);
		} catch(Throwable e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static Field getField(Object instance, String fieldName)throws Exception
	{
		Class c = instance.getClass();

		// Busco el field		
		while(c != null)
		{
			Field[] fieldList = c.getDeclaredFields();
			for(int i = 0; i < fieldList.length; i++)
			{
				if(fieldList[i].getName().equals(fieldName))
				{// Si el field no fuese publico que le cambiamos el flag de accesss
					return fieldList[i];
				}
			}
			c = c.getSuperclass();
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
		//if	(Application.getApplet() != null)
		//	Application.getApplet().showStatus(sText);
		//else
			System.err.println(sText);
	}


	public static void msgBox( String sText)
	{
	}

	public static void msg(Object panel, String sText)
	{
		System.out.println(sText);
	}

	public static void error(Object panel, String sText)
	{
		System.err.println(sText);
	}

	public static void errorHandler(String text, Exception ex) 
	{
		System.err.println(Thread.currentThread() + ": " + getStackTraceAsString(ex));

		msg(Thread.currentThread() + " - " + text + " : " + ex.getMessage());
		// no exit application in android 
		//Application.exit();
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

	public static String addLastPathSeparator(String dir)
	{
		return CommonUtil.addLastPathSeparator(dir);
	}

	public static String addLastChar(String dir, String lastChar)
	{
		return CommonUtil.addLastChar(dir, lastChar);
	
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

					try {
						source = new FileInputStream(target);
						destination = new FileOutputStream(targetdest);
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

		return fileName.substring(0, fileName.indexOf('.'));
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

  		if (ent != null) {
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
			try
			{
	    		FileInputStream in = new FileInputStream(fileName);
    			props.load(new BufferedInputStream(in));
    			in.close();
			}
			catch (IOException e)
			{
				System.err.println("Can't read properties " + fileName );
			}
		}
	}
