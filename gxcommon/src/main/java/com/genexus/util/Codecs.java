package com.genexus.util;

import java.io.*;
import java.util.*;


public class Codecs
{
    private static byte[]  Base64EncMap, Base64DecMap;

    // Class Initializer

    static
    {
	// rfc-2045: Base64 Alphabet
	byte[] map = {
	    (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F',
	    (byte)'G', (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L',
	    (byte)'M', (byte)'N', (byte)'O', (byte)'P', (byte)'Q', (byte)'R',
	    (byte)'S', (byte)'T', (byte)'U', (byte)'V', (byte)'W', (byte)'X',
	    (byte)'Y', (byte)'Z',
	    (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f',
	    (byte)'g', (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l',
	    (byte)'m', (byte)'n', (byte)'o', (byte)'p', (byte)'q', (byte)'r',
	    (byte)'s', (byte)'t', (byte)'u', (byte)'v', (byte)'w', (byte)'x',
	    (byte)'y', (byte)'z',
	    (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
	    (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/' };
	Base64EncMap = map;
	Base64DecMap = new byte[128];
	for (int idx=0; idx<Base64EncMap.length; idx++)
	    Base64DecMap[Base64EncMap[idx]] = (byte) idx;

    }


    // Constructors

    /**
     * This class isn't meant to be instantiated.
     */
    private Codecs() {}


    // Methods

    /**
     * This method encodes the given string using the base64-encoding
     * specified in RFC-2045 (Section 6.8). It's used for example in the
     * "Basic" authorization scheme.
     *
     * @param  str the string
     * @return the base64-encoded <var>str</var>
     */
    public final static String base64Encode(String str)
    {
        return base64Encode(str, "GB2312");
    }

    public final static String base64Encode(String str, String encoding)
    {
        if (str == null)  return  null;

        try
            { return new String(base64Encode(str.getBytes(encoding)), encoding); }
        catch (UnsupportedEncodingException uee)
            { throw new Error(uee.toString()); }
    }

    /**
     * This method encodes the given byte[] using the base64-encoding
     * specified in RFC-2045 (Section 6.8).
     *
     * @param  data the data
     * @return the base64-encoded <var>data</var>
     */
    public final static byte[] base64Encode(byte[] data)
    {
	if (data == null)  return  null;

	int sidx, didx;
	byte dest[] = new byte[((data.length+2)/3)*4];


	// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
	for (sidx=0, didx=0; sidx < data.length-2; sidx += 3)
	{
	    dest[didx++] = Base64EncMap[(data[sidx] >>> 2) & 077];
	    dest[didx++] = Base64EncMap[(data[sidx+1] >>> 4) & 017 |
					(data[sidx] << 4) & 077];
	    dest[didx++] = Base64EncMap[(data[sidx+2] >>> 6) & 003 |
					(data[sidx+1] << 2) & 077];
	    dest[didx++] = Base64EncMap[data[sidx+2] & 077];
	}
	if (sidx < data.length)
	{
	    dest[didx++] = Base64EncMap[(data[sidx] >>> 2) & 077];
	    if (sidx < data.length-1)
	    {
		dest[didx++] = Base64EncMap[(data[sidx+1] >>> 4) & 017 |
					    (data[sidx] << 4) & 077];
		dest[didx++] = Base64EncMap[(data[sidx+1] << 2) & 077];
	    }
	    else
		dest[didx++] = Base64EncMap[(data[sidx] << 4) & 077];
	}

	// add padding
	for ( ; didx < dest.length; didx++)
	    dest[didx] = (byte) '=';

	return dest;
    }

    public final static String base64Decode(String str, String encoding){

        if (str == null)  return  null;

        try
            { return new String(base64Decode(str.getBytes(encoding)), encoding); }
        catch (UnsupportedEncodingException uee)
            { throw new Error(uee.toString()); }
    }

    /**
     * This method decodes the given string using the base64-encoding
     * specified in RFC-2045 (Section 6.8).
     *
     * @param  str the base64-encoded string.
     * @return the decoded <var>str</var>.
     */
    public final static String base64Decode(String str)
    {
        return base64Decode(str, "GB2312");
    }


    /**
     * This method decodes the given byte[] using the base64-encoding
     * specified in RFC-2045 (Section 6.8).
     *
     * @param  data the base64-encoded data.
     * @return the decoded <var>data</var>.
     */
    public final static byte[] base64Decode(byte[] data)
    {
	if (data == null)  return  null;

	int tail = data.length;
	while (data[tail-1] == '=')  tail--;

	byte dest[] = new byte[tail - data.length/4];


	// ascii printable to 0-63 conversion
	for (int idx = 0; idx <data.length; idx++)
	    data[idx] = Base64DecMap[data[idx]];

        try
        {

          // 4-byte to 3-byte conversion
          int sidx, didx;
          for (sidx = 0, didx = 0; didx < dest.length - 2; sidx += 4, didx += 3) {
            dest[didx] = (byte) ( ( (data[sidx] << 2) & 255) |
                                 ( (data[sidx + 1] >>> 4) & 003));
            dest[didx + 1] = (byte) ( ( (data[sidx + 1] << 4) & 255) |
                                     ( (data[sidx + 2] >>> 2) & 017));
            dest[didx + 2] = (byte) ( ( (data[sidx + 2] << 6) & 255) |
                                     (data[sidx + 3] & 077));
          }
          if (didx < dest.length)
            dest[didx] = (byte) ( ( (data[sidx] << 2) & 255) |
                                 ( (data[sidx + 1] >>> 4) & 003));
          if (++didx < dest.length)
            dest[didx] = (byte) ( ( (data[sidx + 1] << 4) & 255) |
                                 ( (data[sidx + 2] >>> 2) & 017));
        }
        catch(ArrayIndexOutOfBoundsException exc)
        {
          //Ignore ArrayIndexOutOfBoundsException
        }
	return dest;
    }

    /**
     * This method decodes the given urlencoded string.
     *
     * @param  str the url-encoded string
     * @return the decoded string
     * @exception ParseException If a '%' is not followed by a valid
     *                           2-digit hex number.
     */
    public final static String URLDecode(String str)
    {
		if (str == null)  return  null;

		char[] res  = new char[str.length()];
		int    didx = 0;

		for (int sidx=0; sidx<str.length(); sidx++)
		{
		    char ch = str.charAt(sidx);
		    if (ch == '+')
				res[didx++] = ' ';
		    else if (ch == '%')
		    {
				try
				{
			    	res[didx++] = (char)
					Integer.parseInt(str.substring(sidx+1,sidx+3), 16);
			    	sidx += 2;
				}
				catch (NumberFormatException e)
				{
					System.err.println(str.substring(sidx,sidx+3) + " is an invalid code");
				}
		    }
		    else
			{
				res[didx++] = ch;
			}
		}

		return String.valueOf(res, 0, didx);
    }

        static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');
    static String dfltEncName = null;

    static {

	/* The list of characters that are not encoded has been
	 * determined as follows:
	 *
	 * RFC 2396 states:
	 * -----
	 * Data characters that are allowed in a URI but do not have a
	 * reserved purpose are called unreserved.  These include upper
	 * and lower case letters, decimal digits, and a limited set of
	 * punctuation marks and symbols.
	 *
	 * unreserved  = alphanum | mark
	 *
	 * mark        = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"
	 *
	 * Unreserved characters can be escaped without changing the
	 * semantics of the URI, but this should not be done unless the
	 * URI is being used in a context that does not allow the
	 * unescaped character to appear.
	 * -----
	 *
	 * It appears that both Netscape and Internet Explorer escape
	 * all special characters from this list with the exception
	 * of "-", "_", ".", "*". While it is not clear why they are
	 * escaping the other characters, perhaps it is safest to
	 * assume that there might be contexts in which the others
	 * are unsafe if not escaped. Therefore, we will use the same
	 * list. It is also noteworthy that this is consistent with
	 * O'Reilly's "HTML: The Definitive Guide" (page 164).
	 *
	 * As a last note, Intenet Explorer does not encode the "@"
	 * character which is clearly not unreserved according to the
	 * RFC. We are being consistent with the RFC in this matter,
	 * as is Netscape.
	 *
	 */

	dontNeedEncoding = new BitSet(256);
	int i;
	for (i = 'a'; i <= 'z'; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = 'A'; i <= 'Z'; i++) {
	    dontNeedEncoding.set(i);
	}
	for (i = '0'; i <= '9'; i++) {
	    dontNeedEncoding.set(i);
	}
	dontNeedEncoding.set(' '); /* encoding a space to a + is done
				    * in the encode() method */
	dontNeedEncoding.set('-');
	dontNeedEncoding.set('_');
	dontNeedEncoding.set('.');
	dontNeedEncoding.set('*');

    	dfltEncName = "UTF8";
    	//JDK 1.2 (String)AccessController.doPrivileged ( new GetPropertyAction("file.encoding") 	);
    }

    /**
     * Translates a string into <code>x-www-form-urlencoded</code>
     * format. This method uses the platform's default encoding
     * as the encoding scheme to obtain the bytes for unsafe characters.
     *
     * @param   s   <code>String</code> to be translated.
     * @deprecated The resulting string may vary depending on the platform's
     *             default encoding. Instead, use the encode(String,String)
     *             method to specify the encoding.
     * @return  the translated <code>String</code>.
     */
    public static String encode(String s) {

	String str = null;

	try {
	    str = encode(s, dfltEncName);
	} catch (UnsupportedEncodingException e) {
	    // The system should always have the platform default
	}

	return str;
    }

    /**
     * Translates a string into <code>application/x-www-form-urlencoded</code>
     * format using a specific encoding scheme. This method uses the
     * supplied encoding scheme to obtain the bytes for unsafe
     * characters.
     * <p>
     * <em><strong>Note:</strong> The <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that
     * UTF-8 should be used. Not doing so may introduce
     * incompatibilites.</em>
     *
     * @param   s   <code>String</code> to be translated.
     * @param   enc   The name of a supported
     *    <a href="../lang/package-summary.html#charenc">character
     *    encoding</a>.
     * @return  the translated <code>String</code>.
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     * @see URLDecoder#decode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String encode(String s, String enc)
	throws UnsupportedEncodingException {

	boolean needToChange = false;
	boolean wroteUnencodedChar = false;
	int maxBytesPerChar = 10; // rather arbitrary limit, but safe for now
        StringBuffer out = new StringBuffer(s.length());
	ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);

	OutputStreamWriter writer = new OutputStreamWriter(buf, enc);

	for (int i = 0; i < s.length(); i++) {
	    int c = (int) s.charAt(i);
	    //System.out.println("Examining character: " + c);
	    if (dontNeedEncoding.get(c)) {
		if (c == ' ') {
		    c = '+';
		    needToChange = true;
		}
		//System.out.println("Storing: " + c);
		out.append((char)c);
		wroteUnencodedChar = true;
	    } else {
		// convert to external encoding before hex conversion
		try {
		    if (wroteUnencodedChar) { // Fix for 4407610
		    	writer = new OutputStreamWriter(buf, enc);
			wroteUnencodedChar = false;
		    }
		    writer.write(c);
		    /*
		     * If this character represents the start of a Unicode
		     * surrogate pair, then pass in two characters. It's not
		     * clear what should be done if a bytes reserved in the
		     * surrogate pairs range occurs outside of a legal
		     * surrogate pair. For now, just treat it as if it were
		     * any other character.
		     */
		    if (c >= 0xD800 && c <= 0xDBFF) {
			/*
			  System.out.println(Integer.toHexString(c)
			  + " is high surrogate");
			*/
			if ( (i+1) < s.length()) {
			    int d = (int) s.charAt(i+1);
			    /*
			      System.out.println("\tExamining "
			      + Integer.toHexString(d));
			    */
			    if (d >= 0xDC00 && d <= 0xDFFF) {
				/*
				  System.out.println("\t"
				  + Integer.toHexString(d)
				  + " is low surrogate");
				*/
				writer.write(d);
				i++;
			    }
			}
		    }
		    writer.flush();
		} catch(IOException e) {
		    buf.reset();
		    continue;
		}
		byte[] ba = buf.toByteArray();
		for (int j = 0; j < ba.length; j++) {
		    out.append('%');
		    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
		    // converting to use uppercase letter as part of
		    // the hex value if ch is a letter.
		    if (Character.isLetter(ch)) {
			ch -= caseDiff;
		    }
		    out.append(ch);
		    ch = Character.forDigit(ba[j] & 0xF, 16);
		    if (Character.isLetter(ch)) {
			ch -= caseDiff;
		    }
		    out.append(ch);
		}
		buf.reset();
		needToChange = true;
	    }
	}

	return (needToChange? out.toString() : s);
	}
    // The platform default encoding
    /**
     * Decodes a <code>x-www-form-urlencoded</code> string.
     * The platform's default encoding is used to determine what characters
     * are represented by any consecutive sequences of the form
     * "<code>%<i>xy</i></code>".
     * @param s the <code>String</code> to decode
     * @deprecated The resulting string may vary depending on the platform's
     *          default encoding. Instead, use the decode(String,String) method
     *          to specify the encoding.
     * @return the newly decoded <code>String</code>
     */
    public static String decode(String s) {

	String str = null;

	try {
	    str = decode(s, dfltEncName);
	} catch (UnsupportedEncodingException e) {
	    // The system should always have the platform default
	}

	return str;
    }

    /**
     * Decodes a <code>application/x-www-form-urlencoded</code> string using a specific
     * encoding scheme.
     * The supplied encoding is used to determine
     * what characters are represented by any consecutive sequences of the
     * form "<code>%<i>xy</i></code>".
     * <p>
     * <em><strong>Note:</strong> The <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that
     * UTF-8 should be used. Not doing so may introduce
     * incompatibilites.</em>
     *
     * @param s the <code>String</code> to decode
     * @param enc   The name of a supported
     *    <a href="../lang/package-summary.html#charenc">character
     *    encoding</a>.
     * @return the newly decoded <code>String</code>
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     * @see URLEncoder#encode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String decode(String s, String enc)
	throws UnsupportedEncodingException{

	boolean needToChange = false;
	StringBuffer sb = new StringBuffer();
	int numChars = s.length();
	int i = 0;

	if (enc.length() == 0) {
	    throw new UnsupportedEncodingException ("URLDecoder: empty string enc parameter");
	}

	while (i < numChars) {
            char c = s.charAt(i);
            switch (c) {
	    case '+':
		sb.append(' ');
		i++;
		needToChange = true;
		break;
	    case '%':
		/*
		 * Starting with this instance of %, process all
		 * consecutive substrings of the form %xy. Each
		 * substring %xy will yield a byte. Convert all
		 * consecutive  bytes obtained this way to whatever
		 * character(s) they represent in the provided
		 * encoding.
		 */

		try {

		    // (numChars-i)/3 is an upper bound for the number
		    // of remaining bytes
		    byte[] bytes = new byte[(numChars-i)/3];
		    int pos = 0;

		    while ( ((i+2) < numChars) &&
			    (c=='%')) {
			bytes[pos++] =
			    (byte)Integer.parseInt(s.substring(i+1,i+3),16);
			i+= 3;
			if (i < numChars)
			    c = s.charAt(i);
		    }

		    // A trailing, incomplete byte encoding such as
		    // "%x" will cause an exception to be thrown

		    if ((i < numChars) && (c=='%'))
			throw new IllegalArgumentException(
		         "URLDecoder: Incomplete trailing escape (%) pattern");

		    sb.append(new String(bytes, 0, pos, enc));
		} catch (NumberFormatException e) {
		    throw new IllegalArgumentException(
                    "URLDecoder: Illegal hex characters in escape (%) pattern - "
		    + e.getMessage());
		}
		needToChange = true;
		break;
	    default:
		sb.append(c);
		i++;
		break;
            }
        }

        return (needToChange? sb.toString() : s);
    }



}
