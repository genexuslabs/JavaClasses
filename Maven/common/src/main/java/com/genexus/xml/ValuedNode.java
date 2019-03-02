package com.genexus.xml;

public class ValuedNode extends Node
{
	String m_value;
	int m_nodetype;
	
	public ValuedNode(int nodeType, String value)
	{
		m_nodetype = nodeType;
		m_value = value;
	}
	
	public String getValue()
	{
		return m_value;
	}
	
	public void setValue(String value)
	{
		m_value  = value;
	}
	
	
	public int getNodeType()
	{
		return m_nodetype;
	}
	
	public void setNodeType(int nodeType)
	{
		m_nodetype = nodeType;
	}
}
