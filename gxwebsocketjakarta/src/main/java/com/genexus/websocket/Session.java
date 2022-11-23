package com.genexus.websocket;

import jakarta.websocket.RemoteEndpoint;
import java.io.IOException;

public class Session implements ISession {
	private jakarta.websocket.Session session;

	public Session(jakarta.websocket.Session session) {
		this.session = session;
	}

	public Integer getHashCode() {
		return new Integer(session.hashCode());
	}

	public String getId() {
		return session.getQueryString();
	}

	public boolean isOpen() {
		return session.isOpen();
	}

	public void sendEndPointText(String message) throws IOException {
		RemoteEndpoint.Basic endPoint = session.getBasicRemote();
		endPoint.sendText(message);
	}
}
