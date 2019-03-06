package com.genexus.xml;

public class EntityDeclaration
{
	String m_name;
	String m_publicid;
	String m_systemid;
	String m_notation;
					  
	public EntityDeclaration(String name, String publicid, String systemid, String notation)
	{
		m_name = ( name != null)? name : "";
		m_publicid = ( publicid != null)? publicid : "";
		m_systemid = ( systemid != null)? systemid : "";
		m_notation = ( notation != null)? notation : "";
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public String getPublicID()
	{
		return m_publicid;
	}
	
	public String getSystemID()
	{
		return m_systemid;
	}
	
	public String getNotation()
	{
		return m_notation;
	}
}
