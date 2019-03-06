package com.genexus.management;

import com.genexus.db.Namespace;

public class NamespaceJMX implements NamespaceJMXMBean{
	
	private Namespace namespace;
	

  public NamespaceJMX(Namespace namespace)
  {
    this.namespace = namespace;
  }

  static public void CreateNamespaceJMX(Namespace namespace)
  {
    try
    {
      MBeanUtils.createMBean(namespace);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register Namespace MBean."+e.toString());
    }
  }

  public int getUserCount()
  {
	  return namespace.getUserCounts();
  }
 
}
