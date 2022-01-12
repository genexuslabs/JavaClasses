
package com.genexus;

import java.io.*;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.platform.INativeFunctions;

public class LoadLibrary 
{
	public static void load(final String libraryName)
	{
		SpecificImplementation.NativeFunctions.getInstance().executeWithPermissions(
			new Runnable()
			{
				public void run()
				{
					try
					{
						System.load(libraryName);
					}
					catch (java.lang.UnsatisfiedLinkError ee)
					{
						try
						{
							System.loadLibrary(libraryName);
						}
						catch (java.lang.UnsatisfiedLinkError e)
						{
							String outPath = System.getProperty("sun.boot.library.path");

							if	(outPath == null)
								throw e;

							InputStream is = ResourceReader.getFileAsStream(libraryName + ".dll");
							if	(is == null)
								throw e;

				 			CommonUtil.InputStreamToFile(is, outPath + "\\" + libraryName + ".dll");
							System.loadLibrary(libraryName);
						}
					}
				}
			},  INativeFunctions.LOAD_LIBRARY);
	}
}
