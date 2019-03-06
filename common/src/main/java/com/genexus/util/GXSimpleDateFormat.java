// $Log: GXSimpleDateFormat.java,v $
// Revision 1.9  2006/10/02 19:09:20  alevin
// (CMurialdo): El arreglo de la revision anterior va tambien para las demas versiones del framework (ya que el mismo problema ocurre en la versi�n 1.1 y 2.0 del framework).
//
// Revision 1.8  2005/10/21 15:34:36  alevin
// - Arreglo para J# en el metodo parse, hago que el hack de las revisiones de abajo sea valido
//   solo para el framework 1.0.
//
// Revision 1.7  2005/06/24 20:16:24  gusbro
// - Hago que el formateo no sea 'lenient'
//
// Revision 1.6  2005/05/20 20:28:08  gusbro
// - Arreglos en dates para J#
//
// Revision 1.5  2004/05/04 18:50:48  gusbro
// - El arreglo que se hacia en JSharp para las fechas con am/pm se deja de hacer porque
//   parece que esta arreglado el SimpleDateFormat de J#
//
// Revision 1.4  2004/04/30 20:29:26  gusbro
// - En japones no estaba validando bien los datetimes con AM/PM con la VM de Sun
//
// Revision 1.3  2003/07/10 16:26:55  gusbro
// - Arreglo para J#
//
// Revision 1.2  2002/08/07 18:09:04  gusbro
// - Subclase de SimpleDateFormat para mantener compatibilidad con el formato que usa el J#
// 
package com.genexus.util;

import java.text.*;
import java.util.*;


public class GXSimpleDateFormat extends SimpleDateFormat
{
	public GXSimpleDateFormat(String format)
	{
		this(format, Locale.ENGLISH);
	}

	public GXSimpleDateFormat(String format, Locale locale)
	{
		super(format, locale);
		setLenient(false);
	}


	public String gxFormat(Date date) //tenemos que usar un nombre nuevo porque format es 'final'
	{
		return super.format(date);
	}
}
