package com.genexus.websocket;

import java.io.IOException;

public interface ISession {
	String getQueryString();
	boolean isOpen();
	void sendEndPointText(String message) throws IOException;
}
