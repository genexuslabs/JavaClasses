// $Log: MailRecipient.java,v $
// Revision 1.2  2008/10/31 17:28:32  alevin
// - Cambio para que el encoding de los recipients se haga al armar la lista, sino quedaba mal formado el string y al
//   recibir el mail no se veian todas las direcciones que estaban en el TO. SAC24284
//
// Revision 1.1  2002/04/04 21:00:34  gusbro
// Initial revision
//
// Revision 1.1.1.1  2002/04/04 21:00:34  gusbro
// GeneXus Java Olimar
//
package com.genexus.internet;

import java.io.IOException;

import com.genexus.CommonUtil;

public class MailRecipient
{
	private String name;
	private String address;

	public MailRecipient()
	{
		name = "";
		address = "";
	}

	public MailRecipient(String name, String address)
	{
		if (name == null)
			name = "";
		this.name = name;
		this.address = address;

		if	(name.trim().equals(""))
		{
			this.name = address;
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name.trim();
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address.trim();
	}

	String getRecipientString(int addressFormat)
	{
		switch (addressFormat)
		{
			case 0:
				return "\"" + name.trim() + "\"";
			case 1:
				return address.trim();
		}
		
		return getRecipientString();
	}

	String getRecipientString()
	{
		if	(name.length() > 0)
		{
			return "\"" + GXMailer.getEncodedString(name) + "\"" + (address.length() > 0?(" <" + address + ">"):"");
		}

		return address;
	}

	static MailRecipient getFromString(String token) throws IOException
	{
		QuotedPrintableDecoder dec = new QuotedPrintableDecoder();
		MailRecipient ret = new MailRecipient();
		token = token.trim();		
		if	(token.indexOf('<') >= 0 && token.endsWith(">"))
		{						
			ret.setName(CommonUtil.removeAllQuotes(dec.decodeHeader(token.substring(0, token.indexOf('<')))));							
			ret.setAddress(token.substring(token.indexOf('<') + 1, token.lastIndexOf('>')));
		}
		else
		{
			ret.setAddress(token);
			ret.setName(token);
		}

		return ret;
	}
}

