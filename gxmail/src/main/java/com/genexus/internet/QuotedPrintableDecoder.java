// $Log: QuotedPrintableDecoder.java,v $
// Revision 1.1  2001/10/30 21:55:32  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/10/30 21:55:32  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.*;
import com.genexus.util.Codecs;

class QuotedPrintableDecoder implements MimeDecoder
{
	// =?iso-8859-1?Q?
	public String getCharset(String text)
	{
		int type = text.indexOf("?", 3);
		return text.substring(2, text.length() - type);
	}

	public char getEncoding(String text)
	{
		int type = text.indexOf("?", 3);
		return text.charAt(type + 1);
	}

	public boolean canDecode(String text)
	{
		if	(text.length() > 2 && text.substring(0, 2).equals("=?"))
		{
			return getEncoding(text) == 'Q' || getEncoding(text) == 'B';
		}

		return false;
	}

	public void decode(MailReader in, OutputStream out) throws IOException
	{
		decode(in, out, false);
	}

	public String decodeHeader(InputStream in) throws IOException
	{
		int stateInDecoding = 2; 
		int stateInCharset = 1;
		int stateInEncodeType = 1;
		int stateNoneEqual = 3;
		int stateNone = 0;
		int state = stateNone;

		int read;
		StringBuffer out = new StringBuffer();
		StringBuffer outTemp = new StringBuffer();
		String charSet;
		char encoding;

//Subject: =?iso-8859-1?Q?Los_ricos=2C_cada_vez_m=E1s_ricos=2C_con_Bill_G?=
//         =?iso-8859-1?Q?ates_en_cabeza_por_tercer_a=F1o_?=

//     To: =?iso-8859-1?Q?=27Andr=E9s_Aguiar=27?= <aaguiar@genexus.es>

		while ( (read = in.read()) != -1)
		{
			if	(state == stateNone)
			{
				if	(read == '=')
				{
					state = stateNoneEqual;
					outTemp.append(read);
				}
				else
				{
					out.append( (char) read);
				}
			}
			else if (state == stateNoneEqual)
			{
				if	(read == '?')
				{
					state = stateInCharset;
				}
				else
				{
					out.append('=');
					out.append(outTemp);
					outTemp.setLength(0);
				}
			}
			else if (state == stateInCharset)
			{
				if	(read == '?')
				{
					charSet = outTemp.toString();
					state = stateInEncodeType;
				}
				else
				{
					outTemp.append((char) read);
				}
			}
			else if (state == stateInEncodeType)
			{
				if	(read == 'Q' || read == 'B')
				{
					encoding = (char) read;

					read = in.read();
					if	(read == '?')
					{
						state = stateInDecoding;
					}
					else
					{
						out.append(outTemp);
						out.append( (char) encoding);
						out.append( (char) read );
						state = stateNoneEqual;
					}
					
					outTemp.setLength(0);
				}
				else
				{
					state = stateNoneEqual;
					out.append(outTemp);
					outTemp.setLength(0);
				}
			} 
			else if (state == stateInDecoding)
			{
				if	(read == '=')
				{
				}
					
			}
		}

		return out.toString();
	}

	public String decodeHeader(String in) throws IOException
	{
	 //	if	(!canDecode(in))
	 //		return in;

		String out = "";
		String line = in;
		int leftEqual = 0;
		int rightEqual = 0;
		int lastRight = 0;

		int start = line.indexOf("=?");
		if	(start > 1)
			out = in.substring(0, start);

		while (true)
		{
			leftEqual  = line.indexOf("=?", rightEqual);
			if	(leftEqual == -1)
				break;

			rightEqual = line.indexOf("?=", rightEqual) + 2;
			if	(line.substring(rightEqual - 4, rightEqual - 2).equalsIgnoreCase("?Q") || line.substring(rightEqual - 4,  rightEqual - 2).equalsIgnoreCase("?B"))
				rightEqual = line.indexOf("?=", rightEqual) + 2;

			if	(rightEqual == -1)
				break;

			lastRight = rightEqual;

			in = line.substring(leftEqual, rightEqual);

			String left = in.substring(0, in.indexOf("?" + getEncoding(in) + "?") + 3);
			String encoding = left.substring(2, left.length() - 3);
			
			if	(getEncoding(in) == 'Q')
			{
				ByteArrayOutputStream sout = new ByteArrayOutputStream();
				in = in.substring(left.length(), in.length() - 2);
				decode(new RFC822Reader (new BufferedReader(new StringReader(in))), sout, true);
				out = out + sout.toString(encoding); //new String(sout.toByteArray());
			}
			else
			{
				in = in.substring(left.length(), in.length() - 2);
				out = out + Codecs.base64Decode(in, encoding);
			}

		}

		return out + line.substring(rightEqual, line.length());
	}

	public void decode64(InputStream in, OutputStream out) throws IOException
	{
	}

	private int hexToDec(int hex)
	{
		if	(hex >= 'A' && hex <= 'F')
			return  10 + hex - 'A';

		return hex - '0';
	}

	public void decode(MailReader in, OutputStream out, boolean header) throws IOException
	{
		int charRead = 0;
		int msb = 0;
		int lsb = 0;
		boolean encoding = true;

		while ( (charRead = in.read()) != -1)
		{
			if	(header && (charRead == GXInternetConstants.CR || charRead == GXInternetConstants.LF))
				continue;

			if	(encoding && charRead == '_' && header)
			{
				out.write(' ');
			}
			else if	(encoding && charRead == '?' && header)
			{
				in.read();
				encoding = false;
			}
			else if	(encoding && charRead == '=')
			{
				msb = in.read();
				if	(msb != -1)
				{
					if	(msb == 13 || msb == 10)
					{
						// Si viene un = y en seguida un CRLF es un 'soft return' y hay que
						// eliminarlo
						charRead = in.read();
						//if	(charRead == 13 || charRead == 10)
						//	charRead = in.read();
					}
					else
					{
						lsb = in.read();
						if	(lsb != -1)
						{
							out.write( (char) ( (hexToDec(msb) * 16) + hexToDec(lsb)));
						}
					}
				}
			}
			else
			{
				out.write(charRead);
			}
		}
	}		
}
