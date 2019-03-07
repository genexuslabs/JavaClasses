package com.genexus.db;

import com.genexus.GXParameterPacker;
import com.genexus.GXParameterUnpacker;

/**
 * This stores the state of a single cursor. It's used in the 'client side' when executing
 * in 3 tiers.
 *
 * @version	1.0, 15/11/00
 * @author	Andres Aguiar
 * @since	Solis
 */

public class RemoteCursor 
{
	int status;

	Object[] buffers;
	int[] resultsTypes;
	int[] parmsTypes;
	GXParameterPacker packer = new GXParameterPacker();
	GXParameterUnpacker unpacker = new GXParameterUnpacker();

	boolean currentOf = false;
	boolean wasUsed = false;

	public RemoteCursor()
	{
		this(new Object[0], new int[0], new int[0]);
	}

	public RemoteCursor(Object[] buffers, int[] parmsTypes, int[] resultsTypes)
	{
		this(buffers, parmsTypes, resultsTypes, false);
	}

	public RemoteCursor(Object[] buffers, int[] parmsTypes, int[] resultsTypes, boolean currentOf)
	{
		this.buffers = buffers;
		this.parmsTypes = parmsTypes;
		this.resultsTypes = resultsTypes;
		this.currentOf = currentOf;
		
	}
/*
	public void setParameters(Object[] parms)
	{
		if	(parmsSet)
			return;

		parmsSet = true;
		for (int i = 0; i < parms.length; i++)
		{
			if	(parms[i] instanceof Byte)
			{
				paramTypes[i] = BYTE;
			}
			else if (parms[i] instanceof Short)
			{
				paramTypes[i] = SHORT;
			}
			else if (parms[i] instanceof Integer)
			{
				paramTypes[i] = INT;
			}
			else if (parms[i] instanceof Long)
			{
				paramTypes[i] = LONG;
			}
			else if (parms[i] instanceof Float)
			{
				paramTypes[i] = FLOAT;
			}
			else if (parms[i] instanceof Double)
			{
				paramTypes[i] = DOUBLE;
			}
			else if (parms[i] instanceof java.util.Date)
			{
				paramTypes[i] = DATE;
			}
			else if (parms[i] instanceof String)
			{
				paramTypes[i] = STRING;
			}
			else
			{
				System.err.println("Unrecognized parm");
			}
		}
	}

	public void setOutputBuffers(Object[] output)
	{
		this.buffers = output;

		paramTypes = new int[output.length];

		for (int i = 0 ; i < output.length; i++)
		{
			if	(output[i] instanceof byte[])
			{
				paramTypes[i] = BYTE;
			}
			else if (output[i] instanceof short[])
			{
				paramTypes[i] = SHORT;
			}
			else if (output[i] instanceof int[])
			{
				paramTypes[i] = INT;
			}
			else if (output[i] instanceof long[])
			{
				paramTypes[i] = LONG;
			}
			else if (output[i] instanceof float[])
			{
				paramTypes[i] = FLOAT;
			}
			else if (output[i] instanceof double[])
			{
				paramTypes[i] = DOUBLE;
			}
			else if (output[i] instanceof java.util.Date[])
			{
				paramTypes[i] = DATE;
			}
			else if (output[i] instanceof String[])
			{
				paramTypes[i] = STRING;
			}
			else
			{
				System.err.println("Unrecognized parm " + output[i].getClass());
			}
		}
	}
*/

}
