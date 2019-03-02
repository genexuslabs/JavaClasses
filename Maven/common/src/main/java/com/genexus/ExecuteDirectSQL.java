// $Log: ExecuteDirectSQL.java,v $
// Revision 1.2  2005/10/20 13:24:28  iroqueta
// Hago llegar el contexto al getconnection para poder implementar bien los metodos before y after connection
//
// Revision 1.1  2001/08/07 17:07:24  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/08/07 17:07:24  gusbro
// GeneXus Java Olimar
//
//
//   Rev 1.10   23 Sep 1998 19:48:32   AAGUIAR
//
//   Rev 1.7   May 29 1998 10:04:54   AAGUIAR
//	-	Se cambiaron las lecturas de preferences, para
//		adaptarlas al nuevo modelo.
//
//   Rev 1.6   May 28 1998 10:02:16   AAGUIAR
//Sincro28May1998

package com.genexus;

import java.sql.SQLException;

import com.genexus.common.classes.AbstractModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.*;
import com.genexus.util.*;

public class ExecuteDirectSQL
{
	public static void execute(AbstractModelContext context, int handle, String dataSource, String Statement)
	{						 
		try
		{
			SpecificImplementation.Application.executeStatement(context, handle, dataSource, Statement);
		}
		catch (SQLException ex)
		{
			if (SpecificImplementation.Application.handlSQLException(handle, dataSource, ex))
				return;
			
			if (ApplicationContext.getInstance().getReorganization())
			{
				ex.printStackTrace();
				ReorgSubmitThreadPool.setAnError();
			}
			else
			{
				SpecificImplementation.Application.GXLocalException(handle,  "ExecuteDirectSQL/" + Statement, ex); 
			}
		}
	}

	public static void executeWithThrow(AbstractModelContext context, int handle, String dataSource, String Statement) throws SQLException
	{
		try
		{
			if (ApplicationContext.getInstance().getReorganization())
			{
				if (!SpecificImplementation.Application.executedBefore(Statement))
				{
					SpecificImplementation.Application.executeStatement(context, handle, dataSource, Statement);
					SpecificImplementation.Application.addExecutedStatement(Statement);
				}
			}
			else
			{
				SpecificImplementation.Application.executeStatement(context, handle, dataSource, Statement);
			}
		}
		catch (SQLException ex)
		{
			//if (Application.getConnectionManager(context).getDataSource(handle, dataSource).dbms.ObjectNotFound(ex))
			//	return;
			
			throw ex;
		}
	}	
}