package com.genexus;

import java.io.PrintStream;
import java.util.Date;

public interface IProcedureInfo {
	// Getter methods
	long getCount();
	String getName();
	Date getTimeLastExecute();
	long getTotalTimeExecute();
	float getAverageTimeExecute();
	long getWorstTimeExecute();
	long getBestTimeExecute();

	// Other methods
	void incCount();
	void dump(PrintStream out);
	void setTimeExecute(long time);
}
