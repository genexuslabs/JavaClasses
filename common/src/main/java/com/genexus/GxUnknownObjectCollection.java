package com.genexus;

import java.util.*;
import java.lang.reflect.*;
import com.genexus.xml.*;
				  
public class GxUnknownObjectCollection extends GXSimpleCollection
{	
	public GxUnknownObjectCollection()
	{
	}
	public void writexmlcollection(XMLWriter writer, String name, String namespace, String itemName, String itemNamespace)
	{
		writexmlcollection(writer, name, namespace, itemName, itemNamespace, true);
	}	
	public void writexmlcollection(XMLWriter writer, String name, String namespace, String itemName, String itemNamespace, boolean includeState)
	{
		try
		{
			writer.writeStartElement(name);

			for(Enumeration enumera = this.elements(); enumera.hasMoreElements();)
			{
				Object currentElement = enumera.nextElement();
				((GXXMLSerializable)currentElement).writexml(writer, "", "", includeState);
			}		
			writer.writeEndElement();
		}
		catch(Exception e)
		{
			System.err.println(e.toString());
		}
	}
	
	public short readxml(IXMLReader reader, String sName)
	{
		return 0;
	}
	
	public short readxmlcollection( IXMLReader oReader, String sName, String itemName )
	{	
		return 0;
	}
	
	public String toJson()
	{
		return toJSonString();
	}	
}