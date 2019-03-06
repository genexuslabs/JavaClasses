package com.genexus;
import java.util.Vector;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.StringBuffer;

public class GXBCCollection<T extends GxSilentTrnSdt> extends GXBaseCollection<T> {

	public GXBCCollection()
	{
	}

	public GXBCCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace)
	{
		this(elementsType, elementsName, containedXmlNamespace, (int)-1);
	}

	public GXBCCollection(Class<T> elementsType, String elementsName, String containedXmlNamespace, int remoteHandle)
	{
		this.elementsType = elementsType;
		this.elementsName = elementsName;
		xmlElementsName = elementsName;
		this.containedXmlNamespace = containedXmlNamespace;
		this.remoteHandle = remoteHandle;
	}

	public GXBCCollection( Class<T> elementsType ,
			String elementsName ,
			String containedXmlNamespace ,
			Vector<T> data )
	{
		super(elementsType, elementsName, containedXmlNamespace, data);
	}

	public GXBCCollection( Class<T> elementsType ,
			String elementsName ,
			String containedXmlNamespace ,
			Vector<T> data ,
			int remoteHandle )
	{
		super(elementsType, elementsName, containedXmlNamespace, data, remoteHandle);
	}

	@Override
	public Vector getStruct()
	{
		Vector struct = new Vector();
		for (T Item : this)
		{
			try {
				struct.add(Item.getClass().getMethod("getStruct", new Class[]{}).invoke(Item, (Object[])null));
			}
			catch (  Exception e) {
				e.printStackTrace();
			}
		}
		return struct;
	}
		//-- Add de una collection de BC
	public boolean addElementTrn(Object item)
	{
		try
		{
			elementsType.getMethod(getMethodName(false, "Mode"), new Class[]{String.class}).invoke(item, new Object[]{"INS"});
			elementsType.getMethod(getMethodName(false, "Modified"), new Class[]{short.class}).invoke(item, new Object[]{new Short((short)1)});
		}catch(Exception e)
		{
			System.err.println("[addElementTrn]:" + e.toString());
			e.printStackTrace();
		}
		return super.add((T)item); //Vector add element
	}

	public byte removeElementTrn(double index)
	{
		if(index > 0 && index <= size())
		{
			Object element = item((int)index);
			try
			{
				if("INS".equals(elementsType.getMethod(getMethodName(true, "Mode"), new Class[]{}).invoke(element)))
				{
					this.remove(index); //GXBaseCollection.remove
				}
				else
				{
					elementsType.getMethod(getMethodName(false, "Mode"), new Class[]{String.class}).invoke(element, new Object[]{"DLT"});
				}
				return (byte)1;
			}catch(Exception e)
			{
				System.err.println(e.toString());
			}
		}
		return (byte)0;
	}

	public boolean insert(){
		boolean result = true;
		for (T Item : this)
		{
			result = Item.Insert() & result; //Try to do insert in all items regardless result value
		}
		return result;
	}
	public boolean update(){
		boolean result = true;
		for (T Item : this)
		{
			result = Item.Update() & result; //Try to do Update in all items regardless result value
		}
		return result;
	}
	public boolean insertOrUpdate(){
		boolean result = true;
		for (T Item : this)
		{
			result = Item.InsertOrUpdate() & result; //Try to do InsertOrUpdate in all items regardless result value
		}
		return result;
	}
	public boolean delete(){
		boolean result = true;
		for (T Item : this)
		{
			Item.Delete();
			result = Item.Success() & result; //Try to do Delete in all items regardless result value
		}
		return result;
	}     
	public boolean removeByKey(Object... key)
	{
		int index=0;
		for (T item : this)
		{
			if (isEqualComparedByKey(item, key))
			{
				this.removeElementTrn(index+1);
				return true;
			}
			index++;
		}
		return false;
	}
	public T getByKey(Object... key)
	{
		for (T item: this)
		{
			if(isEqualComparedByKey(item, key))
				return item;		
		}
		T item= null;
		try
		{
			item=elementsType.getConstructor(new Class[] { int.class }).newInstance(new Object[]{1});
		} 
		catch(Exception e)
		{
			System.err.println("GXBCCollection<" + elementsType.getName() + "> (getByKey): " + e.toString());
		}
		return item;
	}

	private boolean isEqualComparedByKey(T item, Object... key)
	{  
		Object[][] itemKey = item.GetBCKey();
		Object[] returnedKey = new Object[itemKey.length];
		Object[] parsedKey = new Object[key.length];
		for (int i = 0; i < itemKey.length; i++)
		{
			try
			{
				Class<?> c = item.getClass();
				String method = "getgxTv_" + c.getSimpleName() + "_" + toTitleCase(itemKey[i][0].toString());
				returnedKey[i]= elementsType.getMethod(method, new Class[]{}).invoke(item, (Object[])null);
				parsedKey[i] = key[i];

				if (returnedKey[i] instanceof String)
				{
					String rString = (String)returnedKey[i];
					returnedKey[i] = CommonUtil.rtrim(rString);
					String pString = (String)parsedKey[i];
					parsedKey[i] = CommonUtil.rtrim(pString);
				}
			}
			catch(Exception e)
			{
				System.err.println(e.toString());
			}
		}
		return Arrays.equals(returnedKey, parsedKey);
	}

	private String toTitleCase(String text)
	{
		if(text.length()<2)
			return text;
		else
			return new StringBuffer(text.length()).append(Character.toTitleCase(text.charAt(0))).append(text.substring(1).toLowerCase()).toString();
	}

}
