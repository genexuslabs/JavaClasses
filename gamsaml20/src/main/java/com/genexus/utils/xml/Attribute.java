package com.genexus.utils.xml;

import org.w3c.dom.Document;

import java.text.MessageFormat;

public class Attribute extends XmlTypes{

	Element element;
	private final String tag;

	public Attribute(String namespace, String element, String tag)
	{
		this.element = new Element(namespace, element);
		this.tag = tag;
	}

	public String getTag()
	{
		return tag;
	}

	public String findValue(Document doc)
	{
		return element.getElement(doc).getAttributes().getNamedItem(tag).getNodeValue();
	}

	public String printJson(Document xmlDoc)
	{
		String value = findValue(xmlDoc);
		return value == null ? null : MessageFormat.format( "\"{0}\": \"{1}\"", tag, value);
	}
}