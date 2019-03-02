
package com.genexus.xml;

public class PInstructionNode extends Node
{
	private String m_value;
	private String m_name;
	
	
	public PInstructionNode (String target, String value)
	{
		setName(target);
		setValue(value);
	}
	
	public int getNodeType()
	{
		return PROCESSING_INSTRUCTION;
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public void setName(String name)
	{
		m_name = (name != null)? name : "";
	}
	
	public String getLocalName()
	{
		return m_name;
	}
	
	public String getValue()
	{
		return m_value;
	}
	
	public void setValue(String value)
	{
		m_value = (value != null)? value : "";
	}	
}
