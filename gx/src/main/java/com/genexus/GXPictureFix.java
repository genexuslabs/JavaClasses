// $Log: GXPictureFix.java,v $
// Revision 1.4  2008-05-12 14:06:28  cmurialdo
// Agrego metodo getMask (ver puts posteriores, para resolver sac 22957)
//
// Revision 1.3  2006/06/27 18:23:39  iroqueta
// Con el ultimo put se rompia el caso que el largo de la picture sea mas grande que el largo del texto, por ejemplo cuando la picture es ZZZZ/Z9 y el valor era 200606
//
// Revision 1.2  2006/05/31 19:20:58  iroqueta
// Arreglo el metodo format para soportar pictures con perfijos no numericos por ejemplo U$.
// SAC 14733
//
// Revision 1.1  2002/04/17 17:51:26  gusbro
// Initial revision
//
// Revision 1.1.1.1  2002/04/17 17:51:26  gusbro
// GeneXus Java Olimar
//
//
//   Rev 1.6   23 Sep 1998 19:48:24   AAGUIAR
//
//   Rev 1.5   May 28 1998 10:00:34   DMENDEZ
//Sincro28May1998

package com.genexus;

public class GXPictureFix extends GXPicture
{
	public GXPictureFix(String mask)
	{
		super(mask);
	}
	
	public GXPictureFix(String mask, int length)
	{
		super(mask, length);
	}
	public String getMask()
	{
		return mask;
	}
	public int getPos(int posTent )  // si la posicion es valida la devuelve sino 
	{									// busca la siguiente mas proxima valida
		if (posTent < mask.length() )
		{
			if (! isSeparator(mask.charAt(posTent)) )
			{
				return posTent;
			}
			return getPos(posTent + 1);
		}
			
		return -1;
	}

	/**
	* si la posicion es valida la devuelve sino busca la anterior mas proxima valida
	*/
	public int getAnt(int posTent )   
	{
		if (posTent >= 0  )
		{
			if (! isSeparator(mask.charAt(posTent)) )
			{
				return posTent;
			}
			return getAnt(posTent - 1);
		}

		return -1;
	}

	public int getNextSeparatorPos(int pos)
	{
		for (int i = pos; i < mask.length(); i++)
		{
			if (isSeparator(mask.charAt(i)) )
				return i;
		}

		return -1;
	}

	public boolean haveSeparator()
	{					
		for (int i = 0 ; i < mask.length() ; i++ )
		{
			if (isSeparator(mask.charAt(i)) )
				return (true);
		}
		return (false || cambio);
	}
	
	public String format(String oldText , int pos , char newChar , boolean ins )
	{
		return (format(oldText));
	}
	
	public String format(String oldText )
	{
		if 	(oldText.equals("") )
		{
			return nullMask();
		}
		else
		{
			if	(CommonUtil.rtrim(mask).length() == 0)
				return CommonUtil.rtrim(oldText);
			
			StringBuffer ret = new StringBuffer(nullMask()) ;
			int len = nullMask().length();
			int maskIndex = 0;
			int j = 0;
			char c;

			// ZZZ  ZZ9
			//        1
			while (maskIndex < len && j < oldText.length())
			{
				c = oldText.charAt(j); 

				if (isSeparator(mask.charAt(maskIndex)))
				{
					if (c == mask.charAt(maskIndex))
					{
						ret.setCharAt(maskIndex, c);
						j++;
					}

					maskIndex++;
				 }
				 else
				 {
					 if (mask.charAt(maskIndex) != 'Z' && mask.charAt(maskIndex) != '9' && (len - maskIndex > oldText.length()))
					 {
						 maskIndex++;
					 }
					 else
					 {
						if (isValid(c, maskIndex) || (c == ' ' && mask.charAt(maskIndex) == 'Z'))
						{
							ret.setCharAt(maskIndex, getCaret(c, maskIndex, ret));
							maskIndex++;
							j++;
						} 
						else
						{
					 		j++;
						}
					 }
				}
			}
					
			return (ret.toString());
		}
	}

	public String formatValid(String oldText )
	{
		if 	(oldText.equals("") )
			return (nullMask());
		else
		{
			if	(CommonUtil.rtrim(mask).length() == 0)
				return CommonUtil.rtrim(oldText);
			
			StringBuffer ret = new StringBuffer(nullMask()) ;
			int len = nullMask().length();
			int maskIndex = 0;
			int j = 0;
			char c;
			while (maskIndex < len && j < oldText.length())
			{
				c = oldText.charAt(j); 
			
				if (isSeparator(mask.charAt(maskIndex)))
				{
					if ( c == mask.charAt(maskIndex))
						j++;
					maskIndex++;
				}
				else
				{
					ret.setCharAt(maskIndex, getCaret(c, maskIndex, ret));
					maskIndex++;
					j++;
				 }
			}
					
			return (ret.toString());
		}
	}

}