package com.genexus.performance;

import java.util.Date;

public interface ProcedureJMXMBean
{
	long getCount();
	Date getLastExecute();
	long getTotalTime();
	float getAverageTime();
	long getWorstTime();
	long getBestTime();
}
