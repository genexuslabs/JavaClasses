package com.genexus.performance;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

public class ProceduresInfo
{
	static private Hashtable<String, ProcedureInfo> procedureInfo = new Hashtable<>();
	static private IApplication application;
	
  public ProceduresInfo()
  {
  }
  	
  static public void dump(PrintStream out)
  {
	  for (Enumeration en = procedureInfo.elements(); en.hasMoreElements(); )
	  {
			ProcedureInfo pInfo = (ProcedureInfo) en.nextElement();
			pInfo.dump(out);
			out.println("");
			out.println("");			
	  }
  } 
  
  static public ProcedureInfo addProcedureInfo(String name)
  {
	  if (!procedureInfo.containsKey(name))
	  {
		  ProcedureInfo pInfo = new ProcedureInfo(name);
		  procedureInfo.put(name, pInfo);
		  if (IApplication.staticIsJMXEnabled())
			ProcedureJMX.CreateProcedureJMX(pInfo);
	  }
	  return procedureInfo.get(name);
  }	
	
  static public ProcedureInfo getProcedureInfo(String name)
  {
	  return procedureInfo.get(name);
  }  
}
