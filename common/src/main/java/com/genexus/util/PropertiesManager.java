package com.genexus.util;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;

import com.genexus.*;

public final class PropertiesManager
{
	private static PropertiesManager instance;

	private Hashtable propertyFiles;

	private PropertiesManager()
	{
		propertyFiles = new Hashtable();
	}

	private static Object handleLock = new Object();	
	
	public static PropertiesManager getInstance()
	{
		if	(instance == null)
			synchronized (handleLock)
			{
				if	(instance == null)
					instance = new PropertiesManager();
			}

		return instance;
	}
	
	public static void endPropertiesManager()
	{
		instance = null;
	}	

	public void flushProperties()
	{
   		for (Enumeration e = propertyFiles.keys(); e.hasMoreElements() ;) 
   		{
			String fileName = (String) e.nextElement();
			try (FileOutputStream outputStream = new FileOutputStream(fileName);)
			{
				((Properties) propertyFiles.get(fileName)).store(outputStream, "");
			}
			catch (IOException ex)
			{
				System.err.println("Exception writing " + fileName);
			}
		}
	}
}