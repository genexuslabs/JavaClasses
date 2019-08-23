package com.genexus;
import java.util.*;
import com.genexus.xml.GXXMLSerializable;

public class GXExternalCollection<T extends GXXMLSerializable> extends GXBaseCollection<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected AbstractCollection vectorExternal = new Vector();
	public GXExternalCollection()
	{
		vectorExternal = new Vector();
	}

	public GXExternalCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace)
	{
		this(elementsType, elementsName, containedXmlNamespace, new Vector(), (int)-1);
	}

	public GXExternalCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, int remoteHandle)
	{
		this(elementsType, elementsName, containedXmlNamespace, new Vector(), remoteHandle);
	}	

	public GXExternalCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, Vector data)
	{
		this(elementsType, elementsName, containedXmlNamespace, data, (int)-1);
	}

	public GXExternalCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, Vector data, int remoteHandle)
	{
		this.elementsType = elementsType;
	}	

	public boolean IsSimpleCollection()
	{
		return false;
	}

	//-- Add

	public boolean add(T item)
	{
		return add(item, 0);
	}

	@SuppressWarnings("unchecked")
	public boolean add(T item, int index)
	{
		super.add(item);
		try
		{
			vectorExternal.add(elementsType.getMethod("getExternalInstance", new Class[]{}).invoke(item));
			return true;
		}
		catch(Exception e)
		{
			System.err.println("[addElement]:" + e.toString());
			return false;
		}
	}

	public void setExternalStruct(AbstractCollection data)
	{
		vectorExternal = data;
	}

	@SuppressWarnings("unchecked")
	public Vector getStruct() 
	{
		Vector struct = new Vector();
		for (T Item : this)
		{
			try
			{
				struct.add(Item.getClass().getMethod("getExternalInstance", new Class[]{}).invoke(Item));
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return struct;
	}
	
}

