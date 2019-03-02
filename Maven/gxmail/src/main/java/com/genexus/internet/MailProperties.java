// $Log: MailProperties.java,v $
// Revision 1.4  2006/11/30 15:14:31  alevin
// - Cambios para desencodear bien los mensajes japoneses de acuerdo al RFC1468.
//   Se agrega la clase JapaneseMimeDecoder.
//
// Revision 1.3  2006/11/23 16:27:45  alevin
// - Arreglo para leer bien los subjects que vienen en japones.
//
// Revision 1.2  2006/07/10 13:53:38  alevin
// - En el putKey, el header Received se tiene que guardar solo una vez (el primero).
//
// Revision 1.1  2001/12/07 18:11:54  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/12/07 18:11:54  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.util.*;
import com.genexus.CommonUtil;

class MailProperties extends Hashtable
{
	MailProperties()
	{
		super();
		putKey("Content-Type: text/plain");
	}

	public void putKey(String reply)
	{
                if(reply.startsWith(GXInternetConstants.RECEIVED))
                {//Si ya se guard� un Received no guardo mas
                       if(get(GXInternetConstants.RECEIVED.toUpperCase()) != null)
                       {
                               return;
                       }
                }

		int pos = reply.indexOf(':');
		if (pos != -1)
		{
			String key   = reply.substring(0, pos);
			String value = reply.substring(pos + 1).replace('\t', ' ').replace((char) 13, ' ').replace((char) 10, ' ').trim();

            if(key.equalsIgnoreCase(GXInternetConstants.SUBJECT))
            {
                           value = JapaneseMimeDecoder.decode(value);
                        }

                        put(key.toUpperCase(), value);
		}
	}

	public String getKeyPrincipal(String key)
	{
		String value = (String) get(key.toUpperCase());
		
		if	(value != null)
		{
			int pos = value.indexOf(";");
			if	(pos >= 0)
				return value.substring(0, pos);

			return value;
		}

		return "";
	}

	public String getKeyProperty(String key, String property)
	{
		String value = (String) get(key.toUpperCase());
		property = CommonUtil.upper(property);

		if	(value != null)
		{	
			StringTokenizer st = new StringTokenizer(value, ";");
     		while (st.hasMoreTokens()) 
     		{
				String token = st.nextToken().trim();

				if	(CommonUtil.upper(token).startsWith(property + "="))
					return token.substring(token.indexOf("=") + 1, token.length());
     		}
		}

		return "";
	}

	public String getMimeMediaSubtype()
	{
		String mediaType = getKeyPrincipal(GXInternetConstants.CONTENT_TYPE);
		int pos = mediaType.indexOf('/');

		if	(pos >= 0)
			return mediaType.substring(pos + 1, mediaType.length());

		return mediaType.toLowerCase();
	}

	public String getMimeMediaType()
	{
		String mediaType = getKeyPrincipal(GXInternetConstants.CONTENT_TYPE);
		int pos = mediaType.indexOf('/');

		if	(pos >= 0)
			return mediaType.substring(0, pos);

		return mediaType.toLowerCase();
	}
	
	
	public String getField(String field)
	{
		String ret = (String) get(field.toUpperCase());
		return ret == null?"":ret;
	}
}

