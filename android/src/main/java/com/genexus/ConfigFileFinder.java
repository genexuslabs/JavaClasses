// $Log: ConfigFileFinder.java,v $
// Revision 1.1  2002/01/30 18:47:00  gusbro
// Initial revision
//
// Revision 1.1.1.1  2002/01/30 18:47:00  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import java.util.*;		    
import java.net.URL;		    
import java.io.*;
import com.genexus.util.*;
import com.genexus.platform.*;
import com.artech.base.services.AndroidContext;

public class ConfigFileFinder
{
	static class GetInputStream implements Runnable
	{
		String fileName;
		InputStream is;

		public GetInputStream(String fileName)
		{
			this.fileName = fileName;
		}

		public void run()
		{
			//try
			//{
				is = AndroidContext.ApplicationContext.getResourceStream(fileName, "raw"); //$NON-NLS-1$
				//is = new BufferedInputStream(new URL(Application.getApplet().getCodeBase().toString() + fileName).openStream());
			//}
			//catch (IOException e)
			//{
			//}
		}

		public InputStream getInputStream()
		{
			return is;
		}
	}

	private static final String cryptoCfg = "crypto.cfg";
	
	private static String prependDirectory = "";
	/** Agrega una ruta de prepend donde buscar un cfg como �ltimo recurso
	 *  Si el prepend es un archivo, lo abre como ultimo recurso, y sino intenta
	 * abrir el prepend + fileName pasado al getConfigFile
	 */
	public static void setConfigFilePrepend(String prepend)
	{
		prependDirectory = prepend;
	}

	public static IniFile getConfigFile(Class resourceClass, String fileName, Class defaultResourceClass) 
	{
		InputStream is 	   = null;
		InputStream crypto = null;
		
		if	(is == null && resourceClass != null)
		{
			is = ResourceReader.getResourceAsStream(resourceClass, fileName);
			if	(is != null)
			{
				crypto = ResourceReader.getResourceAsStream(resourceClass, cryptoCfg);
			}
		}
		
		// Esto es para soportar que los programas de GX seteen donde
		// tiene que estar el .cfg

		if	(is == null && defaultResourceClass != null)
		{
			is = ResourceReader.getResourceAsStream(defaultResourceClass, fileName);
			if	(is != null)
			{
				crypto = ResourceReader.getResourceAsStream(resourceClass, cryptoCfg);
			}
		}																	  

		if	(is == null)
		{
			//if	(Application.getApplet() == null)
			//{
				try
				{
					is = new BufferedInputStream(new FileInputStream(fileName));
					if	(is != null)
					{
						crypto = new BufferedInputStream(new FileInputStream(cryptoCfg));
					}
				}
				catch (FileNotFoundException e)
				{
					try
					{
						String temp = new File(prependDirectory).isFile() ? prependDirectory : prependDirectory + fileName;
						is = new BufferedInputStream(new FileInputStream(temp));
						if	(is != null)
						{
							crypto = new BufferedInputStream(new FileInputStream(prependDirectory + cryptoCfg));
						}
					}
					catch (FileNotFoundException e2) { ; }
				}

			//}
			//else
			//{
			//	GetInputStream gi = new GetInputStream(fileName);
			//	try
			//	{
			//		gi.run();
			//	}
			//	catch (SecurityException e)
			//	{
			//		NativeFunctions.getInstance().executeWithPermissions(gi, INativeFunctions.CONNECT);
			//	}
			//	is = gi.getInputStream();
			//	
			//	if	(is != null)
			//	{
			//		gi = new GetInputStream(cryptoCfg);
			//		try
			//		{
			//			gi.run();
			//		}
			//		catch (SecurityException e)
			//		{
			//			NativeFunctions.getInstance().executeWithPermissions(gi, INativeFunctions.CONNECT);
			//		}
			//		crypto = gi.getInputStream();
			//	}
			//}
		}

		IniFile iniFile = null;

		try
		{
			iniFile = new IniFile(is);
		}
		catch (IOException e)
		{
				String userDir;
				try
				{
					userDir = System.getProperty("user.dir") + "\\";
				}
				catch (SecurityException ex)
				{
					userDir = "";
				}

				throw new InternalError("Can't open " + userDir + fileName + " / " + e.getMessage());
		}

		iniFile.setEncryptionStream(crypto);

		return iniFile;
	}
	
	public static IniFile getConfigFile(String fileName) 
	{
		InputStream is 	   = null;
		InputStream crypto = null;
		
		if	(is == null)
		{
			fileName = fileName.replace(".", "_");
			int id = AndroidContext.ApplicationContext.getResource(fileName, "raw"); //$NON-NLS-1$
			if (id != 0)
				is = AndroidContext.ApplicationContext.openRawResource(id);
			else	
				is = AndroidContext.ApplicationContext.getResourceStream(fileName, "raw"); //$NON-NLS-1$
		
		}
				
		IniFile iniFile = null;

		try
		{
			iniFile = new IniFile(is);
		}
		catch (IOException e)
		{
				String userDir;
				try
				{
					userDir = System.getProperty("user.dir") + "\\";
				}
				catch (SecurityException ex)
				{
					userDir = "";
				}

				throw new InternalError("Can't open " + userDir + fileName + " / " + e.getMessage());
		}

		return iniFile;
	}
}