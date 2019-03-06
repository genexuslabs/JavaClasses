package com.genexus.management;

import java.util.*;

public interface ConnectionJMXMBean
{
	int getId();
	String getPhysicalId();
	Date getCreateTime();
	Date getLastAssignedTime();	
	int getLastUserAssigned();
	boolean getError();
	//boolean getInAssigment();
	boolean getAvailable();
	int getOpenCursorCount();
	boolean getUncommitedChanges();
	int getRequestCount();
	Date getLastSQLStatementTime();
	String getLastSQLStatement();
	String getLastObject();
	boolean getLastSQLStatementEnded();
	
	void disconnect();
	void dumpConnectionInformation();
}
