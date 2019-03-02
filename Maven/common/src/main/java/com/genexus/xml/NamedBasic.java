
package com.genexus.xml;
								  
abstract public class NamedBasic extends Node
{
	private String m_name;
	private String m_prefix;
	private String m_localName;
	private String m_uri;
	
	NamedBasic()
	{
		setName("");
		setPrefix("");
		setLocalName("");
		setNamespaceURI("");
	}
	
	NamedBasic(String name, String prefix, String local, String uri)
	{
		setName(name);
		setPrefix(prefix);
		setLocalName(local);
		setNamespaceURI(uri);
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public void setName(String name)
	{
		m_name = (name != null)? name : "";
	}
	
	public String getPrefix()
	{
		return m_prefix;
	}
	
	public void setPrefix(String prefix)
	{
		m_prefix = (prefix != null)? prefix : "";
	}
	
	public String getLocalName()
	{
		return m_localName;
	}
	
	public void setLocalName(String localName)
	{
		m_localName = (localName != null)? localName : "";
	}
	
	public String getNamespaceURI()
	{
		return m_uri;
	}
	
	public void setNamespaceURI(String uri)
	{
		m_uri = (uri != null)? uri : "";
	}
	
}
