package com.genexus.performance;

import java.util.Date;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

public class SentenceJMX extends NotificationBroadcasterSupport implements SentenceJMXMBean{
	
	SentenceInfo sentenceInfo;
	
	private long sequenceNumber = 0;
	
  public SentenceJMX(DataStoreProviderInfo dsInfo, String name)
  {
	  sentenceInfo = dsInfo.getSentenceInfo(name);
	  sentenceInfo.sentenceJMX = this;
  }
  
  static public void CreateSentenceJMX(DataStoreProviderInfo dsInfo, String name)
  {
    try
    {
      MBeanUtils.createMBeanSentence(dsInfo, name);
    }
    catch(Exception e)
    {
      System.err.println("Cannot register Sentence MBean."+e.toString());
    }
  }
  
  public long getCount()
  {
	  return sentenceInfo.getSentenceCount();
  }
  
  public String getSQLStatement()
  {
	  return sentenceInfo.getSQLSentence();
  }
  
  public Date getLastExecute()
  {
	  return sentenceInfo.getTimeLastExecute();
  }
  
  public long getTotalTime()
  {
	  return sentenceInfo.getTotalTimeExecute();
  }
  
  public float getAverageTime()
  {
	  return sentenceInfo.getAverageTimeExecute();
  }
  
  public long getWorstTime()
  {
	  return sentenceInfo.getWorstTimeExecute();
  }
  
  public long getBestTime()
  {
	  return sentenceInfo.getBestTimeExecute();
  } 
  
  public long getBeforeNotificationWaitTime()
  {
	return sentenceInfo.getMaxTimeForNotification();
  }
	
  public void setBeforeNotificationWaitTime(long value)
  {
	sentenceInfo.setMaxTimeForNotification(value);
  }  
  
  //Notifications
	public boolean getNotificationEnabled()
	{
		return sentenceInfo.getEnableNotifications();
	}
	
	public void setNotificationEnabled(boolean value)
	{
		sentenceInfo.setEnableNotifications(value);
	}  
	
	public void SentencePoorPerformance()
	{
        Notification n = new Notification("com.genexus.performance.poorperformance",this,sequenceNumber++,System.currentTimeMillis(),"Poor performance in SQLStatement " + getSQLStatement()); 
 
		sendNotification(n);
	}
	
    public MBeanNotificationInfo[] getNotificationInfo() 
	{ 
        String[] types = new String[] {"com.genexus.performance.poorperformance"}; 
        String name = Notification.class.getName(); 
        String description = "Sentence with poor performance"; 
        MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description); 
		        
		return new MBeanNotificationInfo[] {info}; 
    }  
}
