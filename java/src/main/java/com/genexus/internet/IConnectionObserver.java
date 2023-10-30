package com.genexus.internet;


public interface IConnectionObserver {
	void onConnectionCreated(IdentifiableHttpRoute route);
	void onConnectionDestroyed(IdentifiableHttpRoute route);
}