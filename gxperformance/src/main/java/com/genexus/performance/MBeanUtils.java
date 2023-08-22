package com.genexus.performance;

import java.util.ArrayList;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

public class MBeanUtils {
	
	private static MBeanServer mbs = null;
	private static Vector<ObjectName> registeredObjects = new Vector<>();


	
  public MBeanUtils() {
  }
  
	private static MBeanServer getMBeanServer()		
	{
		if(mbs == null)
		{
			try
			{
				ArrayList list = MBeanServerFactory.findMBeanServer(null);
				if (list.size() > 0)
					mbs = (MBeanServer) list.get(0);
			}
			catch(Throwable t)
			{
				t.printStackTrace(System.out);
				System.exit(1);
			}
		}
		return mbs;
	}  

  public static void createMBeanDataStoreProviders(IDataStoreProviderInfo dataStoreProviderInfo)
  {
    MBeanServer mbs = getMBeanServer();
	if (mbs == null)
		return;	

    try
    {
      ObjectName name = new ObjectName("com.genexus.performance:type=DataStoreProviders");
	  registeredObjects.addElement(name);

      DataStoreProvidersJMX mbean = new DataStoreProvidersJMX(dataStoreProviderInfo);
	  
      mbs.registerMBean(mbean, name);
    }
    catch(javax.management.MalformedObjectNameException e)
    {
      System.out.println(e);
    }
    catch(javax.management.InstanceAlreadyExistsException e)
    {
      System.out.println(e);
    }
    catch(javax.management.MBeanRegistrationException e)
    {
      System.out.println(e);
    }
    catch(javax.management.NotCompliantMBeanException e)
    {
      System.out.println(e);
    }
  }  
  
  public static void createMBeanDataStoreProvider(String dsName, IDataStoreProviderInfo dataStoreProviderInfo)
  {
    MBeanServer mbs = getMBeanServer();
	if (mbs == null)
		return;	

    try
    {
      ObjectName name = new ObjectName("com.genexus.performance:type=DataStoreProviders.DataStoreProvider,name= " + dsName);
	  registeredObjects.addElement(name);

      DataStoreProviderJMX mbean = new DataStoreProviderJMX(dsName, dataStoreProviderInfo);
	  
      mbs.registerMBean(mbean, name);
    }
    catch(javax.management.MalformedObjectNameException e)
    {
      System.out.println(e);
    }
    catch(javax.management.InstanceAlreadyExistsException e)
    {
      System.out.println(e);
    }
    catch(javax.management.MBeanRegistrationException e)
    {
      System.out.println(e);
    }
    catch(javax.management.NotCompliantMBeanException e)
    {
      System.out.println(e);
    }
  }    
  
  public static void createMBeanSentence(DataStoreProviderInfo dsInfo, String sname)
  {
    MBeanServer mbs = getMBeanServer();
	if (mbs == null)
		return;	

    try
    {
      ObjectName name = new ObjectName("com.genexus.performance:type=DataStoreProviders.DataStoreProvider.SQLStatement,DataStoreProvider="+ dsInfo.getName() +",name= " + sname);
	  registeredObjects.addElement(name);

      SentenceJMX mbean = new SentenceJMX(dsInfo, sname);
	  
      mbs.registerMBean(mbean, name);
    }
    catch(javax.management.MalformedObjectNameException e)
    {
      System.out.println(e);
    }
    catch(javax.management.InstanceAlreadyExistsException e)
    {
      System.out.println(e);
    }
    catch(javax.management.MBeanRegistrationException e)
    {
      System.out.println(e);
    }
    catch(javax.management.NotCompliantMBeanException e)
    {
      System.out.println(e);
    }
  }  
  
  public static void createMBeanProcedure(ProcedureInfo pInfo)
  {
    MBeanServer mbs = getMBeanServer();
	if (mbs == null)
		return;	

    try
    {
      ObjectName name = new ObjectName("com.genexus.performance:type=Procedures,name= " + pInfo.getName());
	  registeredObjects.addElement(name);

      ProcedureJMX mbean = new ProcedureJMX(pInfo);
	  
      mbs.registerMBean(mbean, name);
    }
    catch(javax.management.MalformedObjectNameException e)
    {
      System.out.println(e);
    }
    catch(javax.management.InstanceAlreadyExistsException e)
    {
      System.out.println(e);
    }
    catch(javax.management.MBeanRegistrationException e)
    {
      System.out.println(e);
    }
    catch(javax.management.NotCompliantMBeanException e)
    {
      System.out.println(e);
    }
  }  
  
  public static void unregisterObjects()
  {
	  try
	  {
		for (int i = 0; i < registeredObjects.size(); i++)
		{
			mbs.unregisterMBean(registeredObjects.elementAt(i));
		}
	  }
	  catch(javax.management.InstanceNotFoundException e)
	  {
		System.out.println(e);
	  }
	  catch(javax.management.MBeanRegistrationException e)
	  {
		  System.out.println(e);
	  }
	  mbs = null;
  }
}
