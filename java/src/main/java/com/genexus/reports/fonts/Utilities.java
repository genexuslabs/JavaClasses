package com.genexus.reports.fonts;

import java.util.Vector;



/** Esta clase provee de metodos �tiles para varias clases del GXWS
 */

public class Utilities
{
	private static String predefinedSearchPath = ""; // Contiene los predefinedSearchPaths

	/** Agrega una lista de paths de b�squeda predefinidos
	 * *  Por ejemplo en MS se podr�a pasarle algunos paths con SystemInformation
	 * @param predefinedPaths Array de Strings con los paths predefinidos
	 * */
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

	/** Separa partes de una lines y las retorna en un Vector. Las partes pueden estar encerradas por comillas
	 * @param line linea a separar
	 * @param separator String conteniendo el separador de partes
	 * @return Vector de Strings conteniendo las partes separadas
	 */
	public static Vector<String> parseLine(String line, String separator)
	{
		Vector<String> partes=new Vector<>();
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
