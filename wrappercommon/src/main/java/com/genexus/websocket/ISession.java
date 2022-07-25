package com.genexus.websocket;

import java.io.IOException;

public interface ISession {
	Integer getHashCode();
	String getId();
	boolean isOpen();
	void sendEndPointText(String message) throws IOException;
}
