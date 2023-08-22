package com.genexus.performance;

import java.util.Date;

public class ProcedureJMX implements ProcedureJMXMBean{
	
	ProcedureInfo procedureInfo;
	
	public ProcedureJMX(ProcedureInfo pInfo)
	{
	  procedureInfo = pInfo;
	}
  
  static public void CreateProcedureJMX(ProcedureInfo pInfo)
  {
    try
    {
      MBeanUtils.createMBeanProcedure(pInfo);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register Procedure MBean."+e.toString());
    }
  }
  
  public long getCount()
  {
	  return procedureInfo.getCount();
  }
    
  public Date getLastExecute()
  {
	  return procedureInfo.getTimeLastExecute();
  }
  
  public long getTotalTime()
  {
	  return procedureInfo.getTotalTimeExecute();
  }
  
  public float getAverageTime()
  {
	  return procedureInfo.getAverageTimeExecute();
  }
  
  public long getWorstTime()
  {
	  return procedureInfo.getWorstTimeExecute();
  }
  
  public long getBestTime()
  {
	  return procedureInfo.getBestTimeExecute();
  }  
}
