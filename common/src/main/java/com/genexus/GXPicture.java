
package com.genexus;

public abstract class  GXPicture
{
	public    abstract String format(String text);
	protected abstract int getPos(int posTent );
	protected abstract int getAnt(int posTent );

	private   String nullMask;
	protected String mask;
	protected int pos ;
	protected boolean cambio;
	protected int length;
	
	public GXPicture(String pic)
	{
		this(pic, pic.length());
	}

	public GXPicture(String picture, int len )
	{
		if	(picture.startsWith("@") && picture.indexOf('!') > 0)
		{
			cambio = true;
			setMask(CommonUtil.replicate("!", len));
		}
		else
		{
			cambio = false;
			setMask(picture);
		}
		
		this.length = len;
	}

	protected void setMask(String mask)
	{
		this.mask     = mask;
		this.nullMask = PictureFormatter.getNullMask(mask);
	}

	public boolean isValid(char c, int pos )  // en una posicion valida quiero insertar un char
	{
		if (pos < 0 || (!Character.isLetterOrDigit(c)))
			return false;
		
		switch (mask.charAt(pos))
		{
			case '9' :
				return Character.isDigit(c);
			case 'X' :
				return true;
			case '!' :
				return true;
			case 'Z' :
				return Character.isDigit(c);
			case ' ' :
				return c == ' ';
			case 'A' :
				return c == 'A' || c == 'P' || c == 'a' || c == 'p';
			case 'M' :
				return c == 'M' || c == 'm';
		}

		return false;
	}

	public char getCaret(char cIn , int pos, StringBuffer b)  
	{
		switch (mask.charAt(pos))
		{
			case '9' :
				if (!Character.isDigit(cIn) )
					return ' ';
				else
					return cIn;
				
			case 'X' :
					return cIn;
			case 'A' :
			case 'M' :
			case '!' :
					return Character.toUpperCase(cIn);
			case 'Z' :
					if (Character.isDigit(cIn) && cIn != '0')
					{
						return cIn;
					}
					else
					{
						if	(cIn == '0')
						{
							boolean first = true;
							for (int i = 0; i < pos; i++)
							{
								if	(b.charAt(i) != ' ')
								{
									first = false;
									break;
								}
							}

							if	(first)
							{
								return ' ';
							}
							else
							{
								return '0';
							}
						}
					}
		}

		return ' ';
	}

	public String space(String s, int inicio, int fin)
	{
		return CommonUtil.left(s, inicio) + nullMask.substring(inicio, fin) + s.substring(Math.min(fin, s.length()));
	}

	public String nullMask()
	{
		return nullMask;
	}
		
	public static boolean isSeparator(char c)
	{
		return (c != '?' && c != '#' && c != '9' && c != 'X' && c != '!' && c != 'Z' && c != 'A' && c != 'M');
	}
}