package com.genexus.util;

import java.io.*;

public class FileUtilities
{

	public static String getCurrentPath()
	{
		String fileWithPoint = new File(".").getAbsolutePath();
		int length = fileWithPoint.length();
		
		// Pone el '.' al final.
		if	(length > 0 && fileWithPoint.charAt(length - 1) == '.')
			fileWithPoint = fileWithPoint.substring(0, length -1);

		// Devuelve un \\ al final si est� en el directorio raiz.
		if	(fileWithPoint.endsWith(File.separator + File.separator))
			fileWithPoint = fileWithPoint.substring(0, fileWithPoint.length() - 1);
		
		return fileWithPoint;
	}
}