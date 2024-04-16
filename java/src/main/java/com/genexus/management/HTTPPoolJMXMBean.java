package com.genexus.management;

public interface HTTPPoolJMXMBean {
	int getNumberOfConnectionsInUse();
	int getNumberOfRequestsWaiting();
	int getNumberOfAvailableConnections();
	int getMaxNumberOfConnections();
}