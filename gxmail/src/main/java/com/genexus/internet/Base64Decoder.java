// $Log: Base64Decoder.java,v $
// Revision 1.2  2006/07/04 22:05:51  alevin
// - Arreglo en el decode, en algunos casos daba ArrayIndexOutOfBounds.
//
// Revision 1.1  1999/06/30 15:19:50  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/06/30 15:19:50  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.IOException;
import java.io.OutputStream;

class Base64Decoder implements MimeDecoder
{
	private final int LINE_SIZE = 80;
	
	byte	vec[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".getBytes();

	public Base64Decoder ( ) 
	{
		super();
	}

	public void decode(MailReader input, OutputStream out) throws IOException
	{
		int	i, num, len, j;
		long	d, val;
		byte	nw[] = new byte[4];
		byte  buf[] = null;
		byte p, c;

		try
		{
			String str = null;
			while (true)
			{
				// Read a line
				str = input.readLine();
	
				if (str != null)
					buf = str.getBytes();
				else
					break;
			
				len = buf.length - 1;	
				for (i=0; i < len; i += 4)
				{
					val = 0;
					num = 3;
					//c = buf[i]; 
                                        if (i + 2 >= len)
                                                num = 0;
					else if (buf[i+2] == '=')
						num = 1;
					else if (buf[i+3] == '=')
						num = 2;

					for (j = 0; j <= num; j++)
					{
						/* Check if valid character	*/
			
							d = getValue(buf[i+j]); // vec index.
							d <<= (3-j)*6;
							val += d;
					}

					for (j = 2; j >= 0; j--)
					{
						nw[j] = (new Long(val & 255)).byteValue();
						val >>= 8;
					}

					out.write(nw, 0, num);
				}
			}
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			 throw new IOException("Error in decoding message");
		}	
	}

	private int getValue(byte b) 
	{
		if ( b >= 65 && b <= 90 )
			return ( (int) b - 65 );
		else if ( b >= 97 && b <= 122 )
			return ( (int) b - 65 - 6 );
		else if ( b >= 48 && b <= 57 )
			return ( (int) b - 48 + 52 );
		else if ( b == 43)
			return ( (int) 62 );
		else if ( b == 47)
			return ( (int) 63 );
		else return 0; // ERROR
	}
}
