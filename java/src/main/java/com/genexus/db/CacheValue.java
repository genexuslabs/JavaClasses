package com.genexus.db;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;


public class CacheValue implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int hits;
	private Vector<CachedIFieldGetter> items;
	private long timestamp;
	private java.util.Date timeCreated;
	protected boolean isRemote = false;
	private TimeZone mTimeZone;

	private CacheKey key;

	private int expiryTime = 0;
	private int expiryHits = 0;
	private long cachedSize = 0;

	public CacheValue()
	{
	}	
	public CacheValue(String sentence, Object [] parms)
	{
		if(parms == null)
		{
			key = new CacheKey(sentence, null);
		}
		else
		{
			Object [] keyParms = new Object[parms.length];
			System.arraycopy(parms, 0, keyParms, 0, parms.length);
			key = new CacheKey(sentence, keyParms);
		}
		items = new Vector<CachedIFieldGetter>();
		
		cachedSize = sentence.length();
	}
	
	public CacheKey getKey()
	{
		return key;
	}
	
	/** Setea el tiempo de expiración (en segundos)
	 *  o 0 para indicar que no expira por tiempo
	 */
	public void setExpiryTime(int expiryTimeSeconds)
	{
		this.expiryTime = expiryTimeSeconds;
	}
	
	public long getExpiryTimeMilliseconds()
        {
		return expiryTime*1000;
	}
	public int getExpiryTimeSeconds()
	{
		return expiryTime;
	}
	
	/** Setea la cantidad de hits para expirar, o 0 si no
	 * expira por cantidad de hits
	 */	
	public void setExpiryHits(int expiryHits)
	{
		this.expiryHits = expiryHits;
	}
	
	public int getExpiryHits()
	{
		return expiryHits;
	}		
	
	/** Indica si este CacheValue ha expirado
	 */
	public boolean hasExpired()
	{
            return (expiryHits > 0 && hits >= expiryHits) || 
			   (expiryTime > 0 && (timestamp + (getExpiryTimeMilliseconds())) < System.currentTimeMillis());
	}
	
	private int []resultSetTypes;
	
	private void getResultSetTypes(Object [] resultSet)
	{
		resultSetTypes = new int[resultSet.length];
		for(int i = 0; i < resultSet.length; i++)
		{
			Class componentType = resultSet[i].getClass().getComponentType();
			resultSetTypes[i] = DynamicExecute.getPrimitiveType(componentType);
		}
	}
	
	public void setTimeZone(TimeZone cachedValueTimeZone) 
	{
		mTimeZone = cachedValueTimeZone;
	}
	
	protected void setIsRemote(boolean isRemote)
	{
		this.isRemote = isRemote;
	}

	@SuppressWarnings("unchecked")
	public <T> void addItem(T value)
	{
		if (value instanceof String)
		{
			cachedSize += value.toString().length();
		}
		if (value instanceof CachedIFieldGetter){

			CachedIFieldGetter cValue = (CachedIFieldGetter)value;
			cValue.setTimeZone(mTimeZone);
			items.addElement(cValue);
		}
		else{
			T[] arr = (T[]) java.lang.reflect.Array.newInstance(value.getClass(), 1);
			arr[0] = value;
			T[][] values = (T[][]) java.lang.reflect.Array.newInstance(arr.getClass(), 1);
			values[0] = arr;
			items.addElement(new CachedIFieldGetter(values));
		}
	}
	
	public void addItem(Object [] resultSet, long thisSize)
	{
		cachedSize += thisSize + 8 * resultSet.length;
		
		// @HACK
		// Tenemos que hacer un deep copy del resultSet
		// pero como usamos tipos primitivos no podemos hacer el arrayCopy
		// Lo que hacemos es en el primer add, obtenemos los tipos y luego
		// hacemos las copias
		if(resultSetTypes == null)
		{
			getResultSetTypes(resultSet);
		}		
		Object [] copy = (Object[])resultSet.clone();
		for(int i = 0; i < resultSet.length; i++)
		{
			switch(resultSetTypes[i])
			{
				case DynamicExecute.TYPE_BYTE: copy[i] = ((byte[])resultSet[i]).clone(); break;
				case DynamicExecute.TYPE_CHARACTER: copy[i] = ((char[])resultSet[i]).clone(); break;
				case DynamicExecute.TYPE_SHORT: copy[i] = ((short[])resultSet[i]).clone(); break;
				case DynamicExecute.TYPE_INTEGER: copy[i] = ((int[])resultSet[i]).clone(); break;
				case DynamicExecute.TYPE_LONG: copy[i] = ((long[])resultSet[i]).clone(); break;
				case DynamicExecute.TYPE_FLOAT: copy[i] = ((float[])resultSet[i]).clone(); break;
				case DynamicExecute.TYPE_DOUBLE: copy[i] = ((double[])resultSet[i]).clone(); break;
				case DynamicExecute.TYPE_BOOLEAN: copy[i] = ((boolean[])resultSet[i]).clone(); break;
				default: 
						copy[i] = ((Object[])resultSet[i]).clone();
						System.arraycopy(resultSet[i], 0, copy[i], 0, 1);
			}
		}
		CachedIFieldGetter cValue = new CachedIFieldGetter(copy);
		cValue.setTimeZone(mTimeZone);
		items.addElement(cValue);
	}
	
	public void setTimestamp()
	{
		timestamp = System.currentTimeMillis();
		setTimeCreated(new java.util.Date());
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}

	@JsonIgnore
	public Enumeration getIterator()
	{
		return items.elements();
	}
	
	protected void incHits()
	{
		hits++;
	}
	
	public int getHitCount()
	{
		return hits;
	}
	
	protected int getCantItems()
	{
		return items.size();
	}
	
	/** Retorna una estimación del 'tamaño' de este cacheValue
	 *  En 2 capas, el tamaño del CacheValue lo contamos como la cantidad de filas
	 *  multiplicado por la cantidad de columnas
	 *  En 3 capas, lo contamos como un decimo de la cantidad de bytes que ocupa
	 */
	public long getSize()
	{
		return cachedSize;
	}
	
	public java.util.Date getTimeCreated()
	{
		return timeCreated;
	}
	
	public void setTimeCreated(java.util.Date timeCreated)
	{
		this.timeCreated = timeCreated;
	}	
	
	
}
