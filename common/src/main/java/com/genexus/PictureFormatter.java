
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
