// $Log: PictureFormatter.java,v $
// Revision 1.5  2006/12/01 20:24:10  iroqueta
// Cambio para que las pictures de los date puedan quedar todos vacios si el valor es empty.
// Developed by Claudia
//
// Revision 1.4  2004/05/25 20:18:53  gusbro
// - Arreglo para que funcionen las pictures tipo ZZZ.Z9
//
// Revision 1.3  2003/03/17 19:11:35  gusbro
// - Cambios para soportar pictures del tipo $,$$$,$$$.99 en reportes
//
// Revision 1.2  2002/07/23 18:53:07  aaguiar
// - Arreglo en la IsTimeInPicture para que banque pictures 99-99-99 99
//
// Revision 1.1.1.1  2000/12/06 21:00:42  gusbro
// Entran los fuentes al CVS
//
// Revision 1.1.1.1  2000/12/06 21:00:42  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import com.genexus.common.interfaces.SpecificImplementation;

public class PictureFormatter
{
	public static String format(String value, String picture)
	{
		return SpecificImplementation.PictureFormatter.format(value, picture);
	}

	public static String getNullMask(String picture)
	{
		if(LocalUtil.getBLANK_EMPTY_DATE())
		{
			picture = picture.replace('/', ' ').replace(':', ' ');

		}
		return  picture.replace('9',' ').replace('X',' ').replace('!',' ').replace('Z',' ').replace('A', ' ').replace('M', ' ');
	}

	public static boolean isDateEmpty(String date)
	{
		for (int i = date.length() - 1; i >= 0; i--)
		{
			char car = date.charAt(i);

			// HACK: las fechas-horas que vienen todas con 0 tambien las asumimos como
			// nulas.

			if	(car != ' ' && car != '/' && car != '-' && car != ':' && car != '0')
				return false;
		}

		return true;
	}

	public static boolean isDateInPicture(String picture)
	{
		return (picture.length() > 7 && (picture.indexOf('/') > 0 || picture.indexOf('-') > 0));
	}

	public static boolean isTimeInPicture(String picture)
	{
		return picture.equals("99") || picture.indexOf(':') > 0 || picture.toUpperCase().endsWith("M") || picture.endsWith(" 99");
	}


	public static String getTimePictureInPicture(String picture)
	{
		int pos;
		if	( (pos = picture.indexOf("99:")) >= 0)
		{
			return picture.substring(pos);
		}

		if	( (pos = picture.indexOf(" 9")) >= 0)
		{
			return picture.substring(pos + 1);
		}

		if	(picture.equals("99"))
		{
			return picture;
		}

		return "";
	}


	/**
	* Convierte un picture GeneXus a NumberFormat Java.
	* <p>
	* Pasa los '9' y 'Z' a '#', y antes del . decimal agrega un '0' para que
	* siempre ponga el 0 si el valor es < 1.
	*
	*/
	public static String pictureToNumberFormat(String picture)
	{
		boolean hasDollar = false;
		StringBuffer newPicture = new StringBuffer(picture.length());

		for (int i = 0; i < picture.length(); i++)
		{
			char car = picture.charAt(i);
			switch (car)
			{
				case '9' :
						newPicture.append('#');
						break;
				case 'Z' :
						newPicture.append('#');
						break;
				case '.' :
						if	(i > 0)
						{
							newPicture.setCharAt(i-1, '0');
						}
						newPicture.append(car);
						if(i+1 < picture.length() &&
						   picture.indexOf('0', i+1) > i+1)
						{
							picture = picture.substring(0, i) + picture.substring(i).replace('0', '#');
							newPicture.append('0');
							i++;
						}

						break;
				case '$':
						hasDollar = true;
						newPicture.append('#');
						break;
				default :
						newPicture.append(car);
			}
		}
		return hasDollar ? "$" + newPicture.toString() : newPicture.toString();
	}

}
