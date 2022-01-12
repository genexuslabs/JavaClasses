package com.genexus;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import com.genexus.db.Namespace;
import com.genexus.db.ServerConnectionManager;
import com.genexus.db.driver.GXDBDebug;

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
			isJTAavail = Boolean.valueOf(false);
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
