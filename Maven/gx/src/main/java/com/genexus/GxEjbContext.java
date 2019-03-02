package com.genexus;

import javax.ejb.SessionContext;

public class GxEjbContext {

  private SessionContext sc = null;
  private String userId;
  static boolean initialized = false;

  public GxEjbContext() {
  	if(!initialized)
  	{
  		ApplicationContext.getInstance().setEJBEngine(true);
  		initialized = true;
  	}
  }

  public SessionContext getSessionContext()
  {
    return sc;
  }

  public void setSessionContext(SessionContext sc)
  {
    this.sc = sc;
  }

  public void setRollback()
  {
    sc.setRollbackOnly();
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public String getUserId()
  {
    return userId;
  }

}
