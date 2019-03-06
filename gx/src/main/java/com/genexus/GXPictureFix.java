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
	
	public int getPos(int posTent )   
	{								
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