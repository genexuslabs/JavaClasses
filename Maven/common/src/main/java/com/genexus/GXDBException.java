// $Log: GXDBException.java,v $
// Revision 1.1  1999/08/04 01:05:54  gusbro
// Initial revision
//
// Revision 1.1.1.1  1999/08/04 01:05:54  gusbro
// GeneXus Java Olimar
//
package com.genexus;

import com.genexus.common.classes.AbstractModelContext;
import com.genexus.common.interfaces.SpecificImplementation;

public class GXDBException extends Exception 
{
	int handle;

	public static String parseSQLException (java.sql.SQLException e)
	{
		return ("Text       : " + e.getMessage()   + "\n" +
			 	"Error Code : " + e.getErrorCode() + 
				" SQLState   : " + e.getSQLState());
	}
	
	public GXDBException(AbstractModelContext context, int handle, String message, Exception e)
	{
		this(context, handle, message + "\n" + e.getMessage());
	}

	public GXDBException(AbstractModelContext  context, int handle, String message, java.sql.SQLException e)
	{
		this(context, handle, message + "\n" + parseSQLException(e));
	}

	public GXDBException(AbstractModelContext  context, int handle, String message)
	{
		super(message);
		this.handle = handle;

		SpecificImplementation.Application.GXLocalException(context, handle, "", this);
	}

	public int getHandle()
	{
		return handle;
	}
}