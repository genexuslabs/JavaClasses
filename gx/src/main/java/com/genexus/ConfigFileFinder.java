// $Log: ConfigFileFinder.java,v $
// Revision 1.1  2002/01/30 18:47:00  gusbro
// Initial revision
//
// Revision 1.1.1.1  2002/01/30 18:47:00  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import java.io.*;
import com.genexus.util.*;

public class ConfigFileFinder
{
	private static final String cryptoCfg = "crypto.cfg";

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
						is = new BufferedInputStream(new FileInputStream(fileName));
						if	(is != null)
						{
							crypto = new BufferedInputStream(new FileInputStream(cryptoCfg));
						}
					}
					catch (FileNotFoundException e2) { ; }
				}
		}

		if	(is == null)
		{
			if	(ApplicationContext.getInstance().isGXUtility())
			{
				try
				{
					is = new FileInputStream(fileName);
					if	(is != null)
					{
						crypto = new FileInputStream(fileName);
					}
				}
				catch (IOException e)
				{
				}
			}
		}

		IniFile iniFile = null;

		try
		{
			iniFile = new IniFile(is);
		}
		catch (IOException e)
		{
			if	(ApplicationContext.getInstance().isGXUtility())
			{
				iniFile = new IniFile(fileName);
			}
			else
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
		}

		iniFile.setEncryptionStream(crypto);

		return iniFile;
	}
}