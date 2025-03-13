package com.genexus.utils.xml;

import org.w3c.dom.Document;

import java.text.MessageFormat;

public class Element extends XmlTypes{

	private final String namespace;
	private final String tag;

	public Element(String namespace, String tag)
	{
		this.namespace = namespace;
		this.tag = tag;
	}

	public String getNamespace()
	{
		return  namespace;
	}

	public String getTag()
	{
		return tag;
	}

	public org.w3c.dom.Node getElement(Document doc)
	{
		return doc.getElementsByTagNameNS(namespace, tag).item(0);
	}

	public String findValue(Document doc)
	{
		return getElement(doc).getTextContent();
	}

	public String printJson(Document xmlDoc)
	{
		String value = findValue(xmlDoc);
		return value == null ? null : MessageFormat.format( "\"{0}\": \"{1}\"", tag, value) ;
	}
}
