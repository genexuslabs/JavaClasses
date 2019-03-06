package com.genexus;

import java.lang.reflect.*;
import java.security.MessageDigest;

import com.genexus.common.interfaces.SpecificImplementation;

abstract public class GxGenericCollectionItem<T>
{
	protected T sdt;

	public GxGenericCollectionItem()
	{
		SpecificImplementation.Application.init(getClass());
	}

	public GxGenericCollectionItem(T item)
	{
		SpecificImplementation.Application.init(getClass());
		sdt = item;
	}

	public T getSdt()
	{
		return sdt;
	}

	public String toString()
	{
		String result = "";
		Object paramsObj[] = {};
		Class classObj[] = {};
		Method method, zMethod;
		try
		{
			Method methlist[] = this.getClass().getMethods();
			java.util.TreeSet<String> sortMethods = new java.util.TreeSet<String>();
			for (int i = 0; i < methlist.length;i++) 
			{
				Method m = methlist[i];
				if (m.getName().startsWith("getgxTv_") && m.isAnnotationPresent(GxSeudo.class))
				{				
					sortMethods.add(m.getName());
				}
			}
			java.util.Iterator<String> iter = sortMethods.iterator();
			while(iter.hasNext()){
			    String methodName = iter.next();
			    zMethod = sdt.getClass().getMethod(methodName + "_Z", classObj);
			    method = this.getClass().getMethod(methodName, classObj);
			    if (zMethod!=null)
				result += zMethod.invoke(sdt, paramsObj).toString();
			    else
				result += method.invoke(this, paramsObj).toString();
			}
		}
		catch (Throwable e) 
		{
			System.out.println("Error in GxGenericCollectionItem toString method: " + e.toString());
		}
		return result;
	}
	
	private static final char[] HEXADECIMAL = { '0', '1', '2', '3','4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	public String getHash()
	{
		try
		{
			String str = toString();
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] thedigest = md5.digest(str.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(2 * thedigest.length);
			for (int i = 0; i < thedigest.length; i++) 
			{
				int low = (int)(thedigest[i] & 0x0f);
                int high = (int)((thedigest[i] & 0xf0) >> 4);
                sb.append(HEXADECIMAL[high]);
                sb.append(HEXADECIMAL[low]);
            }
			return sb.toString();
		}
		catch (Throwable e) 
		{
			System.out.println("Error in GxGenericCollectionItem getHash method: " + e.toString());
			return "";
		}
	}
}