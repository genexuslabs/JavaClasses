
package com.genexus;

import java.io.*;
import java.net.*;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.platform.*;


public class ResourceReader
{
	public static InputStream getFile(String fileName)
	{
		try
		{
			if(ApplicationContext.getInstance().isServletEngine() && !new File(fileName).isAbsolute())
			{ // Si estoy en un motor de servlets y el archivo es relativo, lo voy a buscar bajo el
			  // WEB-INF de la aplicacion
				try
				{
					String file = ApplicationContext.getInstance().getServletEngineDefaultPath();
					if(!file.equals("") && !file.endsWith(File.separator))
					{
						file += File.separator + "WEB-INF" + File.separator + fileName;
					}
					else
					{
						file += "WEB-INF" + File.separator + fileName;
					}
					return new FileInputStream(file);
				}catch(Exception ee) { ; }
			}

			return new FileInputStream(fileName);
		}
		catch (IOException e)
		{
			return getFileAsStream(fileName);
		}
	}

	public static InputStream getFileAsStream(String fileName)
	{
		InputStream out = getResourceAsStream(fileName);
		return out;
	}

	public static InputStream getResourceAsStream(String fileName)
	{
		return getResourceAsStream(SpecificImplementation.Application.getConfigurationClass(), fileName);
	}

	public static InputStream getResourceAsStream(Class resourceClass, String fileName)
	{
		int pos;
		if	( (pos = fileName.lastIndexOf('\\')) >= 0 || (pos = fileName.lastIndexOf('/')) >= 0)
		{
			fileName = fileName.substring(pos + 1);
		}

		InputStream ret = null;

		if	(  GXImageList.contains(fileName.toLowerCase()) ||
			  ( fileName.length() > 3 &&
			    fileName.substring(fileName.length() - 3, fileName.length()).equals("dll"))
			)
		{
			try
			{
	     		ret = ResourceReader.class.getResourceAsStream(fileName.toLowerCase());
			}
			catch (SecurityException e)
			{
				System.err.println("Unable to read " + fileName + " form resources");
			}

			if	(ret == null)
			{
				try
				{
					URL u = ResourceReader.class.getResource(fileName.toLowerCase());
					if	(u != null)
			     		ret = u.openStream();
				}
				catch (IOException e)
				{
				}

				catch (SecurityException e)
				{
					System.err.println("Unable to read " + fileName + " form resources");
				}
			}
		}

		if	(ret != null)
			return ret;

		if	(resourceClass != null)
		{
			GetResource gr = new GetResource(fileName, resourceClass);
			try
			{
				gr.run();
			}
			catch (SecurityException e)
			{
				SpecificImplementation.NativeFunctions.getInstance().executeWithPermissions(gr, INativeFunctions.FILE_ALL);
			}

			ret = gr.getInputStream();
		}
		return ret;
	}
}

class GetResource implements Runnable
{
	String fileName;
	Class resourceClass;
	InputStream is;

	public GetResource(String fileName, Class resourceClass)
	{
		this.fileName = fileName;
		this.resourceClass = resourceClass;
	}

	public void run()
	{
	 	try
	 	{
	 		is = resourceClass.getResourceAsStream(fileName.toLowerCase());
		}
	 	catch (SecurityException e)
	 	{
			System.err.println("security " + e.getMessage());
			throw e;
	 	}
	}

	public InputStream getInputStream()
	{
		return is;
	}

}
