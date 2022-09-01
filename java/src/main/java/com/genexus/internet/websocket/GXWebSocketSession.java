package com.genexus.internet.websocket;

import com.genexus.websocket.ISession;

public class GXWebSocketSession{
	
	private String id;
	private ISession session;
	
	public String getId(){
		return id;
	}
	public GXWebSocketSession(ISession session){
		id = session.getId();
		this.session = session;
	}
	public ISession getSession()
	{
		return session;
	}
	public boolean equals(Object obj){
		return ((GXWebSocketSession)obj).session.equals(this.session);		
	}
}
