package com.genexus.performance;

import java.io.PrintStream;
import java.util.Date;

public class ProcedureInfo
{
	private long count;  
	private String name;
	private Date timeLastExecute;
	private long totalTimeExecute;
	private float averageTimeExecute;
	private long worstTimeExecute;
	private long bestTimeExecute;
	
	public ProcedureInfo(String name)
	{
		this.name = name;
	}	
	
	public long getCount()
	{
		return count;
	}
  
	public void incCount()
	{
		count ++;
		timeLastExecute = new Date();
	}	
	
	public String getName()
	{
		return name;
	}
	
	public Date getTimeLastExecute()
	{
		return timeLastExecute;
	}
	
	public long getTotalTimeExecute()
	{
		return totalTimeExecute;
	}
	
	public float getAverageTimeExecute()
	{
		return averageTimeExecute;
	}
	
	public long getWorstTimeExecute()
	{
		return worstTimeExecute;
	}
	
	public long getBestTimeExecute()
	{
		return bestTimeExecute;
	}
	
	public void dump(PrintStream out)
	{
		out.println("\t\tProcedure name : " + name);
		out.println("\t\tNumber of executions : " + count);
		out.println("\t\tLast time executed : " + timeLastExecute);
		out.println("\t\tTotal time : " + totalTimeExecute);		
		out.println("\t\tAverage time : " + averageTimeExecute);
		out.println("\t\tWorst time : " + worstTimeExecute);
		out.println("\t\tBest time : " + bestTimeExecute);						
	}
	
	public void setTimeExecute(long time)
	{
		totalTimeExecute = totalTimeExecute + time;
		averageTimeExecute = totalTimeExecute / count;
		if (time > worstTimeExecute)
			worstTimeExecute = time;
		if (time < bestTimeExecute || bestTimeExecute == 0)
			bestTimeExecute = time;
	}
 }