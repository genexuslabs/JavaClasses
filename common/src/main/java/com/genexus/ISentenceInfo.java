package com.genexus;

import java.io.PrintStream;
import com.genexus.xml.XMLWriter;
import java.util.Date;

public interface ISentenceInfo {

	long getSentenceCount();
	void incSentenceCount();

	String getSQLSentence();

	Date getTimeLastExecute();

	long getTotalTimeExecute();

	float getAverageTimeExecute();

	long getWorstTimeExecute();

	long getBestTimeExecute();

	long getMaxTimeForNotification();
	void setMaxTimeForNotification(long value);

	boolean getEnableNotifications();
	void setEnableNotifications(boolean value);

	void dump(PrintStream out);
	void dump(XMLWriter writer);

	void setTimeExecute(long time);

	// Additional methods, if necessary, can be added here
}

