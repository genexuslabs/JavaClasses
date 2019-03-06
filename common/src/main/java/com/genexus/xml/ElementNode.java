
package com.genexus.xml;

import org.apache.xerces.xni.XMLAttributes;

public class ElementNode extends NamedBasic
{
	XMLAttributes m_attributes;
	String m_value;
	int indexMapping[] = new int[10];
	int indexMappingCount;
	boolean isReadRawXML;
	
	ElementNode(String name, String prefix, String local, String uri, XMLAttributes atts)
	{
		this(name, prefix, local, uri, atts, false);
	}
	
	ElementNode(String name, String prefix, String local, String uri, XMLAttributes atts, boolean isReadRaw)
	{
		super(name, prefix, local, uri);
		isReadRawXML = isReadRaw;
		setValue("");
		setAttributes(atts);
	}
	
	public void setAttributes(XMLAttributes atts)
	{
		String qname;
		int i;
		int length = atts.getLength();
		
		if (m_attributes == null)
			m_attributes = new org.apache.xerces.util.XMLAttributesImpl();
		
		m_attributes.removeAllAttributes();
		for (i = 0; i < length; i++)
		{
			org.apache.xerces.xni.QName iqname = new org.apache.xerces.xni.QName();
			atts.getName(i, iqname);
			m_attributes.addAttribute(iqname, atts.getType(i), atts.getValue(i));
		}
		
		indexMappingCount = 0;
		if (indexMapping == null || indexMapping.length < length)
			indexMapping = new int[length];
		
		for (i = 0; i < length; i++)
		{
			qname = m_attributes.getQName(i);
			if (isReadRawXML || (!qname.startsWith("xmlns:") && !qname.equals("xmlns")))
			{
				indexMapping[indexMappingCount++] = i;
			}
		}
	}
	
	public String getValue()
	{
		return m_value;
	}
	
	public void setValue(String value)
	{
		m_value = (value != null) ? value : "";
	}	
	
	public int getNodeType()
	{
		return ELEMENT;
	}
	
	public int getAttributeCount()
	{
		return indexMappingCount;
	}
	
	
	public String getAttributeByIndex(int Index)
	{
		if (Index < 0 || Index >= indexMappingCount) return "";
		String res = m_attributes.getValue(indexMapping[Index]);
		return (res != null) ? res : "";
	}
	
	public String getAttributeByName(String name)
	{
		String res = m_attributes.getValue(name);
		return (res != null) ? res : "";
	}
	
	public int existsAttribute(String name)
	{
		return (m_attributes.getValue(name) != null)? 1 : 0;
	}

	public String getAttributeName(int Index)
	{
		if (Index < 0 || Index >= indexMappingCount) return "";
		String res = m_attributes.getQName(indexMapping[Index]);
		return (res != null) ? res : "";
	}

	public String getAttributePrefix(int Index)
	{
		int pos;
		String qname = getAttributeName(Index);
		if ((pos = qname.indexOf(":")) >= 0)
			return qname.substring(0, pos);
		else
			return "";
	}

	public String getAttributeLocalName(int Index)
	{
		if (Index < 0 || Index >= indexMappingCount) return "";
		String res = m_attributes.getLocalName(indexMapping[Index]);
		return (res != null) ? res : "";
	}

	public String getAttributeURI(int Index)
	{
		if (Index < 0 || Index >= indexMappingCount) return "";
		String res = m_attributes.getURI(indexMapping[Index]);
		return (res != null) ? res : "";
	}
	
	
	
/*	
@gusbro: -> Hay que implementarlo???
	public String getAttEntityValueByName(String name) 
	{
		int index = m_attributes.getIndex(name);
		if (index < 0) return "";
		String entity = m_attributes.getEntityName(index, 0);
		return (entity != null) ? entity : "";
	}
*/
	public String getAttEntityNotationByName(String name) 
	{
		return "";
	}

/*
@gusbro: -> Hay que implementarlo???
	public String getAttEntityValueByIndex(int Index)
   	{
		if (Index < 0 || Index >= indexMappingCount) return "";
		int count = m_attributes.getEntityCount(indexMapping[Index]);
		if (count == 0) return "";
		String entity = m_attributes.getEntityName(indexMapping[Index], 0);
		return (entity != null) ? entity : "";
   	}
*/

   	public String getAttEntityNotationByIndex(int index)
   	{	
		return "";	
   	}
	
}
