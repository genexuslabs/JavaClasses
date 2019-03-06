package com.genexus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.internet.IGxJSONAble;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.xml.GXXMLSerializer;
import com.genexus.xml.IXMLReader;
import com.genexus.xml.XMLWriter;

import json.org.json.IJsonFormattable;
import json.org.json.JSONArray;

public class GXBaseCollection<T extends GXXMLSerializable> extends GXSimpleCollection<T> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public GXBaseCollection()
	{
	}

	public GXBaseCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace)
	{
		this(elementsType, elementsName, containedXmlNamespace, (int)-1);
	}

	public GXBaseCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, int remoteHandle)
	{
		this(elementsType, elementsName, containedXmlNamespace, null, remoteHandle);
	}

	public GXBaseCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, Vector<T> data)
	{
		this(elementsType, elementsName, containedXmlNamespace, data, (int)-1);
	}

	public GXBaseCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, Vector<T> data, int remoteHandle)
	{

		this.elementsType = elementsType;
		this.elementsName = elementsName;
		xmlElementsName = elementsName;
		this.containedXmlNamespace = containedXmlNamespace;
		this.remoteHandle = remoteHandle;
	}
	@Override
	public boolean IsSimpleCollection()
	{
		return false;
	}
	private short readCollectionFromXML(IXMLReader reader)
	{
		removeAllItems();
		try
		{
			reader.read();
			short currError = reader.read();
			GXXMLSerializable obj;

			while( reader.getLocalName().equalsIgnoreCase(xmlElementsName) && currError > 0)
			{
				if (GxSilentTrnSdt.class.isAssignableFrom(elementsType))
				{
					obj = elementsType.getConstructor(new Class[] {int.class}).newInstance(new Object[] {Integer.valueOf(remoteHandle)});
				}
				else
				{
					obj = (GXXMLSerializable)elementsType.getConstructor().newInstance();
				}
				currError = (short)obj.readxml(reader);
				add((T)obj);
				reader.read();
			}
			return (byte)currError;
		}catch(Exception e)
		{
			System.err.println("GXBaseCollection<" + elementsType.getName() + "> (readxml): " + e.toString());
			e.printStackTrace();
			return -1;
		}
	}
	@Override
	public void writexmlcollection(XMLWriter writer, String name, String namespace, String itemName, String itemNamespace, boolean includeState)
	{
		try
		{
			if (!name.trim().equals(""))
			{
				writer.writeStartElement(name);
				if (!namespace.startsWith("[*:nosend]"))
					writer.writeAttribute("xmlns", namespace);
			}

			String itemName1 = xmlElementsName;
			if (!itemName.trim().equals(""))
			{
				itemName1 = itemName;
			}
			for( T currentElement: this)
			{
				currentElement.writexml(writer, itemName1, itemNamespace, includeState);
			}

			if (!name.trim().equals(""))
			{
				writer.writeEndElement();
			}
		}catch(Exception e)
		{
			System.err.println("GXBaseCollection<" + elementsType.getName() + "> (writexml): " + e.toString());
		}
	}

	@Override
	public short AddObjectInstance(IXMLReader reader)
	{
		try
		{
			short currError;

			T obj = elementsType.getConstructor().newInstance();
			add(obj);
			currError = obj.readxml(reader);
			return currError;
		}catch(Exception e)
		{
			System.err.println("GXBaseCollection<" + elementsType.getName() + "> (AddObjectInstance): " + e.toString());
			if(e instanceof InvocationTargetException)
			{
				System.err.println("Contained exception: " + ((InvocationTargetException)e).toString());
			}
			return -1;
		}
	}
    @Override
    public String toxml(boolean includeHeader, boolean includeState, String header, String namespace) {
		if(SpecificImplementation.Application.getProperty("SIMPLE_XML_SUPPORT", "0").equals("1")){
			try {
				Class me = getClass();
				Object struct = me.getMethod("getStruct", new Class[]{}).invoke(this, (Object[]) null);
				return GXXMLSerializer.serializeSimpleXml(includeHeader, SpecificImplementation.Application.createCollectionWrapper(struct), header, this.elementsName, this.containedXmlNamespace); //simplexml
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}else{
			return super.toxml(includeHeader, includeState, header, namespace);
		}
    }

    @Override
    public Vector getStruct() {
        Vector struct = new Vector();
        for (T Item : this) {
            try {
                struct.add(Item.getClass().getMethod("getStruct", new Class[]{}).invoke(Item, (Object[]) null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return struct;
    }

	@Override
	public boolean fromxml(String xml, GXBaseCollection<SdtMessages_Message> messages, String collName)
	{
		try{
			IXMLReader reader = SpecificImplementation.Application.createXMLReader();
			reader.openFromString(xml);
			short result;
			result = readCollectionFromXML(reader);
			reader.close();
			if (result <= 0) { 
				CommonUtil.ErrorToMessages(String.valueOf(reader.getErrCode()), reader.getErrDescription(), messages);
				return false;
			}
			else
				return true;
		}
		catch(Exception ex)
		{
			CommonUtil.ErrorToMessages("fromxml error", ex.getMessage(), messages);
			return false;
		}
	}

	@Override
	public boolean fromxml(String xml, String collName)
	{
		return fromxml(xml, null, collName);
	}

	protected String getMethodName(boolean isGet, String method)
	{
		String getName = elementsType.getName();
		int packageIndex = getName.lastIndexOf('.');
		if(packageIndex != -1)
		{
			getName = getName.substring(packageIndex+1);
		}
		return (isGet ? GET_METHOD_NAME : SET_METHOD_NAME) + getName + "_" + method;
	}

	//-- Add

	public boolean add(T item)
	{
		return super.add(item);
	}

	@Override
	public void FromJSONObject(IJsonFormattable obj)
	{
		this.clear();
		JSONArray jsonArr = (JSONArray)obj;
		for (int i = 0; i < jsonArr.length(); i++)
		{
			try
			{
				Object jsonObj = jsonArr.get(i);				
				Class[] parTypes = new Class[] {};
				Object[] arglist = new Object[] {};
				if (elementsType == null)
				{
					add((T) jsonObj);
				}
				else
				{
					Object currObj = jsonObj;
					if (elementsType.getSuperclass().getName().equals("com.genexus.GxSilentTrnSdt"))
					{
						parTypes = new Class[] {int.class};
						arglist = new Object[] {Integer.valueOf(remoteHandle)};
					}
					if (IGxJSONAble.class.isAssignableFrom(elementsType))
					{

						Constructor<T> constructor = elementsType.getConstructor(parTypes);
						currObj = constructor.newInstance(arglist);
						((IGxJSONAble)currObj).FromJSONObject((IJsonFormattable)jsonObj);
					}
					add((T)currObj);
				}
			}
			catch (Exception exc) {
			exc.printStackTrace();
			}
		}
	}
	public GXBaseCollection<T> Clone()
	{
		return (GXBaseCollection<T>)clone();
	}

}
