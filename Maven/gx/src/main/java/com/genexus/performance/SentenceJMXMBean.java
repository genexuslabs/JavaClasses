package com.genexus.performance;

import java.util.*;

public interface SentenceJMXMBean
{
	long getCount();
	String getSQLStatement();
	Date getLastExecute();
	long getTotalTime();
	float getAverageTime();
	long getWorstTime();
	long getBestTime();
	boolean getNotificationEnabled();
	void setNotificationEnabled(boolean value);	
	long getBeforeNotificationWaitTime();
	void setBeforeNotificationWaitTime(long value);
}
