// $Log: MimeEncoder.java,v $
// Revision 1.1.2.1  2006/03/03 19:24:52  alevin
// - Initial release.
//
//

package com.genexus.internet;

import java.io.*;
import java.util.*;

public class MimeEncoder
{
    private BASE64Encoder base64Encoder = new BASE64Encoder();

    public String encodeText(String str, String encoding) throws UnsupportedEncodingException
    {
        StringBuffer strbuff = new StringBuffer();
        encodeText(str, com.genexus.CommonUtil.normalizeEncodingName(encoding), 68 - encoding.length(), "=?" + encoding + "?B?", false, strbuff);
        return strbuff.toString();
    }

    private void encodeText(String str, String encoding, int len, String strEnd, boolean blank, StringBuffer stringbuffer) throws UnsupportedEncodingException
    {
        byte oldBytes[] = str.getBytes(encoding);

        int j = ((oldBytes.length + 2) / 3) * 4;
        int k;

        if(j > len && (k = str.length()) > 1)
        {
            encodeText(str.substring(0, k / 2), encoding, len, strEnd, blank, stringbuffer);
            encodeText(str.substring(k / 2, k), encoding, len, strEnd, true, stringbuffer);
            return;
        }

        ByteArrayInputStream bin = new ByteArrayInputStream(oldBytes);
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();

        try {
            base64Encoder.encodeBuffer(bin, bytearrayoutputstream);
            bytearrayoutputstream.close();
        } catch (IOException ex) {
        }

        byte newBytes[] = bytearrayoutputstream.toByteArray();

        if(blank)
        {
            stringbuffer.append(" ");
        }
        stringbuffer.append(strEnd);

        for(int l = 0; l < newBytes.length; l++)
        {
            stringbuffer.append((char) newBytes[l]);
        }

        stringbuffer.append("?=");
    }
}
