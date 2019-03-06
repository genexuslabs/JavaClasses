// $Log: JapaneseMimeDecoder.java,v $
// Revision 1.1.2.1  2006/11/30 15:14:32  alevin
// - Cambios para desencodear bien los mensajes japoneses de acuerdo al RFC1468.
//   Se agrega la clase JapaneseMimeDecoder.
//

package com.genexus.internet;

public class JapaneseMimeDecoder
{
	private static String[] encodingStarts = new String[] { "$B", "$@" };
	private static String[] encodingEnds = new String[] { "(B", "(J" };

	public static String decode(String encoded)
	{
		int strLen = encoded.length();
		int currentIndex = 0;

		StringBuffer buffer = new StringBuffer();

		while(currentIndex < strLen)
		{
			int encStart = getEncodedLimit(encodingStarts, encoded, currentIndex);
			int encEnd = getEncodedLimit(encodingEnds, encoded, encStart + 3);
			if((encStart != -1) && (encEnd != -1))
			{
				if(currentIndex < encStart)
				{
					buffer.append(encoded.substring(currentIndex, encStart));
				}
				String encodedPart = encoded.substring(encStart, encEnd + 3);
				try
				{
					buffer.append(new String(encodedPart.getBytes(), "ISO-2022-JP"));
				}
				catch (java.io.UnsupportedEncodingException ex)
				{
					buffer.append(encodedPart);
				}
				currentIndex = encEnd + 3;
			}
			else
			{
				break;
			}
		}

		if(currentIndex < strLen)
		{
			buffer.append(encoded.substring(currentIndex));
		}

		return buffer.toString();
	}

	private static int getEncodedLimit(String[] limiters, String str, int fromIdx)
	{
		for(int i=0; i<limiters.length; i++)
		{
			int idx = str.indexOf(limiters[i], fromIdx);
			if(idx != -1)
			{
				return idx;
			}
		}
		return -1;
	}
}
