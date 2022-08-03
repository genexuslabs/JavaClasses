package com.genexus;

import java.sql.SQLException;

import com.genexus.common.classes.AbstractGXConnection;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.*;
import com.genexus.util.*;

public class ExecuteDirectSQL
{
	public static void execute(ModelContext context, int handle, String dataSource, String Statement)
	{
		execute(context, handle, dataSource, Statement, null);
	}

	public static void execute(ModelContext context, int handle, String dataSource, String statement, IErrorHandler errorHandler)
	{						 
		try
		{
			SpecificImplementation.Application.executeStatement(context, handle, dataSource, statement);
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
				if (errorHandler == null)
				{
					SpecificImplementation.Application.GXLocalException(handle, "ExecuteDirectSQL/" + statement, ex);
				}
				else
				{
					try {
						AbstractGXConnection conn = new DefaultConnectionProvider().getConnection(context, handle, dataSource, true, true);
						SpecificImplementation.Application.handleSQLError(errorHandler, ex, context, handle, conn,  dataSource, new DirectStatement(statement));
					}
					catch (SQLException e) {
						throw new GXRuntimeException(e);
					}
				}

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