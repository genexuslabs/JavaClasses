package com.genexus;

import java.sql.SQLException;

import com.genexus.ModelContext;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.*;
import com.genexus.util.*;

public class ExecuteDirectSQL
{
	public static void execute(ModelContext context, int handle, String dataSource, String Statement)
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

	public static void executeWithThrow(ModelContext context, int handle, String dataSource, String Statement) throws SQLException
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