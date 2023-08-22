package com.genexus.performance;

import java.io.PrintStream;
import java.util.Date;

public class SentenceInfo
{
	private long sentenceCount;  
	private String sqlSentence;
	private Date timeLastExecute;
	private long totalTimeExecute;
	private float averageTimeExecute;
	private long worstTimeExecute;
	private long bestTimeExecute;
	private long maxTimeForNotification = 10000;
	private boolean enableNotifications = true;
	private IApplication application;
	public SentenceJMX sentenceJMX = null;
	
	public SentenceInfo(String sqlSentence, IApplication application)
	{
		this.sqlSentence = sqlSentence;
		this.application = application;
	}	
	
	public long getSentenceCount()
	{
		return sentenceCount;
	}
  
	public void incSentenceCount()
	{
		sentenceCount ++;
		timeLastExecute = new Date();
	}	
	
	public String getSQLSentence()
	{
		return sqlSentence;
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
	
	public long getMaxTimeForNotification()
	{
		return maxTimeForNotification;
	}
	
	public void setMaxTimeForNotification(long value)
	{
		maxTimeForNotification = value;
	}	
	
	public boolean getEnableNotifications()
	{
		return enableNotifications;
	}
	
	public void setEnableNotifications(boolean value)
	{
		enableNotifications = value;
	}		
	
	public void dump(PrintStream out)
	{
		out.println("\t\tSQL sentence : " + sqlSentence);
		out.println("\t\tNumber of executions : " + sentenceCount);
		out.println("\t\tLast time executed : " + timeLastExecute);
		out.println("\t\tTotal time : " + totalTimeExecute);		
		out.println("\t\tAverage time : " + averageTimeExecute);
		out.println("\t\tWorst time : " + worstTimeExecute);
		out.println("\t\tBest time : " + bestTimeExecute);						
	}
	
	public void dump(com.genexus.xml.XMLWriter writer)
	{
		writer.writeStartElement("SQLStatement");
		writer.writeStartElement("SQLStatement");
			writer.writeCData(sqlSentence);
		writer.writeEndElement();
		writer.writeElement("Count",sentenceCount);
		writer.writeElement("LastExecute",timeLastExecute.toString());
		writer.writeElement("TotalTime",totalTimeExecute);
		writer.writeElement("AverageTime",averageTimeExecute);
		writer.writeElement("WorstTime",worstTimeExecute);
		writer.writeElement("BestTime",bestTimeExecute);
		writer.writeEndElement();
	}
	
	public void setTimeExecute(long time)
	{
		if (this.application.isJMXEnabled())
			if (time > maxTimeForNotification && enableNotifications)
			{
					sentenceJMX.SentencePoorPerformance();
			}
		totalTimeExecute = totalTimeExecute + time;
		averageTimeExecute = totalTimeExecute / sentenceCount;
		if (time > worstTimeExecute)
			worstTimeExecute = time;
		if (time < bestTimeExecute || bestTimeExecute == 0)
			bestTimeExecute = time;
	}
 }