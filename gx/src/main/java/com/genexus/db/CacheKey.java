package com.genexus.db;

import java.io.Serializable;
import java.util.Date;

import com.genexus.CommonUtil;
import com.genexus.GXutil;

/** $Log: CacheKey.java,v $
/** Revision 1.3  2005/07/21 15:10:38  iroqueta
/** Implementacion de soporte de JMX
/**
/** Revision 1.2  2004/02/19 17:08:56  gusbro
/** .
/**
/** Revision 1.1  2004/02/10 16:41:09  gusbro
/** - Release inicial
/**
 */
@SuppressWarnings("serial")
public class CacheKey implements Serializable
{
	private String key;
	private Object []parms;

	public CacheKey()
	{
	}
	public CacheKey(String key, Object [] parms)
	{
		this.key = key;
		this.parms = parms;
	}
		
	public String getKey()
	{
		return key;
	}
	
	public Object [] getParameters()
	{
		return parms;
	}	
	
	public int hashCode()
	{
		return key.hashCode();
	}
	
	/** Este método retorna un hash sobre los parametros
	 * Sirve para que en el equals cuando se utiliza un
	 * DataStoreProvider remoto no se deba comparar todos
	 * los bytes del parametro, primero comparamos el hash y si 
	 * ya son distintos salimos.
	 */
	private Integer paramsHash = null;
	protected int paramsHashCode()
	{
		if(paramsHash == null)
		{ // Si es la primera vez que se llama a paramsHash, calculo el hash
			int hash = 0;
			if(parms != null)
			{
				for(int i = 0; i < parms.length; i++)
				{
					if(parms[i] instanceof byte[])
					{
						int len = ((byte[])parms[i]).length;
						byte [] temp = (byte[])parms[i];
						for(int j = 0; j < len; j++)
						{ 
							hash ^= temp[j];
							hash <<= 1;
						}
					}
					else
					{
						hash ^= parms[i].hashCode();
					}
				}
			}
			paramsHash = new Integer(hash);
		}
		return paramsHash.intValue();
	}
	
	private String stringKey = null;
	@Override
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		if(stringKey == null)
		{
			if(parms != null)
			{
				for(int i = 0; i < parms.length; i++)
				{
					if(parms[i] instanceof byte[])
					{
						
						int len = ((byte[])parms[i]).length;
						byte [] temp = (byte[])parms[i];
						for(int j = 0; j < len; j++)
						{ 
							str.append(Byte.toString(temp[j]));
						}
					}
					else if (parms[i] instanceof String)
					{
						str.append(parms[i]);
						str.append(((String)parms[i]).length());
					}
					else if (parms[i] instanceof Date)
					{
						str.append(GXutil.DateTimeToUTC((Date) parms[i]));
					}
					else
					{
						str.append(parms[i].toString());
					}
					str.append(',');
				}
			}
			str.append(key);
			stringKey = str.toString();
		}
		return stringKey;
	}

	public boolean equals(Object o)
	{
		if(!(o instanceof CacheKey))
		{
			return false;
		}
		CacheKey comp = (CacheKey)o;
		
		// Primero que nada comparamos con la key
		if(!key.equalsIgnoreCase(comp.key))
		{
			return false;
		}
		
		if(paramsHashCode() != comp.paramsHashCode())
		{ // Si el hash code de los parametros tampoco coincide
			return false;
		}
		
		// Ok, el hashcode coincidio, ahora debo ver si los parms son iguales
		if(parms == null)
		{ // Si la key no tiene parametros...
			return (comp.parms == null);
		}
		else
		{ // Si la key tiene parametros
			if(comp.parms == null || parms.length != comp.parms.length)
			{ // Chequeo que la cantidad de parametros sea la misma
				return false;
			}
		}
		return toString().equals(comp.toString());
	}		
}
