

package com.genexus.util;

public final class MemoLines
{
	public static int gxmlines(String sInput, int nWidth)
	{
		int	nLCount = 0, nLSize;
		String ps;

		while ((ps = GX_nextline( sInput, nWidth)) != null)
		{
			nLCount++;
			sInput = sInput.substring(ps.length());	
		}

		return nLCount;
	}

	public static String gxgetmli( String sInput, int nLineno, int nWidth)
	{
		String ps;
		int nLCount = 0;

		while ( (ps = GX_nextline( sInput, nWidth)) != null )
		{
			nLCount++;
			if  (nLCount == nLineno)
			{
				if(ps.length() > nWidth)
				{ 
					ps = ps.substring(0, nWidth);
				}
				return removeTrailingLineDelimiter(ps);
			}

			sInput = sInput.substring(ps.length());
		}

		return "";
	}

	public static String GX_nextline( String sInput, int nWidth)
	{
		int nSize   = sInput.length();
		int nMinLen = ((nSize > nWidth) ? nWidth : nSize);

		if (nMinLen == 0)
			return null;

		/*
			Search for word delimiters and line delimiters. Save position of
			leftmost line delimiter and rightmost word delimiter.
		*/
			
		int leftmostLDel  = -1;
		int rightmostWDel = -1;
		if(nMinLen < nSize && GX_is_word_delimiter(sInput.charAt(nMinLen)))
	    {
			rightmostWDel = nMinLen;
		}
		for (int i = nMinLen - 1; i >= 0; i--)
		{
			if (GX_is_line_delimiter( sInput.charAt(i)))
				leftmostLDel = i;
			else 
			{
				if	(nSize > nWidth)
					if (rightmostWDel == -1 && GX_is_word_delimiter( sInput.charAt(i)))
						rightmostWDel = i; 
			}
		}

		if (leftmostLDel != -1)
		{
			int wide = 1;
			if	(leftmostLDel < sInput.length() - 1 && GX_is_line_delimiter(sInput.charAt(leftmostLDel + 1))				 )
				 wide++;
			return sInput.substring(0, leftmostLDel + wide);
		}

		if	(sInput.length() >= nMinLen + 2 && GX_is_line_delimiter(sInput.charAt(nMinLen)) && GX_is_line_delimiter(sInput.charAt(nMinLen+1)))
		{
			return sInput.substring(0, nMinLen + 2);
		}

		if	(rightmostWDel != -1)
		{
			return sInput.substring(0, rightmostWDel + 1);
		}

		return (sInput.substring(0, nMinLen));
	}

	public static boolean GX_is_line_delimiter( char cChar)
	{
      return (cChar <= 0x0020) &&
             ((((
                 (1L << 0x000A) |
                 (1L << 0x000D)) >> cChar) & 1L) != 0);
    }

	public static boolean GX_is_word_delimiter( char cChar)
	{
		return Character.isWhitespace(cChar);
	}
	
	public static String removeTrailingLineDelimiter(String sInput)
	{
		for (int i = sInput.length() - 1; i >= 0; i--)
		{
			if (!GX_is_line_delimiter( sInput.charAt(i)))
				return sInput.substring(0, i+1);
		}
		return "";
	}	
}
