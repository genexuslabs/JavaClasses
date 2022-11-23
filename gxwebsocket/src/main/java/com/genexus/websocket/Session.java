package com.genexus.websocket;

import javax.websocket.RemoteEndpoint;
import java.io.IOException;

public class Session implements ISession{
	private javax.websocket.Session session;

	public Session(javax.websocket.Session session) {
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
