package com.genexus.internet;

import org.apache.http.conn.routing.HttpRoute;

public interface IConnectionObserver {
	void onConnectionCreated(HttpRoute route);
	void onConnectionDestroyed(HttpRoute route);
}

