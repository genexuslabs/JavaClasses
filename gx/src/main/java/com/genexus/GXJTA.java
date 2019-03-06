// $Log: GXJTA.java,v $
// Revision 1.6  2006/04/28 21:35:27  iroqueta
// El isJTATX no estaba considerando el caso de una aplic en 3 capas.
//
// Revision 1.5  2006/04/19 20:00:40  iroqueta
// Se usa JTA solo si se tiene definido mas de un DataStore
//
// Revision 1.4  2004/08/26 17:31:48  iroqueta
// Se controla de no trabajar con transacciones si se tiene la preference de integridad transaccional en No
//
// Revision 1.3  2004/06/25 13:51:34  iroqueta
// JTA - solo se usa si se utiliza el pool de los servidores J2EE
//
// Revision 1.2  2004/05/26 20:54:16  dmendez
// Se maneja correctamente las utls si no hay jta presente.
//
// Revision 1.1  2004/05/24 20:46:34  dmendez
// Soporte de JTA
//
package com.genexus;
import javax.transaction.UserTransaction;
import javax.naming.InitialContext;
import java.sql.SQLException;
import com.genexus.db.driver.GXDBDebug;
import com.genexus.util.*;
import com.genexus.db.Namespace;
import com.genexus.ModelContext;
import com.genexus.db.ServerConnectionManager;

public class GXJTA
{
  private static final boolean DEBUG = DebugFlag.DEBUG;
  static GXDBDebug debug;
  static Boolean isJTAavail;

  public static boolean isJTATX(int handle, ModelContext context)
  {
    if (isJTAavail == null)
    {
		Namespace nameSpace;
		if (ApplicationContext.getInstance().isApplicationServer())
		{
			nameSpace = ServerConnectionManager.getInstance().getUserInformation(handle).getNamespace();
		}
		else
		{
			nameSpace = Namespace.getNamespace(context.getNAME_SPACE());
		}
		
		if (nameSpace.getDataSourceCount() == 1 || nameSpace.iniFile.getProperty(nameSpace.getName(), "JTA", "0").equals("0"))
		{
			isJTAavail = new Boolean(false);
			return false;
		}
		
      boolean ret = false;
      try
      {
        ret = getUserTransaction() != null;
      } catch( Exception e) { ;}
      catch( NoClassDefFoundError e) { ;}
      isJTAavail = new Boolean(ret);
    }
    return isJTAavail.booleanValue();
  }

  private static UserTransaction getUserTransaction()
  {
    UserTransaction userTx = null;
    try {
      InitialContext jndiCtx = new InitialContext();
      userTx = (UserTransaction) jndiCtx.lookup("java:comp/UserTransaction");
    }
    catch( Exception e) {
      GXJTA.log("GXJTA::getUserTransaction " + e.getMessage());
    }
    return userTx;
  }

  static private void log( String message)
  {
    if (DEBUG)
      debug.log(GXDBDebug.LOG_MIN, message);
  }

  static private void beginTransaction() throws SQLException
  {
    UserTransaction userTx = getUserTransaction();
    try {
      if (userTx.getStatus() == javax.transaction.Status.STATUS_NO_TRANSACTION)
          userTx.begin();
    }
    catch ( Exception e) {
      GXJTA.log("GXJTA::beginTransaction " + e.getMessage());
      throw new SQLException( e.toString());
    }
  }

  static public void commit() throws SQLException
  {
    UserTransaction userTx = getUserTransaction();
    try {
      if ( userTx.getStatus() != javax.transaction.Status.STATUS_NO_TRANSACTION)
        userTx.commit();
    }
    catch ( Exception e) {
      GXJTA.log("GXJTA::commit " + e.getMessage());
      throw new SQLException( e.toString());
    }
    beginTransaction();
  }

  static public void rollback() throws SQLException
  {
    UserTransaction userTx = getUserTransaction();
    try {
      userTx.rollback();
    }catch( Exception e) {
      GXJTA.log("GXJTA::rollback " + e.getMessage());
      throw new SQLException( e.toString());
    }
    beginTransaction();
  }

  static public void initTX( GXDBDebug debug, boolean TransactionalIntegrity) throws SQLException
  {
    if (TransactionalIntegrity)
    {
      GXJTA.debug = debug;
      beginTransaction();
    }
  }

  static public void cleanTX(boolean TransactionalIntegrity) throws SQLException
  {
    if (TransactionalIntegrity)
    {
      UserTransaction userTx = getUserTransaction();
      try {
        if (userTx.getStatus() !=
            javax.transaction.Status.STATUS_NO_TRANSACTION)
          userTx.rollback();
      }
      catch (Exception e) {
        GXJTA.log("GXJTA::cleanTX " + e.getMessage());
        throw new SQLException(e.toString());
      }
    }
  }
}
