// $Log: DirectConnectionPool.java,v $
// Revision 1.11  2006/04/19 20:04:13  iroqueta
// Cuando se usa JTA el beginTransaction hay que hacerlo antes de obtener la conexion del pool, sino puede que en algunos casos (por ejemplo usando drivers de Oracle) no queden grabados los datos luego del commit (SAC 19965)
// Por eso saco el GXJTA.initTX de aca y lo pongo en el GXConnection antes de obtener la conexion del pool
//
// Revision 1.10  2005/02/18 21:19:27  iroqueta
// Le hago llegar el ModelContext al GXConnection para poder pasarselo al proc que se llama para obtener el nombre del datasource al cual conectarse.
//
// Revision 1.9  2004/11/15 19:50:22  iroqueta
// Se agregan controles el metodo disconnect para no dejar cosas sin cerrar.
//
// Revision 1.8  2004/09/16 18:14:07  iroqueta
// Agrego compilacion condicional para c# para que no moleste la implementacion de EJBs
//
// Revision 1.7  2004/09/16 18:08:34  iroqueta
// Agrego compilacion condicional para c# para que no moleste la implementacion de EJBs
//
// Revision 1.6  2004/09/09 18:44:02  iroqueta
// Se implement� el soporte para que las TRNs de los EJBs puedan ser manejadas por el contenedor.
//
// Revision 1.5  2004/08/26 17:32:47  iroqueta
// Se controla de no trabajar con transacciones si se tiene la preference de integridad transaccional en No y se usa JTA
//
// Revision 1.4  2004/06/25 14:44:06  iroqueta
// Arreglos JTA
//
// Revision 1.3  2004/06/25 13:51:34  iroqueta
// JTA - solo se usa si se utiliza el pool de los servidores J2EE
//
// Revision 1.2  2004/06/23 21:59:23  iroqueta
// Arreglo para JTA
//
// Revision 1.1  2004/02/13 20:20:38  gusbro
// - Release inicial
//

/**
* El NullConnectionPool no utiliza un pool de conexiones, sino que delega este trabajo
* al JDBCDataSource que existe
*
*/
package com.genexus.db.driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.genexus.ModelContext;

public final class DirectConnectionPool implements IConnectionPool
{
	private static final Enumeration enum1 = new Vector().elements();
	private DataSource dataSource;
	private Hashtable<Integer, GXConnection> connections = new Hashtable<Integer, GXConnection>();

	DirectConnectionPool(DataSource dataSource)
	{
		this.dataSource 	= dataSource;
	}

	public Enumeration getROPools()
	{
		return enum1;
	}

	public Enumeration getRWPools()
	{
		return enum1;
	}

	public ConnectionPool getROConnectionPool(String user)
	{
		return null;
	}

	public ConnectionPool getRWConnectionPool(String user)
	{
		return null;
	}

	public Connection checkOut(ModelContext context, DataSource dataSource1, int handle, String user, String password, boolean readOnly, boolean sticky) throws SQLException
	{
		Integer theHandle = new Integer(handle);
		GXConnection con = (GXConnection)connections.get(theHandle);
		if(con == null)
		{
			if (dataSource1 != null)
				con = new GXConnection(context, handle, user, password, dataSource1);
			else
				con = new GXConnection(context, handle, user, password, dataSource);
			connections.put(theHandle, con);
		}
		con.setHandle(handle);
		return con;
	}


	public void disconnectOnException(int handle) throws SQLException
	{
		disconnect(handle);
	}

	public void disconnect(int handle) throws SQLException
	{
		Integer theHandle = new Integer(handle);
		GXConnection con = (GXConnection)connections.remove(theHandle);
		if(con != null)
		{
			con.close();
		}
	}

	public void disconnect() throws SQLException
	{
               SQLException error = null;
		for (Enumeration<GXConnection> en = connections.elements(); en.hasMoreElements(); )
		{
                  try
                  {
                    ( (GXConnection) en.nextElement()).close();
                  }
                  catch(SQLException e)
                  {
                    error=e;
                  }
		}

		connections.clear();

                if (error!=null)
                  throw error;
	}

	@Override
	public void runWithLock(Runnable runnable) {
		runnable.run();
	}
	@Override
	public void removeElement(GXConnection con) {
	}
}
