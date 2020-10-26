package com.genexus.db;
					   
import com.genexus.IHttpContext;
import com.genexus.ModelContext;

/**
 * This is the base class for the generated set of 'foreachs' for one datastore
 * in one GX object. It's just an utility class, to avoid to generate the code for this
 * methods if they have the default values.
 *
 * @version	1.0, 15/11/00
 * @author	Andres Aguiar
 * @since	Solis
 */
import com.genexus.db.IFieldGetter;
import java.sql.SQLException;
public abstract class DataStoreHelperBase 
{
	public com.genexus.LocalUtil localUtil = com.genexus.Application.getClientLocalUtil();
	
	public String Gx_ope ;
	public String Gx_etb ;	
	
	public static final int GX_NOMASK		 =  0;
	public static final int GX_MASKLOCKERR   =  1;
	public static final int GX_MASKNOTFOUND  =  2;
	public static final int GX_MASKDUPKEY    =  4;
	public static final int GX_MASKOBJEXIST  =  8;
	public static final int GX_MASKLOOPLOCK  = 16;
	public static final int GX_MASKFOREIGNKEY = 32;
	public static final int GX_ROLLBACKSAVEPOINT = CommonDataStoreHelperBase.GX_ROLLBACKSAVEPOINT;
	static final String AND = " and ";
	static final String WHERE = " WHERE ";

	/**
     * Returns the default connection provider. This method will be overriden if the
     * object is going to run inside a container that provides another way to get the
     * connection (ie inside Oracle).
     */
	public IConnectionProvider getConnectionProvider()
	{
		return new DefaultConnectionProvider();
	}

	/**
     * This indicates if the connection to the datastore should be read-only or not. All
	 * the foreachs in the object/datastore should use the same connection pool (this is
	 * a change from 7.0)
     * @return true if it should use a read-only connection, false if it should use a read-write connection
     */
	public boolean needsReadOnlyConnection()
	{
		return false;
	}

	/**
     * Returns the data store name. The default implementation returns the default name.
     */
	public String getDataStoreName()
	{
		return "DEFAULT";
	}
	
	public Object[] getDynamicStatement( int cursor ,  ModelContext context, int remoteHandle, IHttpContext httpContext, Object [] dynConstraints )
	{
		return null;
	}

	public void getErrorResults(int cursor, IFieldGetter rslt, Object[] buf) throws SQLException
	{ 
	}
	
	public void setParametersRT(int cursor, IFieldSetter stmt, Object[] buffers)
	{
	}

	public StringBuffer addWhere(StringBuffer currentWhere, String condition)
	{
		if (currentWhere.length() > 0)
			currentWhere.append(AND);
		else
			currentWhere.append(WHERE);
		return currentWhere.append(condition);
	}

}
