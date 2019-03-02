

package com.genexus.xml;

public abstract class Node
{
	
	public static final int ELEMENT = 1; 
	public static final int END_TAG = 2; 
	public static final int TEXT = 4;  
	public static final int COMMENT = 8; 
	public static final int WHITE_SPACE = 16; 
	public static final int CDATA = 32; 
	public static final int PROCESSING_INSTRUCTION = 64; 
	public static final int DOCUMENT_TYPE = 128;
	
	
	public abstract int getNodeType();

	public String getName()
	{
		return "";
	}
	
	public void setName(String name)
	{
	}
	
	public String getValue()
	{
		return "";
	}
	
	public void setValue(String value)
	{
	}		
		
	public int getAttributeCount()
	{
		return 0;
	}
			
	public String getAttributeByIndex(int Index)
	{
		return "";
	}
	
	public String getAttributeByName(String name)
	{
		return "";
	}
	
	public int existsAttribute(String name)
	{
		return 0;
	}

	public String getAttributeName(int Index)
	{
		return "";
	}

	public String getAttributePrefix(int Index)
	{
		return "";
	}

	public String getAttributeLocalName(int Index)
	{
		return "";
	}

	public String getAttributeURI(int Index)
	{
		return "";
	}

	public String getPrefix()
	{
		return "";
	}
	
	public void setPrefix(String prefix)
	{
	}
			
	public String getLocalName()
	{
		return "";
	}
	
	public void setLocalName(String localName)
	{
	}
			
	public String getNamespaceURI()
	{
		return "";
	}
	
	public void setNamespaceURI(String uri)
	{
	}
	
	public String getAttEntityValueByName(String name) 
	{
		return "";
	}

	public String getAttEntityNotationByName(String name) 
	{
		return "";
	}

   	public String getAttEntityValueByIndex(int Index)
   	{
		return "";	
   	}

   	public String getAttEntityNotationByIndex(int Index)
   	{	
		return "";	
   	}
			
}
